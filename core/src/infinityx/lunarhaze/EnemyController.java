package infinityx.lunarhaze;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.steer.behaviors.*;
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
import infinityx.util.astar.AStarPathFinding;
import infinityx.util.astar.Node;

/** Controller class, handles logic for a single enemy */
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


    public AStarPathFinding pathfinder;

    /** Whether the game is in BATTLE phase */
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

    /** Face direction behavior */
    public Face<Vector2> faceSB;

    /** Pathfinding behavior */
    public FollowPath followPathSB;

    /**
     * Creates an EnemyController for the enemy with the given id.
     *
     * @param enemy The enemy being controlled by this AIController
     */
    public EnemyController(Enemy enemy) {
        this.enemy = enemy;
        this.inBattle = false;

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
         * Focused view - same angle as flashlight, extends between [1, 4]
         * Short distance - angle of 100, extends between [0.6, 2.5]
         * Peripheral vision - angle of 180, extends between [0.4, 1.75]
         * Hearing radius - angle of 360, extends between [1.5, 3.5]
         * Lerp between player stealth for max distance,
         * but maybe add cutoffs for NONE?
         */

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
        if (dist <= lerp.apply(1.5f, 3.5f, target.getStealth())) {
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
        Vector2 random_point = new Vector2(
                MathUtils.random(enemy.getBottomLeftOfRegion().x, enemy.getTopRightOfRegion().x),
                MathUtils.random(enemy.getBottomLeftOfRegion().y, enemy.getTopRightOfRegion().y)
        );
        return random_point;
    }
}