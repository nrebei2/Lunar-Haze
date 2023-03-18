package infinityx.lunarhaze;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.SceneObject;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.LightSource;
import infinityx.util.Drawable;

import java.util.ArrayList;
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
     * Need an ongoing reference to the asset directory
     */
    protected AssetDirectory directory;
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
    private World world;
    /**
     * Stores Enemies
     */
    private EnemyList enemies;
    /**
     * Stores SceneObjects
     */
    private Array<SceneObject> sceneObjects;
    /**
     * Stores Werewolf. Since there is always one and only one player in a level,
     * this attribute is always initialized and carried over across levels.
     * Therefore, this is like a player cache.
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
    private final Vector2 view = new Vector2();

    /**
     * Holds references to all drawable entities on the level (i.e. sceneObjects, player, enemies)
     */
    private Array<Drawable> drawables;
    private final DrawableCompare drawComp = new DrawableCompare();

    /**
     * Constants for enemy initialization
     */
    JsonValue enemiesJson;

    /**
     * Constants for scene object initialization
     */
    JsonValue objectJson;

    /**
     * Constants for player initialization
     */
    JsonValue playerJson;

    /**
     * is the player hidden
     */
    private boolean hidden;

    /**
     * Initialize attributes
     */
    private void initialize() {
        world = new World(new Vector2(0, 0), true);
        rayHandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
        rayHandler.setAmbientLight(1);

        drawables = new Array<Drawable>();

        Werewolf player = new Werewolf();
        player.initialize(directory, playerJson, this);
        setPlayer(player);

        board = null;
        enemies = new EnemyList();
        sceneObjects = new Array<>(true, 5);

        remainingMoonlight = 0;
    }

    /**
     * Creates a new LevelContainer with no active elements.
     */
    public LevelContainer(AssetDirectory directory, JsonValue enemiesJson, JsonValue objectJson, JsonValue playerJson) {
        this.enemiesJson = enemiesJson;
        this.objectJson = objectJson;
        this.playerJson = playerJson;
        this.directory = directory;
        initialize();
    }

    /**
     * "flush" all objects from this level and resets level.
     * P
     */
    public void flush() {
        initialize();
        // The player object can be carried over!
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
     * @return Enemy added with updated id
     */
    public Enemy addEnemy(Enemy enemy) {
        enemies.addEnemy(enemy);
        drawables.add(enemy);

        enemy.setId(enemies.size() - 1);
        return enemy;
    }

    /**
     * @param type   type of Enemy to append to enemy list (e.g. villager)
     * @param x      world x-position
     * @param y      world y-position
     * @param patrol patrol path for this enemy
     * @return Enemy added with updated id
     */
    public Enemy addEnemy(String type, float x, float y, ArrayList<Vector2> patrol) {
        Enemy enemy = new Enemy();
        enemy.initialize(directory, enemiesJson.get(type), this);

        enemy.setPatrolPath(patrol);
        enemy.setPosition(x, y);

        return addEnemy(enemy);
    }

    /**
     * Add object for this container to draw.
     */
    public void addDrawable(Drawable drawable) {
        drawables.add(drawable);
    }

    /**
     * Add objects for this container to draw.
     */
    public void addDrawables(Drawable... drawable) {
        drawables.addAll(drawable);
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

    public AssetDirectory getDirectory() {
        return this.directory;
    }

    /**
     * Hide player from drawing. Used for level editor.
     */
    public void hidePlayer() {
        if (hidden) return;
        hidden = true;
        drawables.removeValue(player, true);
    }

    /**
     * Show player for drawing. Used for level editor.
     */
    public void showPlayer() {
        if (!hidden) return;
        hidden = false;
        drawables.add(player);
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
     * @return scene object added
     */
    public SceneObject addSceneObject(SceneObject obj) {
        sceneObjects.add(obj);
        drawables.add(obj);

        return obj;
    }

    /**
     * @param type  type of scene object to add (e.g. house)
     * @param x     world x-position
     * @param y     world y-position
     * @param scale scale of object
     * @return scene object added
     */
    public SceneObject addSceneObject(String type, float x, float y, float scale) {
        SceneObject object = new SceneObject();
        object.initialize(directory, objectJson.get(type), this);

        object.setPosition(x, y);
        object.setScale(scale, scale);

        return addSceneObject(object);
    }

    /**
     * Set view translation
     *
     * @param x x screen units along x-axis
     * @param y y screen units along y-axis
     */
    public void setViewTranslation(float x, float y) {
        view.set(x, y);
    }

    /**
     * Translate the screen view
     *
     * @param x x screen units along x-axis
     * @param y y screen units along y-axis
     */
    public void translateView(float x, float y) {
        view.add(x, y);
    }

    /**
     * Returns last set view transform
     */
    public Vector2 getView() {
        return view;
    }

    /**
     * Draws the entire scene to the canvas
     *
     * @param canvas The drawing context
     */
    public void drawLevel(GameCanvas canvas) {
        canvas.beginT(view.x, view.y);

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
        // In an ideal world they would be the same, but lights should be scaled while textures shouldn't
        OrthographicCamera raycamera = new OrthographicCamera(
                canvas.getWidth() / canvas.WorldToScreenX(1),
                canvas.getHeight() / canvas.WorldToScreenY(1)
        );
        if (player != null) {
            raycamera.translate(player.getPosition().x, player.getPosition().y);
            raycamera.update();
        }
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
