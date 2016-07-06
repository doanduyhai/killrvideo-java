package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import info.archinnov.achilles.annotations.ClusteringColumn;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;

@Table(keyspace = KEYSPACE, table = "tags_by_letter")
public class TagsByLetter {

    @PartitionKey
    @Column("first_letter")
    private String firstLetter;

    @ClusteringColumn
    private String tag;

    public TagsByLetter() {
    }

    public TagsByLetter(String firstLetter, String tag) {
        this.firstLetter = firstLetter;
        this.tag = tag;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
