package killrvideo.graph;

/**
 * Constants for use with our KillrVideo DSL (Domain Specific Language)
 * These effectively define the vertices, edges, and keys within
 * our KillrVideo graph schema.
 */
public interface KillrVideoTraversalConstants {

   String VERTEX_VIDEO = "video";
   
   String VERTEX_USER = "user";
   
   String VERTEX_TAG = "tag";

   String EDGE_RATED = "rated";
   
   String EDGE_UPLOADED = "uploaded";
   
   String EDGE_TAGGED_WITH = "taggedWith";

   String KEY_RATING = "rating";
   
   String KEY_USER_ID = "userId";
   
   String KEY_VIDEO_ID = "videoId";
   
   String KEY_NAME = "name";
   
   String KEY_EMAIL = "email";
   
   String KEY_ADDED_DATE = "added_date";
   
   String KEY_TAGGED_DATE = "tagged_date";
   
   String KEY_DESCRIPTION = "description";
   
   String KEY_PREVIEW_IMAGE_LOCATION = "preview_image_location";
   
}
