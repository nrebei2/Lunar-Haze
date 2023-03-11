/*
 *  GameObject.java
 *
 * In this application, we have a lot more model classes than we did in
 * previous labs.  Because these classes have a lot in common, we have
 * put their features into a base class, just as you learned in OO programming.
 *
 * With that said, you have to be very careful when subclassing your models.
 * Your hierarchy can get deep and complicated very fast if you are not
 * careful.  In fact, we have a later lecture about how subclassing is
 * not always a good idea. But it is okay in this instance because we are
 * only subclass one-level deep.
 *
 * This class continues our policy of using "passive" models. It does not
 * access the methods or fields of any other Model class.  It also
 * does not store any other model object as a field. This allows us
 * to prevent the models from being tightly coupled.  All of the coupled
 * behavior has been moved to GameplayController.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import infinityx.lunarhaze.physics.BoxObstacle;
import infinityx.util.Drawable;

/**
 * Base class for all Model objects in the game.
 */
public abstract class GameObject extends BoxObstacle implements Drawable {

    /**
     * Enum specifying the type of this game object.
     * <p>
     * This Enum is not strictly necessary.  We could use runtime-time
     * typing instead.  However, enums can be used in switch statements
     * (which are very fast), which types cannot. That is the motivation
     * for this Enum.
     * If you add new subclasses of GameObject, you will need to add
     * to this Enum as well.
     */
    public enum ObjectType {
        ENEMY,
        WEREWOLF,
    }

    // Attributes for all game objects
    /**
     * Reference to texture origin
     */
    protected Vector2 origin;
    /**
     * Radius of the object shadow (used for collisions)
     */
    protected float radius;
    /**
     * Whether or not the object should be removed at next timestep.
     */
    protected boolean destroyed;
    /**
     * CURRENT image for this object.
     */
    // TODO: Change to FilmStrip to support animations easier
    protected Texture texture;

    /**
     * Creates game object at (0, 0)
     */
    public GameObject() {
        super(0, 0);
    }

    /**
     * Creates a new game object.
     * <p>
     * All units (position, velocity, etc.) are in world units.
     *
     * @param x The object x-coordinate in world
     * @param y The object y-coordinate in world
     */
    public GameObject(float x, float y) {
        // player takes half of a tile
        super(x, y, Board.TILE_WIDTH / 2, Board.TILE_HEIGHT / 2);
        setFixedRotation(true);
        setBodyType(BodyDef.BodyType.DynamicBody);

        radius = Board.TILE_WIDTH / 4;
        destroyed = false;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
        this.origin = new Vector2();
        this.origin.set(texture.getWidth() / 2.0f, 0);
    }

    public Texture getTexture() {
        return texture;
    }

    /**
     * Returns the shadowposition of this object (e.g. location of the center pixel of the shadow)
     * <p>
     * The value returned is a reference to the position vector, which may be
     * modified freely.
     *
     * @return the shadowposition of this object
     */
    public Vector2 getShadowposition() {
        return this.getPosition();
    }


    /**
     * Returns true if this object is destroyed.
     * <p>
     * Objects are not removed immediately when destroyed.  They are garbage collected
     * at the end of the frame.  This tells us whether the object should be garbage
     * collected at the frame end.
     *
     * @return true if this object is destroyed
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Sets whether this object is destroyed.
     * <p>
     * Objects are not removed immediately when destroyed.  They are garbage collected
     * at the end of the frame.  This tells us whether the object should be garbage
     * collected at the frame end.
     *
     * @param value whether this object is destroyed
     */
    public void setDestroyed(boolean value) {
        destroyed = value;
    }

    /**
     * Returns the radius of this object's shadow.
     * <p>
     * All of our objects are circles, to make collision detection easy.
     *
     * @return the radius of this object's shadow.
     */
    public float getRadius() {
        return this.getWidth();
    }

    /**
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    public abstract ObjectType getType();

    /**
     * Updates the (local) state of this object.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {

    }

    public float getDepth() {
        return this.getY();
    }

    @Override
    public void draw(GameCanvas canvas) {
        canvas.draw(texture, Color.WHITE, origin.x, origin.y,
                GameCanvas.WorldToScreenX(getPosition().x), GameCanvas.WorldToScreenY(getPosition().y), 0.0f, 1.0f, 1.f);
    }
}