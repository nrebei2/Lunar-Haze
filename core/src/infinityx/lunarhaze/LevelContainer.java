package infinityx.lunarhaze;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.lunarhaze.physics.LightSource;
import infinityx.util.Drawable;

import java.util.Comparator;

/**
 * Model class
 * <p>
 * This controller also acts as the root class for all the models.
 * Holds a collection of model objects representing the game scene.
 * This includes the Board, player, enemies, lights, and scene objects.
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
 * All models' positions are in world coordinates, Drawing should only be doing the relevant transformations
 */
public class LevelContainer {

    /**
     * Rayhandler for storing lights
     */
    protected RayHandler rayHandler;

    /**
     * All light sources in level
     */
    private final Array<LightSource> lights = new Array<LightSource>();

    /**
     * The Box2D World
     */
    private final World world;
    /**
     * Stores Enemies
     */
    private final EnemyList enemies;
    /**
     * Stores SceneObjects
     */
    private final Array<SceneObject> sceneObjects;
    /**
     * Stores Werewolf
     */
    private Werewolf player;
    /**
     * Stores Board
     */
    private Board board;

    private int remainingMoonlight;

    private PointLight ambienceMoonlight;

    /**
     * Keeps player centered
     */
    private final Affine2 view = new Affine2();

    public Vector2 worldToScreen = new Vector2();

    /**
     * Holds references to all drawable entities on the level (i.e. sceneObjects, player, enemies)
     */
    private final Array<Drawable> drawables;
    private final DrawableCompare drawComp = new DrawableCompare();

    /**
     * Maps tile indices to their respective PointLight pointing on it
     */
    private final IntMap<PointLight> moonlight;

    /**
     * Creates a new LevelContainer with no active elements.
     */
    public LevelContainer() {
        // BOX2D initialization
        world = new World(new Vector2(0, 0), true);
        rayHandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());

        player = null;
        board = null;
        enemies = new EnemyList();
        sceneObjects = new Array<>(true, 5);
        moonlight = new IntMap<>();

        drawables = new Array<Drawable>();
        remainingMoonlight = 0;
    }

    public RayHandler getRayHandler() {
        return rayHandler;
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
     * Return world held by this container
     */
    public World getWorld() {
        return world;
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

        view.setToTranslation(
                -canvas.WorldToScreenX(player.getPosition().x) + canvas.getWidth() / 2,
                -canvas.WorldToScreenY(player.getPosition().y) + canvas.getHeight() / 2
        );
        canvas.begin(view);

        // Debug prints
        //System.out.printf(
        //        "Player pos: (%f, %f), Spotlight pos: (%f, %f) \n",
        //        player.getPosition().x, player.getPosition().y, player.getSpotlight().getPosition().x, player.getSpotlight().getPosition().y);

        // Render order: Board tiles -> (players, enemies, scene objects) sorted by depth (y coordinate)
        board.draw(canvas);
        // Uses timsort, so O(n) if already sorted, which is nice since it usually will be
        drawables.sort(drawComp);
        for (Drawable d : drawables) {
            d.draw(canvas);
        }

        // Flush information to the graphic buffer.
        canvas.end();

        // A separate transform for lights :(
        // In an ideal world they would be the same, but I like using GameCanvas
        OrthographicCamera raycamera = new OrthographicCamera(
                canvas.getWidth() / canvas.WorldToScreenX(1),
                canvas.getHeight() / canvas.WorldToScreenY(1)
        );
        raycamera.translate(player.getPosition().x, player.getPosition().y);
        raycamera.update();
        rayHandler.setCombinedMatrix(raycamera);

        // Finally, draw lights
        rayHandler.updateAndRender();
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
