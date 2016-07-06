package killrvideo.events;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Session;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import info.archinnov.achilles.generated.manager.TagsByLetter_Manager;
import info.archinnov.achilles.generated.manager.VideoByTag_Manager;
import killrvideo.entity.TagsByLetter;
import killrvideo.entity.VideoByTag;
import killrvideo.video_catalog.events.VideoCatalogEvents;
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded;

@Component
public class VideoAddedHandlers {

    @Inject
    VideoByTag_Manager videoByTagManager;

    @Inject
    TagsByLetter_Manager tagsByLetterManager;

    @Subscribe
    public void handle(YouTubeVideoAdded youTubeVideoAdded) {

        final UUID userId = UUID.fromString(youTubeVideoAdded.getUserId().getValue());
        final UUID videoId = UUID.fromString(youTubeVideoAdded.getVideoId().getValue());
        final HashSet<String> tags = Sets.newHashSet(youTubeVideoAdded.getTagsList());
        Date addedDate = Date.from(Instant.ofEpochSecond(youTubeVideoAdded.getAddedDate().getSeconds(), youTubeVideoAdded.getTimestamp().getNanos()));
        Date taggedDate = Date.from(Instant.ofEpochSecond(youTubeVideoAdded.getTimestamp().getSeconds(), youTubeVideoAdded.getTimestamp().getNanos()));

        final BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);

        tags.forEach(tag -> {

            batchStatement.add(videoByTagManager
                    .crud()
                    .insert(new VideoByTag(tag, videoId, userId, addedDate, taggedDate))
                    .generateAndGetBoundStatement());

            batchStatement.add(tagsByLetterManager
                    .crud()
                    .insert(new TagsByLetter(tag.substring(0,1), tag))
                    .generateAndGetBoundStatement());
        });

        batchStatement.setDefaultTimestamp(taggedDate.getTime());

        videoByTagManager.getNativeSession().executeAsync(batchStatement);
    }
}
