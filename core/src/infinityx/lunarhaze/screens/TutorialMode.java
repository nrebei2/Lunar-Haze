package infinityx.lunarhaze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.ScreenObservable;

public class TutorialMode extends ScreenObservable implements Screen, InputProcessor {
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
     * Current page number for the screen
     */
    private int page_number;
    /**
     * Background texture for the page
     */
    private Texture page;
    /**
     * Background texture for the page1
     */
    private Texture page1;
    /**
     * Background texture for the page2
     */
    private Texture page2;
    /**
     * Background texture for the page3
     */
    private Texture page3;
    /**
     * Background texture for the page4
     */
    private Texture page4;
    /**
     * Background texture for the page5
     */
    private Texture page5;

    /**
     * Back button to display when done
     */
    private Texture backButton;
    private static final float BUTTON_SCALE = 0.8f;
    private static final float ARROW_BUTTON_SCALE = 0.5f;

    /**
     * Right button to display when done
     */
    private Texture rightButton;

    /**
     * Left button to display when done
     */
    private Texture leftButton;
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
     * The y-coordinate of the center of the arrow button
     */
    private int centerYArrow;
    /**
     * The x-coordinate of the center of the back button
     */
    private int centerX;
    /**
     * The x-coordinate of the center of the left button
     */
    private int centerXLeft;
    /**
     * The x-coordinate of the center of the right button
     */
    private int centerXRight;
    /**
     * The current state of the setting button
     */
    private int pressBackState;
    /**
     * The current state of the setting button
     */
    private int pressLeftState;
    /**
     * The current state of the setting button
     */
    private int pressRightState;
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
     * Ratio of back width from bottom
     */
    private static final float LEFT_WIDTH_RATIO = 0.07f;
    /**
     * Ratio of back width from bottom
     */
    private static final float RIGHT_WIDTH_RATIO = 0.93f;
    /**
     * Ratio of back height from bottom
     */
    private static final float BACK_HEIGHT_RATIO = 0.93f;
    /**
     * Ratio of back height from bottom
     */
    private static final float ARROW_HEIGHT_RATIO = 0.52f;

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressBackState == 2;
    }

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReadyRight() {
        return pressRightState == 2;
    }

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReadyLeft() {
        return pressLeftState == 2;
    }


    public TutorialMode(GameCanvas canvas) {
        this.canvas = canvas;
    }

    public void gatherAssets(AssetDirectory directory) {
        page1 = directory.getEntry("tutorial-1", Texture.class);
        page1.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        page2 = directory.getEntry("tutorial-2", Texture.class);
        page2.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        page3 = directory.getEntry("tutorial-3", Texture.class);
        page3.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        page4 = directory.getEntry("tutorial-4", Texture.class);
        page4.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        page5 = directory.getEntry("tutorial-5", Texture.class);
        page5.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        backButton = directory.getEntry("tutorial-back", Texture.class);
        leftButton = directory.getEntry("tutorial-left", Texture.class);
        rightButton = directory.getEntry("tutorial-right", Texture.class);
    }

    private void draw() {
        canvas.beginUI(GameCanvas.DrawPass.SPRITE);
        Color alphaTint = Color.WHITE;
        canvas.drawOverlay(page, alphaTint, true);
        Color color = new Color(45.0f / 255.0f, 74.0f / 255.0f, 133.0f / 255.0f, 1.0f);
        Color tintBack = (pressBackState == 1 ? color : Color.WHITE);
        canvas.draw(backButton, tintBack, backButton.getWidth() / 2, backButton.getHeight() / 2,
                centerX, centerY, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        if (page_number != 1) {
            Color tintLeft = (pressLeftState == 1 ? color : Color.WHITE);
            canvas.draw(leftButton, tintLeft, leftButton.getWidth() / 2, leftButton.getHeight() / 2,
                    centerXLeft, centerYArrow, 0, ARROW_BUTTON_SCALE * scale,
                    ARROW_BUTTON_SCALE * scale);
        }
        if (page_number != 5) {
            Color tintRight = (pressRightState == 1 ? color : Color.WHITE);
            canvas.draw(rightButton, tintRight, rightButton.getWidth() / 2,
                    rightButton.getHeight() / 2,
                    centerXRight, centerYArrow, 0, ARROW_BUTTON_SCALE * scale,
                    ARROW_BUTTON_SCALE * scale);
        }
        canvas.end();
    }

    /**
     * Update the status of this menu.
     */
    private void update(float delta) {
        if (page_number == 1) {
            page = page1;
            if (isReadyRight()) {
                pressRightState = 0;
                page_number = 2;
            }
        } else if (page_number == 2) {
            page = page2;
            if (isReadyRight()) {
                pressRightState = 0;
                page_number = 3;
            } else if (isReadyLeft()) {
                pressLeftState = 0;
                page_number = 1;
            }
        } else if (page_number == 3) {
            page = page3;
            if (isReadyRight()) {
                pressRightState = 0;
                page_number = 4;
            } else if (isReadyLeft()) {
                pressLeftState = 0;
                page_number = 2;
            }
        } else if (page_number == 4) {
            page = page4;
            if (isReadyRight()) {
                pressRightState = 0;
                page_number = 5;
            } else if (isReadyLeft()) {
                pressLeftState = 0;
                page_number = 3;
            }
        } else {
            page = page5;
            if (isReadyLeft()) {
                pressLeftState = 0;
                page_number = 4;
            }

        }


    }

    @Override
    public void show() {
        active = true;
        pressBackState = 0;
        pressLeftState = 0;
        pressRightState = 0;
        page_number = 1;
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
        centerYArrow = (int) (ARROW_HEIGHT_RATIO * height);
        centerXLeft = (int) (LEFT_WIDTH_RATIO * width);
        centerXRight = (int) (RIGHT_WIDTH_RATIO * width);
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

        // TODO: Fix scaling
        float x = BUTTON_SCALE * scale * backButton.getWidth() / 2;
        float distX = Math.abs(screenX - centerX);
        float y = BUTTON_SCALE * scale * backButton.getHeight() / 2;
        float distYBack = Math.abs(screenY - centerY);
        if (distX < x && distYBack < y) {
            pressBackState = 1;
        }


        float xLeft = ARROW_BUTTON_SCALE * scale * leftButton.getWidth() / 2;
        float distXLeft = Math.abs(screenX - centerXLeft);
        float yLeft = ARROW_BUTTON_SCALE * scale * leftButton.getHeight() / 2;
        float distYLeft = Math.abs(screenY - centerYArrow);
        if (distXLeft < xLeft && distYLeft < yLeft && page_number != 1) {
            pressLeftState = 1;
        }

        float xRight = ARROW_BUTTON_SCALE * scale * rightButton.getWidth() / 2;
        float distXRight = Math.abs(screenX - centerXRight);
        float yRight = ARROW_BUTTON_SCALE * scale * rightButton.getHeight() / 2;
        float distYRight = Math.abs(screenY - centerYArrow);
        if (distXRight < xRight && distYRight < yRight && page_number != 5) {
            pressRightState = 1;
        }


        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressBackState == 1) {
            pressBackState = 2;
            return false;
        }
        if (pressLeftState == 1) {
            pressLeftState = 2;
            return false;
        }
        if (pressRightState == 1) {
            pressRightState = 2;
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
