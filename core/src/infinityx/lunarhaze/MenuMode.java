package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.ScreenObservable;

/**
 * Provides a menu screen for level select
 */
public class MenuMode extends ScreenObservable implements Screen, InputProcessor {
    public static final int GO_EDITOR = 0;
    public static final int GO_PLAY = 1;
    public static final int GO_SETTING = 2;
    public static final int GO_ABOUT_US = 3;
    public static final int GO_EXIT = 4;
    /**
     * Background texture for start-up
     */
    private Texture background;
    /**
     * Play button to display when done
     */
    private Texture playButton;
    private static final float BUTTON_SCALE = 0.75f;

    /**
     * Setting button
     */
    private Texture settingButton;
    private static final float BUTTON_SCALE_SETTING = 0.3f;
    /**
     * About Us button
     */
    private Texture aboutUsButton;
    private static final float BUTTON_SCALE_ABOUT_US = 0.3f;
    /**
     * Exit button
     */
    private Texture exitButton;
    private static final float BUTTON_SCALE_EXIT = 0.3f;


    /**
     * Reference to GameCanvas created by the root
     */
    private final GameCanvas canvas;

    /**
     * The height of the canvas window (necessary since sprite origin != screen origin)
     */
    private int heightY;
    /**
     * Scaling factor for when the student changes the resolution.
     */
    private float scale;

    /**
     * The y-coordinate of the center of the play button
     */
    private int centerY;
    /**
     * The y-coordinate of the center of the setting button
     */
    private int centerYSetting;
    /**
     * The y-coordinate of the center of the about us button
     */
    private int centerYAboutUs;
    /**
     * The y-coordinate of the center of the exit button
     */
    private int centerYExit;
    /**
     * The x-coordinate of the center of the play button
     */
    private int centerX;
    /**
     * The x-coordinate of the center of the setting button
     */
    private int centerXSetting;
    /**
     * The x-coordinate of the center of the sbout us button
     */
    private int centerXAboutUs;
    /**
     * The x-coordinate of the center of the exit button
     */
    private int centerXExit;

    /**
     * The current state of the play button
     */
    private int pressPlayState;

    /**
     * The current state of the setting button
     */
    private int pressSettingState;

    /**
     * The current state of the about us button
     */
    private int pressAboutUsState;

    /**
     * The current state of the exit button
     */
    private int pressExitState;

    /**
     * Whether or not this player mode is still active
     */
    private boolean active;

    /**
     * Standard window size (for scaling)
     */
    private static final int STANDARD_WIDTH = 800;
    /**
     * Standard window height (for scaling)
     */
    private static final int STANDARD_HEIGHT = 700;

    /**
     * Ratio of play height from bottom
     */
    private static final float PLAY_HEIGHT_RATIO = 0.15f;
    /**
     * Ratio of setting height from bottom
     */
    private static final float SETTING_HEIGHT_RATIO = 0.15f;
    /**
     * Ratio of about us height from bottom
     */
    private static final float ABOUT_US_HEIGHT_RATIO = 0.15f;
    /**
     * Ratio of about us height from bottom
     */
    private static final float EXIT_HEIGHT_RATIO = 0.15f;

    /**
     * What level was selected by the player
     */
    private int levelSelected;

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressPlayState == 2;
    }

    /**
     * Returns true if all assets are loaded and the player is ready to change setting.
     *
     * @return true if the player is ready to change setting
     */
    public boolean isSettingReady() {
        return pressSettingState == 2;
    }

    /**
     * Returns true if all assets are loaded and the player is ready to get to about us page.
     *
     * @return true if the player is ready to get to about us page
     */
    public boolean isAboutUsReady() {
        return pressAboutUsState == 2;
    }
    /**
     * Returns true if all assets are loaded and the player is ready to quit game.
     *
     * @return true if the player is ready to quit game
     */
    public boolean isExitReady() {
        return pressExitState == 2;
    }

    /**
     * Creates a new Menu
     *
     * @param canvas The game canvas to draw to
     */
    public MenuMode(GameCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Gather the assets for this controller.
     * <p>
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        //for (String s : directory.getAssetNames()) {
        //    System.out.println(s);
        //}
        //directory.isLoaded("")
        background = directory.getEntry("background", Texture.class);
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        playButton = directory.getEntry("play", Texture.class);
        settingButton = directory.getEntry("setting", Texture.class);
        exitButton = directory.getEntry("exit", Texture.class);
        aboutUsButton = directory.getEntry("about-us", Texture.class);
    }

    public int getLevelSelected() {
        return levelSelected;
    }

    public void setLevelSelected(int levelSelected) {
        this.levelSelected = levelSelected;
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
//        System.out.println(pressSettingState);
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
        canvas.drawOverlay(background, alphaTint, true);
//        canvas.draw(background, 0, 0);
        Color colorplay = new Color(142.0f/255.0f, 157.0f/255.0f, 189.0f/255.0f, 1.0f);
        Color colorother = new Color(89.0f/255.0f, 18.0f/255.0f, 13.0f/255.0f, 1.0f);
        Color tintPlay = (pressPlayState == 1 ? colorplay : Color.WHITE);
        canvas.draw(playButton, tintPlay, playButton.getWidth() / 2, playButton.getHeight() / 2,
                centerX, centerY, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        Color tintSetting = (pressSettingState == 1 ? colorother : Color.WHITE);
        canvas.draw(settingButton, tintSetting, settingButton.getWidth() / 2, settingButton.getHeight() / 2,
                centerXSetting, centerYSetting, 0, BUTTON_SCALE_SETTING * scale, BUTTON_SCALE_SETTING * scale);
        Color tintAboutUs = (pressAboutUsState == 1 ? colorother : Color.WHITE);
        canvas.draw(aboutUsButton, tintAboutUs, aboutUsButton.getWidth() / 2, aboutUsButton.getHeight() / 2,
                centerXAboutUs, centerYAboutUs, 0, BUTTON_SCALE_ABOUT_US * scale, BUTTON_SCALE_ABOUT_US * scale);
        Color tintExit = (pressExitState == 1 ? colorother : Color.WHITE);
        canvas.draw(exitButton, tintExit, exitButton.getWidth() / 2, exitButton.getHeight() / 2,
                centerXExit, centerYExit, 0, BUTTON_SCALE_EXIT * scale, BUTTON_SCALE_EXIT * scale);
        canvas.end();
    }

    // ADDITIONAL SCREEN METHODS

    /**
     * Called when the Screen should render itself.
     * <p>
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (Gdx.input.isKeyPressed(Input.Keys.L)) {
                observer.exitScreen(this, GO_EDITOR);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.T)) {
                setLevelSelected(0);
                observer.exitScreen(this, GO_PLAY);
            }

            // We are are ready, notify our listener
            if (isReady() && observer != null) {
                setLevelSelected(1);
                observer.exitScreen(this, GO_PLAY);
            }
            // Settings are ready, notify our listener
            if (isSettingReady() && observer != null) {
                observer.exitScreen(this, GO_SETTING);
            }
            // About Us are ready, notify our listener
            if (isAboutUsReady() && observer != null) {
                observer.exitScreen(this, GO_ABOUT_US);
            }
            // Exit are ready, notify our listener
            if (isExitReady() && observer != null) {
                observer.exitScreen(this, GO_EXIT);
            }
        }
    }

    /**
     * Called when the Screen is resized.
     * <p>
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        centerY = (int) (PLAY_HEIGHT_RATIO * height);
        centerYSetting = (int) (SETTING_HEIGHT_RATIO * height);
        centerYAboutUs = (int) (ABOUT_US_HEIGHT_RATIO * height);
        centerYExit = (int) (EXIT_HEIGHT_RATIO * height);
        centerX = width / 2;
        centerXSetting = width/32*21;
        centerXAboutUs = width/32*24;
        centerXExit = width/32*27;
        heightY = height;
    }

    /**
     * Called when the Screen is paused.
     * <p>
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     * <p>
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
        pressPlayState = 0;
        pressSettingState = 0;
        pressAboutUsState = 0;
        pressExitState = 0;
        Gdx.input.setInputProcessor(this);
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    // PROCESSING PLAYER INPUT

    /**
     * Called when a key was pressed
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed
     */
    public boolean keyDown(int keycode) {
        return false;
    }

    /**
     * Called when a key was released
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed
     */
    public boolean keyUp(int keycode) {
        return false;
    }

    /**
     * Called when a key was typed
     *
     * @param character The character
     * @return whether the input was processed
     */
    public boolean keyTyped(char character) {
        return false;
    }

    /**
     * Called when the screen was touched or a mouse button was pressed.
     * <p>
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (playButton == null || pressPlayState == 2 || pressSettingState == 2 || pressAboutUsState == 2) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY - screenY;

        //System.out.printf("%d, %d", screenX, screenY);

        // TODO: Fix scaling
        // Play button is a circle.
        float radiusPlay = BUTTON_SCALE * scale * playButton.getWidth() / 2.0f;
        float distPlay = (screenX - centerX) * (screenX - centerX) + (screenY - centerY) * (screenY - centerY);
        if (distPlay < radiusPlay * radiusPlay) {
            pressPlayState = 1;
        }
        // Setting button is a circle.
        float radiusSetting = BUTTON_SCALE_SETTING * scale * settingButton.getWidth() / 2.0f;
        float distSetting = (screenX - centerXSetting) * (screenX - centerXSetting) + (screenY - centerYSetting) * (screenY - centerYSetting);
        if (distSetting < radiusSetting * radiusSetting) {
            pressSettingState = 1;
        }
        // About Us button is a circle.
        float radiusAboutUs = BUTTON_SCALE_ABOUT_US * scale * aboutUsButton.getWidth() / 2.0f;
        float distAboutUs = (screenX - centerXAboutUs) * (screenX - centerXAboutUs) + (screenY - centerYAboutUs) * (screenY - centerYAboutUs);
        if (distAboutUs < radiusAboutUs * radiusAboutUs) {
            pressAboutUsState = 1;
        }
        float radiusExit = BUTTON_SCALE_EXIT * scale * exitButton.getWidth() / 2.0f;
        float distExit = (screenX - centerXExit) * (screenX - centerXExit) + (screenY - centerYExit) * (screenY - centerYExit);
        if (distExit < radiusExit * radiusExit) {
            pressExitState = 1;
        }

        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     * <p>
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressPlayState == 1) {
            pressPlayState = 2;
            return false;
        }
        if (pressSettingState == 1) {
            pressSettingState = 2;
            return false;
        }
        if (pressAboutUsState == 1) {
            pressAboutUsState = 2;
            return false;
        }
        if (pressExitState == 1) {
            pressExitState = 2;
            return false;
        }

        return true;
    }

    /**
     * Called when a finger or the mouse was dragged.
     *
     * @param screenX
     * @param screenY
     * @param pointer the pointer for the event.
     * @return whether the input was processed
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. Will not be called on iOS.
     *
     * @param screenX
     * @param screenY
     * @return whether the input was processed
     */
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    /**
     * Called when the mouse wheel was scrolled. Will not be called on iOS.
     *
     * @param amountX the horizontal scroll amount, negative or positive depending on the direction the wheel was scrolled.
     * @param amountY the vertical scroll amount, negative or positive depending on the direction the wheel was scrolled.
     * @return whether the input was processed.
     */
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

}