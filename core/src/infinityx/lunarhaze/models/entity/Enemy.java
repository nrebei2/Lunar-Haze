package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.SteeringGameObject;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.util.Direction;
import infinityx.util.PatrolPath;

import java.util.Random;

/**
 * Model class representing an enemy.
 */
public abstract class Enemy extends SteeringGameObject implements Pool.Poolable {

    /**
     * Types of enemies
     */
    public enum EnemyType {
        Villager,
        Archer;

        /**
         * Inverse of to string
         */
        public static EnemyType fromString(String string) {
            if (string.equalsIgnoreCase("villager"))
                return Villager;
            return Archer;
        }
    }

    public enum Detection {
        /**
         * The enemy is alerted (Exclamation point!)
         */
        ALERT,
        /**
         * The enemy has noticed sometime amiss (Question mark?)
         */
        NOTICED,
        /**
         * The enemy indicator is increasing
         */
        INDICATOR,
        /**
         * Neither heard nor seen anything
         */
        NONE
    }

    /**
     * The current detection level
     */
    private Detection detection;

    /** How long (in seconds) this enemy was in the current detection */
    public float detectionTime;

    /**
     * How much the indicator has been filled, in [0, 1]
     */
    float indicatorAmount;

    /**
     * Patrol path for this enemy
     */
    private PatrolPath patrolPath;

    /**
     * The light source on this enemy representing the flashlight
     */
    private ConeSource flashlight;

    /**
     * The direction the enemy is facing
     */
    public Direction direction;

    /**
     * Sound effect of enemy alerted
     */
    private Sound alert_sound;

    /**
     * Current filmstrip directions prefix
     */
    private String name = "idle";

    /**
     * Whether the enemy is in battle mode
     */
    private boolean inBattle;
    /**
     * Whether the enemy alerting allies
     */
    private boolean isAlerting;

    /** Used to update the direction sprite only once per interval */
    private float timeSinceLastDirChange = 0f;
    private static final float DIR_CHANGE_INTERVAL = 0.5f;

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
     * Returns the specific enemy type of this object
     */
    public abstract EnemyType getEnemyType();

    public ConeSource getFlashlight() {
        return flashlight;
    }

    public float strafeDistance;

    public Random rand = new Random();


    /**
     * Initialize an enemy with dummy position, id, and patrol path
     */
    public Enemy() {
        super(false);
        detection = Detection.NONE;
        inBattle = false;
        direction = Direction.RIGHT;
        detectionTime = 0;
        isAlerting = false;


        // TODO
//        setMaxLinearAcceleration(0.61f);
//        setMaxLinearSpeed(2.61f);

        setMaxLinearAcceleration(0.3f);
        setMaxLinearSpeed(0.8f);

        setMaxAngularAcceleration(1);
        // Angular speed is only used in NOTICED when turning around
        setMaxAngularSpeed(1f);
    }

    @Override
    public void reset() {
        hp = maxHp;
        detection = Detection.NONE;
        tint.set(Color.WHITE);
        setScale(1);
    }

    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
    }

    public boolean isInBattle() {
        return inBattle;
    }

    /**
     * Parse and initialize specific enemy  attributes.
     *
     * @param json      Json tree holding enemy information
     * @param container LevelContainer which this player is placed in
     */
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);

        JsonValue light = json.get("flashlight");
        float[] color = light.get("color").asFloatArray();
        float dist = light.getFloat("distance");
        int rays = light.getInt("rays");
        float degrees = light.getFloat("degrees");

        ConeSource flashLight = new ConeSource(
                container.getRayHandler(), rays, Color.RED, dist,
                getX(), getY(), 0f, degrees
        );
        flashLight.setColor(color[0], color[1], color[2], color[3]);
        flashLight.setSoft(light.getBoolean("soft"));

        // Body may already exist since enemy is pooled
        if (body == null) activatePhysics(container.getWorld());
        setFlashlight(flashLight);
        setFlashlightOn(true);
    }

    public void updateStrafeDistance() {
        // set strafe distance randomly between attack range*2 and attackrange*2 + 2
        this.strafeDistance = rand.nextInt(3)  + getAttackRange() * 2;
    }

    public float getStrafeDistance() {
        return this.strafeDistance;
    }

    public void setPatrolPath(PatrolPath patrolPath) {
        this.patrolPath = patrolPath;
    }

    public PatrolPath getPatrolPath() {
        return patrolPath;
    }

    public void setDetection(Detection detection) {
        if (detection == this.detection) return;
        this.detectionTime = 0;
        this.detection = detection;
    }

    public void setAlerting(Boolean bool){
        this.isAlerting = bool;
    }

    public boolean getAlerting(){
        return isAlerting;
    }

    public Detection getDetection() {
        return detection;
    }

    public void setIndicatorAmount(float indicatorAmount) {
        this.indicatorAmount = indicatorAmount;
    }

    public float getIndicatorAmount() {
        return indicatorAmount;
    }

    /**
     * Attaches light to enemy as a flashlight
     */
    public void setFlashlight(ConeSource cone) {
        flashlight = cone;
        attachFlashlight(true);
        flashlight.setActive(false);
    }

    /**
     *
     * @param attach Whether to attach or detach
     */
    public void attachFlashlight(boolean attach) {
        if (attach) {
            flashlight.attachToBody(getBody(), 0.5f, 0, 0);
        } else {
            flashlight.attachToBody(null);
        }
    }

    /**
     * @param on Whether to turn the flashlight on (true) or off (false)
     */
    public void setFlashlightOn(boolean on) {
        flashlight.setActive(on);
    }

    /**
     * @return percentage of current hp to maximum hp
     */
    public float getHealthPercentage() {
        return hp / maxHp;
    }

    /**
     * Sets the filmstrip animation of the enemy. Assumes there exists filmstrips for each cardinal direction with suffixes "-b", "-f", "-l", "-r".
     * The enemy will then automatically switch to the filmstrip depending on its direction.
     *
     * @param name Common prefix of filmstrip family. See {@link GameObject#setTexture(String)}.
     */
    public void setFilmstripPrefix(String name) {
        if (this.name != null && this.name.equals(name)) return;
        this.name = name;
        setTexDir(direction);
    }

    /**
     * Sets the texture from {@link #name} depending on direction
     */
    private void setTexDir(Direction direction) {
        switch (direction) {
            case UP:
                setTexture(name + "-b");
                break;
            case DOWN:
                setTexture(name + "-f");
                break;
            case LEFT:
                setTexture(name + "-l");
                break;
            case RIGHT:
                setTexture(name + "-r");
                break;
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        detectionTime += delta;

        timeSinceLastDirChange += delta;
        if (timeSinceLastDirChange >= DIR_CHANGE_INTERVAL) {
            float orientation = getOrientation();
            Direction newDirection;

            if (MathUtils.isEqual(orientation, 0, MathUtils.PI / 6)) {
                newDirection = Direction.RIGHT;
            } else if (MathUtils.isEqual(orientation, MathUtils.PI / 2, MathUtils.PI / 3)) {
                newDirection = Direction.UP;
            } else if (MathUtils.isEqual(orientation, -MathUtils.PI / 2, MathUtils.PI / 3)) {
                newDirection = Direction.DOWN;
            } else {
                newDirection = Direction.LEFT;
            }

            if (newDirection != direction) {
                setTexDir(newDirection);
                timeSinceLastDirChange = 0;
            }
            direction = newDirection;
        }
    }

}
