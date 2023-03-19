package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.GameplayController.Phase;
import infinityx.util.ScreenObservable;


/**
 * The primary controller class for the game.
 * This is the player mode class for running the game. In initializes all
 * of the other classes in the game and hooks them together.  It also provides the
 * basic game loop (update-draw).
 */
public class GameMode extends ScreenObservable implements Screen {
    /**
     * Need an ongoing reference to the asset directory
     */
    protected AssetDirectory directory;
    // Exit codes
    /**
     * User requested to go to menu
     */
    public final static int GO_MENU = 0;

    /**
     * Width of the HP bar
     */
    private final static float BAR_WIDTH = 300f;
    /**
     * Height of the HP bar
     */
    private final static float BAR_HEIGHT = 40.0f;

    /**
     * Owns the GameplayController
     */
    private GameplayController gameplayController;

    private UIRender uiRender;
    /**
     * Reference to drawing context to display graphics (VIEW CLASS)
     */
    private GameCanvas canvas;
    /**
     * Reads input from keyboard or game pad (CONTROLLER CLASS)
     */
    private InputController inputController;
    /**
     * Contains level details! May be null.
     */
    private LevelContainer levelContainer;
    /**
     * Constants for level initialization
     */
    private JsonValue levelFormat;
    /**
     * The font for giving messages to the player
     */
    protected BitmapFont displayFont;
    /**
     * Variable to track total time played in milliseconds (SIMPLE FIELDS)
     * TODO: use this for timer
     */
    private float totalTime = 0;

    // TODO: Maybe change to enum if there are not that many levels, or string maybe?
    /**
     * Current level
     */
    private int level;

    public GameMode(GameCanvas canvas) {
        this.canvas = canvas;
        // Create the controllers:
        inputController = new InputController();
        gameplayController = new GameplayController();
    }

    /**
     * Set the current level
     *
     * @param level
     */
    public void setLevel(int level) {
        //TODO DELETE ONE OF THESE
        this.level = level;
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
        this.directory = directory;

        inputController.loadConstants(directory);
        levelFormat = directory.getEntry("levels", JsonValue.class);
        displayFont = directory.getEntry("retro", BitmapFont.class);
        uiRender = new UIRender(displayFont, directory);
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
    public void update(float delta) {
        // Process the game input
        inputController.readKeyboard();

        switch (gameplayController.getState()) {
            case OVER:
                // TODO: make seperate screen
            case WIN:
                if (inputController.didReset()) {
                    setupLevel();
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
     * Initializes the levelContainer given level.
     */
    private void setupLevel() {
        LevelParser ps = LevelParser.LevelParser();
        levelContainer = ps.loadLevel(directory, levelFormat.get(String.valueOf(level)));
        gameplayController.start(levelContainer);
    }

    /**
     * This method processes a single step in the game loop.
     *
     * @param delta Number of seconds since last animation frame
     */
    protected void play(float delta) {
        levelContainer.getWorld().step(delta, 6, 2);
        gameplayController.resolveActions(inputController, delta);

        totalTime += (delta * 1000); // Seconds to milliseconds
    }

    /**
     * Draw the game mode.
     */
    public void draw(float delta) {
        canvas.clear();

        // Puts player at center of canvas
        levelContainer.setViewTranslation(
                -canvas.WorldToScreenX(levelContainer.getPlayer().getPosition().x) + canvas.getWidth() / 2,
                -canvas.WorldToScreenY(levelContainer.getPlayer().getPosition().y) + canvas.getHeight() / 2
        );
        // Draw the level
        levelContainer.drawLevel(canvas);

        switch (gameplayController.getState()) {
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
            case PLAY:
                Phase phase = gameplayController.getPhase();
                uiRender.drawUI(canvas, levelContainer, phase);
                break;
        }
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    public void show() {
        setupLevel();
    }

    /**
     * Called when the screen should render itself.
     * <p>
     * The main game loop called by libGDX
     *
     * @param delta The time in seconds since the last render.
     */
    public void render(float delta) {
        update(delta);
        draw(delta);

        if (inputController.didExit() && observer != null) {
            observer.exitScreen(this, GO_MENU);
        }

    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        // TODO: save player stats to json for persistence?
        // Though definitely save levels completed
        inputController = null;
        gameplayController = null;
        //physicsController = null;
        canvas = null;
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


}
