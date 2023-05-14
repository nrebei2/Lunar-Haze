package infinityx.util.astar;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * A node for a {@link AStarMap}
 */
public class Node {
    /**
     * Whether there is an obstacle on this node
     */
    public boolean isObstacle;
    /**
     * Index of the node in an array. Libgdx's assumes all the nodes representing a map are stored in an array
     */
    private int index;

    /**
     * World position assigned to this node. Represents the center of the grid tile
     */
    public Vector2 position;

    /**
     * Edges from this node to another node
     */
    private Array<Connection<Node>> connections;

    /**
     * Grid position in map
     */
    public int x, y;

    /**
     * Create a new Node with attributes
     *
     * @param map Map holding this node
     * @param x   Grid x-position in map
     * @param y   Grid y-position map
     * @param wx  world x-position
     * @param wy  world y-position
     */
    public Node(AStarMap map, int x, int y, float wx, float wy) {
        this.x = x;
        this.y = y;
        this.position = new Vector2(wx, wy);
        this.index = x * map.getHeight() + y;
        this.isObstacle = false;
        this.connections = new Array<>();
    }

    /**
     * Creates a new Node with only its position attribute defined
     */
    public Node(float wx, float wy) {
        this.position = new Vector2(wx, wy);
    }

    public int getIndex() {
        return index;
    }

    public Array<Connection<Node>> getConnections() {
        return connections;
    }

    @Override
    public String toString() {
        return "Node: (" + x + ", " + y + ")";
    }

}