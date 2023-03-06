package infinityx.lunarhaze;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class EnemyController {

    // Constants for the control codes
    // We would normally use an enum here, but Java enums do not bitmask nicely
    /** Do not do anything */
    public static final int CONTROL_NO_ACTION  = 0x00;
    /** Move the ship to the left */
    public static final int CONTROL_MOVE_LEFT  = 0x01;
    /** Move the ship to the right */
    public static final int CONTROL_MOVE_RIGHT = 0x02;
    /** Move the ship to the up */
    public static final int CONTROL_MOVE_UP    = 0x04;
    /** Move the ship to the down */
    public static final int CONTROL_MOVE_DOWN  = 0x08;
    /** Fire the ship weapon */
    public static final int CONTROL_ATTACK 	   = 0x10;

    private static final float DETECT_DIST = 2;
    private static final float CHASE_DIST = 4;
    private static final int ATTACK_DIST = 1;

    /**
     * Enumeration to encode the finite state machine.
     */
    public enum FSMState {
        /** The enemy just spawned */
        SPAWN,
        /** The enemy is patrolling around a set path */
        PATROL,
        /** The enemy is wandering around where target is last seen*/
        WANDER,
        /** The enemy has a target, but must get closer */
        CHASE,
        /** The enemy has a target and is attacking it */
        ATTACK,
        /** The enemy lost its target and is returnng to its patrolling path */
        RETURN,
    }

    private enum Detection {
        /** The enemy can only detect the player in a straight line*/
        LINE,
        /** The enemy can  detect the player in an area line*/
        AREA,

    }
    /**
     * Enumeration to describe what direction the enemy is facing
     */


    // Instance Attributes
    /** The enemy being controlled by this AIController */
    private Enemy enemy;

    private Detection detection;

    /** The other enemies; used to find targets */
    private EnemyList enemies;

    /** The game board; used for pathfinding */
    private Board board;
    /** The ship's current state in the FSM */
    private FSMState state;
    /** The target ship (to chase or attack). */
    private Werewolf target;


    /** The enemy next action (may include firing). */
    private int move; // A ControlCode

    /** The direction the enemy is facing*/

    /** The number of ticks since we started this controller */
    private long ticks;




    /**
     * Creates an EnemyController for the enemy with the given id.
     *
     * @param id The unique ship identifier
     * @param board The game board (for pathfinding)
     * @param enemies The list of enemies (for detection)
     */
    public EnemyController( int id, Werewolf target, EnemyList enemies, Board board){
        this.enemy = enemies.get(id);
        this.board = board;
        this.target = target;
        this.state = FSMState.SPAWN;
        this.enemies = enemies;
        this.detection = Detection.LINE;

    }
    /**
     * Returns the distance between two board coordinates given two world coordinates
     *
     * @param x x-coord in screen coord of first ship
     * @param y y-coord in screen coord of first ship
     * @param tx x-coord in screen coord of second ship
     * @param tx  y-coord in screen coord of second ship
     * @return
     */
    private float worldToBoardDistance (float x, float y, float tx, float ty){
        int dx = board.worldToBoard(tx) - board.worldToBoard(x);
        int dy = board.worldToBoard(ty) - board.worldToBoard(y);
        return (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy,2));
    }

    /**
     * Returns true if we detected the player (when the player walks in line of sight)
     *
     * @return true if we can both fire and hit our target
     */
    private boolean detectedPlayer(){
        Enemy.Direction direction = enemy.getDirection();
        if (direction == null){
            return false;
        }
        boolean inLine;
        switch (direction){
            case NORTH:
                 inLine = (target.getX() == enemy.getX()) && (target.getY() > enemy.getY());
                break;
            case SOUTH:
                inLine = (target.getX() == enemy.getX()) && (target.getY() < enemy.getY());
                break;
            case EAST:
                inLine = (target.getX() > enemy.getX()) && (target.getY() == enemy.getY());
                break;
            case WEST:
                inLine = (target.getX() < enemy.getX()) && (target.getY() == enemy.getY());
                break;
            default:
                throw new NotImplementedException();
        }
        return (worldToBoardDistance(enemy.getX(), enemy.getY(), target.getX(), target.getY()) <= DETECT_DIST && inLine );
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
        if (!board.isWalkable(x,y) || target == null){
            return false;
        }
        int dx = board.worldToBoard(target.getX()) - x;
        int dy = board.worldToBoard(target.getY()) - y;

        if ((dx == 0 && dy <= ATTACK_DIST ) || (dy == 0 && dx <= ATTACK_DIST)){
            return true;
        }

        return false;
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
        if (canHitTargetFrom(board.worldToBoard(enemy.getX()), board.worldToBoard(enemy.getY()))){
            return true;
        }

        return false;
    }

    private boolean canChase(){
        return  (worldToBoardDistance(enemy.getX(), enemy.getY(), target.getX(), target.getY()) <= CHASE_DIST );

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
    public int getAction(){
        ticks++;
        if ((enemy.getId() + ticks) % 10 == 0) {
            // Process the FSM
            changeStateIfApplicable();

            // Pathfinding
            markGoalTiles();
            move = getMoveAlongPathToGoalTile();

        }
        int action = move;

        if (state == FSMState.ATTACK) {
            action |= CONTROL_ATTACK;
        }

        return action;
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
        switch (state){
            case SPAWN:
                state = FSMState.PATROL;
                break;
            case PATROL:
                if (detectedPlayer()){
                    state = FSMState.CHASE;
                }
                break;
            case CHASE:
                if (canHitTarget()){
                    state = FSMState.ATTACK;
                }
                if (!canChase()){
                    state = FSMState.WANDER;
                }
                break;
            case WANDER:
                if (Math.random() <= (double) (1/10)) {
                    state = FSMState.RETURN;
                }
            case ATTACK:
                if (!canHitTarget()){
                    state = FSMState.CHASE;
                }
        }
    }

    private void markGoalTiles() {
        board.clearMarks();
        boolean setGoal = false; // Until we find a goal
        switch (state){
            case SPAWN:
                break;
            case PATROL:
                Vector2 pos = enemy.getNextPatrol();
                board.setGoal((int)pos.x, (int)pos.y);
            case WANDER:
                int x = board.worldToBoard(enemy.getX());
                int y = board.worldToBoard(enemy.getY());
                //#endregion
                for (int i = -1; i < 2; i++ ){
                    for (int j = -1 ; j <2 ; j++){
                        if (board.isWalkable(x+i, y+j) && Math.random() <= (double)(1/9) && !setGoal && board.inBounds(x +i, y+j) ){
                            board.setGoal(board.worldToBoard(x+i), board.worldToBoard(y+j) );
                            setGoal = true;
                        }
                    }
                }
                break;
            case CHASE:
                if (target != null) {
                    board.setGoal(board.worldToBoard(target.getX()), board.worldToBoard(target.getY()));
                }
                break;
            case ATTACK:
                if (target != null) {
                    int target_x = board.worldToBoard(target.getX());
                    int target_y = board.worldToBoard(target.getY());
                    //#endregion
                    for (int i = -4; i < 5; i++) {
                        for (int j = -4; j < 5; j++) {
                            if (board.isWalkable(target_x + i, target_y + j) && canHitTargetFrom(target_x + i, target_y + i)
                                    && board.inBounds(target_x +i, target_y+j)) {
                                board.setGoal(board.worldToBoard(target_x + i), board.worldToBoard(target_y + j));
                                setGoal = true;
                            }
                        }
                    }

                }
        }
        if (!setGoal) {
            int sx = board.worldToBoard(enemy.getX());
            int sy = board.worldToBoard(enemy.getY());
            board.setGoal(sx, sy);
        }
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







}
