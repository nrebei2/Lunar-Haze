/*
 * BoxObject.java
 *
 * Given the name Box2D, this is your primary model class.  Most of the time,
 * unless it is a player controlled avatar, you do not even need to subclass
 * BoxObject.  Look through the code and see how many times we use this class.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package infinityx.lunarhaze.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;

/**
 * Box-shaped model to support collisions.
 * <p>
 * Unless otherwise specified, the center of mass is as the center.
 */
public class BoxObstacle extends SimpleObstacle {
    /**
     * Shape information for this box
     */
    protected PolygonShape shape;
    /**
     * The width and height of the box
     */
    private final Vector2 dimension;
    /**
     * A cache value for when the user wants to access the dimensions
     */
    private final Vector2 sizeCache;
    /**
     * A cache value for the fixture (for resizing)
     */
    private Fixture geometry;
    /**
     * Cache of the polygon vertices (for resizing)
     */
    private final float[] vertices;

    /**
     * Where is the position located on the object
     */
    public enum POSITIONED {
        CENTERED, BOTTOM_LEFT
    }

    protected POSITIONED positioned = POSITIONED.CENTERED;

    protected Vector2 scale = new Vector2(1, 1);

    /**
     * Returns the dimensions of this box
     * <p>
     * This method does NOT return a reference to the dimension vector. Changes to this
     * vector will not affect the shape.  However, it returns the same vector each time
     * its is called, and so cannot be used as an allocator.
     *
     * @return the dimensions of this box
     */
    public Vector2 getDimension() {
        return sizeCache.set(dimension);
    }

    /**
     * Returns the box width
     *
     * @return the box width
     */
    public float getWidth() {
        return dimension.x;
    }

    /**
     * Returns the box height
     *
     * @return the box height
     */
    public float getHeight() {
        return dimension.y;
    }

    /**
     * Sets the dimensions of this box
     *
     * @param width  The width of this box
     * @param height The height of this box
     */
    public void setDimension(float width, float height) {
        dimension.set(width, height);
        resize(width, height);
        createFixtures();
    }

    /**
     * Sets where the position is fixed on this box
     *
     * @param positioned bottom-left? centered?
     */
    public void setPositioned(POSITIONED positioned) {
        this.positioned = positioned;
        resize(getWidth(), getHeight());
        createFixtures();
    }

    /**
     * Sets the scale of the box. This will scale BOTH the texture and box collider.
     *
     * @param s_x scale x-axis
     * @param s_y scale y_axis
     */
    public void setScale(float s_x, float s_y) {
        scale.set(s_x, s_y);
        dimension.set(getWidth() * s_x, getHeight() * s_y);
        resize(getWidth(), getHeight());
        createFixtures();
    }

    /**
     * Creates a new box at the origin.
     * <p>
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param width  The object width in physics units
     * @param height The object width in physics units
     */
    public BoxObstacle(float width, float height) {
        this(0, 0, width, height);
    }

    /**
     * Creates a new box object.
     * <p>
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x      Initial x position of the box center
     * @param y      Initial y position of the box center
     * @param width  The object width in physics units
     * @param height The object width in physics units
     */
    public BoxObstacle(float x, float y, float width, float height) {
        super(x, y);
        dimension = new Vector2(width, height);
        sizeCache = new Vector2();
        shape = new PolygonShape();
        vertices = new float[8];
        geometry = null;

        // Initialize
        resize(width, height);
    }

    /**
     * Creates a new box object.
     * <p>
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x      Initial x position of the box center
     * @param y      Initial y position of the box center
     * @param width  The object width in physics units
     * @param height The object width in physics units
     */
    public BoxObstacle(POSITIONED positioned, float x, float y, float width, float height) {
        super(x, y);
        dimension = new Vector2(width, height);
        sizeCache = new Vector2();
        shape = new PolygonShape();
        vertices = new float[8];
        geometry = null;
        this.positioned = positioned;

        // Initialize
        resize(width, height);
    }

    /**
     * Reset the polygon vertices in the shape to match the dimension.
     */
    private void resize(float width, float height) {
        if (positioned == POSITIONED.CENTERED) {
            // Make the box with the center on the position
            vertices[0] = -width / 2.0f;
            vertices[1] = -height / 2.0f;
            vertices[2] = -width / 2.0f;
            vertices[3] = height / 2.0f;
            vertices[4] = width / 2.0f;
            vertices[5] = height / 2.0f;
            vertices[6] = width / 2.0f;
            vertices[7] = -height / 2.0f;
        } else {
            // Make the box with the bottom-left on the position
            vertices[0] = 0;
            vertices[1] = 0;
            vertices[2] = 0;
            vertices[3] = height;
            vertices[4] = width;
            vertices[5] = height;
            vertices[6] = width;
            vertices[7] = 0;
        }
        shape.set(vertices);
    }

    /**
     * Create new fixtures for this body, defining the shape
     * <p>
     * This is the primary method to override for custom physics objects
     */
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        // Create the fixture
        fixture.shape = shape;
        geometry = body.createFixture(fixture);
        geometry.setUserData(this);
    }

    /**
     * Release the fixtures for this body, reseting the shape
     * <p>
     * This is the primary method to override for custom physics objects
     */
    protected void releaseFixtures() {
        if (geometry != null) {
            body.destroyFixture(geometry);
            geometry = null;
        }
    }
}