package infinityx.lunarhaze;

import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.util.Drawable;

import java.util.Comparator;

/**
 * Model class
 * <p>
 * This controller also acts as the root class for all the models.
 * Holds a collection of model objects representing the game scene.
 * This includes the Board, player, enemies, and scene objects.
 * <p>
 * World coordinates:
 * ------------------ (n, n) (board is nxn tiles)
 * +---+---+----------+
 * |   |   |   ...    |
 * +-------+----------+
 * |   |   |   ...    |
 * | . | . |   ...    |
 * | . | . |   ...    |
 * | . | . |   ...    |
 * |   |   |   ...    |
 * +-------+----------+
 * |   |   |   ...    |
 * +-------+----------+
 * |   |   |   ...    |
 * +---+---+----------+
 * (0,0)
 * <p>
 * Represent all coordinates for models with world coordinates,
 * GameCanvas should be doing any transformations
 */
public class LevelContainer {
    /**
     * Stores Enemies
     */
    private final EnemyList enemies;
    /**
     * Stores SceneObjects
     */
    private Array<SceneObject> sceneObjects;
    /**
     * Stores Werewolf
     */
    private Werewolf player;
    /**
     * Stores Board
     */
    private Board board;

    private int remainingMoonlight;

    /**
     * Keeps player centered
     */
    private final Affine2 view = new Affine2();

    /**
     * Holds references to all drawable entities on the level (i.e. sceneObjects, player, enemies)
     */
    private final Array<Drawable> drawables;
    private final DrawableCompare drawComp = new DrawableCompare();


    /**
     * Creates a new LevelContainer with no active elements.
     */
    public LevelContainer() {
        player = null;
        board = null;
        enemies = new EnemyList();
        //sceneObjects = new Array<>(true, 5);

        drawables = new Array<Drawable>();

        remainingMoonlight = 0;
    }

    /**
     * @return All enemies in level. Note EnemyList contains dead ones too.
     */
    public EnemyList getEnemies() {
        return enemies;
    }

    /**
     * @param enemy Enemy to append to enemy list
     * @return id of the added enemy
     */
    public int addEnemy(Enemy enemy) {
        enemies.addEnemy(enemy);
        drawables.add(enemy);
        return enemies.size() - 1;
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
        drawables.add(player);
        this.player = player;
    }

    public void addMoonlight() {
        remainingMoonlight++;
    }

    public int getRemainingMoonlight() {
        return remainingMoonlight;
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
        drawables.add(obj);
    }

    /**
     * Draws the entire scene to the canvas
     *
     * @param canvas The drawing context
     */
    public void drawLevel(GameCanvas canvas) {
        // Puts player at center of canvas
        view.setToTranslation(-GameCanvas.WorldToScreenX(player.position.x) + canvas.getWidth() / 2, -GameCanvas.WorldToScreenY(player.position.y) + canvas.getHeight() / 2);
        canvas.begin(view);

        System.out.printf("Player pos: (%f, %f)\n", player.position.x, player.position.y);

        // Render order: Board tiles -> (players, enemies, scene objects) sorted by depth (y coordinate)
        board.draw(canvas);
        // Uses timsort, so O(n) if already sorted, which is nice since it usually will be
        drawables.sort(drawComp);
        for (Drawable d : drawables) {
            d.draw(canvas);
        }

        // Flush information to the graphic buffer.
        canvas.end();
    }
}

/**
 * Depth comparison function used for drawing
 */
class DrawableCompare implements Comparator<Drawable> {
    @Override
    public int compare(Drawable d1, Drawable d2) {
        return (int) Math.signum(d2.getDepth() - d1.getDepth());
    }
}
