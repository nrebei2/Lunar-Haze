package infinityx.lunarhaze;

/**
 * Interface for either player or AI control
 */
public interface InputController {

    // Constants for the control codes
    // We would normally use an enum here, but Java enums do not bitmask nicely
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
    public static final int CONTROL_ATTACK 	   = 0x10;

    /**
     * Return the action of this ship (but do not process)
     *
     * The value returned must be some bitmasked combination of the static ints
     * in this class.  For example, if the ship moves left and fires, it returns
     * CONTROL_MOVE_LEFT | CONTROL_FIRE
     *
     * @return the action of this ship
     */
    public int getAction();
}
