package infinityx.lunarhaze.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;

/**
 * The CombinedContext behavior iterates through the behaviors,
 * comparing each slot across multiple maps and taking a maximum.
 */
public class CombinedContext extends ContextBehavior {

    /**
     * The list of context behaviors considered.
     */
    protected Array<ContextBehavior> behaviors = new Array();

    private final FloatArray dangerOutput;
    private final FloatArray interestOutput;

    /**
     * Creates a {@code CombinedContext} behavior for the specified owner.
     *
     * @param owner the owner of this behavior
     */
    public CombinedContext(Steerable<Vector2> owner) {
        super(owner);
        dangerOutput = new FloatArray();
        interestOutput = new FloatArray();
    }


    /**
     * Adds the specified behavior to the considered behaviors.
     *
     * @param behavior the behavior to add
     * @return this behavior for chaining.
     */
    public CombinedContext add(ContextBehavior behavior) {
        behaviors.add(behavior);
        return this;
    }


    @Override
    protected ContextMap calculateRealMaps(ContextMap map) {
        // Set size and zero out
        dangerOutput.setSize(map.getResolution());
        for (int i = 0; i < dangerOutput.size; i++) {
            dangerOutput.set(i, 0);
        }
        interestOutput.setSize(map.getResolution());
        for (int i = 0; i < interestOutput.size; i++) {
            interestOutput.set(i, 0);
        }

        // Get max for each slot
        for (ContextBehavior behavior : behaviors) {
            behavior.calculateRealMaps(map);
            for (int i = 0; i < map.getResolution(); i++) {
                dangerOutput.set(i, Math.max(dangerOutput.get(i), map.dangerMap[i]));
                interestOutput.set(i, Math.max(interestOutput.get(i), map.interestMap[i]));
            }
        }

        // Push into map
        for (int i = 0; i < interestOutput.size; i++) {
            map.interestMap[i] = interestOutput.get(i);
        }
        for (int i = 0; i < dangerOutput.size; i++) {
            map.dangerMap[i] = dangerOutput.get(i);
        }
        return map;
    }

}
