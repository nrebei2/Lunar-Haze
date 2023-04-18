package infinityx.lunarhaze.screens;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.GameplayController;
import infinityx.lunarhaze.GameplayController.GameState;
import infinityx.lunarhaze.GameplayController.Phase;
import infinityx.lunarhaze.InputController;
import infinityx.lunarhaze.LevelContainer;
import infinityx.lunarhaze.LevelParser;
import infinityx.lunarhaze.UIRender;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.ScreenObservable;


/**
 * The primary controller class for the game.
 * This is the player mode class for running the game. It provides the
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
    public final static int GO_PAUSE = 0;

    /**
     * User requested to go to allocate
     */
    public final static int GO_ALLOCATE = 1;

    /**
     * Owns the GameplayController
     */
    private GameplayController gameplayController;

    /**
     * Owns the UIRender
     */
    private UIRender uiRender;

    /**
     * Lobby and pause background music
     */
    private Music lobby_background;

    /**
     * Stealth background music
     */
    private Music stealth_background;

    /**
     * Battle background music
     */
    private Music battle_background;

    /**
     * Whether lobby background is set
     */
    private boolean lobby_playing = false;

    /**
     * Whether stealth background is set
     */
    private boolean stealth_playing = false;

    /**
     * Whether battle background is set
     */
    private boolean battle_playing = false;

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

    protected BitmapFont UIFont_large;

    protected BitmapFont UIFont_small;

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

    public GameplayController getGameplayController() {
        return gameplayController;
    }

    public void setGameplayController(GameplayController gc) {
        gameplayController = gc;
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
        UIFont_large = directory.getEntry("libre-large", BitmapFont.class);
        UIFont_small = directory.getEntry("libre-small", BitmapFont.class);
        uiRender = new UIRender(UIFont_large, UIFont_small, directory);
        stealth_background = directory.getEntry("stealthBackground", Music.class);
        battle_background = directory.getEntry("battleBackground", Music.class);
        lobby_background = directory.getEntry("lobbyBackground", Music.class);

//        win_sound = directory.getEntry("level-passed", Sound.class);
    }

    public void updateMusic(float delta) {
        switch (gameplayController.getState()) {
            case OVER:
            case WIN:
            case PAUSED:
                if (stealth_playing) {
                    stealth_background.stop();
                    stealth_playing = false;
                } else if (battle_playing) {
                    battle_background.stop();
                    stealth_playing = false;
                }
                if (!lobby_playing) {
                    lobby_background.setLooping(true);
                    lobby_background.play();
                    lobby_playing = true;
                }
                break;
            case PLAY:
                if (lobby_playing) {
                    lobby_background.stop();
                    lobby_playing = false;
                }
                switch (gameplayController.getPhase()) {
                    case STEALTH:
                    case TRANSITION:
                    case ALLOCATE:
                        if (!stealth_playing) {
                            stealth_background.setLooping(true);
                            stealth_background.play();
                            stealth_playing = true;
                        }
                    case BATTLE:
                        stealth_background.stop();
                        if (!battle_playing) {
                            battle_background.setLooping(true);
                            battle_background.play();
                            battle_playing = true;
                        }
                }
                break;
            default:
                break;
        }
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
        updateMusic(delta);

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
                switch (gameplayController.getPhase()) {
                    case STEALTH:
                    case TRANSITION:
                    case ALLOCATE:
                    case BATTLE:
                }
                play(delta);
                break;
            default:
                break;
        }
    }

    /**
     * Initializes the levelContainer given level.
     */
    public void setupLevel() {
        LevelParser ps = LevelParser.LevelParser();
        levelContainer = ps.loadLevel(directory, levelFormat.get(String.valueOf(level)));
        gameplayController.start(levelContainer, levelFormat.get(String.valueOf(level)));
    }

    /**
     * This method processes a single step in the game loop.
     *
     * @param delta Number of seconds since last animation frame
     */
    protected void play(float delta) {
        levelContainer.getWorld().step(delta, 6, 2);
        gameplayController.resolveActions(inputController, delta);
    }

    /**
     * Draw the game mode.
     */
    public void draw(float delta) {
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
                canvas.begin(GameCanvas.DrawPass.SPRITE);
                canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
                canvas.end();
                break;
            case OVER:
                displayFont.setColor(Color.RED);
                canvas.begin(GameCanvas.DrawPass.SPRITE); // DO NOT SCALE
                canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
                canvas.end();
                break;
            case PLAY:
                Phase phase = gameplayController.getPhase();
                uiRender.drawUI(canvas, levelContainer, phase, gameplayController, delta);
                break;
        }
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    public void show() {
//        setupLevel();
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
            observer.exitScreen(this, GO_PAUSE);
        }

        if (gameplayController.getPhase() == Phase.ALLOCATE && observer != null) {
            observer.exitScreen(this, GO_ALLOCATE);
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
