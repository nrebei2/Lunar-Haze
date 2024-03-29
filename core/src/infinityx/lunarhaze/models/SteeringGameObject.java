package infinityx.lunarhaze.models;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.AngleUtils;


/**
 * Extension of GameObject implementing Steerable, allowing behaviors to work on this object.
 * Since only enemy uses this class, we extend {@link AttackingGameObject} as well.
 */
public abstract class SteeringGameObject extends AttackingGameObject implements Steerable<Vector2> {
    /**
     * tag bit
     */
    boolean tagged;

    /**
     * Caps the velocity of this object
     */
    float maxLinearSpeed, maxAngularSpeed;

    /**
     * Steering behaviors will use these as a limiter
     */
    float maxLinearAcceleration, maxAngularAcceleration;

    /**
     * If independent facing is on (true), the object's rotation is independent of its linear velocity.
     * Otherwise (false), its rotation always points towards the linear velocity.
     */
    boolean independentFacing;

    /**
     * The current behavior that defines the movement of this object.
     */
    public SteeringBehavior<Vector2> steeringBehavior;

    /**
     * Clamped linear output from steering behavior
     */
    protected Vector2 clampedLinear;

    /**
     * Cache for output from steeringBehavior
     */
    private static final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<>(new Vector2());

    public SteeringGameObject(boolean independentFacing) {
        super();
        setIndependentFacing(independentFacing);
        clampedLinear = new Vector2();
    }

    public boolean isIndependentFacing() {
        return independentFacing;
    }

    public void setIndependentFacing(boolean independentFacing) {
        this.independentFacing = independentFacing;
    }

    public void setSteeringBehavior(SteeringBehavior<Vector2> steeringBehavior) {
        this.steeringBehavior = steeringBehavior;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (steeringBehavior != null) {
            applySteering(delta);
        }
    }

    /**
     * Align the vectors direction with the nearest cardinal direction (up, down, left, or right),
     * but only if the original direction deviates from the cardinal direction by more than a specified angle.
     *
     * @param vector        The vector whose direction is to be clamped. This vector is modified in place.
     * @param epsilonDegree The maximum allowable deviation from a cardinal direction, in degrees.
     */
    public Vector2 clampToCardinal(Vector2 vector, float epsilonDegree) {
        clampedLinear.set(vector);
        float angle = clampedLinear.angleDeg();
        float nearest = 0f;
        float minDiff = 180f;

        for (int cardinal = 0; cardinal < 360; cardinal += 90) {
            float diff = Math.abs(cardinal - angle);
            diff = diff > 180f ? 360f - diff : diff;

            if (diff < minDiff) {
                minDiff = diff;
                nearest = cardinal;
            }
        }

        if (minDiff > epsilonDegree) {
            if (angle - nearest > 0 && 180 > angle - nearest) {
                clampedLinear.setAngleDeg(nearest + epsilonDegree);
            } else {
                clampedLinear.setAngleDeg(nearest - epsilonDegree);
            }
        }

        return clampedLinear;
    }


    /**
     * Calculates and applies the appropriate linear and angular acceleration using the current steering behavior.
     * Adapted from https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/box2d/Box2dSteeringEntity.java.
     */
    protected void applySteering(float deltaTime) {
        steeringBehavior.calculateSteering(steeringOutput);

        boolean anyAccelerations = false;
        // Update position and linear velocity.
        if (!steeringOutput.linear.isZero()) {
            clampToCardinal(steeringOutput.linear, 25);

            body.setLinearVelocity(clampedLinear.x, clampedLinear.y);
            anyAccelerations = true;
        }

        // Update orientation and angular velocity
        if (isIndependentFacing()) {
            if (steeringOutput.angular != 0) {
                // this method internally scales the torque by deltaTime
                body.applyTorque(steeringOutput.angular, true);
                anyAccelerations = true;
            }
        } else {
            // Use the un-clamped output to set the orientation
            // This is so the flashlights rotate smoothly
            Vector2 linVel = steeringOutput.linear;
            if (!linVel.isZero(getZeroLinearSpeedThreshold())) {
                float newOrientation = vectorToAngle(linVel);
                body.setTransform(body.getPosition(), newOrientation);
            }
        }

        if (anyAccelerations) {
            // Cap the linear speed
            Vector2 velocity = body.getLinearVelocity();
            float currentSpeedSquare = velocity.len2();
            float maxLinearSpeed = getMaxLinearSpeed();
            if (currentSpeedSquare > maxLinearSpeed * maxLinearSpeed) {
                body.setLinearVelocity(velocity.scl(maxLinearSpeed / (float) Math.sqrt(currentSpeedSquare)));
            }

            // Cap the angular speed
            float maxAngVelocity = getMaxAngularSpeed();
            if (body.getAngularVelocity() > maxAngVelocity) {
                body.setAngularVelocity(maxAngVelocity);
            }
        }
    }

    /**
     * Returns {@code true} if this Steerable is tagged; {@code false} otherwise.
     */
    @Override
    public boolean isTagged() {
        return tagged;
    }

    /**
     * Tag/untag this Steerable. This is a generic flag utilized in a variety of ways.
     *
     * @param tagged the boolean value to set
     */
    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    /**
     * Returns the threshold below which the linear speed can be considered zero. It must be a small positive value near to zero.
     * Usually it is used to avoid updating the orientation when the velocity vector has a negligible length.
     */
    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0.001f;
    }

    /**
     * Sets the threshold below which the linear speed can be considered zero. It must be a small positive value near to zero.
     * Usually it is used to avoid updating the orientation when the velocity vector has a negligible length.
     *
     * @param value
     */
    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the maximum linear speed.
     */
    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    /**
     * Sets the maximum linear speed.
     *
     * @param maxLinearSpeed
     */
    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    /**
     * Returns the maximum linear acceleration.
     */
    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }

    /**
     * Sets the maximum linear acceleration.
     *
     * @param maxLinearAcceleration
     */
    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    /**
     * Returns the maximum angular speed.
     */
    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    /**
     * Sets the maximum angular speed.
     *
     * @param maxAngularSpeed
     */
    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    /**
     * Returns the maximum angular acceleration.
     */
    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    /**
     * Sets the maximum angular acceleration.
     *
     * @param maxAngularAcceleration
     */
    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcceleration = maxAngularAcceleration;
    }

    /**
     * DEBUG, draws the clamped steering output in PINK, and the un-clamped output in ORANGE
     */
    public void drawSteeringOutput(GameCanvas canvas) {
        if (steeringBehavior == null) return;
        steeringBehavior.calculateSteering(steeringOutput);
        clampToCardinal(steeringOutput.linear, 25);

        canvas.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        canvas.shapeRenderer.setColor(Color.ORANGE);
        canvas.shapeRenderer.rectLine(getPosition().cpy(), getPosition().cpy().add(steeringOutput.linear), 0.1f);
        canvas.shapeRenderer.setColor(Color.PINK);
        canvas.shapeRenderer.rectLine(getPosition().cpy(), getPosition().cpy().add(clampedLinear), 0.1f);
        canvas.shapeRenderer.end();
    }

    /**
     * Returns the float value indicating the orientation of this location. The orientation is the angle in radians representing
     * the direction that this location is facing.
     */
    @Override
    public float getOrientation() {
        float angle = body.getAngle();
        angle %= (2 * Math.PI);
        if (angle > Math.PI) {
            angle -= 2 * Math.PI;
        } else if (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    /**
     * Sets the orientation of this location, i.e. the angle in radians representing the direction that this location is facing.
     *
     * @param orientation the orientation in radians
     */
    @Override
    public void setOrientation(float orientation) {
        body.setTransform(getPosition(), orientation);
    }

    /**
     * Returns the angle in radians pointing along the specified vector.
     *
     * @param vector the vector
     */
    @Override
    public float vectorToAngle(Vector2 vector) {
        return AngleUtils.vectorToAngle(vector);
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
        return AngleUtils.angleToVector(outVector, angle);
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
        throw new UnsupportedOperationException();
    }
}
