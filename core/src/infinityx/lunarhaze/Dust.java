package infinityx.lunarhaze;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Dust implements Pool.Poolable {
    /** How many pixels the particle moves per animation frame */
    public static final float PARTICLE_SPEED = 2.0f;

    /** The particle position */
    private Vector2 position;

    /** The particle velocity (not directly accessible) */
    private Vector2 velocity;

    /** The particle angle of movement (according to initial position) */
    private float angle;

    /**
     * Returns the position of this particle.
     *
     * The object returned is a reference to the position vector.  Therefore,
     * changes to this vector are reflected in the particle animation.
     *
     * @return the position of this particle
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * Returns the x-coordinate of the particle position.
     *
     * @return the x-coordinate of the particle position
     */
    public float getX() {
        return position.x;
    }

    /**
     * Sets the x-coordinate of the particle position.
     *
     * @param x  the x-coordinate of the particle position
     */
    public void setX(float x) {
        position.x = x;
    }

    /**
     * Returns the y-coordinate of the particle position.
     *
     * @return the y-coordinate of the particle position
     */
    public float getY() {
        return position.y;
    }

    /**
     * Sets the y-coordinate of the particle position.
     *
     * @param y  the y-coordinate of the particle position
     */
    public void setY(float y) {
        position.y = y;
    }

    /**
     * Returns the angle of this particle
     *
     * The particle velocity is (PARTICLE_SPEED,angle) in polar-coordinates.
     *
     * @return the angle of this particle
     */
    public float getAngle() {
        return angle;
    }

    /**
     * Sets the angle of this particle
     *
     * When the angle is set, the particle will change its velocity
     * to (PARTICLE_SPEED,angle) in polar-coordinates.
     *
     * @param angle  the angle of this particle
     */
    public void setAngle(float angle) {
        this.angle = angle;
        velocity.set((float)(PARTICLE_SPEED*Math.cos(angle)),
                (float)(PARTICLE_SPEED*Math.sin(angle)));
    }

    /**
     * Creates a new (unitialized) Particle.
     *
     * The position and velocity are initially 0.  To initialize
     * the particle, use the appropriate setters.
     */
    public Dust() {
        position = new Vector2();
        velocity = new Vector2();
    }

    /**
     * Move the particle one frame, adding the velocity to the position.
     */
    public void update() {
        position.add(velocity);
    }

    /**
     * Resets the object for reuse. Object references should be nulled and fields may be set to default values.
     */
    @Override
    public void reset() {
        position.set(0,0);
        velocity.set(0,0);
        angle = 0;
    }
}
