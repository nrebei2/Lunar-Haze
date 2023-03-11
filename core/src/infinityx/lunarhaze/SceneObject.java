package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import infinityx.util.Drawable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Represents a (collection of) foreground tiles.
 * Buildings, trees, large rocks etc.
 * This class is just for drawing, collisions should be handled through Board.
 */
public class SceneObject implements Drawable {

    // TODO: We can switch to FilmStrip later if we want animations
    private Texture ObjectTexture;

    /**
     * World position of bottom-left corner of object
     */
    private final Vector2 position;

    /**
     * Position and texture of object should be known at creation
     */
    public SceneObject(Vector2 position, Texture texture) {
        this.position = position;
        setTexture(texture);
    }

    /**
     * @param texture The texture of the scene object
     */
    public void setTexture(Texture texture) {
        ObjectTexture = texture;
    }

    /**
     * Simple depth buffer, object-wise instead of pixel-wise.
     *
     * @return depth of object from camera/
     * private final
     */
    @Override
    public float getDepth() {
        return position.x;
    }

    @Override
    public void draw(GameCanvas canvas) {
        throw new NotImplementedException();
    }
}
