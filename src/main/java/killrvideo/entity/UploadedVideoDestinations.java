package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

//import info.archinnov.achilles.annotations.Column;
//import info.archinnov.achilles.annotations.PartitionKey;
//import info.archinnov.achilles.annotations.Table;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = KEYSPACE, name = "uploaded_video_destinations")
public class UploadedVideoDestinations {

    @PartitionKey
    @Column(name = "upload_url")
    private String uploadUrl;

    @Column
    private String assetid;

    @Column
    private String filename;

    @Column
    private String locatorid;

    public UploadedVideoDestinations() {
    }

    public UploadedVideoDestinations(String uploadUrl, String assetid, String filename, String locatorid) {
        this.uploadUrl = uploadUrl;
        this.assetid = assetid;
        this.filename = filename;
        this.locatorid = locatorid;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getAssetid() {
        return assetid;
    }

    public void setAssetid(String assetid) {
        this.assetid = assetid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLocatorid() {
        return locatorid;
    }

    public void setLocatorid(String locatorid) {
        this.locatorid = locatorid;
    }
}
