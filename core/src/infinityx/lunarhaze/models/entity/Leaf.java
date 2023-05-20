package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import infinityx.lunarhaze.graphics.Animation;
import infinityx.lunarhaze.graphics.FilmStrip;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.Dust;
import infinityx.lunarhaze.models.GameObject;
import infinityx.util.Drawable;

public class Leaf implements Drawable {

    // Max in a scene ever
    public static int MAX = 100;

    /**
     * The particle position in world coordinates
     */
    private final Vector3 position;

    /**
     * The particle velocity
     */
    private final Vector3 velocity;

    /**
     * Track the current state.
     */
    public enum State {
        APPEARING, ALIVE, DECAYING
    }

    private State state;


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
    private Animation filmStrip;

    /**
     * How much the texture of this object should be scaled when drawn
     */
    private float textureScale;

    /**
     * Scale of specific dust particle (on top of texture scale)
     */
    private float scale;

    /**
     * Whether this particle should be destroyed (not drawn) next frame
     */
    private boolean destroyed;

    /** Whether this should be updating */
    public boolean active;

    /**
     * Returns the position of this particle.
     * <p>
     * The object returned is a reference to the position vector.  Therefore,
     * changes to this vector are reflected in the particle animation.
     *
     * @return the position of this particle
     */
    public Vector3 getPosition() {
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
     * Returns the z-coordinate of the particle position.
     *
     * @return the z-coordinate of the particle position
     */
    public float getZ() {
                      return position.z;
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
     * Sets the z-coordinate of the particle position.
     *
     * @param z the z-coordinate of the particle position
     */
    public void setZ(float z) {
                            position.z = z;
                                           }

    /**
     * Set the velocity of this particle
     */
    public Vector3 setVelocity(float x, float y, float z) {
                                                        return velocity.set(x, y, z);
                                                                                     }

    public void setTexture(Animation texture) { this.filmStrip = texture; }

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
     * Once called this particle will begin decaying (disappearing) then become destroyed.
     */
    public void beginDestruction() {
        state = State.DECAYING;
        elapsed = 0;
        fadeTime = MathUtils.random(fadeMin / 3, fadeMax / 3);
    }

    /**
     * Creates a new (uninitialized) Particle.
     * <p>
     * Many attributes of this particle should be set; use the appropriate setters.
     */
    public Leaf() {
        position = new Vector3();
        velocity = new Vector3();
        reset();
    }


    /**
     * Update attributes of this dust particle
     */
    public void update(float delta) {
        if (isDestroyed() || !active) return;

        elapsed += delta;
        switch (state) {
            case APPEARING:
                // progress along fade-in
                float inProg = Math.min(1f, elapsed / fadeTime);
                alpha = EAS_FN.apply(inProg);
                if (inProg == 1f) {
                    state = State.ALIVE;
                    this.fadeTime = MathUtils.random(fadeMin, fadeMax) * 5;
                    elapsed = 0;
                }
                break;
            case DECAYING:
                // progress along fade-out
                float outProg = Math.min(1f, elapsed / fadeTime);
                alpha = EAS_FN.apply(1 - outProg);
                if (outProg == 1f) {
                    setDestroyed(true);
                    active = false;
                }
                break;
        }
        position.add(velocity.x * delta, velocity.y * delta, velocity.z * delta);
        if (position.z < 0) {
            position.z = velocity.x = velocity.y = velocity.z = 0;
            if (state != State.DECAYING) {
                elapsed = 0;
                state = State.DECAYING;
            }
        }
    }

    /**
     * Resets the object for reuse.
     */
    public void reset() {
        this.state = State.APPEARING;
        setDestroyed(false);
        this.alpha = 0;
        this.elapsed = 0;
        this.scale = 1;
        this.active = false;
        this.fadeTime = MathUtils.random(fadeMin, fadeMax);
    }

    Vector2 pos = new Vector2();

    @Override
    public Vector2 getPos() {
        return pos.set(getPosition().x, getPosition().y + getPosition().z * 3 / 4);
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
        FilmStrip cur = filmStrip.getKeyFrame(state == State.DECAYING ? 0 : Gdx.graphics.getDeltaTime());
        canvas.draw(cur, alpha, cur.getRegionWidth() / 2, cur.getRegionHeight() / 2,
                canvas.WorldToScreenX(getPosition().x), canvas.WorldToScreenY(getPosition().y + getPosition().z), 0,
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

    @Override
    public void setDestroyed(boolean destroyed) {
                                              this.destroyed = destroyed;
                                                                         }
}
