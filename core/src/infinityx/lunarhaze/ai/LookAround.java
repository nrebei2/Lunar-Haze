package infinityx.lunarhaze.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.ReachOrientation;
import com.badlogic.gdx.ai.utils.ArithmeticUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import infinityx.util.Box2dLocation;


/**
 * Look around delegates most of its work to ReachOrientation, as it simply makes the entity face in two directions, one after the other.
 */
public class LookAround extends ReachOrientation<Vector2> {

    float target1;
    float target2;

    /**
     * Whether the entity has finished looking around
     */
    private boolean finished;

    /**
     * The total amount (in degrees) the owner will look around for
     */
    private final float angle;

    /**
     * Create a look around behavior for the specified owner
     *
     * @param angle The total amount (in degrees) the owner will look around for
     */
    public LookAround(Steerable<Vector2> owner, float angle) {
        super(owner);
        this.angle = angle;
        reset();
    }

    @Override
    protected SteeringAcceleration<Vector2> calculateRealSteering(SteeringAcceleration<Vector2> steering) {
        super.calculateRealSteering(steering);

        float rotation = ArithmeticUtils.wrapAngleAroundZero(target.getOrientation() - owner.getOrientation());

        // Absolute rotation
        float rotationSize = rotation < 0f ? -rotation : rotation;

        // Check if we are there
        if (rotationSize <= alignTolerance) {
            if (target.getOrientation() == target1) {
                setTargetOrientation(target2);
                finished = false;
            } else {
                finished = true;
            }
        }
        return steering;
    }

    /**
     * Reset attributes for reuse
     */
    public void reset() {
        target1 = owner.getOrientation() - MathUtils.degreesToRadians * angle / 2;
        target2 = owner.getOrientation() + MathUtils.degreesToRadians * angle / 2;
        setTarget(new Box2dLocation());
        setTargetOrientation(target1);
        finished = false;
    }

    public void setTargetOrientation(float orientation) {
        target.setOrientation(orientation);
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public LookAround setAlignTolerance(float alignTolerance) {
        this.alignTolerance = alignTolerance;
        return this;
    }

    @Override
    public LookAround setDecelerationRadius(float decelerationRadius) {
        this.decelerationRadius = decelerationRadius;
        return this;
    }

    @Override
    public LookAround setTimeToTarget(float timeToTarget) {
        this.timeToTarget = timeToTarget;
        return this;
    }
}
