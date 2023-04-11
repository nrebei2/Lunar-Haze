package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.combat.PlayerAttackHandler;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.ScreenObservable;

/**
 * Provides a menu screen for level select
 */
public class AllocateScreen extends ScreenObservable implements Screen, InputProcessor {

    /**
     * User request to continue the game
     */
    public static final int GO_PLAY = 2;

    /**
     * Background texture for allocate screen
     */
    private Texture background;

    /**
     * Button to add hp
     */
    private Texture addHpButton;

    /**
     * Button to add attack power
     */
    private Texture addAttackPowButton;

    /**
     * Button to add attack range
     */
    private Texture addAttackRanButton;

    /**
     * Icon representing moon
     */
    private Texture moon_icon;

    /**
     * Icon representing health
     */
    private Texture heart_icon;

    /**
     * Icon representing attack power
     */
    private Texture attack_pow_icon;

    /**
     * Icon representing attack range
     */
    private Texture attack_ran_icon;

    /**
     * Texture for background stroke
     */
    private Texture stroke;

    /**
     * Texture for empty star
     */
    private Texture star_empty;

    /**
     * Texture for filled star
     */
    private Texture star_filled;

    /**
     * Left title ornament
     */
    private Texture title_left;

    /**
     * Right title ornament
     */
    private Texture title_right;

    private static final float CENTER_BAR_HEIGHT_RATIO = 0.5f;

    private static final float BUTTON_SCALE = 0.75f;

    /**
     * Standard window size (for scaling)
     */
    private static final int STANDARD_WIDTH = 800;
    /**
     * Standard window height (for scaling)
     */
    private static final int STANDARD_HEIGHT = 700;

    /**
     * Width and height of moon icon
     */
    private static final float MOON_ICON_WIDTH = 30.0f;

    /**
     * Width and height of small icons
     */
    private static final float SMALL_ICON_WIDTH = 30.0f;

    /**
     * Width of strokes
     */
    private static final float STROKE_WIDTH = 60.0f;

    /**
     * Height of strokes
     */
    private static final float STROKE_HEIGHT = 45.0f;

    /**
     * Width of stars
     */
    private static final float STAR_WIDTH = 25.0f;

    /**
     * Height of stars
     */
    private static final float STAR_HEIGHT = STAR_WIDTH;

    /**
     * Distance between attributes
     */
    private float LINE_DIST;

    /**
     * Gap between lines
     */
    private static final float INLINE_DIST = 45.0f;

    /**
     * Large font for UI
     */
    protected BitmapFont UIFont_large;

    /**
     * Small font for UI
     */
    protected BitmapFont UIFont_small;

    /**
     * The height of the canvas window (necessary since sprite origin != screen origin)
     */
    private int heightY;
    /**
     * Scaling factor for when the student changes the resolution.
     */
    private float scale;

    /**
     * Reference to GameCanvas created by the root
     */
    private GameCanvas canvas;

    /**
     * Reference to playerController
     */
    private PlayerController playerController;

    /**
     * Reference to gameplayController
     */
    private GameMode gameMode;

    /**
     * The current state of the HP button
     */
    private int pressStateHp;

    /**
     * The current state of the attack power button
     */
    private int pressStateAttackPow;

    /**
     * The current state of the attack range button
     */
    private int pressStateAttackRan;

    /**
     * X coordinate of the 3 buttons
     */
    private float centerX;

    /**
     * Y coordinate of center bar
     */
    private float centerY;

    /**
     * Y coordinate of the button 0
     */
    private float centerY0;

    /**
     * Y coordinate of the button 1
     */
    private float centerY1;

    /**
     * Y coordinate of the button 2
     */
    private float centerY2;

    /**
     * Whether or not this player mode is still active
     */
    private boolean active;

    /**
     * Total moonlight collected for display
     */
    private int totalMoonlightCollected;

    /**
     * Whether it's first time rendering
     */
    private boolean firstRender = true;

    /**
     * Creates a new Allocate screen
     *
     * @param canvas The game canvas to draw to
     */
    public AllocateScreen(GameCanvas canvas, GameMode gameMode) {
        this.canvas = canvas;
        this.gameMode = gameMode;
    }

    public void setGameMode(GameMode gm){
        gameMode = gm;
    }

    public void setCanvas(GameCanvas gc){
        canvas = gc;
    }

    public GameplayController getGameplayController(){
        return gameMode.getGameplayController();
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
        background = directory.getEntry("bkg_allocate", Texture.class);
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        addHpButton = directory.getEntry("add", Texture.class);
        addAttackPowButton = directory.getEntry("add", Texture.class);
        addAttackRanButton = directory.getEntry("add", Texture.class);

        moon_icon = directory.getEntry("moon-icon", Texture.class);
        heart_icon = directory.getEntry("health-icon", Texture.class);
        attack_pow_icon = directory.getEntry("attack-pow-icon", Texture.class);
        attack_ran_icon = directory.getEntry("attack-pow-icon", Texture.class);
        stroke = directory.getEntry("square-stroke", Texture.class);

        star_empty = directory.getEntry("star-empty", Texture.class);
        star_filled = directory.getEntry("star-filled", Texture.class);
        title_left = directory.getEntry("title-left", Texture.class);
        title_right = directory.getEntry("title-right", Texture.class);

        UIFont_large = directory.getEntry("libre-large", BitmapFont.class);
        UIFont_small = directory.getEntry("libre-small", BitmapFont.class);
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {

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
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        LINE_DIST = height / 4;

        centerY0 = (int) (CENTER_BAR_HEIGHT_RATIO * height + LINE_DIST - INLINE_DIST);
        centerY1 = (int) (CENTER_BAR_HEIGHT_RATIO * height - INLINE_DIST);
        centerY2 = (int) (CENTER_BAR_HEIGHT_RATIO * height - LINE_DIST - INLINE_DIST);
        centerX = width * 0.75f;
        heightY = height;
    }

    /**
     * Update the status of this menu.
     */
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
        setFontColor(Color.WHITE);

        canvas.begin(GameCanvas.DrawPass.SPRITE);
        Color alphaTint = Color.WHITE;
        canvas.drawOverlay(background, alphaTint, true);

        // Temporary position for the display
        canvas.draw(moon_icon, alphaTint, canvas.getWidth()/4 - MOON_ICON_WIDTH/2, canvas.getHeight()/2, MOON_ICON_WIDTH, MOON_ICON_WIDTH);
        canvas.drawText("" + playerController.getPlayer().getMoonlightCollected(),
                UIFont_large, canvas.getWidth()/4 + MOON_ICON_WIDTH, canvas.getHeight()/2 + MOON_ICON_WIDTH * 0.8f);
        Color gray = new Color(255f/255.0f, 255f/255.0f, 255f/255.0f, 0.4f);
        setFontColor(gray);
        canvas.drawText(" / " + totalMoonlightCollected,
                UIFont_small, canvas.getWidth()/4 + MOON_ICON_WIDTH + UIFont_small.getAscent() * 4, canvas.getHeight()/2 + MOON_ICON_WIDTH/2);
        setFontColor(alphaTint);
        canvas.drawText("Moonlight Remaining",
                UIFont_small, canvas.getWidth()/4 - MOON_ICON_WIDTH * 3/2, canvas.getHeight()/2 - MOON_ICON_WIDTH/2);

        Color tint0 = (pressStateHp == 1 ? Color.GRAY : Color.WHITE);
        Color tint1 = (pressStateAttackPow == 1 ? Color.GRAY : Color.WHITE);
        Color tint2 = (pressStateAttackRan == 1 ? Color.GRAY : Color.WHITE);

        canvas.draw(addHpButton, tint0, addHpButton.getWidth() / 2, addHpButton.getHeight() / 2,
                centerX, centerY0, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        canvas.draw(addAttackPowButton, tint1, addAttackPowButton.getWidth() / 2, addAttackPowButton.getHeight() / 2,
                centerX, centerY1, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        canvas.draw(addAttackRanButton, tint2, addAttackPowButton.getWidth() / 2, addAttackPowButton.getHeight() / 2,
                centerX, centerY2, 0,
                BUTTON_SCALE * scale, BUTTON_SCALE * scale);

        drawStats("Attack Power");
        drawStats("Attack Range");
        drawStats("Health");
        canvas.end();
    }

    /**
     * Draw stats around button
     */
    public void drawStats(String s){
        float buttonCenterX = centerX;
        float buttonCenterY;
        Texture icon;
        int stat;
        if (s == "Attack Power") {
            Texture currButton = addAttackPowButton;
            buttonCenterY = centerY1;
            icon = attack_pow_icon;
            stat = (int) (playerController.getPlayer().getAttackPower() / 0.1);
        } else if (s == "Attack Range") {
            Texture currButton = addAttackRanButton;
            buttonCenterY = centerY2;
            icon = attack_ran_icon;
            stat = (int) ((playerController.getPlayer().getAttackRange() - 1) / 0.1);
        } else {
            Texture currButton = addHpButton;
            buttonCenterY = centerY0;
            icon = heart_icon;
            stat = playerController.getPlayer().getHp();
        }

        canvas.drawText("" + stat,
                UIFont_small, buttonCenterX - UIFont_small.getAscent() * 17, buttonCenterY + UIFont_small.getLineHeight()/2);
        Color gray = new Color(1.0f, 1.0f, 1.0f, 0.4f);
        setFontColor(gray);
        canvas.drawText(" / " + 10,
                UIFont_small, buttonCenterX - UIFont_small.getAscent() * 14, buttonCenterY + UIFont_small.getLineHeight()/2);

        float starY = buttonCenterY + INLINE_DIST;
        canvas.draw(stroke, Color.WHITE, (float) (stroke.getWidth() / 2), (float) (stroke.getHeight() / 2),
                centerX - 6 * STAR_WIDTH - STROKE_WIDTH, starY, 0,
                STROKE_WIDTH / stroke.getWidth(), STROKE_HEIGHT / stroke.getHeight());
        float angle = 0.0f;
        if (icon == attack_pow_icon){
            angle = 30.f / 180.0f *  (float) Math.PI;
        }
        canvas.draw(icon, Color.WHITE, (float) (icon.getWidth() / 2), (float) (icon.getHeight() / 2),
                centerX - 6 * STAR_WIDTH - STROKE_WIDTH, starY, angle,
                SMALL_ICON_WIDTH / icon.getWidth(), SMALL_ICON_WIDTH / icon.getHeight());
        for (int i = 1; i <= 10; i++) {
            if (stat >= i) {
                // Draw a filled star for the ith star
                canvas.draw(star_filled, Color.WHITE, star_filled.getWidth() / 2, star_filled.getHeight() / 2,
                        centerX + (i - 6) * STAR_WIDTH, starY, 0,
                        STAR_WIDTH / star_filled.getWidth(), STAR_HEIGHT / star_filled.getHeight());
            } else {
                // Draw an empty heart for the ith heart
                canvas.draw(star_empty, Color.WHITE, star_filled.getWidth() / 2, star_filled.getHeight() / 2,
                        centerX + (i - 6) * STAR_WIDTH, starY, 0,
                        STAR_WIDTH / star_filled.getWidth(), STAR_HEIGHT / star_filled.getHeight());
            }
        }

        float titleY = starY + INLINE_DIST;
        setFontColor(Color.WHITE);
        canvas.draw(title_left, Color.WHITE, centerX - 6 * STAR_WIDTH - STAR_WIDTH * 0.5f, titleY - UIFont_small.getCapHeight(),
                STAR_WIDTH * 3.5f, UIFont_small.getCapHeight());
        canvas.drawText(s, UIFont_small, centerX - STAR_WIDTH - s.length() * UIFont_small.getAscent(), titleY);
        canvas.draw(title_right, Color.WHITE, centerX + STAR_WIDTH * 1.5f, titleY - UIFont_small.getCapHeight(),
                STAR_WIDTH * 3.5f, UIFont_small.getCapHeight());
    }

    /**
     * Set color for the two fonts for UI drawing
     */
    public void setFontColor(Color color){
        UIFont_large.setColor(color);
        UIFont_small.setColor(color);
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
            playerController = gameMode.getGameplayController().getPlayerController();
            if (firstRender) {
                totalMoonlightCollected = playerController.getPlayer().getMoonlightCollected();
                firstRender = false;
            }
            update(delta);
            draw();
//            System.out.println("States: " + pressStateHp + pressStateAttackPow + pressStateAttackRan);

            if (pressStateHp == 2 && playerController.getPlayer().getHp() < Werewolf.MAX_HP){
                playerController.allocateHp();
                pressStateHp = 0;
            } else if (pressStateAttackPow == 2 && playerController.getPlayer().getAttackPower() < Werewolf.MAX_POWER){
                playerController.allocateAttackPow();
                pressStateAttackPow = 0;
            } else if (pressStateAttackRan == 2 && playerController.getPlayer().getAttackRange() < Werewolf.MAX_RANGE){
                playerController.allocateAttackRange();
                pressStateAttackRan = 0;
            }
            if (playerController.getPlayer().getMoonlightCollected() <= 0 && observer != null){
                active = false;
                playerController.setAllocateReady(true);
                PlayerAttackHandler.setAttackPower(playerController.getPlayer().getAttackPower());
                PlayerAttackHandler.setAttackRange(playerController.getPlayer().getAttackRange());
                gameMode.getGameplayController().setPhase(GameplayController.Phase.BATTLE);
                observer.exitScreen(this, GO_PLAY);
            }
        }
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
        pressStateHp = 0;
        pressStateAttackPow = 0;
        pressStateAttackRan = 0;
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
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Flip to match graphics coordinates
        screenY = heightY - screenY;

        float[] buttonheights = {centerY0, centerY1, centerY2};
        int[] pressStates = {pressStateHp, pressStateAttackPow, pressStateAttackRan};
        Texture[] buttonList = {addHpButton, addAttackPowButton, addAttackRanButton};

        for (int i = 0; i < 3; i++) {
            if (buttonList[i] == null || pressStates[i] == 2) {
                return true;
            }
            Texture curr_button = buttonList[i];
            float radius = BUTTON_SCALE * scale * curr_button.getWidth() / 2.0f;
            float dist = (screenX - centerX) * (screenX - centerX) + (screenY - buttonheights[i]) * (screenY - buttonheights[i]);
            if (dist < radius * radius) {
                System.out.println("Button" + i + "touch down");
                if (i == 0){
                    pressStateHp = 1;
                } else if (i == 1){
                    pressStateAttackPow = 1;
                } else if (i == 2){
                    pressStateAttackRan = 1;
                }
            }
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
        if (pressStateHp == 1){
            pressStateHp = 2;
        } else if (pressStateAttackPow == 1){
            pressStateAttackPow = 2;
        } else if (pressStateAttackRan == 1){
            pressStateAttackRan = 2;
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

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

}