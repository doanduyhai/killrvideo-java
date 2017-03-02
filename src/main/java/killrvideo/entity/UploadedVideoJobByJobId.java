package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.UUID;

//import info.archinnov.achilles.annotations.Column;
//import info.archinnov.achilles.annotations.PartitionKey;
//import info.archinnov.achilles.annotations.Table;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = KEYSPACE, name = "uploaded_video_jobs_by_jobid")
public class UploadedVideoJobByJobId {

    @PartitionKey
    private String jobid;

    @Column(name = "upload_url")
    private String uploadUrl;

    @Column
    private UUID videoid;

    public UploadedVideoJobByJobId() {
    }

    public UploadedVideoJobByJobId(UUID videoid, String uploadUrl, String jobid) {
        this.videoid = videoid;
        this.uploadUrl = uploadUrl;
        this.jobid = jobid;
    }

    public UUID getVideoid() {
        return videoid;
    }

    public void setVideoid(UUID videoid) {
        this.videoid = videoid;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getJobid() {
        return jobid;
    }

    public void setJobid(String jobid) {
        this.jobid = jobid;
    }
}
