package infinityx.lunarhaze;

import box2dLight.Light;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.lunarhaze.physics.LightSource;
import infinityx.util.Drawable;
import com.badlogic.gdx.math.*;

import java.util.ArrayList;
import java.util.Comparator;
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
    /** Stores Enemies*/
    private EnemyList enemies;
    /** Stores SceneObjects*/
    private Array<SceneObject> sceneObjects;
    /** Stores Werewolf*/
    private Werewolf player;
    /** Stores Board*/
    private Board board;

    /** Holds references to all drawable entities on the level (i.e. sceneObjects, player, enemies) */
    private Array<Drawable> drawables;
    private DrawableCompare drawComp = new DrawableCompare();

    /** Box2D world, for lighting */
    protected World world;

    /** Rayhandler for storing lights */
    protected RayHandler rayHandler;

    /** All light sources in level */
    private Array<LightSource> lights = new Array<LightSource>();

    /** The camera defining the RayHandler view; scale is in physics coordinates */
    protected OrthographicCamera raycamera;

    /**
     * Creates a new LevelContainer with no active elements.
     */
    public LevelContainer() {
        player = null;
        board = null;
        enemies = new EnemyList();
        //sceneObjects = new Array<>(true, 5);

        drawables = new Array<Drawable>();

        world = null;
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
        // Render order: Board tiles -> (players, enemies, scene objects) sorted by depth (y coordinate)
        board.draw(canvas);
        // Uses timsort, so O(n) if already sorted, which is nice since it usually will be
        drawables.sort(drawComp);
        for (Drawable d: drawables) {
            d.draw(canvas);
        }

    }

    public void initLights(boolean gamma, boolean diffuse, int blur) {
        // Initialize lighting system
        world = new World(Vector2.Zero, false);

        int width = 16;
        int height = 12;

        raycamera = new OrthographicCamera(width, height);
        raycamera.position.set(width/2.0f, height/2.0f, 0);
        raycamera.update();

        RayHandler.setGammaCorrection(gamma);
        RayHandler.useDiffuseLight(diffuse);
        rayHandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
        rayHandler.setCombinedMatrix(raycamera);

        rayHandler.setAmbientLight(Color.WHITE);
        rayHandler.setBlur(blur > 0);
        rayHandler.setBlurNum(blur);

        // Create light for each enemy and attach
        for(Enemy e : enemies) {
            ConeSource cone = new ConeSource(rayHandler, 512);
        }
    }
}

/**
 * Depth comparison function used for drawing
 */
class DrawableCompare implements Comparator<Drawable> {
    @Override
    public int compare(Drawable d1, Drawable d2) {
        return (int)Math.signum(d1.getDepth() - d2.getDepth());
    }
}
