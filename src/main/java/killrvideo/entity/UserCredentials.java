package killrvideo.entity;

import static killrvideo.entity.Schema.KEYSPACE;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = KEYSPACE, name = "user_credentials")
public class UserCredentials {

    @PartitionKey
    private String email;

    @NotBlank
    @Column(name = "password")
    private String password;

    @NotNull
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
