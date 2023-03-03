package infinityx.lunarhaze.entity;

import infinityx.lunarhaze.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Enemy extends GameObject{

    /** Movement of the enemy **/
    private float movement;

    /** Current animation frame for this werewolf */
    private float animeframe;

    /** Whether the enemy is alerted. Once alerted,
     * the enemy start chasing werewolf */
    private Boolean isAlerted;

    /**
     * Returns the type of this object.
     *
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    public ObjectType getType() {
        return ObjectType.ENEMY;
    }

    /**
     * Returns the enemy (left/right) movement input.
     *
     * @return the enemy movement input.
     */
    public float getMovement() {
        return movement;
    }

    /**
     * Sets the enemy movement input.
     *
     * @param value the enemy movement input.
     */
    public void setMovement(float value) {
        movement = value;
    }

    /**
     * Returns whether the enemy is alerted.
     */
    public Boolean getIsAlerted() {
        return isAlerted;
    }

    /**
     * Sets whether the enemy is alerted.
     */
    public void setIsAlerted(Boolean value) {
        isAlerted = value;
    }

    /**
     * Initialize an enemy not alerted.
     */
    public Enemy() {
        animeframe = 0.0f;
        isAlerted = false;
    }

    public void setTexture(Texture texture) {
        throw new NotImplementedException();
    }

    /**
     * Updates the animation frame and position of this enemy.
     *
     * Notice how little this method does.  It does not actively fire the weapon.  It
     * only manages the cooldown and indicates whether the weapon is currently firing.
     * The result of weapon fire is managed by the GameplayController.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {
        // Call superclass's update
        super.update(delta);

        throw new NotImplementedException();
    }

    /**
     * Draws this shell to the canvas
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
