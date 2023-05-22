package infinityx.lunarhaze.ai;

import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.utils.Path;
import com.badlogic.gdx.ai.steer.utils.Path.PathParam;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;

/**
 * {@link com.badlogic.gdx.ai.steer.behaviors.FollowPath} but assumes velocity.
 *
 * @param <P> Type of path parameter implementing the {@link PathParam} interface
 */
public class FollowPath<P extends PathParam> extends SteeringBehavior<Vector2> {

    Location<Vector2> target;

    /**
     * The path to follow
     */
    protected Path<Vector2, P> path;

    /**
     * The distance along the path to generate the target. Can be negative if the owner has to move along the reverse direction.
     */
    protected float pathOffset;

    /**
     * The current position on the path
     */
    protected P pathParam;

    /**
     * The flag indicating whether to use {@link Arrive} behavior to approach the end of an open path. It defaults to {@code true}.
     */
    protected boolean arriveEnabled;

    /**
     * The time in the future to predict the owner's position. Set it to 0 for non-predictive path following.
     */
    protected float predictionTime;

    private Vector2 internalTargetPosition;

    /**
     * Creates a non-predictive {@code FollowPath} behavior for the specified owner and path.
     *
     * @param owner the owner of this behavior
     * @param path  the path to be followed by the owner.
     */
    public FollowPath(Steerable<Vector2> owner, Path<Vector2, P> path) {
        this(owner, path, 0);
    }

    /**
     * Creates a non-predictive {@code FollowPath} behavior for the specified owner, path and path offset.
     *
     * @param owner      the owner of this behavior
     * @param path       the path to be followed by the owner
     * @param pathOffset the distance along the path to generate the target. Can be negative if the owner is to move along the
     *                   reverse direction.
     */
    public FollowPath(Steerable<Vector2> owner, Path<Vector2, P> path, float pathOffset) {
        this(owner, path, pathOffset, 0);
    }

    /**
     * Creates a {@code FollowPath} behavior for the specified owner, path, path offset, maximum linear acceleration and prediction
     * time.
     *
     * @param owner          the owner of this behavior
     * @param path           the path to be followed by the owner
     * @param pathOffset     the distance along the path to generate the target. Can be negative if the owner is to move along the
     *                       reverse direction.
     * @param predictionTime the time in the future to predict the owner's position. Can be 0 for non-predictive path following.
     */
    public FollowPath(Steerable<Vector2> owner, Path<Vector2, P> path, float pathOffset, float predictionTime) {
        super(owner);
        this.path = path;
        this.pathParam = path.createParam();
        this.pathOffset = pathOffset;
        this.predictionTime = predictionTime;

        this.arriveEnabled = true;
        this.internalTargetPosition = newVector(owner);
    }

    @Override
    protected SteeringAcceleration<Vector2> calculateRealSteering(SteeringAcceleration<Vector2> steering) {
        if (path == null) return steering.setZero();

        // Predictive or non-predictive behavior?
        Vector2 location = (predictionTime == 0) ?
                // Use the current position of the owner
                owner.getPosition()
                :
                // Calculate the predicted future position of the owner. We're reusing steering.linear here.
                steering.linear.set(owner.getPosition()).mulAdd(owner.getLinearVelocity(), predictionTime);

        // Find the distance from the start of the path
        float distance = path.calculateDistance(location, pathParam);

        // Offset it
        float targetDistance = distance + pathOffset;

        // Calculate the target position
        path.calculateTargetPosition(internalTargetPosition, pathParam, targetDistance);

        // Seek the target position
        steering.linear.set(internalTargetPosition).sub(owner.getPosition()).nor()
                .scl(getActualLimiter().getMaxLinearSpeed());

        // No angular acceleration
        steering.angular = 0;

        // Output steering acceleration
        return steering;
    }

    /**
     * Returns the path to follow
     */
    public Path<Vector2, P> getPath() {
        return path;
    }

    /**
     * Sets the path followed by this behavior.
     *
     * @param path the path to set
     * @return this behavior for chaining.
     */
    public FollowPath<P> setPath(Path<Vector2, P> path) {
        this.path = path;
        return this;
    }

    /**
     * Returns the path offset.
     */
    public float getPathOffset() {
        return pathOffset;
    }

    /**
     * Returns the flag indicating whether to use {@link Arrive} behavior to approach the end of an open path.
     */
    public boolean isArriveEnabled() {
        return arriveEnabled;
    }

    /**
     * Returns the prediction time.
     */
    public float getPredictionTime() {
        return predictionTime;
    }

    /**
     * Sets the prediction time. Set it to 0 for non-predictive path following.
     *
     * @param predictionTime the predictionTime to set
     * @return this behavior for chaining.
     */
    public FollowPath<P> setPredictionTime(float predictionTime) {
        this.predictionTime = predictionTime;
        return this;
    }

    /**
     * Sets the flag indicating whether to use {@link Arrive} behavior to approach the end of an open path. It defaults to
     * {@code true}.
     *
     * @param arriveEnabled the flag value to set
     * @return this behavior for chaining.
     */
    public FollowPath<P> setArriveEnabled(boolean arriveEnabled) {
        this.arriveEnabled = arriveEnabled;
        return this;
    }

    /**
     * Sets the path offset to generate the target. Can be negative if the owner has to move along the reverse direction.
     *
     * @param pathOffset the pathOffset to set
     * @return this behavior for chaining.
     */
    public FollowPath<P> setPathOffset(float pathOffset) {
        this.pathOffset = pathOffset;
        return this;
    }

    /**
     * Returns the current path parameter.
     */
    public P getPathParam() {
        return pathParam;
    }

    /**
     * Returns the current position of the internal target. This method is useful for debug purpose.
     */
    public Vector2 getInternalTargetPosition() {
        return internalTargetPosition;
    }

    //
    // Setters overridden in order to fix the correct return type for chaining
    //

    @Override
    public FollowPath<P> setOwner(Steerable<Vector2> owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public FollowPath<P> setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear speed and
     * acceleration. However the maximum linear speed is not required for a closed path.
     *
     * @return this behavior for chaining.
     */
    @Override
    public FollowPath<P> setLimiter(Limiter limiter) {
        this.limiter = limiter;
        return this;
    }

    public FollowPath<P> setTarget(Location<Vector2> target) {
        this.target = target;
        return this;
    }
}
