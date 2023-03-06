package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Array;

public class PlayerController{

    /** Do not do anything */
    public static final int CONTROL_NO_ACTION  = 0x00;
    /** Move the ship to the left */
    public static final int CONTROL_MOVE_LEFT  = 0x01;
    /** Move the ship to the right */
    public static final int CONTROL_MOVE_RIGHT = 0x02;
    /** Move the ship to the up */
    public static final int CONTROL_MOVE_UP    = 0x04;
    /** Move the ship to the down */
    public static final int CONTROL_MOVE_DOWN  = 0x08;
    /** Fire the ship weapon */
    public static final int CONTROL_FIRE 	   = 0x10;

    /** Whether to enable keyboard control (as opposed to X-Box) */
    private boolean keyboard;

    /** The XBox Controller hooked to this machine */
    private XBoxController xbox;

    /** Constructs a PlayerController with keyboard control enabled.
     *  Currently, there is no support for Xbox controller.
     */
    public PlayerController() {
        keyboard = true;
        xbox = null;

        // If we have a game-pad for id, then use it.
        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get(0);
            keyboard = false;
        }
    }

    /**
     * Return the action of the player (but do not process)
     * Actions that the player is allowed to take:
     *      Move
     *      Attack
     *      Collect Trap
     *      Use Trap
     **/
    public int getAction() {
        int code = CONTROL_NO_ACTION;

        if (keyboard) {
            if (Gdx.input.isKeyPressed(Keys.UP))    code |= CONTROL_MOVE_UP;
            if (Gdx.input.isKeyPressed(Keys.LEFT))  code |= CONTROL_MOVE_LEFT;
            if (Gdx.input.isKeyPressed(Keys.DOWN))  code |= CONTROL_MOVE_DOWN;
            if (Gdx.input.isKeyPressed(Keys.RIGHT)) code |= CONTROL_MOVE_RIGHT;
            if (Gdx.input.isKeyPressed(Keys.SPACE)) code |= CONTROL_FIRE;
        } else {
            double ANALOG_THRESH  = 0.3;
            double TRIGGER_THRESH = -0.75;
            if (xbox.getLeftY() < -ANALOG_THRESH)	code |= CONTROL_MOVE_UP;
            if (xbox.getLeftX() < -ANALOG_THRESH)  	code |= CONTROL_MOVE_LEFT;
            if (xbox.getLeftY() > ANALOG_THRESH)   code |= CONTROL_MOVE_DOWN;
            if (xbox.getLeftX() > ANALOG_THRESH) 	code |= CONTROL_MOVE_RIGHT;
            if (xbox.getRightTrigger() > TRIGGER_THRESH) code |= CONTROL_FIRE;
        }

        // Prevent diagonal movement.
        if ((code & CONTROL_MOVE_UP) != 0 && (code & CONTROL_MOVE_LEFT) != 0) {
            code ^= CONTROL_MOVE_UP;
        }

        if ((code & CONTROL_MOVE_UP) != 0 && (code & CONTROL_MOVE_RIGHT) != 0) {
            code ^= CONTROL_MOVE_RIGHT;
        }

        if ((code & CONTROL_MOVE_DOWN) != 0 && (code & CONTROL_MOVE_RIGHT) != 0) {
            code ^= CONTROL_MOVE_DOWN;
        }

        if ((code & CONTROL_MOVE_DOWN) != 0 && (code & CONTROL_MOVE_LEFT) != 0) {
            code ^= CONTROL_MOVE_LEFT;
        }

        // Cancel out conflicting movements.
        if ((code & CONTROL_MOVE_LEFT) != 0 && (code & CONTROL_MOVE_RIGHT) != 0) {
            code ^= (CONTROL_MOVE_LEFT | CONTROL_MOVE_RIGHT);
        }

        if ((code & CONTROL_MOVE_UP) != 0 && (code & CONTROL_MOVE_DOWN) != 0) {
            code ^= (CONTROL_MOVE_UP | CONTROL_MOVE_DOWN);
        }

        return code;
    }

 }
