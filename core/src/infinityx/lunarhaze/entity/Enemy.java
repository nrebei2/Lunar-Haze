package infinityx.lunarhaze.entity;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.GameObject;
import infinityx.lunarhaze.LevelContainer;
import infinityx.lunarhaze.physics.ConeSource;

import java.util.ArrayList;

/**
 * Model class representing an enemy.
 */
public class Enemy extends GameObject implements Pool.Poolable, Steerable<Vector2> {
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
        super(0, 0);
        this.patrolPath = new ArrayList<>();
        animeframe = 0.0f;
        alerted = false;
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

    /**
     * Updates the animation frame and position of this enemy.
     * @param move impulse to apply
     */
    public void update(Vector2 move) {
        body.applyLinearImpulse(move, new Vector2(), true);

        //Limits maximum speed
        if (body.getLinearVelocity().len() > 1f) {
            body.setLinearVelocity(body.getLinearVelocity().clamp(0f, 1f));
        }

    }

    /**
     * Sets the specific angle of the enemy (and thus flashlight)
     *
     * @param ang the angle
     */
    public void setFlashLightRot(float ang) {
        body.setTransform(body.getPosition(), ang);
    }


    /**
     * Returns the bounding radius of this Steerable.
     */
    @Override
    public float getBoundingRadius() {
        return this.getWidth() / 2;
    }

    /**
     * Returns {@code true} if this Steerable is tagged; {@code false} otherwise.
     */
    @Override
    public boolean isTagged() {
        return false;
    }

    /**
     * Tag/untag this Steerable. This is a generic flag utilized in a variety of ways.
     *
     * @param tagged the boolean value to set
     */
    @Override
    public void setTagged(boolean tagged) {
    }

    /**
     * Returns the threshold below which the linear speed can be considered zero. It must be a small positive value near to zero.
     * Usually it is used to avoid updating the orientation when the velocity vector has a negligible length.
     */
    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0.01f;
    }

    /**
     * Sets the threshold below which the linear speed can be considered zero. It must be a small positive value near to zero.
     * Usually it is used to avoid updating the orientation when the velocity vector has a negligible length.
     *
     * @param value
     */
    @Override
    public void setZeroLinearSpeedThreshold(float value) {
    }

    /**
     * Returns the maximum linear speed.
     */
    @Override
    public float getMaxLinearSpeed() {
        return 0;
    }

    /**
     * Sets the maximum linear speed.
     *
     * @param maxLinearSpeed
     */
    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {

    }

    /**
     * Returns the maximum linear acceleration.
     */
    @Override
    public float getMaxLinearAcceleration() {
        return 0;
    }

    /**
     * Sets the maximum linear acceleration.
     *
     * @param maxLinearAcceleration
     */
    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {

    }

    /**
     * Returns the maximum angular speed.
     */
    @Override
    public float getMaxAngularSpeed() {
        return 0;
    }

    /**
     * Sets the maximum angular speed.
     *
     * @param maxAngularSpeed
     */
    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {

    }

    /**
     * Returns the maximum angular acceleration.
     */
    @Override
    public float getMaxAngularAcceleration() {
        return 0;
    }

    /**
     * Sets the maximum angular acceleration.
     *
     * @param maxAngularAcceleration
     */
    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {

    }

    /**
     * Returns the float value indicating the orientation of this location. The orientation is the angle in radians representing
     * the direction that this location is facing.
     */
    @Override
    public float getOrientation() {
        return 0;
    }

    /**
     * Sets the orientation of this location, i.e. the angle in radians representing the direction that this location is facing.
     *
     * @param orientation the orientation in radians
     */
    @Override
    public void setOrientation(float orientation) {

    }

    /**
     * Returns the angle in radians pointing along the specified vector.
     *
     * @param vector the vector
     */
    @Override
    public float vectorToAngle(Vector2 vector) {
        return 0;
    }

    /**
     * Returns the unit vector in the direction of the specified angle expressed in radians.
     *
     * @param outVector the output vector.
     * @param angle     the angle in radians.
     * @return the output vector for chaining.
     */
    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        return null;
    }

    /**
     * Creates a new location.
     * <p>
     * This method is used internally to instantiate locations of the correct type parameter {@code T}. This technique keeps the API
     * simple and makes the API easier to use with the GWT backend because avoids the use of reflection.
     *
     * @return the newly created location.
     */
    @Override
    public Location<Vector2> newLocation() {
        return null;
    }
}
