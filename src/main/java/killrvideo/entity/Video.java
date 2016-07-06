package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;
import killrvideo.utils.TypeConverter;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.VideoLocationType;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.VideoPreview;

@Table(keyspace = KEYSPACE, table = "videos")
public class Video extends AbstractVideo{

    @PartitionKey
    private UUID videoid;

    @Column
    private UUID userid;

    @Column
    private String description;

    @Column
    private String location;

    @Column("location_type")
    private String locationType;

    @Column
    private Set<String> tags;

    @Column("added_date")
    private Date addedDate;

    public Video() {
    }

    public Video(UUID videoid, UUID userid, String name, String description, String locationType, Set<String> tags, Date addedDate) {
        this.videoid = videoid;
        this.userid = userid;
        this.name = name;
        this.description = description;
        this.locationType = locationType;
        this.tags = tags;
        this.addedDate = addedDate;
    }

    public Video(UUID videoid, UUID userid, String name, String description, String location, String locationType, String previewImageLocation, Set<String> tags, Date addedDate) {
        this.videoid = videoid;
        this.userid = userid;
        this.name = name;
        this.description = description;
        this.location = location;
        this.locationType = locationType;
        this.previewImageLocation = previewImageLocation;
        this.tags = tags;
        this.addedDate = addedDate;
    }

    public UUID getVideoid() {
        return videoid;
    }

    public void setVideoid(UUID videoid) {
        this.videoid = videoid;
    }

    public UUID getUserid() {
        return userid;
    }

    public void setUserid(UUID userid) {
        this.userid = userid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public GetVideoResponse toVideoResponse() {
        final GetVideoResponse videoResponse = GetVideoResponse
                .newBuilder()
                .setAddedDate(TypeConverter.dateToTimestamp(addedDate))
                .setDescription(description)
                .setLocation(location)
                .setLocationType(VideoLocationType.valueOf(locationType))
                .setName(name)
                .setUserId(TypeConverter.uuidToUuid(userid))
                .setVideoId(TypeConverter.uuidToUuid(videoid))
                .build();

        videoResponse.getTagsList().addAll(tags);

        return videoResponse;
    }

    public VideoPreview toVideoPreview() {
        return VideoPreview.newBuilder()
                .setAddedDate(TypeConverter.dateToTimestamp(addedDate))
                .setName(name)
                .setPreviewImageLocation(previewImageLocation)
                .setUserId(TypeConverter.uuidToUuid(userid))
                .setVideoId(TypeConverter.uuidToUuid(videoid))
                .build();
    }
}
