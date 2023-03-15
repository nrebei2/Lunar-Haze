package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import java.awt.geom.RectangularShape;

/**
 * Device-independent input manager.
 * <p>
 * This class supports keyboard controller.
 */
public class InputController {
    /** Input key for moving up */
    private static final int UP = Input.Keys.W;
    /** Input key for moving down */
    private static final int DOWN = Input.Keys.S;
    /** Input key for moving left */
    private static final int LEFT = Input.Keys.A;
    /** Input key for moving right */
    private static final int RIGHT = Input.Keys.D;
    /** Input key for attacking */
    private static final int ATTACK = Input.Keys.SPACE;
    /** Input key for collecting resource */
    private static final int COLLECT = Input.Keys.E;
    /** Input key for using resource */
    private static final int USE = Input.Keys.F;
    /** Input key for running */
    private static final int RUN = Input.Keys.SHIFT_LEFT;
    /** Input key for resetting the current level */
    private static final int RESET = Input.Keys.R;
    /** Input key for exiting the current level; */
    private static final int EXIT = Input.Keys.ESCAPE;

    /**
     * Constants from asset directory
     */
    JsonValue inputJson;
    float runSpeed;
    float walkSpeed;

    /**
     * Caches all constants (between levels) from directory
     *
     * @param directory asset manager holding list of ... assets
     */
    public void loadConstants(AssetDirectory directory) {
        // TODO: create and cache player and board? not sure if that would do much
        // There is not a lot constant between levels
        JsonValue inputJson = directory.getEntry("input", JsonValue.class);
        runSpeed = inputJson.get("runSpeed").asFloat();
        walkSpeed = inputJson.get("walkSpeed").asFloat();
    }

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
     * Whether the run button was pressed.
     */
    private boolean runPressed;

    /**
     * Whether the reset button was pressed.
     */
    private boolean resetPressed;

    /**
     * Whether the reset button was pressed.
     */
    private boolean exitPressed;

    /**
     * The singleton instance of the input controller
     */
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
     * Returns true if the use button was pressed.
     *
     * @return true if the use button was pressed.
     */
    public boolean didUse() {
        return usePressed;
    }

    /**
     * Returns true if the run button was pressed
     *
     * @return true if the run button was pressed.
     */
    public boolean didRun() {return runPressed;}

    /**
     * Returns true if the reset button was pressed.
     *
     * @return true if the reset button was pressed.
     */
    public boolean didReset() {
        return resetPressed;
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
        attackPressed = Gdx.input.isKeyPressed(ATTACK);
        collectPressed = Gdx.input.isKeyPressed(COLLECT);
        usePressed = Gdx.input.isKeyPressed(USE);
        runPressed = Gdx.input.isKeyPressed(RUN);
        resetPressed = Gdx.input.isKeyPressed(RESET);
        exitPressed = Gdx.input.isKeyPressed(EXIT);

        // Directional controls
        float move;
        if(runPressed){
            move = runSpeed;
        }else{
            move = walkSpeed;
        }

        horizontal = 0.0f;
        if (Gdx.input.isKeyPressed(RIGHT)) {
            horizontal += move;
        }
        if (Gdx.input.isKeyPressed(LEFT)) {
            horizontal -= move;
        }

        vertical = 0.0f;
        if (Gdx.input.isKeyPressed(UP)) {
            vertical += move;
        }
        if (Gdx.input.isKeyPressed(DOWN)) {
            vertical -= move;
        }

    }

}