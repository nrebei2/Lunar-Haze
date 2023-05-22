package infinityx.util;

import com.badlogic.gdx.math.MathUtils;

/**
 * Represents a cardinal direction
 */
public enum Direction {
    LEFT, RIGHT, UP, DOWN;

    public float toAngle() {
        switch (this) {
            case RIGHT:
                return MathUtils.PI;
            case LEFT:
                return 0;
            case DOWN:
                return - MathUtils.PI / 2;
            case UP:
                return MathUtils.PI / 2;
        }
        return 0;
    }
}
