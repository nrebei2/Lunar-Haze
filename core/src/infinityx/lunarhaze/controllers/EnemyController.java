package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.ai.*;
import infinityx.lunarhaze.combat.AttackHandler;
import infinityx.lunarhaze.combat.MeleeHandler;
import infinityx.lunarhaze.combat.RangeHandler;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.*;
import infinityx.lunarhaze.physics.Box2DRaycastCollision;
import infinityx.lunarhaze.physics.RaycastInfo;
import infinityx.util.PatrolPath;
import infinityx.util.astar.AStarPathFinding;

/**
 * Controller class, handles logic for a single enemy
 */
public class EnemyController {

    // Constants for vision cone lengths
    private static float FOCUSED_MIN = 3.0f;
    private static float FOCUSED_MAX = 3.7f;
    private static float SHORT_MIN = 2.25f;
    private static float SHORT_MAX = 3.2f;
    private static float PERIPHERAL_MIN = 1.9f;
    private static float PERIPHERAL_MAX = 2.5f;

    /**
     * Output collision cache from Box2DRaycastCollision
     */
    final Collision<Vector2> collCache;

    /**
     * Raycast cache
     */
    final RaycastInfo raycast;

    /**
     * Raycast cache for detection specifically
     */
    final RaycastInfo detectionCast;

    /**
     * Collision detector for target detection
     */
    private Box2DRaycastCollision raycastCollision;

    /**
     * Collision detector for path detection
     */
    public Box2DRaycastCollision pathCollision;

    /**
     * Collision detector for enemy communication
     */
    public Box2DRaycastCollision communicationCollision;

    /**
     * Pathfinder reference from level container
     */
    public AStarPathFinding pathfinder;

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
     * Pathfinding behavior
     */
    public FollowPath followPathSB;

    /**
     * Source position used for pathfinding
     */
    private Vector2 sourcePos;

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
    /**
     * Steering behavior for evading
     */
    public ContextBehavior evade;

    public ContextSteering avoidSB;

    public PrioritySteering<Vector2> followPathAvoid;

    /**
     * Attack handler for the attached enemy
     */
    private AttackHandler attackHandler;

    Ray<Vector2> rayCache = new Ray<>(new Vector2(), new Vector2());

    public Sound getAlertSound() {
        return alert_sound;
    }

    public void setAlertSound(Sound s) {
        alert_sound = s;
    }

    private Enemy.Detection cachedDetection;

    /** Used so the detection only updates once every interval */
    private float detectionTime;

    /**
     * Creates an EnemyController for the given enemy.
     *
     * @param enemy The enemy being controlled by this AIController
     */
    public EnemyController(Enemy enemy) {
        patrolTarget = new Vector2();
        this.targetPos = new Vector2();
        this.sourcePos = new Vector2();
        this.enemy = enemy;
        this.stateMachine = new DefaultStateMachine<>(this, EnemyState.INIT, EnemyState.ANY_STATE);
        this.combinedContext = new CombinedContext(enemy);
        cachedDetection = Enemy.Detection.NONE;

        this.raycast = new RaycastInfo(enemy) {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                // Right now, all hit bodies are contained in GameObjects
                GameObject objHit = (GameObject) fixture.getBody().getUserData();

                if (objHit == requestingObject || ignore.contains(objHit.getType())) {
                    return 1;
                }
                if (outputCollision != null)
                    outputCollision.set(point, normal);
                this.fixture = fixture;
                this.fraction = fraction;
                this.hit = fraction != 0;
                if (this.hit) {
                    this.hitObject = objHit;
                }
                return fraction;
            }
        };

        this.detectionCast = new RaycastInfo(enemy) {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                // Right now, all hit bodies are contained in GameObjects
                GameObject objHit = (GameObject) fixture.getBody().getUserData();

                if (objHit == requestingObject || ignore.contains(objHit.getType())) {
                    return 1;
                }

                // Further ignore objects that can be overlooked
                if (objHit.getType() == GameObject.ObjectType.SCENE && ((SceneObject) objHit).isSeeThru()) {
                    return 1;
                }
                if (outputCollision != null)
                    outputCollision.set(point, normal);
                this.fixture = fixture;
                this.fraction = fraction;
                this.hit = fraction != 0;
                if (this.hit) {
                    this.hitObject = objHit;
                }
                return fraction;
            }
        };
        this.collCache = new Collision<>(new Vector2(), new Vector2());
    }

    /**
     * Populate attributes used for sensory information.
     *
     * @param container holding surrounding model objects
     */
    public void populate(final LevelContainer container) {
        detectionTime = 0;
        target = container.getPlayer();

        switch (enemy.getEnemyType()) {
            case Villager:
                this.attackHandler = new MeleeHandler(enemy, ((Villager) enemy).attackHitbox);
                break;
            case Archer:
                this.attackHandler = new RangeHandler((Archer) enemy, target, container);
                break;
        }

        this.raycastCollision = new Box2DRaycastCollision(container.getWorld(), detectionCast);
        raycastCollision
                .addIgnore(GameObject.ObjectType.ENEMY)
                .addIgnore(GameObject.ObjectType.HITBOX);

        this.communicationCollision = new Box2DRaycastCollision(container.getWorld(), raycast);
        communicationCollision
                .addIgnore(GameObject.ObjectType.WEREWOLF)
                .addIgnore(GameObject.ObjectType.HITBOX);

        this.pathCollision = new Box2DRaycastCollision(container.getWorld(), raycast);
        pathCollision
                .addIgnore(GameObject.ObjectType.WEREWOLF)
                .addIgnore(GameObject.ObjectType.ENEMY)
                .addIgnore(GameObject.ObjectType.HITBOX);

        this.pathfinder = container.pathfinder;

        // Dummy path
        Array<Vector2> waypoints = new Array<>();
        waypoints.add(new Vector2());
        waypoints.add(new Vector2());
        followPathSB = new FollowPath(enemy, new LinePath(waypoints), 0.05f, 0.5f);

//        Separation<Vector2> avoid = new Separation<>(enemy, (Proximity<Vector2>) container.getEnemies());


        // Prefer directions towards target
        attack = new ContextBehavior(enemy, false) {
            @Override
            protected ContextMap calculateRealMaps(ContextMap map) {
                map.setZero();
                if (getAttackHandler().canStartNewAttack()) {
                    Vector2 targetDir = target.getPosition().sub(enemy.getPosition()).nor();
                    for (int i = 0; i < map.getResolution(); i++) {
                        map.interestMap[i] = Math.max(0, map.dirFromSlot(i).dot(targetDir));
                    }
                }

                return map;
            }
        };

        strafe = new Strafe(enemy, target, Strafe.Rotation.COUNTERCLOCKWISE);

        separation = new ContextBehavior(enemy, true) {

            @Override
            protected ContextMap calculateRealMaps(ContextMap map) {
                map.setZero();
                for (int i = 0; i < map.getResolution(); i++) {
                    Vector2 dir = map.dirFromSlot(i);
                    // Ray extends two units
                    rayCache.set(enemy.getPosition(), dir.scl(1.5f).add(enemy.getPosition()));
                    //System.out.printf("Ray: (%s, %s)\n", rayCache.start, rayCache.end);
                    communicationCollision.findCollision(collCache, rayCache);
                    if (raycast.hit) {
                        map.dangerMap[i] = 1;

                        for (int j = -2; j <= 2; j++) {
                            map.dangerMap[(i + j + map.getResolution()) % map.getResolution()] = 1;
                        }

                    }
                }

                return map;
            }
        };

        evade = new ContextBehavior(enemy, true) {
            @Override
            protected ContextMap calculateRealMaps(ContextMap map) {
                map.setZero();
                if (!getAttackHandler().canStartNewAttack()) {
                    Vector2 evade_dir = enemy.getPosition().sub(target.getPosition());
                    for (int i = 0; i < map.getResolution(); i++) {
                        map.interestMap[i] = 1 / evade_dir.len() * Math.max(0, map.dirFromSlot(i).dot(evade_dir.nor()));
                    }
                }

                return map;
            }
        };
        this.combinedContext.add(attack);
        this.combinedContext.add(strafe);
        this.combinedContext.add(separation);
        this.combinedContext.add(evade);

        avoidSB = new ContextSteering(enemy, separation, 30);

        followPathAvoid = new PrioritySteering<>(enemy);
        followPathAvoid.add(avoidSB);
        followPathAvoid.add(followPathSB);

        this.battleSB = new ContextSteering(enemy, combinedContext, 30);
    }

    public AttackHandler getAttackHandler() {
        return attackHandler;
    }

    /**
     * Draws what the battleSB is thinking.
     * Lots of allocations here, should only be used for debugging.
     */
    public void drawGizmo(GameCanvas canvas) {
        float radius = 1;
        ContextMap map = battleSB.getMap();

        canvas.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // Draw interest map in green
        canvas.shapeRenderer.setColor(Color.GREEN);
        for (int i = 0; i < map.getResolution(); i++) {
            Vector2 dir = map.dirFromSlot(i);
            canvas.shapeRenderer.line(
                    getEnemy().getPosition().cpy().add(dir.cpy().scl(radius)),
                    getEnemy().getPosition().cpy().add(dir.cpy().scl(radius + map.interestMap[i]))
            );
        }

        // Draw danger map in red
        canvas.shapeRenderer.setColor(Color.RED);
        for (int i = 0; i < map.getResolution(); i++) {
            Vector2 dir = map.dirFromSlot(i);
            canvas.shapeRenderer.line(
                    getEnemy().getPosition().cpy().add(dir.cpy().scl(radius)),
                    getEnemy().getPosition().cpy().add(dir.cpy().scl(radius + map.dangerMap[i]))
            );
        }

        // Draw final vector in blue
        canvas.shapeRenderer.setColor(Color.BLUE);
        Vector2 dir = battleSB.getDirection();
        canvas.shapeRenderer.line(
                getEnemy().getPosition().cpy().add(dir.cpy().nor().scl(radius)),
                getEnemy().getPosition().cpy().add(dir.cpy().nor().scl(radius + dir.len()))
        );
        canvas.shapeRenderer.end();
    }

    /**
     * Updates the enemy being controlled by this controller
     *
     * @param delta time between last frame in seconds
     */
    public void update(LevelContainer container, float delta) {
        detectionTime += delta;
        time += delta;
        attackHandler.update(delta);
        if (enemy.hp <= 0) {
            container.removeEnemy(enemy);
        }

        if (enemy.isInBattle() && stateMachine.getCurrentState() != EnemyState.ALERT && !enemy.isAttacking()) {
            stateMachine.changeState(EnemyState.ALERT);
        }

        // Process the FSM
        stateMachine.update();
        enemy.update(delta);

        // If the enemy is alerted and the player is close enough, force the flashlight to always shine on the player
        switch (enemy.getDetection()) {
            case ALERT:
            case INDICATOR:
                if (getDetection() != Enemy.Detection.NONE) {
                    enemy.getFlashlight().updateDirection(false);
                    Vector2 enemyToTarget = target.getPosition().sub(enemy.getPosition());
                    enemy.getFlashlight().setDirection(enemyToTarget.angleDeg());
                    break;
                }
            case NOTICED:
            case NONE:
                enemy.getFlashlight().updateDirection(true);
        }
    }


    /**
     * @return the current detection the enemy has on the target
     */
    public Enemy.Detection getDetection() {
        /* Area of interests:
         * Focused view - same angle as flashlight, extends between [2.75, 3]
         * Short distance - angle of 100, extends between [1.75, 2]
         * Peripheral vision - angle of 180, extends between [1.25, 2.25]
         * Hearing radius - angle of 360, extends between [1.75, 3.5]
         * Lerp between player stealth for max distance,
         * but maybe add cutoffs for NONE?
         */
        if (enemy.isInBattle()) return Enemy.Detection.ALERT;
        if (detectionTime < 0.5f) {
            return cachedDetection;
        }
        detectionTime = 0;

        // Fake range increasing for ALERT and INDICATOR
        float stealth = target.getStealth();
        if (enemy.getDetection() == Enemy.Detection.ALERT) {
            stealth *= 1.5f;
        } else if (enemy.getDetection() == Enemy.Detection.INDICATOR) {
            stealth *= 1.2f;
        }

        Interpolation lerp = Interpolation.linear;
        raycastCollision.findCollision(collCache, rayCache.set(enemy.getPosition(), target.getPosition()));

        if (!detectionCast.hit) {
            // For any reason...
            cachedDetection = Enemy.Detection.NONE;
            return Enemy.Detection.NONE;
        }

        Vector2 enemyToPlayer = target.getPosition().sub(enemy.getPosition());
        float dist = enemyToPlayer.len();

        // degree between enemy orientation and enemy-to-player
        double degree = Math.abs(enemy.getOrientation() - enemy.vectorToAngle(enemyToPlayer)) * MathUtils.radiansToDegrees;

        if (detectionCast.hitObject == target) {
            if (degree <= enemy.getFlashlight().getConeDegree() / 2 && dist <= lerp.apply(FOCUSED_MIN, FOCUSED_MAX, stealth)) {
                cachedDetection = Enemy.Detection.ALERT;
                return Enemy.Detection.ALERT;
            }
            if (degree <= 50 && dist <= lerp.apply(SHORT_MIN, SHORT_MAX, stealth)) {
                cachedDetection = Enemy.Detection.ALERT;
                return Enemy.Detection.ALERT;
            }
            if (degree <= 90 && dist <= lerp.apply(PERIPHERAL_MIN, PERIPHERAL_MAX, stealth)) {
                cachedDetection = Enemy.Detection.ALERT;
                return Enemy.Detection.ALERT;
            }
            if (dist <= target.getNoiseRadius()) {
                cachedDetection = Enemy.Detection.NOTICED;
                return Enemy.Detection.NOTICED;
            }
        }

        // Target is too far away
        cachedDetection = Enemy.Detection.NONE;
        return Enemy.Detection.NONE;
    }

    /**
     * Draws the areas of interests (cones of vision) for the enemy controlled by this controller
     */
    public void drawDetection(GameCanvas canvas) {
        canvas.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        canvas.shapeRenderer.setColor(Color.PURPLE);

        // Fake range increasing for ALERT and INDICATOR
        float stealth = target.getStealth();
        if (enemy.getDetection() == Enemy.Detection.ALERT) {
            stealth *= 1.5f;
        } else if (enemy.getDetection() == Enemy.Detection.INDICATOR) {
            stealth *= 1.2f;
        }

        Interpolation lerp = Interpolation.linear;

        canvas.shapeRenderer.arc(
                enemy.getX(),
                enemy.getY(),
                lerp.apply(FOCUSED_MIN, FOCUSED_MAX, stealth),
                enemy.getOrientation() * MathUtils.radiansToDegrees - enemy.getFlashlight().getConeDegree() / 2,
                enemy.getFlashlight().getConeDegree(), 20
        );
        canvas.shapeRenderer.arc(
                enemy.getX(),
                enemy.getY(),
                lerp.apply(SHORT_MIN, SHORT_MAX, stealth),
                enemy.getOrientation() * MathUtils.radiansToDegrees - 50,
                100, 20
        );
        canvas.shapeRenderer.arc(
                enemy.getX(),
                enemy.getY(),
                lerp.apply(PERIPHERAL_MIN, PERIPHERAL_MAX, stealth),
                enemy.getOrientation() * MathUtils.radiansToDegrees - 90,
                180, 20
        );

        canvas.shapeRenderer.end();
    }

    /**
     * @return A next way point to move to in patrol path
     */
    public Vector2 getPatrolTarget() {
        PatrolPath path = enemy.getPatrolPath();
        return patrolTarget.set(path.getNextPatrol());
    }

    /**
     * used to find ray collsion between this enemy and another enemy
     */
    public void findCollision(Enemy target) {
        communicationCollision.findCollision(collCache, rayCache.set(enemy.getPosition(), target.getPosition()));
    }

    /**
     * Updates path for pathfinding. Source is the enemy position and target is {@link #targetPos}
     */
    public void updatePath() {
        Path path = pathfinder.findPath(sourcePos.set(enemy.getPosition()), targetPos);
        followPathSB.setPath(path);
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
     * helper method for determining if an enemy is behind the player (greater than 90 degrees)
     */
    public boolean isBehind(Enemy enemy, Werewolf target) {
        Vector2 target_to_enemy = enemy.getPosition().sub(target.getPosition()).nor();
        double dot = target_to_enemy.x * Math.cos(target.getOrientation()) + target_to_enemy.y * Math.sin(target.getOrientation());

        return dot < 0;
    }
}