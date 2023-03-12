package infinityx.util;

import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.GameCanvas;


/**
 * Represents a drawable entity. Objects implementing this class can be drawn on a GameCanvas.
 */
public interface Drawable {

    /**
     * Simple depth buffer, object-wise instead of pixel-wise.
     *
     * @return depth of object from camera
     */
    public float getDepth();

    /**
     * Draws this object to the given canvas
     *
     * @param canvas The drawing context
     */
    public void draw(GameCanvas canvas);

}
