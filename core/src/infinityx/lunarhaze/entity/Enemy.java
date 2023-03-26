package infinityx.lunarhaze.entity;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.behaviors.CollisionAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.GameObject;
import infinityx.lunarhaze.LevelContainer;
import infinityx.lunarhaze.SteeringGameObject;
import infinityx.lunarhaze.physics.ConeSource;

import java.util.ArrayList;

/**
 * Model class representing an enemy.
 */
public class Enemy extends SteeringGameObject implements Pool.Poolable {
    /**
     * Current animation frame for this werewolf
     */
    private final float animeframe;

    /**
     * Whether the enemy is alerted.
     */
    private Boolean alerted;

    /**
     * rectangular region represented by [[b_lx, b_ly], [t_rx, t_ry]]
     */
    private ArrayList<Vector2> patrolPath;

    /**
     * The light source on this enemy representing the flashlight
     */
    private ConeSource flashlight;

    private float attackKnockback;

    private int attackDamage;

    /** The maximum amount of hit-points for this enemy */
    private float maxHp;

    /** The current amount of hit-points for this enemy */
    private float hp;

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
     * @return whether the enemy is alerted
     */
    public boolean getAlerted () {
        return alerted;
    }


    /**
     * Initialize an enemy with dummy position, id, and patrol path
     */
    public Enemy() {
        super(false);
        this.patrolPath = new ArrayList<>();
        animeframe = 0.0f;
        alerted = false;

        // TODO
        setMaxLinearAcceleration(1);
        setMaxAngularAcceleration(1);
        setMaxAngularSpeed(1);
        setMaxLinearSpeed(1);
    }

    /**
     * Resets the object for reuse.
     */
    @Override
    public void reset() {
        hp = maxHp;
        alerted = false;
    }

    /**
     * Parse and initialize specific enemy  attributes.
     *
     * @param json      Json tree holding enemy information
     * @param container LevelContainer which this player is placed in
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

        // Body may already exist since enemy is pooled
        if (body == null) activatePhysics(container.getWorld());
        setFlashlight(flashLight);

        setFlashlightOn(true);

        JsonValue attack = json.get("attack");
        setAttackKnockback(attack.getFloat("knockback"));
        setAttackDamage(attack.getInt("damage"));
        setMaxHp(json.getFloat("hp"));
    }

    public void setPatrolPath(ArrayList<Vector2> path) {
        this.patrolPath = path;
    }

    /**
     * Sets whether the enemy is alerted.
     */
    public void setAlerted(Boolean value) {
        alerted = value;
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

    public float getAttackKnockback() {
        return attackKnockback;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public void setAttackKnockback(float knockback) {
        attackKnockback = knockback;
    }

    public void setAttackDamage(int dmg) {
        attackDamage = dmg;
    }

    public float getHp() {
        return hp;
    }

    /**
     * Sets the new hp of this enemy. Clamped to always be non-negative.
     * @param value the hp to set
     */
    public void setHp(float value) {
        hp = Math.max(0, value);
    }

    /**
     * Sets the max hp (and starting current hp) of this enemy.
     * @param value the hp to set
     */
    public void setMaxHp(float value) {
        maxHp = value;
        hp = maxHp;
    }

    /**
     * @return percentage of current hp to maximum hp
     */
    public float getHealthPercentage() {
        return hp / maxHp;
    }
}
