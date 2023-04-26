package infinityx.lunarhaze.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;

/**
 * A behavior that favors perpendicular movement between the owner and target.
 * Allows the configuration of its direction of movement.
 * TODO: strafe distance
 */
public class Strafe extends ContextBehavior {


    public enum Rotation {
        COUNTERCLOCKWISE,
        CLOCKWISE
    }

    /**
     * Location to strafe around
     */
    private Location<Vector2> target;

    /**
     * direction of movement around target
     */
    private Rotation rotation;

    /**
     * Creates a {@code ContextBehavior} for the specified owner.
     * The behavior is enabled and has no explicit limiter, meaning that the owner is used instead.
     *
     * @param owner    the owner of this context behavior
     * @param target   location to strafe around
     * @param rotation direction of movement around target
     */
    public Strafe(Steerable<Vector2> owner, Location<Vector2> target, Rotation rotation) {
        super(owner);
        this.target = target;
        this.rotation = rotation;
    }

    public void changeRotation() {
        if (this.rotation == Rotation.CLOCKWISE) {
            rotation = Rotation.COUNTERCLOCKWISE;
        } else {
            rotation = Rotation.CLOCKWISE;
        }
    }

    /**
     * Calculates the context maps and writes it to the given ContextMap.
     * <p>
     * This method is called by {@link #calculateMaps(ContextMap)} when this behavior is enabled.
     *
     * @param map the context maps to be calculated.
     * @return the calculated ContextMap for chaining.
     */
    @Override
    protected ContextMap calculateRealMaps(ContextMap map) {
        map.setZero();
        Vector2 targetDir = target.getPosition().sub(owner.getPosition()).nor();

        // Prefer directions perpendicular to target
        // Choose perpendicular vector from rotation
        targetDir.rotate90(rotation.ordinal() - 1);

        for (int i = 0; i < map.getResolution(); i++) {
            map.interestMap[i] = Math.max(0, map.dirFromSlot(i).dot(targetDir));
        }
        return map;
    }
}
