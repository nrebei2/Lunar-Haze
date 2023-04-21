package infinityx.lunarhaze.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.Drawable;

/**
 * Instance is a single dust particle.
 * Many attributes must be set, including position, (linear/angular) velocity.
 */
public class Dust implements Drawable {
    /**
     * The particle position in world coordinates
     */
    private final Vector2 position;

    /**
     * The particle velocity
     */
    private final Vector2 velocity;

    /**
     * The current rotation of texture, affected by rps
     */
    private float textureRot;

    /**
     * Rotations per second
     */
    private float rps;

    /**
     * Track the current state of this dust particle.
     */
    public enum DustState {
        APPEARING, DECAYING
    }

    private DustState state;

    /**
     * What should the particle do after it finished decaying.
     */
    private enum Condition {
        RESET, CONTINUE, DESTROY
    }

    private Condition condition;


    /**
     * Used for fading in/out
     */
    private float elapsed;

    /**
     * time range for fading in/out
     */
    private float fadeMin;
    private float fadeMax;

    /**
     * The current fade time
     */
    private float fadeTime;

    /**
     * Easing in function, easing out is reversed
     */
    private static final Interpolation EAS_FN = Interpolation.exp5Out;

    /**
     * alpha tint, corresponds to state
     */
    private float alpha;

    /**
     * May be null, texture drawn on canvas
     */
    private Texture texture;

    /**
     * How much the texture of this object should be scaled when drawn
     */
    private float textureScale;

    /**
     * Whether the object should be reset at next timestep.
     */
    private boolean reset = false;

    /**
     * Scale of specific dust particle (on top of texture scale)
     */
    private float scale;

    /**
     * Whether this particle should be destroyed (not drawn) next frame
     */
    private boolean destroyed;

    /**
     * Returns the position of this particle.
     * <p>
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
     * @param x the x-coordinate of the particle position
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
     * @param y the y-coordinate of the particle position
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
        velocity.set((float) (speed * Math.cos(angle)),
                (float) (speed * Math.sin(angle)));
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
     * sets texture scale for drawing
     */
    public void setTextureScale(float scale) {
        this.textureScale = scale;
    }

    /**
     * Set scale which further scales this dust particle for drawing
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Set time range for fading in/out
     *
     * @param min minimum time
     * @param max maximum time
     */
    public void setFadeRange(float min, float max) {
        fadeMin = min;
        fadeMax = max;
    }

    /**
     * Once called this particle will begin decaying (disappearing) then become reset.
     */
    public void beginReset() {
        this.condition = Condition.RESET;
    }

    /**
     * Once called this particle will begin decaying (disappearing) then become destroyed.
     */
    public void beginDestruction() {
        this.condition = Condition.DESTROY;
    }

    /**
     * The dust particle is in the process of being destroyed.
     */
    public boolean inDestruction() {
        return this.condition == Condition.DESTROY;
    }

    /**
     * Returns true if this object should be reset.
     */
    public boolean shouldReset() {
        return reset;
    }

    /**
     * Creates a new (uninitialized) Particle.
     * <p>
     * Many attributes of this particle should be set; use the appropriate setters.
     */
    public Dust() {
        position = new Vector2();
        velocity = new Vector2();
        reset();
    }

    /**
     * Update attributes of this dust particle
     */
    public void update(float delta) {
        if (isDestroyed()) return;

        elapsed += delta;
        switch (state) {
            case APPEARING:
                // progress along fade-in
                float inProg = Math.min(1f, elapsed / fadeTime);
                alpha = EAS_FN.apply(inProg);
                if (inProg == 1f) {
                    state = DustState.DECAYING;
                    this.fadeTime = MathUtils.random(fadeMin, fadeMax);
                    elapsed = 0;
                }
                break;
            case DECAYING:
                // progress along fade-out
                float outProg = Math.min(1f, elapsed / fadeTime);
                alpha = EAS_FN.apply(1 - outProg);
                if (outProg == 1f) {
                    switch (condition) {
                        case RESET:
                            reset = true;
                            break;
                        case DESTROY:
                            setDestroyed();
                            break;
                        case CONTINUE:
                            this.state = DustState.APPEARING;
                            this.fadeTime = MathUtils.random(fadeMin, fadeMax);
                            break;
                    }
                }
                break;
        }
        position.add(velocity.x * delta, velocity.y * delta);
        textureRot += rps * delta;
    }

    /**
     * Resets the object for reuse.
     */
    public void reset() {
        this.state = DustState.APPEARING;
        this.condition = Condition.CONTINUE;
        this.reset = false;
        this.destroyed = false;
        this.textureRot = 0;
        this.alpha = 0;
        this.elapsed = 0;
        this.scale = 1;
        this.fadeTime = MathUtils.random(fadeMin, fadeMax);
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
                canvas.WorldToScreenX(getPosition().x), canvas.WorldToScreenY(getPosition().y), textureRot,
                textureScale * scale, textureScale * scale);
    }

    /**
     * Returns true if this object is destroyed.
     *
     * @return true if this object is destroyed
     */
    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Sets this object as destroyed. Will be removed from drawing next timestep.
     */
    @Override
    public void setDestroyed() {
        destroyed = true;
    }
}
