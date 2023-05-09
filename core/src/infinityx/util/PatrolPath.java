package infinityx.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Represents a patrol path for an enemy. The path is a piecewise linear curve.
 */
public class PatrolPath {

    /** Current index into {@link #path} the enemy is considering moving towards */
    private int currentWayPoint;

    /** Vertices of path */
    Array<Vector2> path;

    /**
     * Creates a new PatrolPath instance with the given waypoints.
     *
     * @param patrolPath an array of waypoints
     */
    public PatrolPath(Array<Vector2> patrolPath) {
        this.path = patrolPath;
    }

    /**
     * Creates a new PatrolPath instance with an empty path.
     *
     */
    public PatrolPath() {
        this.path = new Array<>();
    }

    /**
     * Returns the next waypoint in the patrol path.
     *
     * @return the next waypoint in the patrol path
     */
    public Vector2 getNextPatrol() {
        Vector2 next = path.get(currentWayPoint);
        currentWayPoint++;
        if (currentWayPoint > path.size - 1) {
            if (path.size == 1) {
                currentWayPoint = 0;
                return next;
            }
            path.reverse();
            currentWayPoint = 1;
        }
        return next;
    }

    /**
     * Returns the number of waypoints in the patrol path.
     *
     * @return the number of waypoints in the patrol path
     */
    public int getWaypointCount() {
        return path.size;
    }

    /**
     * Returns the waypoint at the specified index.
     *
     * @param index the index of the waypoint to return
     * @return the waypoint at the specified index
     */
    public Vector2 getWaypointAtIndex(int index) {
        return path.get(index);
    }

    /**
     * Removes the waypoint at the specified index from the patrol path.
     *
     * @param index the index of the waypoint to remove
     */
    public void removeWaypoint(int index) {
        if (index < path.size) {
            path.removeIndex(index);
        }
    }

    public Array<Vector2> getPath() {
        return path;
    }

    /**
     * Appends a new vertex to the path at (x, y)
     */
    public PatrolPath addWaypoint(float x, float y) {
        path.add(new Vector2(x, y));
        return this;
    }

    /**
     * Adds a new vertex to the path at (x, y) at a specific index
     */
    public PatrolPath addWaypointAt(float x, float y, int index) {
        path.insert(index, new Vector2(x, y));
        return this;
    }
}
