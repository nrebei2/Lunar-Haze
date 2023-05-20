package infinityx.lunarhaze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
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
     * Background texture for title
     */
    private Texture title;

    /**
     * Background texture for ON selected
     */
    private Texture on_s;
    /**
     * Background texture for ON unselected
     */
    private Texture on_u;

    /**
     * Background texture for Off selected
     */
    private Texture off_s;

    /**
     * Background texture for Off unselected
     */
    private Texture off_u;

    private Texture music;

    private Texture music_volume;

    private Texture sound;

    private Texture sound_volume;

    private Texture sound_s;

    private Texture sound_u;

    private Texture control_s;

    private Texture control_u;

    private static final float BUTTON_SCALE = 0.8f;

    private static final float MUSIC_SCALE = 0.1f;

    private static final float TEXT_SCALE = 0.8f;

    /**
     * Back button
     */
    private Texture backButton;
    /**
     * Texture for empty star
     */
    private Texture star_empty;

    /**
     * Texture for filled star
     */
    private Texture star_filled;
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

    private static final float TITLE_WIDTH_RATIO = 0.5f;

    private static final float TITLE_HEIGHT_RATIO = 0.83f;

    private static final float CHOICE_WIDTH_RATIO = 0.14f;

    private static final float CHOICE1_HEIGHT_RATIO = 0.5f;

    private static final float CHOICE2_HEIGHT_RATIO = 0.47f;


    private static final float TEXT_WIDTH_RATIO = 0.4f;

    private static final float MUSIC_HEIGHT_RATIO = 0.65f;

    private static final float MUSIC_OF_HEIGHT_RATIO = MUSIC_HEIGHT_RATIO + 0.005f;

    private static final float MUSIC_VOLUME_HEIGHT_RATIO = 0.55f;

    private static final float MUSIC_STAR_HEIGHT_RATIO = MUSIC_VOLUME_HEIGHT_RATIO + 0.005f;

    private static final float SOUND_HEIGHT_RATIO = 0.45f;
    private static final float SOUND_OF_HEIGHT_RATIO = SOUND_HEIGHT_RATIO + 0.005f;

    private static final float SOUND_VOLUME_HEIGHT_RATIO = 0.35f;
    private static final float SOUND_STAR_HEIGHT_RATIO = SOUND_VOLUME_HEIGHT_RATIO + 0.005f;

    private static final float ON_WIDTH_RATIO = 0.57f;

    private static final float OFF_WIDTH_RATIO = 0.62f;

    private static final float STAR_WIDTH_RATIO = 0.6f;

    private boolean setting_choice_sound;

    private Texture sound_choice;
    private Texture control_choice;
    private Texture music_on;
    private Texture music_off;
    private Texture sound_on;
    private Texture sound_off;

    /**
     * Lobby and pause background music
     */
    private Music lobby_background;

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
    private int pressMusicOnState;
    private int pressMusicOffState;
    private int pressSoundOnState;
    private int pressSoundOffState;


    /**
     * The current state of the setting button
     */
    private int[] pressStarState;

    /**
     * The current state of the setting button
     */
    private int[] pressStarSoundState;

    /**
     * Standard window size (for scaling)
     */
    private static final int STANDARD_WIDTH = 800;
    /**
     * Standard window height (for scaling)
     */
    private static final int STANDARD_HEIGHT = 700;

    private float starY_music;
    private float starY_sound;
    private float centerY_sound;
    private float centerX_sound;

    private float centerX_music_on;
    private float centerX_music_off;
    private float centerX_sound_on;
    private float centerX_sound_off;
    private float centerY_music_on;
    private float centerY_music_off;
    private float centerY_sound_on;
    private float centerY_sound_off;




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
    public boolean isMusicOnReady() {
        return pressMusicOnState == 2;
    }

    public boolean isMusicOffReady() {
        return pressMusicOffState == 2;
    }



    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isSoundOnReady() {
        return pressSoundOnState == 2;
    }

    public boolean isSoundOffReady() {
        return pressSoundOffState == 2;
    }

    public int isStarReady() {
        for (int i = 1; i <= 10; i++) {
            if (pressStarState[i] == 2) {
                return i;
            }
        }
        return 0;
    }

    public int isStarSoundReady() {
        for (int i = 1; i <= 10; i++) {
            if (pressStarSoundState[i] == 2) {
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
        title = directory.getEntry("setting-title", Texture.class);
        on_s = directory.getEntry("setting-on-s", Texture.class);
        on_u = directory.getEntry("setting-on-u", Texture.class);
        off_s = directory.getEntry("setting-off-s", Texture.class);
        off_u = directory.getEntry("setting-off-u", Texture.class);
        music = directory.getEntry("setting-music", Texture.class);
        music_volume = directory.getEntry("setting-music-volume", Texture.class);
        sound = directory.getEntry("setting-sound", Texture.class);
        sound_volume = directory.getEntry("setting-sound-volume", Texture.class);
        sound_s = directory.getEntry("setting-sound-s", Texture.class);
        sound_u = directory.getEntry("setting-sound-u", Texture.class);
        control_s = directory.getEntry("setting-control-s", Texture.class);
        control_u = directory.getEntry("setting-control-u", Texture.class);
        backButton = directory.getEntry("back", Texture.class);
        star_empty = directory.getEntry("star-empty", Texture.class);
        star_filled = directory.getEntry("star-filled", Texture.class);
        lobby_background = directory.getEntry("lobbyBackground", Music.class);
    }

    private void draw() {
        canvas.beginUI(GameCanvas.DrawPass.SPRITE);
        Color alphaTint = Color.WHITE;
        canvas.drawOverlay(background, alphaTint, true);
        canvas.draw(title, alphaTint, title.getWidth()/2, title.getHeight()/2, canvas.getWidth() * TITLE_WIDTH_RATIO,
                canvas.getHeight() * TITLE_HEIGHT_RATIO,0,TEXT_SCALE * scale, TEXT_SCALE * scale);
        canvas.draw(sound_choice, alphaTint, sound_choice.getWidth()/2, sound_choice.getHeight()/2,canvas.getWidth() * CHOICE_WIDTH_RATIO,
                canvas.getHeight() * CHOICE1_HEIGHT_RATIO, 0, TEXT_SCALE * scale, TEXT_SCALE * scale );
//        canvas.draw(control_choice, alphaTint, control_choice.getWidth()/2, control_choice.getHeight()/2,canvas.getWidth() * CHOICE_WIDTH_RATIO,
//                canvas.getHeight() * CHOICE2_HEIGHT_RATIO, 0, TEXT_SCALE * scale, TEXT_SCALE * scale );
//

        canvas.draw(music, alphaTint, music.getWidth()/2, music.getHeight()/2, canvas.getWidth() * TEXT_WIDTH_RATIO,
                canvas.getHeight() * MUSIC_HEIGHT_RATIO,0,TEXT_SCALE * scale, TEXT_SCALE * scale);
        canvas.draw(music_volume, alphaTint, music_volume.getWidth()/2, music_volume.getHeight()/2, canvas.getWidth() * TEXT_WIDTH_RATIO,
                canvas.getHeight() * MUSIC_VOLUME_HEIGHT_RATIO,0,TEXT_SCALE * scale, TEXT_SCALE * scale);
        canvas.draw(sound, alphaTint, sound.getWidth()/2, sound.getHeight()/2, canvas.getWidth() * TEXT_WIDTH_RATIO,
                canvas.getHeight() * SOUND_HEIGHT_RATIO,0,TEXT_SCALE * scale, TEXT_SCALE * scale);
        canvas.draw(sound_volume, alphaTint, sound_volume.getWidth()/2, sound_volume.getHeight()/2, canvas.getWidth() * TEXT_WIDTH_RATIO,
                canvas.getHeight() * SOUND_VOLUME_HEIGHT_RATIO,0,TEXT_SCALE * scale, TEXT_SCALE * scale);


        canvas.draw(music_on, alphaTint, music_on.getWidth()/2, music_on.getHeight()/2,
                centerX_music_on, centerY_music_on, 0, TEXT_SCALE * scale, TEXT_SCALE * scale);
        canvas.draw(music_off, alphaTint, music_off.getWidth()/2, music_off.getHeight()/2,
                centerX_music_off, centerY_music_off, 0, TEXT_SCALE * scale, TEXT_SCALE * scale);
        canvas.draw(sound_on, alphaTint, sound_on.getWidth()/2, sound_on.getHeight()/2,
                centerX_sound_on, centerY_sound_on, 0, TEXT_SCALE * scale, TEXT_SCALE * scale);
        canvas.draw(sound_off, alphaTint, sound_off.getWidth()/2, sound_off.getHeight()/2,
                centerX_sound_off, centerY_sound_off, 0, TEXT_SCALE * scale, TEXT_SCALE * scale);

        int stat_music = (int) (setting.getMusicVolume() * 10);
        for (int i = 1; i <= 10; i++) {
            if (stat_music >= i) {
                // Draw a filled star for the ith star
                canvas.draw(star_filled, Color.WHITE, star_filled.getWidth() / 2, star_filled.getHeight() / 2,
                        canvas.getWidth() * STAR_WIDTH_RATIO + (i - 6) * STAR_WIDTH, starY_music, 0,
                        STAR_WIDTH / star_filled.getWidth(), STAR_HEIGHT / star_filled.getHeight());
            } else {
                // Draw an empty heart for the ith heart
                canvas.draw(star_empty, Color.WHITE, star_filled.getWidth() / 2, star_filled.getHeight() / 2,
                        canvas.getWidth() * STAR_WIDTH_RATIO + (i - 6) * STAR_WIDTH, starY_music, 0,
                        STAR_WIDTH / star_filled.getWidth(), STAR_HEIGHT / star_filled.getHeight());
            }
        }

        int stat_sound = (int) (setting.getSoundVolume() * 10);
        for (int i = 1; i <= 10; i++) {
            if (stat_sound >= i) {
                // Draw a filled star for the ith star
                canvas.draw(star_filled, Color.WHITE, star_filled.getWidth() / 2, star_filled.getHeight() / 2,
                        canvas.getWidth() * STAR_WIDTH_RATIO + (i - 6) * STAR_WIDTH, starY_sound, 0,
                        STAR_WIDTH / star_filled.getWidth(), STAR_HEIGHT / star_filled.getHeight());
            } else {
                // Draw an empty heart for the ith heart
                canvas.draw(star_empty, Color.WHITE, star_filled.getWidth() / 2, star_filled.getHeight() / 2,
                        canvas.getWidth() * STAR_WIDTH_RATIO + (i - 6) * STAR_WIDTH, starY_sound, 0,
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
        lobby_background.setVolume(setting.getMusicVolume());
        if(setting.isMusicEnabled()) {
            lobby_background.setLooping(true);
            lobby_background.play();
        }else{
            lobby_background.stop();
        }
        sound_choice = (setting_choice_sound ? sound_s : sound_u);
        control_choice = (setting_choice_sound ? control_u : control_s );
        music_on = (setting.isMusicEnabled() ? on_s : on_u);
        music_off = (setting.isMusicEnabled()? off_u : off_s);
        sound_on = (setting.isSoundEnabled() ? on_s : on_u);
        sound_off = (setting.isSoundEnabled()? off_u : off_s);
        inputController.readKeyboard();
    }

    @Override
    public void show() {
        active = true;
        setting_choice_sound = true;
        pressBackState = 0;
        pressMusicOnState = 0;
        pressMusicOffState = 0;
        pressSoundOnState = 0;
        pressSoundOffState = 0;
        pressStarState = new int[11];
        for (int i = 1; i <= 10; i++) {
            pressStarState[i] = 0;
        }
        pressStarSoundState = new int[11];
        for (int i = 1; i <= 10; i++) {
            pressStarSoundState[i] = 0;
        }
        Gdx.input.setInputProcessor(this);

    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();
            if ((isReady() || inputController.didExit()) && observer != null) {
                lobby_background.stop();
                if (game.getPreviousScreen() == "pause") {
                    observer.exitScreen(this, GO_PAUSE);
                }
                if (game.getPreviousScreen() == "menu") {
                    observer.exitScreen(this, GO_MENU);
                }
            }
            if (isStarReady() != 0 && observer != null) {
                setting.setMusicVolume(((float) isStarReady()) / 10.0f);
                for (int i = 1; i <= 10; i++) {
                    pressStarState[i] = 0;
                }
            }

            if (isStarSoundReady() != 0 && observer != null) {
                setting.setSoundVolume(((float) isStarSoundReady()) / 10.0f);
                for (int i = 1; i <= 10; i++) {
                    pressStarSoundState[i] = 0;
                }
            }

            if (isMusicOnReady() && observer != null) {
                setting.setMusicEnabled(true);
                pressMusicOnState = 0;
            }
            if (isMusicOffReady() && observer != null) {
                setting.setMusicEnabled(false);
                pressMusicOffState = 0;
            }
            if (isSoundOnReady() && observer != null) {
                setting.setSoundEnabled(true);
                pressSoundOnState = 0;
            }

            if (isSoundOffReady() && observer != null) {
                setting.setSoundEnabled(false);
                pressSoundOffState = 0;
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
        centerX_music = width / 2;
        centerX_sound = width / 2;
        centerY_music = (int) (MUSIC_HEIGHT_RATIO * height);
        centerY_sound = (int) (SOUND_HEIGHT_RATIO * height);
        heightY = height;
        starY_music = canvas.getHeight() * MUSIC_STAR_HEIGHT_RATIO;
        starY_sound = canvas.getHeight() * SOUND_STAR_HEIGHT_RATIO;
        centerX_music_on = (int) (ON_WIDTH_RATIO * width);
        centerX_music_off = (int) (OFF_WIDTH_RATIO * width);
        centerX_sound_on = (int) (ON_WIDTH_RATIO * width);
        centerX_sound_off = (int) (OFF_WIDTH_RATIO * width);
        centerY_music_on = (int) (MUSIC_OF_HEIGHT_RATIO * height);
        centerY_music_off = (int) (MUSIC_OF_HEIGHT_RATIO * height);
        centerY_sound_on = (int) (SOUND_OF_HEIGHT_RATIO * height);
        centerY_sound_off = (int) (SOUND_OF_HEIGHT_RATIO * height);
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

        float x_back = BUTTON_SCALE * scale * backButton.getWidth() / 2;
        float distX_back = Math.abs(screenX - centerX);
        float y_back = BUTTON_SCALE * scale * backButton.getHeight() / 2;

        float distYBack = Math.abs(screenY - centerY);
        if (distX_back < x_back && distYBack < y_back) {
            pressBackState = 1;
        }

        float x_star = STAR_WIDTH;
        float y_star = STAR_HEIGHT;
        float[] disXstar = new float[11];
        float[] disYstar = new float[11];
        for (int i = 1; i <= 10; i++) {
            disXstar[i] = Math.abs(screenX - (canvas.getWidth() * STAR_WIDTH_RATIO  + (i - 6) * STAR_WIDTH));
            disYstar[i] = Math.abs(screenY - starY_music);
        }
        for (int i = 1; i <= 10; i++) {
            if (disXstar[i] < x_star && disYstar[i] < y_star) {
                pressStarState[i] = 1;
            }
        }

        float[] disXstarSound = new float[11];
        float[] disYstarSound = new float[11];
        for (int i = 1; i <= 10; i++) {
            disXstarSound[i] = Math.abs(screenX - (canvas.getWidth() * STAR_WIDTH_RATIO  + (i - 6) * STAR_WIDTH));
            disYstarSound[i] = Math.abs(screenY - starY_sound);
        }
        for (int i = 1; i <= 10; i++) {
            if (disXstarSound[i] < x_star && disYstarSound[i] < y_star) {
                pressStarSoundState[i] = 1;
            }
        }


        float x_on_music = TEXT_SCALE * scale * music_on.getWidth() / 2;
        float distX_on_music = Math.abs(screenX - centerX_music_on);
        float y_on_music = TEXT_SCALE * scale * music_on.getHeight() / 2;
        float distY_on_music = Math.abs(screenY - centerY_music_on);
        if (distX_on_music < x_on_music && distY_on_music < y_on_music) {
            pressMusicOnState = 1;
        }

        float x_off_music = TEXT_SCALE * scale * music_off.getWidth() / 2;
        float distX_off_music = Math.abs(screenX - centerX_music_off);
        float y_off_music = TEXT_SCALE * scale * music_off.getHeight() / 2;
        float distY_off_music = Math.abs(screenY - centerY_music_off);
        if (distX_off_music < x_off_music && distY_off_music < y_off_music) {
            pressMusicOffState = 1;
        }

        float x_on_sound = TEXT_SCALE * scale * sound_on.getWidth() / 2;
        float distX_on_sound = Math.abs(screenX - centerX_sound_on);
        float y_on_sound = TEXT_SCALE * scale * sound_on.getHeight() / 2;
        float distY_on_sound = Math.abs(screenY - centerY_sound_on);
        if (distX_on_sound < x_on_sound && distY_on_sound < y_on_sound) {
            pressSoundOnState = 1;
        }

        float x_off_sound = TEXT_SCALE * scale * sound_off.getWidth() / 2;
        float distX_off_sound = Math.abs(screenX - centerX_sound_off);
        float y_off_sound = TEXT_SCALE * scale * sound_off.getHeight() / 2;
        float distY_off_sound = Math.abs(screenY - centerY_sound_off);
        if (distX_off_sound < x_off_sound && distY_off_sound < y_off_sound) {
            pressSoundOffState = 1;
        }



        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressBackState == 1) {
            pressBackState = 2;
            return false;
        }
        if (pressMusicOnState == 1) {
            pressMusicOnState = 2;
            return false;
        }
        if (pressSoundOnState == 1) {
            pressSoundOnState = 2;
            return false;
        }
        if (pressMusicOffState == 1) {
            pressMusicOffState = 2;
            return false;
        }
        if (pressSoundOffState == 1) {
            pressSoundOffState = 2;
            return false;
        }
        for (int i = 1; i <= 10; i++) {
            if (pressStarState[i] == 1) {
                pressStarState[i] = 2;
            }
        }

        for (int i = 1; i <= 10; i++) {
            if (pressStarSoundState[i] == 1) {
                pressStarSoundState[i] = 2;
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