package killrvideo.graph;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Date;
import java.util.UUID;

import static killrvideo.graph.KillrVideoTraversalConstants.*;

/**
 * Traversal Source for our KillrVideo graph DSL (Domain Specific Language)
 *
 * Initial DSL credit goes to Stephen Mallette who provided me with a working
 * KillrVideo DSL example that I used as the basis for the following code.
 * Be sure to check out his excellent blog post explaining DSL's
 * here -> https://www.datastax.com/dev/blog/gremlin-dsls-in-java-with-dse-graph
 */
public class KillrVideoTraversalSourceDsl extends GraphTraversalSource {

    public KillrVideoTraversalSourceDsl(final Graph graph, final TraversalStrategies traversalStrategies) {
        super(graph, traversalStrategies);
    }

    public KillrVideoTraversalSourceDsl(final Graph graph) {
        super(graph);
    }

    /**
     * Provides a traversal that filters on one or more videos
     */
    public GraphTraversal<Vertex, Vertex> videos(String... videoIds) {
        GraphTraversal traversal = this.clone().V();
        traversal = traversal.hasLabel(VERTEX_VIDEO);
        if (videoIds.length == 1)
            traversal = traversal.has(KEY_VIDEO_ID, videoIds);
        else if (videoIds.length > 1)
            traversal = traversal.has(KEY_VIDEO_ID, P.within(videoIds));

        return traversal;
    }

    /**
     * Provides a traversal that filters on one or more users
     */
    public GraphTraversal<Vertex, Vertex> users(String... userIds) {
        GraphTraversal traversal = this.clone().V();
        traversal = traversal.hasLabel(VERTEX_USER);
        if (userIds.length == 1)
            traversal = traversal.has(KEY_USER_ID, userIds);
        else if (userIds.length > 1)
            traversal = traversal.has(KEY_USER_ID, P.within(userIds));

        return traversal;
    }

    /**
     * Provides a traversal that filters on one or more tags
     */
    public GraphTraversal<Vertex, Vertex> tags(String... tags) {
        GraphTraversal traversal = this.clone().V();
        traversal = traversal.hasLabel(VERTEX_TAG);
        if (tags.length == 1)
            traversal = traversal.has(KEY_NAME, tags);
        else if (tags.length > 1)
            traversal = traversal.has(KEY_NAME, P.within(tags));

        return traversal;
    }

    /**
     * Creates a video vertex if one does not exist and allows for updating
     * video vertex properties.
     * @param videoId
     * @param name
     * @param added_date
     * @param description
     * @param previewImageLocation
     * @return
     */
    public GraphTraversal<Vertex, Vertex> video(UUID videoId, String name, Date added_date, String description, String previewImageLocation) {
        if (null == videoId)
            throw new IllegalArgumentException("The videoId must not be null");
        if (null == name || name.isEmpty())
            throw new IllegalArgumentException("The name of the video must not be null or empty");
        if (null == added_date)
            throw new IllegalArgumentException("The added_date must not be null");
        if (null == description || description.isEmpty())
            throw new IllegalArgumentException("The description must not be null or empty");
        if (null == previewImageLocation || previewImageLocation.isEmpty())
            throw new IllegalArgumentException("The previewImageLocation must not be null or empty");

        GraphTraversal traversal = this.clone().V();

        return traversal
                .has(VERTEX_VIDEO, KEY_VIDEO_ID, videoId)
                .fold()
                .coalesce(
                        __.unfold(),
                        __.addV(VERTEX_VIDEO).property(KEY_VIDEO_ID, videoId)
                )
                .property(KEY_ADDED_DATE, added_date)
                .property(KEY_NAME, name)
                .property(KEY_DESCRIPTION, description)
                .property(KEY_PREVIEW_IMAGE_LOCATION, previewImageLocation);
    }

    /**
     * Creates a user vertex if one does not exist and does not allow for updating
     * user vertex properties.  This was done to match current application design that
     * does not allow for altering user properties once created.
     * @param userId
     * @param email
     * @param added_date
     * @return
     */
    public GraphTraversal<Vertex, Vertex> user(UUID userId, String email, Date added_date) {
        if (null == userId)
            throw new IllegalArgumentException("The userId must not be null");
        if (null == email || email.isEmpty())
            throw new IllegalArgumentException("The email of the user must not be null or empty");
        if (null == added_date)
            throw new IllegalArgumentException("The added_date must not be null");

        GraphTraversal traversal = this.clone().V();

        return traversal
                .has(VERTEX_USER, KEY_USER_ID, userId)
                .fold()
                .coalesce(
                        __.unfold(),
                        __.addV(VERTEX_USER)
                                .property(KEY_USER_ID, userId)
                                .property(KEY_ADDED_DATE, added_date)
                                .property(KEY_EMAIL, email)
                );
    }

    /**
     * Creates a tag vertex if one does not exist and does not allow for updating
     * tag vertex properties.
     * @param name
     * @param tagged_date
     * @return
     */
    public GraphTraversal<Vertex, Vertex> tag(String name, Date tagged_date) {
        if (null == name || name.isEmpty())
            throw new IllegalArgumentException("The name of the tag must not be null or empty");
        if (null == tagged_date)
            throw new IllegalArgumentException("The tagged_date must not be null");

        GraphTraversal traversal = this.clone().V();

        return traversal
                .has(VERTEX_TAG, KEY_NAME, name)
                .fold()
                .coalesce(
                        __.unfold(),
                        __.addV(VERTEX_TAG)
                                .property(KEY_NAME, name)
                                .property(KEY_TAGGED_DATE, tagged_date)
                );
    }

}
