package infinityx.lunarhaze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.InputController;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.ScreenObservable;

public class PauseMode extends ScreenObservable implements Screen, InputProcessor {
    /**
     * Whether or not this player mode is still active
     */
    private boolean active;
    /**
     * User requested to go to menu
     */
    public final static int GO_RESUME = 0;
    /**
     * User requested to go to menu
     */
    public final static int GO_RESTART = 1;
    /**
     * User requested to go to menu
     */
    public final static int GO_SETTING = 2;
    /**
     * User requested to go to menu
     */
    public final static int GO_MENU = 3;
    /**
     * User requested to go to menu
     */
    public final static int GO_EXIT = 4;
    /**
     * Need an ongoing reference to the asset directory
     */
    protected AssetDirectory directory;
    /**
     * Background texture for paused screen
     */
    private Texture pause_menu;
    /**
     * Background texture for paused review screen
     */
    private Texture review;
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
     * Reference to drawing context to display graphics (VIEW CLASS)
     */
    private GameCanvas canvas;
    /**
     * Reads input from keyboard or game pad (CONTROLLER CLASS)
     */
    private InputController inputController;

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
     * Creates a new Menu
     *
     * @param canvas The game canvas to draw to
     */
    public PauseMode(GameCanvas canvas) {
        this.canvas = canvas;
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
        pause_menu = directory.getEntry("pause", Texture.class);
        review = directory.getEntry("review", Texture.class);
        pause_resume = directory.getEntry("pause-resume", Texture.class);
        pause_restart = directory.getEntry("pause-restart", Texture.class);
        pause_review = directory.getEntry("pause-review", Texture.class);
        pause_exit = directory.getEntry("pause-exit", Texture.class);
        pause_quit = directory.getEntry("pause-quit", Texture.class);
    }

    private void update(float delta){

    }

    /**
     * Draw the status of this player mode.
     * <p>
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
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
    }

    @Override
    public void show() {
        active = true;
        pressResumeState = 0;
        pressRestartState = 0;
        pressReviewState = 0;
        pressExitState = 0;
        pressQuitState = 0;
        pressBackState = 0;
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();
            if (isReadyResume() && observer != null) {
                observer.exitScreen(this, GO_RESUME);
            }
            if(isReadyRestart()&& observer !=null){
                observer.exitScreen(this, GO_RESTART);
            }
            if(isReadyReview()&& observer !=null){
                observer.exitScreen(this, GO_SETTING);
            }
            if (isReadyExit() && observer != null) {
                observer.exitScreen(this, GO_MENU);
            }
            if (isReadyQuit() && observer != null) {
                observer.exitScreen(this, GO_EXIT);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        centerYResume = (int) (BACK_HEIGHT_RATIO * height);
        centerYRestart = centerYResume - (int) (0.6 * pause_resume.getHeight());
        centerYReview = centerYRestart - (int) (0.6 * pause_resume.getHeight());
        centerYExit = centerYReview - (int) (0.6 * pause_resume.getHeight());
        centerYQuit = centerYExit - (int) (0.6 * pause_resume.getHeight());
        centerX = width / 2;
        heightY = height;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        active = false;

    }

    @Override
    public void dispose() {

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

        // TODO: Fix scaling
        // Button are rectangles with same x-coordinate and shapes.
        float x = BUTTON_SCALE * scale * pause_resume.getWidth() / 2;
        float distX = Math.abs(screenX - centerX);
        float y = BUTTON_SCALE * scale * pause_resume.getHeight() / 2;

        float distYResume = Math.abs(screenY - centerYResume);
        if (distX < x && distYResume < y) {
            pressResumeState = 1;
        }
        float distYRestart = Math.abs(screenY - centerYRestart);
        if (distX < x && distYRestart < y) {
            pressRestartState = 1;
        }
        float distYReview = Math.abs(screenY - centerYReview);
        if (distX < x && distYReview < y) {
            pressReviewState = 1;
        }
        float distYExit = Math.abs(screenY - centerYExit);
        if (distX < x && distYExit < y) {
            pressExitState = 1;
        }
        float distYQuit = Math.abs(screenY - centerYQuit);
        if (distX < x && distYQuit < y) {
            pressQuitState = 1;
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
