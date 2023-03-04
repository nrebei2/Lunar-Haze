package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Represents a (collection of) foreground tiles.
 * Buildings, trees, large rocks etc.
 * This class is just for drawing, collisions should be handled through Board.
 */
public class SceneObject {

    // We can switch to FilmStrip later if we want animations
    private Texture ObjectTexture;
/**
	 * Creates a new GameplayController with no active elements.
	 */
    /**
     * Object world position (positioned on bottom left corner of sprite)
     */
    private Vector2 position;

    /**
     *
     * @param texture The texture of the scene object
     */
    public void setTexture(Texture texture) {
        ObjectTexture = texture;
    }

    /**
     * Draws this obstacle to the canvas
     *
     * @param canvas The drawing context
     */
    public void draw(GameCanvas canvas) {
        throw new NotImplementedException();
    }
}
