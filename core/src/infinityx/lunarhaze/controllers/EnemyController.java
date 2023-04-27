package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.steer.behaviors.Face;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.utils.Path;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.ai.*;
import infinityx.lunarhaze.combat.AttackHandler;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Enemy;
import infinityx.lunarhaze.models.entity.Werewolf;
import infinityx.lunarhaze.physics.Box2DRaycastCollision;
import infinityx.lunarhaze.physics.RaycastInfo;
import infinityx.util.astar.AStarPathFinding;

/**
 * Controller class, handles logic for a single enemy
 */
public class EnemyController extends AttackHandler {
    /**
     * Raycast cache for target detection
     */
    private final RaycastInfo raycast;
    public Vector2 flank_pos;

    /**
     * Collision detector for target detection
     */
    private Box2DRaycastCollision raycastCollision;

    /**
     * raycast for enemy communication
     */
    private final RaycastInfo commRay;

    /**
     * Collision detector for enemy communication
     */
    public Box2DRaycastCollision communicationCollision;

    /**
     * Cache for collision output from communicationCollision
     */
    Collision<Vector2> commCache = new Collision<>(new Vector2(), new Vector2());

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
     * Current target position for pathfinding. You should almost always use {@link Vector2#set(Vector2)} to update this.
     */
    public Vector2 targetPos;

    /**
     * Patrol target cache
     */
    public Vector2 patrolTarget;

    private Sound alert_sound;

    /**
     * Holds context behaviors for strafing,
     */
    public CombinedContext combinedContext;

    /**
     * Steering behavior from {@link #combinedContext}
     */
    public ContextSteering battleSB;

    /**
     * Steering behavior for strafing around target
     */
    public Strafe strafe;

    /**
     * Steering behavior for avoiding colliding into other enemies
     */
    public ContextBehavior separation;

    /**
     * Steering behavior for attacking
     */
    public ContextBehavior attack;

    public Sound getAlertSound() {
        return alert_sound;
    }

    public void setAlertSound(Sound s) {
        alert_sound = s;
    }

    /**
     * Creates an EnemyController for the enemy with the given id.
     *
     * @param enemy The enemy being controlled by this AIController
     */
    public EnemyController(final Enemy enemy) {
        super(enemy);
        patrolTarget = new Vector2();
        this.targetPos = new Vector2();
        this.enemy = enemy;
        this.inBattle = false;
        this.stateMachine = new DefaultStateMachine<>(this, EnemyState.INIT, EnemyState.ANY_STATE);
        this.combinedContext = new CombinedContext(enemy);

        this.raycast = new RaycastInfo(enemy);
        raycast.addIgnores(GameObject.ObjectType.ENEMY, GameObject.ObjectType.HITBOX);

        this.commRay = new RaycastInfo(enemy);
        commRay.addIgnores(GameObject.ObjectType.WEREWOLF);

    }

    /**
     * Populate attributes used for sensory information.
     *
     * @param container holding surrounding model objects
     */
    public void populate(final LevelContainer container) {
        target = container.getPlayer();
        this.raycastCollision = new Box2DRaycastCollision(container.getWorld(), raycast);
        this.communicationCollision = new Box2DRaycastCollision(container.getWorld(), commRay);
        this.pathfinder = container.pathfinder;

        // Steering behaviors
        this.faceSB = new Face<>(enemy)
                .setAlignTolerance(MathUtils.degreesToRadians * 10)
                .setDecelerationRadius(MathUtils.degreesToRadians * 10);

        // Dummy path
        Array<Vector2> waypoints = new Array<>();
        waypoints.add(new Vector2());
        waypoints.add(new Vector2());
        followPathSB = new FollowPath(enemy, new LinePath(waypoints), 0.05f, 0.5f);


        // Prefer directions towards target
        attack = new ContextBehavior(enemy, true) {
            @Override
            protected ContextMap calculateRealMaps(ContextMap map) {
                map.setZero();
                Vector2 targetDir = target.getPosition().sub(enemy.getPosition()).nor();
                for (int i = 0; i < map.getResolution(); i++) {
                    map.interestMap[i] = Math.max(0, map.dirFromSlot(i).dot(targetDir));
                }

                return map;
            }
        };

        strafe = new Strafe(enemy, target, Strafe.Rotation.COUNTERCLOCKWISE);

        separation = new ContextBehavior(enemy, true) {
            Ray<Vector2> rayCache = new Ray<>(new Vector2(), new Vector2());

            @Override
            protected ContextMap calculateRealMaps(ContextMap map) {
                map.setZero();
                for (int i = 0; i < map.getResolution(); i++) {
                    Vector2 dir = map.dirFromSlot(i);
                    // Ray extends two units
                    rayCache.set(enemy.getPosition(), dir.scl(2).add(enemy.getPosition()));
                    //System.out.printf("Ray: (%s, %s)\n", rayCache.start, rayCache.end);
                    communicationCollision.findCollision(collisionCache, rayCache);
                    if (commRay.hit) {
                        map.dangerMap[i] = raycast.fraction;
                    }
                }

                return map;
            }
        };
//        this.combinedContext.add(attack);
        this.combinedContext.add(strafe);
        this.combinedContext.add(separation);

        //Resolution is set to 8 to represent the 8 directions in which enemies can move
        this.battleSB = new ContextSteering(enemy, combinedContext, 20);
    }

    /**
     * Draws what the battleSB is thinking.
     * Lots of allocations here, should only be used for debugging.
     */
    public void drawGizmo(GameCanvas canvas) {
        float s_x = canvas.WorldToScreenX(1);
        float s_y = canvas.WorldToScreenY(1);
        float radius = 1;
        ContextMap map = battleSB.getMap();

        canvas.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // Draw interest map in green
        canvas.shapeRenderer.setColor(Color.GREEN);
        for (int i = 0; i < map.getResolution(); i++) {
            Vector2 dir = map.dirFromSlot(i);
            canvas.shapeRenderer.line(
                    getEnemy().getPosition().cpy().add(dir.cpy().scl(radius)).scl(s_x, s_y),
                    getEnemy().getPosition().cpy().add(dir.cpy().scl(radius + map.interestMap[i])).scl(s_x, s_y)
            );
        }

        // Draw danger map in red
        canvas.shapeRenderer.setColor(Color.RED);
        for (int i = 0; i < map.getResolution(); i++) {
            Vector2 dir = map.dirFromSlot(i);
            canvas.shapeRenderer.line(
                    getEnemy().getPosition().cpy().add(dir.cpy().scl(radius)).scl(s_x, s_y),
                    getEnemy().getPosition().cpy().add(dir.cpy().scl(radius + map.dangerMap[i])).scl(s_x, s_y)
            );
        }

        // Draw final vector in blue
        canvas.shapeRenderer.setColor(Color.BLUE);
        Vector2 dir = battleSB.getDirection();
        canvas.shapeRenderer.line(
                getEnemy().getPosition().cpy().add(dir.cpy().nor().scl(radius)).scl(s_x, s_y),
                getEnemy().getPosition().cpy().add(dir.cpy().nor().scl(radius + dir.len())).scl(s_x, s_y)
        );
        canvas.shapeRenderer.end();
    }

    /**
     * Updates the enemy being controlled by this controller
     *
     * @param delta time between last frame in seconds
     */
    public void update(LevelContainer container, float delta) {
        super.update(delta);
        if (enemy.hp <= 0) container.removeEnemy(enemy);

        //if (inBattle && !stateMachine.isInState(EnemyState.ALERT)) {
        //    stateMachine.changeState(EnemyState.ALERT);
        //}

        // Process the FSM
        enemy.update(delta);
        stateMachine.update();
        if (enemy.getDetection() == Enemy.Detection.NOTICED) {
            alert_sound.play();
        }
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

        // Fake range increasing for ALERT and INDICATOR
        float stealth = target.getStealth();
        if (enemy.getDetection() == Enemy.Detection.ALERT) {
            stealth = 1;
        } else if (enemy.getDetection() == Enemy.Detection.INDICATOR) {
            stealth = 0.5f;
        }
        if (getTarget().isOnMoonlight) {
            stealth = 2;
        }

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
            if (degree <= enemy.getFlashlight().getConeDegree() / 2 && dist <= lerp.apply(2.75f, 3f, stealth)) {
                return Enemy.Detection.ALERT;
            }
            if (degree <= 50 && dist <= lerp.apply(1.75f, 2f, stealth)) {
                return Enemy.Detection.ALERT;
            }
            if (degree <= 90 && dist <= lerp.apply(1.25f, 1.75f, stealth)) {
                return Enemy.Detection.ALERT;
            }
        }

        // target may be behind object, but enemy should still be able to hear
        if (dist <= lerp.apply(1.75f, 3.5f, stealth)) {
            return Enemy.Detection.NOTICED;
        }

        // Target is too far away
        return Enemy.Detection.NONE;
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
     * used to find ray collsion between this enemy and another enemy
     */
    public void findCollision(Enemy target) {
        communicationCollision.findCollision(commCache, new Ray<>(enemy.getPosition(), target.getPosition()));
    }

    /**
     * Updates path for pathfinding. Source is the enemy position and target is {@link #targetPos}
     */
    public void updatePath() {
        Path path = pathfinder.findPath(enemy.getPosition(), targetPos);
        followPathSB.setPath(path);
    }

    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
    }

    public boolean isInBattle() {
        return inBattle;
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
}