package infinityx.lunarhaze.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import infinityx.lunarhaze.GameCanvas;
import infinityx.lunarhaze.GameObject;

public class Werewolf extends GameObject {

    /**
     * The frame number for a ship that is not turning
     */
    public static final int SHIP_IMG_FLAT = 9;

    /**
     * Move speed
     **/
    private static final float WEREWOLF_SPEED = 800f;

    /**
     * How fast we change frames (one frame per 4 calls to update)
     */
    private static final float ANIMATION_SPEED = 0.25f;

    /**
     * Initial hp of the werewolf is 100
     **/
    private static final float INITIAL_HP = 100;

    /** Reference to werewolf's sprite for drawing */
    //private FilmStrip werewolfSprite;

    /**
     * The right/left movement of the werewolf
     **/
    private float movementH = 0.0f; //DEPRECATED

    /**
     * The up/down movement of the werewolf
     **/
    private float movementV = 0.0f; // DEPRECATED

    public Body body;

    /**
     * Whether the  player stands on a moonlight tile
     **/
    private Boolean moonlight;

    /**
     * Whether the  player face right or not
     **/
    private Boolean faceRight;

    /**
     * Number of moonlight tiles collected
     **/
    private int moonlightCollected;

    /**
     * Current animation frame for this werewolf
     */
    private final float animeframe;

    /**
     * Health point (hp) of the werewolf
     */
    private float hp;

    private final Vector2 forceCache = new Vector2();

//    /**
//     * Returns the image filmstrip for this ship
//     *
//     * This value should be loaded by the GameMode and set there. However, we
//     * have to be prepared for this to be null at all times
//     *
//     * @return the image texture for this ship
//     */
//    public FilmStrip getFilmStrip() {
//        return werewolfSprite;
//    }


    /**
     * Sets the image texture for this ship
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * param value the image texture for this ship
     */
    /*public void setFilmStrip(FilmStrip value) {
        werewolfSprite = value;
        werewolfSprite.setFrame(SHIP_IMG_FLAT);
    }*/

    /**
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    public ObjectType getType() {
        return ObjectType.WEREWOLF;
    }

    /**
     * Returns the current player (left/right) movement input.
     *
     * @return the current player movement input.
     */
    public float getMovementH() {
        return movementH;
    }

    /**
     * Returns the current player (up/down) movement input.
     *
     * @return the current player movement input.
     */
    public float getMovementV() {
        return movementV;
    }

    /**
     * Sets the current player (left/right) movement input.
     *
     * @param value the current player movement input.
     */
    public void setMovementH(float value) {
        movementH = value;
    }

    /**
     * Sets the current player (uo/down) movement input.
     *
     * @param value the current player movement input.
     */
    public void setMovementV(float value) {
        movementV = value;
    }

    /**
     * Returns the current hp of the werewolf.
     */
    public float getHp() {
        return hp;
    }

    /**
     * Sets the current hp of the werewolf.
     *
     * @param value the current hp of the werewolf.
     */
    public void setHp(float value) {
        hp = value;
    }

    /**
     * Returns true if the player is on a moonlight tile.
     *
     * @return true if the player is on a moonlight tile.
     */
    public boolean isOnMoonlight() {
        return moonlight;
    }

    public void setOnMoonlight(Boolean b) {
        moonlight = b;
    }

    public void collectMoonlight() {
        moonlightCollected++;
    }

    /**
     * Initialize a werewolf not standing on moonlight tile.
     */
    public Werewolf(float x, float y) {

        super(x, y);
        animeframe = 0.0f;
        moonlight = false;
        hp = INITIAL_HP;
        moonlightCollected = 0;
    }

    /**
     * Updates the animation frame and position of this werewolf.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {

        // get the current velocity of the player's Box2D body
        Vector2 velocity = body.getLinearVelocity();

        // update the velocity based on the input from the player
        velocity.x = movementH * WEREWOLF_SPEED;
        velocity.y = movementV * WEREWOLF_SPEED;

        // set the updated velocity to the player's Box2D body
        body.setLinearVelocity(velocity);
        position = body.getPosition();
    }

    /**
     * Draws this werewolof to the canvas
     * <p>
     * There is only one drawing pass in this application, so you can draw the objects
     * in any order.
     *
     * @param canvas The drawing context
     */
    public void draw(GameCanvas canvas) {
        // TODO
        /** For animation */
//        float effect = true ? 1.0f : -1.0f;
//        float ox = 0.5f * werewolfSprite.getRegionWidth();
//        float oy = 0.5f * werewolfSprite.getRegionHeight();
        canvas.draw(super.getTexture(), Color.WHITE, origin.x, origin.y, position.x, position.y, 0.0f, 1.0f, 1.f);
    }
}