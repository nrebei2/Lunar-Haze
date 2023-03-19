package infinityx.lunarhaze.entity;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;

import infinityx.lunarhaze.GameObject;
import infinityx.lunarhaze.LevelContainer;

public class Werewolf extends GameObject {

    /**
     * The frame number for a ship that is not turning
     */
    public static final int SHIP_IMG_FLAT = 9;

    /**
     * How fast we change frames (one frame per 4 calls to update)
     */
    private static final float ANIMATION_SPEED = 0.25f;

    /**
     * Initial light value of the werewolf is 0.0
     **/
    private static final float INITIAL_LIGHT = 0.0f;

    /**
     * Initial hp of the werewolf is 100.0
     **/
    public static int INITIAL_HP = 5;

    /**
     * Maximum light of the werewolf is 100.0
     **/
    public static final float MAX_LIGHT = 100.0f;

    private int maxHp;

    /* Returns whether the werewolf can move or not; the werewolf can't move
       if its being knocked back by an attack.
     */
    private boolean canMove;

    /** Controls how long the werewolf gets knocked back by an attack and the window of the
     *  damage animation.
     */
    private float lockout;

    /** Reference to werewolf's sprite for drawing */
    //private FilmStrip werewolfSprite;

    /**
     * The right/left movement of the werewolf
     **/
    private float movementH = 0.0f;

    /**
     * The up/down movement of the werewolf
     **/
    private float movementV = 0.0f;

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

    private int hp;

    /**
     * Light collected of the werewolf.
     * The value is a percentage of light on the map, between 0 and 100.
     */
    private float light;

    /**
     * Stealth value of the werewolf.
     * The value is a float between 0 and 1.
     */
    private float stealth;


    /**
     * Controls how long the werewolf gets knocked back by an attack and the window of the
     * damage animation.
     */

    private float lockoutTime;

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

    private LevelContainer levelContainer;

    /**
     * Point light pointed on werewolf at all times
     */
    private PointLight spotLight;

    private final Vector2 forceCache = new Vector2();

    private boolean isAttacking;

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
    public int getHp() {
        return hp;
    }

    /**
     * Sets the current hp of the werewolf.
     *
     * @param value the current hp of the werewolf.
     */
    public void setHp(int value) {
        hp = value;
    }

    public void initHp(int value) {
        INITIAL_HP = value;
    }

    public void initLockout(float value) {
        lockout = value;
    }

    /**
     * Returns the current stealth of the werewolf.
     */
    public float getStealth() {
        return stealth;
    }

    /**
     * Sets the current stealth of the werewolf.
     *
     * @param value the current stealth of the werewolf.
     */
    public void setStealth(float value) {
        stealth = value;
    }

    /**
     * Returns the current light collected of the werewolf.
     */
    public float getLight() {
        return light;
    }

    /**
     * Sets the current light collected of the werewolf.
     *
     * @param value the current light of the werewolf.
     */
    public void setLight(float value) {
        light = value;
    }

    /**
     * @return Point light on player
     */
    public PointLight getSpotlight() {
        return spotLight;
    }

    /**
     * Attaches light to player as a spotlight (pointed down at player at all times)
     */
    public void setSpotLight(PointLight light) {
        spotLight = light;
        spotLight.attachToBody(getBody(), 0, 0);
        spotLight.setActive(true);
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

    public void addMoonlightCollected(){
        moonlightCollected ++;
    }

    public int getMoonlightCollected(){
        return moonlightCollected;

    public void setCanMove(boolean value) { canMove = value; }

    public void setAttacking(boolean value) { isAttacking = value; }
    public boolean isAttacking() { return isAttacking; }

    /**
     * Initialize a werewolf.
     */
    public Werewolf(float x, float y) {

        super(x, y);
        animeframe = 0.0f;
        lockoutTime = 0.0f;
        moonlight = false;
        light = INITIAL_LIGHT;
        hp = INITIAL_HP;
        stealth = 0.0f;
        moonlightCollected = 0;
        isAttacking = false;
        canMove = true;
    }

    /**
     * Initialize the werewolf with the given data
     *
     * @param container LevelContainer which this player is placed in
     */
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);

        JsonValue light = json.get("spotlight");
        float[] color = light.get("color").asFloatArray();
        float dist = light.getFloat("distance");
        int rays = light.getInt("rays");

        int health = json.getInt("health");
        float lockout = json.getFloat("lockout");

        initHp(health);
        initLockout(lockout);

        PointLight spotLight = new PointLight(
                container.getRayHandler(), rays, Color.WHITE, dist,
                0, 0
        );
        spotLight.setColor(color[0], color[1], color[2], color[3]);
        spotLight.setSoft(light.getBoolean("soft"));
        activatePhysics(container.getWorld());
        setSpotLight(spotLight);
    }

    /**
     * Deep clones player, can be used independently of this
     *
     * @return new player
     */
    public Werewolf deepClone(LevelContainer container) {
        Werewolf werewolf = new Werewolf(container.getPlayer().getWidth(), container.getPlayer().getHeight());
        werewolf.setSpeed(speed);
        werewolf.setTexture(getTexture());
        werewolf.initHp(INITIAL_HP);
        werewolf.initLockout(lockout);
        werewolf.setOrigin((int) origin.x, (int) origin.y);
        PointLight spotLight = new PointLight(
                container.getRayHandler(), this.spotLight.getRayNum(), this.spotLight.getColor(), this.spotLight.getDistance(),
                0, 0
        );
        werewolf.setBodyState(body);
        spotLight.setSoft(this.spotLight.isSoft());
        werewolf.activatePhysics(container.getWorld());
        werewolf.setSpotLight(spotLight);

        werewolf.setDimension(getDimension().x, getDimension().y);
        werewolf.setPositioned(positioned);
        return werewolf;
    }

    public void resolveAttack(GameObject enemy, int damage, float knockback) {

        Body enemyBody = enemy.getBody();
        Vector2 pos = body.getPosition();
        Vector2 enemyPos = enemyBody.getPosition();
        Vector2 direction = pos.sub(enemyPos).nor();

        canMove = false;
        body.applyLinearImpulse(direction.scl(knockback), body.getWorldCenter(), true);
        setHp(hp - damage);
    }

    @Override
    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) {
            return false;
        }

        // Define the shape of the player's fixture
        PolygonShape playerShape = new PolygonShape();
        playerShape.setAsBox(0.5f, 1.0f);

        // Define the properties of the player's fixture
        fixture.shape = playerShape;
        fixture.density = 1.0f;
        fixture.friction = 0.5f;
        fixture.restitution = 0.0f;
        Fixture fix = body.createFixture(fixture);
        fix.setUserData(this);
//        fixture = getBody().createFixture(fixtureDef);
        playerShape.dispose();
        return true;
    }


    /**
     * Updates the animation frame and position of this werewolf.
     *
     * @param delta Number of seconds since last animation frame
     */
        public void update(float delta) {
            // get the current velocity of the player's Box2D body
            Vector2 velocity = body.getLinearVelocity();
            if (canMove) {
                // update the velocity based on the input from the player
                velocity.x = movementH * speed;
                velocity.y = movementV * speed;

                // set the updated velocity to the player's Box2D body
                body.setLinearVelocity(velocity);
            } else if (lockoutTime >= lockout) {
                canMove = true;
                lockoutTime = 0f;
            } else {
                lockoutTime += delta;
            }
        }
}