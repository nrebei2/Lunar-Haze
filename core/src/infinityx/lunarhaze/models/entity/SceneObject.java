package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.util.Drawable;

/**
 * Represents a foreground object.
 * Buildings, trees, large rocks etc.
 */
public class SceneObject extends GameObject implements Drawable {

    /**
     * Name of object from json
     */
    private final String type;

    /**
     * Whether the texture is flipped horizontally
     */
    private boolean flipped;

    /**
     * Whether you can see past this object
     */
    private boolean seeThru;

    /**
     * Initialize a scene object.
     */
    public SceneObject(float x, float y, String type) {
        super(x, y);
        this.type = type;
        this.flipped = false;
        this.seeThru = false;
    }

    /**
     * Initialize scene object with dummy position
     */
    public SceneObject(String type) {
        this(0, 0, type);
    }

    @Override
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);
        if (json.has("overlook")) {
            seeThru = json.getBoolean("overlook");
        }
    }

    /**
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    @Override
    public ObjectType getType() {
        return ObjectType.SCENE;
    }

    public String getSceneObjectType() {
        return type;
    }

    /**
     * Call after {@link #initialize(AssetDirectory, JsonValue, LevelContainer)}.
     *
     * @param flipped whether the texture should be flipped horizontally
     */
    public void setFlipped(boolean flipped) {
        if (this.flipped == flipped) return;
        this.flipped = flipped;

        // Flip the collider offset
        ShapeCache bodyInfo = getShapeInformation("body");
        if (bodyInfo.shape.getType() == Shape.Type.Polygon && !bodyInfo.offset.isZero())
            resizeBox("body", bodyInfo.width, bodyInfo.height, bodyInfo.offset.scl(-1, 1));
    }

    public boolean isFlipped() {
        return flipped;
    }

    public boolean isSeeThru() {
        return seeThru;
    }

    @Override
    public void draw(GameCanvas canvas) {

        if (!canvas.playerCoords.epsilonEquals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)) {
            // ugly but it is what it is
            float recX = canvas.WorldToScreenX(getPosition().x)
                    - origin.x * (flipped ? -1 : 1) * textureScale * scale
                    - (flipped ? 1 : 0) * getTextureWidth();
            float recY = canvas.WorldToScreenY(getPosition().y) - origin.y * textureScale * scale;
            float width = getTextureWidth();
            float height = getTextureHeight();

            boolean playerInside =
                    recX <= canvas.playerCoords.x
                            && recX + width >= canvas.playerCoords.x
                            && recY <= canvas.playerCoords.y
                            && recY + height >= canvas.playerCoords.y;

            if (playerInside) {
                tint.a = Math.max(0.65f, tint.a - 0.01f);
            } else {
                tint.a = Math.min(1f, tint.a + 0.01f);
            }
        }

        canvas.draw(filmstrip, tint, origin.x, origin.y,
                canvas.WorldToScreenX(getPosition().x), canvas.WorldToScreenY(getPosition().y), 0.0f,
                (flipped ? -1 : 1) * textureScale * scale, textureScale * scale);
    }
}
