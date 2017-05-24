package killrvideo.entity;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import killrvideo.utils.TypeConverter;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.VideoPreview;

@Table(keyspace = Schema.KEYSPACE, name = "user_videos")
public class UserVideos extends AbstractVideoList {

    @PartitionKey
    private UUID userid;

    public UserVideos() {
    }

    public UserVideos(UUID userid, UUID videoid, String name, Date addedDate) {
        this.userid = userid;
        this.videoid = videoid;
        this.name = name;
        this.addedDate = addedDate;
    }

    public UserVideos(UUID userid, UUID videoid, String name, String previewImageLocation, Date addedDate) {
        this.userid = userid;
        this.videoid = videoid;
        this.name = name;
        this.previewImageLocation = previewImageLocation;
        this.addedDate = addedDate;
    }

    public UUID getUserid() {
        return userid;
    }

    public void setUserid(UUID userid) { this.userid = userid; }

    public VideoPreview toVideoPreview() {
        return VideoPreview
                .newBuilder()
                .setAddedDate(TypeConverter.dateToTimestamp(addedDate))
                .setName(name)
                .setPreviewImageLocation(Optional
                        .ofNullable(previewImageLocation)
                        .orElse("N/A"))
                .setUserId(TypeConverter.uuidToUuid(userid))
                .setVideoId(TypeConverter.uuidToUuid(videoid))
                .build();
    }
}
