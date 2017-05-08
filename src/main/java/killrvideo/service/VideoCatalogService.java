package killrvideo.service;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static killrvideo.utils.ExceptionUtils.mergeStackTrace;
import static com.datastax.driver.mapping.Mapper.Option.*;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.BuiltStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.Result;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.*;
import com.sun.org.apache.bcel.internal.generic.LOOKUPSWITCH;
import killrvideo.entity.*;
import killrvideo.utils.FutureUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.core.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import killrvideo.events.CassandraMutationError;
import killrvideo.common.CommonTypes.Uuid;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;
import killrvideo.video_catalog.VideoCatalogServiceGrpc.AbstractVideoCatalogService;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*;
import killrvideo.video_catalog.events.VideoCatalogEvents.UploadedVideoAccepted;
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded;

@Service
public class VideoCatalogService extends AbstractVideoCatalogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoCatalogService.class);

    public static final int MAX_DAYS_IN_PAST_FOR_LATEST_VIDEOS = 7;
    public static final int LATEST_VIDEOS_TTL_SECONDS = MAX_DAYS_IN_PAST_FOR_LATEST_VIDEOS * 24 * 3600;
    public static final Pattern PARSE_LATEST_PAGING_STATE = Pattern.compile("((?:[0-9]{8}_){7}[0-9]{8}),([0-9]),(.*)");

    @Inject
    Mapper<Video> videoMapper;

    @Inject
    Mapper<UserVideos> userVideosMapper;

    @Inject
    Mapper<LatestVideos> latestVideosMapper;

    @Inject
    MappingManager manager;

    @Inject
    EventBus eventBus;

    @Inject
    ExecutorService executorService;

    @Inject
    KillrVideoInputValidator validator;

    private Session session;
    private String latestVideosTableName;
    private String userVideosTableName;
    private PreparedStatement latestVideoPreview_startingPointPrepared;
    private PreparedStatement latestVideoPreview_noStartingPointPrepared;
    private PreparedStatement userVideoPreview_startingPointPrepared;
    private PreparedStatement userVideoPreview_noStartingPointPrepared;

    @PostConstruct
    public void init(){
        this.session = manager.getSession();

        /**
         * Set the following up in PostConstruct because 1) we have to
         * wait until after dependency injection for these to work,
         * and 2) we only want to load the prepared statements once at
         * the start of the service.  From here the prepared statements should
         * be cached on our Cassandra nodes.
         */

        // Prepared statements for getLatestVideoPreviews()
        latestVideosTableName = latestVideosMapper.getTableMetadata().getName();
        latestVideoPreview_startingPointPrepared = session.prepare(
                "" +
                        "SELECT * " +
                        "FROM " + Schema.KEYSPACE + "." + latestVideosTableName + " " +
                        "WHERE yyyymmdd = :ymd " +
                        "AND (added_date, videoid) <= (:ad, :vid)"
        );

        latestVideoPreview_noStartingPointPrepared = session.prepare(
                "" +
                        "SELECT * " +
                        "FROM " + Schema.KEYSPACE + "." + latestVideosTableName + " " +
                        "WHERE yyyymmdd = :ymd "
        );

        // Prepared statements for getUserVideoPreviews()
        userVideosTableName = userVideosMapper.getTableMetadata().getName();
        userVideoPreview_startingPointPrepared = session.prepare(
                "" +
                        "SELECT * " +
                        "FROM " + Schema.KEYSPACE + "." + userVideosTableName + " " +
                        "WHERE userid = :uid " +
                        "AND (added_date, videoid) <= (:ad, :vid)"
        );

        userVideoPreview_noStartingPointPrepared = session.prepare(
                "" +
                        "SELECT * " +
                        "FROM " + Schema.KEYSPACE + "." + userVideosTableName + " " +
                        "WHERE userid = :uid "
        );
    }

    @Override
    public void submitYouTubeVideo(SubmitYouTubeVideoRequest request, StreamObserver<SubmitYouTubeVideoResponse> responseObserver) {

        LOGGER.debug("-----Start submitting youtube video-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final Date now = new Date();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        final String yyyyMMdd = dateFormat.format(now);
        final String location = request.getYouTubeVideoId();
        final String previewImageLocation = "//img.youtube.com/vi/"+ location + "/hqdefault.jpg";
        final UUID videoId = UUID.fromString(request.getVideoId().getValue());
        final UUID userId = UUID.fromString(request.getUserId().getValue());

        final Statement s1 = videoMapper
                .saveQuery(new Video(videoId, userId, request.getName(), request.getDescription(), location,
                        VideoLocationType.YOUTUBE.ordinal(), previewImageLocation, Sets.newHashSet(request.getTagsList().iterator()), now));

        final Statement s2 = userVideosMapper
                .saveQuery(new UserVideos(userId, videoId, request.getName(), previewImageLocation, now));

        final Statement s3 = latestVideosMapper
                .saveQuery(new LatestVideos(yyyyMMdd, userId, videoId, request.getName(), previewImageLocation, now)
                        ,ttl(LATEST_VIDEOS_TTL_SECONDS));

        /**
         * Logged batch insert for automatic retry
         */
        final BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);
        batchStatement.add(s1);
        batchStatement.add(s2);
        batchStatement.add(s3);
        batchStatement.setDefaultTimestamp(now.getTime());

        ResultSetFuture batchResultsFuture = session.executeAsync(batchStatement);
        FutureUtils.buildCompletableFuture(batchResultsFuture)
                .handle((rs, ex) -> {
                    if (rs != null) {
                        /**
                         * See class {@link VideoAddedHandlers} for the impl
                         */
                        final YouTubeVideoAdded.Builder youTubeVideoAdded = YouTubeVideoAdded.newBuilder()
                                .setAddedDate(TypeConverter.dateToTimestamp(now))
                                .setDescription(request.getDescription())
                                .setLocation(location)
                                .setName(request.getName())
                                .setPreviewImageLocation(previewImageLocation)
                                .setTimestamp(TypeConverter.dateToTimestamp(now))
                                .setUserId(request.getUserId())
                                .setVideoId(request.getVideoId());
                        youTubeVideoAdded.addAllTags(Sets.newHashSet(request.getTagsList()));

                        //:TODO figure out if this is linked to handle() in VideoAddedHandlders
                        eventBus.post(youTubeVideoAdded.build());

                        responseObserver.onNext(SubmitYouTubeVideoResponse.newBuilder().build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End submitting youtube video");

                    } else if (ex != null) {

                        LOGGER.error("Exception submitting youtube video : " + mergeStackTrace(ex));

                        eventBus.post(new CassandraMutationError(request, ex));
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());

                    }
                    return rs;
                });
    }

    @Override
    public void getVideo(GetVideoRequest request, StreamObserver<GetVideoResponse> responseObserver) {

        LOGGER.debug("-----Start getting video-----");
        LOGGER.debug("Request is: " + request.toString());

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());

        // videoId matches the partition key set in the Video class
        //:TODO getQuery does more than simply generate the query and causes a transaction.  This is not true async and I must replace with async call.
        //Statement videoQuery = videoMapper.getQuery(videoId);

        //:TODO notice that Olivier had me put the videoMapper.getAsync call directly into the callback...don't forget that
        //:TODO a call to getQuery still produces a prepared statement and that needs to be handled aync otherwise it will block
        //ResultSetFuture resultsFuture = session.executeAsync(videoQuery);
        FutureUtils.buildCompletableFuture(videoMapper.getAsync(videoId))
                .handle((video, ex) -> {
                    //Video video = videoMapper.map(videoResult).one();

                    if (video != null) {
                        LOGGER.debug("Video is: " + (video.getName()));
                        responseObserver.onNext((video.toVideoResponse()));
                        responseObserver.onCompleted();
                        LOGGER.debug("End getting video");

                    } else if (video == null) {
                        LOGGER.warn("Video with id " + videoId + " was not found");
                        responseObserver.onError(Status.NOT_FOUND
                                .withDescription("Video with id " + videoId + " was not found").asRuntimeException());

                    } else if (ex != null) {
                        LOGGER.error("Exception getting video : " + mergeStackTrace(ex));
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());

                    }
                    return video;
                });
    }

    @Override
    public void getVideoPreviews(GetVideoPreviewsRequest request, StreamObserver<GetVideoPreviewsResponse> responseObserver) {

        LOGGER.debug("-----Start getting video preview-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final GetVideoPreviewsResponse.Builder builder = GetVideoPreviewsResponse.newBuilder();

        if (request.getVideoIdsCount() == 0 || request.getVideoIdsList() == null) {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();

            LOGGER.warn("No video id provided for video preview");

            return;
        }

        /**
         * Fire a list of async SELECT, one for each video id
         */
//        final List<CompletableFuture<Video>> listFuture = request
//                .getVideoIdsList()
//                .stream()
//                .map(uuid -> UUID.fromString(uuid.getValue()))
//                .map(uuid -> videoManager.crud().findById(uuid).getAsync())
//                .collect(toList());

        /**
         * Merge all the async SELECT results
         */
//        CompletableFuture
//                .allOf(listFuture.toArray(new CompletableFuture[listFuture.size()]))
//                .thenApply(v -> listFuture.stream().map(CompletableFuture::join).collect(toList()))
//                .handle((list,ex) -> {
//                    if (list != null) {
//                        list.stream()
//                                .filter(x -> x != null)
//                                .forEach(entity -> builder.addVideoPreviews(entity.toVideoPreview()));
//
//                        responseObserver.onNext(builder.build());
//                        responseObserver.onCompleted();
//
//                        LOGGER.debug("End getting video preview");
//
//                    } else if (ex != null) {
//
//                        LOGGER.error("Exception getting video preview : " + mergeStackTrace(ex));
//
//                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
//
//                    }
//                    return list;
//                });

//        LOGGER.debug("videoIdsList is: " + request.getVideoIdsList().toString());
//        LOGGER.debug("videoIdsList is: " + request.getVideoIdsList().get(0).getValue());
//        List<UUID> ids = new ArrayList<>(0);
//        ids.add(UUID.fromString("d4ae7bef-8b5a-4342-b4c5-caa2f71e15e1"));

        try {
//            final List<CompletableFuture<Video>> listFuture = ids
//                    .stream()
//                    .map(uuid -> FutureUtils.buildCompletableFuture(videoMapper.getAsync(uuid)))
//                    .collect(toList());

        final List<CompletableFuture<Video>> listFuture = request
                .getVideoIdsList()
                .stream()
                .map(uuid -> UUID.fromString(uuid.getValue()))
                .map(uuid -> FutureUtils.buildCompletableFuture(videoMapper.getAsync(uuid)))
                .collect(toList());

            CompletableFuture
                    .allOf(listFuture.toArray(new CompletableFuture[listFuture.size()]))
                    .thenApply(v -> listFuture.stream().map(CompletableFuture::join).collect(toList()))
                    .handle((list, ex) -> {
                        if (list != null) {
                            list.stream()
                                    .filter(x -> x != null)
                                    .forEach(entity -> builder.addVideoPreviews(entity.toVideoPreview()));

                            responseObserver.onNext(builder.build());
                            responseObserver.onCompleted();

                            LOGGER.debug("End getting video preview");

                        } else if (ex != null) {

                            LOGGER.error("Exception getting video preview : " + mergeStackTrace(ex));

                            responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());

                        }
                        return list;
                    });

        } catch (Exception ex) {
            LOGGER.error("Exception getting video preview : " + mergeStackTrace(ex));

            responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
        }
//        BuiltStatement bs = QueryBuilder
//                .select().all()
//                .from(Schema.KEYSPACE,"users")
//                .where(QueryBuilder.in("userid",userIds));
//
//        ResultSetFuture future = session.executeAsync(bs);

        /*BuiltStatement bs = QueryBuilder
                .select().all()
                .from(Schema.KEYSPACE,"users")
                .where(QueryBuilder.in("userid",userIds));

        ResultSetFuture future = session.executeAsync(bs);*/

//        Futures.addCallback(listFuture,
//                new FutureCallback<ResultSet>() {
//                    @Override
//                    public void onSuccess(@Nullable ResultSet result) {
//                        Result<Video> videos = videoMapper.map(result);
//                        videos.forEach(video -> builder.addVideoPreviews(video.toVideoPreview()));
//
//                        responseObserver.onNext(builder.build());
//                        responseObserver.onCompleted();
//
//                        LOGGER.debug("End getting user profile");
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        LOGGER.error("Exception getting user profile : " + mergeStackTrace(t));
//
//                        responseObserver.onError(Status.INTERNAL.withCause(t).asRuntimeException());
//                    }
//                }
//                //MoreExecutors.sameThreadExecutor()
//        );
    }

    /**
     * In this method, we craft our own paging state. The custom paging state format is:
     * <br/>
     * <br/>
     * <code>
     * yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd_yyyyMMdd,&lt;index&gt;,&lt;Cassandra paging state as string&gt;
     * </code>
     * <br/>
     * <br/>
     * <ul>
     *     <li>The first field is the date of 7 days in the past, starting from <strong>now</strong></li>
     *     <li>The second field is the index in this date list, to know at which day in the past we stop at the previous query</li>
     *     <li>The last field is the serialized form of the native Cassandra paging state</li>
     * </ul>
     *
     * On the first query, we create our own custom paging state in the server by computing the list of 8 days
     * in the past, the <strong>index</strong> is set to 0 and there is no native Cassandra paging state
     *
     * <br/>
     * <br/>
     *
     * On subsequent request, we decode the custom paging state coming from the web app and resume querying from
     * the appropriate date and we inject also the native Cassandra paging state.
     *
     * <br/>
     * <br/>
     *
     * <strong>However, we can only use the native Cassandra paging state for the 1st query in the for loop. Indeed
     * Cassandra paging state is a hash of query string and bound values. We may switch partition to move one day
     * back in the past to fetch more results so the paging state will no longer be usable</strong>
     *
     *
     */
    @Override
    public void getLatestVideoPreviews(GetLatestVideoPreviewsRequest request, StreamObserver<GetLatestVideoPreviewsResponse> responseObserver) {

        LOGGER.debug("-----Start getting latest video preview-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        /**
         * TupleValue here contains a tuple with 3 elements (List<String>, Integer, String)
         */
        final TupleValue tuple3 = parseCustomPagingState(Optional.ofNullable(request.getPagingState()))
                .orElseGet(this.buildFirstCustomPagingState());

        final List<String> buckets = tuple3.getList(0, new TypeToken<String>() {});
        int bucketIndex = tuple3.getInt(1);
        final String rowPagingState = tuple3.getString(2);
        LOGGER.debug("Tuple is: buckets: " + buckets.size() + " index: " + bucketIndex + " state: " + rowPagingState);

        final Optional<Date> startingAddedDate = Optional
                .ofNullable(request.getStartingAddedDate())
                //.filter(x -> StringUtils.isNotBlank(x.toString()))
                .map(x -> Instant.ofEpochSecond(x.getSeconds(), x.getNanos()))
                .map(Date::from);

        final Optional<UUID> startingVideoId = Optional
                .ofNullable(request.getStartingVideoId())
                .filter(x -> StringUtils.isNotBlank(x.toString()))
                .map(x -> x.getValue())
                .filter(StringUtils::isNotBlank)
                .map(UUID::fromString);

        final List<VideoPreview> results = new ArrayList<>();
        String nextPageState = "";

        /**
         * Boolean to know if the native Cassandra paging
         * state has been used
         */
        final AtomicBoolean cassandraPagingStateUsed = new AtomicBoolean(false);

        try {
            while (bucketIndex < buckets.size()) {
                int recordsStillNeeded = request.getPageSize() - results.size();
                final String yyyyMMdd = buckets.get(bucketIndex);

                final Optional<String> pagingStateString =
                        Optional.ofNullable(rowPagingState)
                                .filter(StringUtils::isNotBlank)
                                .filter(pg -> !cassandraPagingStateUsed.get());

                ResultSetFuture future;
                ResultSet futureResults;
                BoundStatement bound;

                /**
                 * If startingAddedDate and startingVideoId are provided,
                 * we do NOT use the paging state
                 */
                if (startingAddedDate.isPresent() && startingVideoId.isPresent()) {
                    /**
                     * The startingPointPrepared statement can be found at the top
                     * of the class within PostConstruct
                     */
                    bound = latestVideoPreview_startingPointPrepared.bind()
                            .setString("ymd", yyyyMMdd)
                            //.setString("ymd", "20170427")
                            .setTimestamp("ad", startingAddedDate.get())
                            .setUUID("vid", startingVideoId.get());

                    bound
                            .setFetchSize(recordsStillNeeded);

//                    future = session.executeAsync(bound);
//                    futureResults = future.getUninterruptibly();

                    LOGGER.debug("Current query is: " + bound.preparedStatement().getQueryString());

                } else {
                    /**
                     * The noStartingPointPrepared statement can be found at the top
                     * of the class within PostConstruct
                     */
                    bound = latestVideoPreview_noStartingPointPrepared.bind()
                            .setString("ymd", yyyyMMdd);

                    bound
                            .setFetchSize(recordsStillNeeded);

//                    /**
//                     * Not entirely sure why DuyHai used getUninterruptibly within his
//                     * getListWithStats() call from Achilles, but I copied it to ensure
//                     * I replicated the same functionality.  Must get clarification on this.
//                     */
//                    future = session.executeAsync(bound);
//                    futureResults = future.getUninterruptibly();
//
//                    cassandraPagingStateUsed.compareAndSet(false, true);

                    LOGGER.debug("Current query is: " + bound.preparedStatement().getQueryString());
                }

                //:TODO Figure out more streamlined way to do this with Optional and java 8 lambda
                if (pagingStateString.isPresent()) {
                    bound.setPagingState(PagingState.fromString(pagingStateString.get()));
                    cassandraPagingStateUsed.compareAndSet(false, true);
                }

                /**
                 * Not entirely sure why DuyHai used getUninterruptibly within his
                 * getListWithStats() call from Achilles, but I copied it to ensure
                 * I replicated the same functionality.  Must get clarification on this.
                 */
                future = session.executeAsync(bound);
                //:TODO Find a way to do this properly in an async fashion, in talking to Olivier
                //:TODO there is a way to do it, but it is more complicated.  ControlConnection
                futureResults = future.getUninterruptibly();

                //Result<LatestVideos> videos = latestVideosMapper.map(Uninterruptibles.getUninterruptibly(future));
                Result<LatestVideos> videos = latestVideosMapper.map(futureResults);
                results.addAll(videos.all()
                        .stream()
                        .map(LatestVideos::toVideoPreview)
                        .collect(toList()));

                final ExecutionInfo executionInfo = videos.getExecutionInfo();

                // See if we can stop querying
                if (results.size() >= request.getPageSize()) {
                    final PagingState pagingState = executionInfo.getPagingState();

                    // Are there more rows in the current bucket?
                    if (pagingState != null) {
                        // Start from where we left off in this bucket if we get the next page
                        nextPageState = createPagingState(buckets, bucketIndex, pagingState.toString());

                    } else if (bucketIndex != buckets.size() - 1) {
                        // Start from the beginning of the next bucket since we're out of rows in this one
                        nextPageState = createPagingState(buckets, bucketIndex + 1, "");
                    }
                    break;
                }

                LOGGER.debug("------------------" +
                        " buckets: " + buckets.size() +
                        " index: " + bucketIndex +
                        " state: " + rowPagingState +
                        " results size: " + results.size() +
                        " request pageSize: " + request.getPageSize()
                );
                bucketIndex++;
            }

            responseObserver.onNext(GetLatestVideoPreviewsResponse
                    .newBuilder()
                    .addAllVideoPreviews(results)
                    .setPagingState(nextPageState).build());
            responseObserver.onCompleted();

        } catch (Throwable throwable) {
            LOGGER.error("Exception when getting latest preview videos : " + mergeStackTrace(throwable));

            responseObserver.onError(Status.INTERNAL.withCause(throwable).asRuntimeException());
        }
        LOGGER.debug("End getting latest video preview");
    }


    @Override
    public void getUserVideoPreviews(GetUserVideoPreviewsRequest request, StreamObserver<GetUserVideoPreviewsResponse> responseObserver) {

        LOGGER.debug("-----Start getting user video preview-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final UUID userId = UUID.fromString(request.getUserId().getValue());
        final Optional<UUID> startingVideoId = Optional
                .ofNullable(request.getStartingVideoId())
                .map(Uuid::getValue)
                .filter(StringUtils::isNotBlank)
                .map(UUID::fromString);

        final Optional<Date> startingAddedDate = Optional
                .ofNullable(request.getStartingAddedDate())
                .map(ts -> Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()))
                .map(Date::from);

        //final CompletableFuture<Tuple2<List<UserVideos>, ExecutionInfo>> listAsync;
        final Optional<String> pagingStateString = Optional.ofNullable(request.getPagingState()).filter(StringUtils::isNotBlank);
        ResultSetFuture future;
        BoundStatement bound;

        /**
         * If startingAddedDate and startingVideoId are provided,
         * we do NOT use the paging state
         */
        if (startingVideoId.isPresent() && startingAddedDate.isPresent()) {
            /**
             * The startingPointPrepared statement can be found at the top
             * of the class within PostConstruct
             */
            bound = userVideoPreview_startingPointPrepared.bind()
                    .setUUID("uid", userId)
                    .setTimestamp("ad", startingAddedDate.get())
                    .setUUID("vid", startingVideoId.get());

            bound
                    .setFetchSize(request.getPageSize());

            LOGGER.debug("Current query is: " + bound.preparedStatement().getQueryString());

        } else {
            /**
             * The noStartingPointPrepared statement can be found at the top
             * of the class within PostConstruct
             */
            bound = userVideoPreview_noStartingPointPrepared.bind()
                    .setUUID("uid", userId);

            bound
                    .setFetchSize(request.getPageSize());

            LOGGER.debug("Current query is: " + bound.preparedStatement().getQueryString());
        }

        //:TODO Figure out more streamlined way to do this with Optional and java 8 lambda
        if (pagingStateString.isPresent()) {
            bound.setPagingState(PagingState.fromString(pagingStateString.get()));
        }
        future = session.executeAsync(bound);

        FutureUtils.buildCompletableFuture(userVideosMapper.mapAsync(future))
                .handle((userVideos, ex) -> {
                    try {

                        if (userVideos != null) {
                            final GetUserVideoPreviewsResponse.Builder builder = GetUserVideoPreviewsResponse.newBuilder();

                            int remaining = userVideos.getAvailableWithoutFetching();
                            for (UserVideos userVideo : userVideos) {
                                builder.addVideoPreviews(userVideo.toVideoPreview());
                                builder.setUserId(request.getUserId());

                                if (--remaining == 0) {
                                    break;
                                }
                            }

                            Optional.ofNullable(userVideos.getExecutionInfo().getPagingState())
                                    .map(PagingState::toString)
                                    .ifPresent(builder::setPagingState);
                            responseObserver.onNext(builder.build());
                            responseObserver.onCompleted();

                            LOGGER.debug("End getting user video preview");

                        } else if (ex != null) {
                            LOGGER.error("Exception getting user video preview : " + mergeStackTrace(ex));

                            responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());

                        }

                    } catch (Exception e) {
                        LOGGER.error("Exception CATCH getting user video preview : " + mergeStackTrace(e));

                        responseObserver.onError(Status.INTERNAL.withCause(e).asRuntimeException());
                    }
                    return userVideos;

                });
    }


    /**
     * Create a paging state string from the passed in parameters
     * @param buckets
     * @param bucketIndex
     * @param rowsPagingState
     * @return String
     */
    private String createPagingState(List<String> buckets, int bucketIndex, String rowsPagingState) {
        StringJoiner joiner = new StringJoiner("_");
        buckets.forEach(joiner::add);
        return joiner.toString() + "," + bucketIndex + "," + rowsPagingState;
    }


    /**
     * Parse the passed in paging state and return a
     * TupleValue that essentially acts as a
     * tuple with 3 elements (List<String>, Integer, String) as Optional.
     * @param customPagingState
     * @return Optional
     */
    private Optional<TupleValue> parseCustomPagingState(Optional<String> customPagingState) {
        return customPagingState
                .map(pagingState -> {
                    Matcher matcher = PARSE_LATEST_PAGING_STATE.matcher(pagingState);
                    if (matcher.matches()) {
                        final List<String> buckets = Lists.newArrayList(matcher.group(1).split("_"));
                        final int currentBucket = Integer.parseInt(matcher.group(2));
                        final String cassandraPagingState = matcher.group(3);
                        TupleType tuple3 = session.getCluster().getMetadata()
                                .newTupleType(DataType.list(DataType.text()), DataType.cint(), DataType.text());
                        return tuple3.newValue(buckets, currentBucket, cassandraPagingState);
                    } else {
                        return null;
                    }
                });
    }


    /**
     * Build the first paging state if one does not already exist
     * and return a TupleValue that essentially acts as a
     * tuple with 3 elements (List<String>, Integer, String) as Supplier.
     * @return TupleValue
     */
    //:TODO a tuple may not be the best way to do this as we are not using it as intended.  Take a look at Patrick's modified
    //:TODO killrvideo schema for tuple/UDT examples
    private Supplier<TupleValue> buildFirstCustomPagingState() {
        return () -> {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            final ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
            final List<String> buckets = LongStream.rangeClosed(0L, 7L).boxed()
                    .map(now::minusDays)
                    .map(x -> x.format(formatter))
                    .collect(Collectors.toList());
            TupleType tuple3 = session.getCluster().getMetadata()
                    .newTupleType(DataType.list(DataType.text()), DataType.cint(), DataType.text());
            return tuple3.newValue(buckets, 0, null);
        };
    }
}