package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import killrvideo.utils.EmptyCollectionIfNull;

import killrvideo.utils.TypeConverter;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.VideoLocationType;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.VideoPreview;

@Table(keyspace = KEYSPACE, name = "videos")
public class Video extends AbstractVideo{

    @PartitionKey
    private UUID videoid;

    @NotNull
    @Column
    private UUID userid;

    @NotBlank
    @Column
    private String description;

    @NotBlank
    @Column
    private String location;

    @Column(name = "location_type")
    private int locationType;

    @Column
    @EmptyCollectionIfNull
    private Set<String> tags;

    @NotNull
    @Column(name = "added_date")
    private Date addedDate;

    public Video() {
    }

    public Video(UUID videoid, UUID userid, String name, String description, int locationType, Set<String> tags, Date addedDate) {
        this.videoid = videoid;
        this.userid = userid;
        this.name = name;
        this.description = description;
        this.locationType = locationType;
        this.tags = tags;
        this.addedDate = addedDate;
    }

    public Video(UUID videoid, UUID userid, String name, String description, String location, int locationType, String previewImageLocation, Set<String> tags, Date addedDate) {
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

    public int getLocationType() {
        return locationType;
    }

    public void setLocationType(int locationType) {
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
                .addAllTags(tags)
                .build();

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
