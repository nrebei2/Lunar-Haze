package infinityx.lunarhaze.physics;

import box2dLight.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

/**
 * A light shaped as a circle's sector with a radius, direction and angle
 */
public class ConeSource extends ConeLight implements LightSource {
    /** The default distance for a cone source light */
    private static float DEFAULT_DISTANCE = 15.0f;
    /** The default (half) cone witdth */
    private static float DEFAULT_ANGLE    = 30.0f;

    /** Copy of the collision filter.  Necessary because the original version is private */
    protected Filter collisions;

    /**
     * Creates light shaped as a circle with default radius, color and cone settings.
     *
     * The default radius is DEFAULT_DISTANCE, while the default color is DEFAULT_COLOR
     * in LightSource.  The default position is the origin.  The default direction is
     * 0 degrees, and the default cone (half) width is DEFAULT_ANGLE.
     *
     * RayHandler is NOT allowed to be null.  This is the source of many design problems.
     *
     * The number of rays determines how realistic the light looks.  More rays will
     * decrease performance.  The number of rays cannot be less than MIN_RAYS.
     *
     * The soft shadow length is set to distance * 0.1f.  This is why it ignores thin
     * walls, and is not particularly useful.
     *
     * @param rayHandler	a non-null instance of RayHandler
     * @param rays			the number of rays
     */
    public ConeSource(RayHandler rayHandler, int rays) {
        super(rayHandler, rays, DEFAULT_COLOR, DEFAULT_DISTANCE, 0, 0, 0, DEFAULT_ANGLE);
    }

    /**
     * Creates light shaped as a circle with default color, position, and direction
     *
     * The default color is DEFAULT_COLOR in LightSource.  The default position is the
     * origin, while the default direction is 0 degrees.
     *
     * RayHandler is NOT allowed to be null.  This is the source of many design problems.
     *
     * The number of rays determines how realistic the light looks.  More rays will
     * decrease performance.  The number of rays cannot be less than MIN_RAYS.
     *
     * The soft shadow length is set to distance * 0.1f.  This is why it ignores thin
     * walls, and is not particularly useful.
     *
     * The cone-sector is defined by half its arc.  So a setting of 90 has a 180 degree
     * view of everything.
     *
     * @param rayHandler	a non-null instance of RayHandler
     * @param rays			the number of rays
     * @param distance		the light radius
     * @param coneDegree	half-size of the cone sector, centered over direction
     */
    public ConeSource(RayHandler rayHandler, int rays, float distance, float coneDegree) {
        super(rayHandler, rays, DEFAULT_COLOR, distance, 0, 0, 0, coneDegree);
    }

    /**
     * Creates light shaped as a circle with given radius, color and cone settings.
     *
     * RayHandler is NOT allowed to be null.  This is the source of many design problems.
     *
     * The number of rays determines how realistic the light looks.  More rays will
     * decrease performance.  The number of rays cannot be less than MIN_RAYS.
     *
     * The soft shadow length is set to distance * 0.1f.  This is why it ignores thin
     * walls, and is not particularly useful.
     *
     * The cone-sector is defined by half its arc.  So a setting of 90 has a 180 degree
     * view of everything.
     *
     * @param rayHandler	a non-null instance of RayHandler
     * @param rays			the number of rays
     * @param color			the light color, or null for default
     * @param distance		the light radius
     * @param x				the horizontal position in world coordinates
     * @param y				the vertical position in world coordinates
     * @param direction		the center of the cone, as measured from the horizontal
     * @param coneDegree	half-size of the cone sector, centered over direction
     */
    public ConeSource(RayHandler rayHandler, int rays, Color color, float distance,
                      float x, float y, float direction, float coneDegree) {
        super(rayHandler, rays, color, distance, x, y, direction, coneDegree);
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
     *
     * The contact filter defines which obstacles block the light, and which are see
     * through.  As a general rule, sensor objects should not block light beams.
     * See Filter for a complete description of these parameters.
     *
     * @param categoryBits	the category of this light (to allow objects to exclude this light)
     * @param groupIndex    the group index of the light, for coarse-grain filtering
     * @param maskBits      the mask of this light (to allow the light to exclude objects)
     */
    public void setContactFilter(short categoryBits, short groupIndex, short maskBits) {
        collisions = new Filter();
        collisions.categoryBits = categoryBits;
        collisions.groupIndex = groupIndex;
        collisions.maskBits = maskBits;
        super.setContactFilter(collisions);
    }
}
