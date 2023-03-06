package infinityx.lunarhaze.entity;

import infinityx.lunarhaze.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Werewolf extends GameObject{

    /** Move speed **/
    private static final float WEREWOLF_SPEED = 4.0f;

    /** How fast we change frames (one frame per 4 calls to update) */
    private static final float ANIMATION_SPEED = 0.25f;

    /** Initial hp of the werewolf is 100 **/
    private static final float INITIAL_HP = 100;

    /** The right/left movement of the werewolf **/
    private float movementH = 0.0f;

    /** The up/down movement of the werewolf **/
    private float movementV = 0.0f;

    /** Whether the  player stands on a moonlight tile**/
    private Boolean moonlight;

    /** Current animation frame for this werewolf */
    private float animeframe;

    /** Health point (hp) of the werewolf */
    private float hp;

    /**
     * Returns the type of this object.
     *
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    public ObjectType getType() {
        return ObjectType.WEREWOLF;
    }

    /**
     * Returns the current player (left/right) movement input.
     *
     * @return the current player movement input.
     */
    public float getMovementH() {
        return movementH;
    }

    /**
     * Returns the current player (up/down) movement input.
     *
     * @return the current player movement input.
     */
    public float getMovementV() {
        return movementV;
    }

    /**
     * Sets the current player (left/right) movement input.
     *
     * @param value the current player movement input.
     */
    public void setMovementH(float value) {
        movementH = value;
    }

    /**
     * Sets the current player (uo/down) movement input.
     *
     * @param value the current player movement input.
     */
    public void setMovementV(float value) {
        movementV = value;
    }

    /**
     * Returns the current hp of the werewolf.
     */
    public float getHp() {
        return hp;
    }

    /**
     * Sets the current hp of the werewolf.
     *
     * @param value the current hp of the werewolf.
     */
    public void setHp(float value) {
        hp = value;
    }

    /**
     * Returns true if the player is on a moonlight tile.
     *
     * @return true if the player is on a moonlight tile.
     */
    public boolean isOnMoonlight() {
        return moonlight;
    }

    public void setOnMoonlight(Boolean b) {
        moonlight = b;
    }

    /**
     * Initialize a werewolf not standing on moonlight tile.
     */
    public Werewolf(float x, float y) {
        super(x, y);
        animeframe = 0.0f;
        moonlight = false;
        hp = INITIAL_HP;
    }

    public void setTexture(Texture texture) {
        throw new NotImplementedException();
    }

    /**
     * Updates the animation frame and position of this werewolf.
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {
        // Call superclass's update
        super.update(delta);

        if (movementH != 0.0f) {
            animeframe += ANIMATION_SPEED;
            position.x += movementH * WEREWOLF_SPEED;
        }

        if (movementV != 0.0f) {
            animeframe += ANIMATION_SPEED;
            position.x += movementV * WEREWOLF_SPEED;
        }

    }

    /**
     * Draws this werewolof to the canvas
     *
     * There is only one drawing pass in this application, so you can draw the objects
     * in any order.
     *
     * @param canvas The drawing context
     */
    public void draw(GameCanvas canvas) {
        throw new NotImplementedException();
    }
}