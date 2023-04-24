package infinityx.lunarhaze.ai;

import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector2;

/**
 * An augmentation on {@link SteeringBehavior} that deals with context maps.
 * See http://www.gameaipro.com/GameAIPro2/GameAIPro2_Chapter18_Context_context_Behavior-Driven_context_at_the_Macro_Scale.pdf
 */
public abstract class ContextBehavior {

    /**
     * The owner of this context behavior
     */
    protected Steerable<Vector2> owner;

    /**
     * The limiter of this context behavior
     */
    protected Limiter limiter;

    /**
     * A flag indicating whether this context behavior is enabled or not.
     */
    protected boolean enabled;

    /**
     * Creates a {@code ContextBehavior} for the specified owner.
     * The behavior is enabled and has no explicit limiter, meaning that the owner is used instead.
     *
     * @param owner the owner of this context behavior
     */
    public ContextBehavior(Steerable<Vector2> owner) {
        this(owner, null, true);
    }

    /**
     * Creates a {@code ContextBehavior} for the specified owner and activation flag. The behavior has no explicit limiter,
     * meaning that the owner is used instead.
     * The default resolution is 8, for each diagonal and cardinal direction.
     *
     * @param owner   the owner of this context behavior
     * @param enabled a flag indicating whether this context behavior is enabled or not
     */
    public ContextBehavior(Steerable<Vector2> owner, boolean enabled) {
        this(owner, null, enabled);
    }

    /**
     * Creates a {@code ContextBehavior} for the specified owner, limiter, activation flag, and resolution.
     *
     * @param owner   the owner of this context behavior
     * @param limiter the limiter of this context behavior
     * @param enabled a flag indicating whether this context behavior is enabled or not
     */
    public ContextBehavior(Steerable<Vector2> owner, Limiter limiter, boolean enabled) {
        this.owner = owner;
        this.limiter = limiter;
        this.enabled = enabled;
    }

    /**
     * If this behavior is enabled calculates the context maps and writes it to the given ContextMap.
     * If it is disabled the maps are zeroed.
     *
     * @param map the context maps to be calculated.
     * @return the calculated ContextMap for chaining.
     */
    public ContextMap calculateMaps(ContextMap map) {
        return isEnabled() ? calculateRealMaps(map) : map.setZero();
    }

    /**
     * Calculates the context maps and writes it to the given ContextMap.
     * <p>
     * This method is called by {@link #calculateMaps(ContextMap)} when this behavior is enabled.
     *
     * @param map the context maps to be calculated.
     * @return the calculated ContextMap for chaining.
     */
    protected abstract ContextMap calculateRealMaps(ContextMap map);


    /**
     * Returns the owner of this context behavior.
     */
    public Steerable<Vector2> getOwner() {
        return owner;
    }

    /**
     * Sets the owner of this context behavior.
     *
     * @return this behavior for chaining.
     */
    public ContextBehavior setOwner(Steerable<Vector2> owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Returns the limiter of this context behavior.
     */
    public Limiter getLimiter() {
        return limiter;
    }

    /**
     * Sets the limiter of this context behavior.
     *
     * @return this behavior for chaining.
     */
    public ContextBehavior setLimiter(Limiter limiter) {
        this.limiter = limiter;
        return this;
    }

    /**
     * Returns true if this context behavior is enabled; false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets this context behavior on/off.
     *
     * @return this behavior for chaining.
     */
    public ContextBehavior setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Returns the actual limiter of this context behavior.
     */
    protected Limiter getActualLimiter() {
        return limiter == null ? owner : limiter;

    }

}