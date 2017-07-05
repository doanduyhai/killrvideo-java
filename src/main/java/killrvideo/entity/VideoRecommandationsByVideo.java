package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.Date;
import java.util.UUID;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.ClusteringColumn;

@Table(keyspace = KEYSPACE, name = "video_recommendations_by_video")
public class VideoRecommandationsByVideo {

    @PartitionKey
    private UUID videoid;

    @Column(name = "added_date")
    private Date addedDate;

    @Column
    private UUID authorid;

    @Column
    private String name;

    @Column(name = "preview_image_location")
    private String previewImageLocation;

    @ClusteringColumn
    private UUID userid;

    @Column
    private float rating;

    public VideoRecommandationsByVideo() {
    }

    public VideoRecommandationsByVideo(UUID videoid, Date addedDate, UUID authorid, String name, String previewImageLocation, UUID userid, float rating) {
        this.videoid = videoid;
        this.addedDate = addedDate;
        this.authorid = authorid;
        this.name = name;
        this.previewImageLocation = previewImageLocation;
        this.userid = userid;
        this.rating = rating;
    }

    public UUID getVideoid() {
        return videoid;
    }

    public void setVideoid(UUID videoid) {
        this.videoid = videoid;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public UUID getAuthorid() {
        return authorid;
    }

    public void setAuthorid(UUID authorid) {
        this.authorid = authorid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreviewImageLocation() {
        return previewImageLocation;
    }

    public void setPreviewImageLocation(String previewImageLocation) {
        this.previewImageLocation = previewImageLocation;
    }

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
}
