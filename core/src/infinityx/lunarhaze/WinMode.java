package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import infinityx.assets.AssetDirectory;
import infinityx.util.ScreenObservable;

/**
 * Provides a menu screen for level select
 */
public class WinMode extends ScreenObservable implements Screen {
    /** Background texture for start-up */
    private Texture background;
    private static float BUTTON_SCALE  = 0.75f;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The y-coordinate of the center of the play button */
    private int centerY;
    /** The x-coordinate of the center of the play button */
    private int centerX;

    /** The current state of the play button */
    private int   pressState;

    /** Whether or not this player mode is still active */
    private boolean active;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;

    /** Ratio of play height from bottom */
    private static float PLAY_HEIGHT_RATIO = 0.25f;

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 2;
    }

    /**
     * Creates a new Menu
     * @param canvas 	The game canvas to draw to
     */
    public WinMode(GameCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Gather the assets for this controller.
     *
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory	Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        //for (String s : directory.getAssetNames()) {
        //    System.out.println(s);
        //}
        //directory.isLoaded("")
        background = directory.getEntry( "won", Texture.class );
        background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {

    }

    /**
     * Update the status of this menu.
     */
    private void update(float delta) {
        //System.out.println(pressState);
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.begin();
        canvas.draw(background, 0, 0);
        canvas.end();
    }

    // ADDITIONAL SCREEN METHODS
    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            // We are are ready, notify our listener
            if (isReady() && observer != null) {
                observer.exitScreen(this, 1);
            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        centerY = (int)(PLAY_HEIGHT_RATIO*height);
        centerX = width/2;
        heightY = height;
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

}