package infinityx.lunarhaze;

import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;

import java.util.List;

/**
 * Model class
 *
 * Holds a collection of model objects representing the game scene.
 * This includes the Board, player, enemies, and scene objects.
 *
 *  World coordinates:
 *       1   1           (n, n) (board is nxn tiles)
 *     +---+---+----------+
 *  1  |   |   |   ...    |
 *     +-------+----------+
 *     |   |   |   ...    |
 *     | . | . |   ...    |
 *     | . | . |   ...    |
 *     | . | . |   ...    |
 *     |   |   |   ...    |
 *     +-------+----------+
 *  1  |   |   |   ...    |
 *     +-------+----------+
 *  1  |   |   |   ...    |
 *     +---+---+----------+
 *  (0,0)
 *
 *  Represent all coordinates for models with world coordinates,
 *  GameCanvas should be doing any transformations
 */
public class LevelContainer {
    // TODO: At this point I do not feel a need for a memory pool or garbage collection
    // since we will not have more than a few enemies or scene objects for each level
    private Array<Enemy> enemies;
    private Array<SceneObject> sceneObjects;
    private Werewolf player;
    private Board board;

    /**
     * Creates a new LevelContainer with no active elements.
     */
    public LevelContainer() {
        player = null;
        board = null;
        enemies = new Array<>(true, 5);
        sceneObjects = new Array<>(true, 5);
    }

    /**
     * ID is just position in enemy list
     *
     * @return Corresponding enemy given ID
     */
    public Enemy getEnemyByID(int ID) {
        return enemies.get(ID);
    }

    /**
     * @param enemy Enemy to add to scene
     */
    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    /**
     * Returns a reference to the currently active player.
     *
     * @return a reference to the currently active player.
     */
    public Werewolf getPlayer() {
        return player;
    }

    /**
     * Set the active player for this level
     *
     * @param player
     */
    public void setPlayer(Werewolf player) {
        this.player = player;
    }

    /**
     * @return Scene board holding all background tiles
     */
    public Board getBoard() {
        return board;
    }

    /**
     * @param board Board to set for this scene
     */
    public void setBoard(Board board) {
        this.board = board;
    }

    /**
     * @param obj Scene Object to add
     */
    public void addSceneObject(SceneObject obj) {
        sceneObjects.add(obj);
    }

    /** Draws the entire scene to the canvas
     *
     * @param canvas The drawing context
     */
    public void drawLevel(GameCanvas canvas) {
        // Render order: Board tiles -> (players, enemeies, scene objects) sorted by depth (y coordinate)
    }
}
