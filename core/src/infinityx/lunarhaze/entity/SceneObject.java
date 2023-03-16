package infinityx.lunarhaze.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.GameCanvas;
import infinityx.lunarhaze.GameObject;
import infinityx.lunarhaze.LevelContainer;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.util.Drawable;

/**
 * Represents a (collection of) foreground tiles.
 * Buildings, trees, large rocks etc.
 * This class is just for drawing, collisions should be handled through Board.
 */
public class SceneObject extends GameObject implements Drawable {

    /**
     * Initialize a scene object.
     */
    public SceneObject(float x, float y) {
        super(x, y);
    }

    /**
     * Initialize scene object with dummy position
     */
    public SceneObject() {
        this(0, 0);
    }

    /**
     * Initialize scene object given json.
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

    /**
     * Deep clones scene object, can be used independently of this
     * @return new scene object
     */
    public SceneObject deepClone(LevelContainer container) {
        SceneObject object = new SceneObject(getX(), getY());
        object.setTexture(getTexture());
        object.setOrigin((int)origin.x, (int)origin.y);
        object.setBodyState(body);
        object.activatePhysics(container.getWorld());

        object.setDimension(getDimension().x, getDimension().y);
        object.setPositioned(positioned);
        return object;
    }
}
