package infinityx.lunarhaze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.controllers.InputController;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.ScreenObservable;

/**
 * Provides a screen for selecting levels
 */
public class LevelSelectionMode extends ScreenObservable implements Screen, InputProcessor {
    /**
     * Whether or not this player mode is still active
     */
    private boolean active;
    /**
     * What level was selected by the player
     */
    private int levelSelected;
    /**
     * User requested to go back to menu
     */
    public final static int GO_BACK = 0;
    /**
     * User requested to go to a level
     */
    public final static int GO_Play = 1;
    /**
     * Need an ongoing reference to the asset directory
     */
    protected AssetDirectory directory;
    /**
     * Background texture for level selection screen
     */
    private Texture background;
    /**
     * Title texture for level selection screen
     */
    private Texture title;
    /**
     * Back texture for level selection screen
     */
    private Texture back;
    /**
     * Texture for level 1
     */
    private Texture level1;
    /**
     * Texture for level 2
     */
    private Texture level2;
    /**
     * Texture for level 3
     */
    private Texture level3;
    /**
     * Texture for level 4
     */
    private Texture level4;
    /**
     * Texture for level 5
     */
    private Texture level5;
    /**
     * Texture for level 6
     */
    private Texture level6;
    /**
     * Texture for level 7
     */
    private Texture level7;
    /**
     * Texture for level 8
     */
    private Texture level8;
    /**
     * Texture for level 9
     */
    private Texture level9;
    /**
     * Texture for level 10
     */
    private Texture level10;
    /**
     * Texture for level 11
     */
    private Texture level11;
    /**
     * Texture for level 12
     */
    private Texture level12;
    /**
     * Texture for level 13
     */
    private Texture level13;
    /**
     * Texture for level 14
     */
    private Texture level14;
    /**
     * Texture for level 15
     */
    private Texture level15;
    /**
     * Texture for level 1 number
     */
    private Texture no1;
    /**
     * Texture for level 2 number
     */
    private Texture no2;
    /**
     * Texture for level 3 number
     */
    private Texture no3;
    /**
     * Texture for level 4 number
     */
    private Texture no4;
    /**
     * Texture for level 5 number
     */
    private Texture no5;
    /**
     * Texture for level 6 number
     */
    private Texture no6;
    /**
     * Texture for level 7 number
     */
    private Texture no7;
    /**
     * Texture for level 8 number
     */
    private Texture no8;
    /**
     * Texture for level 9 number
     */
    private Texture no9;
    /**
     * Texture for level 10 number
     */
    private Texture no10;
    /**
     * Texture for level 11 number
     */
    private Texture no11;
    /**
     * Texture for level 12 number
     */
    private Texture no12;
    /**
     * Texture for level 13 number
     */
    private Texture no13;
    /**
     * Texture for level 14 number
     */
    private Texture no14;
    /**
     * Texture for level 15 number
     */
    private Texture no15;

    /**
     * The current state of the level 1 button
     */
    private int press1State;
    /**
     * The current state of the level 2 button
     */
    private int press2State;
    /**
     * The current state of the level 3 button
     */
    private int press3State;
    /**
     * The current state of the level 4 button
     */
    private int press4State;
    /**
     * The current state of the level 5 button
     */
    private int press5State;
    /**
     * The current state of the level 6 button
     */
    private int press6State;
    /**
     * The current state of the level 7 button
     */
    private int press7State;
    /**
     * The current state of the level 8 button
     */
    private int press8State;
    /**
     * The current state of the level 9 button
     */
    private int press9State;
    /**
     * The current state of the level 10 button
     */
    private int press10State;
    /**
     * The current state of the level 11 button
     */
    private int press11State;
    /**
     * The current state of the level 12 button
     */
    private int press12State;
    /**
     * The current state of the level 13 button
     */
    private int press13State;
    /**
     * The current state of the level 14 button
     */
    private int press14State;
    /**
     * The current state of the level 15 button
     */
    private int press15State;
    /**
     * The current state of the back button
     */
    private int pressBackState;
    /**
     * The x-coordinate of the center of the back button
     */
    private int centerX_back;
    /**
     * The x-coordinate of the center of the column 1 buttons
     */
    private int centerX_column1;
    /**
     * The x-coordinate of the center of the column 2 buttons
     */
    private int centerX_column2;
    /**
     * The x-coordinate of the center of the column 3 buttons
     */
    private int centerX_column3;
    /**
     * The x-coordinate of the center of the column 4 buttons
     */
    private int centerX_column4;
    /**
     * The x-coordinate of the center of the column 5 buttons
     */
    private int centerX_column5;
    /**
     * The y-coordinate of the center of the back buttons
     */
    private int centerY_back;
    /**
     * The y-coordinate of the center of the row 1 buttons
     */
    private int centerY_row1;
    /**
     * The y-coordinate of the center of the row 2 buttons
     */
    private int centerY_row2;
    /**
     * The y-coordinate of the center of the row 3 buttons
     */
    private int centerY_row3;

    /**
     * Scale for buttons
     */
    private static final float BUTTON_SCALE = 0.8f;
    /**
     * Scale for title
     */
    private static final float TITLE_SCALE = 0.2f;

    /**
     * Scale for back
     */
    private static final float BACK_SCALE = 0.8f;
    /**
     * Ratio of back height from bottom
     */
    private static final float BACK_HEIGHT_RATIO = 0.93f;
    /**
     * Ratio of row1 height from bottom
     */
    private static final float ROW1_HEIGHT_RATIO = 0.64f;
    /**
     * Ratio of row2 height from bottom
     */
    private static final float ROW2_HEIGHT_RATIO = 0.45f;
    /**
     * Ratio of row3 height from bottom
     */
    private static final float ROW3_HEIGHT_RATIO = 0.26f;
    /**
     * Ratio of the difference of numbers height from their buttons
     */
    private static final float NO_HEIGHT_OFFSET_RATIO = 0.075f;
    /**
     * Ratio of back width from bottom
     */
    private static final float BACK_WIDTH_RATIO = 0.1f;
    /**
     * Ratio of column1 width from bottom
     */
    private static final float COLUMN1_WIDTH_RATIO = 0.26f;
    /**
     * Ratio of column2 width from bottom
     */
    private static final float COLUMN2_WIDTH_RATIO = 0.38f;
    /**
     * Ratio of column3 width from bottom
     */
    private static final float COLUMN3_WIDTH_RATIO = 0.5f;
    /**
     * Ratio of column4 width from bottom
     */
    private static final float COLUMN4_WIDTH_RATIO = 0.62f;
    /**
     * Ratio of column5 width from bottom
     */
    private static final float COLUMN5_WIDTH_RATIO = 0.74f;
    /**
     * Ratio of the difference of numbers width from their buttons
     */
    private static final float NO_WIDTH_OFFSET_RATIO = 0.035f;


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
    public boolean isReady1() {
        return press1State == 2;
    }

    /**
     * @return true if the player is ready to restart the game
     */
    public boolean isReady2() {
        return press2State == 2;
    }

    /**
     * @return true if the player is ready to review the setting
     */
    public boolean isReady3() {
        return press3State == 2;
    }

    /**
     * @return true if the player is ready to exit to the menu
     */
    public boolean isReady4() {
        return press4State == 2;
    }

    /**
     * @return true if the player is ready to quit the game
     */
    public boolean isReady5() {
        return press5State == 2;
    }

    /**
     * @return true if the player is ready to resume the game
     */
    public boolean isReady6() {
        return press6State == 2;
    }

    /**
     * @return true if the player is ready to restart the game
     */
    public boolean isReady7() {
        return press7State == 2;
    }

    /**
     * @return true if the player is ready to review the setting
     */
    public boolean isReady8() {
        return press8State == 2;
    }

    /**
     * @return true if the player is ready to exit to the menu
     */
    public boolean isReady9() {
        return press9State == 2;
    }

    /**
     * @return true if the player is ready to quit the game
     */
    public boolean isReady10() {
        return press10State == 2;
    }

    /**
     * @return true if the player is ready to resume the game
     */
    public boolean isReady11() {
        return press11State == 2;
    }

    /**
     * @return true if the player is ready to restart the game
     */
    public boolean isReady12() {
        return press12State == 2;
    }

    /**
     * @return true if the player is ready to review the setting
     */
    public boolean isReady13() {
        return press13State == 2;
    }

    /**
     * @return true if the player is ready to exit to the menu
     */
    public boolean isReady14() {
        return press14State == 2;
    }

    /**
     * @return true if the player is ready to quit the game
     */
    public boolean isReady15() {
        return press15State == 2;
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
    public LevelSelectionMode(GameCanvas canvas) {
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
        background = directory.getEntry("background-level-selection", Texture.class);
        title = directory.getEntry("level-selection-title", Texture.class);
        back = directory.getEntry("level-selection-back", Texture.class);
        level1 = directory.getEntry("level1", Texture.class);
        level2 = directory.getEntry("level2", Texture.class);
        level3 = directory.getEntry("level3", Texture.class);
        level4 = directory.getEntry("level4", Texture.class);
        level5 = directory.getEntry("level5", Texture.class);
        level6 = directory.getEntry("level6", Texture.class);
        level7 = directory.getEntry("level7", Texture.class);
        level8 = directory.getEntry("level8", Texture.class);
        level9 = directory.getEntry("level9", Texture.class);
        level10 = directory.getEntry("level10", Texture.class);
        level11 = directory.getEntry("level11", Texture.class);
        level12 = directory.getEntry("level12", Texture.class);
        level13 = directory.getEntry("level13", Texture.class);
        level14 = directory.getEntry("level14", Texture.class);
        level15 = directory.getEntry("level15", Texture.class);
        no1 = directory.getEntry("no1", Texture.class);
        no2 = directory.getEntry("no2", Texture.class);
        no3 = directory.getEntry("no3", Texture.class);
        no4 = directory.getEntry("no4", Texture.class);
        no5 = directory.getEntry("no5", Texture.class);
        no6 = directory.getEntry("no6", Texture.class);
        no7 = directory.getEntry("no7", Texture.class);
        no8 = directory.getEntry("no8", Texture.class);
        no9 = directory.getEntry("no9", Texture.class);
        no10 = directory.getEntry("no10", Texture.class);
        no11 = directory.getEntry("no11", Texture.class);
        no12 = directory.getEntry("no12", Texture.class);
        no13 = directory.getEntry("no13", Texture.class);
        no14 = directory.getEntry("no14", Texture.class);
        no15 = directory.getEntry("no15", Texture.class);
    }

    public int getLevelSelected() {
        return levelSelected;
    }

    public void setLevelSelected(int levelSelected) {
        this.levelSelected = levelSelected;
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
        canvas.beginUI(GameCanvas.DrawPass.SPRITE);
        Color alphaTint = Color.WHITE;
        canvas.drawOverlay(background, alphaTint, true);
        canvas.draw(title, alphaTint, title.getWidth() / 2, title.getHeight() / 2, canvas.getWidth() / 2, canvas.getHeight() * 0.83f, 0, scale * TITLE_SCALE, scale * TITLE_SCALE);
        Color tintBack = (pressBackState == 1 ? Color.GRAY : Color.WHITE);
        canvas.draw(back, tintBack, back.getWidth() / 2, back.getHeight() / 2, centerX_back, centerY_back, 0, scale * BACK_SCALE, scale * BACK_SCALE);

        int[][] levelStates = {
                {press1State, press2State, press3State, press4State, press5State},
                {press6State, press7State, press8State, press9State, press10State},
                {press11State, press12State, press13State, press14State, press15State}
        };

        Texture[] levelTextures = {level1, level2, level3, level4, level5, level6, level7, level8, level9, level10, level11, level12, level13, level14, level15};
        Texture[] noTextures = {no1, no2, no3, no4, no5, no6, no7, no8, no9, no10, no11, no12, no13, no14, no15};

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                int index = row * 5 + col;
                float centerX = centerX_column1 + col * (centerX_column2 - centerX_column1);
                float centerY = centerY_row1 + row * (centerY_row2 - centerY_row1);
                drawLevel(levelStates[row][col], levelTextures[index], noTextures[index], centerX, centerY);
            }
        }

        canvas.end();
    }

    private void drawLevel(int pressState, Texture levelTexture, Texture noTexture, float centerX, float centerY) {
        Color tint = (pressState == 1 ? Color.BLACK : Color.WHITE);
        canvas.draw(levelTexture, tint, levelTexture.getWidth() / 2, levelTexture.getHeight() / 2, centerX, centerY, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        canvas.draw(noTexture, Color.WHITE, noTexture.getWidth() / 2, noTexture.getHeight() / 2, centerX + canvas.getWidth() * NO_WIDTH_OFFSET_RATIO, centerY + canvas.getHeight() * NO_HEIGHT_OFFSET_RATIO, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
    }


    @Override
    public void show() {
        active = true;
        press1State = 0;
        press2State = 0;
        press3State = 0;
        press4State = 0;
        press5State = 0;
        press6State = 0;
        press7State = 0;
        press8State = 0;
        press9State = 0;
        press10State = 0;
        press11State = 0;
        press12State = 0;
        press13State = 0;
        press14State = 0;
        press15State = 0;
        pressBackState = 0;
        Gdx.input.setInputProcessor(this);

    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (Gdx.input.isKeyPressed(Input.Keys.T)) {
                setLevelSelected(0);
                observer.exitScreen(this, GO_Play);
            }

            if (isReady1() && observer != null) {
                setLevelSelected(1);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady2() && observer != null) {
                setLevelSelected(2);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady3() && observer != null) {
                setLevelSelected(3);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady4() && observer != null) {
                setLevelSelected(4);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady5() && observer != null) {
                setLevelSelected(5);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady6() && observer != null) {
                setLevelSelected(6);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady7() && observer != null) {
                setLevelSelected(7);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady8() && observer != null) {
                setLevelSelected(8);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady9() && observer != null) {
                setLevelSelected(9);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady10() && observer != null) {
                setLevelSelected(10);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady11() && observer != null) {
                setLevelSelected(11);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady12() && observer != null) {
                setLevelSelected(12);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady13() && observer != null) {
                setLevelSelected(13);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady14() && observer != null) {
                setLevelSelected(14);
                observer.exitScreen(this, GO_Play);
            }
            if (isReady15() && observer != null) {
                setLevelSelected(15);
                observer.exitScreen(this, GO_Play);
            }
            if (isReadyBack() && observer != null) {
                observer.exitScreen(this, GO_BACK);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        centerX_back = (int) (BACK_WIDTH_RATIO * width);
        centerX_column1 = (int) (COLUMN1_WIDTH_RATIO * width);
        centerX_column2 = (int) (COLUMN2_WIDTH_RATIO * width);
        centerX_column3 = (int) (COLUMN3_WIDTH_RATIO * width);
        centerX_column4 = (int) (COLUMN4_WIDTH_RATIO * width);
        centerX_column5 = (int) (COLUMN5_WIDTH_RATIO * width);

        centerY_back = (int) (BACK_HEIGHT_RATIO * height);
        centerY_row1 = (int) (ROW1_HEIGHT_RATIO * height);
        centerY_row2 = (int) (ROW2_HEIGHT_RATIO * height);
        centerY_row3 = (int) (ROW3_HEIGHT_RATIO * height);

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
        if (level1 == null || press1State == 2 || press2State == 2 || press3State == 2 || press4State == 2) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY - screenY;

        float x = BUTTON_SCALE * scale * back.getWidth() / 2;
        float distX = Math.abs(screenX - centerX_back);
        float y = BUTTON_SCALE * scale * back.getHeight() / 2;

        float distYBack = Math.abs(screenY - centerY_back);
        if (distX < x && distYBack < y) {
            pressBackState = 1;
        }

        // TODO: Fix scaling
        // Level button is a circle.
        float radius1 = BUTTON_SCALE * scale * level1.getWidth() / 2.0f;
        float dist1 = (screenX - centerX_column1) * (screenX - centerX_column1) + (screenY - centerY_row1) * (screenY - centerY_row1);
        if (dist1 < radius1 * radius1) {
            press1State = 1;
        }

        float radius2 = BUTTON_SCALE * scale * level2.getWidth() / 2.0f;
        float dist2 = (screenX - centerX_column2) * (screenX - centerX_column2) + (screenY - centerY_row1) * (screenY - centerY_row1);
        if (dist2 < radius2 * radius2) {
            press2State = 1;
        }

        float radius3 = BUTTON_SCALE * scale * level3.getWidth() / 2.0f;
        float dist3 = (screenX - centerX_column3) * (screenX - centerX_column3) + (screenY - centerY_row1) * (screenY - centerY_row1);
        if (dist3 < radius3 * radius3) {
            press3State = 1;
        }

        float radius4 = BUTTON_SCALE * scale * level4.getWidth() / 2.0f;
        float dist4 = (screenX - centerX_column4) * (screenX - centerX_column4) + (screenY - centerY_row1) * (screenY - centerY_row1);
        if (dist4 < radius4 * radius4) {
            press4State = 1;
        }

        float radius5 = BUTTON_SCALE * scale * level5.getWidth() / 2.0f;
        float dist5 = (screenX - centerX_column5) * (screenX - centerX_column5) + (screenY - centerY_row1) * (screenY - centerY_row1);
        if (dist5 < radius5 * radius5) {
            press5State = 1;
        }

        float radius6 = BUTTON_SCALE * scale * level6.getWidth() / 2.0f;
        float dist6 = (screenX - centerX_column1) * (screenX - centerX_column1) + (screenY - centerY_row2) * (screenY - centerY_row2);
        if (dist6 < radius6 * radius6) {
            press6State = 1;
        }

        float radius7 = BUTTON_SCALE * scale * level7.getWidth() / 2.0f;
        float dist7 = (screenX - centerX_column2) * (screenX - centerX_column2) + (screenY - centerY_row2) * (screenY - centerY_row2);
        if (dist7 < radius7 * radius7) {
            press7State = 1;
        }

        float radius8 = BUTTON_SCALE * scale * level8.getWidth() / 2.0f;
        float dist8 = (screenX - centerX_column3) * (screenX - centerX_column3) + (screenY - centerY_row2) * (screenY - centerY_row2);
        if (dist8 < radius8 * radius8) {
            press8State = 1;
        }

        float radius9 = BUTTON_SCALE * scale * level9.getWidth() / 2.0f;
        float dist9 = (screenX - centerX_column4) * (screenX - centerX_column4) + (screenY - centerY_row2) * (screenY - centerY_row2);
        if (dist9 < radius9 * radius9) {
            press9State = 1;
        }

        float radius10 = BUTTON_SCALE * scale * level10.getWidth() / 2.0f;
        float dist10 = (screenX - centerX_column5) * (screenX - centerX_column5) + (screenY - centerY_row2) * (screenY - centerY_row2);
        if (dist10 < radius10 * radius10) {
            press10State = 1;
        }

        float radius11 = BUTTON_SCALE * scale * level11.getWidth() / 2.0f;
        float dist11 = (screenX - centerX_column1) * (screenX - centerX_column1) + (screenY - centerY_row3) * (screenY - centerY_row3);
        if (dist11 < radius11 * radius11) {
            press11State = 1;
        }

        float radius12 = BUTTON_SCALE * scale * level12.getWidth() / 2.0f;
        float dist12 = (screenX - centerX_column2) * (screenX - centerX_column2) + (screenY - centerY_row3) * (screenY - centerY_row3);
        if (dist12 < radius12 * radius12) {
            press12State = 1;
        }

        float radius13 = BUTTON_SCALE * scale * level13.getWidth() / 2.0f;
        float dist13 = (screenX - centerX_column3) * (screenX - centerX_column3) + (screenY - centerY_row3) * (screenY - centerY_row3);
        if (dist13 < radius13 * radius13) {
            press13State = 1;
        }

        float radius14 = BUTTON_SCALE * scale * level14.getWidth() / 2.0f;
        float dist14 = (screenX - centerX_column4) * (screenX - centerX_column4) + (screenY - centerY_row3) * (screenY - centerY_row3);
        if (dist14 < radius14 * radius14) {
            press14State = 1;
        }

        float radius15 = BUTTON_SCALE * scale * level15.getWidth() / 2.0f;
        float dist15 = (screenX - centerX_column5) * (screenX - centerX_column5) + (screenY - centerY_row3) * (screenY - centerY_row3);
        if (dist15 < radius15 * radius15) {
            press15State = 1;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressBackState == 1) {
            pressBackState = 2;
            return false;
        }
        if (press1State == 1) {
            press1State = 2;
            return false;
        }
        if (press2State == 1) {
            press2State = 2;
            return false;
        }
        if (press3State == 1) {
            press3State = 2;
            return false;
        }
        if (press4State == 1) {
            press4State = 2;
            return false;
        }
        if (press5State == 1) {
            press5State = 2;
            return false;
        }
        if (press6State == 1) {
            press6State = 2;
            return false;
        }
        if (press7State == 1) {
            press7State = 2;
            return false;
        }
        if (press8State == 1) {
            press8State = 2;
            return false;
        }
        if (press9State == 1) {
            press9State = 2;
            return false;
        }
        if (press10State == 1) {
            press10State = 2;
            return false;
        }
        if (press11State == 1) {
            press11State = 2;
            return false;
        }
        if (press12State == 1) {
            press12State = 2;
            return false;
        }
        if (press13State == 1) {
            press13State = 2;
            return false;
        }
        if (press14State == 1) {
            press14State = 2;
            return false;
        }
        if (press15State == 1) {
            press15State = 2;
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
