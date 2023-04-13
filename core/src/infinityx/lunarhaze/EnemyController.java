package infinityx.lunarhaze;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.steer.behaviors.*;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.ai.LookAround;
import infinityx.lunarhaze.ai.TacticalManager;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.Box2DRaycastCollision;
import infinityx.lunarhaze.physics.RaycastInfo;
import infinityx.util.astar.AStarPathFinding;
import infinityx.util.astar.Node;


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

    public Node nextNode;

    public AStarPathFinding pathfinder;
    private GameplayController.Phase curPhase;

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

    /**
     * The enemy being controlled by this AIController
     */
    private final Enemy enemy;

    /**
     * The target (to chase or attack).
     */
    public Werewolf target;

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
    public Face<Vector2> faceSB;

    public FollowPath followPathSB;

    public RaycastCollisionDetector raycastCollisionDetector;

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
        raycast.addIgnores(GameObject.ObjectType.ENEMY, GameObject.ObjectType.HITBOX);
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
        this.arriveSB = new Arrive<>(enemy, null);
        arriveSB.setArrivalTolerance(0.1f);
        arriveSB.setDecelerationRadius(0.4f);

        RaycastInfo collRay = new RaycastInfo(enemy);
        collRay.addIgnores(GameObject.ObjectType.WEREWOLF, GameObject.ObjectType.HITBOX);
         this.raycastCollisionDetector = new Box2DRaycastCollision(container.getWorld(), collRay);

        this.collisionSB = new RaycastObstacleAvoidance<>(
                enemy,
                new CentralRayWithWhiskersConfiguration<>(enemy, 5, 3, 35 * MathUtils.degreesToRadians),
                raycastCollisionDetector, 1
        );

        this.patrolSB =
                new PrioritySteering<>(enemy, 0.0001f)
                        .add(collisionSB)
                        .add(arriveSB);


        this.lookAroundSB = new LookAround(enemy, 160)
                .setAlignTolerance(MathUtils.degreesToRadians * 10)
                .setTimeToTarget(0.1f);

        this.faceSB = new Face<>(enemy).setAlignTolerance(MathUtils.degreesToRadians * 10);

        this.pathfinder = container.pathfinder;
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

        if (currentPhase == GameplayController.Phase.BATTLE && !stateMachine.isInState(EnemyState.ALERT)) {
            stateMachine.changeState(EnemyState.ALERT);
        }

        this.curPhase = currentPhase;

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

    public Werewolf getTarget(){
        return target;
    }

    /**
     * @return the current detection the enemy has on the target
     */
    public Enemy.Detection getDetection() {
        /* Area of interests:
         * Focused view - same angle as flashlight, extends between [1, 4]
         * Short distance - angle of 100, extends between [0.6, 2.5]
         * Peripheral vision - angle of 180, extends between [0.4, 1.75]
         * Hearing radius - angle of 360, extends between [1.5, 4.4]
         * Lerp between player stealth for max distance,
         * but maybe add cutoffs for NONE?
         */

        if (curPhase == GameplayController.Phase.BATTLE) return Enemy.Detection.ALERT;

        Interpolation lerp = Interpolation.linear;
        raycastCollision.findCollision(collisionCache, new Ray<>(enemy.getPosition(), target.getPosition()));

        if (!raycast.hit) {
            // For any reason...
            return Enemy.Detection.NONE;
        }

        Vector2 enemyToPlayer = target.getPosition().sub(enemy.getPosition());
        float dist = enemyToPlayer.len();

        // degree between enemy orientation and enemy-to-player
        double degree = Math.abs(enemy.getOrientation() - enemy.vectorToAngle(enemyToPlayer)) * MathUtils.radiansToDegrees;
        if (raycast.hitObject == target) {
            //System.out.printf("degree: %f, dist: %f, stealth: %f\n", degree, dist, target.getStealth());
            if (degree <= enemy.getFlashlight().getConeDegree() / 2 && dist <= lerp.apply(1, 4, target.getStealth())) {
                return Enemy.Detection.ALERT;
            }
            if (degree <= 50 && dist <= lerp.apply(0.6f, 2.5f, target.getStealth())) {
                return Enemy.Detection.ALERT;
            }
            if (degree <= 90 && dist <= lerp.apply(0.4f, 1.75f, target.getStealth())) {
                return Enemy.Detection.ALERT;
            }
        }


        // target may be behind object, but enemy should still be able to hear
        {
            if (dist <= lerp.apply(1.5f, 3.5f, target.getStealth())) {
                return Enemy.Detection.NOTICED;
            }
        }
        return Enemy.Detection.NONE;
    }

    /**
     * @return Random point in patrol area
     */
    public Vector2 getPatrolTarget() {
        Vector2 random_point = new Vector2(
                MathUtils.random(enemy.getBottomLeftOfRegion().x, enemy.getTopRightOfRegion().x),
                MathUtils.random(enemy.getBottomLeftOfRegion().y, enemy.getTopRightOfRegion().y)
        );
        //System.out.println("I found a point with x: " + random_point.x + ", y: " + random_point.y + "!");
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
}