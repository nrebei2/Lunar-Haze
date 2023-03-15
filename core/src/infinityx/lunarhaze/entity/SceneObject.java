package infinityx.lunarhaze.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.GameCanvas;
import infinityx.lunarhaze.GameObject;
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
}
