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
import infinityx.util.Drawable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Base class for all Model objects in the game.
 */
public abstract class GameObject implements Drawable {

	/**
	 * Enum specifying the type of this game object.
	 *
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
	/** Object world position (center) */
	protected Vector2 position;
	/** Object shadow position (centered on the texture middle) */
	protected Vector2 shadowposition;
	/** Object velocity vector */
	protected Vector2 velocity;
	/** Reference to texture origin */
	protected Vector2 origin;
	/** Reference to shadow origin */
	protected Vector2 shadoworigin;
	/** Radius of the object shadow (used for collisions) */
	protected float radius;
	/** Width of the object shadow (used for collisions) */
	protected float shadowwidth;
	/** Height of the object shadow (used for collisions) */
	protected float shadowheight;
	/** Whether or not the object should be removed at next timestep. */
	protected boolean destroyed;
	/** CURRENT image for this object.  */
	// TODO: Change to FilmStrip to support animations easier
	protected Texture texture;
//	//protected FilmStrip animator;
//	protected FilmStrip filmStrip;

	public void setTexture(Texture texture) {

		this.texture = texture;
		this.origin = new Vector2();
		this.origin.set(texture.getWidth()/2.0f, texture.getHeight()/2.0f);

	}

	public Texture getTexture() {
		return texture;
	}

	/**
	 * Returns the shadowposition of this object (e.g. location of the center pixel of the shadow)
	 *
	 * The value returned is a reference to the position vector, which may be
	 * modified freely.
	 *
	 * @return the shadowposition of this object
	 */
	public Vector2 getShadowposition() {
		return shadowposition;
	}

	/**
	 * Returns the center world position of this object
	 *
	 * The value returned is a reference to the position vector, which may be
	 * modified freely.
	 *
	 * @return the position of this object
	 */
	public Vector2 getPosition() {
		return position;
	}

	/**
	 * Returns the x value of origin of the shadow of this object.
	 *
	 * The value returned should be the position of the bottom-left corner. For example:
	 *
	 *          [ ]
	 *          -|-
	 *          ||
	 *       —————————
	 *   (25,3)    (29,3)
	 * The function should return 25.
	 * @return the x value of the origin of the shadow of this object
	 */
	public float getShadoworiginX() {
		return shadoworigin.x;
	}

	/**
	 * Returns the x value of origin of the shadow of this object.
	 *
	 * The value returned should be the position of the bottom-left corner. For example:
	 *
	 *          [ ]
	 *          -|-
	 *          ||
	 *       —————————
	 *   (25,3)    (29,3)
	 * The function should return 3.
	 * @return the y value of the origin of the shadow of this object
	 */
	public float getShadoworiginY() {
		return shadoworigin.y;
	}

	/**
	 * Returns the x-coordinate of the object position (center).
	 *
	 * @return the x-coordinate of the object position
	 */
	public float getX() {
		return position.x;
	}

	/**
	 * Sets the x-coordinate of the object position (center).
	 *
	 * @param value the x-coordinate of the object position
	 */
	public void setX(float value) {
		position.x = value;
	}

	/**
	 * Returns the y-coordinate of the object position (center).
	 *
	 * @return the y-coordinate of the object position
	 */
	public float getY() {
		return position.y;
	}

	/**
	 * Sets the y-coordinate of the object position (center).
	 *
	 * @param value the y-coordinate of the object position
	 */
	public void setY(float value) {
		position.y = value;
	}

	/**
	 * Returns the velocity of this object in pixels per animation frame.
	 *
	 * The value returned is a reference to the velocity vector, which may be
	 * modified freely.
	 *
	 * @return the velocity of this object
	 */
	public Vector2 getVelocity() {
		return velocity;
	}

	/**
	 * Returns the x-coordinate of the object velocity.
	 *
	 * @return the x-coordinate of the object velocity.
	 */
	public float getVX() {
		return velocity.x;
	}

	/**
	 * Sets the x-coordinate of the object velocity.
	 *
	 * @param value the x-coordinate of the object velocity.
	 */
	public void setVX(float value) {
		velocity.x = value;
	}

	/**
	 * Sets the y-coordinate of the object velocity.
	 */
	public float getVY() {
		return velocity.y;
	}

	/**
	 * Sets the y-coordinate of the object velocity.
	 *
	 * @param value the y-coordinate of the object velocity.
	 */
	public void setVY(float value) {
		velocity.y = value;
	}


	/**
	 * Returns true if this object is destroyed.
	 *
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
	 *
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
	 *
	 * All of our objects are circles, to make collision detection easy.
	 *
	 * @return the radius of this object's shadow.
	 */
	public float getRadius() {
		return radius;
	}

	/**
	 * Returns the width of this object's shadow.
	 *
	 * All of our objects are circles, to make collision detection easy.
	 *
	 * @return the width of this object's shadow.
	 */
	public float getShadowwidth() {
		return shadowwidth;
	}

	/**
	 * Returns the height of this object's shadow.
	 *
	 * All of our objects are circles, to make collision detection easy.
	 *
	 * @return the height of this object's shadow.
	 */
	public float getShadowheight() {
		return shadowheight;
	}

	/**
	 * Returns the type of this object.
	 *
	 * We use this instead of runtime-typing for performance reasons.
	 *
	 * @return the type of this object.
	 */
	public abstract ObjectType getType();

	/**
	 * Constructs a trivial game object
	 *
	 * The created object has no position or size.  These should be set by the subclasses.
	 */
	public GameObject(float x, float y) {
		position = new Vector2(x, y);
		velocity = new Vector2(0.0f, 0.0f);
		radius = 0.0f;
		destroyed = false;
		shadowposition = new Vector2(x, y);
	}

	/**
	 * Updates the state of this object.
	 *
	 * This method only is only intended to update values that change local state in
	 * well-defined ways, like position or a cooldown value.  It does not handle
	 * collisions (which are determined by the CollisionController).  It is
	 * not intended to interact with other objects in any way at all.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void update(float delta) {
		position.add(velocity);
	}

	public float getDepth() {
		return this.getY() - this.radius;
	}

	@Override
	public void draw(GameCanvas canvas) {
		canvas.draw(texture, Color.WHITE, origin.x, origin.y,
				position.x, position.y, 0.0f, 1.0f, 1.f);
	}
}