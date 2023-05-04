package infinityx.util.astar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.*;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.ai.steer.utils.Path;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.physics.Box2DRaycastCollision;
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
    private final SmoothableGraphPath<Node, Vector2> connectionPath;

    /**
     * Waypoints cache for findPath
     */
    private Array<Vector2> waypoints;

    /**
     * Collision detector for path smoothing
     */
    public RaycastCollisionDetector raycastCollisionDetector;

    /**
     * @param map   Map pathfinding will be perform on
     * @param world Box2D world
     */
    public AStarPathFinding(AStarMap map, World world) {
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

//        RaycastInfo collRay = new RaycastInfo(null);
//        // collider should only care about scene objects
//        collRay.addIgnores(GameObject.ObjectType.WEREWOLF, GameObject.ObjectType.HITBOX, GameObject.ObjectType.ENEMY);
//        this.raycastCollisionDetector = new Box2DRaycastCollision(world, collRay);
    }

    /**
     * @param source world position of source
     * @param target world position of target
     * @return Path from source to target using A*
     */
    public Path findPath(Vector2 source, Vector2 target) {
        // World to grid
        Node sourceNode = map.getNodeAtWorld(source.x, source.y);
        if (sourceNode.isObstacle){
            for (Connection<Node> neighbor: sourceNode.getConnections()){
                Node next = neighbor.getToNode();
                if (!next.isObstacle) sourceNode = map.getNodeAtWorld(next.x, next.y);
            }
        }

        Node targetNode = map.getNodeAtWorld(target.x, target.y);

        if (targetNode.isObstacle){
            for (Connection<Node> neighbor: targetNode.getConnections()){
                Node next = neighbor.getToNode();
                if (!next.isObstacle) targetNode = map.getNodeAtWorld(next.x, next.y);
            }
        }

        if (sourceNode == null || targetNode == null) {
            System.out.printf("source : %s, target: %s\n", source, target);
            System.out.println(map);
            Gdx.app.error("AStarPathFinding", "Source or target is outside pathfinding range!", new IllegalStateException());
        }

        connectionPath.clear();
        pathfinder.searchNodePath(sourceNode, targetNode, heuristic, connectionPath);

//        PathSmoother smoother = new PathSmoother(raycastCollisionDetector);
//        int removed = smoother.smoothPath(connectionPath);
////        System.out.println("removed " + removed);

        // Use the source and target world positions instead of start and goal node
        // This is so we always have at least two waypoints and the path is more accurate
        waypoints.clear();
        waypoints.add(source);
        for (int i = 1; i < connectionPath.getCount() - 1; i++) {
            Node node = connectionPath.get(i);
            waypoints.add(node.position);
        }
        waypoints.add(target);

        //System.out.println("source "  + sourceNode.wx + ", " + sourceNode.wy );
        //System.out.println("target " + targetNode.wx + ", " + targetNode.wy );
        //System.out.println("start of path");
        //System.out.println(waypoints);

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