package killrvideo.entity;

import static java.util.UUID.fromString;
import static killrvideo.entity.Schema.KEYSPACE;

import java.util.Date;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;

import com.datastax.driver.mapping.annotations.*;
import killrvideo.comments.CommentsServiceOuterClass;
import killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest;
import killrvideo.utils.TypeConverter;

@Table(keyspace = KEYSPACE, name = "comments_by_user")
public class CommentsByUser {

    @PartitionKey
    private UUID userid;

    @ClusteringColumn
    private UUID commentid;

    @NotNull
    @Column
    private UUID videoid;

    @NotBlank
    @Column
    private String comment;

    /**
     * In order to properly use the @Computed annotation for dateOfComment
     * you must execute a query using the mapper with this entity, NOT QueryBuilder.
     * If QueryBuilder is used you must use a call to fcall() and pass the CQL function
     * needed to it directly.  Here is an example pulled from CommentsByVideo.getVideoComments().
     * fcall("toTimestamp", QueryBuilder.column("commentid")).as("comment_timestamp")
     * This will execute the toTimeStamp() function against the commentid column and return the
     * result with an alias of comment_timestamp.  Again, reference CommentService.getUserComments()
     * or CommentService.getVideoComments() for examples of how to implement.
     */
    @NotNull
    @Computed("toTimestamp(commentid)")
    private Date dateOfComment;

    public CommentsByUser() {
    }

    public CommentsByUser(UUID userid, UUID commentid, UUID videoid, String comment) {
        this.userid = userid;
        this.commentid = commentid;
        this.videoid = videoid;
        this.comment = comment;
    }

    public CommentsByUser(CommentOnVideoRequest request) {
        this.userid = fromString(request.getUserId().getValue());
        this.commentid = fromString(request.getCommentId().getValue());
        this.videoid = fromString(request.getVideoId().getValue());
        this.comment = request.getComment();
    }


    public UUID getUserid() {
        return userid;
    }

    public void setUserid(UUID userid) {
        this.userid = userid;
    }

    public UUID getCommentid() {
        return commentid;
    }

    public void setCommentid(UUID commentid) {
        this.commentid = commentid;
    }

    public UUID getVideoid() {
        return videoid;
    }

    public void setVideoid(UUID videoid) {
        this.videoid = videoid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDateOfComment() {
        return dateOfComment;
    }

    public void setDateOfComment(Date dateOfComment) {
        this.dateOfComment = dateOfComment;
    }

    public CommentsServiceOuterClass.UserComment toUserComment() {
        return CommentsServiceOuterClass.UserComment
                .newBuilder()
                .setComment(comment)
                .setCommentId(TypeConverter.uuidToTimeUuid(commentid))
                .setVideoId(TypeConverter.uuidToUuid(videoid))
                .setCommentTimestamp(TypeConverter.dateToTimestamp(dateOfComment))
                .build();
    }
}
