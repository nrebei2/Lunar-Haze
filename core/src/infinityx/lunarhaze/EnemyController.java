package infinityx.lunarhaze;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.lunarhaze.physics.RaycastInfo;

import java.util.Iterator;
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
    private static final float CHASE_DIST = 4;

    /**
     * Distance from which the enemy can attack the player
     */
    private static final int ATTACK_DIST = 1;

    private static final int ALERT_DIST = 20;

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
        ALERT,
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
        MOON,

        FULL_MOON,
    }

    /**
     * The enemy being controlled by this AIController
     */
    private final Enemy enemy;

    private Detection detection;

    /**
     * Set of active enemies on the current level
     */
    Array<Enemy> enemies;

    /**
     * The game board; used for pathfinding
     */
    private Board board;

    /**
     * The enemies current state in the FSM
     */
    private FSMState state;

    /**
     * The target (to chase or attack).
     */
    private Werewolf target;

    /**
     * The number of ticks since we started this controller
     */
    private long ticks;

    /**
     * The last time the enemy was in the CHASING state.
     */
    private long chased_ticks;

    /**
     * The last time the enemy was in the IDLE state.
     */
    private long idle_ticks;

    /**
     * The current goal (world) position
     */
    private Vector2 goal;

    /**
     * Where the player was last seen before WANDER
     */
    private Vector2 end_chase_pos;

    /**
     * Where the player was seen when an enemy alerts other enemies
     */
    public Vector2 alert_pos;

    private boolean isAtAlertLocation = false;

    /**
     * Reference of model-contoller map originially from EnemyPool
     */
    private ObjectMap<Enemy, EnemyController> controls;

    /**
     * Creates an EnemyController for the enemy with the given id.
     *
     * @param enemy   The enemy being controlled by this AIController
     */
    public EnemyController(Enemy enemy) {
        this.enemy = enemy;
        this.state = FSMState.SPAWN;
        this.detection = Detection.LIGHT;
        this.ticks = 0;
        this.chased_ticks = 0;
        this.idle_ticks = 0;
        this.end_chase_pos = new Vector2();
    }

    /**
     * (re)initialize attributes of this controller given the enemy.
     * This must be called when the enemy is reused from its pool.
     */
    public void initialize() {
        this.goal = getRandomPointinRegion();
    }

    /**
     * Populate
     * @param container holding surrounding model objects
     */
    public void populateSurroundings(LevelContainer container) {
        board = container.getBoard();
        target = container.getPlayer();
        enemies = container.getEnemies();
        controls = container.getEnemyControllers();
        //map = container.getMap();
    }

    public RaycastInfo raycast(GameObject requestingObject, Vector2 point1, Vector2 point2, World world) {
        RaycastInfo callback = new RaycastInfo(requestingObject);
        world.rayCast(callback, new Vector2(point1.x, point1.y), new Vector2(point2.x, point2.y));
        return callback;
    }

    public boolean lightDetect(LevelContainer container) {
        Werewolf player = container.getPlayer();

        Vector2 point1 = enemy.getPosition();
        ConeSource flashlight = enemy.getFlashlight();
        float light_dis = flashlight.getDistance();

        Vector2 temp = new Vector2(enemy.getBody().getTransform().getOrientation());
        Vector2 enemy_direction = temp.nor();


        Vector2 direction = new Vector2(player.getX() - point1.x, player.getY() - point1.y).nor();
        Vector2 point2 = new Vector2(point1.x + light_dis * direction.x, player.getY() + light_dis * direction.y);
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
            return enemy.getPosition().dst(target.getPosition()) <= DETECT_DIST;
        } else if (detection == Detection.MOON) {
            return enemy.getPosition().dst(target.getPosition()) <= DETECT_DIST_MOONLIGHT;
        } else if (detection == Detection.FULL_MOON) {
            return true;
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
        return Vector2.dst(x, y, pos_x, pos_y) <= ATTACK_DIST;
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
        return enemy.getPosition().dst(target.getPosition()) <= CHASE_DIST;
    }


    /**
     * Updates the enemy being controlled by this controller
     *
     * @param container
     * @param currentPhase of the game
     */
    public void update(LevelContainer container, GameplayController.Phase currentPhase) {
        ticks++;
        if (enemy.isDestroyed()) return;
        if (enemy.getHp() <= 0) container.removeEnemy(enemy);

        // Do not need to rework ourselves every frame. Just every 10 ticks.
        if (ticks % 10 == 0) {
            // Process the FSM
            changeStateIfApplicable(container, ticks);
            changeDetectionIfApplicable(currentPhase);

            // Pathfinding
            Vector2 next_move = findPath();
            enemy.update(next_move);
        }

        //other things?
        enemy.update(new Vector2());
    }

    private void alertAllies() {
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()){
            Enemy curr_enemy = enemyIterator.next();
            if (!curr_enemy.equals(enemy)){
                if (curr_enemy.getPosition().dst(enemy.getPosition()) <= ALERT_DIST){
                    curr_enemy.setAlerted(true);
                    controls.get(curr_enemy).alert_pos = (new Vector2(target.getX(), target.getY()));
                }
            }
        }

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
                    alertAllies();
                    state = FSMState.CHASE;
                }
                if (enemy.getAlerted()){
                    state = FSMState.ALERT;
                }

                break;
            case CHASE:
                if (!canChase() && !target.isOnMoonlight()) {
                    state = FSMState.WANDER;
                    chased_ticks = ticks;
                    end_chase_pos = new Vector2(enemy.getPosition().x, enemy.getPosition().y);
                    goal = getRandomPointtoWander(2f);
                }
                break;
            case WANDER:
                if (detectedPlayer(container)) {
                    state = FSMState.CHASE;
                } else if (!canChase() && !target.isOnMoonlight() && (ticks - chased_ticks >= 30)) {
                    state = FSMState.PATROL;
                }
                break;
            case IDLE:
                if (enemy.getAlerted()){
                    state = FSMState.ALERT;
                }
                idle_ticks++;
                if (idle_ticks >= 15) {
                    enemy.getBody().setAngularVelocity(0);
                    state = FSMState.PATROL;
                    idle_ticks = 0;
                    break;
                }
                if (lightDetect(container)) {
                    alertAllies();
                    enemy.getBody().setAngularVelocity(0);
                    state = FSMState.CHASE;
                    break;
                }
                if (idle_ticks % 5 == 0) {
                    Random rand = new Random();
                    if ((rand.nextInt(10) >= 5)) {
                        enemy.getBody().setAngularVelocity(1f);
                    } else {
                        enemy.getBody().setAngularVelocity(-1f);
                    }

                }
                break;
            case ALERT:
                if (lightDetect(container)){
                    state = FSMState.CHASE;
                }
                if (isAtAlertLocation){
                    state = FSMState.IDLE;
                }
            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                break;
        }
    }


    private void changeDetectionIfApplicable(GameplayController.Phase currentPhase) {
        if (target.isOnMoonlight()) {
            detection = Detection.MOON;
        } else if (state == FSMState.CHASE) {
            detection = Detection.AREA;
            enemy.getFlashlight().setConeDegree(0);
        }else if (currentPhase == GameplayController.Phase.BATTLE){
            enemy.getFlashlight().setConeDegree(0);
            detection = Detection.FULL_MOON;
            state = FSMState.CHASE;
        } else {
            detection = Detection.LIGHT;
            enemy.getFlashlight().setConeDegree(30);

        }
    }


    private Vector2 getRandomPointinRegion() {
        //Calculates the two point coordinates in a world base
        Vector2 bottom_left = board.boardToWorld((int) enemy.getBottomLeftOfRegion().x, (int) enemy.getBottomLeftOfRegion().y);
        Vector2 top_right = board.boardToWorld((int) enemy.getTopRightOfRegion().x, (int) enemy.getTopRightOfRegion().y);
        Vector2 random_point = new Vector2(MathUtils.random(bottom_left.x, top_right.x), MathUtils.random(bottom_left.y, top_right.y));
        //System.out.println("I found a point with x: "+ random_point.x+", y: "+random_point.y+"!");
        return random_point;
    }

    private Vector2 getRandomPointtoWander(float offset) {
        //Calculates the two point coordinates in a world base
        Vector2 bottom_left = new Vector2(this.end_chase_pos.x - offset, this.end_chase_pos.y - offset);
        Vector2 top_right = new Vector2(this.end_chase_pos.x + offset, this.end_chase_pos.y + offset);
        Vector2 random_point = new Vector2(MathUtils.random(bottom_left.x, top_right.x), MathUtils.random(bottom_left.y, top_right.y));
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
                if (temp_cur_pos.sub(goal).len() <= 0.2f) {
                    goal = getRandomPointinRegion();
                    state = FSMState.IDLE;
                    enemy.setVX(0);
                    enemy.setVY(0);
                    return new Vector2();
                    /**stay there for a while*/
                }
                return new Vector2(goal.x - cur_pos.x, goal.y - cur_pos.y).nor();
            case CHASE:
                Vector2 target_pos = new Vector2(target.getPosition());
                return (target_pos.sub(cur_pos)).nor();
            case WANDER:
                Vector2 temp_cur_pos2 = new Vector2(cur_pos.x, cur_pos.y);
                if (temp_cur_pos2.sub(goal).len() <= 0.3f) {
                    goal = getRandomPointtoWander(1f);
                }
                return new Vector2(goal.x - cur_pos.x, goal.y - cur_pos.y).nor();
            case IDLE:
                return new Vector2(0, 0);
            case ALERT:
                //alert_pos = target.getPosition();
                Vector2 temp_cur_pos3 = new Vector2(cur_pos.x, cur_pos.y);
                if (temp_cur_pos3.sub(alert_pos).len() <= 0.2f) {
                    enemy.setAlerted(false);
                    this.isAtAlertLocation = true;
                    return new Vector2(0,0);
                }
                return new Vector2(alert_pos.x - cur_pos.x, alert_pos.y - cur_pos.y).nor();
        }
        return new Vector2(0, 0);
    }
}