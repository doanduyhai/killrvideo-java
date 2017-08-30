package killrvideo.events;

import com.datastax.driver.core.*;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import killrvideo.entity.Schema;
import killrvideo.entity.TagsByLetter;
import killrvideo.entity.VideoByTag;
import killrvideo.utils.FutureUtils;
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class VideoAddedHandlers {

    private static Logger LOGGER = LoggerFactory.getLogger(VideoAddedHandlers.class);

    @Inject
    Mapper<VideoByTag> videosByTagMapper;

    @Inject
    Mapper<TagsByLetter> tagsByLetterMapper;

    @Inject
    MappingManager manager;

    @Inject
    DseSession dseSession;

    private String videosByTagTableName;
    private String tagsByLetterTableName;
    private PreparedStatement videosByTagPrepared;
    private PreparedStatement tagsByLetterPrepared;

    @PostConstruct
    public void init() {
        videosByTagTableName = videosByTagMapper.getTableMetadata().getName();
        tagsByLetterTableName = tagsByLetterMapper.getTableMetadata().getName();

        // Prepared statements for handle()
        videosByTagPrepared = dseSession.prepare(
                "INSERT INTO " + Schema.KEYSPACE + "." + videosByTagTableName + " " +
                        "(tag, videoid, added_date, userid, name, preview_image_location, tagged_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)"
        ).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        tagsByLetterPrepared = dseSession.prepare(
                "INSERT INTO " + Schema.KEYSPACE + "." + tagsByLetterTableName + " " +
                        "(first_letter, tag) VALUES (?, ?)"
        ).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
    }

    /**
     * Make @Subscribe subscriber magic happen anytime a youTube video is added from
     * VideoCatalogService.submitYouTubeVideo() with a call to eventBus.post().
     * This method inserts any provided tags into multiple tag based tables for use with
     * searching later using batch functionality.
     * @param youTubeVideoAdded
     */
    @Subscribe
    public void handle(YouTubeVideoAdded youTubeVideoAdded) {
        final String className = this.getClass().getName();

        LOGGER.debug("Start handling YouTubeVideoAdded for " + className);

        final UUID userId = UUID.fromString(youTubeVideoAdded.getUserId().getValue());
        final UUID videoId = UUID.fromString(youTubeVideoAdded.getVideoId().getValue());
        final HashSet<String> tags = Sets.newHashSet(youTubeVideoAdded.getTagsList());
        final String name = youTubeVideoAdded.getName();
        final String previewImageLocation = youTubeVideoAdded.getPreviewImageLocation();
        Date addedDate = Date.from(Instant.ofEpochSecond(youTubeVideoAdded.getAddedDate().getSeconds(), youTubeVideoAdded.getTimestamp().getNanos()));
        Date taggedDate = Date.from(Instant.ofEpochSecond(youTubeVideoAdded.getTimestamp().getSeconds(), youTubeVideoAdded.getTimestamp().getNanos()));

        final BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);

        LOGGER.debug("Handler thread " + Thread.currentThread().toString());

        //:TODO Potential implement this with mapper async by using saveAsync and build each statement in the batch with a future and handle
        tags.forEach(tag -> {
            BoundStatement videosByTagBound = videosByTagPrepared.bind(
                    tag, videoId, addedDate, userId, name, previewImageLocation, taggedDate
            );

            BoundStatement tagsByLetterBound = tagsByLetterPrepared.bind(
                    tag.substring(0, 1), tag
            );

            batchStatement.add(videosByTagBound);
            batchStatement.add(tagsByLetterBound);
        });

        batchStatement.setDefaultTimestamp(taggedDate.getTime());

        CompletableFuture<ResultSet> batchFuture = FutureUtils.buildCompletableFuture(dseSession.executeAsync(batchStatement))
                .handle((rs, ex) -> {
                    if (rs != null) {
                        LOGGER.debug("End handling YouTubeVideoAdded");
                    }
                    if (ex != null) {
                        //:TODO We should probably put in some logic that will repeat the transaction if it fails for some reason

                    }
                    return rs;
                });
    }
}
