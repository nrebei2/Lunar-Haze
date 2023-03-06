package infinityx.util;

import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.GameCanvas;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 * Objects extending this class can be drawn on a GameCanvas
 *
 * World position is included as the main drawing method must order drawing based on it
 */
public abstract class Drawable {
    /** Object world position (on bottom left corner) */
    protected Vector2 position;

    /**
     * Draws this object to the given canvas
     *
     * @param canvas The drawing context
     */
    public abstract void draw(GameCanvas canvas);

}
