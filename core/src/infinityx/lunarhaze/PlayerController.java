package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Array;

public class PlayerController {

    /** Constructs a PlayerController with keyboard control enabled.
     *  Currently, there is no support for Xbox controller.
     */
    public PlayerController() {

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
        return 0;
    }

 }
