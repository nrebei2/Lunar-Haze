package infinityx.util;

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

    /**
     * Returns true if this object is destroyed.
     * @return true if this object is destroyed
     */
    boolean isDestroyed();
    /**
     * Sets this object as destroyed. Will be removed from drawing next timestep.
     */
    void setDestroyed();

}
