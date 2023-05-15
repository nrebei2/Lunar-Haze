package infinityx.lunarhaze.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.Drawable;

public class Billboard implements Drawable {

    /**
     * Texture drawn on canvas
     */
    private Texture texture;

    /**
     * The billboard position in world coordinates
     */
    private Vector3 position;

    /**
     * Whether this billboard should be destroyed (not drawn) next frame
     */
    private boolean destroyed;

    /**
     * How much the texture of this object should be scaled when drawn
     */
    private float textureScale;

    /**
     * Scale of specific billboard (on top of texture scale)
     */
    private float scale;

    public Billboard(Texture texture, Vector3 position) {
        this.texture = texture;
        this.position = position;
    }

    public Vector3 getPosition() {
        return position;
    }

    /**
     * Set the position to the given components
     * @param x x-position in world
     * @param y y-position in world
     * @param z z-position in world
     */
    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    /**
     * sets texture scale for drawing
     */
    public void setTextureScale(float scale) {
        this.textureScale = scale;
    }

    /**
     * Set scale which further scales this billboard for drawing
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public float getDepth() {
        return position.y;
    }

    @Override
    public void draw(GameCanvas canvas) {
        canvas.draw(texture, 0, texture.getWidth() / 2, texture.getHeight() / 2,
                canvas.WorldToScreenX(getPosition().x), canvas.WorldToScreenY(getPosition().y + getPosition().z), 0,
                textureScale * scale, textureScale * scale);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
}
