package infinityx.lunarhaze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.GDXRoot;
import infinityx.lunarhaze.controllers.InputController;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.ScreenObservable;

/**
 * Provides a menu screen for level select
 */
public class SettingMode extends ScreenObservable implements Screen, InputProcessor {
    // Exit codes
    /**
     * User requested to go to menu
     */
    public final static int GO_MENU = 0;
    /**
     * User requested to go to pause
     */
    public final static int GO_PAUSE = 1;
    /**
     * Reference to GameCanvas created by the root
     */
    private final GameSetting setting;
    /**
     * Reads input from keyboard or game pad (CONTROLLER CLASS)
     */
    private InputController inputController;
    /**
     * Reference to GameCanvas created by the root
     */
    private final GameCanvas canvas;
    /**
     * Reference to Game created by the root
     */
    private final GDXRoot game;
    /**
     * Background texture for start-up
     */
    private Texture background;
    /**
     * Music on texture for music button
     */
    private Texture musicOnTexture;
    /**
     * Music off texture for music button
     */
    private Texture musicOffTexture;

    private Texture music;
    /**
     * Back button to display when done
     */
    private Texture backButton;
    private static final float BUTTON_SCALE = 0.25f;

    private static final float MUSIC_SCALE = 0.1f;
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
     * The y-coordinate of the center of the music button
     */
    private int centerY_music;
    /**
     * The x-coordinate of the center of the music button
     */
    private int centerX_music;
    /**
     * The current state of the setting button
     */
    private int pressBackState;
    /**
     * The current state of the setting button
     */
    private int pressMusicState;
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
    private static final float MUSIC_HEIGHT_RATIO = 0.4f;
    /**
     * Ratio of play height from bottom
     */
    private static final float BACK_HEIGHT_RATIO = 0.9f;

    private ImageButton musicButton;

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
    public boolean isMusicReady() {
        return pressMusicState == 2;
    }


    public SettingMode(GameCanvas canvas, GDXRoot game, GameSetting setting) {
        this.canvas = canvas;
        this.game = game;
        this.setting = setting;
        this.inputController = InputController.getInstance();
    }

    public void gatherAssets(AssetDirectory directory) {
        background = directory.getEntry("background-setting", Texture.class);
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        musicOnTexture = directory.getEntry("setting-music-on", Texture.class);
        musicOffTexture = directory.getEntry("setting-music-off",Texture.class);
        backButton = directory.getEntry("back", Texture.class);
    }

    private void draw() {
        canvas.begin(GameCanvas.DrawPass.SPRITE);
        Color alphaTint = Color.WHITE;
        canvas.drawOverlay(background, alphaTint, true);
        canvas.drawTextCentered("Music Enabled", new BitmapFont(), canvas.getHeight() *0.3f);
        canvas.draw(music, alphaTint, music.getWidth() / 2, music.getHeight() / 2,
                centerX_music, centerY_music, 0, 0.1f* scale, MUSIC_SCALE * scale);
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
        music = (setting.isMusicEnabled()? musicOnTexture : musicOffTexture);
        inputController.readKeyboard();
    }

    @Override
    public void show() {
        active = true;
        pressBackState = 0;
        pressMusicState = 0;
        Gdx.input.setInputProcessor(this);
    }

    private void toggleMusic(boolean enabled) {
        if (enabled) {
            // Play/Resume music
        } else {
            // Pause music
        }
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();
            if ((isReady() || inputController.didExit()) && observer != null) {
                if (game.getPreviousScreen() == "pause") {
                    observer.exitScreen(this, GO_PAUSE);
                }
                if (game.getPreviousScreen() == "menu") {
                    observer.exitScreen(this, GO_MENU);
                }

            }

            if(isMusicReady() && observer !=null){
                setting.setMusicEnabled(!setting.isMusicEnabled());
                pressMusicState = 0;
            }
        // We are are ready, notify our listener
        }
    }

    @Override
    public void resize(int width, int height) {
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        centerY = (int) (BACK_HEIGHT_RATIO * height);
        centerX = width / 16;
        centerX_music = width/2;
        centerY_music = (int) (MUSIC_HEIGHT_RATIO * height);
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
        // Play button is a circle.
        float radiusBack = BUTTON_SCALE * scale * backButton.getWidth() / 2.0f;
        float distBack = (screenX - centerX) * (screenX - centerX) + (screenY - centerY) * (screenY - centerY);
        if (distBack < radiusBack * radiusBack) {
            pressBackState = 1;
        }


        float x = MUSIC_SCALE * scale * music.getWidth() / 2;
        float distX = Math.abs(screenX - centerX_music);
        float y = MUSIC_SCALE * scale * music.getHeight() / 2;

        // Play button is a rectangle.
        float distYPlay = Math.abs(screenY - centerY_music);
        if (distX < x && distYPlay < y) {
            pressMusicState = 1;
        }


        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressBackState == 1) {
            pressBackState = 2;
            return false;
        }
        if (pressMusicState == 1) {
            pressMusicState = 2;
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