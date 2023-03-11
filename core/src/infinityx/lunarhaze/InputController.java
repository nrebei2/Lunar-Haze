package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;

/**
 * Device-independent input manager.
 * <p>
 * This class supports keyboard controller.
 */
public class InputController {
    /**
     * How much did we move horizontally?
     */
    private float horizontal;
    /**
     * How much did we move vertically?
     */
    private float vertical;
    /**
     * Did we press the attack button?
     */
    private boolean attackPressed;
    /**
     * Whether the collect button was pressed.
     */
    private boolean collectPressed;
    /**
     * Whether the use button was pressed.
     */
    private boolean usePressed;
    /**
     * Whether the reset button was pressed.
     */
    private boolean resetPressed;

    private boolean exitPressed;
    /** The singleton instance of the input controller */
    private static InputController theController = null;

    /**
     * Returns the amount of sideways movement.
     * <p>
     * -1 = left, 1 = right, 0 = still
     *
     * @return the amount of sideways movement.
     */
    public float getHorizontal() {
        return horizontal;
    }

    /**
     * Returns the amount of vertical movement.
     * <p>
     * -1 = down, 1 = up, 0 = still
     *
     * @return the amount of vertical movement.
     */
    public float getVertical() {
        return vertical;
    }

    /**
     * Returns true if the reset button was pressed.
     *
     * @return true if the reset button was pressed.
     */
    public boolean didReset() {
        return resetPressed;
    }

    /**
     * Returns true if the attack button was pressed.
     *
     * @return true if the attack button was pressed.
     */
    public boolean didAttack() {
        return attackPressed;
    }

    /**
     * Returns true if the collect button was pressed.
     *
     * @return true if the collect button was pressed.
     */
    public boolean didCollect() {
        return collectPressed;
    }

    /**
     * Returns true if the exit button was pressed.
     *
     * @return true if the exit button was pressed.
     */
    public boolean didExit() {
        return exitPressed;
    }

    /**
     * Returns true if the use button was pressed.
     *
     * @return true if the use button was pressed.
     */
    public boolean didUse() {
        return usePressed;
    }

    /**
     * Return the singleton instance of the input controller
     *
     * @return the singleton instance of the input controller
     */
    public static InputController getInstance() {
        if (theController == null) {
            theController = new InputController();
        }
        return theController;
    }

    /**
     * Reads input from the keyboard.
     * <p>
     * This controller reads from the keyboard.
     */
    public void readKeyboard() {
//        resetPressed = (resetPressed) || (Gdx.input.isKeyPressed(Input.Keys.R));
//        attackPressed = (attackPressed) || (Gdx.input.isKeyPressed(Keys.SPACE));
//        collectPressed = (collectPressed) || (Gdx.input.isKeyPressed(Input.Keys.E));
//        usePressed  = (usePressed) || (Gdx.input.isKeyPressed(Input.Keys.F));
//        exitPressed = (exitPressed) || (Gdx.input.isKeyPressed(Keys.ESCAPE));
        resetPressed = Gdx.input.isKeyPressed(Input.Keys.R);
        attackPressed = Gdx.input.isKeyPressed(Keys.SPACE);
        collectPressed = Gdx.input.isKeyPressed(Input.Keys.E);
        usePressed = Gdx.input.isKeyPressed(Input.Keys.F);
        exitPressed = Gdx.input.isKeyPressed(Keys.ESCAPE);

        // Directional controls
        horizontal = 0.0f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            horizontal += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            horizontal -= 1.0f;
        }
        vertical = 0.0f;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vertical += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vertical -= 1.0f;
        }

    }

}