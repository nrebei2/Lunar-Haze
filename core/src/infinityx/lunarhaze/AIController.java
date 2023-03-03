package infinityx.lunarhaze;

import com.badlogic.gdx.utils.Queue;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class AIController {

    private static enum action {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_RIGHT,
        MOVE_LEFT,
    }

    /**
     * Mark all desirable tiles to move to.
     *
     * This method implements pathfinding through the use of goal tiles.
     * It searches for all desirable tiles to move to (there may be more than
     * one), and marks each one as a goal. Then, the pathfinding method
     * getMoveAlongPathToGoalTile() moves the ship towards the closest one.
     *
     * POSTCONDITION: There is guaranteed to be at least one goal tile
     * when completed.
     */
    private void markGoalTiles() {

    }



    /**
     * Returns a movement direction that moves towards a goal tile.
     *
     * This is one of the longest parts of the assignment. Implement
     * breadth-first search (from 2110) to find the best goal tile
     * to move to. However, just return the movement direction for
     * the next step, not the entire path.
     *
     * The value returned should be a control code.  See PlayerController
     * for more information on how to use control codes.
     *
     * @return a movement direction that moves towards a goal tile.
     */
    private int getMoveAlongPathToGoalTile() {
        throw new NotImplementedException();
    }

    private int getDirection(Tile t){
        throw new NotImplementedException();
    }


}

