package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.UUID;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;

@Table(keyspace = KEYSPACE, table = "user_credentials")
public class UserCredentials {

    @PartitionKey
    private String email;

    @Column
    private String password;

    @Column
    private UUID userid;

    public UserCredentials() {
    }

    public UserCredentials(String email, String password, UUID userid) {
        this.email = email;
        this.password = password;
        this.userid = userid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UUID getUserid() {
        return userid;
    }

    public void setUserid(UUID userid) {
        this.userid = userid;
    }
}
