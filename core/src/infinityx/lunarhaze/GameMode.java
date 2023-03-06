package infinityx.lunarhaze;


import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.util.ScreenObservable;


/**
 * The primary controller class for the game.
 * This is the player mode class for running the game. In initializes all
 * of the other classes in the game and hooks them together.  It also provides the
 * basic game loop (update-draw).
 */
public class GameMode extends ScreenObservable implements Screen {

    /**
     * Track the current state of the game for the update loop.
     */
    public enum GameState {
        // TODO add states
        // Game Over, Win, etc.
    }

    /** Owns the GameplayController */
    private GameplayController gameplayController;
    private GameCanvas canvas;

    /** Both may be null, requires assets retrieved from AssetManager */
    private JsonValue levelLayout;
    private LevelContainer levelContainer;

    // TODO: Maybe change to enum if there are not that many levels
    private int level;

    public GameMode(GameCanvas canvas) {
        this.canvas = canvas;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Gather the required assets for the given level.
     *
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * Creates the level container
     *
     * @param directory	Reference to global asset manager.
     * @param level the level to load
     */
    public void setupLevel(AssetDirectory directory, int level) {

    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    public void show() {
        // TODO: Create and hold LevelContainer through LevelParser
    }

    /**
     * Called when the screen should render itself.
     *
     * The game loop called by libGDX
     *
     * @param delta The time in seconds since the last render.
     */
    public void render(float delta) {
        levelContainer.drawLevel(canvas);
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
        // TODO: save player stats to json for persistence?
        // Though definitely save levels completed
    }

}
