package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.Date;
import java.util.UUID;

import info.archinnov.achilles.annotations.*;

@Table(keyspace = KEYSPACE, table = "video_recommendations_by_video")
public class VideoRecommandationsByVideo {

    @PartitionKey
    private UUID videoid;

    @Column("added_date")
    @Static
    private Date addedDate;

    @Column
    @Static
    private UUID authorid;

    @Column
    @Static
    private String name;

    @Column("preview_image_location")
    @Static
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
