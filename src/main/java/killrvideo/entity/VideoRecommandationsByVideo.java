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

    /**
     * Getter for attribute 'videoid'.
     *
     * @return
     *       current value of 'videoid'
     */
    public UUID getVideoid() {
        return videoid;
    }

    /**
     * Setter for attribute 'videoid'.
     * @param videoid
     * 		new value for 'videoid '
     */
    public void setVideoid(UUID videoid) {
        this.videoid = videoid;
    }

    /**
     * Getter for attribute 'addedDate'.
     *
     * @return
     *       current value of 'addedDate'
     */
    public Date getAddedDate() {
        return addedDate;
    }

    /**
     * Setter for attribute 'addedDate'.
     * @param addedDate
     * 		new value for 'addedDate '
     */
    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    /**
     * Getter for attribute 'authorid'.
     *
     * @return
     *       current value of 'authorid'
     */
    public UUID getAuthorid() {
        return authorid;
    }

    /**
     * Setter for attribute 'authorid'.
     * @param authorid
     * 		new value for 'authorid '
     */
    public void setAuthorid(UUID authorid) {
        this.authorid = authorid;
    }

    /**
     * Getter for attribute 'name'.
     *
     * @return
     *       current value of 'name'
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for attribute 'name'.
     * @param name
     * 		new value for 'name '
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for attribute 'previewImageLocation'.
     *
     * @return
     *       current value of 'previewImageLocation'
     */
    public String getPreviewImageLocation() {
        return previewImageLocation;
    }

    /**
     * Setter for attribute 'previewImageLocation'.
     * @param previewImageLocation
     * 		new value for 'previewImageLocation '
     */
    public void setPreviewImageLocation(String previewImageLocation) {
        this.previewImageLocation = previewImageLocation;
    }

    /**
     * Getter for attribute 'userid'.
     *
     * @return
     *       current value of 'userid'
     */
    public UUID getUserid() {
        return userid;
    }

    /**
     * Setter for attribute 'userid'.
     * @param userid
     * 		new value for 'userid '
     */
    public void setUserid(UUID userid) {
        this.userid = userid;
    }

    /**
     * Getter for attribute 'rating'.
     *
     * @return
     *       current value of 'rating'
     */
    public float getRating() {
        return rating;
    }

    /**
     * Setter for attribute 'rating'.
     * @param rating
     * 		new value for 'rating '
     */
    public void setRating(float rating) {
        this.rating = rating;
    }
    
    
}
