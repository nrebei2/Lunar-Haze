package infinityx.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Represents a patrol path for an enemy. The path is a piecewise linear curve.
 */
public class PatrolPath {
    private int currentWayPoint;
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
     * Returns the next waypoint in the patrol path.
     *
     * @return the next waypoint in the patrol path
     */
    public Vector2 getNextPatrol() {
        Vector2 next = path.get(currentWayPoint);
        currentWayPoint++;
        if (currentWayPoint > path.size - 1) {
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
}
