package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.SteeringGameObject;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.util.Direction;

import java.util.ArrayList;

/**
 * Model class representing an enemy.
 */
public class Enemy extends SteeringGameObject implements Pool.Poolable {
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

    /**
     * How much the indicator has been filled
     */
    float indicatorAmount;

    /**
     * rectangular region represented by [[b_lx, b_ly], [t_rx, t_ry]]
     */
    private ArrayList<Vector2> patrolPath;

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
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    public ObjectType getType() {
        return ObjectType.ENEMY;
    }

    public ConeSource getFlashlight() {
        return flashlight;
    }

    /**
     * Initialize an enemy with dummy position, id, and patrol path
     */
    public Enemy() {
        super(false);
        this.patrolPath = new ArrayList<>();
        detection = Detection.NONE;

        // TODO
        setMaxLinearAcceleration(0.5f);
        setMaxLinearSpeed(0.7f);
        setMaxAngularAcceleration(1);
        // Angular speed is only used in NOTICED when turning around
        setMaxAngularSpeed(0.05f);
    }

    @Override
    public void reset() {
        hp = maxHp;
        detection = Detection.NONE;
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

    public void setPatrolPath(ArrayList<Vector2> path) {
        this.patrolPath = path;
    }

    public ArrayList<Vector2> getPatrolPath() {
        return patrolPath;
    }

    public void setDetection(Detection detection) {
        this.detection = detection;
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

    public Vector2 getBottomLeftOfRegion() {
        return this.patrolPath.get(0);
    }

    public Vector2 getTopRightOfRegion() {
        return this.patrolPath.get(1);
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

    /**
     * @return percentage of current hp to maximum hp
     */
    public float getHealthPercentage() {
        return hp / maxHp;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        float orientation = getOrientation();
        // Set the direction given velocity
        // We give each direction an equal amount of degrees (90)
        if (MathUtils.isEqual(orientation, 0, MathUtils.PI / 4)) {
            direction = Direction.RIGHT;
        } else if (MathUtils.isEqual(orientation, MathUtils.PI / 2, MathUtils.PI / 4)) {
            direction = Direction.UP;
        } else if (MathUtils.isEqual(orientation, -MathUtils.PI / 2, MathUtils.PI / 4)) {
            direction = Direction.DOWN;
        } else {
            direction = Direction.LEFT;
        }
    }
}
