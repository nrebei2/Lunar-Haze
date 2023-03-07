package infinityx.lunarhaze;


import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.util.ScreenObservable;
import infinityx.util.ScreenObserver;



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
        // TODO add state
            /** Before the game has started */
            INTRO,
            /** While we are playing the game */
            PLAY,
            /** When the werewolf is dead */
            OVER
    }

    /** Owns the GameplayController */
    private GameplayController gameplayController;
    /** Reference to drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;
    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;
    /** Handle collision and physics (CONTROLLER CLASS) */
    private CollisionController physicsController;
    /** Listener that will update the player mode when we are done */
    private ScreenObserver observer;
    /** Both may be null, requires assets retrieved from AssetManager */
    private JsonValue levelLayout;
    /** Contains level details! */
    private LevelContainer levelContainer;
    /** Variable to track total time played in milliseconds (SIMPLE FIELDS) */
    private float totalTime = 0;
    /** Whether or not this player mode is still active */
    private boolean active;
    /** Variable to track the game state (SIMPLE FIELDS) */
    private GameState gameState;


    // TODO: Maybe change to enum if there are not that many levels
    private int level;

    public GameMode(GameCanvas canvas) {
        this.canvas = canvas;
        active = false;
        gameState = GameState.INTRO;
        // Create the controllers.
        inputController = new InputController();
        gameplayController = new GameplayController();
        physicsController = new CollisionController(canvas.getWidth(), canvas.getHeight(),levelContainer);
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
        LevelParser ps = new LevelParser();
        levelContainer = ps.loadData(directory, level);
        gameState = GameState.INTRO;
    }

    /**
     * Update the game state.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        // Process the game input
        inputController.readKeyboard();

        // Test whether to reset the game.
        switch (gameState) {
            case INTRO:
                gameplayController.start(levelContainer);
                gameState = GameState.PLAY;
                break;
            case OVER:
                if (inputController.didReset()) {
                    gameState = GameState.PLAY;
                    gameplayController.reset();
                    gameplayController.start(levelContainer);
                } else {
                    play(delta);
                }
                break;
            case PLAY:
                play(delta);
                break;
            default:
                break;
        }
    }

    /**
     * This method processes a single step in the game loop.
     *
     * @param delta Number of seconds since last animation frame
     */
    protected void play(float delta) {

        // if no player is alive, declare game over
        if (!gameplayController.isAlive()) {
            gameState = GameState.OVER;
        }

        // Update objects.
        gameplayController.resolveActions(inputController,delta);

        // Check for collisions
        totalTime += (delta*1000); // Seconds to milliseconds
        physicsController.processCollisions(gameplayController.getObjects());

        // Clean up destroyed objects
       // gameplayController.garbageCollect();
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw(float delta) {
        canvas.clear();
        canvas.begin();

        // Draw the game objects
        levelContainer.drawLevel(canvas);

        if (gameState == GameState.OVER) {
            //TODO
        }
        // Flush information to the graphic buffer.
        canvas.end();
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    public void show() {
        active = true;
    }

    /**
     * Called when the screen should render itself.
     *
     * The game loop called by libGDX
     *
     * @param delta The time in seconds since the last render.
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw(delta);
            if (inputController.didExit() && observer != null) {
                observer.exitScreen(this, 0);
            }
        }
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
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenObservable(ScreenObserver observer) {
        this.observer = observer;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        // TODO: save player stats to json for persistence?
        // Though definitely save levels completed
        inputController = null;
        gameplayController = null;
        physicsController  = null;
        canvas = null;
    }

}
