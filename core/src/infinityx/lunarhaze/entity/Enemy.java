package infinityx.lunarhaze.entity;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.EnemyController;
import infinityx.lunarhaze.GameObject;
import infinityx.lunarhaze.LevelContainer;
import infinityx.lunarhaze.physics.ConeSource;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import infinityx.util.Box2dLocation;
import infinityx.util.Box2dSteeringUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


import java.util.ArrayList;
import java.math.*;

public class Enemy extends GameObject implements Steerable<Vector2> {
// Instance Attributes
    /**
     * A unique identifier; used to decouple classes.
     */
    private int id;
    /**
     * Movement of the enemy
     **/
    private float movement;

    private Boolean faceRight;

    /**
     * Current animation frame for this werewolf
     */
    private final float animeframe;

    /**
     * Whether the enemy is alerted. Once alerted,
     * the enemy start chasing werewolf
     */
    private Boolean isAlerted;

    /**
     * Whether the enemy is alive.
     */
    private final boolean isAlive;

    /**
     * points (in Tile index) in the enemy's patrolPath
     */
    // TODO: if we wanna be fancy use a bezier
    // Not that bad since we can easily compute the tangent and add force on enemy along it
    private ArrayList<Vector2> patrolPath;

    private Direction direction;

    private ConeSource flashlight;

    private float attackKnockback;

    private float attackDamage;

    /** -----------------------------------------------------START---------------------------------------------*/

    /** LIBGDX AI */
    private final float boundingRadius = 1f;
    private boolean tagged;
    private float maxLinearAcceleration;
    private float maxAngularAcceleration;
    private float maxLinearSpeed = 100f;
    private float maxAngularSpeed = 200f;

    private float zeroLinearSpeedThreshold;

    private SteeringBehavior<Vector2> steeringBehavior;
    private final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<>(new Vector2());


    /** LIBGDX AI */

    /** -----------------------------------------------------START---------------------------------------------*/

    // behavior
    public static final int WANDER_BEHAVIOR = 0;
    public static final int ARRIVE_BEHAVIOR = 1;


    // Define the enemy's behaviors
    //TODO CHANGE IT LATER
    private Arrive<Vector2> arriveBehavior;
    private Wander<Vector2> wanderBehavior;

    public Arrive<Vector2> getArriveBehavior() {
        return arriveBehavior;
    }

    public Wander<Vector2> getWanderBehavior() {
        return wanderBehavior;
    }

    @Override
    public Vector2 getLinearVelocity() {
        return body.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity() {
        return body.getAngularVelocity();
    }

    @Override
    public float getBoundingRadius() {
        return boundingRadius;
    }

    @Override
    public boolean isTagged() {
        return tagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public float getOrientation() {
        return body.getAngle();
    }

    @Override
    public void setOrientation(float orientation) {
        body.setTransform(getPosition(), orientation);
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return Box2dSteeringUtils.vectorToAngle(vector);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        return Box2dSteeringUtils.angleToVector(outVector, angle);

    }

    @Override
    public Location<Vector2> newLocation() {
        return new Box2dLocation(new Vector2(0,0));
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return zeroLinearSpeedThreshold;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        this.zeroLinearSpeedThreshold = value;
    }

    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcceleration = maxAngularAcceleration;
    }


    /**
     * Initialize an enemy not alerted.
     */
    public Enemy(int id, float x, float y) {
        super(x, y);
        this.id = id;
        isAlive = true;
        animeframe = 0.0f;
        isAlerted = false;
        direction = Direction.NORTH;
    }

    private void createWanderBehavior() {
        wanderBehavior = new Wander<>(this)
                .setEnabled(true)
                .setWanderRadius(2f)
                .setWanderRate((float) (Math.PI * 4))
                .setWanderOffset(2f)
                .setWanderOrientation(0);
    }


    /**
     * Initialize an enemy with dummy position, id, and patrol path
     */
    public Enemy() {
        super(0, 0);
        isAlive = true;
        this.patrolPath = new ArrayList<>();
        animeframe = 0.0f;
        isAlerted = false;
        direction = Direction.NORTH;

        tagged = false;
        maxAngularAcceleration = 1.0f;
        maxLinearAcceleration = 1.0f;
        maxLinearSpeed = 2f;
        maxAngularSpeed = 1.0f;

    }

    public void setBehavior(int behavior, Location target) {
        switch (behavior) {
            case ARRIVE_BEHAVIOR:
                if (arriveBehavior == null) {
                    if (target != null) {
                        arriveBehavior = new Arrive<>(this, target)
                                .setEnabled(true)
                                .setTimeToTarget(0.1f)
                                .setArrivalTolerance(0.5f);
                        steeringBehavior = arriveBehavior;
                    }
                } else {
                    steeringBehavior = arriveBehavior;
                }
                break;
            case WANDER_BEHAVIOR:
                steeringBehavior = wanderBehavior;
            default:
                break;
        }
    }



    // Apply the steering acceleration to the werewolf's velocity and position
    private void applySteering() {
        boolean anyAcceleration = false;

        if (!steeringOutput.linear.isZero()) {
            body.applyForceToCenter(steeringOutput.linear, true);
            anyAcceleration = true;
        }

        if (anyAcceleration) {

            // cap the linear speed
            Vector2 velocity = body.getLinearVelocity();
            float currentSpeedSquare = velocity.len2();
            if (currentSpeedSquare > maxLinearSpeed * maxLinearSpeed) {
                body.setLinearVelocity(velocity.scl(maxLinearSpeed / (float) Math.sqrt(currentSpeedSquare)));
            }
        }
    }

    /**
     * Updates the animation frame and position of this enemy.
     * <p>
     * Notice how little this method does.  It does not actively fire the weapon.  It
     * only manages the cooldown and indicates whether the weapon is currently firing.
     * The result of weapon fire is managed by the GameplayController.
     */
    public void update() {

        if (steeringBehavior != null) {
            steeringBehavior.calculateSteering(steeringOutput);
            this.applySteering();
            //applyingSteering(deltaTime);
        }
    }

    /** -----------------------------------------------------END---------------------------------------------*/


    public enum Direction {
        NORTH(1), SOUTH(3), WEST(2), EAST(0);

        private final int scale;

        private Direction(int scale) {
            this.scale = scale;
        }

        public int getRotScale() {
            return scale;
        }
    }


    /**
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    public ObjectType getType() {
        return ObjectType.ENEMY;
    }

    /**
     * Returns whether the enemy is alerted.
     */
    public Boolean getIsAlerted() {
        return isAlerted;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public ConeSource getFlashlight() {
        return flashlight;
    }

    /**
     * Sets whether the enemy is alerted.
     */
    public void setIsAlerted(Boolean value) {
        isAlerted = value;
    }

    private int currentWayPoint;

    /**
     * Initialize an enemy not alerted.
     */
    public Enemy(int id, float x, float y, ArrayList<Vector2> patrolPath) {
        super(x, y);
        this.id = id;
        isAlive = true;
        this.patrolPath = patrolPath;
        animeframe = 0.0f;
        isAlerted = false;
        direction = Direction.NORTH;
    }

//    /**
//     * Initialize an enemy with dummy position, id, and patrol path
//     */
//    public Enemy() {
//        super(0, 0);
//        isAlive = true;
//        this.patrolPath = new ArrayList<>();
//        animeframe = 0.0f;
//        isAlerted = false;
//        direction = Direction.NORTH;
//    }

    /**
     * Initalize the enemy with the given data
     */
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        JsonValue p_dim = json.get("collider");
        setDimension(p_dim.get("width").asFloat(), p_dim.get("height").asFloat());

        super.initialize(directory, json, container);

        JsonValue light = json.get("flashlight");
        float[] color = light.get("color").asFloatArray();
        float dist = light.getFloat("distance");
        int rays = light.getInt("rays");
        float degrees = light.getFloat("degrees");

        ConeSource flashLight = new ConeSource(
                container.getRayHandler(), rays, Color.WHITE, dist,
                getX(), getY(), 0f, degrees
        );
        flashLight.setColor(color[0], color[1], color[2], color[3]);
        flashLight.setSoft(light.getBoolean("soft"));

        activatePhysics(container.getWorld());
        setFlashlight(flashLight);

        setFlashlightOn(true);
        //getBody().setActive(false);

        JsonValue attack = json.get("attack");
        setAttackKnockback(attack.getFloat("knockback"));
        setAttackDamage(attack.getFloat("damage"));

        createWanderBehavior();

    }

    /**
     * Deep clones enemy, can be used independently of this
     *
     * @return new enemy
     */
    public Enemy deepClone(LevelContainer container) {
        Enemy enemy = new Enemy();
        enemy.setSpeed(speed);
        enemy.setTexture(getTexture());
        enemy.setOrigin((int) origin.x, (int) origin.y);
        ConeSource flashLight = new ConeSource(
                container.getRayHandler(), this.flashlight.getRayNum(), this.flashlight.getColor(), this.flashlight.getDistance(),
                0, 0, this.flashlight.getDirection(), this.flashlight.getConeDegree()
        );
        flashLight.setSoft(this.flashlight.isSoft());
        enemy.setBodyState(body);
        enemy.activatePhysics(container.getWorld());
        enemy.setFlashlight(flashlight);
        enemy.setFlashlightOn(true);

        enemy.setDimension(getDimension().x, getDimension().y);
        enemy.setPositioned(positioned);
        return enemy;
    }

    /**
     * get the next patrol point of the enemy
     */
    public Vector2 getNextPatrol() {
        Vector2 next = patrolPath.get(currentWayPoint);
        currentWayPoint++;
        if (currentWayPoint > patrolPath.size() - 1) {
            currentWayPoint = 0;
        }
        return next;
    }

    /**
     * get the patrol point this enemy is currently moving to
     */
    public Vector2 getCurrentPatrol() {
        return patrolPath.get(currentWayPoint);
    }

    public void setPatrolPath(ArrayList<Vector2> path) {
        this.patrolPath = path;
    }

    /**
     * Returns whether or not the ship is alive.
     * <p>
     * A ship is dead once it has fallen past MAX_FALL_AMOUNT. A dead ship cannot be
     * targeted, involved in collisions, or drawn.  For all intents and purposes, it
     * does not exist.
     *
     * @return whether or not the ship is alive
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Attaches light to enemy as a flashlight
     */
    public void setFlashlight(ConeSource cone) {
        flashlight = cone;
        flashlight.attachToBody(getBody(), 0.5f, 0, flashlight.getDirection());
        flashlight.setActive(false);
    }

    /**
     * @param on Whether to turn the flashlight on (true) or off (false)
     */
    public void setFlashlightOn(boolean on) {
        flashlight.setActive(on);
    }

    public float getAttackKnockback() {
        return attackKnockback;
    }

    public float getAttackDamage() {
        return attackDamage;
    }

    public void setAttackKnockback(float knockback) {
        attackKnockback = knockback;
    }

    public void setAttackDamage(float dmg) {
        attackDamage = dmg;
    }

    /**
     * Updates the animation frame and position of this enemy.
     * <p>
     * Notice how little this method does.  It does not actively fire the weapon.  It
     * only manages the cooldown and indicates whether the weapon is currently firing.
     * The result of weapon fire is managed by the GameplayController.
     */
    public void update(int controlCode) {
        boolean movingLeft = (controlCode & EnemyController.CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (controlCode & EnemyController.CONTROL_MOVE_RIGHT) != 0;
        boolean movingDown = (controlCode & EnemyController.CONTROL_MOVE_DOWN) != 0;
        boolean movingUp = (controlCode & EnemyController.CONTROL_MOVE_UP) != 0;

        float xVelocity = 0.0f;
        float yVelocity = 0.0f;
        if (movingLeft) {
            xVelocity = -speed;
            direction = Direction.WEST;
        } else if (movingRight) {
            xVelocity = speed;
            direction = Direction.EAST;
        }
        if (movingDown) {
            yVelocity = -speed;
            direction = Direction.SOUTH;
        } else if (movingUp) {
            yVelocity = speed;
            direction = Direction.NORTH;
        }
        body.setLinearVelocity(xVelocity, yVelocity);
    }

    /**
     * As the name suggests.
     * Can someone think of a better name? Im too tired for this rn
     */
    public void setFlashLightRotAlongDir() {
        body.setTransform(body.getPosition(), getDirection().getRotScale() * (float) (Math.PI / 2f));
    }

    /**
     * Sets the specific angle of the flashlight on this enemy
     *
     * @param ang the angle...
     */
    public void setFlashLightRot(float ang) {
        body.setTransform(body.getPosition(), ang);

    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
