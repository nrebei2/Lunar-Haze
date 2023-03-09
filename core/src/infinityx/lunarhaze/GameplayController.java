package infinityx.lunarhaze;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.physics.MoonlightSource;
import infinityx.util.FilmStrip;

public class GameplayController {

    /** Texture for werewolf */
    private Texture werewolfTexture;
    /** Texture for all villagers, as they look the same */
    private Texture villagerTexture;

    /** Reference to player (need to change to allow multiple players) */
    private Werewolf player;

    private EnemyList enemies;

    /** The currently active object */
    private Array<GameObject> objects;

    private EnemyController[] controls;

    private LightingController lightingController;

    public Board board;

    private static final float MOONLIGHT_COLLECT_TIME = 1.5f;

    private float timeOnMoonlight;

    private int remainingMoonlight;
    private boolean gameWon;

    private boolean gameLost;

    private World world;

    public GameplayController() {
        player = null;
        enemies = null;
        board = null;
        objects = new Array<GameObject>();
        timeOnMoonlight = 0;
    }
//
//    /**
//     * Populates this mode from the given the directory.
//     *
//     * The asset directory is a dictionary that maps string keys to assets.
//     * Assets can include images, sounds, and fonts (and more). This
//     * method delegates to the gameplay controller
//     *
//     * @param directory 	Reference to the asset directory.
//     */
//    public void populate(AssetDirectory directory) {
//        werewolfTexture = directory.getEntry("werewolf", Texture.class);
//        villagerTexture = directory.getEntry("villager", Texture.class);
//    }


    /**
     * Returns the list of the currently active (not destroyed) game objects
     *
     * As this method returns a reference and Lists are mutable, other classes can
     * technical modify this list.  That is a very bad idea.  Other classes should
     * only mark objects as destroyed and leave list management to this class.
     *
     * @return the list of the currently active (not destroyed) game objects
     */
    public Array<GameObject> getObjects() {
        return objects;
    }


    /**
     * Returns a reference to the currently active player.
     *
     * This property needs to be modified if you want multiple players.
     *
     * @return a reference to the currently active player.
     */
    public Werewolf getPlayer() { return player;}


    /**
     * Returns true if the currently active player is alive.
     *
     * This property needs to be modified if you want multiple players.
     *
     * @return true if the currently active player is alive.
     */
    public boolean isAlive() {
        return player != null;
    }

    public RayHandler getRayHandler() { return lightingController.getRayHandler(); };

    /**
     * Starts a new game.
     *
     * This method creates a single player, but does nothing else.
     */
    public void start(LevelContainer levelContainer) {
        player = levelContainer.getPlayer();
        enemies = levelContainer.getEnemies();
        objects.add(player);
        board = levelContainer.getBoard();
        remainingMoonlight = levelContainer.getRemainingMoonlight();
        controls = new EnemyController[enemies.size()];

        gameWon = false;
        gameLost = false;

        // BOX2D initialization
        world = new World(new Vector2(0,0), true);
        // create a body definition for the player
        BodyDef playerDef = new BodyDef();
        playerDef.type = BodyDef.BodyType.DynamicBody;
        playerDef.position.set(player.position);

        FixtureDef playerFixtureDef = new FixtureDef();
        playerFixtureDef.shape = new CircleShape();

        player.body = world.createBody(playerDef);
        player.body.createFixture(playerFixtureDef);

        for(int ii = 0; ii < enemies.size(); ii++) {
            Enemy curr = enemies.get(ii);
            BodyDef enemyDef = new BodyDef();
            enemyDef.type = BodyDef.BodyType.DynamicBody;
            enemyDef.position.set(curr.position);

            FixtureDef enemyFixtureDef = new FixtureDef();
            enemyFixtureDef.shape = new CircleShape();
            curr.body = world.createBody(enemyDef);
            curr.body.createFixture(enemyFixtureDef);
            controls[ii] = new EnemyController(ii,player,enemies, board);
            objects.add(enemies.get(ii));
        }

        // Intialize lighting
        lightingController = new LightingController(enemies, board.getMoonlightTiles(), board.getWidth(), board.getHeight());
        lightingController.initLights(true, true, 2, world);

        /*PointLight light = new PointLight(getRayHandler(), 512, new Color(0.5f, 0.5f, 1f, 0.3f), 2000f, 0, 0);
        */
    }

    /**
     * Resets the game, deleting all objects.
     */
    public void reset() {
        player = null;
        objects.clear();
    }

    /**
     * Resolve the actions of all game objects (player and shells)
     *
     * You will probably want to modify this heavily in Part 2.
     *
     * @param input  Reference to the input controller
     * @param delta  Number of seconds since last animation frame
     */
    public void resolveActions(InputController input, float delta) {
        // Process the player
        if (player != null) {
            resolvePlayer(input,delta);
            resolveMoonlight(delta);
        }
        resolveEnemies();
        world.step(delta, 6, 2);
    }

    /**
     * Process the player's actions.
     *
     * Notice that firing bullets allocates memory to the heap.  If we were REALLY
     * worried about performance, we would use a memory pool here.
     *
     * @param input  Reference to the input controller
     * @param delta  Number of seconds since last animation frame
     */
    public void resolvePlayer(InputController input, float delta) {
        player.setMovementH(input.getHorizontal());
        player.setMovementV(input.getVertical());
        player.update(delta);
    }

    public void resolveMoonlight(float delta) {
        Vector2 pos = board.worldToBoard(player.position.x, player.position.y - (player.texture.getHeight() / 3f));
        int px = (int) pos.x;
        int py = (int) pos.y;

        if(board.isLit(px, py)) {
            timeOnMoonlight += delta; // Increase variable by time
            player.setOnMoonlight(true);
            if(timeOnMoonlight > MOONLIGHT_COLLECT_TIME) {
                player.collectMoonlight();
                remainingMoonlight--;
                timeOnMoonlight = 0;

                board.setLit(px, py, false);
                lightingController.removeLightAt(px, py);

                // Check if game is won here
                if(remainingMoonlight == 0) gameWon = true;
            }
        } else {
            timeOnMoonlight = 0;
            player.setOnMoonlight(false);
        }
    }

    public void resolveEnemies(){
        board.clearVisibility();
        for (Enemy en: enemies){
            if (controls[en.getId()] != null) {
                EnemyController curEnemyController = controls[en.getId()];
                int action = curEnemyController.getAction();
                curEnemyController.setVisibleTiles();
//                boolean attacking = (action & EnemyController.CONTROL_ATTACK) != 0;
                en.update(action);
//                if (attacking &&) {
//                    fireWeapon(s);
//                } else {
//                    s.coolDown(true);
//                }

            } else {

                en.update(EnemyController.CONTROL_NO_ACTION);
            }
        }

    }

    public boolean isGameWon() { return gameWon; }
    public boolean isGameLost() { return gameLost; }


    /**
     * Garbage collects all deleted objects.
     *
     * This method works on the principle that it is always cheaper to copy live objects
     * than to delete dead ones.  Deletion restructures the list and is O(n^2) if the
     * number of deletions is high.  Since Add() is O(1), copying is O(n).
     *
    public void garbageCollect() {
        // INVARIANT: backing and objects are disjoint
        for (GameObject o : objects) {
            if (o.isDestroyed()) {
                destroy(o);
            } else {
                backing.add(o);
            }
        }

        // Swap the backing store and the objects.
        // This is essentially stop-and-copy garbage collection
        Array<GameObject> tmp = backing;
        backing = objects;
        objects = tmp;
        backing.clear();
    }
*/


}