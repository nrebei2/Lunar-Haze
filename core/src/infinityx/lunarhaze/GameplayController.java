package infinityx.lunarhaze;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.lunarhaze.physics.RaycastInfo;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;

/**
 * Controller to handle gameplay interactions.
 */
public class GameplayController {
    /**
     * Reference to player (need to change to allow multiple players)
     */
    private Werewolf player;

    private EnemyList enemies;

    /**
     * The currently active object
     */
    private final Array<GameObject> objects;

    private EnemyController[] controls;

    private LightingController lightingController;

    public Board board;

    private static final float MOONLIGHT_COLLECT_TIME = 1.5f;

    private float timeOnMoonlight;

    private int remainingMoonlight;
    private boolean gameWon;

    private boolean gameLost;
    private LevelContainer levelContainer;

    /**
     * This is the collision controller (handels collisions between all objects in our world
     */
    private CollisionController collisionController;

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
     * <p>
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
     * <p>
     * This property needs to be modified if you want multiple players.
     *
     * @return a reference to the currently active player.
     */
    public Werewolf getPlayer() {
        return player;
    }


    /**
     * Returns true if the currently active player is alive.
     *
     * @return true if the currently active player is alive.
     */
    public boolean isAlive() {
        return player != null;
    }

    /**
     * Starts a new game.
     * <p>
     * This method creates a single player, but does nothing else.
     */
    public void start(LevelContainer levelContainer) {
        this.levelContainer = levelContainer;
        this.collisionController = new CollisionController(levelContainer);
        player = levelContainer.getPlayer();
        enemies = levelContainer.getEnemies();
        objects.add(player);
        board = levelContainer.getBoard();
        remainingMoonlight = levelContainer.getRemainingMoonlight();
        controls = new EnemyController[enemies.size()];

        gameWon = false;
        gameLost = false;

        for (int ii = 0; ii < enemies.size(); ii++) {
            Enemy curr = enemies.get(ii);
            controls[ii] = new EnemyController(ii, player, enemies, board);
            objects.add(enemies.get(ii));
        }

        // Intialize lighting
        lightingController = new LightingController(levelContainer);

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
     * <p>
     * You will probably want to modify this heavily in Part 2.
     *
     * @param input Reference to the input controller
     * @param delta Number of seconds since last animation frame
     */
    public void resolveActions(InputController input, float delta) {
        // Process the player only when the game is in play
        lightingController.updateDust(delta);
        if (player != null && !(gameLost || gameWon)) {
            resolvePlayer(input, delta);
            resolveMoonlight(delta);
        }
        resolveEnemies(delta);
    }

    /**
     * Process the player's actions.
     * <p>
     * Notice that firing bullets allocates memory to the heap.  If we were REALLY
     * worried about performance, we would use a memory pool here.
     *
     * @param input Reference to the input controller
     * @param delta Number of seconds since last animation frame
     */
    public void resolvePlayer(InputController input, float delta) {
        player.setMovementH(input.getHorizontal());

        player.setMovementV(input.getVertical());
        player.update(delta);
    }

    public void resolveMoonlight(float delta) {
        int px = board.worldToBoardX(player.getPosition().x);
        int py = board.worldToBoardX(player.getPosition().y);

        if (board.isLit(px, py)) {
            if (board.isCollectable(px, py)) {
                timeOnMoonlight += delta; // Increase variable by time
            }
            player.setOnMoonlight(true);
            if (board.isCollectable(px, py) && timeOnMoonlight > MOONLIGHT_COLLECT_TIME) {
                player.collectMoonlight(levelContainer);
                remainingMoonlight--;
                timeOnMoonlight = 0;
                board.setCollected(px, py);
            }
            // Check if game is won here
            if (remainingMoonlight == 0) gameWon = true;
        } else {
            timeOnMoonlight = 0;
            player.setOnMoonlight(false);
        }
    }

    // TODO: THIS SHOULD BE IN ENEMYCONTROLLER, also this code is a mess
    public void resolveEnemies(float delta) {
        //board.clearVisibility();
        for (Enemy en : enemies) {
//            if (controls[en.getId()] != null) {
                EnemyController curEnemyController = controls[en.getId()];
                curEnemyController.update(levelContainer, delta);
//                System.out.println(en.getBody());
//                curEnemyController.updateSteering(levelContainer);
//                boolean attacking = (action & EnemyController.CONTROL_ATTACK) != 0;
                en.update(delta);

                // TODO: make more interesting actions                //curEnemyController.setVisibleTiles();
                if (en.getIsAlerted()) {
                    // angle between enemy and player
                    double ang = Math.atan2(player.getPosition().y - en.getPosition().y, player.getPosition().x - en.getPosition().y);
                    en.setFlashLightRot((float) ang);
                } else {
//                    en.setFlashLightRotAlongDir();
                }
//            } else {

//                en.update(EnemyController.CONTROL_NO_ACTION);
//            }
        }

    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setWin(boolean win) {
        if (win) this.gameWon = true;
        else this.gameLost = true;
    }

    public boolean isGameLost() {
        return gameLost;
    }


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