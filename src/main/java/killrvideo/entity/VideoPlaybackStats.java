package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.Optional;
import java.util.UUID;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Counter;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;
import killrvideo.statistics.StatisticsServiceOuterClass;
import killrvideo.statistics.StatisticsServiceOuterClass.PlayStats;
import killrvideo.utils.TypeConverter;

@Table(keyspace = KEYSPACE, table = "video_playback_stats")
public class VideoPlaybackStats {

    @PartitionKey
    private UUID videoid;

    @Column
    @Counter
    private Long views;

    public UUID getVideoid() {
        return videoid;
    }

    public void setVideoid(UUID videoid) {
        this.videoid = videoid;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public PlayStats toPlayStats() {
        return PlayStats.newBuilder()
                .setVideoId(TypeConverter.uuidToUuid(videoid))
                .setViews(Optional.ofNullable(views).orElse(0L))
                .build();
    }
}
