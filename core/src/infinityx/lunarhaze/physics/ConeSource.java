package infinityx.lunarhaze.physics;

import box2dLight.ConeLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;

/**
 * A light shaped as a circle's sector with a radius, direction and angle
 */
public class ConeSource extends ConeLight implements LightSource {
    /**
     * The default distance for a cone source light
     */
    private static final float DEFAULT_DISTANCE = 15.0f;
    /**
     * The default (half) cone witdth
     */
    private static final float DEFAULT_ANGLE = 30.0f;

    /**
     * Copy of the collision filter.  Necessary because the original version is private
     */
    protected Filter collisions;

    /**
     * Whether direction should update with body if parented
     */
    private boolean updateDirection;

    /**
     * Creates light shaped as a circle with default radius, color and cone settings.
     * <p>
     * The default radius is DEFAULT_DISTANCE, while the default color is DEFAULT_COLOR
     * in LightSource.  The default position is the origin.  The default direction is
     * 0 degrees, and the default cone (half) width is DEFAULT_ANGLE.
     * <p>
     * RayHandler is NOT allowed to be null.  This is the source of many design problems.
     * <p>
     * The number of rays determines how realistic the light looks.  More rays will
     * decrease performance.  The number of rays cannot be less than MIN_RAYS.
     * <p>
     * The soft shadow length is set to distance * 0.1f.  This is why it ignores thin
     * walls, and is not particularly useful.
     *
     * @param rayHandler a non-null instance of RayHandler
     * @param rays       the number of rays
     */
    public ConeSource(RayHandler rayHandler, int rays) {
        super(rayHandler, rays, DEFAULT_COLOR, DEFAULT_DISTANCE, 0, 0, 0, DEFAULT_ANGLE);
        updateDirection = true;
    }

    /**
     * Creates light shaped as a circle with default color, position, and direction
     * <p>
     * The default color is DEFAULT_COLOR in LightSource.  The default position is the
     * origin, while the default direction is 0 degrees.
     * <p>
     * RayHandler is NOT allowed to be null.  This is the source of many design problems.
     * <p>
     * The number of rays determines how realistic the light looks.  More rays will
     * decrease performance.  The number of rays cannot be less than MIN_RAYS.
     * <p>
     * The soft shadow length is set to distance * 0.1f.  This is why it ignores thin
     * walls, and is not particularly useful.
     * <p>
     * The cone-sector is defined by half its arc.  So a setting of 90 has a 180 degree
     * view of everything.
     *
     * @param rayHandler a non-null instance of RayHandler
     * @param rays       the number of rays
     * @param distance   the light radius
     * @param coneDegree half-size of the cone sector, centered over direction
     */
    public ConeSource(RayHandler rayHandler, int rays, float distance, float coneDegree) {
        super(rayHandler, rays, DEFAULT_COLOR, distance, 0, 0, 0, coneDegree);
    }

    /**
     * Creates light shaped as a circle with given radius, color and cone settings.
     * <p>
     * RayHandler is NOT allowed to be null.  This is the source of many design problems.
     * <p>
     * The number of rays determines how realistic the light looks.  More rays will
     * decrease performance.  The number of rays cannot be less than MIN_RAYS.
     * <p>
     * The soft shadow length is set to distance * 0.1f.  This is why it ignores thin
     * walls, and is not particularly useful.
     * <p>
     * The cone-sector is defined by half its arc.  So a setting of 90 has a 180 degree
     * view of everything.
     *
     * @param rayHandler a non-null instance of RayHandler
     * @param rays       the number of rays
     * @param color      the light color, or null for default
     * @param distance   the light radius
     * @param x          the horizontal position in world coordinates
     * @param y          the vertical position in world coordinates
     * @param direction  the center of the cone, as measured from the horizontal
     * @param coneDegree half-size of the cone sector, centered over direction
     */
    public ConeSource(RayHandler rayHandler, int rays, Color color, float distance,
                      float x, float y, float direction, float coneDegree) {
        super(rayHandler, rays, color, distance, x, y, direction, coneDegree);
    }

    /**
     * If parented to a body, whether the direction will update
     */
    public void updateDirection(boolean update) {
        updateDirection = update;
    }

    @Override
    protected void updateBody() {
        if (body == null || staticLight) return;

        final Vector2 vec = body.getPosition();
        if (!updateDirection) {
            float angle = getDirection() * MathUtils.degreesToRadians;
            final float cos = MathUtils.cos(angle);
            final float sin = MathUtils.sin(angle);
            final float dX = bodyOffsetX * cos - bodyOffsetY * sin;
            final float dY = bodyOffsetX * sin + bodyOffsetY * cos;
            start.x = vec.x + dX;
            start.y = vec.y + dY;
        } else {
            float angle = body.getAngle();
            final float cos = MathUtils.cos(angle);
            final float sin = MathUtils.sin(angle);
            final float dX = bodyOffsetX * cos - bodyOffsetY * sin;
            final float dY = bodyOffsetX * sin + bodyOffsetY * cos;
            start.x = vec.x + dX;
            start.y = vec.y + dY;
            setDirection(bodyAngleOffset + angle * MathUtils.radiansToDegrees);
        }
    }

    @Override
    /**
     * Returns the direction of this light in degrees
     *
     * The angle is measured from the right horizontal, as normal.  If the light
     * does not have a direction, this value is 0.
     *
     * @return the direction of this light in degrees
     */
    public float getDirection() {
        return direction;
    }

    @Override
    public Filter getContactFilter() {
        return collisions;
    }

    @Override
    /**
     * Sets the current contact filter for this light
     *
     * The contact filter defines which obstacles block the light, and which are see
     * through.  As a general rule, sensor objects should not block light beams.
     *
     * @param filter the current contact filter for this light
     */
    public void setContactFilter(Filter filter) {
        collisions = filter;
        super.setContactFilter(filter);
    }

    /**
     * Creates a new contact filter for this light with given parameters
     * <p>
     * The contact filter defines which obstacles block the light, and which are see
     * through.  As a general rule, sensor objects should not block light beams.
     * See Filter for a complete description of these parameters.
     *
     * @param categoryBits the category of this light (to allow objects to exclude this light)
     * @param groupIndex   the group index of the light, for coarse-grain filtering
     * @param maskBits     the mask of this light (to allow the light to exclude objects)
     */
    public void setContactFilter(short categoryBits, short groupIndex, short maskBits) {
        collisions = new Filter();
        collisions.categoryBits = categoryBits;
        collisions.groupIndex = groupIndex;
        collisions.maskBits = maskBits;
        super.setContactFilter(collisions);
    }
}
