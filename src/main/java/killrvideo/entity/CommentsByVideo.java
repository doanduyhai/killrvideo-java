package killrvideo.entity;

import static java.util.UUID.fromString;
import static killrvideo.entity.Schema.KEYSPACE;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

//import info.archinnov.achilles.annotations.*;
//import info.archinnov.achilles.validation.Validator;
import com.datastax.driver.mapping.annotations.*;
import killrvideo.comments.CommentsServiceOuterClass;
import killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest;
import killrvideo.utils.TypeConverter;

@Table(keyspace = KEYSPACE, name = "comments_by_video")
public class CommentsByVideo {

    @PartitionKey
    private UUID videoid;

    @ClusteringColumn
    //:TODO Is there TimeUUID support in DSE driver?
    //@TimeUUID
    private UUID commentid;

    @NotNull
    @Column
    private UUID userid;

    @NotBlank
    @Column
    private String comment;

//    @NotNull
//    @Column
    //:TODO figure out to to convert Computed annotation
    //@Computed(function = "toTimestamp", targetColumns = {"commentid"}, alias = "comment_timestamp", cqlClass = Date.class)
    @Computed("dateOf(commentid)")
    private Date dateOfComment;

    public CommentsByVideo() {
    }

    public CommentsByVideo(UUID videoid, UUID commentid, UUID userid, String comment) {
        this.videoid = videoid;
        this.commentid = commentid;
        this.userid = userid;
        this.comment = comment;
    }

    public CommentsByVideo(CommentOnVideoRequest request) {
        this.videoid = fromString(request.getVideoId().getValue());
        this.commentid = fromString(request.getCommentId().getValue());
        this.userid = fromString(request.getUserId().getValue());
        this.comment = request.getComment();
    }

    public UUID getVideoid() {
        return videoid;
    }

    public void setVideoid(UUID videoid) {
        this.videoid = videoid;
    }

    public UUID getCommentid() {
        return commentid;
    }

    public void setCommentid(UUID commentid) {
        this.commentid = commentid;
    }

    public UUID getUserid() {
        return userid;
    }

    public void setUserid(UUID userid) {
        this.userid = userid;
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

    public CommentsServiceOuterClass.VideoComment toVideoComment() {
        return CommentsServiceOuterClass.VideoComment
                .newBuilder()
                .setComment(comment)
                .setCommentId(TypeConverter.uuidToTimeUuid(commentid))
                .setUserId(TypeConverter.uuidToUuid(userid))
                .setCommentTimestamp(TypeConverter.dateToTimestamp(dateOfComment))
                .build();
    }
}
