package infinityx.lunarhaze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
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
     * Font used in setting mode
     */
    private BitmapFont font;
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
     * Back button
     */
    private Texture backButton;

    /**
     * Texture of Bar for slider
     */
    private Texture sliderBar;
    /**
     * Texture of Knob for slider
     */
    private Texture sliderKnob;
    /**
     * Texture for empty star
     */
    private Texture star_empty;

    /**
     * Texture for filled star
     */
    private Texture star_filled;
    /**
     * Slider for music volume
     */
    private Slider musicVolumeSlider;
    /**
     * Width of stars
     */
    private static final float STAR_WIDTH = 25.0f;

    /**
     * Height of stars
     */
    private static final float STAR_HEIGHT = STAR_WIDTH;
    /**
     * Ratio of back width from bottom
     */
    private static final float BACK_WIDTH_RATIO = 0.1f;
    /**
     * Ratio of back height from bottom
     */
    private static final float BACK_HEIGHT_RATIO = 0.93f;

    private static final float BUTTON_SCALE = 0.8f;

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
     * The current state of the setting button
     */
    private int[] pressStarState;

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
    private static final float MUSIC_HEIGHT_RATIO = 0.7f;
    private static final float MUSIC_BAR_HEIGHT_RATIO = 0.4f;
    private ImageButton musicButton;


    private boolean dragging;

    private float sliderValue;
    private float starY;

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

    public int isStarReady() {
        for (int i = 1; i<=10; i++ ){
            if(pressStarState[i] == 2){
                return i;
            }
        }
        return 0;
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
        sliderBar = directory.getEntry("setting-music-volume-bar", Texture.class);
        sliderKnob = directory.getEntry("setting-music-volume-knob", Texture.class);
        backButton = directory.getEntry("back", Texture.class);
        star_empty = directory.getEntry("star-empty", Texture.class);
        star_filled = directory.getEntry("star-filled", Texture.class);
    }

    private void draw() {
        canvas.begin(GameCanvas.DrawPass.SPRITE);
        Color alphaTint = Color.WHITE;
        canvas.drawOverlay(background, alphaTint, true);
        canvas.drawTextCentered("Music Enabled", font, canvas.getHeight()*0.3f);
        canvas.draw(music, alphaTint, music.getWidth() / 2, music.getHeight() / 2,
                centerX_music, centerY_music, 0, MUSIC_SCALE * scale, MUSIC_SCALE * scale);
        canvas.drawTextCentered("Music Volume", font, canvas.getHeight() * 0.05f);
        int stat = (int) (setting.getMusicVolume() * 10);
        for (int i = 1; i <= 10; i++) {
            if (stat >= i) {
                // Draw a filled star for the ith star
                canvas.draw(star_filled, Color.WHITE, star_filled.getWidth() / 2, star_filled.getHeight() / 2,
                        canvas.getWidth()/2 + (i - 6) * STAR_WIDTH, starY, 0,
                        STAR_WIDTH / star_filled.getWidth(), STAR_HEIGHT / star_filled.getHeight());
            } else {
                // Draw an empty heart for the ith heart
                canvas.draw(star_empty, Color.WHITE, star_filled.getWidth() / 2, star_filled.getHeight() / 2,
                        canvas.getWidth()/2 + (i - 6) * STAR_WIDTH, starY, 0,
                        STAR_WIDTH / star_filled.getWidth(), STAR_HEIGHT / star_filled.getHeight());
            }
        }
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
//        System.out.println(setting.isMusicEnabled());
    }

    @Override
    public void show() {
        active = true;
        pressBackState = 0;
        pressMusicState = 0;
        pressStarState = new int[11];
        for (int i = 1; i<=10; i++ ){
            pressStarState[i] = 0;
        }
        font = new BitmapFont();
        font.getData().setScale(25 / font.getCapHeight());
        Gdx.input.setInputProcessor(this);

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
            if(isStarReady()!=0 && observer != null){
                setting.setMusicVolume(((float)isStarReady())/10.0f);
                for (int i = 1; i<=10; i++ ){
                    pressStarState[i] = 0;
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
        float sx = ((float) width) / ((float) STANDARD_WIDTH);
        float sy = ((float) height) / ((float) STANDARD_HEIGHT);
        scale = (sx < sy ? sx : sy);

        centerY = (int) (BACK_HEIGHT_RATIO * height);
        centerX = (int) (BACK_WIDTH_RATIO * width);
        centerX_music = width/2;
        centerY_music = (int) (MUSIC_HEIGHT_RATIO * height);
        heightY = height;
        starY = canvas.getHeight() * 0.45f;
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
        float x_back = BUTTON_SCALE * scale * backButton.getWidth() / 2;
        float distX_back = Math.abs(screenX - centerX);
        float y_back = BUTTON_SCALE * scale * backButton.getHeight() / 2;

        float distYBack = Math.abs(screenY - centerY);
        if (distX_back < x_back && distYBack < y_back) {
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

        float x_star = STAR_WIDTH;
        float y_star = STAR_HEIGHT;
        float[] disXstar = new float[11];
        float[] disYstar = new float[11];
        for (int i = 1; i<=10; i++ ){
            disXstar[i] = Math.abs(screenX - (canvas.getWidth()/2 + (i - 6) * STAR_WIDTH));
            disYstar[i] = Math.abs(screenY - starY);
        }
        for (int i = 1; i<=10; i++ ){
            if(disXstar[i] < x_star && disYstar[i] < y_star){
                pressStarState[i] = 1;
            }
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
        for (int i = 1; i<=10; i++ ){
            if(pressStarState[i] == 1){
                pressStarState[i] = 2;
            }
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