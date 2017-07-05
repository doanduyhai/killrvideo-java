package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import org.hibernate.validator.constraints.NotBlank;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = KEYSPACE, name = "tags_by_letter")
public class TagsByLetter {

    @PartitionKey
    @Column(name = "first_letter")
    private String firstLetter;

    @NotBlank
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
