package infinityx.lunarhaze;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.RaycastInfo;


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

    private PlayerController playerController;

    public Board board;

    private boolean gameWon;

    private boolean gameLost;
    private LevelContainer levelContainer;

    /**This is the collision controller (handels collisions between all objects in our world*/
    private CollisionController collisionController;

    public GameplayController() {
        player = null;
        enemies = null;
        board = null;
        objects = new Array<GameObject>();
    }

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
     * Returns a reference to the current PlayerController.
     */
    public PlayerController getPlayerController() {
        return playerController;
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
        this.playerController = new PlayerController(player, board, levelContainer);
        controls = new EnemyController[enemies.size()];

        gameWon = false;
        gameLost = false;

        for (int ii = 0; ii < enemies.size(); ii++) {
            Enemy curr = enemies.get(ii);
            controls[ii] = new EnemyController(ii, player, enemies, board);
            objects.add(enemies.get(ii));
        }

        // Intialize lighting
        lightingController = new LightingController(enemies, board);

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
        if (player != null && !(gameLost || gameWon)) {
            playerController.update(input, delta);
            gameWon = playerController.isGameWon();
            gameLost = playerController.getPlayerHp() <= 0;
        }
        EnemyController.resolveEnemies(controls,player,enemies);
    }


    public RaycastInfo raycast(GameObject requestingObject, Vector2 point1, Vector2 point2){
        RaycastInfo callback = new RaycastInfo(requestingObject);
        World world = levelContainer.getWorld();
        world.rayCast(callback, new Vector2(point1.x, point1.y), new Vector2(point2.x, point2.y));
        return callback;
    }

    public boolean detectPlayer(Enemy enemy){
        Vector2 point1 = enemy.getPosition();
        float dist = enemy.getFlashlight().getDistance();
        float vx = enemy.getVX();
        float vy = enemy.getVY();
        if (vx == 0 && vy ==0) return false;
        Vector2 direction = new Vector2(vx, vy).nor();
        Vector2 point2 = new Vector2(point1.x + dist*direction.x, point1.y + dist*direction.y);
        RaycastInfo info = raycast(enemy, point1, point2);
        return info.hit && info.hitObject == player;
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