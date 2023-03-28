package infinityx.lunarhaze;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.ai.LookAround;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.Box2DRaycastCollision;
import infinityx.lunarhaze.physics.RaycastInfo;


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
     * Raycast cache for target detection
     */
    private final RaycastInfo raycast;

    /**
     * Collision detector for target detection
     */
    private Box2DRaycastCollision raycastCollision;

    /**
     * Cache for collision output from raycastCollision
     */
    Collision<Vector2> collisionCache = new Collision<>(new Vector2(), new Vector2());

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
         * The enemy is alerted (Exclamation point!)
         */
        ALERT,
        /**
         * The enemy has noticed sometime amiss (Question mark?)
         */
        NOTICED,
        /**
         * Neither heard nor seen the target
         */
        NONE
    }

    /**
     * The enemy being controlled by this AIController
     */
    private final Enemy enemy;

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
    private final long chased_ticks;

    /**
     * The last time the enemy was in the IDLE state.
     */
    private final long idle_ticks;

    /**
     * The current goal (world) position
     */
    private Vector2 goal;

    /**
     * Where the player was last seen before WANDER
     */
    private final Vector2 end_chase_pos;

    /**
     * Where the player was seen when an enemy alerts other enemies
     */
    public Vector2 alert_pos;

    /**
     * AI state machine for the given enemy.
     */
    private final StateMachine<EnemyController, EnemyState> stateMachine;

    // Steering behaviors
    public Arrive<Vector2> arriveSB;
    public RaycastObstacleAvoidance<Vector2> collisionSB;
    public PrioritySteering<Vector2> patrolSB;
    public LookAround lookAroundSB;

    /**
     * Creates an EnemyController for the enemy with the given id.
     *
     * @param enemy The enemy being controlled by this AIController
     */
    public EnemyController(Enemy enemy) {
        this.enemy = enemy;
        this.ticks = 0;
        this.chased_ticks = 0;
        this.idle_ticks = 0;
        this.end_chase_pos = new Vector2();
        this.stateMachine = new DefaultStateMachine<>(this, EnemyState.INIT, EnemyState.ANY_STATE);
        this.raycast = new RaycastInfo(enemy);
        raycast.addIgnores(GameObject.ObjectType.ENEMY);
    }

    /**
     * Populate attributes used for sensory information.
     *
     * @param container holding surrounding model objects
     */
    public void populate(LevelContainer container) {
        target = container.getPlayer();
        this.raycastCollision = new Box2DRaycastCollision(container.getWorld(), raycast);

        // Steering behaviors
        this.arriveSB = new Arrive<>(enemy, target);
        arriveSB.setArrivalTolerance(0.1f);
        arriveSB.setDecelerationRadius(0.9f);

        RaycastInfo collRay = new RaycastInfo(enemy);
        collRay.addIgnores(GameObject.ObjectType.WEREWOLF);
        RaycastCollisionDetector<Vector2> raycastCollisionDetector = new Box2DRaycastCollision(container.getWorld(), collRay);

        this.collisionSB = new RaycastObstacleAvoidance<>(
                enemy,
                new CentralRayWithWhiskersConfiguration<>(enemy, 5, 3, 35 * MathUtils.degreesToRadians),
                raycastCollisionDetector, 1
        );

        this.patrolSB =
                new PrioritySteering<>(enemy, 0.0001f)
                        .add(collisionSB)
                        .add(arriveSB);

        this.lookAroundSB = new LookAround(enemy, 160).
                setAlignTolerance(MathUtils.degreesToRadians * 10);
    }

    /**
     * Updates the enemy being controlled by this controller
     *
     * @param container
     * @param currentPhase of the game
     * @param delta
     */
    public void update(LevelContainer container, GameplayController.Phase currentPhase, float delta) {
        ticks++;
        if (enemy.getHp() <= 0) container.removeEnemy(enemy);

        // Process the FSM
        //changeStateIfApplicable(container, ticks);
        //changeDetectionIfApplicable(currentPhase);

        // Pathfinding
        //Vector2 next_move = findPath();

        stateMachine.update();
        enemy.update(delta);
    }

    public StateMachine<EnemyController, EnemyState> getStateMachine() {
        return stateMachine;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    /**
     * @return the current detection the enemy has on the target
     */
    private Detection getDetection(LevelContainer container) {
        /* Area of interests:
         * Focused view - same angle as flashlight, extends between [0, 5]
         * Short distance - angle of 100, extends between [0, 3.5]
         * Peripheral vision - angle of 180, extends between [0, 1.75]
         * Hearing radius - angle of 360, extends between [0, 3]
         * Lerp between player stealth for max distance,
         * but maybe add cutoffs for NONE?
         */

        Interpolation lerp = Interpolation.linear;
        raycastCollision.findCollision(collisionCache, new Ray<>(enemy.getPosition(), target.getPosition()));

        if (!raycast.hit) {
            // For any reason...
            return Detection.NONE;
        }

        Vector2 enemyToPlayer = target.getPosition().sub(enemy.getPosition());
        float dist = enemyToPlayer.len();

        // degree between enemy orientation and enemy-to-player
        double degree = Math.abs(enemy.getOrientation() - enemy.vectorToAngle(enemyToPlayer));

        if (raycast.hitObject == target) {
            if (degree <= enemy.getFlashlight().getConeDegree() / 2 && dist <= lerp.apply(0, 5, target.getStealth())) {
                return Detection.ALERT;
            }
            if (degree <= 50 && dist <= lerp.apply(0f, 3.5f, target.getStealth())) {
                return Detection.ALERT;
            }
            if (degree <= 90 && dist <= lerp.apply(0f, 1.75f, target.getStealth())) {
                return Detection.ALERT;
            }
        }

        // target may be behind object, but enemy should still be able to hear
        if (dist <= lerp.apply(0, 3f, target.getStealth())) {
            return Detection.NOTICED;
        }
        return Detection.NONE;
    }

    /**
     * @return true if the enemy attached to this controller can attack the enemy
     */
    private boolean canHitTarget() {
        return true;
    }

    private boolean canChase() {
        // This is radial
        return enemy.getPosition().dst(target.getPosition()) <= CHASE_DIST;
    }


    //private void alertAllies() {
    //    Iterator<Enemy> enemyIterator = enemies.iterator();
    //    while (enemyIterator.hasNext()){
    //        Enemy curr_enemy = enemyIterator.next();
    //        if (!curr_enemy.equals(enemy)){
    //            if (curr_enemy.getPosition().dst(enemy.getPosition()) <= ALERT_DIST){
    //                curr_enemy.setAlerted(true);
    //                controls.get(curr_enemy).alert_pos = (new Vector2(target.getX(), target.getY()));
    //            }
    //        }
    //    }
    //
    //}

    /**
     * Change the state of the enemy.
     * <p>
     * A Finite State Machine (FSM) is just a collection of rules that,
     * given a current state, and given certain observations about the
     * environment, chooses a new state. For example, if we are currently
     * in the ATTACK state, we may want to switch to the CHASE state if the
     * target gets out of range.
     */
    //private void changeStateIfApplicable(LevelContainer container, long ticks) {
    //    switch (state) {
    //        case SPAWN:
    //            state = FSMState.PATROL;
    //            break;
    //        case PATROL:
    //            if (detectedPlayer(container)) {
    //                alertAllies();
    //                state = FSMState.CHASE;
    //            }
    //            if (enemy.getAlerted()){
    //                state = FSMState.ALERT;
    //            }
    //
    //            break;
    //        case CHASE:
    //            if (!canChase() && !target.isOnMoonlight()) {
    //                state = FSMState.WANDER;
    //                chased_ticks = ticks;
    //                end_chase_pos = new Vector2(enemy.getPosition().x, enemy.getPosition().y);
    //                goal = getRandomPointtoWander(2f);
    //            }
    //            break;
    //        case WANDER:
    //            if (detectedPlayer(container)) {
    //                state = FSMState.CHASE;
    //            } else if (!canChase() && !target.isOnMoonlight() && (ticks - chased_ticks >= 30)) {
    //                state = FSMState.PATROL;
    //            }
    //            break;
    //        case IDLE:
    //            if (enemy.getAlerted()){
    //                state = FSMState.ALERT;
    //            }
    //            idle_ticks++;
    //            if (idle_ticks >= 15) {
    //                enemy.getBody().setAngularVelocity(0);
    //                state = FSMState.PATROL;
    //                idle_ticks = 0;
    //                break;
    //            }
    //            if (lightDetect(container)) {
    //                alertAllies();
    //                enemy.getBody().setAngularVelocity(0);
    //                state = FSMState.CHASE;
    //                break;
    //            }
    //            if (idle_ticks % 5 == 0) {
    //                Random rand = new Random();
    //                if ((rand.nextInt(10) >= 5)) {
    //                    enemy.getBody().setAngularVelocity(1f);
    //                } else {
    //                    enemy.getBody().setAngularVelocity(-1f);
    //                }
    //
    //            }
    //            break;
    //        case ALERT:
    //            if (lightDetect(container)){
    //                state = FSMState.CHASE;
    //            }
    //            if (isAtAlertLocation){
    //                state = FSMState.IDLE;
    //            }
    //        default:
    //            // Unknown or unhandled state, should never get here
    //            assert (false);
    //            break;
    //    }
    //}

    //
    //private void changeDetectionIfApplicable(GameplayController.Phase currentPhase) {
    //    if (target.isOnMoonlight()) {
    //        detection = Detection.MOON;
    //    } else if (state == FSMState.CHASE) {
    //        detection = Detection.AREA;
    //        enemy.getFlashlight().setConeDegree(0);
    //    }else if (currentPhase == GameplayController.Phase.BATTLE){
    //        enemy.getFlashlight().setConeDegree(0);
    //        detection = Detection.FULL_MOON;
    //        state = FSMState.CHASE;
    //    } else {
    //        detection = Detection.LIGHT;
    //        enemy.getFlashlight().setConeDegree(30);
    //
    //    }
    //}


    /**
     * @return Random point in patrol area
     */
    public Vector2 getPatrolTarget() {
        Vector2 random_point = new Vector2(
                MathUtils.random(enemy.getBottomLeftOfRegion().x, enemy.getTopRightOfRegion().x),
                MathUtils.random(enemy.getBottomLeftOfRegion().y, enemy.getTopRightOfRegion().y)
        );
        System.out.println("I found a point with x: " + random_point.x + ", y: " + random_point.y + "!");
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

    //private Vector2 findPath() {
    //    //TODO CHANGE--SHOULD GIVE US THE NEXT MOVEMENT
    //    //Gets enemy position
    //    Vector2 cur_pos = enemy.getPosition();
    //    switch (state) {
    //        case SPAWN:
    //            break;
    //        case PATROL:
    //            Vector2 temp_cur_pos = new Vector2(cur_pos.x, cur_pos.y);
    //            if (temp_cur_pos.sub(goal).len() <= 0.2f) {
    //                goal = getRandomPointinRegion();
    //                state = FSMState.IDLE;
    //                enemy.setVX(0);
    //                enemy.setVY(0);
    //                return new Vector2();
    //                /**stay there for a while*/
    //            }
    //            return new Vector2(goal.x - cur_pos.x, goal.y - cur_pos.y).nor();
    //        case CHASE:
    //            Vector2 target_pos = new Vector2(target.getPosition());
    //            return (target_pos.sub(cur_pos)).nor();
    //        case WANDER:
    //            Vector2 temp_cur_pos2 = new Vector2(cur_pos.x, cur_pos.y);
    //            if (temp_cur_pos2.sub(goal).len() <= 0.3f) {
    //                goal = getRandomPointtoWander(1f);
    //            }
    //            return new Vector2(goal.x - cur_pos.x, goal.y - cur_pos.y).nor();
    //        case IDLE:
    //            return new Vector2(0, 0);
    //        case ALERT:
    //            //alert_pos = target.getPosition();
    //            Vector2 temp_cur_pos3 = new Vector2(cur_pos.x, cur_pos.y);
    //            if (temp_cur_pos3.sub(alert_pos).len() <= 0.2f) {
    //                enemy.setAlerted(false);
    //                this.isAtAlertLocation = true;
    //                return new Vector2(0,0);
    //            }
    //            return new Vector2(alert_pos.x - cur_pos.x, alert_pos.y - cur_pos.y).nor();
    //    }
    //    return new Vector2(0, 0);
    //}
}