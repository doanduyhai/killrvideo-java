package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Date;
import java.util.UUID;


import javax.validation.constraints.NotNull;

import info.archinnov.achilles.annotations.*;
import killrvideo.search.SearchServiceOuterClass.SearchResultsVideoPreview;
import killrvideo.suggested_videos.SuggestedVideosService.SuggestedVideoPreview;
import killrvideo.utils.TypeConverter;

@Table(keyspace = KEYSPACE, table = "videos_by_tag")
public class VideoByTag extends AbstractVideo{

    @PartitionKey
    private String tag;

    @ClusteringColumn
    private UUID videoid;

    @NotNull
    @Column("added_date")
    private Date addedDate;

    @NotNull
    @Column
    private UUID userid;

    @NotNull
    @Column("tagged_date")
    private Date taggedDate;

    public VideoByTag() {
    }

    public VideoByTag(String tag, UUID videoid, UUID userid, String name, String previewImageLocation, Date taggedDate, Date addedDate) {
        this.tag = tag;
        this.name = name;
        this.previewImageLocation = previewImageLocation;
        this.videoid = videoid;
        this.userid = userid;
        this.addedDate = addedDate;
        this.taggedDate = taggedDate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    public UUID getUserid() {
        return userid;
    }

    public void setUserid(UUID userid) {
        this.userid = userid;
    }

    public Date getTaggedDate() {
        return taggedDate;
    }

    public void setTaggedDate(Date taggedDate) {
        this.taggedDate = taggedDate;
    }

    public SearchResultsVideoPreview toResultVideoPreview() {
        return SearchResultsVideoPreview
                .newBuilder()
                .setAddedDate(TypeConverter.dateToTimestamp(addedDate))
                .setName(name)
                .setPreviewImageLocation(previewImageLocation)
                .setUserId(TypeConverter.uuidToUuid(userid))
                .setVideoId(TypeConverter.uuidToUuid(videoid))
                .build();
    }

    public SuggestedVideoPreview toSuggestedVideoPreview() {
        return SuggestedVideoPreview
                .newBuilder()
                .setVideoId(TypeConverter.uuidToUuid(videoid))
                .setAddedDate(TypeConverter.dateToTimestamp(addedDate))
                .setName(name)
                .setPreviewImageLocation(previewImageLocation)
                .setUserId(TypeConverter.uuidToUuid(userid))
                .build();
    }
}
