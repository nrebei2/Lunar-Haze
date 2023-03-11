package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

    // Exit codes
    /**
     * User requested to go to menu
     */
    public final static int GO_MENU = 0;

    /**
     * Track the current state of the game for the update loop.
     */
    public enum GameState {
        /**
         * Before the game has started
         */
        INTRO,
        /**
         * While we are playing the game
         */
        PLAY,
        /**
         * The werewolf is dead
         */
        OVER,
        /**
         * The werewolf has prevailed!
         */
        WIN
    }

    /**
     * Owns the GameplayController
     */
    private GameplayController gameplayController;
    /**
     * Reference to drawing context to display graphics (VIEW CLASS)
     */
    private GameCanvas canvas;
    /**
     * Reads input from keyboard or game pad (CONTROLLER CLASS)
     */
    private InputController inputController;
    /**
     * Handle collision and physics (CONTROLLER CLASS)
     */
    private CollisionController physicsController;
    /**
     * Contains level details! May be null.
     */
    private LevelContainer levelContainer;
    /**
     * Constants for level initialization
     */
    private JsonValue constants;
    /**
     * The font for giving messages to the player
     */
    protected BitmapFont displayFont;
    /**
     * Variable to track total time played in milliseconds (SIMPLE FIELDS)
     */
    private float totalTime = 0;
    /**
     * Whether or not this player mode is still active
     */
    private boolean active;
    /**
     * Variable to track the game state (SIMPLE FIELDS)
     */
    private GameState gameState;

    // TODO: Maybe change to enum if there are not that many levels, or string maybe?
    /**
     * Current level
     */
    private int level;

    public GameMode(GameCanvas canvas) {
        this.canvas = canvas;
        active = false;
        gameState = GameState.INTRO;
        // Create the controllers:
        inputController = new InputController();
        gameplayController = new GameplayController();
        physicsController = new CollisionController(canvas.getWidth(), canvas.getHeight(), levelContainer);
    }

    /**
     * Set the current level
     *
     * @param level
     */
    public void setLevel(int level) {
        this.level = level;
        // must reload level container and controllers
        this.gameState = GameState.INTRO;
    }

    /**
     * Gather the required assets.
     * <p>
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        LevelParser ps = LevelParser.LevelParser();
        ps.loadTextures(directory);
        constants = directory.getEntry("levels", JsonValue.class);
        displayFont = directory.getEntry("retro", BitmapFont.class);
    }

    /**
     * Update the game state.
     * <p>
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
                setupLevel();
                gameplayController.start(levelContainer);
                gameState = GameState.PLAY;
                break;
            case OVER:
            case WIN:
                if (inputController.didReset()) {
                    gameState = GameState.INTRO;
                    gameplayController.reset();
                } else {
                    play(delta);
                }
                break;
            case PLAY:
                play(delta);
                if (gameplayController.isGameWon()) {
                    gameState = GameState.WIN;
                } else if (gameplayController.isGameLost()) {
                    gameState = GameState.OVER;
                }
                break;
            default:
                break;
        }
    }

    /**
     * Initializes the levelContainer given the set level
     */
    private void setupLevel() {
        LevelParser ps = LevelParser.LevelParser();
        levelContainer = ps.loadData(constants.get(String.valueOf(level)));
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
        levelContainer.getWorld().step(delta, 6, 2);
        gameplayController.resolveActions(inputController, delta);

        // Check for collisions
        totalTime += (delta * 1000); // Seconds to milliseconds
        //physicsController.processCollisions(gameplayController.getObjects());
        // Clean up destroyed objects
        // gameplayController.garbageCollect();
    }

    /**
     * Draw the status of this player mode.
     * <p>
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw(float delta) {
        canvas.clear();

        // Draw the level
        levelContainer.drawLevel(canvas);

        switch (gameState) {
            case WIN:
                displayFont.setColor(Color.YELLOW);
                canvas.begin();
                canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
                canvas.end();
                break;
            case OVER:
                displayFont.setColor(Color.RED);
                canvas.begin(); // DO NOT SCALE
                canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
                canvas.end();
                break;
        }
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    public void show() {
        active = true;
        gameState = GameState.INTRO;
    }

    /**
     * Called when the screen should render itself.
     * <p>
     * The game loop called by libGDX
     *
     * @param delta The time in seconds since the last render.
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw(delta);

            if (inputController.didExit() && observer != null) {
                observer.exitScreen(this, GO_MENU);
            }

            // for convenience
            if (Gdx.input.isKeyPressed(Input.Keys.PERIOD)) {
                gameState = GameState.WIN;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.COMMA)) {
                gameState = GameState.OVER;
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
     * Called when this screen should release all resources.
     */
    public void dispose() {
        // TODO: save player stats to json for persistence?
        // Though definitely save levels completed
        inputController = null;
        gameplayController = null;
        physicsController = null;
        canvas = null;
    }

}
