package killrvideo.entity;

import java.util.Date;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import killrvideo.user_management.UserManagementServiceOuterClass.UserProfile;
import killrvideo.utils.TypeConverter;

@Table(keyspace = Schema.KEYSPACE, name = "users")
public class User {

    @PartitionKey
    private UUID userid;

    @NotBlank
    @Column
    private String firstname;

    @NotBlank
    @Column
    private String lastname;

    @NotBlank
    @Column
    private String email;

    @NotNull
    @Column(name = "created_date")
    private Date createdAt;

    public User() {
    }

    public User(UUID userid, String firstname, String lastname, String email, Date createdAt) {
        this.userid = userid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.createdAt = createdAt;
    }

    public UUID getUserid() {
        return userid;
    }

    public void setUserid(UUID userid) {
        this.userid = userid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public UserProfile toUserProfile() {
        return UserProfile.newBuilder()
                .setEmail(email)
                .setFirstName(firstname)
                .setLastName(lastname)
                .setUserId(TypeConverter.uuidToUuid(userid))
                .build();
    }
}
