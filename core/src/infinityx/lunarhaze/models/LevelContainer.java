package infinityx.lunarhaze.models;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.TimeUtils;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.controllers.EnemyController;
import infinityx.lunarhaze.controllers.EnemySpawner;
import infinityx.lunarhaze.controllers.InputController;
import infinityx.lunarhaze.graphics.CameraShake;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.entity.*;
import infinityx.util.Drawable;
import infinityx.util.PatrolPath;
import infinityx.util.astar.AStarMap;
import infinityx.util.astar.AStarPathFinding;

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
     * Owns the enemy spawner, used for battle phase
     */
    private EnemySpawner enemySpawner;

    /**
     * Memory pool for efficient storage of Enemies
     */
    private EnemyPool<Villager> villagers;

    private EnemyPool<Archer> archers;
    /**
     * List of active enemies
     */
    private Array<Enemy> activeEnemies;

    /**
     * List of active enemy controllers
     */
    private Array<EnemyController> activeControllers;

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
     * the total amount of collectable moonlight on the board at initialization
     */
    private int totalMoonlight;

    /**
     * Settings for this level
     */
    private Settings battleSettings;

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

    /**
     * Lights attached to lamp scene objects
     */
    private Array<PointLight> lampLights;

    private boolean debugPressed;

    private final ShaderProgram lightShader;
    private float totalTime;

    /**
     * Initialize attributes
     */
    private void initialize() {
        totalTime = 0;
        world = new World(new Vector2(0, 0), true);
        rayHandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
        rayHandler.setAmbientLight(1);
        rayHandler.setLightShader(lightShader);
        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);

        drawables = new Array<>();
        backing = new Array<>();
        lampLights = new Array<>();

        // There will always be a player
        // So it's fine to initialize now
        Werewolf player = new Werewolf();
        player.initialize(directory, playerJson, this);
        setPlayer(player);

        board = null;
        pathfinder = null;
        enemySpawner = new EnemySpawner(this);
        villagers = new EnemyPool<>(20, Villager.class);
        archers = new EnemyPool<>(20, Archer.class);
        activeEnemies = new Array<>(10);
        activeControllers = new Array<>(10);
        sceneObjects = new Array<>(true, 5);

        battleSettings = new Settings();
    }

    public void setEnemyDamage(float damage) {
        for (Enemy enemy : activeEnemies) {
            enemy.attackDamage = damage;
        }
    }

    /**
     * Creates a new LevelContainer with no active elements.
     */
    public LevelContainer(AssetDirectory directory, JsonValue enemiesJson, JsonValue objectJson, JsonValue playerJson) {
        this.enemiesJson = enemiesJson;
        this.objectJson = objectJson;
        this.playerJson = playerJson;
        this.directory = directory;
        this.lightShader = directory.get("light", ShaderProgram.class);
        System.out.println(lightShader.getLog());
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

    public Settings getSettings() {
        return battleSettings;
    }

    /**
     * @return All active enemies in level.
     */
    public Array<Enemy> getEnemies() {
        return activeEnemies;
    }

    /**
     * @return the total amount of collectable moonlight on the board at initialization
     */
    public float getTotalMoonlight() {
        return totalMoonlight;
    }

    /**
     * @param enemy           enemy to append to enemy list
     * @param enemyController controller of that enemy
     * @return enemy added
     */
    public Enemy addEnemy(Enemy enemy, EnemyController enemyController) {
        activeEnemies.add(enemy);
        activeControllers.add(enemyController);
        addDrawables(enemy);
        //if (enemy.getEnemyType() == Enemy.EnemyType.Villager)
        //    addDrawables(((Villager) enemy).attackHitbox);

        // Update enemy controller assigned to the new enemy
        enemyController.populate(this);
        alert_sound = this.getDirectory().getEntry("alerted", Sound.class);
        enemyController.setAlertSound(alert_sound);

        enemy.setActive(true);
        enemy.getFlashlight().setActive(true);

        return enemy;
    }

    /**
     * @param enemy           enemy to append to enemy list
     * @return enemy added
     */
    public Enemy addEnemy(Enemy enemy) {
        activeEnemies.add(enemy);
        addDrawables(enemy);
        enemy.setActive(true);
        enemy.getFlashlight().setActive(true);

        return enemy;
    }

    /**
     * Removes enemy from the level.
     *
     * @param enemy enemy to remove
     */
    public void removeEnemy(Enemy enemy) {
        switch (enemy.getEnemyType()) {
            case Archer:
                archers.free((Archer) enemy);
                activeControllers.removeValue(archers.controls.get((Archer) enemy), true);
                break;
            case Villager:
                villagers.free((Villager) enemy);
                activeControllers.removeValue(villagers.controls.get((Villager) enemy), true);
                //drawables.removeValue(((Villager) enemy).attackHitbox, true);
        }

        activeEnemies.removeValue(enemy, true);
        drawables.removeValue(enemy, true);
        enemy.setActive(false);
        enemy.getFlashlight().setActive(false);
    }

    /**
     * Adds an enemy to the level with no patrol path.
     *
     * @param type type of enemy to append to enemy list (e.g. archer)
     * @param x    world x-position
     * @param y    world y-position
     * @return Enemy added
     */
    public Enemy addEnemy(Enemy.EnemyType type, float x, float y) {
        return addEnemy(type, x, y, new PatrolPath().addWaypoint(x, y));
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
    public Enemy addEnemy(Enemy.EnemyType type, float x, float y, PatrolPath patrol) {
        Enemy enemy = null;
        EnemyController controller = null;
        switch (type) {
            case Villager:
                enemy = villagers.obtain();
                controller = villagers.controls.get((Villager) enemy);
                break;
            case Archer:
                enemy = archers.obtain();
                controller = archers.controls.get((Archer) enemy);
                break;
        }
        enemy.initialize(directory, enemiesJson.get(type.toString()), this);
        enemy.setPatrolPath(patrol);
        enemy.setPosition(x, y);

        return addEnemy(enemy, controller);
    }

    public Array<EnemyController> getActiveControllers() {
        return activeControllers;
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
     * Sets Moonlight color of point lights. Does not actually update the lights.
     * Currently used only for the editor.
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
        drawables.add(player.attackHitbox);
        this.player = player;
    }

    public AssetDirectory getDirectory() {
        return this.directory;
    }

    /**
     * Hide player from drawing. Used for level editor.
     */
    public void hidePlayer() {
        drawables.removeValue(player, true);
        player.setActive(false);
        player.getSpotlight().setActive(false);
    }

    /**
     * Show player for drawing. Used for level editor.
     */
    public void showPlayer() {
        drawables.add(player);
        player.setActive(true);
        player.getSpotlight().setActive(true);
    }


    public EnemySpawner getEnemySpawner() {
        return enemySpawner;
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
        obj.setActive(true);

        return obj;
    }

    /**
     * Removes a scene object from the level.
     *
     * @param object scene object to remove
     */
    public void removeSceneObject(SceneObject object) {
        sceneObjects.removeValue(object, true);
        drawables.removeValue(object, true);
        object.setActive(false);
    }

    /**
     * @param type    type of scene object to add (e.g. house)
     * @param x       world x-position
     * @param y       world y-position
     * @param scale   scale of object
     * @param flipped sets {@link SceneObject#isFlipped()}
     * @return scene object added
     */
    public SceneObject addSceneObject(String type, float x, float y, float scale, boolean flipped) {
        SceneObject object = new SceneObject(type);
        object.initialize(directory, objectJson.get(type), this);
        object.activatePhysics(world);

        object.setPosition(x, y);
        object.setScale(scale);
        object.setName(type);
        object.setFlipped(flipped);

        if (type.equalsIgnoreCase("lamp")) {
            PointLight light = new PointLight(
                    rayHandler, 20,
                    new Color(moonlightColor[0], moonlightColor[1], moonlightColor[2], moonlightColor[3]),
                    5, x, y
            );
            light.setActive(true);
            lampLights.add(light);
        }

        return addSceneObject(object);
    }

    public Array<PointLight> getLampLights() {
        return lampLights;
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


    /**
     * Draws the entire scene to the canvas
     *
     * @param canvas The drawing context
     */
    public void drawLevel(float delta, GameCanvas canvas) {
        totalTime += delta;
        garbageCollect();

        //Camera shake logic
        if (CameraShake.timeLeft() > 0) {
            CameraShake.update(Gdx.graphics.getDeltaTime());
            translateView(CameraShake.getShakeOffset().x, CameraShake.getShakeOffset().y);
        }

        // Render order: Board tiles -> (players, enemies, scene objects) sorted by depth (y coordinate) -> Lights
        canvas.begin(GameCanvas.DrawPass.SPRITE, view.x, view.y);
        board.draw(canvas);

        // Uses timsort, so O(n) if already sorted, which is nice since it usually will be
        // TODO: if this ever becomes a bottleneck, we can instead add the
        //  depth as the z-position so OpenGL's depth buffer can do all the work
        drawables.sort(drawComp);
        for (Drawable d : drawables) {
            d.draw(canvas);
        }

        // The scene objects rendered before the player (behind) should not become transparent
        canvas.playerCoords.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);

        // Flush information to the graphic buffer.
        canvas.end();

        canvas.begin(GameCanvas.DrawPass.LIGHT, view.x, view.y);
        lightShader.bind();
        lightShader.setUniformf("iTime", totalTime);
        canvas.drawLights(rayHandler);
        canvas.end();

        // ------------------------ DEBUG --------------------------
        if (InputController.getInstance().didDebug()) {
            debugPressed = !debugPressed;
        }
        if (debugPressed) {
            if (player.isAttacking) {
                canvas.begin(GameCanvas.DrawPass.SHAPE, view.x, view.y);
                player.attackHitbox.drawHitbox(canvas);
                canvas.end();
            }

            canvas.begin(GameCanvas.DrawPass.SHAPE, view.x, view.y);
            for (EnemyController e : getActiveControllers()) {
                e.drawGizmo(canvas);
                e.drawDetection(canvas);
                e.getEnemy().drawSteeringOutput(canvas);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
                pathfinder.map.drawMap(canvas);
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

    // used in pathfinder obstacle callback
    private boolean scene;

    /**
     * Creates a tiled (grid) A* path finder.
     *
     * @param gridSize width and height of each grid in world size
     */
    public void createPathFinder(Vector2 gridSize) {
        // fill board space
        int width = (int) (board.getWidth() * board.getTileWorldDim().x / gridSize.x);
        int height = (int) (board.getHeight() * board.getTileWorldDim().y / gridSize.y);
        AStarMap aStarMap = new AStarMap(width, height, gridSize);

        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                GameObject hitObj = (GameObject) fixture.getUserData();
                if (hitObj.getType() == GameObject.ObjectType.SCENE && !hitObj.isSensor()) {
                    scene = true;
                    return false; // stop finding other fixtures in the query area
                }
                return true;
            }
        };

        // If a node overlaps any part of a scene objects body, mark as an obstacle
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                scene = false;
                world.QueryAABB(
                        queryCallback,
                        x * gridSize.x,
                        y * gridSize.y,
                        (x + 1) * gridSize.x,
                        (y + 1) * gridSize.y
                );
                if (scene) {
                    aStarMap.getNodeAt(x, y).isObstacle = true;
                }
            }
        }

        //System.out.println(aStarMap);
        pathfinder = new AStarPathFinding(aStarMap);
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