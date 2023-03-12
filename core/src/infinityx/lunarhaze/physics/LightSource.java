package infinityx.lunarhaze.physics;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;

/**
 * Interface representing a single light source.
 * <p>
 * The light source is attached to a rayhandler on creation and cannot reassigned.  The
 * light source should be removed (which has an implicit dispose) when it is no longer
 * needed.
 */
public interface LightSource {
    /**
     * The minimum number of rays to function properly
     */
    int MIN_RAYS = 3;
    /**
     * The default color for all lights
     */
    Color DEFAULT_COLOR = new Color(0.75f, 0.75f, 0.5f, 0.75f);

    /**
     * Returns true if this light is active
     * <p>
     * An inactive light is not added to the render pass of the rayhandler.
     *
     * @return true if this light is active
     */
    boolean isActive();

    /**
     * Sets whether this light is active
     * <p>
     * An inactive light is not added to the render pass of the rayhandler.
     *
     * @param active whether this light is active
     */
    void setActive(boolean active);

    /**
     * Returns the current color of this light
     * <p>
     * Note that you can also use colorless light with shadows, e.g. (0,0,0,1).
     *
     * @return current color of this light
     */
    Color getColor();

    /**
     * Sets the current color of this light
     * <p>
     * Note that you can also use colorless light with shadows, e.g. (0,0,0,1).
     *
     * @param newColor the color of this light
     */
    void setColor(Color newColor);

    /**
     * Sets the current color of this light
     * <p>
     * Note that you can also use colorless light with shadows, e.g. (0,0,0,1).
     *
     * @param r lights color red component
     * @param g lights color green component
     * @param b lights color blue component
     * @param a lights shadow intensity
     */
    void setColor(float r, float g, float b, float a);

    /**
     * Returns the ray distance of this light (without gamma correction)
     * <p>
     * The minimum value is capped at 0.1f meter.
     *
     * @return the ray distance of this light (without gamma correction)
     */
    float getDistance();

    /**
     * Sets the ray distance of this light (without gamma correction)
     * <p>
     * The minimum value is capped at 0.1f meter.
     *
     * @param dist the ray distance of this light (without gamma correction)
     */

    void setDistance(float dist);

    /**
     * Returns the direction of this light in degrees
     * <p>
     * The angle is measured from the right horizontal, as normal.  If the light
     * does not have a direction, this value is 0.
     *
     * @return the direction of this light in degrees
     */
    float getDirection();

    /**
     * Sets the direction of this light in degrees
     * <p>
     * The angle is measured from the right horizontal, as normal.  If the light
     * does not have a direction, this value is 0.
     *
     * @param directionDegree the direction of this light in degrees
     */
    void setDirection(float directionDegree);

    /**
     * Returns the starting position of light in world coordinates
     * <p>
     * This is a copy of the position vector and not a reference.  Changing the
     * contents of this vector does nothing.
     *
     * @return the starting position of light in world coordinates
     */
    Vector2 getPosition();

    /**
     * Returns the horizontal starting position of light in world coordinates
     *
     * @return the horizontal starting position of light in world coordinates
     */
    float getX();

    /**
     * Returns the vertical starting position of light in world coordinates
     *
     * @return the vertical starting position of light in world coordinates
     */
    float getY();

    /**
     * Sets the light starting position
     *
     * @param x the initial horizontal position
     * @param y the initial vertical position
     */
    void setPosition(float x, float y);

    /**
     * Sets the light starting position
     * <p>
     * This method does not retain a reference to the parameter.
     *
     * @param position the initial starting position
     */
    void setPosition(Vector2 position);

    /**
     * Returns true if the light beams go through obstacles
     * <p>
     * Allowing a light to penetrate obstacles reduces the CPU burden of light
     * about 70%.
     *
     * @return true if the light beams go through obstacles
     */
    boolean isXray();

    /**
     * Sets whether the light beams go through obstacles
     * <p>
     * Allowing a light to penetrate obstacles reduces the CPU burden of light
     * about 70%.
     *
     * @param xray whether the light beams go through obstacles
     */
    void setXray(boolean xray);

    /**
     * Returns true if this light has static behavior
     * <p>
     * Static lights do not get any automatic updates but setting any
     * parameters will update it. Static lights are useful for lights that you
     * want to collide with static geometry but ignore all the dynamic objects.
     * <p>
     * This can reduce the CPU burden of light about 90%
     *
     * @return true if this light has static behavior
     */
    boolean isStaticLight();

    /**
     * Sets whether this light has static behavior
     * <p>
     * Static lights do not get any automatic updates but setting any
     * parameters will update it. Static lights are useful for lights that you
     * want to collide with static geometry but ignore all the dynamic objects.
     * <p>
     * This can reduce the CPU burden of light about 90%
     *
     * @param staticLight whether this light has static behavior
     */
    void setStaticLight(boolean staticLight);

    /**
     * Returns true if the tips of the light beams are soft.
     * <p>
     * Soft light beams allow the player to see through thin obstacles, making this
     * type of shadow less desirable for many applications.  It does look pretty,
     * however.
     *
     * @return true if the tips of this light beams are soft
     */
    boolean isSoft();

    /**
     * Sets whether the tips of the light beams are soft.
     * <p>
     * Soft light beams allow the player to see through thin obstacles, making this
     * type of shadow less desirable for many applications.  It does look pretty,
     * however.
     *
     * @return soft whether tips of this light beams are soft
     */
    void setSoft(boolean soft);

    /**
     * Returns the softness value for the beam tips.
     * <p>
     * By default, this value is 2.5f
     *
     * @return the softness value for the beam tips.
     */
    float getSoftShadowLength();

    /**
     * Sets the softness value for the beam tips.
     * <p>
     * By default, this value is 2.5f
     *
     * @param softShadowLength the softness value for the beam tips.
     */
    void setSoftnessLength(float softShadowLength);

    /**
     * Returns the number of rays set for this light
     * <p>
     * The number of rays determines how realistic the light looks.  More rays will
     * decrease performance.  The number of rays cannot be less than MIN_RAYS.
     *
     * @return the number of rays set for this light
     */
    int getRayNum();

    /// MEMORY MANAGEMENT

    /**
     * Adds a light to specified RayHandler
     * <p>
     * It is only safe to attach a rayhandler if (1) there is no rayhandler
     * currently attached and (2) this light has not been disposed.
     *
     * @param rayHandler the RayHandler
     */
    void add(RayHandler rayHandler);

    /**
     * Removes the light from the active RayHandler and disposes it
     * <p>
     * A disposed light may not be reused.
     */
    void remove();

    /**
     * Removes the light from the active RayHandler and disposes it if requested
     * <p>
     * A disposed light may not be reused.
     *
     * @param doDispose whether to dispose the light
     */
    void remove(boolean doDispose);


    /// PHYSICS METHODS

    /**
     * Returns the body assigned to this light
     * <p>
     * If the light has position, but no body, this method returns null.
     *
     * @return the body assigned to this light
     */
    Body getBody();

    /**
     * Attaches light to specified body
     * <p>
     * The light will automatically follow the body.  Note that the body rotation angle
     * is taken into account for the light offset and direction calculations.
     *
     * @param body the body to assign this light
     */
    void attachToBody(Body body);

    /**
     * Attaches light to specified body
     * <p>
     * The light will automatically follow the body.  Note that the body rotation angle
     * is taken into account for the light offset and direction calculations.
     *
     * @param body the body to assign this light
     * @param dx   horizontal position in body coordinates
     * @param dy   vertical position in body coordinates
     */
    void attachToBody(Body body, float dx, float dy);

    /**
     * Attaches light to specified body
     * <p>
     * The light will automatically follow the body.  Note that the body rotation angle
     * is taken into account for the light offset and direction calculations.
     *
     * @param body    the body to assign this light
     * @param dx      horizontal position in body coordinates
     * @param dy      vertical position in body coordinates
     * @param degrees directional relative offset in degrees
     */
    void attachToBody(Body body, float offsetX, float offSetY, float degrees);

    /**
     * Returns true if the attached body fixtures should be ignored during raycasting
     * <p>
     * If this value is true, all the fixtures of attached body will be ignored and
     * will not create any shadows for this light. By default this is false.
     *
     * @erturn true if the attached body fixtures should be ignored during raycasting
     */
    boolean getIgnoreAttachedBody();

    /**
     * Sets whether the attached body fixtures should be ignored during raycasting
     * <p>
     * If this value is true, all the fixtures of attached body will be ignored and
     * will not create any shadows for this light. By default this is false.
     *
     * @param flag whether the attached body fixtures should be ignored during raycasting
     */
    void setIgnoreAttachedBody(boolean flag);

    /**
     * Returns the current contact filter for this light
     * <p>
     * The contact filter defines which obstacles block the light, and which are see
     * through.  As a general rule, sensor objects should not block light beams.
     *
     * @return the current contact filter for this light
     */
    Filter getContactFilter();

    /**
     * Sets the current contact filter for this light
     * <p>
     * The contact filter defines which obstacles block the light, and which are see
     * through.  As a general rule, sensor objects should not block light beams.
     *
     * @param filter the current contact filter for this light
     */
    void setContactFilter(Filter filter);

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
    void setContactFilter(short categoryBits, short groupIndex, short maskBits);

    /**
     * Returns true if given point is inside of this light area
     *
     * @param x the horizontal position of point in world coordinates
     * @param y the vertical position of point in world coordinates
     * @return true if given point is inside of this light area
     */
    boolean contains(float x, float y);

}
