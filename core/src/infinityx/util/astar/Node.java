package infinityx.util.astar;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;

public class Node {

    public final int x;
    public final int y;

    /**Is this node an obstable*/
    public boolean isObstacle;
    /**Index of the node in an array. Libgdx's assumes all the nodes representing a map are stored in an array*/
    private final int index;

    public float wx;
    public float wy;

    /**Edges from this node to another node*/
    private final Array<Connection<Node>> connections;

    public Node(AStarMap map, int x, int y, float wx, float wy) {
        this.x = x;
        this.y = y;
        this.wx = wx;
        this.wy = wy;
        this.index = x * map.getHeight() + y;
        this.isObstacle = false;
        this.connections = new Array<Connection<Node>>();
    }

    public int getIndex () {
        return index;
    }

    public Array<Connection<Node>> getConnections () {
        return connections;
    }

    @Override
    public String toString() {
        return "Node: (" + x + ", " + y + ")";
    }

}