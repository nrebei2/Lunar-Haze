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
     * Vertical gap between bars
     */
    private static final float LINE_DIST = 20.0f;

    /**
     * Horizontal gap between text and bar
     */
    private static final float TEXT_BAR_DIST = 120.0f;

    /**
     * Horizontal gap between bar and add button
     */
    private static final float BAR_ADD_DIST = 25.0f;

    /**
     * Width of the HP bar
     */
    private final static float BAR_WIDTH = 300f;

    /**
     * Height of the HP bar
     */
    private final static float BAR_HEIGHT = 30.0f;

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
     * The button being pressed by player
     * 0 - hp, 1 - attack power, 2 - attack range
     */
    private int buttonSelected;

    /**
     * Whether or not this player mode is still active
     */
    private boolean active;

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
        background = directory.getEntry("background", Texture.class);
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        addHpButton = directory.getEntry("add", Texture.class);
        addAttackPowButton = directory.getEntry("add", Texture.class);
        addAttackRanButton = directory.getEntry("add", Texture.class);
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

        centerY0 = (int) ((CENTER_BAR_HEIGHT_RATIO + (BAR_HEIGHT + LINE_DIST) * scale/height) * height);
        centerY1 = (int) (CENTER_BAR_HEIGHT_RATIO * height);
        centerY2 = (int) ((CENTER_BAR_HEIGHT_RATIO - (BAR_HEIGHT + LINE_DIST) * scale/height) * height);
        centerX = width / 2 + BAR_WIDTH;
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
        setFontColor(Color.BLACK);

        canvas.begin(GameCanvas.DrawPass.SPRITE);
        Color alphaTint = Color.WHITE;
        canvas.drawOverlay(background, alphaTint, true);

        // Temporary position for the display
        canvas.drawText("Remaining Moonlight: " + playerController.getPlayer().getMoonlightCollected(),
                UIFont_large, BAR_WIDTH, heightY - BAR_HEIGHT);

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
        canvas.end();

        float bar_centerX = centerX - BAR_WIDTH - addHpButton.getWidth()/2 - BAR_ADD_DIST;
        canvas.begin(GameCanvas.DrawPass.SHAPE);
        canvas.drawHpBar(bar_centerX, centerY0 - BAR_HEIGHT/2,
                BAR_WIDTH, BAR_HEIGHT, playerController.getPlayer().getHp());
        canvas.drawAttackPow(bar_centerX, centerY1 - BAR_HEIGHT/2,
                BAR_WIDTH, BAR_HEIGHT, playerController.getPlayer().getAttackPower());
        canvas.drawAttackRange(bar_centerX, centerY2 - BAR_HEIGHT/2,
                BAR_WIDTH, BAR_HEIGHT, playerController.getPlayer().getAttackRange());
        canvas.end();

        float text_centerX = centerX - BAR_WIDTH - TEXT_BAR_DIST - addHpButton.getWidth()/2 - BAR_ADD_DIST;
        canvas.begin(GameCanvas.DrawPass.SPRITE);
        canvas.drawText("HP", UIFont_small, text_centerX, centerY0);
        canvas.drawText("Attack Power", UIFont_small, text_centerX, centerY1);
        canvas.drawText("Attack Range", UIFont_small, text_centerX, centerY2);
        canvas.end();
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
//            System.out.println("Button: " + i);
//            System.out.println("Radius of button: " + radius);
//            System.out.println("Dist to center: " + dist);
//            System.out.println("Center X: " + centerX);
//            System.out.println("Center Y: " + buttonheights[i]);
//            System.out.println("Screen X: " + screenX);
//            System.out.println("Screen Y: " + screenY);
//            System.out.println(dist < radius * radius);
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