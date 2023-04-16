package infinityx.lunarhaze;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.steer.behaviors.Face;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.utils.Path;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.Box2DRaycastCollision;
import infinityx.lunarhaze.physics.RaycastInfo;
import infinityx.util.astar.AStarPathFinding;

/**
 * Controller class, handles logic for a single enemy
 */
public class EnemyController {
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
     * Pathfinder reference from level container
     */
    public AStarPathFinding pathfinder;

    /**
     * Whether the game is in BATTLE phase
     */
    private boolean inBattle;

    /**
     * The enemy being controlled by this AIController
     */
    private final Enemy enemy;

    /**
     * The target (to chase or attack).
     */
    public Werewolf target;

    /**
     * Used for look-around, time in seconds
     */
    public float time;

    /**
     * AI state machine for the given enemy.
     */
    private final StateMachine<EnemyController, EnemyState> stateMachine;

    /**
     * Face direction behavior
     */
    public Face<Vector2> faceSB;

    /**
     * Pathfinding behavior
     */
    public FollowPath followPathSB;

    /**
     * Current target position for pathfinding
     */
    public Vector2 targetPos;

    /**
     * Patrol target cache
     */
    public Vector2 patrolTarget;

    /**
     * Creates an EnemyController for the enemy with the given id.
     *
     * @param enemy The enemy being controlled by this AIController
     */
    public EnemyController(Enemy enemy) {
        patrolTarget = new Vector2();
        this.enemy = enemy;
        this.inBattle = false;
        this.stateMachine = new DefaultStateMachine<>(this, EnemyState.INIT, EnemyState.ANY_STATE);
        this.raycast = new RaycastInfo(enemy);
        raycast.addIgnores(GameObject.ObjectType.ENEMY, GameObject.ObjectType.HITBOX);

        // Dummy path
        Array<Vector2> waypoints = new Array<>();
        waypoints.add(new Vector2());
        waypoints.add(new Vector2());
        followPathSB = new FollowPath(enemy, new LinePath(waypoints), 0.05f, 0.5f);
    }

    /**
     * Populate attributes used for sensory information.
     *
     * @param container holding surrounding model objects
     */
    public void populate(LevelContainer container) {
        target = container.getPlayer();
        this.raycastCollision = new Box2DRaycastCollision(container.getWorld(), raycast);
        this.pathfinder = container.pathfinder;

        // Steering behaviors
        this.faceSB = new Face<>(enemy).setAlignTolerance(MathUtils.degreesToRadians * 10);
    }

    /**
     * Updates the enemy being controlled by this controller
     *
     * @param delta time between last frame in seconds
     */
    public void update(LevelContainer container, float delta) {
        if (enemy.getHp() <= 0) container.removeEnemy(enemy);

        //if (inBattle && !stateMachine.isInState(EnemyState.ALERT)) {
        //    stateMachine.changeState(EnemyState.ALERT);
        //}

        // Process the FSM
        stateMachine.update();
        enemy.update(delta);
    }

    public StateMachine<EnemyController, EnemyState> getStateMachine() {
        return stateMachine;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public Werewolf getTarget() {
        return target;
    }

    /**
     * @return the current detection the enemy has on the target
     */
    public Enemy.Detection getDetection() {
        /* Area of interests:
         * Focused view - same angle as flashlight, extends between [2.75, 4.5]
         * Short distance - angle of 100, extends between [1.75, 3]
         * Peripheral vision - angle of 180, extends between [1.25, 2.25]
         * Hearing radius - angle of 360, extends between [1.5, 3.5]
         * Lerp between player stealth for max distance,
         * but maybe add cutoffs for NONE?
         */

        // TODO: right now there is no difference in logic between a return of ALERT and NOTICED
        if (inBattle) return Enemy.Detection.ALERT;

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
            if (degree <= enemy.getFlashlight().getConeDegree() / 2 && dist <= lerp.apply(2.75f, 4.5f, target.getStealth())) {
                return Enemy.Detection.ALERT;
            }
            if (degree <= 50 && dist <= lerp.apply(1.75f, 3.0f, target.getStealth())) {
                return Enemy.Detection.ALERT;
            }
            if (degree <= 90 && dist <= lerp.apply(1.25f, 2.25f, target.getStealth())) {
                return Enemy.Detection.ALERT;
            }
        }

        // target may be behind object, but enemy should still be able to hear
        if (dist <= lerp.apply(1.75f, 3.5f, target.getStealth())) {
            return Enemy.Detection.NOTICED;
        }

        // Target is too far away
        return Enemy.Detection.NONE;
    }

    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
    }

    /**
     * @return Random point in patrol area
     */
    public Vector2 getPatrolTarget() {
        return patrolTarget.set(
                MathUtils.random(enemy.getBottomLeftOfRegion().x, enemy.getTopRightOfRegion().x),
                MathUtils.random(enemy.getBottomLeftOfRegion().y, enemy.getTopRightOfRegion().y)
        );
    }

    /**
     * Updates path for pathfinding. Source is the enemy position and target is {@link #targetPos}
     */
    public void updatePath() {
        Path path = pathfinder.findPath(enemy.getPosition(), targetPos);
        followPathSB.setPath(path);
    }
}