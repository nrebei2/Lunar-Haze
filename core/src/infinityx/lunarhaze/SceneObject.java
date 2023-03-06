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
public class SceneObject extends Drawable {

    // TODO: We can switch to FilmStrip later if we want animations
    private Texture ObjectTexture;

    /**
     *
     * @param texture The texture of the scene object
     */
    public void setTexture(Texture texture) {
        ObjectTexture = texture;
    }

    @Override
    public void draw(GameCanvas canvas) {
        throw new NotImplementedException();
    }
}
