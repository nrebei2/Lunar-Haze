package infinityx.util;

import com.badlogic.gdx.Screen;

/**
 * Registers a callback function for a Screen's exit request
 *
 * Used for Game classes to communicate with their Screens
 */
public interface ScreenObserver {

    /**
     * The given screen has made a request to exit.
     *
     * The value exitCode can be used to implement menu options.
     *
     * @param screen   The screen requesting to exit
     * @param exitCode The state of the screen upon exit
     */
    public void exitScreen(Screen screen, int exitCode);
}
