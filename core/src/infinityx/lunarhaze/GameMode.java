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

    private GameplayController.GameState gameState;


    public GameMode(GameCanvas canvas) {
        this.canvas = canvas;
        // Create the controllers:
        inputController = new InputController();
        gameplayController = new GameplayController();
        gameState = GameplayController.GameState.PLAY;
    }

    /**
     * Set the current level
     *
     * @param level
     */
    public void setLevel(int level) {
        //TODO DELETE ONE OF THESE
        this.level = level;
        // must reload level container and controllers
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

        // Test whether to reset the game.
        switch (gameState) {
            case OVER:
                // TODO: make seperate screen
            case WIN:
                if (inputController.didReset()) {
                    gameplayController.reset();
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
     * Initializes the levelContainer given the set level
     */
    private void setupLevel() {
        LevelParser ps = LevelParser.LevelParser();
        levelContainer = ps.loadLevel(directory, levelFormat.get(String.valueOf(level)));
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
            case PLAY:
                displayFont.setColor(Color.YELLOW);
                canvas.begin(); // DO NOT SCALE
                canvas.drawLightBar(BAR_WIDTH,
                        BAR_HEIGHT, gameplayController.getPlayerController().getPlayerLight());
                canvas.drawEnemyHpBars(BAR_WIDTH/4.0f, BAR_HEIGHT/4.0f, levelContainer.getEnemies());
                canvas.drawHpBar(BAR_WIDTH,
                        BAR_HEIGHT, gameplayController.getPlayerController().getPlayerHp());
                canvas.drawStealthBar(BAR_WIDTH,
                        BAR_HEIGHT, gameplayController.getPlayerController().getPlayerStealth());
                canvas.end();
                break;
        }
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    public void show() {
        setupLevel();
        gameplayController.start(levelContainer);
        gameState = GameplayController.GameState.PLAY;
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

        // TODO: for convenience,
        if (Gdx.input.isKeyPressed(Input.Keys.PERIOD)) {
            gameState = GameplayController.GameState.WIN;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.COMMA)) {
            gameState = GameplayController.GameState.OVER;
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
