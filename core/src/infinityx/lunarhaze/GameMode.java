package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.util.ScreenObservable;

/**
 * The primary controller class for the game.
 * This is the player mode class for running the game. In initializes all
 * of the other classes in the game and hooks them together.  It also provides the
 * basic game loop (update-draw).
 */
public class GameMode extends ScreenObservable implements Screen {
    /**
     * Need an ongoing reference to the asset directory
     */
    protected AssetDirectory directory;
    // Exit codes
    /**
     * User requested to go to menu
     */
    public final static int GO_MENU = 0;

    /**
     * Width of the HP bar
     */
    private final static float BAR_WIDTH = 300f;
    /**
     * Height of the HP bar
     */
    private final static float BAR_HEIGHT = 40.0f;

    /**
     * Track the current state of the game for the update loop.
     */
    public enum GameState {
        /**
         * Before the game has started
         */
        INTRO,
        /**
         * While we are playing the game
         */
        PLAY,
        /**
         * The werewolf is dead
         */
        OVER,
        /**
         * The werewolf has prevailed!
         */
        WIN
    }

    /**
     * Owns the GameplayController
     */
    private GameplayController gameplayController;
    /**
     * Reference to drawing context to display graphics (VIEW CLASS)
     */
    private GameCanvas canvas;
    /**
     * Reads input from keyboard or game pad (CONTROLLER CLASS)
     */
    private InputController inputController;
    /**
     * Contains level details! May be null.
     */
    private LevelContainer levelContainer;
    /**
     * Constants for level initialization
     */
    private JsonValue levelFormat;
    /**
     * The font for giving messages to the player
     */
    protected BitmapFont displayFont;
    /**
     * Variable to track total time played in milliseconds (SIMPLE FIELDS)
     */
    private float totalTime = 0;
    /**
     * Whether or not this player mode is still active
     */
    private boolean active;
    /**
     * Variable to track the game state (SIMPLE FIELDS)
     */
    private GameState gameState;

    // TODO: Maybe change to enum if there are not that many levels, or string maybe?
    /**
     * Current level
     */
    private int level;

    public GameMode(GameCanvas canvas) {
        this.canvas = canvas;
        active = false;
        gameState = GameState.INTRO;
        // Create the controllers:
        inputController = new InputController();
        gameplayController = new GameplayController();
    }

    /**
     * Set the current level
     *
     * @param level
     */
    public void setLevel(int level) {
        //TODO DELETE ONE OF THESE
        this.level = level;
        // must reload level container and controllers
        this.gameState = GameState.INTRO;
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

        inputController.loadConstants(directory);
        levelFormat = directory.getEntry("levels", JsonValue.class);
        displayFont = directory.getEntry("retro", BitmapFont.class);
    }

    /**
     * Update the game state.
     * <p>
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {
        // Process the game input
        inputController.readKeyboard();

        // Test whether to reset the game.
        switch (gameState) {
            case INTRO:
                setupLevel();
                gameplayController.start(levelContainer);
                gameState = GameState.PLAY;
                break;
            case OVER:
            case WIN:
                if (inputController.didReset()) {
                    gameState = GameState.INTRO;
                    gameplayController.reset();
                } else {
                    play(delta);
                }
                break;
            case PLAY:
                play(delta);
                if (gameplayController.isGameWon()) {
                    gameState = GameState.WIN;
                } else if (gameplayController.isGameLost()) {
                    gameState = GameState.OVER;
                }
                break;
            default:
                break;
        }
    }

    /**
     * Initializes the levelContainer given the set level
     */
    private void setupLevel() {
        LevelParser ps = LevelParser.LevelParser();
        levelContainer = ps.loadLevel(directory, levelFormat.get(String.valueOf(level)));
    }

    /**
     * Initializes the levelContainer given the set level
     */
    public void reset() {
        LevelParser ps = LevelParser.LevelParser();
        levelContainer = ps.loadLevel(directory, levelFormat.get(String.valueOf(level)));
//        setBounds(this.levelContainer.getBoard().getWidth(), this.levelContainer.getBoard().getHeight());
//        this.world = levelContainer.getWorld();
//        world.setContactListener(this);
    }

    /**
     * This method processes a single step in the game loop.
     *
     * @param delta Number of seconds since last animation frame
     */
    protected void play(float delta) {

        // if no player is alive, declare game over
        if (!gameplayController.isAlive()) {
            gameState = GameState.OVER;
        }
        //this.preUpdate(delta);
        levelContainer.getWorld().step(delta, 6, 2);
//        if (!inBounds(levelContainer.getPlayer())) handleBounds(levelContainer.getPlayer());
//        for (GameObject obj : levelContainer.getEnemies()) {
//            if (!inBounds(obj)) handleBounds(obj);
//        }
        //this.postUpdate(delta);

        // Update objects.
        //levelContainer.getWorld().step(delta, 6, 2);
        gameplayController.resolveActions(inputController, delta);

        // Check for collisions
        totalTime += (delta * 1000); // Seconds to milliseconds
        //physicsController.processCollisions(gameplayController.getObjects());
        // Clean up destroyed objects
        // gameplayController.garbageCollect();
    }

    /**
     *
     * Draw the status of this player mode.
     * <p>
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     */
    public void draw(float delta) {
        canvas.clear();

        // Puts player at center of canvas
        levelContainer.setViewTranslation(
                -canvas.WorldToScreenX(levelContainer.getPlayer().getPosition().x) + canvas.getWidth() / 2,
                -canvas.WorldToScreenY(levelContainer.getPlayer().getPosition().y) + canvas.getHeight() / 2
        );
        // Draw the level
        levelContainer.drawLevel(canvas);

        switch (gameState) {
            case WIN:
                displayFont.setColor(Color.YELLOW);
                canvas.begin();
                canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
                canvas.end();
                break;
            case OVER:
                displayFont.setColor(Color.RED);
                canvas.begin(); // DO NOT SCALE
                canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
                canvas.end();
                break;
            case PLAY:
                displayFont.setColor(Color.YELLOW);
                canvas.begin(); // DO NOT SCALE
                canvas.drawHPBar("Moonlight", displayFont, 0.0f, BAR_WIDTH,
                        BAR_HEIGHT, gameplayController.getPlayer().getHp());
                canvas.end();
                break;
        }
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    public void show() {
        active = true;
        gameState = GameState.INTRO;
    }

    /**
     * Called when the screen should render itself.
     * <p>
     * The game loop called by libGDX
     *
     * @param delta The time in seconds since the last render.
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw(delta);

            if (inputController.didExit() && observer != null) {
                observer.exitScreen(this, GO_MENU);
            }

            // for convenience
            if (Gdx.input.isKeyPressed(Input.Keys.PERIOD)) {
                gameplayController.setWin(true);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.COMMA)) {
                gameplayController.setWin(false);
            }
        }
    }


    /// CONTACT LISTENER METHODS

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();
    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when two objects cease to touch.  We do not use it.
     */
    public void endContact(Contact contact) {
    }

    private final Vector2 cache = new Vector2();

    /**
     * Unused ContactListener method
     */
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    /**
     * Handles any modifications necessary before collision resolution
     * <p>
     * This method is called just before Box2D resolves a collision.  We use this method
     * to implement sound on contact, using the algorithms outlined similar to those in
     * Ian Parberry's "Introduction to Game Physics with Box2D".
     * <p>
     * However, we cannot use the proper algorithms, because LibGDX does not implement
     * b2GetPointStates from Box2D.  The danger with our approximation is that we may
     * get a collision over multiple frames (instead of detecting the first frame), and
     * so play a sound repeatedly.  Fortunately, the cooldown hack in SoundController
     * prevents this from happening.
     *
     * @param contact     The two bodies that collided
     * @param oldManifold The collision manifold before contact
     */
    public void preSolve(Contact contact, Manifold oldManifold) {
        System.out.println("Collision!");
        float speed = 0;

        // Use Ian Parberry's method to compute a speed threshold
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();
        WorldManifold worldManifold = contact.getWorldManifold();
        Vector2 wp = worldManifold.getPoints()[0];
        cache.set(body1.getLinearVelocityFromWorldPoint(wp));
        cache.sub(body2.getLinearVelocityFromWorldPoint(wp));
        speed = cache.dot(worldManifold.getNormal());
    }

    /**
     * @param width
     * @param height
     * @see ApplicationListener#resize(int, int)
     */
    public void resize(int width, int height) {

    }

    /**
     * @see ApplicationListener#pause()
     */
    public void pause() {

    }

    /**
     * @see ApplicationListener#resume()
     */
    public void resume() {

    }

    /**
     * Called when this screen is no longer the current screen for a {@link Game}.
     */
    public void hide() {

    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        // TODO: save player stats to json for persistence?
        // Though definitely save levels completed
        inputController = null;
        gameplayController = null;
        //physicsController = null;
        canvas = null;
    }

}
