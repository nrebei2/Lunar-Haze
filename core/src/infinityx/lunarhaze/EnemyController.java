package infinityx.lunarhaze;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class EnemyController {
    /**
     * Enumeration to encode the finite state machine.
     */
    private static enum FSMState {
        /** The enemy just spawned */
        SPAWN,
        /** The enemy is patrolling around where the target was last seen */
        WANDER,
        /** The enemy has a target, but must get closer */
        CHASE,
        /** The enemy has a target and is attacking it */
        ATTACK,
        /** The enemy lost its target and is returnng to its patrolling path */
        RETURN,
    }

    // Instance Attributes
    /** The ship being controlled by this AIController */
    private Enemy enemy;
    /** The game board; used for pathfinding */
    private Board board;
    /** The ship's current state in the FSM */
    private FSMState state;
    /** The target ship (to chase or attack). */
    private Werewolf target;
    /** The enemy next action (may include firing). */
    private int move; // A ControlCode
    /** The number of ticks since we started this controller */
    private long ticks;

    public EnemyController(){

        throw new NotImplementedException();
    }

    /**
     * Returns the action selected by this InputController
     *
     * The returned int is a bit-vector of more than one possible input
     * option. This is why we do not use an enumeration of Control Codes;
     * Java does not (nicely) provide bitwise operation support for enums.
     *
     * This function tests the environment and uses the FSM to chose the next
     * action of the ship. This function SHOULD NOT need to be modified.  It
     * just contains code that drives the functions that you need to implement.
     *
     * @return the action selected by this InputController
     */
    public int getAction() {
        throw new NotImplementedException();
    }

    // FSM Code for Targeting (MODIFY ALL THE FOLLOWING METHODS)

    /**
     * Change the state of the enemy.
     *
     * A Finite State Machine (FSM) is just a collection of rules that,
     * given a current state, and given certain observations about the
     * environment, chooses a new state. For example, if we are currently
     * in the ATTACK state, we may want to switch to the CHASE state if the
     * target gets out of range.
     */
    private void changeStateIfApplicable() {
        throw new NotImplementedException();
    }

    /**
     * Acquire a target to attack (and put it in field target).
     */
    private void selectTarget() {
        throw new NotImplementedException();

    }


    /**
     * Returns true if we can hit a target from here.
     *
     * @param x The x-index of the source tile
     * @param y The y-index of the source tile
     *
     * @return true if we can hit a target from here.
     */
    private boolean canHitTargetFrom(int x, int y) {
        throw new NotImplementedException();
    }

    /**
     * Returns true if we can both fire and hit our target
     *
     * If we can fire now, and we could hit the target from where we are,
     * we should hit the target now.
     *
     * @return true if we can both fire and hit our target
     */
    private boolean canHitTarget() {
        throw new NotImplementedException();
    }

    /**
     * Returns true if we detected the player (when the player walks in line of sight)
     *
     * @return true if we can both fire and hit our target
     */
    private boolean detectedPlayer(){
        throw new NotImplementedException();
    }






}
