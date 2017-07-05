package killrvideo.entity;

import java.util.Date;
import java.util.UUID;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.ClusteringColumn;

public class AbstractVideoList extends AbstractVideo {

    @ClusteringColumn()
    @Column(name = "added_date")
    protected Date addedDate;

    @ClusteringColumn(1)
    protected UUID videoid;

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public UUID getVideoid() {
        return videoid;
    }

    public void setVideoid(UUID videoid) {
        this.videoid = videoid;
    }
}
