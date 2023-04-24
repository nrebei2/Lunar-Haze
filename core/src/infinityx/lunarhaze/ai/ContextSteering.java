package infinityx.lunarhaze.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

/**
 * {@code ContextSteering} class represents a steering behavior that processes a {@link ContextMap}
 * generated by a {@link ContextBehavior} to determine the best direction and speed to move.
 * The class extends {@link SteeringBehavior} and so can be used as a steering behavior.
 */
public class ContextSteering extends SteeringBehavior<Vector2> {
    /**
     * The {@link ContextBehavior} that provides the {@link ContextMap} for this steering behavior.
     */
    private final ContextBehavior contextBehavior;

    /**
     * Masked slots for filtering the interest map.
     */
    private final boolean[] maskedSlots;

    private final ContextMap cachedContextMap;

    /**
     * Creates a new {@code ContextSteering} instance using the given {@link ContextBehavior}.
     * The constructor initializes the {@code maskedSlots} array and the cached {@link ContextMap}
     * based on the provided resolution.
     *
     * @param owner           The {@link Steerable} object that owns this steering behavior.
     * @param contextBehavior The {@link ContextBehavior} that generates the {@link ContextMap} for this steering behavior.
     * @param resolution      The resolution of the {@link ContextMap}.
     */
    public ContextSteering(Steerable<Vector2> owner, ContextBehavior contextBehavior, int resolution) {
        super(owner);
        this.contextBehavior = contextBehavior;
        this.maskedSlots = new boolean[resolution];
        this.cachedContextMap = new ContextMap(resolution);
    }

    @Override
    public SteeringAcceleration<Vector2> calculateRealSteering(SteeringAcceleration<Vector2> steering) {
        ContextMap contextMap = contextBehavior.calculateMaps(cachedContextMap);
        int resolution = contextMap.getResolution();
        float[] dangerMap = contextMap.dangerMap;
        float[] interestMap = contextMap.interestMap;

        // Find the lowest danger and mask out slots with higher danger
        float lowestDanger = Float.MAX_VALUE;
        Arrays.fill(maskedSlots, false);

        for (int i = 0; i < resolution; i++) {
            float danger = dangerMap[i];
            if (danger < lowestDanger) {
                lowestDanger = danger;
                Arrays.fill(maskedSlots, false);
                maskedSlots[i] = true;
            } else if (danger == lowestDanger) {
                maskedSlots[i] = true;
            }
        }

        // Apply mask to interest map
        for (int i = 0; i < resolution; i++) {
            if (!maskedSlots[i]) {
                interestMap[i] = 0;
            }
        }

        // Find the interest map slot with the highest remaining interest
        int highestInterestSlot = 0;
        float highestInterest = -Float.MAX_VALUE;

        for (int i = 0; i < resolution; i++) {
            float interest = interestMap[i];
            if (interest > highestInterest) {
                highestInterest = interest;
                highestInterestSlot = i;
            }
        }

        // Move in the direction of the highest interest slot
        Vector2 direction = contextMap.dirFromSlot(highestInterestSlot);
        steering.linear.set(direction).scl(highestInterest);
        steering.angular = 0;

        return steering;
    }

    @Override
    public Steerable<Vector2> getOwner() {
        return contextBehavior.getOwner();
    }

    @Override
    public ContextSteering setOwner(Steerable<Vector2> owner) {
        contextBehavior.setOwner(owner);
        return this;
    }

    @Override
    public boolean isEnabled() {
        return contextBehavior.isEnabled();
    }

    @Override
    public SteeringBehavior<Vector2> setEnabled(boolean enabled) {
        contextBehavior.setEnabled(enabled);
        return this;
    }
}

