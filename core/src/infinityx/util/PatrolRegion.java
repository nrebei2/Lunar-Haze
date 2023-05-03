package infinityx.util;

/**
 * Represents a rectangular patrol region.
 */
public class PatrolRegion {

    /**
     * The bottom left corner coordinates of the rectangular region as an array of two floats (x, y).
     */
    private float[] bottomLeft;

    /**
     * The top right corner coordinates of the rectangular region as an array of two floats (x, y).
     */
    private float[] topRight;

    /**
     * Constructor for the PatrolRegion class.
     * Initializes the bottom left and top right corner coordinates.
     *
     * @param bottomLeftX The x-coordinate of the bottom left corner.
     * @param bottomLeftY The y-coordinate of the bottom left corner.
     * @param topRightX   The x-coordinate of the top right corner.
     * @param topRightY   The y-coordinate of the top right corner.
     */
    public PatrolRegion(float bottomLeftX, float bottomLeftY, float topRightX, float topRightY) {
        this.bottomLeft = new float[]{bottomLeftX, bottomLeftY};
        this.topRight = new float[]{topRightX, topRightY};
    }

    /**
     * Get the bottom left corner coordinates.
     *
     * @return float[] containing the x and y coordinates of the bottom left corner.
     */
    public float[] getBottomLeft() {
        return bottomLeft;
    }

    /**
     * Get the top right corner coordinates.
     *
     * @return float[] containing the x and y coordinates of the top right corner.
     */
    public float[] getTopRight() {
        return topRight;
    }

    /**
     * Get the width of this patrol region
     */
    public float getWidth() {
        return topRight[0] - bottomLeft[0];
    }

    /**
     * Get the height of this patrol region
     */
    public float getHeight() {
        return topRight[1] - bottomLeft[1];
    }
}
