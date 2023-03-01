package infinityx.lunarhaze;


import com.badlogic.gdx.Screen;


/**
 * The primary controller class for the game.
 * This is the player mode class for running the game. In initializes all
 * of the other classes in the game and hooks them together.  It also provides the
 * basic game loop (update-draw).
 */
public class GameMode implements Screen {

    /**
     * Track the current state of the game for the update loop.
     */
    public enum GameState {
        // TODO
        // Game Over, Win, etc.
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    public void show() {

    }

    /**
     * Called when the screen should render itself.
     *
     * The game loop called by libGDX
     *
     * @param delta The time in seconds since the last render.
     */
    public void render(float delta) {

    }

    /**
     * @param width
     * @param height
     * @see ApplicationListener#resize(int, int)
     */
    public void resize(int width, int height) {

    }

    /**
     * @see ApplicationListener#pause()
     */
    public void pause() {

    }

    /**
     * @see ApplicationListener#resume()
     */
    public void resume() {

    }

    /**
     * Called when this screen is no longer the current screen for a {@link Game}.
     */
    public void hide() {

    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {

    }
}
