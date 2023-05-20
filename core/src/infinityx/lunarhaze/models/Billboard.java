package infinityx.lunarhaze.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.Animation;
import infinityx.lunarhaze.graphics.FilmStrip;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.Drawable;

public class Billboard implements Drawable {

    /**
     * Texture drawn on canvas
     */
    private Animation animation;

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

    /**
     * The tint applied to the texture when drawing
     */
    protected Color tint;

    public Billboard(Vector3 position, float scale) {
        this.position = position;
        this.scale = scale;
        this.tint = new Color(Color.WHITE);
        this.animation = new Animation();
    }

    public void initialize(AssetDirectory directory, JsonValue json) {
        String texture = json.getString("texture");
        if (directory.hasEntry(texture, FilmStrip.class)) {
            animation.addAnimation("main", directory.getEntry(texture, FilmStrip.class), 0.1f);
        } else {
            animation.addStaticAnimation("main", directory.getEntry(texture, Texture.class));
        }
        animation.setCurrentAnimation("main");
        textureScale = json.getFloat("scale");
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

    public void setX(float x) {
        this.position.x = x;
    }
    public void setY(float y) {
        this.position.y = y;
    }
    public void setZ(float z) {
        this.position.z = z;
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

    public float getScale() {
        return scale;
    }

    @Override
    public float getDepth() {
        return position.y;
    }

    @Override
    public void draw(GameCanvas canvas) {
        FilmStrip curFrame = animation.getKeyFrame(Gdx.graphics.getDeltaTime());
        canvas.draw(curFrame, tint, curFrame.getRegionWidth() / 2, curFrame.getRegionHeight() / 2,
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

    public void setTint(Color selectedColor) {
    }
}
