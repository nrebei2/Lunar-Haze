package infinityx.util.astar;

import com.badlogic.gdx.ai.pfa.*;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.ai.steer.utils.Path;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.LevelContainer;

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
     * Graph of nodes and edges. Used for A* pathfinding
     */
    private final SmoothableGraphPath<Node, Vector2> connectionPath;

    public AStarPathFinding(AStarMap map) {
        this.map = map;
        this.pathfinder = new IndexedAStarPathFinder<Node>(createGraph(map));
        this.connectionPath = new SmoothGraphPath();
        this.heuristic = new Heuristic<Node>() {
            @Override
            public float estimate(Node node, Node endNode) {
                // Euclidean distance, enemy should prefer diagonal movement if possible
                return Vector2.dst(endNode.wx, endNode.wy, node.wx, node.wy);
            }
        };
    }

    /**
     * @param source world position of source
     * @param target world position of target
     * @return Path from source to target using A*
     */
    public Path findPath(Vector2 source, Vector2 target, RaycastCollisionDetector ray) {

        // World to grid
        int sourceX = map.worldToGridX(source.x);
        int sourceY = map.worldToGridY(source.y);
        int targetX = map.worldToGridX(target.x);
        int targetY = map.worldToGridY(target.y);

        if (map == null
                || sourceX < 0 || sourceX >= map.getWidth()
                || sourceY < 0 || sourceY >= map.getHeight()
                || targetX < 0 || targetX >= map.getWidth()
                || targetY < 0 || targetY >= map.getHeight()) {
            return null;
        }

        Node sourceNode = map.getNodeAt(sourceX, sourceY);
        Node targetNode = map.getNodeAt(targetX, targetY);

        connectionPath.clear();
        pathfinder.searchNodePath(sourceNode, targetNode, heuristic, connectionPath);


        // TODO
        //PathSmoother smoother = new PathSmoother(ray);
        //int removed = smoother.smoothPath(connectionPath);
        //System.out.println("removed " + removed);


        Array<Vector2> waypoints = new Array<>();

        // Use the source and target world positions instead of start and goal node
        // This is so we always have at least two waypoints and the path is more accurate
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
                            node.getConnections().add(new DefaultConnection<Node>(node, neighbor));
                        }
                    }
                }
                node.getConnections().shuffle();
            }
        }
        return graph;
    }


    /** Wrapper around AStarMap to implement IndexedGraph */
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