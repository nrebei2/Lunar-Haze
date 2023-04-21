package infinityx.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Static math helper functions
 */
public final class MathUtil {
    /**
     * Returns the angle in radians pointing along the specified vector. Ranges from PI to -PI/2.
     */
    public static float vectorToAngle(Vector2 vector) {
        return MathUtils.atan2(vector.y, vector.x);
    }

    /**
     * Inverse of {@link #vectorToAngle(Vector2)}.
     */
    public static Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.set(MathUtils.cos(angle), MathUtils.sin(angle));

        return outVector;
    }
}