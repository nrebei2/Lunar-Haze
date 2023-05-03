package infinityx.lunarhaze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import infinityx.assets.AssetDirectory;
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
     * User requested to go to menu
     */
    public final static int GO_EDITOR = 5;

    /**
     * Need an ongoing reference to the asset directory
     */
    protected AssetDirectory directory;
    /**
     * Background texture for paused screen
     */
    private Texture pause_menu;
    /**
     * Game logo texture for paused screen
     */
    private Texture pause_logo;
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
     * Texture for goto editor button
     */
    private Texture pause_editor;
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
     * The current state of the goto editor button
     */
    private int pressEditorState;
    /**
     * The x-coordinate of the center of the buttons
     */
    private int centerX;
    /**
     * The y-coordinate of the center of the buttons
     */
    private int centerYResume, centerYRestart, centerYReview, centerYExit, centerYQuit, centerYEditor;

    /**
     * Scale for buttons
     */
    private static final float BUTTON_SCALE = 0.25f;
    /**
     * Ratio of play height from bottom
     */
    private static final float LOGO_HEIGHT_RATIO = 0.7f;
    /**
     * Ratio of play height from bottom
     */
    private static final float RESUME_HEIGHT_RATIO = 0.42f;
    /**
     * Ratio of play height from bottom
     */
    private static final float RESTART_HEIGHT_RATIO = 0.35f;
    /**
     * Ratio of setting height from bottom
     */
    private static final float REVIEW_HEIGHT_RATIO = 0.28f;
    /**
     * Ratio of about us height from bottom
     */
    private static final float EXIT_HEIGHT_RATIO = 0.21f;
    /**
     * Ratio of about us height from bottom
     */
    private static final float QUIT_HEIGHT_RATIO = 0.14f;
    /**
     * Ratio of about us height from bottom
     */
    private static final float EDITOR_HEIGHT_RATIO = 0.07f;
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
     * @return true if the player is ready to quit the game
     */
    public boolean isReadyEditor() {
        return pressEditorState == 2;
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
        pause_logo = directory.getEntry("pause-logo", Texture.class);
        pause_resume = directory.getEntry("pause-resume", Texture.class);
        pause_restart = directory.getEntry("pause-restart", Texture.class);
        pause_review = directory.getEntry("pause-review", Texture.class);
        pause_exit = directory.getEntry("pause-exit", Texture.class);
        pause_quit = directory.getEntry("pause-quit", Texture.class);
        pause_editor = directory.getEntry("pause-editor", Texture.class);
    }

    private void update(float delta) {

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
        canvas.draw(pause_logo, alphaTint, pause_logo.getWidth() / 2, pause_logo.getHeight() / 2,
                canvas.getWidth() / 2, canvas.getHeight() * LOGO_HEIGHT_RATIO, 0, 0.2f * scale, 0.2f * scale);
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
        Color tintEditor = (pressEditorState == 1 ? Color.BLACK : Color.WHITE);
        canvas.draw(pause_editor, tintEditor, pause_editor.getWidth() / 2,
                pause_editor.getHeight() / 2,
                centerX, centerYEditor, 0,
                BUTTON_SCALE * scale * pause_exit.getWidth() / pause_editor.getWidth(),
                BUTTON_SCALE * scale * pause_exit.getHeight() / pause_editor.getHeight());
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
        pressEditorState = 0;
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
            if (isReadyRestart() && observer != null) {
                observer.exitScreen(this, GO_RESTART);
            }
            if (isReadyReview() && observer != null) {
                observer.exitScreen(this, GO_SETTING);
            }
            if (isReadyExit() && observer != null) {
                observer.exitScreen(this, GO_MENU);
            }
            if (isReadyQuit() && observer != null) {
                observer.exitScreen(this, GO_EXIT);
            }
            if (isReadyEditor()) {
                observer.exitScreen(this, GO_EDITOR);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.E) && observer != null) {
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        centerYResume = (int) (RESUME_HEIGHT_RATIO * height);
        centerYRestart = (int) (RESTART_HEIGHT_RATIO * height);
        centerYReview = (int) (REVIEW_HEIGHT_RATIO * height);
        centerYExit = (int) (EXIT_HEIGHT_RATIO * height);
        centerYQuit = (int) (QUIT_HEIGHT_RATIO * height);
        centerYEditor = (int) (EDITOR_HEIGHT_RATIO * height);
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
        float distYEditor = Math.abs(screenY - centerYEditor);
        if (distX < x && distYEditor < y) {
            pressEditorState = 1;
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
        if (pressEditorState == 1) {
            pressEditorState = 2;
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
