package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.UUID;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;

@Table(keyspace = KEYSPACE, table = "uploaded_video_jobs")
public class UploadedVideoJobs {

    @PartitionKey
    private UUID videoid;

    @Column
    private String uploadUrl;

    @Column
    private String jobid;

    public UploadedVideoJobs() {
    }

    public UploadedVideoJobs(UUID videoid, String uploadUrl, String jobid) {
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
