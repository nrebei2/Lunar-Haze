package infinityx.lunarhaze.entity;

import box2dLight.PointLight;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.GameObject;
import infinityx.lunarhaze.LevelContainer;
import infinityx.lunarhaze.SteeringGameObject;
import infinityx.lunarhaze.combat.AttackHitbox;
import infinityx.util.FilmStrip;
import infinityx.util.Box2dLocation;
import infinityx.util.Box2dSteeringUtils;

/**
 * Model class representing the player.
 */
public class Werewolf extends GameObject implements Location<Vector2> {
    /**
     * Initial light value of the werewolf is 0.0
     **/
    private static final float INITIAL_LIGHT = 0.0f;

    /**
     * Initial hp of the werewolf is 5
     **/
    public static int INITIAL_HP = 5;

    /**
     * Maximum hp of the werewolf is 5
     **/
    public static int MAX_HP = 10;

    /** Size of attack hitbox */
    public static float HITBOX_SIZE = 3f;

    /**
     * Maximum light of the werewolf is 100.0
     **/
    public static final float MAX_LIGHT = 100.0f;

    /**
     * Initial attack power of the werewolf is 0.2;
     **/
    public static final float INITIAL_POWER = 0.2f;

    /**
     * Initial attack range of the werewolf is 1.2 tile;
     **/
    public static final float INITIAL_RANGE = 1.2f;

    /**
     * Maximum attack power of the werewolf is 1.0;
     **/
    public static final float MAX_POWER = 1.0f;

    /**
     * Maximum attack range of the werewolf is 2 tiles;
     **/
    public static final float MAX_RANGE = 2.0f;

    /**
     * Move speed (walking)
     **/
    public float walkSpeed;

    /**
     * Move speed (running)
     **/
    public float runSpeed;

    /**
     * Whether the werewolf can move or not; the werewolf can't move
     * if its being knocked back by an attack.
     **/
    private boolean canMove;

    /**
     * Controls how long the werewolf gets knocked back by an attack and the window of the
     * damage animation.
     */
    private float lockout;

    /**
     * The right/left movement of the werewolf
     **/
    private float movementH = 0.0f;

    /**
     * The up/down movement of the werewolf
     **/
    private float movementV = 0.0f;

    /**
     * Whether the  player stands on a moonlight tile
     **/
    private Boolean moonlight;

    /**
     * Number of moonlight tiles collected
     **/
    private int moonlightCollected;

    /**
     * Current animation frame for this werewolf
     */
    private final float animeframe;

    /**
     * Health point (hp) of the werewolf
     */

    private int hp;

    /**
     * Light collected of the werewolf.
     * The value is a percentage of light on the map, between 0 and 100.
     */
    private float light;

    /**
     * Stealth value of the werewolf.
     * The value is a float between 0 and 1.
     */
    private float stealth;


    /**
     * Controls how long the werewolf gets knocked back by an attack and the window of the
     * damage animation.
     */

    private float lockoutTime;

    /**
     * Point light pointed on werewolf at all times
     */
    private PointLight spotLight;

    private boolean isAttacking;

    public AttackHitbox attackHitbox;

    private boolean drawCooldownBar;

    private float cooldownPercent;

    /**
     * A float between 0 and 1 indicating attcking power of the Werewolf
     */
    private float attackPower;

    /**
     * A float indicating number of tiles player's attack can reach
     */
    private float attackRange;

    /**
     * Whether the werewolf is in sprint
     */
    private boolean isRunning;

    public Direction direction;

    /**
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    public ObjectType getType() {
        return ObjectType.WEREWOLF;
    }

    /**
     * Returns the current player (left/right) movement input.
     *
     * @return the current player movement input.
     */
    public float getMovementH() {
        return movementH;
    }

    /**
     * Returns the current player (up/down) movement input.
     *
     * @return the current player movement input.
     */
    public float getMovementV() {
        return movementV;
    }

    /**
     * Sets the current player (left/right) movement input.
     *
     * @param value the current player movement input.
     */
    public void setMovementH(float value) {
        movementH = value;
    }

    /**
     * Sets the current player (uo/down) movement input.
     *
     * @param value the current player movement input.
     */
    public void setMovementV(float value) {
        movementV = value;
    }

    /**
     * Returns the current hp of the werewolf.
     */
    public int getHp() {
        return hp;
    }

    /**
     * Sets the current hp of the werewolf.
     *
     * @param value the current hp of the werewolf.
     */
    public void setHp(int value) {
        hp = value;
    }

    public void initHp(int value) {
        INITIAL_HP = value;
    }

    public void initLockout(float value) {
        lockout = value;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    /**
     * Returns the current stealth of the werewolf.
     */
    public float getStealth() {
        return stealth;
    }

    /**
     * Sets the current stealth of the werewolf.
     *
     * @param value the current stealth of the werewolf.
     */
    public void setStealth(float value) {
        stealth = value;
    }

    /**
     * Returns the current light collected of the werewolf.
     */
    public float getLight() {
        return light;
    }

    /**
     * Sets the current light collected of the werewolf.
     *
     * @param value the current light of the werewolf.
     */
    public void setLight(float value) {
        light = value;
    }

    /**
     * Returns the attack power of werewolf during battle phase.
     */
    public float getAttackPower() {
        return attackPower;
    }

    /**
     * Sets the attack power of werewolf during battle phase.
     */
    public void setAttackPower(float pow) {
        attackPower = pow;
    }

    /**
     * Returns the attack power of werewolf during battle phase.
     */
    public float getAttackRange() {
        return attackRange;
    }

    /**
     * Sets the attack power of werewolf during battle phase.
     */
    public void setAttackRange(float range) {
        attackRange = range;
    }

    /** Sets the cooldown bar to be drawn or not */
    public void setDrawCooldownBar(boolean b, float percentage) {
        drawCooldownBar = b;
        cooldownPercent = percentage;
    }

    /** @return whether the cooldown bar should be drawn */
    public boolean drawCooldownBar() {
        return drawCooldownBar;
    }

    /** @return the percentage of the cooldown bar */
    public float getCooldownPercent() {
        return cooldownPercent;
    }

    /**
     * @return Point light on player
     */
    public PointLight getSpotlight() {
        return spotLight;
    }

    /**
     * Attaches light to player as a spotlight (pointed down at player at all times)
     */
    public void setSpotLight(PointLight light) {
        spotLight = light;
        spotLight.attachToBody(getBody(), 0, 0);
        spotLight.setActive(true);
    }

    /**
     * Returns true if the player is on a moonlight tile.
     *
     * @return true if the player is on a moonlight tile.
     */
    public boolean isOnMoonlight() {
        return moonlight;
    }

    public void setOnMoonlight(Boolean b) {
        moonlight = b;
    }

    public void addMoonlightCollected() {
        moonlightCollected++;
    }

    public void reduceMoonlightCollected() {
        moonlightCollected--;
    }

    public int getMoonlightCollected() {
        return moonlightCollected;
    }

    public void setCanMove(boolean value) {
        canMove = value;
    }

    public void setAttacking(boolean value) {
        isAttacking = value;
        attackHitbox.setActive(value);
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    /**
     * Initialize a werewolf.
     */
    public Werewolf() {
        super();
        animeframe = 0.0f;
        lockoutTime = 0.0f;
        moonlight = false;
        light = INITIAL_LIGHT;
        hp = INITIAL_HP;
        stealth = 0.0f;
        moonlightCollected = 0;
        isAttacking = false;
        canMove = true;
        attackPower = INITIAL_POWER;
        attackRange = INITIAL_RANGE;
    }

    /**
     * Initialize the werewolf with the given data
     *
     * @param container LevelContainer which this player is placed in
     */
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);

        JsonValue light = json.get("spotlight");
        float[] color = light.get("color").asFloatArray();
        float dist = light.getFloat("distance");
        int rays = light.getInt("rays");

        int health = json.getInt("health");
        float lockout = json.getFloat("lockout");

        JsonValue speedInfo = json.get("speed");
        walkSpeed = speedInfo.getFloat("walk");
        runSpeed = speedInfo.getFloat("run");

        initHp(health);
        initLockout(lockout);

        PointLight spotLight = new PointLight(
                container.getRayHandler(), rays, Color.WHITE, dist,
                0, 0
        );
        spotLight.setColor(color[0], color[1], color[2], color[3]);
        spotLight.setSoft(light.getBoolean("soft"));
        activatePhysics(container.getWorld());
        createAttackHitbox(container.getWorld());
        setSpotLight(spotLight);

        body.setFixedRotation(true);
    }

    public void createAttackHitbox(World world) {
        attackHitbox = new AttackHitbox(HITBOX_SIZE, this);

        attackHitbox.activatePhysics(world);
        attackHitbox.setActive(false);
    }

    /**
     public void resolveAttack(GameObject enemy, int damage, float knockback) {

        Body enemyBody = enemy.getBody();
        Vector2 pos = body.getPosition();
        Vector2 enemyPos = enemyBody.getPosition();
        Vector2 direction = pos.sub(enemyPos).nor();

        canMove = false;
        body.applyLinearImpulse(direction.scl(knockback), body.getWorldCenter(), true);
        setHp(hp - damage);
    } */

    /**
     * Updates the animation frame and position of this werewolf.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {
        System.out.println(body.getAngularVelocity());
        super.update(delta);
        // get the current velocity of the player's Box2D body
        Vector2 velocity = body.getLinearVelocity();
        if (canMove) {
            float speed = isRunning ? runSpeed : walkSpeed;
            velocity.x = movementH * speed;
            velocity.y = movementV * speed;

            if (movementV < 0) {
                direction = Direction.DOWN;
            } else if (movementV > 0) {
                direction = Direction.UP;
            } else if (movementH < 0) {
                direction = Direction.LEFT;
            } else  if (movementH > 0) {
                direction = Direction.RIGHT;
            }

            // set the updated velocity to the player's Box2D body
            body.setLinearVelocity(velocity);
        } else if (lockoutTime >= lockout) {
            canMove = true;
            lockoutTime = 0f;
        } else {
            lockoutTime += delta;
        }
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
        return new Box2dLocation(this.getPosition());
    }
}