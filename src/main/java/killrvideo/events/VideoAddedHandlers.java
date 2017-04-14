package killrvideo.events;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import killrvideo.entity.LatestVideos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Session;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

//import info.archinnov.achilles.generated.manager.TagsByLetter_Manager;
//import info.archinnov.achilles.generated.manager.VideoByTag_Manager;
import killrvideo.entity.TagsByLetter;
import killrvideo.entity.VideoByTag;
import killrvideo.utils.FutureUtils;
import killrvideo.video_catalog.events.VideoCatalogEvents;
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded;

@Component
public class VideoAddedHandlers {

    private static Logger LOGGER = LoggerFactory.getLogger(VideoAddedHandlers.class);

    @Inject
    Mapper<VideoByTag> videoByTagMapper;

    @Inject
    Mapper<TagsByLetter> tagsByLetterMapper;

    @Inject
    MappingManager manager;

    @Inject
    ExecutorService executorService;

    private Session session;

    @PostConstruct
    public void init() {
        this.session = manager.getSession();

    }

    @Subscribe
    public void handle(YouTubeVideoAdded youTubeVideoAdded) {

        LOGGER.debug("Start handling YouTubeVideoAdded");

        final UUID userId = UUID.fromString(youTubeVideoAdded.getUserId().getValue());
        final UUID videoId = UUID.fromString(youTubeVideoAdded.getVideoId().getValue());
        final HashSet<String> tags = Sets.newHashSet(youTubeVideoAdded.getTagsList());
        final String name = youTubeVideoAdded.getName();
        final String previewImageLocation = youTubeVideoAdded.getPreviewImageLocation();
        Date addedDate = Date.from(Instant.ofEpochSecond(youTubeVideoAdded.getAddedDate().getSeconds(), youTubeVideoAdded.getTimestamp().getNanos()));
        Date taggedDate = Date.from(Instant.ofEpochSecond(youTubeVideoAdded.getTimestamp().getSeconds(), youTubeVideoAdded.getTimestamp().getNanos()));

        final BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);

        tags.forEach(tag -> {

            //:TODO Make these prepared statements
            batchStatement.add(
                    videoByTagMapper.saveQuery(
                            new VideoByTag(tag, videoId, userId, name, previewImageLocation, addedDate, taggedDate)
                    ));

            batchStatement.add(
                    tagsByLetterMapper.saveQuery(
                            new TagsByLetter(tag.substring(0,1), tag)
                    ));
        });

        batchStatement.setDefaultTimestamp(taggedDate.getTime());

        FutureUtils.buildCompletableFuture(session.executeAsync(batchStatement))
            .handle((rs, ex) -> {
                if (rs != null) {
                    LOGGER.debug("End handling YouTubeVideoAdded");
                }
                if (ex != null) {

                }
                return rs;
            });
    }
}
