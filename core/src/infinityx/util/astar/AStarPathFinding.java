package infinityx.util.astar;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.PathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.ai.steer.utils.Path;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.physics.RaycastInfo;

/**
 * A* pathfinding utility class
 */
public class AStarPathFinding {
    /**
     * Map containing all the nodes in our level
     */
    public final AStarMap map;

    /**
     * Libgdx's pathfinder
     */
    private final PathFinder<Node> pathfinder;

    /**
     * Heuristic function used in A* algorithm
     */
    private final Heuristic<Node> heuristic;

    /**
     * The output path for pathfinder
     */
    private final SmoothGraphPath<Node> connectionPath;

    /**
     * Waypoints cache for findPath
     */
    private Array<Vector2> waypoints;

    /**
     * @param map Map pathfinding will be perform on
     */
    public AStarPathFinding(AStarMap map) {
        this.map = map;
        this.pathfinder = new IndexedAStarPathFinder(createGraph(map));
        this.connectionPath = new SmoothGraphPath();
        this.waypoints = new Array<>();
        this.heuristic = new Heuristic<Node>() {
            @Override
            public float estimate(Node node, Node endNode) {
                // Euclidean distance, enemy should prefer diagonal movement if possible
                return endNode.position.dst(node.position);
            }
        };

        RaycastInfo collRay = new RaycastInfo(null) {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                // Right now, all hit bodies are contained in GameObjects
                GameObject objHit = (GameObject) fixture.getBody().getUserData();

                if (ignore.contains(objHit.getType())) {
                    return 1;
                }

                outputCollision.set(point, normal);
                this.fixture = fixture;
                this.fraction = fraction;
                this.hit = fraction != 0;
                if (this.hit) {
                    this.hitObject = objHit;
                }
                return fraction;
            }
        };
    }

    /**
     * @param source world position of source
     * @param target world position of target
     * @return Path from source to target using A*
     */
    public Path findPath(Vector2 source, Vector2 target) {
        // Check adjacent nodes if source or target node is an obstacle
        Node sourceNode = map.getNodeAtWorld(source.x, source.y);
        if (sourceNode.isObstacle) {
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    Node next = map.getNodeAt(sourceNode.x + i, sourceNode.y + j);
                    float dot = target.dot(next.position) - target.dot(source) - source.dot(next.position) + source.len2();
                    if (!next.isObstacle && dot < 0) sourceNode = next;
                }
            }
        }

        Node targetNode = map.getNodeAtWorld(target.x, target.y);
        if (targetNode.isObstacle) {
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    Node next = map.getNodeAt(targetNode.x + i, targetNode.y + j);
                    float dot = source.dot(next.position) - source.dot(target) - target.dot(next.position) + target.len2();
                    if (!next.isObstacle && dot < 0) targetNode = next;
                }
            }
        }

        if (sourceNode == null || targetNode == null) {
            return null;
        }

        connectionPath.clear();
        pathfinder.searchNodePath(sourceNode, targetNode, heuristic, connectionPath);

        // Use the source and target world positions instead of start and goal node
        // This is so we always have at least two waypoints and the path is more accurate
        waypoints.clear();
        waypoints.add(source);
        for (int i = 1; i < connectionPath.getCount() - 1; i++) {
            Node node = connectionPath.get(i);
            waypoints.add(node.position);
        }
        waypoints.add(target);

        Path path = new LinePath(waypoints);
        return path;
    }

    private static final int[][] NEIGHBORHOOD = new int[][]{
            new int[]{-1, 0},
            new int[]{0, -1},
            new int[]{0, 1},
            new int[]{1, 0},
            new int[]{-1, 1},
            new int[]{-1, -1},
            new int[]{1, -1},
            new int[]{1, 1}
    };

    /**
     * Create graph with adjacent and diagonal connections between nodes.
     *
     * @param map Map holding nodes
     */
    public static AStarGraph createGraph(AStarMap map) {
        final int height = map.getHeight();
        final int width = map.getWidth();
        AStarGraph graph = new AStarGraph(map);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Node node = map.getNodeAt(x, y);
                if (node.isObstacle) {
                    continue;
                }
                // Add a connection for each valid neighbor
                for (int offset = 0; offset < NEIGHBORHOOD.length; offset++) {
                    int neighborX = node.x + NEIGHBORHOOD[offset][0];
                    int neighborY = node.y + NEIGHBORHOOD[offset][1];
                    if (neighborX >= 0 && neighborX < width && neighborY >= 0 && neighborY < height) {
                        Node neighbor = map.getNodeAt(neighborX, neighborY);
                        if (!neighbor.isObstacle) {
                            // Add connection to walkable neighbor
                            node.getConnections().add(new DefaultConnection(node, neighbor));
                        }
                    }
                }
                node.getConnections().shuffle();
            }
        }
        return graph;
    }


    /**
     * Wrapper around AStarMap to implement IndexedGraph
     */
    public static class AStarGraph implements IndexedGraph<Node> {
        AStarMap map;

        public AStarGraph(AStarMap map) {
            this.map = map;
        }

        @Override
        public int getIndex(Node node) {
            return node.getIndex();
        }

        @Override
        public Array<Connection<Node>> getConnections(Node fromNode) {
            return fromNode.getConnections();
        }

        @Override
        public int getNodeCount() {
            return map.getHeight() * map.getWidth();
        }

    }
}