package killrvideo.service;

//import static info.archinnov.achilles.internals.futures.FutureUtils.toCompletableFuture;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static killrvideo.utils.ExceptionUtils.mergeStackTrace;
import static com.datastax.driver.mapping.Mapper.Option.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

import com.datastax.driver.core.querybuilder.BuiltStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.Result;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.sun.org.apache.bcel.internal.generic.LOOKUPSWITCH;
import info.archinnov.achilles.type.tuples.Tuple;
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

//import info.archinnov.achilles.generated.manager.LatestVideos_Manager;
//import info.archinnov.achilles.generated.manager.UserVideos_Manager;
//import info.archinnov.achilles.generated.manager.Video_Manager;
//import info.archinnov.achilles.type.tuples.Tuple2;
//import info.archinnov.achilles.type.tuples.Tuple3;
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

    //:TODO Fix this
    /*
    @Inject
    Video_Manager videoManager;

    @Inject
    UserVideos_Manager userVideosManager;

    @Inject
    LatestVideos_Manager latestVideosManager;
    */
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

    Session session;

    //:TODO Fix this
    /*
    @PostConstruct
    public void init(){
        this.session = videoManager.getNativeSession();
    }
    */

    @Override
    public void submitUploadedVideo(SubmitUploadedVideoRequest request, StreamObserver<SubmitUploadedVideoResponse> responseObserver) {

        LOGGER.debug("-----Start submitting uploaded video-----");

        //:TODO Fix this
//        if (!validator.isValid(request, responseObserver)) {
//            return;
//        }
//
//        final Date now = new Date();
//
//        final UUID videoId = UUID.fromString(request.getVideoId().getValue());
//        final UUID userId = UUID.fromString(request.getUserId().getValue());
//
//        final BoundStatement bs1 = videoManager
//                .crud()
//                .insert(new Video(videoId, userId, request.getName(), request.getDescription(),
//                        VideoLocationType.UPLOAD.ordinal(), Sets.newHashSet(request.getTagsList().iterator()), now))
//                .generateAndGetBoundStatement();
//
//        final BoundStatement bs2 = userVideosManager
//                .crud()
//                .insert(new UserVideos(userId, videoId, request.getName(), now))
//                .generateAndGetBoundStatement();
//
//        /**
//         * Logged batch insert for automatic retry
//         */
//        final BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);
//        batchStatement.add(bs1);
//        batchStatement.add(bs2);
//        batchStatement.setDefaultTimestamp(now.getTime());
//
//        toCompletableFuture(session.executeAsync(batchStatement), executorService)
//                .handle((rs,ex) -> {
//                    if (rs != null) {
//
//                        /**
//                         * Right now there is no defined event handler for
//                         * UploadedVideoAccepted event. To be implemented
//                         */
//                        eventBus.post(UploadedVideoAccepted
//                                .newBuilder()
//                                .setVideoId(request.getVideoId())
//                                .setUploadUrl(request.getUploadUrl())
//                                .setTimestamp(TypeConverter.dateToTimestamp(now))
//                                .build());
//                        responseObserver.onNext(SubmitUploadedVideoResponse.newBuilder().build());
//                        responseObserver.onCompleted();
//
//                        LOGGER.debug("End submitting uploaded video");
//
//                    } else if (ex != null) {
//
//                        LOGGER.error("Exception submitting uploaded video : " + mergeStackTrace(ex));
//
//                        eventBus.post(new CassandraMutationError(request, ex));
//                        responseObserver.onError(ex);
//                    }
//                    return rs;
//                });
    }

    @Override
    public void submitYouTubeVideo(SubmitYouTubeVideoRequest request, StreamObserver<SubmitYouTubeVideoResponse> responseObserver) {

        LOGGER.debug("-----Start submitting youtube video-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        final String yyyyMMdd = dateFormat.format(now);
        final String location = request.getYouTubeVideoId();
        final String previewImageLocation = "//img.youtube.com/vi/"+ location + "/hqdefault.jpg";
        final UUID videoId = UUID.fromString(request.getVideoId().getValue());
        final UUID userId = UUID.fromString(request.getUserId().getValue());

        /*final BoundStatement bs1 = videoManager
                .crud()
                .insert(new Video(videoId, userId, request.getName(), request.getDescription(), location,
                        VideoLocationType.YOUTUBE.ordinal(), previewImageLocation, Sets.newHashSet(request.getTagsList().iterator()), now))
                .generateAndGetBoundStatement();

        final BoundStatement bs2 = userVideosManager
                .crud()
                .insert(new UserVideos(userId, videoId, request.getName(), previewImageLocation, now))
                .generateAndGetBoundStatement();

        final BoundStatement bs3 = latestVideosManager
                .crud()
                .insert(new LatestVideos(yyyyMMdd, userId, videoId, request.getName(), previewImageLocation, now))
                .usingTimeToLive(LATEST_VIDEOS_TTL_SECONDS)
                .generateAndGetBoundStatement();*/

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

        ResultSetFuture batchResultsFuture = manager.getSession().executeAsync(batchStatement);
        FutureUtils.buildCompletableFuture(batchResultsFuture)
        //toCompletableFuture(session.executeAsync(batchStatement), executorService)
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
        Statement videoQuery = videoMapper.getQuery(videoId);
        ResultSetFuture resultsFuture = manager.getSession().executeAsync(videoQuery);
        FutureUtils.buildCompletableFuture(resultsFuture)
                .handle((entity, ex) -> {
                    if (entity != null) {
                        //:TODO Have no idea if I can cast the response this way, check this later after more is hooked up
                        LOGGER.debug("Video is: " + ((Video) entity).getName());
                        responseObserver.onNext(((Video) entity).toVideoResponse());
                        responseObserver.onCompleted();
                        LOGGER.debug("End getting video");

                    } else if (entity == null) {
                        LOGGER.warn("Video with id " + videoId + " was not found");
                        responseObserver.onError(Status.NOT_FOUND
                                .withDescription("Video with id " + videoId + " was not found").asRuntimeException());

                    } else if (ex != null) {
                        LOGGER.error("Exception getting video : " + mergeStackTrace(ex));
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());

                    }
                    return entity;
                });
    }

    @Override
    public void getVideoPreviews(GetVideoPreviewsRequest request, StreamObserver<GetVideoPreviewsResponse> responseObserver) {

        LOGGER.debug("-----Start getting video preview-----");

        if (!validator.isValid(request, responseObserver)) {
            LOGGER.debug("Video request IS VALID");
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
        /* final List<CompletableFuture<Video>> listFuture = request
                .getVideoIdsList()
                .stream()
                .map(uuid -> UUID.fromString(uuid.getValue()))
                .map(uuid -> videoManager.crud().findById(uuid).getAsync())
                .collect(toList()); */

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

        LOGGER.debug("videoIdsList is: " + request.getVideoIdsList().toString());

        final List<ListenableFuture<Video>> listFuture = request
                .getVideoIdsList()
                .stream()
                .map(uuid -> UUID.fromString(uuid.getValue()))
                .map(uuid -> videoMapper.getAsync(uuid))
                .collect(toList());

//        BuiltStatement bs = QueryBuilder
//                .select().all()
//                .from(Schema.KEYSPACE,"users")
//                .where(QueryBuilder.in("userid",userIds));
//
//        ResultSetFuture future = manager.getSession().executeAsync(bs);

        /*BuiltStatement bs = QueryBuilder
                .select().all()
                .from(Schema.KEYSPACE,"users")
                .where(QueryBuilder.in("userid",userIds));

        ResultSetFuture future = manager.getSession().executeAsync(bs);*/

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
            LOGGER.debug("Video request IS NOT VALID");
            return;
        }

//        final Tuple3<List<String>, Integer, String> tuple3 = parseCustomPagingState(Optional.ofNullable(request.getPagingState()))
//                .orElseGet(this.buildFirstCustomPagingState());
        final TupleValue tuple3 = parseCustomPagingState(Optional.ofNullable(request.getPagingState()))
                .orElseGet(this.buildFirstCustomPagingState());

        LOGGER.debug("Paging state is: " + request.getPagingState());

        List<String> buckets = tuple3.getList(0, new TypeToken<String>() {});
        int bucketIndex = tuple3.getInt(1);
        String rowPagingState = tuple3.getString(2);
        LOGGER.debug("Tuple is: buckets: " + buckets.size() + " index: " + bucketIndex + " state: " + rowPagingState);

        final Optional<Date> startingAddedDate = Optional
                .ofNullable(request.getStartingAddedDate())
                .filter(x -> StringUtils.isNotBlank(x.toString()))
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


        LOGGER.debug("BLOCK BLOCK BLOCK");

        /**
         * Boolean to know if the native Cassandra paging
         * state has been used
         */
        final AtomicBoolean cassandraPagingStateUsed = new AtomicBoolean(false);

        try {
            while (bucketIndex < buckets.size()) {

                int recordsStillNeeded = request.getPageSize() - results.size();
                final String yyyyMMdd = buckets.get(bucketIndex);
                //final Tuple2<List<LatestVideos>, ExecutionInfo> resultWithPagingState;
                TupleType resultWithPagingState = manager.getSession().getCluster().getMetadata()
                        .newTupleType(
                                DataType.list(DataType.custom("killrvideo.entity.LatestVideos")),
                                DataType.list(DataType.custom("ExecutionInfo"))
                        );

                final Optional<String> pagingStateString =
                        Optional.ofNullable(rowPagingState)
                                .filter(StringUtils::isNotBlank)
                                .filter(pg -> !cassandraPagingStateUsed.get());

                BuiltStatement statement;
                ResultSetFuture future = null;
                ResultSet resultsTest = null;

                /**
                 * If startingAddedDate and startingVideoId are provided,
                 * we do NOT use the paging state
                 */
                if (startingAddedDate.isPresent() && startingVideoId.isPresent()) {
//                    resultWithPagingState = latestVideosManager
//                            .dsl()
//                            .select()
//                            .allColumns_FromBaseTable()
//                            .where()
//                            .yyyymmdd().Eq(yyyyMMdd)
//                            .addedDate_And_videoid().Lte(startingAddedDate.get(), startingVideoId.get())
//                            .withFetchSize(recordsStillNeeded)
//                            .getListWithStats();

                    statement = QueryBuilder
                            .select().all()
                            .from(Schema.KEYSPACE, latestVideosMapper.getTableMetadata().getName())
                            .where(QueryBuilder.eq("yyyymmdd", yyyyMMdd))
                            //.and(QueryBuilder.lte("videoid", startingVideoId.get()))
                            .and(QueryBuilder.lte("added_date", startingAddedDate.get()));

                    statement
                            .setFetchSize(recordsStillNeeded);

                    future = manager.getSession().executeAsync(statement);
                    resultsTest = future.getUninterruptibly();

                    //resultWithPagingStateValue = resultWithPagingState.newValue(manager.getSession().executeAsync(statement));

                } else {
//                    resultWithPagingState = latestVideosManager
//                            .dsl()
//                            .select()
//                            .allColumns_FromBaseTable()
//                            .where()
//                            .yyyymmdd().Eq(yyyyMMdd)
//                            .withFetchSize(recordsStillNeeded)
//                            .withOptionalPagingStateString(pagingStateString)
//                            .getListWithStats();
//                    cassandraPagingStateUsed.compareAndSet(false, true);

                    statement = QueryBuilder
                            .select().all()
                            .from(Schema.KEYSPACE, latestVideosMapper.getTableMetadata().getName())
                            .where(QueryBuilder.eq("yyyymmdd", yyyyMMdd));

                    statement
                            .setFetchSize(recordsStillNeeded);

                    //:TODO Figure out the proper way to do this with Optional and java 8 lambada
                    if (pagingStateString.isPresent()) {
                        statement.setPagingState(PagingState.fromString(pagingStateString.get()));
                    }

                    future = manager.getSession().executeAsync(statement);
                    resultsTest = future.getUninterruptibly();
                    cassandraPagingStateUsed.compareAndSet(false, true);
                }


//                FutureUtils.buildCompletableFuture(future)
//                        .handle((result, ex) -> {
//                            //final TupleValue resultWithPagingStateValue = resultWithPagingState.newValue(null, null);
//
//                            if (result != null && !result.isExhausted()) {
//                                List<Row> rows = result.all();
//                                //resultWithPagingStateValue.setList(0, rows);
//                                //resultWithPagingStateValue.setList(1, result.getAllExecutionInfo());
//                                //cassandraPagingStateUsed.compareAndSet(false, true);
//
//
//                                results.addAll(resultWithPagingState.newValue(result.all(), result.getExecutionInfo())
//                                        .get(0, TypeTokens.listOf(LatestVideos.class))
//                                        .stream()
//                                        .map(LatestVideos::toVideoPreview)
//                                        .collect(toList()));
//
//                                //final ExecutionInfo executionInfo = resultWithPagingStateValue.get(1, ExecutionInfo.class);
//
//
//                            } else if (result == null) {
//
//                            } else if (ex != null) {
//                                LOGGER.error("Exception when getting latest preview videos : " + mergeStackTrace(ex));
//
//                                responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
//                            }
//                            return result;
//                        });

//                results.addAll(resultWithPagingState
//                        ._1()
//                        .stream()
//                        .map(LatestVideos::toVideoPreview)
//                        .collect(toList()));

                Result<LatestVideos> videos = latestVideosMapper.map(resultsTest);
                //TupleValue resultWithPagingStateValue = resultWithPagingState.newValue(null, null);
                //resultWithPagingStateValue.setList(0, videos.all());
                //resultWithPagingStateValue.setList(1, resultsTest.getAllExecutionInfo());
                //TupleValue resultWithPagingStateValue = resultWithPagingState.newValue(resultsTest, resultsTest.getExecutionInfo());
                results.addAll(videos.all()
                        .stream()
                        .map(LatestVideos::toVideoPreview)
                        .collect(toList()));

//                final ExecutionInfo executionInfo = resultWithPagingState._2();
                  final ExecutionInfo executionInfo = videos.getExecutionInfo();

                // See if we can stop querying
                if (results.size() >= request.getPageSize()) {

                    // Are there more rows in the current bucket?
                    if (executionInfo.getPagingState() != null) {
                        // Start from where we left off in this bucket if we get the next page
                        nextPageState = createPagingState(buckets, bucketIndex, executionInfo.getPagingState().toString());
                    } else if (bucketIndex != buckets.size() - 1) {
                        // Start from the beginning of the next bucket since we're out of rows in this one
                        nextPageState = createPagingState(buckets, bucketIndex + 1, "");
                    }
                    break;
                }

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

        //:TODO Fix this

//        if (!validator.isValid(request, responseObserver)) {
//            return;
//        }
//
//        final UUID userId = UUID.fromString(request.getUserId().getValue());
//        final Optional<UUID> startingVideoId = Optional
//                .ofNullable(request.getStartingVideoId())
//                .map(Uuid::getValue)
//                .filter(StringUtils::isNotBlank)
//                .map(UUID::fromString);
//
//        final Optional<Date> startingAddedDate = Optional
//                .ofNullable(request.getStartingAddedDate())
//                .map(ts -> Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()))
//                .map(Date::from);
//
//        final CompletableFuture<Tuple2<List<UserVideos>, ExecutionInfo>> listAsync;
//        final Optional<String> pagingStateString = Optional.ofNullable(request.getPagingState()).filter(StringUtils::isNotBlank);
//
//        /**
//         * If startingAddedDate and startingVideoId are provided,
//         * we do NOT use the paging state
//         */
//        if (startingVideoId.isPresent() && startingAddedDate.isPresent()) {
//            listAsync = userVideosManager
//                    .dsl()
//                    .select()
//                    .allColumns_FromBaseTable()
//                    .where()
//                    .userid().Eq(userId)
//                    .addedDate_And_videoid().Lte(startingAddedDate.get(), startingVideoId.get())
//                    .withFetchSize(request.getPageSize())
//                    .getListAsyncWithStats();
//
//        } else {
//            listAsync = userVideosManager
//                    .dsl()
//                    .select()
//                    .allColumns_FromBaseTable()
//                    .where()
//                    .userid().Eq(userId)
//                    .withFetchSize(request.getPageSize())
//                    .withOptionalPagingStateString(pagingStateString)
//                    .getListAsyncWithStats();
//        }
//
//        listAsync
//                .handle((tuple2, ex) -> {
//                    if (tuple2 != null) {
//                        final GetUserVideoPreviewsResponse.Builder builder = GetUserVideoPreviewsResponse.newBuilder();
//                        tuple2._1().stream().forEach(entity -> builder.addVideoPreviews(entity.toVideoPreview()));
//                        builder.setUserId(request.getUserId());
//                        Optional.ofNullable(tuple2._2().getPagingState())
//                                .map(PagingState::toString)
//                                .ifPresent(builder::setPagingState);
//                        responseObserver.onNext(builder.build());
//                        responseObserver.onCompleted();
//
//                        LOGGER.debug("End getting user video preview");
//
//                    } else if (ex != null) {
//
//                        LOGGER.error("Exception getting user video preview : " + mergeStackTrace(ex));
//
//                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
//
//                    }
//                    return tuple2;
//                });
    }

    private String createPagingState(List<String> buckets, int bucketIndex, String rowsPagingState) {
        StringJoiner joiner = new StringJoiner("_");
        buckets.forEach(joiner::add);
        return joiner.toString() + "," + bucketIndex + "," + rowsPagingState;
    }

    private Optional<TupleValue> parseCustomPagingState(Optional<String> customPagingState) {
        return customPagingState
                .map(pagingState -> {
                    Matcher matcher = PARSE_LATEST_PAGING_STATE.matcher(pagingState);
                    if (matcher.matches()) {
                        final List<String> buckets = Lists.newArrayList(matcher.group(1).split("_"));
                        final int currentBucket = Integer.parseInt(matcher.group(2));
                        final String cassandraPagingState = matcher.group(3);
                        TupleType tuple3 = manager.getSession().getCluster().getMetadata()
                                .newTupleType(DataType.list(DataType.varchar()), DataType.cint(), DataType.varchar());
                        return tuple3.newValue(buckets, currentBucket, cassandraPagingState);
                    } else {
                        return null;
                    }
                });
    }

//    private Supplier<Tuple3<List<String>, Integer, String>> buildFirstCustomPagingState() {
    private Supplier<TupleValue> buildFirstCustomPagingState() {
        return () -> {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            final ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
            final List<String> buckets = LongStream.rangeClosed(0L, 7L).boxed()
                    .map(now::minusDays)
                    .map(x -> x.format(formatter))
                    .collect(Collectors.toList());
            TupleType tuple3 = manager.getSession().getCluster().getMetadata()
                    .newTupleType(DataType.list(DataType.varchar()), DataType.cint(), DataType.varchar());
            return tuple3.newValue(buckets, 0, null);
            //return Tuple3.of(buckets, 0, null);
        };
    }
}
