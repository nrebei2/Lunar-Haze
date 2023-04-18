package infinityx.util;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;

/**
 * Simple implementation of Location
 */
public class Box2dLocation implements Location<Vector2> {
    Vector2 position;
    float orientation;

    public Box2dLocation(Vector2 loc) {
        position = loc;
        orientation = 0;
    }

    public Box2dLocation(Location<Vector2> loc) {
        position = loc.getPosition().cpy();
        orientation = loc.getOrientation();
    }

    public Box2dLocation() {
        this(new Vector2());
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    @Override
    public float getOrientation() {
        return orientation;
    }

    @Override
    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return MathUtil.vectorToAngle(vector);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        return MathUtil.angleToVector(outVector, angle);
    }

    @Override
    public Location<Vector2> newLocation() {
        return new Box2dLocation(new Vector2());
    }
}