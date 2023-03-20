package infinityx.lunarhaze;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Queue;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyPool;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.lunarhaze.physics.RaycastInfo;
import java.util.Random;


// TODO: move all this stuff into AI controller, EnemyController should hold other enemy actions
public class EnemyController {
    /**
     * Distance for enemy to detect the player
     */
    private static final float DETECT_DIST = 2;

    /**
     * Distance for enemy to detect the player if the player in on moonlight
     */
    private static final float DETECT_DIST_MOONLIGHT = 5;

    /**
     * Distance from which the enemy chases the player
     */
    private static final float CHASE_DIST = 3f;

    /**
     * Distance from which the enemy can attack the player
     */
    private static final int ATTACK_DIST = 1;


    /**
     * Enumeration to encode the finite state machine.
     */
    public enum FSMState {
        /**
         * The enemy just spawned
         */
        SPAWN,
        /**
         * The enemy is patrolling around a set path
         */
        PATROL,
        /**
         * The enemy is wandering around where target is last seen
         */
        WANDER,
        /**
         * The enemy has a target, but must get closer
         */
        CHASE,
        /**
         * The enemy has a target and is attacking it
         */
        ATTACK,
        /**
         * The enemy lost its target and is returnng to its patrolling path
         */
        RETURN,
        /**
         * The enemy is not doing idle
         */
        IDLE,
    }

    private enum Detection {
        /**
         * The enemy can only detect the player in a straight line
         */
        LIGHT,
        /**
         * The enemy can detect the player in an area
         * The enemy can detect the player in an area
         */
        AREA,
        /**
         * The enemy can  detect the player in a half circle
         */
        MOON
    }

    /**
     * Enumeration to describe what direction the enemy is facing
     */

    // Instance Attributes
    /**
     * The enemy being controlled by this AIController
     */
    private final Enemy enemy;

    private Detection detection;

    /**
     * Set of active enemies on the current level
     */
    ObjectSet<Enemy> enemies;

    /**
     * The game board; used for pathfinding
     */
    private final Board board;
    /**
     * The ship's current state in the FSM
     */
    private FSMState state;
    /**
     * The target ship (to chase or attack).
     */
    private final Werewolf target;

    /**
     * The enemy next action (may include firing).
     */
    private int move; // A ControlCode

    /** The direction the enemy is facing*/

    /**
     * The number of ticks since we started this controller
     */
    private long ticks;

    /**
     * The number of ticks since we started this controller
     */
    private long chased_ticks;

    /**
     * The number of ticks since we started this controller
     */
    private long idle_ticks;

    /**
     * The number of ticks since we started this controller
     */
    private Vector2 goal;

    /**
     * The number of ticks since we started this controller
     */
    private Vector2 end_chase_pos;


    /**
     * Creates an EnemyController for the enemy with the given id.
     *
     * @param board   The game board (for pathfinding)
     * @param enemies The list of enemies (for alerting)
     * @param enemy The enemy being controlled by this AIController
     *
     */
    public EnemyController(Werewolf target, ObjectSet<Enemy> enemies, Board board, Enemy enemy) {
        this.enemy = enemy;
        this.board = board;
        this.target = target;
        this.state = FSMState.SPAWN;
        this.enemies = enemies;
        this.detection = Detection.LIGHT;
        this.ticks = 0;
        this.chased_ticks = 0;
        this.idle_ticks = 0;
        this.goal = getRandomPointinRegion();
        this.end_chase_pos = new Vector2();
    }

    /**
     * Returns the distance between two board coordinates given two world coordinates
     *
     * @param x  x-coord in screen coord of first ship
     * @param y  y-coord in screen coord of first ship
     * @param tx x-coord in screen coord of second ship
     * @param tx y-coord in screen coord of second ship
     * @return
     */
    private float boardDistance(float x, float y, float tx, float ty) {
        int dx = board.worldToBoardX(tx) - board.worldToBoardX(x);
        int dy = board.worldToBoardX(ty) - board.worldToBoardX(y);
        return (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }

    public RaycastInfo raycast(GameObject requestingObject, Vector2 point1, Vector2 point2, World world) {
        RaycastInfo callback = new RaycastInfo(requestingObject);
        world.rayCast(callback, new Vector2(point1.x, point1.y), new Vector2(point2.x, point2.y));
        return callback;
    }

    public boolean lightDetect(LevelContainer container){
        Werewolf player = container.getPlayer();

        Vector2 point1 = enemy.getPosition();
        ConeSource flashlight = enemy.getFlashlight();
        float light_dis = flashlight.getDistance();


        Vector2 temp = new Vector2(enemy.getBody().getTransform().getOrientation());
        Vector2 enemy_direction = temp.nor();


        Vector2 direction = new Vector2(player.getX()-point1.x, player.getY()-point1.y).nor();
        Vector2 point2 = new Vector2(point1.x+light_dis*direction.x, player.getY()+light_dis*direction.y);
        RaycastInfo info = raycast(enemy, point1, point2, container.getWorld());
        double degree = Math.toDegrees(Math.acos(enemy_direction.dot(direction)));

        return info.hit && info.hitObject == player && degree <= flashlight.getConeDegree();
    }


    /**
     * Returns true if we detected the player (when the player walks in line of sight)
     *
     * @return true if we can both fire and hit our target
     */
    private boolean detectedPlayer(LevelContainer container) {
        /**Enemy must be in a line and less than detection distance away*/
        if (detection == Detection.LIGHT) {
            return lightDetect(container);
        }
        /**Enemy only need to be less than detection distance away, enemy can see in an area*/
        else if (detection == Detection.AREA) {
            return boardDistance(enemy.getX(), enemy.getY(), target.getX(),
                    target.getY()) <= DETECT_DIST;
        } else if (detection == Detection.MOON) {
            return boardDistance(enemy.getX(), enemy.getY(), target.getX(),
                    target.getY()) <= DETECT_DIST_MOONLIGHT;
        } else {
            return false;
        }
    }

    /**
     * Returns true if we can hit a target from here.
     *
     * @param x The x-index of the source tile
     * @param y The y-index of the source tile
     * @return true if we can hit a target from here.
     */
    private boolean canHitTargetFrom(int x, int y) {
        if (!board.isWalkable(x, y) || target == null) {
            return false;
        }
        int pos_x = board.worldToBoardX(target.getX());
        int pos_y = board.worldToBoardX(target.getY());
        return boardDistance(x, y, pos_x, pos_y) <= ATTACK_DIST;
    }

    /**
     * Returns true if we can both fire and hit our target
     * <p>
     * If we can fire now, and we could hit the target from where we are,
     * we should hit the target now.
     *
     * @return true if we can both fire and hit our target
     */
    private boolean canHitTarget() {
        return canHitTargetFrom(board.worldToBoardX(enemy.getX()), board.worldToBoardX(enemy.getY()));
    }

    private boolean canChase() {
        // This is radial
        return (boardDistance(enemy.getX(), enemy.getY(), target.getX(), target.getY()) <= CHASE_DIST);
    }


    /**
     * Returns the action selected by this InputController
     * <p>
     * The returned int is a bit-vector of more than one possible input
     * option. This is why we do not use an enumeration of Control Codes;
     * Java does not (nicely) provide bitwise operation support for enums.
     * <p>
     * This function tests the environment and uses the FSM to chose the next
     * action of the ship. This function SHOULD NOT need to be modified.  It
     * just contains code that drives the functions that you need to implement.
     *
     * @return the action selected by this InputController
     */
    public Vector2 getMovement(LevelContainer container) {
        //TODO CHANGE
        // Increment the number of ticks.
        ticks++;

        // Do not need to rework ourselves every frame. Just every 10 ticks.
        if ((enemy.getId() + ticks) % 10 == 0) {
            // Process the FSM
//            if (state==FSMState.WANDER) System.out.println(state);
            changeStateIfApplicable(container, ticks);
            changeDetectionIfApplicable();

            // Pathfinding
            /**Code that gives you the next Vector2*/
            Vector2 next_move = findPath();
            return next_move;

        }

        //other things?
        return new Vector2();
    }

    private void alertAllies(){

    }

    /**
     * Change the state of the enemy.
     * <p>
     * A Finite State Machine (FSM) is just a collection of rules that,
     * given a current state, and given certain observations about the
     * environment, chooses a new state. For example, if we are currently
     * in the ATTACK state, we may want to switch to the CHASE state if the
     * target gets out of range.
     */
    private void changeStateIfApplicable(LevelContainer container, long ticks) {
        switch (state) {
            case SPAWN:
                state = FSMState.PATROL;
                break;
            case PATROL:
                if (detectedPlayer(container)) {
                    state = FSMState.CHASE;
                    enemy.setIsAlerted(true);
                    alertAllies();
                }


                break;
            case CHASE:
                if (!canChase() && !target.isOnMoonlight()) {
                    state = FSMState.WANDER;
                    enemy.setIsAlerted(false);
                    chased_ticks = ticks;
                    end_chase_pos = new Vector2(enemy.getPosition().x, enemy.getPosition().y);
                    goal = getRandomPointtoWander(2f);
                }
//                Vector2 temp = new Vector2(enemy.getBody().getTransform().getOrientation());
//                Vector2 direction = new Vector2(target.getX()-enemy.getX(),target.getY()-enemy.getY()).nor();
//                Vector2 enemy_direction = temp.nor();
//                if (direction.x < enemy_direction.x){
//                    enemy.getBody().setAngularVelocity(-1f);
//                }
//                else if (direction.x > enemy_direction.x){
//                    enemy.getBody().setAngularVelocity(1f);
//                }
//                else{
//                    enemy.getBody().setAngularVelocity(0);
//
//                }
                break;
            case WANDER:
                if (detectedPlayer(container)){
                    state = FSMState.CHASE;
                    enemy.setIsAlerted(true);
                }
                else if (!canChase() && !target.isOnMoonlight() && (ticks - chased_ticks >= 30)) {
                    state = FSMState.PATROL;
                    enemy.setIsAlerted(false);
                }
                break;
            case IDLE:
                idle_ticks++;
                if (idle_ticks >= 15){
                    enemy.getBody().setAngularVelocity(0);
                    state = FSMState.PATROL;
                    idle_ticks=0;
                    break;
                }
                if (lightDetect(container)){
                    state = FSMState.CHASE;
                    enemy.getBody().setAngularVelocity(0);
                    break;
                }
                if (idle_ticks % 5 == 0){
                    Random rand = new Random();
                    if ((rand.nextInt(10) >= 5)) {
                        enemy.getBody().setAngularVelocity(1f);
                    } else {
                        enemy.getBody().setAngularVelocity(-1f);
                    }

                }
                break;
            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                state = FSMState.WANDER;
                break;
        }
    }


    private void changeDetectionIfApplicable() {
        if (target.isOnMoonlight()) {
            detection = Detection.MOON;
        } else if (state == FSMState.CHASE) {
            detection = Detection.AREA;
        } else {
            detection = Detection.LIGHT;


        }
    }


    private Vector2 getRandomPointinRegion(){
        Random rand = new Random();
        //Calculates the two point coordinates in a world base
        Vector2 bottom_left =  board.boardToWorld((int)enemy.getBottomLeftOfRegion().x, (int)enemy.getBottomLeftOfRegion().y);
        Vector2 top_right =  board.boardToWorld((int)enemy.getTopRightOfRegion().x, (int)enemy.getTopRightOfRegion().y);
        Vector2 random_point = new Vector2 (rand.nextFloat(bottom_left.x,top_right.x), rand.nextFloat(bottom_left.y,top_right.y));
        //System.out.println("I found a point with x: "+ random_point.x+", y: "+random_point.y+"!");
        return random_point;
    }

    private Vector2 getRandomPointtoWander(float offset){
        Random rand = new Random();
        //Calculates the two point coordinates in a world base
        Vector2 bottom_left = new Vector2 (this.end_chase_pos.x - offset, this.end_chase_pos.y - offset);
        Vector2 top_right = new Vector2 (this.end_chase_pos.x + offset, this.end_chase_pos.y + offset);
        Vector2 random_point = new Vector2 (rand.nextFloat(bottom_left.x,top_right.x), rand.nextFloat(bottom_left.y,top_right.y));
        //System.out.println("I found a point with x: "+ random_point.x+", y: "+random_point.y+"!");
        return random_point;
    }

    private Vector2 findPath() {
        //TODO CHANGE--SHOULD GIVE US THE NEXT MOVEMENT
        //Gets enemy position
        Vector2 cur_pos = enemy.getPosition();
        switch (state) {
            case SPAWN:
                break;
            case PATROL:
                Vector2 temp_cur_pos = new Vector2(cur_pos.x, cur_pos.y);
                if (temp_cur_pos.sub(goal).len() <= 0.2f){
                    goal = getRandomPointinRegion();
                    state = FSMState.IDLE;
                    enemy.setVX(0);
                    enemy.setVY(0);
                    return new Vector2();
                    /**stay there for a while*/
                }
                return new Vector2(goal.x - cur_pos.x, goal.y-cur_pos.y).nor();
            case CHASE:
                Vector2 target_pos = new Vector2(target.getPosition());

                return (target_pos.sub(cur_pos)).nor();
            case WANDER:
                Vector2 temp_cur_pos2 = new Vector2(cur_pos.x, cur_pos.y);
                if (temp_cur_pos2.sub(goal).len() <= 0.3f){
                    goal = getRandomPointtoWander(1f);
                }
                return new Vector2(goal.x - cur_pos.x, goal.y-cur_pos.y).nor();
            case IDLE:
                return new Vector2(0,0);
        }
        return new Vector2(0,0);
    }

    // Add any auxiliary methods or data structures here
    //#region PUT YOUR CODE HERE

    private class TileData {
        private final int x;
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

    public final int[][] directions = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

}