package killrvideo.entity;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import killrvideo.utils.TypeConverter;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.VideoPreview;

@Table(keyspace = Schema.KEYSPACE, name = "latest_videos")
public class LatestVideos extends AbstractVideoList {

    /** Serial. */
    private static final long serialVersionUID = -8527565276521920973L;

    @PartitionKey
    private String yyyymmdd;

    @Column
    private UUID userid;

    public LatestVideos() {
    }

    public LatestVideos(String yyyymmdd, UUID userid, UUID videoid, String name, String previewImageLocation, Date addedDate) {
        super(name, previewImageLocation, addedDate, videoid);
        this.yyyymmdd = yyyymmdd;
        this.userid = userid;
    }
    
    public VideoPreview toVideoPreview() {
        return VideoPreview
                .newBuilder()
                .setAddedDate(TypeConverter.dateToTimestamp(getAddedDate()))
                .setName(getName())
                .setPreviewImageLocation(Optional.ofNullable(getPreviewImageLocation()).orElse("N/A"))
                .setUserId(TypeConverter.uuidToUuid(getUserid()))
                .setVideoId(TypeConverter.uuidToUuid(getVideoid()))
                .build();
    }

    /**
     * Getter for attribute 'yyyymmdd'.
     *
     * @return
     *       current value of 'yyyymmdd'
     */
    public String getYyyymmdd() {
        return yyyymmdd;
    }

    /**
     * Setter for attribute 'yyyymmdd'.
     * @param yyyymmdd
     * 		new value for 'yyyymmdd '
     */
    public void setYyyymmdd(String yyyymmdd) {
        this.yyyymmdd = yyyymmdd;
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
    
    
}
