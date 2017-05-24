package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.Optional;
import java.util.UUID;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse;
import killrvideo.utils.TypeConverter;

@Table(keyspace = KEYSPACE, name = "video_ratings")
public class VideoRating {

    @PartitionKey
    private UUID videoid;

    @Column(name = "rating_counter")
    private Long ratingCounter;

    @Column(name = "rating_total")
    private Long ratingTotal;

    public UUID getVideoid() {
        return videoid;
    }

    public void setVideoid(UUID videoid) {
        this.videoid = videoid;
    }

    public Long getRatingCounter() {
        return ratingCounter;
    }

    public void setRatingCounter(Long ratingCounter) {
        this.ratingCounter = ratingCounter;
    }

    public Long getRatingTotal() {
        return ratingTotal;
    }

    public void setRatingTotal(Long ratingTotal) {
        this.ratingTotal = ratingTotal;
    }

    public GetRatingResponse toRatingResponse() {
        return GetRatingResponse.newBuilder()
                .setVideoId(TypeConverter.uuidToUuid(videoid))
                .setRatingsCount(Optional.ofNullable(ratingCounter).orElse(0L))
                .setRatingsTotal(Optional.ofNullable(ratingTotal).orElse(0L))
                .build();
    }

}
