package infinityx.lunarhaze.models;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.controllers.EnemyController;
import infinityx.lunarhaze.controllers.InputController;
import infinityx.lunarhaze.graphics.CameraShake;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.entity.Enemy;
import infinityx.lunarhaze.models.entity.EnemyPool;
import infinityx.lunarhaze.models.entity.SceneObject;
import infinityx.lunarhaze.models.entity.Werewolf;
import infinityx.util.Drawable;
import infinityx.util.astar.AStarMap;
import infinityx.util.astar.AStarPathFinding;

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
 * ------------------ (n, m) * {@link Board#getTileWorldDim()} (board is nxm tiles)
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
 * All models' positions are in world coordinates; only once we draw
 * should the relevant transformation from world to screen be done.
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
     * The Box2D World
     */
    private World world;
    /**
     * Memory pool for efficient storage of Enemies
     */
    private EnemyPool enemies;
    /**
     * List of active enemies
     */
    private Array<Enemy> activeEnemies;
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

    /**
     * Path finding utility
     */
    public AStarPathFinding pathfinder;

    /**
     * View translation
     */
    private final Vector2 view = new Vector2();

    /**
     * Holds references to all drawable entities on the level (i.e. sceneObjects, player, enemies)
     */
    private Array<Drawable> drawables;
    private final DrawableCompare drawComp = new DrawableCompare();

    /**
     * The backing set for garbage collection
     */
    private Array<Drawable> backing;

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
     * the total amount of collectable moonlight on the board at initialization
     */
    private int totalMoonlight;

    /**
     * the time it takes to transition from stealth to battle
     */
    private float phaseTransitionTime;

    /**
     * the length of a stealth cycle
     */
    private float phaseLength;

    /**
     * Ambient lighting values during stealth phase
     */
    private float[] stealthAmbience;

    /**
     * Ambient lighting values during battle phase
     */
    private float[] battleAmbience;

    private Sound alert_sound;

    /**
     * Moonlight color of point lights
     */
    private float[] moonlightColor;

    private boolean debugPressed;

    /**
     * Initialize attributes
     */
    private void initialize() {
        world = new World(new Vector2(0, 0), true);
        rayHandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
        rayHandler.setAmbientLight(1);

        drawables = new Array<>();
        backing = new Array<>();

        // There will always be a player
        // So it's fine to initialize now
        Werewolf player = new Werewolf();
        player.initialize(directory, playerJson, this);
        setPlayer(player);

        board = null;
        pathfinder = null;
        enemies = new EnemyPool(20);
        activeEnemies = new Array<>(10);
        sceneObjects = new Array<>(true, 5);
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
     */
    public void flush() {
        initialize();
    }

    public RayHandler getRayHandler() {
        return rayHandler;
    }

    /**
     * @return All active enemies in level.
     */
    public Array<Enemy> getEnemies() {
        return activeEnemies;
    }

    /**
     * @return Remaining number of enemies.
     */
    public int getEnemySize() {
        return activeEnemies.size;
    }

    /**
     * @return the total amount of collectable moonlight on the board at initialization
     */
    public float getTotalMoonlight() {
        return totalMoonlight;
    }

    /**
     * @param enemy Enemy to append to enemy list
     * @return Enemy added with updated id
     */
    public Enemy addEnemy(Enemy enemy) {
        activeEnemies.add(enemy);
        System.out.println("Enemy added");
        addDrawables(enemy);
        // Update enemy controller assigned to the new enemy
        getEnemyControllers().get(enemy).populate(this);
        alert_sound = this.getDirectory().getEntry("alerted", Sound.class);
        getEnemyControllers().get(enemy).setAlertSound(alert_sound);

        return enemy;
    }

    /**
     * Removes enemy from the level.
     *
     * @param enemy enemy to remove
     */
    public void removeEnemy(Enemy enemy) {
        enemies.free(enemy);
        activeEnemies.removeValue(enemy, true);
        drawables.removeValue(enemy, true);
    }

    /**
     * Adds an enemy to the level
     *
     * @param type   type of Enemy to append to enemy list (e.g. villager)
     * @param x      world x-position
     * @param y      world y-position
     * @param patrol patrol path for this enemy
     * @return Enemy added
     */
    public Enemy addEnemy(String type, float x, float y, ArrayList<Vector2> patrol) {
        Enemy enemy = enemies.obtain();
        enemy.initialize(directory, enemiesJson.get(type), this);

        enemy.setPatrolPath(patrol);
        enemy.setPosition(x, y);

        enemy.setName(type);

        return addEnemy(enemy);
    }

    /**
     * @return Mapping of all enemies (dead too) with their controllers
     */
    public ObjectMap<Enemy, EnemyController> getEnemyControllers() {
        return enemies.controls;
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
     * @return the time it takes to transition from stealth to battle
     */
    public float getPhaseTransitionTime() {
        return phaseTransitionTime;
    }

    /**
     * Sets the time it takes to transition from stealth to battle
     */
    public void setPhaseTransitionTime(float phaseTransitionTime) {
        this.phaseTransitionTime = phaseTransitionTime;
    }

    /**
     * @return the length of a stealth cycle
     */
    public float getPhaseLength() {
        return phaseLength;
    }

    /**
     * Sets the length of a stealth cycle
     */
    public void setPhaseLength(float phaseLength) {
        this.phaseLength = phaseLength;
    }

    /**
     * Sets Ambient lighting values during stealth phase
     */
    public void setStealthAmbience(float[] stealthAmbience) {
        this.stealthAmbience = stealthAmbience;
    }

    /**
     * Sets Ambient lighting values during battle phase
     */
    public void setBattleAmbience(float[] battleAmbience) {
        this.battleAmbience = battleAmbience;
    }

    /**
     * Sets Moonlight color of point lights
     */
    public void setMoonlightColor(float[] moonlightColor) {
        this.moonlightColor = moonlightColor;
    }

    /**
     * @return moonlight color of point lights
     */
    public float[] getMoonlightColor() {
        return moonlightColor;
    }

    /**
     * @return Ambient lighting values during stealth phase
     */
    public float[] getStealthAmbience() {
        return stealthAmbience;
    }

    /**
     * @return Ambient lighting values during battle phase
     */
    public float[] getBattleAmbience() {
        return battleAmbience;
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
        this.totalMoonlight = board.getRemainingMoonlight();
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
        SceneObject object = new SceneObject(type);
        object.initialize(directory, objectJson.get(type), this);

        object.setPosition(x, y);
        object.setScale(scale);

        return addSceneObject(object);
    }

    public Array<SceneObject> getSceneObjects() {
        return sceneObjects;
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

    public Color backgroundColor = new Color(0x0f4f47ff).mul(0.8f);

    /**
     * Draws the entire scene to the canvas
     *
     * @param canvas The drawing context
     */
    public void drawLevel(GameCanvas canvas) {
        garbageCollect();
        canvas.clear(backgroundColor);

        //Camera shake logic
        if (CameraShake.timeLeft() > 0) {
            CameraShake.update(Gdx.graphics.getDeltaTime());
            translateView(CameraShake.getShakeOffset().x, CameraShake.getShakeOffset().y);
        }

        canvas.begin(GameCanvas.DrawPass.SPRITE, view.x, view.y);

        // Render order: Board tiles -> (players, enemies, scene objects) sorted by depth (y coordinate)
        board.draw(canvas);

        // Uses timsort, so O(n) if already sorted, which is nice since it usually will be
        // TODO: if this ever becomes a bottleneck, we can instead add the
        //  depth as the z-position so OpenGL's depth buffer can do all the work
        drawables.sort(drawComp);
        for (Drawable d : drawables) {
            d.draw(canvas);
        }

        // Flush information to the graphic buffer.
        canvas.end();

        // A separate transform for lights :(
        // In an ideal world they would be the same, but lights should be scaled while textures shouldn't
        // Mimic same view transform
        canvas.begin(GameCanvas.DrawPass.LIGHT,
                (view.x - canvas.getWidth() / 2) / canvas.WorldToScreenX(1),
                (view.y - canvas.getHeight() / 2) / canvas.WorldToScreenY(1)
        );
        canvas.drawLights(rayHandler);
        canvas.end();

//         ----------------------- DEBUG --------------------------
        if (InputController.getInstance().didDebug()){
            debugPressed = !debugPressed;
        }
        if (debugPressed) {
            if (player.isAttacking) {
                canvas.begin(GameCanvas.DrawPass.SHAPE, view.x, view.y);
                player.getAttackHitbox().drawHitbox(canvas);
                canvas.end();
            }

            canvas.begin(GameCanvas.DrawPass.SHAPE, view.x, view.y);
            for (Enemy e : activeEnemies) {
                getEnemyControllers().get(e).drawGizmo(canvas);
            }
            canvas.end();
        }
    }

    /**
     * Remove all objects set as destroyed from drawing queue.
     */
    public void garbageCollect() {
        // INVARIANT: backing and objects are disjoint
        for (Drawable o : drawables) {
            if (!o.isDestroyed()) {
                backing.add(o);
            }
        }
        // stop-and-copy garbage collection
        // no removal which is nice since each removal is worst case O(n)
        Array<Drawable> tmp = backing;
        backing = drawables;
        drawables = tmp;
        backing.clear();
    }

    private boolean scene;

    /**
     * Creates a tiled (grid) A* path finder.
     *
     * @param gridSize width and height of each grid in world size
     */
    public void createPathFinder(float gridSize) {
        // fill board space
        int width = (int) (board.getWidth() * board.getTileWorldDim().x / gridSize);
        int height = (int) (board.getHeight() * board.getTileWorldDim().y / gridSize);
        AStarMap aStarMap = new AStarMap(width, height, gridSize);

        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                scene = ((GameObject)fixture.getUserData()).getType() == GameObject.ObjectType.SCENE;
                return false; // stop finding other fixtures in the query area
            }
        };

        // If a node overlaps any part of a scene objects body, mark as an obstacle
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                scene = false;
                world.QueryAABB(queryCallback, x * gridSize, y * gridSize, (x + 1) * gridSize, (y + 1) * gridSize);
                if (scene) {
                    aStarMap.getNodeAt(x, y).isObstacle = true;
                }
            }
        }

        //System.out.println(aStarMap);
        pathfinder = new AStarPathFinding(aStarMap, world);
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

