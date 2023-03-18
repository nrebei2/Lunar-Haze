package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import infinityx.util.Drawable;

public class Dust implements Drawable {
    /** The particle position in world coordinates */
    private Vector2 position;

    /** The particle velocity */
    private Vector2 velocity;

    /** The current rotation of texture, affected by rps */
    private float textureRot = 0;

    /** Rotations per second */
    private float rps;

    /**
     * Track the current state of this dust particle.
     */
    public enum DustState {
        APPEARING, ALIVE, DECAYING
    }
    private DustState state;

    private float elapsed = 0;
    private static final float FADE_TIME = 2;

    /**
     * Easing in function, easing out is reversed
     */
    private static final Interpolation EAS_FN = Interpolation.exp5Out;

    /**
     * alpha tint, corresponds to state
     */
    private float alpha = 0;

    private Texture texture;

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
     * Sets the angle (radians) of velocity for this particle.
     *
     * @param angle the angle of this particle (radians)
     * @param speed the speed
     */
    public void setVelocity(float angle, float speed) {
        velocity.set((float)(speed*Math.cos(angle)),
                (float)(speed*Math.sin(angle)));
    }

    /**
     * Sets the rotations per second of this particle
     */
    public void setRPS(float rps) {
       this.rps = rps;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
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
    public void update(float delta) {
        switch (state) {
            case APPEARING:
                elapsed += delta;
                // progress along fade-in
                float inProg = Math.min(1f, elapsed / FADE_TIME);
                alpha = EAS_FN.apply(inProg);
                if (inProg == 1f) {
                    state = DustState.ALIVE;
                    elapsed = 0;
                }
                break;
            case DECAYING:
                elapsed += delta;
                // progress along fade-out
                float outProg = Math.min(1f, elapsed / FADE_TIME);
                alpha = EAS_FN.apply(1 - outProg);
                if (outProg == 1f) {
                    this.reset();
                }
        }
        position.add(velocity.scl(delta));
        textureRot += rps * delta;
    }

    /**
     * Resets the object for reuse. Object references should be nulled and fields may be set to default values.
     */
    public void reset() {
        position.set(0,0);
        velocity.set(0,0);
    }

    /**
     * Simple depth buffer, object-wise instead of pixel-wise.
     *
     * @return depth of object from camera
     */
    @Override
    public float getDepth() {
       return getY();
    }

    /**
     * Draws this object to the given canvas
     *
     * @param canvas The drawing context
     */
    @Override
    public void draw(GameCanvas canvas) {
            canvas.draw(texture, alpha, texture.getWidth() / 2, texture.getHeight() / 2,
                    canvas.WorldToScreenX(getPosition().x), canvas.WorldToScreenY(getPosition().y), 0.0f,
                    1, 1);
    }
}
