package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.Date;
import java.util.UUID;

import info.archinnov.achilles.annotations.*;
import killrvideo.search.SearchServiceOuterClass;
import killrvideo.search.SearchServiceOuterClass.SearchResultsVideoPreview;
import killrvideo.utils.TypeConverter;

@Table(keyspace = KEYSPACE, table = "videos_by_tag")
public class VideoByTag extends AbstractVideo{

    @PartitionKey
    private String tag;

    @ClusteringColumn
    private UUID videoid;

    @Column("added_date")
    private Date addedDate;

    @Column
    private UUID userid;

    @Column("tagged_date")
    private Date taggedDate;

    public VideoByTag() {
    }

    public VideoByTag(String tag, UUID videoid, UUID userid, Date taggedDate, Date addedDate) {
        this.tag = tag;
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
}
