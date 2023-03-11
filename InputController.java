package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Array;
import infinityx.util.Controllers;
import infinityx.util.XBoxController;

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

    /** XBox Controller support */
    private XBoxController xbox;

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
     * Creates a new input controller
     *
     * The input controller attempts to connect to the X-Box controller at device 0,
     * if it exists.  Otherwise, it falls back to the keyboard control.
     */
    public InputController() {
        // If we have a game-pad for id, then use it.
        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get(0);
        } else {
            xbox = null;
        }
    }

    /**
     * Reads the input for the player and converts the result into game logic.
     */
    public void readInput() {
        // Check to see if a GamePad is connected
        if (xbox != null && xbox.isConnected()) {
            readGamepad();
            readKeyboard(true); // Read as a back-up
        } else {
            readKeyboard(false);
        }
    }

    private void readGamepad() {
        resetPressed = xbox.getRBumper();
        attackPressed = xbox.getA();
        collectPressed = xbox.getY();
        usePressed  = xbox.getB();
        exitPressed  = xbox.getX();

        // Increase animation frame, but only if trying to move
        horizontal = xbox.getLeftX();
        vertical = xbox.getLeftY();
    }

    /**
     * Reads input from the keyboard.
     * <p>
     * This controller reads from the keyboard.
     */
    public void readKeyboard(boolean secondary) {
        resetPressed = (secondary && resetPressed) || (Gdx.input.isKeyPressed(Input.Keys.R));
        attackPressed = (secondary && attackPressed) || (Gdx.input.isKeyPressed(Keys.SPACE));
        collectPressed = (secondary && collectPressed) || (Gdx.input.isKeyPressed(Input.Keys.E));
        usePressed  = (secondary && usePressed) || (Gdx.input.isKeyPressed(Input.Keys.F));
        exitPressed = (secondary && exitPressed) || (Gdx.input.isKeyPressed(Keys.ESCAPE));

        // Directional controls
        horizontal = (secondary ? horizontal : 0.0f);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            horizontal += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            horizontal -= 1.0f;
        }
        vertical = (secondary ? vertical : 0.0f);
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vertical += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vertical -= 1.0f;
        }

    }

}