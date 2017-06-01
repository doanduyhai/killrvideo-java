package killrvideo.service;

import static java.util.stream.Collectors.toList;
import static killrvideo.utils.ExceptionUtils.mergeStackTrace;
import static com.datastax.driver.mapping.Mapper.Option.*;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.mapping.Result;
import com.google.common.reflect.TypeToken;
import killrvideo.entity.*;
import killrvideo.statistics.StatisticsServiceOuterClass;
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
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded;

@Service
public class VideoCatalogService extends AbstractVideoCatalogService {

    // used as a container for custom paging state for latest videos
    class CustomPagingState {
        public List<String> buckets;
        public int currentBucket;
        public String cassandraPagingState;
    }

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
         *
         * Note I am not using QueryBuilder with bindmarker() for these
         * statements.  This is not a value judgement, just a different way of doing it.
         * Take a look at some of the other services to see QueryBuilder.bindmarker() examples.
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

        //:TODO These either need to be prepared statements or proper async
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

        FutureUtils.buildCompletableFuture(session.executeAsync(batchStatement))
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

                        /**
                         * eventbus.post() below is located in the VideoAddedhandlers class within
                         * the handle() method.  The youTubeVideoAdded type triggers the handler.
                         */
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

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());

        // videoId matches the partition key set in the Video class
        //:TODO notice that Olivier had me put the videoMapper.getAsync call directly into the callback...don't forget that
        //:TODO a call to getQuery still produces a prepared statement and that needs to be handled async otherwise it will block
        FutureUtils.buildCompletableFuture(videoMapper.getAsync(videoId))
                .handle((video, ex) -> {
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

        try {
            /**
             * Fire a list of async SELECT, one for each video id
             */
            final List<CompletableFuture<Video>> listFuture = request
                    .getVideoIdsList()
                    .stream()
                    .map(uuid -> UUID.fromString(uuid.getValue()))
                    .map(uuid -> FutureUtils.buildCompletableFuture(videoMapper.getAsync(uuid)))
                    .collect(toList());

            /**
             * Merge all the async SELECT results
             */
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

        final CustomPagingState customPagingState = parseCustomPagingState(Optional.ofNullable(request.getPagingState()))
                .orElse(this.buildFirstCustomPagingState());

        final List<String> buckets = customPagingState.buckets;
        int bucketIndex = customPagingState.currentBucket;
        final String rowPagingState = customPagingState.cassandraPagingState;
        LOGGER.debug("Custom paging state is: buckets: " + buckets.size() + " index: " + bucketIndex + " state: " + rowPagingState);

        final Optional<Date> startingAddedDate = Optional
                .ofNullable(request.getStartingAddedDate())
                .filter(x -> StringUtils.isNotBlank(x.toString())) //:TODO ensure if this should be included or not
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
                LOGGER.debug("recordsStillNeeded is: " + recordsStillNeeded + " pageSize is: " + request.getPageSize() + " results.size is: " + results.size());

                final String yyyyMMdd = buckets.get(bucketIndex);

                final Optional<String> pagingState =
                        Optional.ofNullable(rowPagingState)
                                .filter(StringUtils::isNotBlank)
                                .filter(pg -> !cassandraPagingStateUsed.get());

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
                            .setTimestamp("ad", startingAddedDate.get())
                            .setUUID("vid", startingVideoId.get());

                    LOGGER.debug("Current query is: " + bound.preparedStatement().getQueryString());

                } else {
                    /**
                     * The noStartingPointPrepared statement can be found at the top
                     * of the class within PostConstruct
                     */
                    bound = latestVideoPreview_noStartingPointPrepared.bind()
                            .setString("ymd", yyyyMMdd);

                    LOGGER.debug("Current query is: " + bound.preparedStatement().getQueryString());
                }

                bound.setFetchSize(recordsStillNeeded);
                LOGGER.debug("FETCH SIZE is: " + bound.getFetchSize() + " ymd is: " + yyyyMMdd);

                pagingState.ifPresent(x -> {
                            bound.setPagingState(PagingState.fromString(x));
                            cassandraPagingStateUsed.compareAndSet(false, true);
                        }
                );

                /**
                 * Not entirely sure why DuyHai used getUninterruptibly within his
                 * getListWithStats() call from Achilles, but I copied it to ensure
                 * I replicated the same functionality.  Must get clarification on this.
                 */
                //:TODO Find a way to do this properly in an async fashion, in talking to Olivier
                //:TODO there is a way to do it, but it is more complicated.  ControlConnection
                ResultSet resultSet = session.executeAsync(bound).getUninterruptibly();
                Result<LatestVideos> videos = latestVideosMapper.map(resultSet);
                results.addAll(videos.all()
                        .stream()
                        .map(LatestVideos::toVideoPreview)
                        .collect(toList()));


                // See if we can stop querying
                if (results.size() >= request.getPageSize()) {
                    final PagingState cassandraPagingState = resultSet.getAllExecutionInfo().get(0).getPagingState();

                    if (cassandraPagingState != null) {
                        LOGGER.debug("results.size() >= request.getPageSize()");
                        // Start from where we left off in this bucket if we get the next page
                        nextPageState = createPagingState(buckets, bucketIndex, cassandraPagingState.toString());

                        break;
                    }

                } else if (bucketIndex == buckets.size() - 1) {
                    LOGGER.debug("bucketIndex == buckets.size() - 1)");
                    // Start from the beginning of the next bucket since we're out of rows in this one
                    nextPageState = createPagingState(buckets, bucketIndex + 1, "");
                }

                LOGGER.debug("------------------" +
                        " buckets: " + buckets.size() +
                        " index: " + bucketIndex +
                        " state: " + nextPageState +
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

        final Optional<String> pagingState = Optional.ofNullable(request.getPagingState()).filter(StringUtils::isNotBlank);
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

        pagingState.ifPresent( x -> bound.setPagingState(PagingState.fromString(x)));

        /**
         * Notice since I am passing userVideosMapper.mapAsync() into my call
         * I get back results that are already mapped to UserVideos entities.
         * This is a really nice convenience the mapper provides.
         */
        FutureUtils.buildCompletableFuture(userVideosMapper.mapAsync(session.executeAsync(bound)))
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
     * Parse the passed in paging state and return an object containing the 3 elements of the state
     * (List<String>, Integer, String) as Optional.
     * @param customPagingStateString
     * @return Optional
     */
    private Optional<CustomPagingState> parseCustomPagingState(Optional<String> customPagingStateString) {
        return customPagingStateString
                .map(pagingState -> {
                    Matcher matcher = PARSE_LATEST_PAGING_STATE.matcher(pagingState);
                    if (matcher.matches()) {
                        final CustomPagingState customPagingState = new CustomPagingState();
                        customPagingState.buckets = Lists.newArrayList(matcher.group(1).split("_"));
                        customPagingState.currentBucket = Integer.parseInt(matcher.group(2));
                        customPagingState.cassandraPagingState = matcher.group(3);
                        return customPagingState;
                    } else {
                        return null;
                    }
                });
    }


    /**
     * Build the first paging state if one does not already exist and return an object containing 3 elements
     * representing the initial state (List<String>, Integer, String).
     * @return CustomPagingState
     */
    private CustomPagingState buildFirstCustomPagingState() {
        //return () -> {
            final CustomPagingState customPagingState = new CustomPagingState();
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            final ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
            customPagingState.buckets = LongStream.rangeClosed(0L, 7L).boxed()
                    .map(now::minusDays)
                    .map(x -> x.format(formatter))
                    .collect(Collectors.toList());
            customPagingState.currentBucket = 0;
            customPagingState.cassandraPagingState = null;
            return customPagingState;
        //};
    }
}