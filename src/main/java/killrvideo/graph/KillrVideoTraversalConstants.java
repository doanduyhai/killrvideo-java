package killrvideo.graph;

/**
 * Constants for use with our KillrVideo DSL (Domain Specific Language)
 * These effectively define the vertices, edges, and keys within
 * our KillrVideo graph schema.
 */
public class KillrVideoTraversalConstants {

    public static final String VERTEX_VIDEO = "video";
    public static final String VERTEX_USER = "user";
    public static final String VERTEX_TAG = "tag";

    public static final String EDGE_RATED = "rated";
    public static final String EDGE_UPLOADED = "uploaded";
    public static final String EDGE_TAGGED_WITH = "taggedWith";

    public static final String KEY_RATING = "rating";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_VIDEO_ID = "videoId";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ADDED_DATE = "added_date";
    public static final String KEY_TAGGED_DATE = "tagged_date";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_PREVIEW_IMAGE_LOCATION = "preview_image_location";

}
