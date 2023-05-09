package infinityx.lunarhaze.models.entity;

import box2dLight.PointLight;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.controllers.InputController;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.AttackingGameObject;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.util.AngleUtils;
import infinityx.util.Box2dLocation;
import infinityx.util.Direction;

/**
 * Model class representing the player.
 */
public class Werewolf extends AttackingGameObject implements Location<Vector2> {
    /**
     * Move speed (walking)
     **/
    public float walkSpeed;

    public float windupSpeed;

    public boolean isOnMoonlight;

    /**
     * Number of moonlight tiles collected
     **/
    private int moonlightCollected;

    /**
     * Stealth value of the werewolf.
     * The value is a float between 0 and 1.
     */
    private float stealth;

    /**
     * Target stealth value {@link #stealth} is moving towards
     */
    private float target;

    /**
     * Point light pointed on werewolf at all times
     */
    private PointLight spotLight;

    /**
     * Whether the werewolf is in sprint
     */
    private boolean isRunning;

    /**
     * The direction the werewolf is facing
     */
    public Direction direction;

    /**
     * Whether the werewolf is locked out from heavy attacks
     */
    public boolean heavyLockedOut;

    /**
     * For use with {@link #heavyLockedOut}
     */
    public float heavyLockoutTime;

    public boolean isWindingUp;

    /** Whether the player is in tall grass */
    public boolean inTallGrass;

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

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public boolean isMoving() {
        return body.getLinearVelocity().x != 0 || body.getLinearVelocity().y != 0;
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

    public void addMoonlightCollected() {
        moonlightCollected++;
    }

    public void reduceMoonlightCollected() {
        moonlightCollected--;
    }

    public int getMoonlightCollected() {
        return moonlightCollected;
    }


    /**
     * Initialize a werewolf.
     */
    public Werewolf() {
        super();
        stealth = 0.0f;
        moonlightCollected = 0;
        heavyLockedOut = false;
        isWindingUp = false;
        heavyLockoutTime = 0.4f; // this can be changed later
        inTallGrass = false;
        direction = Direction.RIGHT;
    }

    /**
     * Begin heavy lock out for this werewolf. Should be called when attacked.
     */
    public void setHeavyLockedOut() {
        heavyLockedOut = true;
        heavyLockoutTime = 0.4f;
    }

    public void setWindingUp(boolean b) {
        isWindingUp = b;
    }

    public boolean isHeavyLockedOut() {
        return heavyLockedOut;
    }

    public float getTargetStealth() {
        return target;
    }

    public void setTargetStealth(float t) {
        target = t;
    }


    /**
     * @return The radius of the werewolf's noise in world length
     */
    public float getNoiseRadius() {
        return Interpolation.linear.apply(1.75f, 3.5f, stealth);
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


        JsonValue speedInfo = json.get("speed");
        walkSpeed = speedInfo.getFloat("walk");
        windupSpeed = walkSpeed / 3f;
        setFixedRotation(true);

        PointLight spotLight = new PointLight(
                container.getRayHandler(), rays, Color.WHITE, dist,
                0, 0
        );
        spotLight.setColor(color[0], color[1], color[2], color[3]);
        spotLight.setSoft(light.getBoolean("soft"));
        activatePhysics(container.getWorld());
        setSpotLight(spotLight);
    }

    /**
     * Updates the animation frame and position of this werewolf.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {
        super.update(delta);
        canMove = canMove && !heavyLockedOut;

        if (heavyLockedOut) {
            // TODO: Change frame to show that the werewolf is locked out? If needed
            heavyLockoutTime -= delta;
            if (heavyLockoutTime <= 0)
                heavyLockedOut = false;
        }

        // get the current velocity of the player's Box2D body
        if (canMove) {
            Vector2 velocity = body.getLinearVelocity();
            float movementH = InputController.getInstance().getHorizontal();
            float movementV = InputController.getInstance().getVertical();

            float speed = isWindingUp ? windupSpeed : walkSpeed;
            velocity.x = movementH * speed;
            velocity.y = movementV * speed;

            // Set the direction given velocity
            // For diagonal movement we prefer using UP or DOWN
            if (movementV < 0) {
                direction = Direction.DOWN;
            } else if (movementV > 0) {
                direction = Direction.UP;
            } else if (movementH < 0) {
                direction = Direction.LEFT;
            } else if (movementH > 0) {
                direction = Direction.RIGHT;
            }

            if (!velocity.isZero())
                setAngle(AngleUtils.vectorToAngle(velocity));

            // set the updated velocity to the player's Box2D body
            body.setLinearVelocity(velocity);
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        canvas.playerCoords.set(
                canvas.WorldToScreenX(getPosition().x),
                canvas.WorldToScreenY(getPosition().y) + getTextureHeight() * 0.6f
        );
    }

    // Location interface methods

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
        return AngleUtils.vectorToAngle(vector);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        return AngleUtils.angleToVector(outVector, angle);
    }

    @Override
    public Location<Vector2> newLocation() {
        return new Box2dLocation(this.getPosition());
    }
}