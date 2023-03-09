package infinityx.lunarhaze.physics;

import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.PositionalLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.Filter;
import com.sun.tools.javac.util.Position;

public class MoonlightSource extends PointLight implements LightSource {
    protected float size;

    /**
     * Creates light shaped as a square with given size
     *
     * @param rayHandler
     *            not {@code null} instance of RayHandler
     * @param rays
     *            number of rays - more rays make light to look more realistic
     *            but will decrease performance, can't be less than MIN_RAYS
     * @param color
     *            color, set to {@code null} to use the default color
     * @param size
     *            size of the square, width and height are equal
     * @param x
     *            horizontal position in world coordinates
     * @param y
     *            vertical position in world coordinates
     */
    public MoonlightSource(RayHandler rayHandler, int rays, Color color,
                       float size, float x, float y) {
        super(rayHandler, rays, color, size, x, y);
    }

    public void update () {
        updateBody();
        if (dirty) setEndPoints();

        if (cull()) return;
        if (staticLight && !dirty) return;

        dirty = false;
        updateMesh();
    }

    @Override
    public void setDistance(float dist) {
        this.size = size < 0.01f ? 0.01f : size;
        this.distance = size * (float)Math.sqrt(2);
        dirty = true;
    }

    @Override
    public void setDirection(float directionDegree) {

    }

    @Override
    public Filter getContactFilter() {
        return null;
    }

    /**
     * Sets light size
     * <p>MIN value capped to 0.01f meter
     * <p>Actual recalculations will be done only on {@link #update()} call
     */
    public void setSize(float size) {

    }

    /**
     * Updates light's end points
     */
    void setEndPoints() {

    }
}
