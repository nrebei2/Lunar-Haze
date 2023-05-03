package infinityx.lunarhaze.models.entity;

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

    private String type;

    /**
     * Initialize a scene object.
     */
    public SceneObject(float x, float y, String type) {
        super(x, y);
        this.type = type;
    }

    /**
     * Initialize scene object with dummy position
     */
    public SceneObject(String type) {
        this(0, 0, type);
    }

    /**
     * Initialize scene object given json.
     *
     * @param container LevelContainer which this player is placed in
     */
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);
        activatePhysics(container.getWorld());
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

    @Override
    public void draw(GameCanvas canvas) {
        float recX = canvas.WorldToScreenX(getPosition().x) - origin.x * textureScale * scale;
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
        super.draw(canvas);
    }
}
