package infinityx.util;

import infinityx.lunarhaze.graphics.GameCanvas;


/**
 * Represents a drawable entity. Objects implementing this class can be drawn on a GameCanvas.
 */
public interface Drawable {

    /**
     * Simple depth buffer, object-wise instead of pixel-wise.
     *
     * @return depth of object from camera
     */
    float getDepth();

    /**
     * Draws this object to the given canvas
     *
     * @param canvas The drawing context
     */
    void draw(GameCanvas canvas);

    /**
     * Returns true if this object is destroyed.
     *
     * @return true if this object is destroyed
     */
    boolean isDestroyed();

    /**
     * Sets this object as destroyed or not.
     * If you set this object as destroyed it will be removed from drawing next timestep.
     * If you wish to reuse this object after destroying you must setDestroyed(false)!
     *
     * @param destroyed Whether to destroy (true) or not (false)
     */
    void setDestroyed(boolean destroyed);

    /**
     * Returns an identifier that groups similar drawables together
     */
    int getID();
}
