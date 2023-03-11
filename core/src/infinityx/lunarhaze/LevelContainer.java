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

    /**
     * Holds references to all drawable entities on the level (i.e. sceneObjects, player, enemies)
     */
    private final Array<Drawable> drawables;
    private final DrawableCompare drawComp = new DrawableCompare();

    /** Maps tile indices to their respective PointLight pointing on it */
    private IntMap<PointLight> moonlight;

    /**
     * Creates a new LevelContainer with no active elements.
     */
    public LevelContainer() {
        // BOX2D initialization
        world = new World(new Vector2(0, 0), true);

        // TODO: Maybe set in asset json and let LevelParser set these?
        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);
        rayHandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
        rayHandler.setAmbientLight(0.25f, 0.22f, 0.32f, 0.25f);
        rayHandler.setBlur(true);
        rayHandler.setBlurNum(5);
        rayHandler.setShadows(true);


        player = null;
        board = null;
        enemies = new EnemyList();
        sceneObjects = new Array<>(true, 5);
        moonlight = new IntMap<>();

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

        // Lighting, physics
        enemy.setFlashlight(new ConeSource(rayHandler, 512, new Color(0.8f, 0.6f, 0f, 0.9f), 2f, enemy.getX(), enemy.getY(), 0f, 30));
        enemy.setFlashlightOn(true);

        enemy.activatePhysics(world);
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

        // Lighting, physics
        player.setSpotLight(new PointLight(rayHandler, 512, new Color(0.65f, 0.6f, 0.77f, 0.6f), 1f, 0, 0));
        player.activatePhysics(world);

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
    public void setBoard(Board board) { this.board = board; }

    /**
     * @param obj Scene Object to add
     */
    public void addSceneObject(SceneObject obj) {
        sceneObjects.add(obj);
        drawables.add(obj);
    }

    /**
     * Sets a tile as lit or not.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setLit(int x, int y, boolean lit) {
        board.setLit(x, y, lit);
        if (lit) {
            addLightAt(x, y);
        } else {
            removeLightAt(x, y);
        }
    }
    private void removeLightAt(int x, int y) {
        int t_pos = x + y * board.getWidth();
        if (!moonlight.containsKey(t_pos)) {
            return;
        }
        moonlight.get(t_pos).setActive(false);
    }
    private void addLightAt(int x, int y) {
        int t_pos = x + y * board.getWidth();
        if (!moonlight.containsKey(t_pos)) {
            Vector2 worldPos = board.boardCenterToWorld(x, y);
            moonlight.put(t_pos, new PointLight(rayHandler, 512, LightingController.LIGHTCOLOR, 1.5f, worldPos.x, worldPos.y));
        }
        moonlight.get(t_pos).setActive(true);
    }

    /**
     * Draws the entire scene to the canvas
     *
     * @param canvas The drawing context
     */
    public void drawLevel(GameCanvas canvas) {
        // Puts player at center of canvas

        view.setToTranslation(
                -GameCanvas.WorldToScreenX(player.getPosition().x) + canvas.getWidth() / 2,
                -GameCanvas.WorldToScreenY(player.getPosition().y) + canvas.getHeight() / 2
        );
        canvas.begin(view);

        //System.out.printf("Player pos: (%f, %f)\n", player.position.x, player.position.y);

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
                canvas.getWidth() / GameCanvas.WorldToScreenX(1),
                canvas.getHeight() / GameCanvas.WorldToScreenY(1)
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
