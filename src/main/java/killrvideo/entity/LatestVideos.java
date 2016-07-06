package killrvideo.entity;

import java.util.Date;
import java.util.UUID;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;

@Table(keyspace = Schema.KEYSPACE, table = "latest_videos")
public class LatestVideos extends AbstractVideoList{

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
}
