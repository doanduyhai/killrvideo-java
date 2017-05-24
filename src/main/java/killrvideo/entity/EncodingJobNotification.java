package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.Date;
import java.util.UUID;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;


@Table(keyspace = KEYSPACE, name = "encoding_job_notifications")
public class EncodingJobNotification {

    @PartitionKey
    private UUID videoid;

    @ClusteringColumn(1)
    @Column(name = "status_date")
    private Date statusDate;

    @ClusteringColumn(2)
    private String etag;

    @Column
    private String jobid;

    @Column
    private String newstate;

    @Column
    private String oldstate;

    public EncodingJobNotification() {
    }

    public EncodingJobNotification(UUID videoid, Date statusDate, String etag, String jobid, String newstate, String oldstate) {
        this.videoid = videoid;
        this.statusDate = statusDate;
        this.etag = etag;
        this.jobid = jobid;
        this.newstate = newstate;
        this.oldstate = oldstate;
    }

    public UUID getVideoid() {
        return videoid;
    }

    public void setVideoid(UUID videoid) {
        this.videoid = videoid;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getJobid() {
        return jobid;
    }

    public void setJobid(String jobid) {
        this.jobid = jobid;
    }

    public String getNewstate() {
        return newstate;
    }

    public void setNewstate(String newstate) {
        this.newstate = newstate;
    }

    public String getOldstate() {
        return oldstate;
    }

    public void setOldstate(String oldstate) {
        this.oldstate = oldstate;
    }
}
