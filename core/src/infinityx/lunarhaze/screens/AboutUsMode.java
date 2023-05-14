package infinityx.lunarhaze.screens;

import com.badlogic.gdx.Gdx;
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
public class AboutUsMode extends ScreenObservable implements Screen, InputProcessor {
    // Exit codes
    /**
     * User requested to go to menu
     */
    public final static int GO_MENU = 0;

    /**
     * Reference to GameCanvas created by the root
     */
    private final GameCanvas canvas;
    /**
     * Background texture for start-up
     */
    private Texture background;
    /**
     * Back button to display when done
     */
    private Texture backButton;
    private static final float BUTTON_SCALE = 0.8f;
    /**
     * Whether or not this player mode is still active
     */
    private boolean active;
    /**
     * The height of the canvas window (necessary since sprite origin != screen origin)
     */
    private int heightY;
    /**
     * Scaling factor for when the student changes the resolution.
     */
    private float scale;

    /**
     * The y-coordinate of the center of the back button
     */
    private int centerY;
    /**
     * The x-coordinate of the center of the back button
     */
    private int centerX;
    /**
     * The current state of the setting button
     */
    private int pressBackState;
    /**
     * Standard window size (for scaling)
     */
    private static final int STANDARD_WIDTH = 800;
    /**
     * Standard window height (for scaling)
     */
    private static final int STANDARD_HEIGHT = 700;

    /**
     * Ratio of back width from bottom
     */
    private static final float BACK_WIDTH_RATIO = 0.1f;
    /**
     * Ratio of back height from bottom
     */
    private static final float BACK_HEIGHT_RATIO = 0.93f;

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressBackState == 2;
    }


    public AboutUsMode(GameCanvas canvas) {
        this.canvas = canvas;
    }

    public void gatherAssets(AssetDirectory directory) {
        background = directory.getEntry("background-about-us", Texture.class);
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        backButton = directory.getEntry("back", Texture.class);
    }

    private void draw() {
        canvas.beginUI(GameCanvas.DrawPass.SPRITE);
        Color alphaTint = Color.WHITE;
        canvas.drawOverlay(background, alphaTint, true);
        Color color = new Color(45.0f / 255.0f, 74.0f / 255.0f, 133.0f / 255.0f, 1.0f);
        Color tintBack = (pressBackState == 1 ? color : Color.WHITE);
        canvas.draw(backButton, tintBack, backButton.getWidth() / 2, backButton.getHeight() / 2,
                centerX, centerY, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        canvas.end();
    }

    /**
     * Update the status of this menu.
     */
    private void update(float delta) {

    }

    @Override
    public void show() {
        active = true;
        pressBackState = 0;
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();
        }
        // We are are ready, notify our listener
        if (isReady() && observer != null) {
            observer.exitScreen(this, GO_MENU);
        }
    }

    @Override
    public void resize(int width, int height) {
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        centerY = (int) (BACK_HEIGHT_RATIO * height);
        centerX = (int) (BACK_WIDTH_RATIO * width);
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
        // Useless if called in outside animation loop
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
        if (backButton == null || pressBackState == 2) {
            return true;
        }
        // Flip to match graphics coordinates
        screenY = heightY - screenY;

        // Play button is a circle.
        float x = BUTTON_SCALE * scale * backButton.getWidth() / 2;
        float distX = Math.abs(screenX - centerX);
        float y = BUTTON_SCALE * scale * backButton.getHeight() / 2;

        float distYBack = Math.abs(screenY - centerY);
        if (distX < x && distYBack < y) {
            pressBackState = 1;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
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
