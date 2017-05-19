package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.Optional;
import java.util.UUID;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import killrvideo.statistics.StatisticsServiceOuterClass.PlayStats;
import killrvideo.utils.TypeConverter;

@Table(keyspace = KEYSPACE, name = "video_playback_stats")
public class VideoPlaybackStats {

    @PartitionKey
    private UUID videoid;

    /**
     * "views" column is a counter type in the underlying DSE database.  As of driver version 3.2 there
     * is no "@Counter" annotation that I know of.  No worries though, just use the incr() function
     * while using the QueryBuilder.  Something similar to with(QueryBuilder.incr("views")).
     */
    @Column
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
