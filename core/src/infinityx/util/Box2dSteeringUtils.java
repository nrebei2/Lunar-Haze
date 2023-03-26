package infinityx.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public final class Box2dSteeringUtils {

    public static float vectorToAngle(Vector2 vector) {
        return MathUtils.atan2(vector.y, vector.x);
    }

    public static Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.set(MathUtils.cos(angle), MathUtils.sin(angle));

        return outVector;
    }
}