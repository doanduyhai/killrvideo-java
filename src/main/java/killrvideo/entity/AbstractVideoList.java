package killrvideo.entity;

import java.util.Date;
import java.util.UUID;

import info.archinnov.achilles.annotations.ClusteringColumn;
import info.archinnov.achilles.annotations.Column;

public class AbstractVideoList extends AbstractVideo {

    @ClusteringColumn(value = 1, asc = false)
    @Column("added_date")
    protected Date addedDate;

    @ClusteringColumn(2)
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
