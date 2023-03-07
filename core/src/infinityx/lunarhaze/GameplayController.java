package infinityx.lunarhaze;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.assets.AssetDirectory;
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

    public Board board;

    public GameplayController() {
        player = null;
        enemies = null;
        board = null;
        objects = new Array<GameObject>();
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
        controls = new EnemyController[enemies.size()];
        for(int ii = 0; ii < enemies.size(); ii++) {
            controls[ii] = new EnemyController(ii,player,enemies, board);
            objects.add(enemies.get(ii));
        }

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
        if (player != null ) {
            resolvePlayer(input,delta);
            resolveMoonlight();
        }
        resolveEnemies();
        // Process the other (non-ship) objects.
//        for (GameObject o : objects) {
//            o.update(delta);
//        }
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

    public void resolveMoonlight() {
        Vector2 pos = board.worldToBoard(player.position.x, player.position.y);
        if(board.isLit((int) pos.x, (int) pos.y)) {
            player.setOnMoonlight(true);
        } else {player.setOnMoonlight(false);}
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