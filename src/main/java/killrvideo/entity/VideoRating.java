package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.Optional;
import java.util.UUID;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Counter;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;
import killrvideo.ratings.RatingsServiceOuterClass;
import killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse;
import killrvideo.utils.TypeConverter;

@Table(keyspace = KEYSPACE, table = "video_ratings")
public class VideoRating {

    @PartitionKey
    private UUID videoid;

    @Column("rating_counter")
    @Counter
    private Long ratingCounter;

    @Column("rating_total")
    @Counter
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
