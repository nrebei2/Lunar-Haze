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
                // Manhattan distance
                return Math.abs(endNode.x - node.x) + Math.abs(endNode.y - node.y);
            }
        };
    }

    /**
     * Finds the next node to move to, given a source and target
     */
    public Path findPath(Vector2 source, Vector2 target, RaycastCollisionDetector ray) {
        int sourceX = MathUtils.floor(source.x);
        int sourceY = MathUtils.floor(source.y);
        int targetX = MathUtils.floor(target.x);
        int targetY = MathUtils.floor(target.y);

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

        if (connectionPath.getCount() < 2) {
            return null;
        }

        Array<Vector2> waypoints = new Array<>();
        for (int i = 0; i < connectionPath.getCount(); i++) {
            Node node = connectionPath.get(i);
            waypoints.add(new Vector2(node.wx, node.wy));
        }

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