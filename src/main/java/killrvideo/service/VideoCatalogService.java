package killrvideo.service;


import static info.archinnov.achilles.internals.futures.FutureUtils.toCompletableFuture;
import static java.util.stream.Collectors.toList;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.core.*;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import info.archinnov.achilles.generated.manager.LatestVideos_Manager;
import info.archinnov.achilles.generated.manager.UserVideos_Manager;
import info.archinnov.achilles.generated.manager.Video_Manager;
import info.archinnov.achilles.type.tuples.Tuple2;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.entity.LatestVideos;
import killrvideo.entity.UserVideos;
import killrvideo.entity.Video;
import killrvideo.utils.TypeConverter;
import killrvideo.video_catalog.VideoCatalogServiceGrpc.AbstractVideoCatalogService;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*;
import killrvideo.video_catalog.events.VideoCatalogEvents.UploadedVideoAccepted;
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded;

public class VideoCatalogService extends AbstractVideoCatalogService {

    public static final int MAX_DAYS_IN_PAST_FOR_LATEST_VIDEOS = 7;
    public static final int LATEST_VIDEOS_TTL_SECONDS = MAX_DAYS_IN_PAST_FOR_LATEST_VIDEOS * 24 * 3600;
    public static final Pattern PARSE_LATEST_PAGING_STATE = Pattern.compile("([0-9]{8}){8}([0-9]{1})(.*)");

    @Inject
    Video_Manager videoManager;

    @Inject
    UserVideos_Manager userVideosManager;

    @Inject
    LatestVideos_Manager latestVideosManager;

    @Inject
    EventBus eventBus;

    @Inject
    ExecutorService executorService;

    private Session session;

    @PostConstruct
    public void init(){
        this.session = videoManager.getNativeSession();
    }

    @Override
    public void submitUploadedVideo(SubmitUploadedVideoRequest request, StreamObserver<SubmitUploadedVideoResponse> responseObserver) {

        final Date now = new Date();

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());
        final UUID userId = UUID.fromString(request.getUserId().getValue());

        final BoundStatement bs1 = videoManager
                .crud()
                .insert(new Video(videoId, userId, request.getName(), request.getDescription(),
                        VideoLocationType.UPLOAD.name(), Sets.newHashSet(request.getTagsList().iterator()), now))
                .generateAndGetBoundStatement();

        final BoundStatement bs2 = userVideosManager
                .crud()
                .insert(new UserVideos(userId, videoId, request.getName(), now))
                .generateAndGetBoundStatement();

        final BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);
        batchStatement.add(bs1);
        batchStatement.add(bs2);
        batchStatement.setDefaultTimestamp(now.getTime());

        toCompletableFuture(session.executeAsync(batchStatement), executorService)
                .handle((rs,ex) -> {
                    if (rs != null) {
                        eventBus.post(UploadedVideoAccepted
                                .newBuilder()
                                .setVideoId(request.getVideoId())
                                .setUploadUrl(request.getUploadUrl())
                                .setTimestamp(TypeConverter.dateToTimestamp(now))
                                .build());
                        responseObserver.onNext(SubmitUploadedVideoResponse.newBuilder().build());
                        responseObserver.onCompleted();

                    } else if (ex != null) {
                        responseObserver.onError(ex);
                    }
                    return rs;
                });

    }

    @Override
    public void submitYouTubeVideo(SubmitYouTubeVideoRequest request, StreamObserver<SubmitYouTubeVideoResponse> responseObserver) {

        final Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        final String yyyyMMdd = dateFormat.format(now);
        final String location = request.getYouTubeVideoId();
        final String previewImageLocation = "//img.youtube.com/vi/"+ location + "/hqdefault.jpg";
        final UUID videoId = UUID.fromString(request.getVideoId().getValue());
        final UUID userId = UUID.fromString(request.getUserId().getValue());

        final BoundStatement bs1 = videoManager
                .crud()
                .insert(new Video(videoId, userId, request.getName(), request.getDescription(), location, VideoLocationType.YOUTUBE.name(), previewImageLocation, Sets.newHashSet(request.getTagsList().iterator()), now))
                .generateAndGetBoundStatement();

        final BoundStatement bs2 = userVideosManager
                .crud()
                .insert(new UserVideos(userId, videoId, request.getName(), previewImageLocation, now))
                .generateAndGetBoundStatement();

        final BoundStatement bs3 = latestVideosManager
                .crud()
                .insert(new LatestVideos(yyyyMMdd, userId, videoId, request.getName(), previewImageLocation, now))
                .usingTimeToLive(LATEST_VIDEOS_TTL_SECONDS)
                .generateAndGetBoundStatement();

        final BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);
        batchStatement.add(bs1);
        batchStatement.add(bs2);
        batchStatement.add(bs3);
        batchStatement.setDefaultTimestamp(now.getTime());

        toCompletableFuture(session.executeAsync(batchStatement), executorService)
                .handle((rs, ex) -> {
                    if (rs != null) {
                        final YouTubeVideoAdded youTubeVideoAdded = YouTubeVideoAdded.newBuilder()
                                .setAddedDate(TypeConverter.dateToTimestamp(now))
                                .setDescription(request.getDescription())
                                .setLocation(location)
                                .setName(request.getName())
                                .setPreviewImageLocation(previewImageLocation)
                                .setTimestamp(TypeConverter.dateToTimestamp(now))
                                .setUserId(request.getUserId())
                                .setVideoId(request.getVideoId())
                                .build();
                        youTubeVideoAdded.getTagsList().addAll(Sets.newHashSet(request.getTagsList()));
                        eventBus.post(youTubeVideoAdded);
                        responseObserver.onNext(SubmitYouTubeVideoResponse.newBuilder().build());
                        responseObserver.onCompleted();

                    } else if (ex != null) {
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return rs;
                });
    }

    @Override
    public void getVideo(GetVideoRequest request, StreamObserver<GetVideoResponse> responseObserver) {

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());

        videoManager
                .crud()
                .findById(videoId)
                .getAsync()
                .handle((entity,ex) -> {
                    if (entity != null) {
                        responseObserver.onNext(entity.toVideoResponse());
                        responseObserver.onCompleted();
                    } else if (entity == null) {
                        responseObserver.onError(Status.NOT_FOUND
                                .withDescription("Video with id " + videoId + " was not found").asRuntimeException());
                    } else if (ex != null) {
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return entity;
                });
    }

    @Override
    public void getVideoPreviews(GetVideoPreviewsRequest request, StreamObserver<GetVideoPreviewsResponse> responseObserver) {
        final GetVideoPreviewsResponse.Builder builder = GetVideoPreviewsResponse.newBuilder();

        if (request.getVideoIdsCount() == 0 || request.getVideoIdsList() == null) {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        if (request.getVideoIdsCount() > 20) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Cannot fetch more than 20 videos at once").asRuntimeException());
        }

        final List<CompletableFuture<Video>> listFuture = request
                .getVideoIdsList()
                .stream()
                .map(uuid -> UUID.fromString(uuid.getValue()))
                .map(uuid -> videoManager.crud().findById(uuid).getAsync())
                .collect(toList());

        CompletableFuture
                .allOf(listFuture.toArray(new CompletableFuture[listFuture.size()]))
                .thenApply(v -> listFuture.stream().map(CompletableFuture::join).collect(toList()))
                .thenAccept(list -> list
                        .stream()
                        .map(entity -> builder.addVideoPreviews(entity.toVideoPreview()))
                        .collect(toList()));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getLatestVideoPreviews(GetLatestVideoPreviewsRequest request, StreamObserver<GetLatestVideoPreviewsResponse> responseObserver) {
        String[] buckets;
        int bucketIndex;
        String rowPagingState;

        //???
    }

    @Override
    public void getUserVideoPreviews(GetUserVideoPreviewsRequest request, StreamObserver<GetUserVideoPreviewsResponse> responseObserver) {

        final UUID userId = UUID.fromString(request.getUserId().getValue());
        final Optional<UUID> startingVideoId = Optional.ofNullable(request.getStartingVideoId()).map(uuid -> UUID.fromString(uuid.getValue()));
        final Instant startingAddedDate = Instant.ofEpochSecond(request.getStartingAddedDate().getSeconds(), request.getStartingAddedDate().getNanos());

        final CompletableFuture<Tuple2<List<UserVideos>, ExecutionInfo>> listAsync;
        if (startingVideoId.isPresent()) {
            listAsync = userVideosManager
                    .dsl()
                    .select()
                    .allColumns_FromBaseTable()
                    .where()
                    .userid_Eq(userId)
                    .limit(request.getPageSize())
                    .withOptionalPagingState(request.getPagingState())
                    .getListAsyncWithStats();

        } else {
            listAsync = userVideosManager
                    .dsl()
                    .select()
                    .allColumns_FromBaseTable()
                    .where()
                    .userid_Eq(userId)
                    .addedDate_And_videoid_Lte(Date.from(startingAddedDate), startingVideoId.get())
                    .limit(request.getPageSize())
                    .withOptionalPagingState(request.getPagingState())
                    .getListAsyncWithStats();
        }

        listAsync
                .handle((tuple2, ex) -> {
                    if (tuple2 != null) {
                        final GetUserVideoPreviewsResponse.Builder builder = GetUserVideoPreviewsResponse.newBuilder();
                        tuple2._1().stream().forEach(entity -> builder.addVideoPreviews(entity.toVideoPreview()));
                        builder.setUserId(request.getUserId());
                        Optional.ofNullable(tuple2._2().getPagingState())
                                .map(PagingState::toString)
                                .ifPresent(builder::setPagingState);
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();
                    } else if (ex != null) {
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }

                    return tuple2;
                });
    }
    
}
