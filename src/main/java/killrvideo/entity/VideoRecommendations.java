package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.UUID;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = KEYSPACE, name = "video_recommendations")
public class VideoRecommendations extends AbstractVideoList {

    @PartitionKey
    private UUID userid;

    @Column
    private float rating;

    @Column
    private UUID authorid;

    public UUID getUserid() {
        return userid;
    }

    public void setUserid(UUID userid) {
        this.userid = userid;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public UUID getAuthorid() {
        return authorid;
    }

    public void setAuthorid(UUID authorid) {
        this.authorid = authorid;
    }
}
