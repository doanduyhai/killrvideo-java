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

    @PartitionKey
    private String yyyymmdd;

    @Column
    private UUID userid;

    public LatestVideos() {
    }

    public LatestVideos(String yyyymmdd, UUID userid, UUID videoid, String name, String previewImageLocation, Date addedDate) {
        this.yyyymmdd = yyyymmdd;
        this.userid = userid;
        this.videoid = videoid;
        this.name = name;
        this.previewImageLocation = previewImageLocation;
        this.addedDate = addedDate;
    }

    public String getYyyymmdd() {
        return yyyymmdd;
    }

    public void setYyyymmdd(String yyyymmdd) {
        this.yyyymmdd = yyyymmdd;
    }

    public UUID getUserid() {
        return userid;
    }

    public void setUserid(UUID userid) {
        this.userid = userid;
    }

    public VideoPreview toVideoPreview() {
        return VideoPreview
                .newBuilder()
                .setAddedDate(TypeConverter.dateToTimestamp(addedDate))
                .setName(name)
                .setPreviewImageLocation(Optional.ofNullable(previewImageLocation).orElse("N/A"))
                .setUserId(TypeConverter.uuidToUuid(userid))
                .setVideoId(TypeConverter.uuidToUuid(videoid))
                .build();
    }
}
