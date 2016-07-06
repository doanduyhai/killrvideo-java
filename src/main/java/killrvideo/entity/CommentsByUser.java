package killrvideo.entity;

import static java.util.UUID.fromString;
import static killrvideo.entity.Schema.KEYSPACE;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import info.archinnov.achilles.annotations.*;
import killrvideo.comments.CommentsServiceOuterClass;
import killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest;
import killrvideo.utils.TypeConverter;

@Table(keyspace = KEYSPACE, table = "comments_by_user")
public class CommentsByUser {

    @PartitionKey
    private UUID userid;

    @ClusteringColumn(asc = false)
    @TimeUUID
    private UUID commentid;

    @Column
    private UUID videoid;

    @Column
    private String comment;

    @Column
    @Computed(function = "toTimestamp", targetColumns = {"commentid"}, alias = "comment_timestamp", cqlClass = Date.class)
    private Date dateOfComment;

    public CommentsByUser() {
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
