package infinityx.lunarhaze;

import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.sound.sampled.Line;

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
    private static final float DETECT_DIST_MOONLIGHT = 5;
    private static final float CHASE_DIST = 3;
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
        /** The enemy can  detect the player in a half circle*/
        MOON


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
        Vector2 diff = board.worldToBoard(x,y).sub(board.worldToBoard(tx, ty));
        return (float) Math.sqrt(Math.pow(diff.x, 2) + Math.pow(diff.y,2));
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
        boolean blind;
        if (detection == Detection.LINE) {
            switch (direction) {
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
            return worldToBoardDistance(enemy.getX(), enemy.getY(), target.getX(),
                    target.getY()) <= DETECT_DIST && inLine;
        }
        else if (detection == Detection.MOON){
            switch (direction) {
                case NORTH:
                    blind = target.getY() < enemy.getY();
                    break;
                case SOUTH:
                    blind = target.getY() > enemy.getY();
                    break;
                case EAST:
                    blind = target.getX() < enemy.getX();
                    break;
                case WEST:
                    blind = target.getX() > enemy.getX();
                    break;
                default:
                    throw new NotImplementedException();
            }
            return worldToBoardDistance(enemy.getX(), enemy.getY(), target.getX(),
                    target.getY()) <= DETECT_DIST_MOONLIGHT && !blind;
        } else {
            return false;
        }
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
        Vector2 pos = board.worldToBoard(target.getX(), target.getY());
        int dx = (int)pos.x - x;
        int dy = (int)pos.y - y;

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

        Vector2 pos = board.worldToBoard(enemy.getX(), enemy.getY());
        if (canHitTargetFrom((int)pos.x , (int)pos.y)){
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
    public int getAction() {
        // Increment the number of ticks.
        ticks++;

        // Do not need to rework ourselves every frame. Just every 10 ticks.
        if ((enemy.getId() + ticks) % 10 == 0) {
            // Process the FSM
            changeStateIfApplicable();
            changeDetectionIfApplicable();

            // Pathfinding
            markGoalTiles();
            move = getMoveAlongPathToGoalTile();
        }

        int action = move;
        System.out.println(move);

        // If we're attacking someone and we can shoot him now, then do so.
        if (state == FSMState.ATTACK && canHitTarget()) {
            action |= CONTROL_ATTACK;
        }

        return action;
    }

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
                    state = FSMState.PATROL;
                }
                break;
//            case WANDER:
//                break;
            case ATTACK:
                if (!canHitTarget()){
                    state = FSMState.CHASE;
                }
                break;
            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                state = FSMState.WANDER; // If debugging is off
                break;
        }
    }

    private void changeDetectionIfApplicable() {
        switch (detection){
            case LINE:
                if (target.isOnMoonlight()){
                    detection = Detection.MOON;
                }
                break;
            case MOON:
                if (!target.isOnMoonlight()){
                    detection = Detection.LINE;
                }
                break;
        }
    }

    private void markGoalTiles() {
        System.out.println("EnemyController markGoalTile in state " + state);
        board.clearMarks();
        Vector2 pos;
        boolean setGoal = false; // Until we find a goal
        switch (state){
            case SPAWN:
                break;
            case PATROL:
                pos = enemy.getNextPatrol();
                board.setGoal((int)pos.x, (int)pos.y);
                setGoal= true;
                break;
//            case WANDER:
//
//                pos = board.worldToBoard(enemy.getX(), enemy.getY());
//                int x = (int)pos.x;
//                int y = (int)pos.y;
//                //#endregion
//                for (int i = -1; i < 2; i++ ){
//                    for (int j = -1 ; j <2 ; j++){
//                        if (board.isWalkable(x+i, y+j) && Math.random() <= (double)(1/9) && !setGoal && board.inBounds(x +i, y+j) ){
//                            board.setGoal(x+i, y+j);
//                            setGoal = true;
//                        }
//                    }
//                }
//                break;
            case CHASE:
                if (target != null) {
                    pos = board.worldToBoard(target.getX(), target.getY());

                    board.setGoal((int)pos.x, (int)pos.y);
                    setGoal= true;
                }
                break;
            case ATTACK:
                if (target != null) {
                    pos = board.worldToBoard(target.getX(), target.getY());
                    int target_x = (int)pos.x;
                    int target_y = (int)pos.y;
                    //#endregion
                    for (int i = -4; i < 5; i++) {
                        for (int j = -4; j < 5; j++) {
                            if (board.isWalkable(target_x + i, target_y + j) && canHitTargetFrom(target_x + i, target_y + i)
                                    && board.inBounds(target_x +i, target_y+j)) {
                                board.setGoal(target_x + i, target_y + j);
                                setGoal = true;
                            }
                        }
                    }

                }
        }
        if (!setGoal) {
            Vector2 position = board.worldToBoard(enemy.getX(), enemy.getY());
            board.setGoal((int)position.x, (int)position.y);
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
        //#region PUT YOUR CODE HERE
        Vector2 pos = board.worldToBoard(enemy.getX(),enemy.getY());
        int x = (int)pos.x;
        int y = (int)pos.y;
        if (board.isGoal(x,y)) {
            return CONTROL_NO_ACTION;
        }

        Queue<TileData> queue = new Queue<>();


        board.setVisited(x, y);
        queue.addLast(new TileData(x,y));
        while (!queue.isEmpty()){
            TileData head = queue.first();
            queue.removeFirst();
            if (board.isGoal(head.x, head.y)){
                return getDirection(head);
            }
            for (int[] direction : directions){
                if(!board.isVisited(head.x + direction[0], head.y + direction[1]) && board.isWalkable(head.x + direction[0], head.y + direction[1]))
                {
                    TileData next = new TileData(head.x + direction[0], head.y + direction[1]);
                    board.setVisited(next.getX(), next.getY());
                    next.setPrevDirection(new int[]{direction[0], direction[1]});
                    next.setPrev(head);
                    queue.addLast(next);
                }
            }
        }
        System.out.println("We should have action!");
        return CONTROL_NO_ACTION;
        //#endregion
    }

    private int getDirection(TileData t){
        while (t.prev.prev != null){
            t = t.prev;
        }
        if (t.prevDirection[0] == -1){
            return CONTROL_MOVE_LEFT;
        }
        else if (t.prevDirection[0] == 1){
            return CONTROL_MOVE_RIGHT;
        }
        else if (t.prevDirection[1] == -1){
            return CONTROL_MOVE_UP;
        }
        else if (t.prevDirection[1] == 1) {
            return CONTROL_MOVE_DOWN;
        }
        return CONTROL_NO_ACTION;
    }

    // Add any auxiliary methods or data structures here
    //#region PUT YOUR CODE HERE

    private class TileData{
        private final int x ;
        private final int y;

        public int[] prevDirection = null;

        public TileData prev = null;

        private TileData(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setPrevDirection(int[] prev_direction) {
            this.prevDirection = prev_direction;
        }

        public void setPrev(TileData prev) {
            this.prev = prev;
        }
    }
    public final int[][] directions = new int[][]{{1,0}, {-1,0}, {0,1}, {0,-1}};

}
