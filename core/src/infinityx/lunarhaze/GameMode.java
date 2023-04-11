package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.GameplayController.GameState;
import infinityx.lunarhaze.GameplayController.Phase;
import infinityx.lunarhaze.graphics.CameraShake;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.graphics.GameCanvas.DrawPass;
import infinityx.lunarhaze.graphics.ScreenFlash;
import infinityx.util.ScreenObservable;


/**
 * The primary controller class for the game.
 * This is the player mode class for running the game. It provides the
 * basic game loop (update-draw).
 */
public class GameMode extends ScreenObservable implements Screen, InputProcessor {
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
     * Stealth background music
     */
    private Music stealth_background;

    /**
     * Battle background music
     */
    private Music battle_background;
    /**
     * Background texture for paused screen
     */
    private Texture pause_menu;
    /**
     * Background texture for paused review screen
     */
    private Texture review;
    /**
     * Background texture for paused review back button
     */
    private Texture back;
    /**
     * Texture for paused screen resume
     */
    private Texture pause_resume;
    /**
     * Texture for paused screen restart
     */
    private Texture pause_restart;
    /**
     * Texture for paused screen review
     */
    private Texture pause_review;
    /**
     * Texture for paused screen exit
     */
    private Texture pause_exit;
    /**
     * Texture for paused screen quit
     */
    private Texture pause_quit;
    /**
     * The current state of the resume button
     */
    private int pressResumeState;
    /**
     * The current state of the restart button
     */
    private int pressRestartState;
    /**
     * The current state of the review button
     */
    private int pressReviewState;
    /**
     * The current state of the exit button
     */
    private int pressExitState;
    /**
     * The current state of the quit button
     */
    private int pressQuitState;
    /**
     * The current state of the back button
     */
    private int pressBackState;
    /**
     * The x-coordinate of the center of the buttons
     */
    private int centerX;
    /**
     * The x-coordinate of the center of the back button
     */
    private int centerXBack;
    /**
     * The y-coordinate of the center of the buttons
     */
    private int centerYResume;
    private int centerYRestart;
    private int centerYReview;
    private int centerYExit;
    private int centerYQuit;
    private int centerYBack;
    private static final float BUTTON_SCALE = 0.25f;
    /**
     * Ratio of play height from bottom
     */
    private static final float BACK_HEIGHT_RATIO = 0.45f;
    /**
     * Scaling factor for when the student changes the resolution.
     */
    private float scale;
    /**
     * Standard window size (for scaling)
     */
    private static final int STANDARD_WIDTH = 800;
    /**
     * Standard window height (for scaling)
     */
    private static final int STANDARD_HEIGHT = 700;
    /**
     * The height of the canvas window (necessary since sprite origin != screen origin)
     */
    private int heightY;

    /**
     * @return true if the player is ready to resume the game
     */
    public boolean isReadyResume() {
        return pressResumeState == 2;
    }
    /**
     * @return true if the player is ready to restart the game
     */
    public boolean isReadyRestart() {
        return pressRestartState == 2;
    }
    /**
     * @return true if the player is ready to review the setting
     */
    public boolean isReadyReview() {
        return pressReviewState == 2;
    }
    /**
     * @return true if the player is ready to exit to the menu
     */
    public boolean isReadyExit() {
        return pressExitState == 2;
    }
    /**
     * @return true if the player is ready to quit the game
     */
    public boolean isReadyQuit() {
        return pressQuitState == 2;
    }
    /**
     * @return true if the player is ready to quit the game
     */
    public boolean isReadyBack() {
        return pressBackState == 2;
    }




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

    public GameplayController getGameplayController(){
        return gameplayController;
    }

    public void setGameplayController(GameplayController gc){
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
        pause_menu = directory.getEntry("pause", Texture.class);
        review = directory.getEntry("review", Texture.class);
        back = directory.getEntry("back", Texture.class);
        pause_resume = directory.getEntry("pause-resume", Texture.class);
        pause_restart = directory.getEntry("pause-restart", Texture.class);
        pause_review = directory.getEntry("pause-review", Texture.class);
        pause_exit = directory.getEntry("pause-exit", Texture.class);
        pause_quit = directory.getEntry("pause-quit", Texture.class);

//        win_sound = directory.getEntry("level-passed", Sound.class);
        System.out.println("gatherAssets called");
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
            case PAUSED:
                if(isReadyResume()){
                    gameplayController.setState(GameState.PLAY);
                    pressResumeState = 0;
                }else if(isReadyRestart()){
                    gameplayController.setState(GameState.PLAY);
                    setupLevel();
                    pressRestartState = 0;
                }else if(isReadyBack()){
                    pressBackState = 0;
                    pressReviewState = 0;
                    gameplayController.setState(GameState.PAUSED);
                }else if(isReadyQuit()){
                    Gdx.app.exit();
                }
                break;
            case PLAY:
                if(inputController.didExit()){
                    gameplayController.setState(GameState.PAUSED);
                }
                switch (gameplayController.getPhase()){
                    case STEALTH:
                    case TRANSITION:
                        if (!stealth_background.isPlaying()) {
                            //stealth_background.setLooping(true);
                            //stealth_background.play();
                        }
                    case BATTLE:
                        stealth_background.stop();
                        if (!battle_background.isPlaying()) {
                            //battle_background.setLooping(true);
                            //battle_background.play();
                        }
                    case ALLOCATE:
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
        System.out.println("GameMode setupLevel is callsed");
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
            case PAUSED:
                if(pressReviewState != 2) {
                    canvas.begin(GameCanvas.DrawPass.SPRITE);
                    Color alphaTint = Color.WHITE;
                    canvas.drawOverlay(pause_menu, alphaTint, true);
                    Color tintResume = (pressResumeState == 1 ? Color.BLACK : Color.WHITE);
                    canvas.draw(pause_resume, tintResume, pause_resume.getWidth() / 2,
                            pause_resume.getHeight() / 2,
                            centerX, centerYResume, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
                    Color tintRestart = (pressRestartState == 1 ? Color.BLACK : Color.WHITE);
                    canvas.draw(pause_restart, tintRestart, pause_restart.getWidth() / 2,
                            pause_restart.getHeight() / 2,
                            centerX, centerYRestart, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
                    Color tintReview = (pressReviewState == 1 ? Color.BLACK : Color.WHITE);
                    canvas.draw(pause_review, tintReview, pause_review.getWidth() / 2,
                            pause_review.getHeight() / 2,
                            centerX, centerYReview, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
                    Color tintExit = (pressExitState == 1 ? Color.BLACK : Color.WHITE);
                    canvas.draw(pause_exit, tintExit, pause_exit.getWidth() / 2,
                            pause_exit.getHeight() / 2,
                            centerX, centerYExit, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
                    Color tintQuit = (pressQuitState == 1 ? Color.BLACK : Color.WHITE);
                    canvas.draw(pause_quit, tintQuit, pause_quit.getWidth() / 2,
                            pause_quit.getHeight() / 2,
                            centerX, centerYQuit, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
                    canvas.end();
                }else{
                    canvas.begin(GameCanvas.DrawPass.SPRITE);
                    Color alphaTint = Color.WHITE;
                    canvas.drawOverlay(review, alphaTint, true);
                    Color color = new Color(45.0f/255.0f, 74.0f/255.0f, 133.0f/255.0f, 1.0f);
                    Color tintBack = (pressBackState == 1 ? color : Color.WHITE);
                    canvas.draw(back, tintBack, back.getWidth() / 2, back.getHeight() / 2,
                            centerXBack, centerYBack, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
                    canvas.end();
                }
            case PLAY:
                Phase phase = gameplayController.getPhase();
                uiRender.drawUI(canvas, levelContainer, phase, gameplayController);
                break;
        }
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    public void show() {
//        setupLevel();
        pressResumeState = 0;
        pressRestartState = 0;
        pressReviewState = 0;
        pressExitState = 0;
        pressQuitState = 0;
        pressBackState = 0;
        Gdx.input.setInputProcessor(this);
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

        if (isReadyExit() && observer != null) {
            observer.exitScreen(this, GO_MENU);
        }
//        if (isReadyQuit() && observer != null) {
//            observer.exitScreen(this, GO_EXIT);
//        }

        if (gameplayController.getPhase() == Phase.ALLOCATE && observer != null){
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
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        centerYResume = (int) (BACK_HEIGHT_RATIO * height);
        centerYRestart = centerYResume - (int)(0.6 * pause_resume.getHeight());
        centerYReview = centerYRestart - (int)(0.6 * pause_resume.getHeight());
        centerYExit = centerYReview - (int)(0.6 * pause_resume.getHeight());
        centerYQuit = centerYExit - (int)(0.6 * pause_resume.getHeight());
        centerX = width / 2;
        heightY = height;
        centerYBack = (int) (0.9f * height);
        centerXBack = width / 16;

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


    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pause_resume == null || pressResumeState == 2) {
            return true;
        }
        // Flip to match graphics coordinates
        screenY = heightY - screenY;

        //System.out.printf("%d, %d", screenX, screenY);

        // TODO: Fix scaling
        // Button are rectangles with same x-coordinate and shapes.
        float x = BUTTON_SCALE * scale * pause_resume.getWidth()/2;
        float distX = Math.abs(screenX - centerX);
        float y = BUTTON_SCALE * scale * pause_resume.getHeight()/2;

        float distYResume = Math.abs(screenY - centerYResume);
        if(distX < x && distYResume <y){
            pressResumeState =1;
        }
        float distYRestart = Math.abs(screenY - centerYRestart);
        if(distX < x && distYRestart <y){
            pressRestartState =1;
        }
        float distYReview = Math.abs(screenY - centerYReview);
        if(distX < x && distYReview <y){
            pressReviewState =1;
        }
        float distYExit = Math.abs(screenY - centerYExit);
        if(distX < x && distYExit <y){
            pressExitState =1;
        }
        float distYQuit = Math.abs(screenY - centerYQuit);
        if(distX < x && distYQuit <y){
            pressQuitState =1;
        }
        float radiusBack = BUTTON_SCALE * scale * back.getWidth() / 2.0f;
        float distBack = (screenX - centerXBack) * (screenX - centerXBack) + (screenY - centerYBack) * (screenY - centerYBack);
        if (distBack < radiusBack * radiusBack) {
            pressBackState = 1;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressResumeState == 1) {
            pressResumeState = 2;
            return false;
        }
        if (pressRestartState == 1) {
            pressRestartState = 2;
            return false;
        }
        if (pressReviewState == 1) {
            pressReviewState = 2;
            return false;
        }
        if (pressExitState == 1) {
            pressExitState = 2;
            return false;
        }
        if (pressQuitState == 1) {
            pressQuitState = 2;
            return false;
        }
        if (pressBackState == 1) {
            pressBackState = 2;
            return false;
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
