package infinityx.lunarhaze.screens;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import imgui.*;
import imgui.flag.ImDrawFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiMouseButton;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.controllers.LevelParser;
import infinityx.lunarhaze.graphics.FilmStrip;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.graphics.ImGuiImplGLES2;
import infinityx.lunarhaze.models.Board;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Enemy;
import infinityx.lunarhaze.models.entity.SceneObject;
import infinityx.util.PatrolPath;
import infinityx.util.ScreenObservable;

import static infinityx.lunarhaze.controllers.LevelSerializer.saveLevel;

/**
 * Screen providing a level editor
 */
public class EditorMode extends ScreenObservable implements Screen, InputProcessor {
    /**
     * Need an ongoing reference to the asset directory
     */
    protected AssetDirectory directory;
    /**
     * Reference to GameCanvas created by the root
     */
    private GameCanvas canvas;

    /**
     * Contains level details!
     */
    private LevelContainer level;

    /**
     * Reference to levels board
     */
    private Board board;

    /**
     * Size of the board (e.g. 10x10 would be [10, 10])
     */
    private int[] boardSize;

    /**
     * User requested to go to menu
     */
    public final static int GO_MENU = 0;
    /**
     * User requested to test current level
     */
    public final static int GO_PLAY = 1;

    /**
     * ImGui classes
     */
    private ImGuiImplGlfw imGuiGlfw;
    private ImGuiImplGLES2 imGuiGl;

    /**
     * Whether to display the File->New popup to select board size
     */
    private ImBoolean showNewBoardWindow = new ImBoolean();

    /**
     * Whether to display the error when you cannot save
     */
    private boolean showCannotSaveError;

    /**
     * Whether the player has been placed on the board
     */
    private boolean playerPlaced;

    /**
     * Background texture for the editor
     */
    private Texture background;

    /**
     * Background music
     */
    private Music music;

    abstract static class Selected {
        /**
         * Enum specifying the type of the selected object.
         */
        public enum Type {
            TILE, PLAYER, ENEMY, OBJECT, EXIST_ENEMY, EXIST_OBJECT, MOONLIGHT
        }

        /**
         * Returns the type of this object.
         * <p>
         * We use this instead of runtime-typing for performance reasons.
         *
         * @return the type of this object.
         */
        public abstract Type getType();
    }

    /**
     * Holds the necessary information to place moonlight.
     * None necessary since moonlight is placed at mouse position.
     */
    class Moonlight extends Selected {
        @Override
        public Type getType() {
            return Type.MOONLIGHT;
        }
    }

    /**
     * Holds the necessary information to place a tile
     */
    class Tile extends Selected {
        public Tile(int num) {
            this.num = num;
        }

        /**
         * Follows {@link infinityx.lunarhaze.models.Tile#getTileNum()}
         */
        public int num;

        @Override
        public Type getType() {
            return Type.TILE;
        }
    }

    /**
     * No information needed since container makes its own player
     */
    class Player extends Selected {
        @Override
        public Type getType() {
            return Type.PLAYER;
        }
    }


    /**
     * Holds the necessary information to place an enemy
     */
    class EnemySelection extends Selected {
        public EnemySelection(Enemy enemy) {
            this.enemy = enemy;
        }

        /**
         * wrapped enemy
         */
        public Enemy enemy;

        @Override
        public Type getType() {
            return Type.ENEMY;
        }
    }

    /**
     * The user has selected an existing enemy in the scene
     */
    class ExistingEnemy extends Selected {
        public ExistingEnemy(Enemy enemy) {
            this.enemy = enemy;
        }

        /**
         * wrapped enemy
         */
        public Enemy enemy;

        @Override
        public Type getType() {
            return Type.EXIST_ENEMY;
        }
    }

    /**
     * Holds the necessary information to place an object
     */
    class ObjectSelection extends Selected {
        public ObjectSelection(SceneObject object) {
            this.object = object;
        }

        /**
         * Wrapped object
         */
        public SceneObject object;

        @Override
        public Type getType() {
            return Type.OBJECT;
        }
    }

    /**
     * The user has selected an existing object in the scene
     */
    class ExistingObject extends Selected {
        public ExistingObject(SceneObject object) {
            this.object = object;
        }

        /**
         * Wrapped object
         */
        public SceneObject object;

        @Override
        public Type getType() {
            return Type.EXIST_OBJECT;
        }
    }

    /**
     * Represents an action that the editor has done.
     */
    abstract static class Action {
        /**
         * Represents the type of action performed.
         */
        public enum Type {
            MOONLIGHT,
            ADD_OBJECT,
            REMOVE_OBJECT,
            ADD_POINT,
            REMOVE_POINT
        }

        /**
         * Returns the type of this object.
         * <p>
         * We use this instead of runtime-typing for performance reasons.
         *
         * @return the type of this object.
         */
        public abstract Type getType();
    }

    /**
     * Represents an action for placing an object.
     */
    public static class PlaceObject extends Action {
        public GameObject gameObject;

        /**
         * Constructs a PlaceObject action with the given GameObject.
         *
         * @param gameObject A GameObject to be placed.
         */
        public PlaceObject(GameObject gameObject) {
            this.gameObject = gameObject;
        }

        @Override
        public Type getType() {
            return Type.ADD_OBJECT;
        }
    }

    /**
     * Represents an action for removing an object.
     */
    public static class RemoveObject extends Action {
        public GameObject gameObject;

        /**
         * Constructs a RemoveObject action with the given GameObject.
         *
         * @param gameObject A GameObject that was removed.
         */
        public RemoveObject(GameObject gameObject) {
            this.gameObject = gameObject;
        }

        @Override
        public Type getType() {
            return Type.REMOVE_OBJECT;
        }
    }

    /**
     * Represents an action for adding a point to a patrol path.
     */
    public static class AddPoint extends Action {

        public Enemy enemy;
        public Vector2 pos;

        /**
         * @param pos   position of point
         * @param enemy which enemy this point patrol path was from
         */
        public AddPoint(Vector2 pos, Enemy enemy) {
            this.pos = pos;
            this.enemy = enemy;
        }

        @Override
        public Type getType() {
            return Type.ADD_POINT;
        }
    }

    /**
     * Represents an action for removing an object.
     */
    public static class RemovePoint extends Action {
        public Enemy enemy;
        public int index;
        public Vector2 pos;

        /**
         * @param pos   position of point
         * @param index index into patrol path it was removed from
         * @param enemy which enemy this point patrol path was from
         */
        public RemovePoint(Vector2 pos, int index, Enemy enemy) {
            this.pos = pos;
            this.index = index;
            this.enemy = enemy;
        }

        @Override
        public Type getType() {
            return Type.REMOVE_POINT;
        }
    }

    /**
     * Represents an action for placing moonlight at a specific board position.
     */
    public static class PlaceMoonlight extends Action {
        public int x;
        public int y;

        /**
         * Constructs a PlaceMoonlight action with the given board position.
         *
         * @param x The x-coordinate of the board position.
         * @param y The y-coordinate of the board position.
         */
        public PlaceMoonlight(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public Type getType() {
            return Type.MOONLIGHT;
        }
    }

    /**
     * Holds the necessary information to display the enemy button
     */
    class EnemyButton {
        public EnemyButton(Texture texture, Enemy.EnemyType type) {
            this.type = type;
            this.texture = texture;
        }

        /**
         * type of Enemy
         */
        public Enemy.EnemyType type;

        /**
         * Texture for button
         */
        public Texture texture;
    }

    /**
     * Holds the necessary information to display the scene object button
     */
    class SceneButton {
        public SceneButton(Texture texture, String type) {
            this.type = type;
            this.texture = texture;
        }

        /**
         * type of scene object
         */
        public String type;

        /**
         * Texture for button
         */
        public Texture texture;
    }


    /**
     * What is on my cursor right now? null if none
     */
    private Selected selected;

    /**
     * Tint for textures before placement
     */
    public static final Color SELECTED_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.9f);

    /**
     * Tint for textures for overlapping
     */
    public static final Color OVERLAPPED_COLOR = Color.RED.cpy().mul(1, 1, 1, 0.8f);

    /**
     * Holds world coordinates of cursor
     */
    private final Vector2 mouseWorld = new Vector2();

    /**
     * Last board position the mouse was on
     */
    private final Vector2 mouseBoard = new Vector2();

    /**
     * Float holding ambient lighting during stealth phase
     */
    private float[] stealthLighting;

    /**
     * Float holding ambient lighting during battle phase
     */
    private float[] battleLighting;

    /**
     * Float holding moonlight PointLight color
     */
    private float[] moonlightLighting;

    /**
     * Float holding object scale
     */
    private float objectScale[];

    /**
     * Current enemy being changed in the Enemy->Enemy x new window
     */
    private Enemy currEnemyControlled;

    /**
     * List of scene object buttons
     */
    Array<SceneButton> objectSelections;

    /**
     * List of enemy brush buttons
     */
    Array<EnemyButton> enemySelections;

    /**
     * Whether the selected object is currently overlapping an existing object
     */
    boolean overlapped;

    /**
     * When false, display the stealth ambient lighting, when true, display the battle ambient lighting
     */
    private boolean showBattleLighting;

    /**
     * Whether an undo or redo was pressed this frame
     */
    private boolean changed = true;

    /**
     * Stores actions that have been done, ordered.
     */
    Array<Action> doneActions;

    /**
     * Stores actions that have been undone and can be redone.
     */
    Array<Action> undoneActions;

    /**
     * Current level to save to
     */
    private ImInt saveTo = new ImInt(1);

    /**
     * Whether to show the "Overwrite level" prompt
     */
    private boolean showOverwritePrompt = false;

    /**
     * Whether to show the save level popup
     */
    private boolean showSaveLevelPopup = false;

    /**
     * Current index into enemy patrol path the user is moving around
     */
    private int selectedWaypointIndex;

    public EditorMode(GameCanvas canvas) {
        this.canvas = canvas;
        objectSelections = new Array<>();
        enemySelections = new Array<>();
    }

    /**
     * Gather the required assets.
     * <p>
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        this.directory = directory;
        JsonValue objects = directory.getEntry("objects", JsonValue.class);
        JsonValue enemies = directory.getEntry("enemies", JsonValue.class);

        // Add all objects in json
        for (JsonValue object : objects) {
            objectSelections.add(
                    new SceneButton(
                            directory.getEntry(
                                    object.get("textures").getString(
                                            object.get("texture").getString("name")
                                    ), Texture.class
                            ), object.name
                    )
            );
        }

        // Add all enemies in json
        for (JsonValue enemy : enemies) {
            enemySelections.add(
                    new EnemyButton(
                            directory.getEntry(
                                    enemy.get("textures").getString(
                                            enemy.get("texture").getString("name")
                                    ), Texture.class
                            ), Enemy.EnemyType.fromString(enemy.name)
                    )
            );
        }

        this.background = directory.getEntry("bkg_allocate", Texture.class);
        this.music = directory.getEntry("editorBackground", Music.class);
    }

    /**
     * Places a tile of the selected type at the current mouse position on the game board
     * Invariant: selected is a Tile
     */
    private void placeTile() {
        // Enforce invariant
        assert selected instanceof Tile;

        if (!board.inBoundsWorld(mouseWorld.x, mouseWorld.y))
            return;

        Tile curr = (Tile) selected;
        board.setTileNum(
                (int) mouseBoard.x, (int) mouseBoard.y,
                curr.num
        );

        // Sets the tile type for serialization
        board.setTileType((int) mouseBoard.x, (int) mouseBoard.y, infinityx.lunarhaze.models.Tile.TileType.Road);
    }


    /**
     * Places the current selection at the current mouse position on the game board,
     * handling each object type with the appropriate method (placeTile(),
     * placeSceneObject(), etc.).
     */
    private void placeSelection() {
        if (overlapped)
            return;

        switch (selected.getType()) {
            case TILE:
                board.removePreview();
                placeTile();
                break;
            case PLAYER:
                level.getPlayer().setPosition(mouseWorld);
                playerPlaced = true;
                level.getPlayer().setTint(Color.WHITE);
                removeSelection();
                break;
            case OBJECT:
                // Perform action
                doneActions.add(new PlaceObject(placeSceneObject()));
                undoneActions.clear();
                break;
            case ENEMY:
                Enemy newEnemy = placeEnemy();

                currEnemyControlled = newEnemy;

                // Perform action
                doneActions.add(new PlaceObject(newEnemy));
                undoneActions.clear();
                break;
            case MOONLIGHT:
                placeMoonlightTile();
                break;
        }
    }

    /**
     * Creates a new enemy of the selected type at the current mouse position on the game board,
     * adding it to the level.
     */
    private Enemy placeEnemy() {
        EnemySelection enemySelection = (EnemySelection) selected;
        // Initial waypoint as enemy position
        Enemy newEnemy = level.addEnemy(
                enemySelection.enemy.getEnemyType(),
                mouseWorld.x, mouseWorld.y,
                enemySelection.enemy.getPatrolPath().clone().addWaypoint(mouseWorld.x, mouseWorld.y)
        );
        newEnemy.setScale(enemySelection.enemy.getScale());
        return newEnemy;
    }

    /**
     * Places a moonlight tile at the current mouse position on the game board, and creates a new PointLight at
     * the tile position. Note that this PointLight is not serialized, and the LevelSerializer
     * searches the board to find all the tiles where board.isLit is true, and the PointLights are only created
     * so that they can be visualized in the LevelEditor.
     */
    private void placeMoonlightTile() {
        int x = (int) mouseBoard.x;
        int y = (int) mouseBoard.y;

        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            if (!board.isLit(x, y)) return;
            board.setLit(x, y, false);
            // So moonlightTiles in board can update
            board.setCollectable(x, y, false);
        } else {
            if (!board.isLit(x, y)) {
                // PointLight logic
                PointLight light = new PointLight(
                        level.getRayHandler(),
                        40,
                        new Color(moonlightLighting[0], moonlightLighting[1], moonlightLighting[2], moonlightLighting[3]),
                        2.5f, board.boardCenterToWorldX(x), board.boardCenterToWorldY(y)
                );

                light.setSoft(true);
                board.setSpotlight(x, y, light);

                // Set board tile to lit
                board.setLit(x, y, true);
                // So moonlightTiles in board can update
                board.setCollectable(x, y, true);
                // Perform action
                doneActions.add(new PlaceMoonlight(x, y));
                undoneActions.clear();
            }
        }
    }

    /**
     * Places a scene object of the selected type at the current mouse position on the game board
     * Invariant: selected is a SceneObject
     */
    private SceneObject placeSceneObject() {
        SceneObject curr = ((ObjectSelection) selected).object;
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            return level.addSceneObject(
                    curr.getName(),
                    mouseBoard.x, mouseBoard.y,
                    curr.getScale(), curr.isFlipped()
            );
        } else {
            return level.addSceneObject(
                    curr.getName(),
                    mouseWorld.x, mouseWorld.y,
                    curr.getScale(), curr.isFlipped()
            );
        }
    }

    /**
     * Reset and stop drawing the current selection if exists
     */
    private void removeSelection() {
        if (selected == null) return;
        switch (selected.getType()) {
            case EXIST_ENEMY:
                ((ExistingEnemy) selected).enemy.setTint(Color.WHITE);
                currEnemyControlled = null;
                selectedWaypointIndex = -1;
                break;
            case EXIST_OBJECT:
                ((ExistingObject) selected).object.setTint(Color.WHITE);
                break;
            case ENEMY:
                level.removeEnemy(((EnemySelection) selected).enemy);
                break;
            case OBJECT:
                level.removeSceneObject(((ObjectSelection) selected).object);
                break;
            case PLAYER:
                if (!playerPlaced)
                    level.hidePlayer();
                break;
            case TILE:
                board.removePreview();
                break;
            default:
                break;
        }
        selected = null;
    }

    /**
     * Undoes the last performed action. Actions include placing enemies, scene objects, and moonlight.
     */
    private void undo() {
        if (!doneActions.isEmpty()) {
            Action lastAction = doneActions.removeIndex(doneActions.size - 1);

            switch (lastAction.getType()) {
                case MOONLIGHT:
                    PlaceMoonlight act = (PlaceMoonlight) lastAction;
                    board.setLit(act.x, act.y, false);
                    break;
                case ADD_OBJECT:
                    GameObject obj = ((PlaceObject) lastAction).gameObject;
                    switch (obj.getType()) {
                        case ENEMY:
                            level.removeEnemy((Enemy) obj);
                            // Remove the controller window if it's controlling the removed enemy
                            if (currEnemyControlled == obj) {
                                currEnemyControlled = null;
                                selectedWaypointIndex = -1;
                            }
                            break;
                        case SCENE:
                            level.removeSceneObject((infinityx.lunarhaze.models.entity.SceneObject) obj);
                            break;
                    }
                    break;
                case REMOVE_OBJECT:
                    GameObject removedObj = ((RemoveObject) lastAction).gameObject;
                    switch (removedObj.getType()) {
                        case ENEMY:
                            level.addEnemy((Enemy) removedObj);
                            currEnemyControlled = (Enemy) removedObj;
                            break;
                        case SCENE:
                            level.addSceneObject((infinityx.lunarhaze.models.entity.SceneObject) removedObj);
                            break;
                    }
                    break;
                case ADD_POINT:
                    if (currEnemyControlled != ((AddPoint) lastAction).enemy) {
                        undo();
                        return;
                    }
                    PatrolPath path = currEnemyControlled.getPatrolPath();
                    path.removeWaypoint(path.getWaypointCount() - 1);
                    break;
                case REMOVE_POINT:
                    if (currEnemyControlled != ((RemovePoint) lastAction).enemy) {
                        undo();
                        return;
                    }
                    RemovePoint removePoint = (RemovePoint) lastAction;
                    PatrolPath patrolPath = currEnemyControlled.getPatrolPath();
                    patrolPath.addWaypointAt(removePoint.pos.x, removePoint.pos.y, removePoint.index);
                    break;
            }
            undoneActions.add(lastAction);
        }
    }

    /**
     * Redoes the last undone action. Actions include placing enemies, scene objects, and moonlight.
     */
    private void redo() {
        if (!undoneActions.isEmpty()) {
            Action lastUndoneAction = undoneActions.removeIndex(undoneActions.size - 1);
            switch (lastUndoneAction.getType()) {
                case MOONLIGHT:
                    PlaceMoonlight act = (PlaceMoonlight) lastUndoneAction;
                    board.setLit(act.x, act.y, true);
                    break;
                case ADD_OBJECT:
                    GameObject obj = ((PlaceObject) lastUndoneAction).gameObject;
                    switch (obj.getType()) {
                        case ENEMY:
                            level.addEnemy((Enemy) obj);
                            currEnemyControlled = (Enemy) obj;
                            break;
                        case SCENE:
                            level.addSceneObject((infinityx.lunarhaze.models.entity.SceneObject) obj);
                            break;
                    }
                    break;
                case REMOVE_OBJECT:
                    GameObject removedObj = ((RemoveObject) lastUndoneAction).gameObject;
                    switch (removedObj.getType()) {
                        case ENEMY:
                            level.removeEnemy((Enemy) removedObj);
                            // Remove the controller window if it's controlling the removed enemy
                            if (currEnemyControlled == removedObj) {
                                currEnemyControlled = null;
                                selectedWaypointIndex = -1;
                            }
                            break;
                        case SCENE:
                            level.removeSceneObject((infinityx.lunarhaze.models.entity.SceneObject) removedObj);
                            break;
                    }
                    break;
                case ADD_POINT:
                    AddPoint addPoint = (AddPoint) lastUndoneAction;
                    if (currEnemyControlled != addPoint.enemy) {
                        redo();
                        return;
                    }
                    PatrolPath path = currEnemyControlled.getPatrolPath();
                    path.addWaypoint(addPoint.pos.x, addPoint.pos.y);
                    break;
                case REMOVE_POINT:
                    if (currEnemyControlled != ((RemovePoint) lastUndoneAction).enemy) {
                        redo();
                        return;
                    }
                    PatrolPath patrolPath = currEnemyControlled.getPatrolPath();
                    patrolPath.removeWaypoint(((RemovePoint) lastUndoneAction).index);
                    break;
            }
            doneActions.add(lastUndoneAction);
        }
    }

    /**
     * Cache for getAABB
     */
    Vector2 vertex = new Vector2();

    /**
     * Computes the Axis-Aligned Bounding Box (AABB) for the given fixture, considering
     * the transform of the body the fixture is attached to. The AABB is represented by
     * its lower and upper bounds.
     *
     * @param fixture    The fixture for which the AABB should be calculated.
     * @param lowerBound A Vector2 instance to store the lower bound of the AABB. The
     *                   method will modify this instance.
     * @param upperBound A Vector2 instance to store the upper bound of the AABB. The
     *                   method will modify this instance.
     * @throws IllegalArgumentException if the fixture has an unsupported shape type.
     */
    private void getAABB(Fixture fixture, Vector2 lowerBound, Vector2 upperBound) {
        Shape shape = fixture.getShape();
        Shape.Type shapeType = shape.getType();
        Transform bodyTransform = fixture.getBody().getTransform();

        if (shapeType == Shape.Type.Polygon) {
            PolygonShape polygonShape = (PolygonShape) shape;
            int vertexCount = polygonShape.getVertexCount();

            // Get the first transformed vertex and initialize the bounds
            polygonShape.getVertex(0, vertex);
            vertex = bodyTransform.mul(vertex);
            lowerBound.set(vertex);
            upperBound.set(vertex);

            // Compute the bounds by iterating over the remaining vertices
            for (int i = 1; i < vertexCount; i++) {
                polygonShape.getVertex(i, vertex);
                vertex = bodyTransform.mul(vertex);

                lowerBound.x = Math.min(lowerBound.x, vertex.x);
                lowerBound.y = Math.min(lowerBound.y, vertex.y);
                upperBound.x = Math.max(upperBound.x, vertex.x);
                upperBound.y = Math.max(upperBound.y, vertex.y);
            }
        } else if (shapeType == Shape.Type.Circle) {
            CircleShape circleShape = (CircleShape) shape;
            Vector2 center = bodyTransform.mul(circleShape.getPosition());
            float radius = circleShape.getRadius();

            lowerBound.set(center.x - radius, center.y - radius);
            upperBound.set(center.x + radius, center.y + radius);
        } else {
            throw new IllegalArgumentException("Unsupported shape type: " + shapeType);
        }
    }

    /**
     * Cache output of getAABB
     */
    Vector2 lowerCache = new Vector2();
    Vector2 upperCache = new Vector2();

    /**
     * Callback used to determine {@link #overlapped}
     */
    QueryCallback queryCallback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if (fixture.getBody().getUserData() == getSelectedObject()) return true;
            overlapped = true;
            return false;
        }
    };

    /**
     * Callback for scene used to determine {@link #overlapped}
     */
    QueryCallback sceneCallback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if (fixture.getBody().getUserData() == getSelectedObject()) return true;
            GameObject hit = (GameObject) fixture.getBody().getUserData();
            if (hit.getType() == GameObject.ObjectType.ENEMY || hit.getType() == GameObject.ObjectType.WEREWOLF) {
                overlapped = true;
                return false;
            }
            return true;
        }
    };

    /**
     * Callback for scene used to determine what object in scene to select
     */
    QueryCallback selectCallback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            GameObject hit = (GameObject) fixture.getBody().getUserData();

            switch (hit.getType()) {
                case ENEMY:
                    selected = new ExistingEnemy((Enemy) hit);
                    // Open controller for selected enemy
                    currEnemyControlled = (Enemy) hit;
                    break;
                case WEREWOLF:
                    selected = new Player();
                    playerPlaced = false;
                    break;
                case SCENE:
                    selected = new ExistingObject((SceneObject) hit);
            }

            hit.setTint(SELECTED_COLOR);
            return true;
        }
    };

    /**
     * Returns the currently selected game object if it exists, null otherwise
     */
    private GameObject getSelectedObject() {
        if (selected == null) return null;
        switch (selected.getType()) {
            case EXIST_OBJECT:
                return ((ExistingObject) selected).object;
            case EXIST_ENEMY:
                return ((ExistingEnemy) selected).enemy;
            case ENEMY:
                return ((EnemySelection) selected).enemy;
            case OBJECT:
                return ((ObjectSelection) selected).object;
            case PLAYER:
                return level.getPlayer();
            default:
                return null;
        }
    }

    /**
     * Update the status of this editor mode.
     * <p>
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        if (level == null) return;
        overlapped = false;

        // Determine if selected object is overlapping
        // I was forced to do this instead of contact listener since the scene objects are static
        GameObject selectedObject = getSelectedObject();

        if (selectedObject != null) {
            getAABB(selectedObject.getShapeInformation("body").fixture, lowerCache, upperCache);
            if (selectedObject.getType() == GameObject.ObjectType.SCENE) {
                if (!selectedObject.isSensor())
                    level.getWorld().QueryAABB(sceneCallback, lowerCache.x, lowerCache.y, upperCache.x, upperCache.y);
            } else {
                level.getWorld().QueryAABB(queryCallback, lowerCache.x, lowerCache.y, upperCache.x, upperCache.y);

                // Don't place werewolf or enemy outside board
                if (!board.inBoundsWorld(selectedObject.getX(), selectedObject.getY())) {
                    overlapped = true;
                }
            }
            // Change tint of object depending on overlapping
            if (overlapped) {
                selectedObject.setTint(OVERLAPPED_COLOR);
            } else {
                selectedObject.setTint(SELECTED_COLOR);
            }
        }
        changed = true;

        if (ImGui.getIO().getWantCaptureKeyboard()) return;
        // Zoom in and out with arrow keys
        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
            canvas.setZoom(canvas.getZoom() + 0.01f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
            canvas.setZoom(canvas.getZoom() - 0.01f);
        }

        // Move world with arrow keys
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            level.translateView(-20, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            level.translateView(20, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            level.translateView(0, -20);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            level.translateView(0, 20);
        }

    }

    /**
     * Draw the status of this editor mode.
     */
    private void draw(float delta) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        canvas.beginUI(GameCanvas.DrawPass.SPRITE);
        canvas.drawOverlay(background, Color.WHITE, true);
        canvas.end();

        if (showNewBoardWindow.get()) {
            // Center create window
            ImGui.setNextWindowPos(ImGui.getMainViewport().getCenterX() - 250, ImGui.getMainViewport().getCenterY() - 95);
            ImGui.setNextWindowSize(420, 150);

            createNewBoardWindow();
            ImGui.render();
            imGuiGl.renderDrawData(ImGui.getDrawData());
            // Draw nothing after for the create window
            return;
        }

        level.drawLevel(canvas);

        canvas.begin(GameCanvas.DrawPass.SHAPE, level.getView().x, level.getView().y);
        board.drawOutline(canvas);
        canvas.end();

        ImGui.begin("Selection");
        if (ImGui.beginTabBar("bruh")) {
            if (ImGui.beginTabItem("Tile")) {
                createTileMenu();
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem("Scene")) {
                createObjectMenu();
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem("Brush")) {
                createBrushSelection();
                ImGui.endTabItem();
            }

            ImGui.endTabBar();
        }
        ImGui.end();

        createSceneMenu();

        // Position window right below Selection
        ImGui.setNextWindowPos(ImGui.getWindowPosX(), ImGui.getWindowPosY() + 300, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(800, 175, ImGuiCond.FirstUseEver);
        createAmbientLightingMenu();

        createToolbar();
        if (showCannotSaveError) {
            cannotSaveWindow();
        }
        if (currEnemyControlled != null) {
            canvas.begin(GameCanvas.DrawPass.SHAPE, level.getView().x, level.getView().y);
            canvas.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            // Draw the path in pink, edges and vertices
            PatrolPath curPath = currEnemyControlled.getPatrolPath();
            for (int i = 0; i < curPath.getWaypointCount(); i++) {
                canvas.shapeRenderer.setColor(Color.PINK);
                Vector2 waypoint = curPath.getWaypointAtIndex(i);
                if (i < curPath.getWaypointCount() - 1) {
                    canvas.shapeRenderer.rectLine(
                            waypoint.x,
                            waypoint.y,
                            curPath.getWaypointAtIndex(i + 1).x,
                            curPath.getWaypointAtIndex(i + 1).y,
                            0.07f
                    );
                    if (i == selectedWaypointIndex)
                        canvas.shapeRenderer.setColor(Color.OLIVE);
                    canvas.shapeRenderer.circle(waypoint.x, waypoint.y, 0.14f, 15);
                }
                if (i == selectedWaypointIndex)
                    canvas.shapeRenderer.setColor(Color.OLIVE);
                canvas.shapeRenderer.circle(
                        waypoint.x,
                        waypoint.y,
                        0.14f, 15
                );
            }

            canvas.shapeRenderer.end();
            canvas.end();
        }

        //ImGui.showDemoWindow();

        ImGui.render();
        imGuiGl.renderDrawData(ImGui.getDrawData());
    }

    /**
     * Called when the screen should render itself.
     *
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        update(delta);
        draw(delta);
    }

    /**
     * Called when this screen becomes the current screen for a {@link com.badlogic.gdx.Game}.
     */
    @Override
    public void show() {
        boardSize = new int[]{10, 10};
        objectScale = new float[]{1};
        showNewBoardWindow.set(level == null);
        if (board != null)
            showCannotSaveError = false;

        showBattleLighting = false;
        showSaveLevelPopup = false;
        showOverwritePrompt = false;
        selectedWaypointIndex = -1;

        Gdx.input.setInputProcessor(this);

        doneActions = new Array<>();
        undoneActions = new Array<>();

        music.setLooping(true);
        music.play();
    }

    /**
     * Set the level explicitly.
     *
     * @param level level to set
     */
    public void setLevel(LevelContainer level) {
        this.level = level;
        board = level.getBoard();

        // Center board on screen
        level.setViewTranslation(
                -canvas.WorldToScreenX(board.boardToWorldX((float) board.getWidth() / 2)) + canvas.getWidth() / 2,
                -canvas.WorldToScreenY(board.boardToWorldY((float) board.getHeight() / 2)) + canvas.getHeight() / 2
        );

        stealthLighting = level.getStealthAmbience();
        battleLighting = level.getBattleAmbience();
        moonlightLighting = level.getMoonlightColor();

        playerPlaced = true;
        showNewBoardWindow.set(false);
    }

    /**
     * Called when this screen is no longer the current screen for a {@link Game}.
     */
    @Override
    public void hide() {
        // Reset zoom
        canvas.resize();
        music.stop();
    }

    /**
     * Called when this screen should release all resources.
     */
    @Override
    public void dispose() {
        canvas.dispose();
        imGuiGlfw.dispose();
        imGuiGl.dispose();
    }

    /**
     * Called when the screen was touched or a mouse button was pressed. The button parameter will be {@link Buttons#LEFT} on iOS.
     *
     * @param screenX The x coordinate, origin is in the upper left corner.
     * @param screenY The y coordinate, origin is in the upper left corner.
     * @param pointer the pointer for the event.
     * @param button  the button
     * @return whether the input was processed
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (level == null) return false;
        if (ImGui.getIO().getWantCaptureMouse()) return false;
        int boardX = board.worldToBoardX(mouseWorld.x);
        int boardY = board.worldToBoardY(mouseWorld.y);

        if (!mouseBoard.epsilonEquals(boardX, boardY)) {
            // mouse is on different tile now
            mouseBoard.set(boardX, boardY);
        }
        if (selected == null
                || selected.getType() == Selected.Type.EXIST_OBJECT
                || selected.getType() == Selected.Type.EXIST_ENEMY
        ) {

            if (currEnemyControlled != null) {
                // Check if a control point in enemy path was selected
                float selectionThreshold = 0.14f;

                for (int i = 0; i < currEnemyControlled.getPatrolPath().getWaypointCount(); i++) {
                    Vector2 waypoint = currEnemyControlled.getPatrolPath().getWaypointAtIndex(i);
                    if (mouseWorld.dst(waypoint) <= selectionThreshold) {
                        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                            currEnemyControlled.getPatrolPath().removeWaypoint(i);
                            // Perform action
                            doneActions.add(new RemovePoint(waypoint, i, currEnemyControlled));
                            undoneActions.clear();
                            return true;
                        }
                        selectedWaypointIndex = i;
                        return true;
                    }
                }
                // de-select
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    PatrolPath patrolPath = currEnemyControlled.getPatrolPath();
                    patrolPath.addWaypoint(
                            MathUtils.clamp(mouseWorld.x, 0, board.getWorldWidth()),
                            MathUtils.clamp(mouseWorld.y, 0, board.getWorldHeight())
                    );
                    // Perform action
                    doneActions.add(new AddPoint(patrolPath.getWaypointAtIndex(patrolPath.getWaypointCount() - 1), currEnemyControlled));
                    undoneActions.clear();
                    return true;
                }
                selectedWaypointIndex = -1;
            }

            removeSelection();
            // select object
            level.getWorld().QueryAABB(
                    selectCallback,
                    mouseWorld.x - 0.05f,
                    mouseWorld.y - 0.05f,
                    mouseWorld.x + 0.05f,
                    mouseWorld.y + 0.05f
            );

            return true;
        }

        placeSelection();
        return true;
    }

    /**
     * Called when a finger or the mouse was dragged.
     *
     * @param screenX
     * @param screenY
     * @param pointer the pointer for the event.
     * @return whether the input was processed
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (level == null) return false;
        mouseWorld.set(canvas.ScreenToWorldX(Gdx.input.getX(), level.getView()), canvas.ScreenToWorldY(Gdx.input.getY(), level.getView()));
        if (ImGui.getIO().getWantCaptureMouse()) return false;

        if (selectedWaypointIndex != -1 && currEnemyControlled != null) {
            currEnemyControlled.getPatrolPath().getWaypointAtIndex(selectedWaypointIndex).set(
                    MathUtils.clamp(mouseWorld.x, 0, board.getWorldWidth()),
                    MathUtils.clamp(mouseWorld.y, 0, board.getWorldHeight())
            );
            return true;
        }

        if (selected == null) {
            return false;
        }

        switch (selected.getType()) {
            case EXIST_OBJECT:
                ((ExistingObject) selected).object.setPosition(mouseWorld.x, mouseWorld.y);
                break;
            case EXIST_ENEMY:
                ((ExistingEnemy) selected).enemy.setPosition(mouseWorld.x, mouseWorld.y);
                break;
            case TILE:
                int boardX = board.worldToBoardX(mouseWorld.x);
                int boardY = board.worldToBoardY(mouseWorld.y);

                if (!mouseBoard.epsilonEquals(boardX, boardY) || !board.hasPreview()) {
                    // mouse is on different tile now
                    mouseBoard.set(boardX, boardY);
                    placeTile();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. Will not be called on iOS.
     *
     * @param screenX
     * @param screenY
     * @return whether the input was processed
     */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (level == null) return false;
        mouseWorld.set(canvas.ScreenToWorldX(Gdx.input.getX(), level.getView()), canvas.ScreenToWorldY(Gdx.input.getY(), level.getView()));

        if (ImGui.getIO().getWantCaptureMouse()) return false;
        int boardX = board.worldToBoardX(mouseWorld.x);
        int boardY = board.worldToBoardY(mouseWorld.y);

        if (selected == null) {
            return true;
        }

        switch (selected.getType()) {
            case TILE:
                // snap to tile
                if (!board.inBoundsWorld(mouseWorld.x, mouseWorld.y)) {
                    board.removePreview();
                    break;
                }
                if (!mouseBoard.epsilonEquals(boardX, boardY) || !board.hasPreview()) {
                    // mouse is on different tile now
                    mouseBoard.set(boardX, boardY);
                    board.setPreviewTile((int) mouseBoard.x, (int) mouseBoard.y, ((Tile) selected).num);
                }
                break;
            case PLAYER:
                // dont update position if placed
                if (playerPlaced) break;
                level.getPlayer().setPosition(mouseWorld);
                break;
            case ENEMY:
                ((EnemySelection) selected).enemy.setPosition(mouseWorld);
                break;
            case OBJECT:
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    mouseBoard.set(boardX, boardY);
                    ((ObjectSelection) selected).object.setPosition(mouseBoard);
                } else
                    ((ObjectSelection) selected).object.setPosition(mouseWorld);
                break;
        }

        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. Will not be called on iOS.
     *
     * @param amountX the horizontal scroll amount, negative or positive depending on the direction the wheel was scrolled.
     * @param amountY the vertical scroll amount, negative or positive depending on the direction the wheel was scrolled.
     * @return whether the input was processed.
     */
    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (level == null) return false;
        if (ImGui.getIO().getWantCaptureMouse() || selected == null) return false;
        if (selected.getType() == Selected.Type.OBJECT) {
            // Update the scale to the selected object
            float delta = 0.02f * amountY;
            objectScale[0] += delta;
            ((ObjectSelection) selected).object.setScale(objectScale[0]);
        }
        if (selected.getType() == Selected.Type.EXIST_OBJECT) {
            // Update the scale to the selected object
            float delta = 0.02f * amountY;
            objectScale[0] += delta;
            ((ExistingObject) selected).object.setScale(objectScale[0]);
        }
        return false;
    }

    /**
     * Called when a key was pressed
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed
     */
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.Q) {
            observer.exitScreen(this, GO_MENU);
        }

        if (level == null) return false;
        if (ImGui.getIO().getWantCaptureKeyboard()) return false;
        if (keycode == Input.Keys.ESCAPE) {
            removeSelection();
        }

        if (keycode == Input.Keys.F) {
            if (selected.getType() == Selected.Type.OBJECT) {
                SceneObject obj = ((ObjectSelection) selected).object;
                obj.setFlipped(!obj.isFlipped());
            }
            if (selected.getType() == Selected.Type.EXIST_OBJECT) {
                SceneObject obj = ((ExistingObject) selected).object;
                obj.setFlipped(!obj.isFlipped());
            }
        }

        if (!changed) {
            return true;
        }

        // C-Z
        if (keycode == Input.Keys.Z) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                undo();
                changed = false;
            }
        }
        if (keycode == Input.Keys.CONTROL_LEFT) {
            if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
                undo();
                changed = false;
            }
        }

        // C-R
        if (keycode == Input.Keys.R) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                redo();
                changed = false;
            }
        }
        if (keycode == Input.Keys.CONTROL_LEFT) {
            if (Gdx.input.isKeyPressed(Input.Keys.R)) {
                redo();
                changed = false;
            }
        }

        // C-S
        if (keycode == Input.Keys.S) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                showSaveLevelPopup = true;
                changed = false;
            }
        }
        if (keycode == Input.Keys.CONTROL_LEFT) {
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                showSaveLevelPopup = true;
                changed = false;
            }
        }

        return true;
    }

    // IMGUI FUNCTIONS

    /**
     * Creates the ImGui "Scene" menu containing dropdowns for enemies and objects in the level.
     * <p>
     * The enemies dropdown lists all enemies, allowing the user to modify their position, scale,
     * and remove them from the level. The objects dropdown lists all scene objects, providing options
     * to modify their position, scale, visibility, and remove them from the level.
     * <p>
     * This function also handles the hover effect on enemies and objects, changing their tint color
     * when hovered over in the ImGui menu.
     */
    private void createSceneMenu() {
        float windowWidth = 300;
        // Set the window position and size
        ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() - windowWidth - 50, 50, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(windowWidth, 550, ImGuiCond.FirstUseEver);

        if (ImGui.begin("Scene")) {
            GameObject selectedObject = getSelectedObject();

            if (selectedObject != null && selectedObject.getType() == GameObject.ObjectType.ENEMY)
                ImGui.setNextItemOpen(true);

            // Enemies dropdown
            if (ImGui.collapsingHeader("Enemies")) {
                int i = 0;
                for (Enemy enemy : level.getEnemies()) {
                    ImGui.pushID(i);

                    if (enemy == selectedObject) {
                        ImGui.setNextItemOpen(true, ImGuiCond.Always);
                        ImGui.pushStyleColor(ImGuiCol.Text, 0.2f, 0.6f, 1.0f, 1.0f);
                    }
                    if (ImGui.treeNode("Enemy " + (i + 1))) {
                        if (enemy == selectedObject) {
                            ImGui.setScrollHereY();
                        } else {
                            if (ImGui.isItemHovered()) {
                                enemy.setTint(SELECTED_COLOR);
                            } else {
                                enemy.setTint(Color.WHITE);
                            }
                        }
                        // Position
                        float[] posX = new float[]{enemy.getX()};
                        float[] posY = new float[]{enemy.getY()};
                        ImGui.dragFloat("X", posX, 0.05f, 0, board.getWorldWidth());
                        ImGui.dragFloat("Y", posY, 0.05f, 0, board.getWorldHeight());
                        enemy.setPosition(posX[0], posY[0]);

                        // Scale
                        float[] scale = new float[]{enemy.getScale()};
                        ImGui.dragFloat("Scale", scale, 0.01f);
                        enemy.setScale(scale[0]);

                        // Remove button
                        if (ImGui.button("Remove")) {
                            if (selected != null && selected.getType() != Selected.Type.ENEMY) {
                                // Do not want to remove the same enemy twice
                                // As this will mess with the free list
                                removeSelection();
                            }
                            level.removeEnemy(enemy);
                            doneActions.add(new RemoveObject(enemy));
                            undoneActions.clear();
                            selected = null;
                            // Remove the controller window if it's controlling the removed enemy
                            if (currEnemyControlled == enemy) {
                                currEnemyControlled = null;
                                selectedWaypointIndex = -1;
                            }
                        }

                        ImGui.treePop();
                    } else {
                        if (ImGui.isItemHovered()) {
                            enemy.setTint(SELECTED_COLOR);
                        } else {
                            enemy.setTint(Color.WHITE);
                        }
                    }

                    if (enemy == selectedObject) {
                        ImGui.popStyleColor();
                    }

                    ImGui.popID();
                    i++;
                }
            }

            if (selectedObject != null && selectedObject.getType() == GameObject.ObjectType.SCENE)
                ImGui.setNextItemOpen(true);

            // Objects dropdown
            if (ImGui.collapsingHeader("Objects")) {
                int i = 0;
                for (SceneObject object : level.getSceneObjects()) {
                    ImGui.pushID(i);
                    if (object == selectedObject) {
                        ImGui.setNextItemOpen(true, ImGuiCond.Always);
                        ImGui.pushStyleColor(ImGuiCol.Text, 0.2f, 0.6f, 1.0f, 1.0f);
                    }
                    if (ImGui.treeNode("Object " + (i + 1) + "   ")) {
                        if (object == selectedObject) {
                            ImGui.setScrollHereY();
                        } else {
                            // Selected object should always be tinted with SELECTED_COLOR
                            if (ImGui.isItemHovered()) {
                                object.setTint(SELECTED_COLOR);
                            } else {
                                object.setTint(Color.WHITE);
                            }
                        }
                        // Position
                        float[] posX = new float[]{object.getX()};
                        float[] posY = new float[]{object.getY()};
                        ImGui.dragFloat("X", posX, 0.05f);
                        ImGui.dragFloat("Y", posY, 0.05f);
                        object.setPosition(posX[0], posY[0]);

                        // Scale
                        float[] scale = new float[]{object.getScale()};
                        ImGui.dragFloat("Scale", scale, 0.01f);
                        object.setScale(scale[0]);

                        ImBoolean flip = new ImBoolean(object.isFlipped());
                        if (ImGui.checkbox("Flip", flip)) {
                            object.setFlipped(flip.get());
                        }

                        // Remove button
                        if (ImGui.button("Remove")) {
                            removeSelection();
                            level.removeSceneObject(object);
                            doneActions.add(new RemoveObject(object));
                            undoneActions.clear();
                        }

                        ImGui.treePop();
                    } else {
                        if (ImGui.isItemHovered()) {
                            object.setTint(SELECTED_COLOR);
                        } else {
                            object.setTint(Color.WHITE);
                        }
                    }
                    if (object == selectedObject) {
                        ImGui.popStyleColor();
                    }

                    ImGui.sameLine();

                    // Visibility
                    ImBoolean visible = new ImBoolean(!object.isDestroyed());
                    if (ImGui.checkbox("Visible", visible)) {
                        if (!visible.get())
                            object.setDestroyed(true);
                        else {
                            level.addDrawables(object);
                            object.setDestroyed(false);
                        }
                    }

                    ImGui.popID();
                    i++;
                }
            }

            ImGui.end();
        }
    }

    /**
     * Popup window for tile selection using ImGui
     */
    private void createTileMenu() {
        FilmStrip tiles = board.getTileSheet();

        // Draw every frame in the tile sheet. Note UV's must be considered as the texture
        //  remains unchanged
        for (int i = 0; i < tiles.getSize(); i++) {
            tiles.setFrame(i);
            // to avoid ID conflicts
            ImGui.pushID(i);
            if (
                    ImGui.imageButton(
                            tiles.getTexture().getTextureObjectHandle(),
                            100, 100 * 3 / 4,
                            tiles.getU(), tiles.getV(),
                            tiles.getU2(), tiles.getV2()
                    )
            ) {
                removeSelection();
                selected = new Tile(i);
            }
            ImGui.popID();

            // Make tile menu have 6 columns
            if ((i + 1) % 6 == 0) {
                ImGui.newLine();
            } else {
                ImGui.sameLine();
            }

        }
    }

    /**
     * Popup window for object selection using ImGui
     */
    private void createObjectMenu() {
        float windowWidth = ImGui.getWindowWidth();
        float rowWidth = 0;

        for (SceneButton obj : objectSelections) {
            // Scale if window height scales
            int height = Math.max(100, (int) ImGui.getWindowHeight() / 4);
            int width = height * obj.texture.getWidth() / obj.texture.getHeight();
            float totalButtonWidth = width + 20;

            // Check if the button fits within the remaining space on the current row
            // If it doesnt, put it on a new line
            if (rowWidth + totalButtonWidth > windowWidth) {
                ImGui.newLine();
                rowWidth = 0;
            }

            if (rowWidth > 0) {
                ImGui.sameLine();
            }

            if (ImGui.imageButton(obj.texture.getTextureObjectHandle(), width, height)) {
                removeSelection();
                SceneObject newObject = level.addSceneObject(obj.type, mouseWorld.x, mouseWorld.y, 1, false);
                selected = new ObjectSelection(newObject);
                objectScale[0] = 1;
                newObject.setTint(SELECTED_COLOR);
            }

            rowWidth += totalButtonWidth;
        }

        ImGui.spacing();

        // Create a slider for the scale of the object
        if (ImGui.sliderFloat("Object Scale", objectScale, 0.1f, 5.0f)) {
            // Apply the scale to the selected object when the slider is moved
            if (selected != null && selected.getType() == Selected.Type.OBJECT) {
                SceneObject selectedObj = ((ObjectSelection) selected).object;
                selectedObj.setScale(objectScale[0]);
            }
        }
    }

    /**
     * Toolbar at top of window with File, Edit, Enemy, View, Settings, and Controls menu
     */
    private void createToolbar() {
        if (ImGui.beginMainMenuBar()) {
            createFileMenu();
            createEditMenu();
            createViewMenu();
            createSettingsMenu();
            createControlsMenu();

            ImGui.endMainMenuBar();
        }

        drawSaveLevelPopup();
        drawOverwritePromptPopup();
    }

    private void createSettingsMenu() {
        if (ImGui.beginMenu("   Settings   ")) {
            drawSettingsWindow();
            ImGui.endMenu();
        }
    }

    /**
     * Menu holding window containing editor controls
     */
    private void createControlsMenu() {
        if (ImGui.beginMenu("   Controls   ")) {

            if (ImGui.beginTable("table", 2)) {

                ImGui.tableNextColumn();
                ImGui.textColored(1.0f, 0.0f, 0.0f, 1.0f, "Key");
                ImGui.tableNextColumn();
                ImGui.textColored(1.0f, 0.0f, 0.0f, 1.0f, "Action");

                ImGui.tableNextColumn();
                ImGui.text("Arrow keys");
                ImGui.tableNextColumn();
                ImGui.text("Move scene");

                ImGui.tableNextColumn();
                ImGui.text("-");
                ImGui.tableNextColumn();
                ImGui.text("Zoom out");

                ImGui.tableNextColumn();
                ImGui.text("=");
                ImGui.tableNextColumn();
                ImGui.text("Zoom in");

                ImGui.tableNextColumn();
                ImGui.text("C-Z");
                ImGui.tableNextColumn();
                ImGui.text("Undo");

                ImGui.tableNextColumn();
                ImGui.text("C-R");
                ImGui.tableNextColumn();
                ImGui.text("Redo");

                ImGui.tableNextColumn();
                ImGui.text("C-S");
                ImGui.tableNextColumn();
                ImGui.text("Save");

                ImGui.tableNextColumn();
                ImGui.text("Mouse scroll");
                ImGui.tableNextColumn();
                ImGui.text("Scale scene selection");

                ImGui.tableNextColumn();
                ImGui.text("F");
                ImGui.tableNextColumn();
                ImGui.text("Flip scene selection");

                ImGui.tableNextColumn();
                ImGui.text("Left-Ctrl");
                ImGui.tableNextColumn();
                ImGui.text("Snap scene selection to board");

                ImGui.tableNextColumn();
                ImGui.text("Ctrl-Click");
                ImGui.tableNextColumn();
                ImGui.text("Add/Remove patrol waypoints");

                ImGui.tableNextColumn();
                ImGui.text("Escape");
                ImGui.tableNextColumn();
                ImGui.text("Stop selecting item");


                ImGui.tableNextColumn();
                ImGui.text("Q");
                ImGui.tableNextColumn();
                ImGui.text("Quit");

                ImGui.endTable();
            }
            ImGui.endMenu();
        }
    }

    /**
     * Draws the "Save Level" popup, which prompts the user to input a level number (1-15).
     * When the "Save" button is clicked, it attempts to save the level using the saveBoardToJsonFile function.
     * If the save is unsuccessful, the "Overwrite Prompt" popup is shown.
     */
    private void drawSaveLevelPopup() {
        if (showSaveLevelPopup) {
            ImGui.openPopup("Save Level");
        }
        if (ImGui.beginPopup("Save Level")) {
            ImGui.text("Enter the level to save to (1-15):");
            if (ImGui.inputInt("Level", saveTo)) {
                saveTo.set(MathUtils.clamp(saveTo.get(), 1, 15));
            }

            if (ImGui.button("Save")) {
                boolean success = saveLevel(level, directory, saveTo.get(), false);
                if (!success) {
                    showOverwritePrompt = true;
                }
                showSaveLevelPopup = false;
                ImGui.closeCurrentPopup();
            }
            ImGui.sameLine();
            if (ImGui.button("Cancel")) {
                showSaveLevelPopup = false;
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    /**
     * Draws the "Overwrite Prompt" popup, which is shown when a level already exists and the user tries to save over it.
     * The user can choose to overwrite the existing level or cancel the operation.
     */
    private void drawOverwritePromptPopup() {
        if (showOverwritePrompt) {
            ImGui.openPopup("Overwrite Level?");
        }

        if (ImGui.beginPopup("Overwrite Level?")) {
            ImGui.text("Level already exists! Overwrite?");
            if (ImGui.button("Yes")) {
                removeSelection();
                saveLevel(level, directory, saveTo.get(), true);
                showOverwritePrompt = false;
                ImGui.closeCurrentPopup();
            }
            ImGui.sameLine();
            if (ImGui.button("No")) {
                showOverwritePrompt = false;
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }


    /**
     * File menu with New, Save, Test and Quit items
     */
    private void createFileMenu() {
        if (ImGui.beginMenu("   File   ")) {
            if (ImGui.menuItem("New")) {
                showNewBoardWindow.set(true);
            }
            ImGui.spacing();
            ImGui.spacing();

            if (ImGui.menuItem("Save")) {
                if (canSave()) {
                    // Cant just open popup due to this issue
                    // https://github.com/ocornut/imgui/issues/331
                    showSaveLevelPopup = true;
                } else {
                    // Cannot save!
                    showCannotSaveError = true;
                }
            }
            ImGui.spacing();
            ImGui.spacing();
            if (ImGui.menuItem("Test")) {
                if (canSave()) {
                    removeSelection();
                    saveLevel(level, directory, 0, true);
                    observer.exitScreen(this, GO_PLAY);
                } else {
                    // Cannot save!
                    showCannotSaveError = true;
                }
            }

            ImGui.spacing();
            ImGui.spacing();

            if (ImGui.menuItem("Quit")) {
                observer.exitScreen(this, GO_MENU);
            }
            ImGui.endMenu();
        }
    }

    /**
     * Display edit menu with Undo/Redo items and functionality
     */
    private void createEditMenu() {
        if (ImGui.beginMenu("   Edit   ")) {
            if (ImGui.menuItem("Undo")) {
                undo();
            }
            ImGui.spacing();
            ImGui.spacing();
            if (ImGui.menuItem("Redo")) {
                redo();
            }
            ImGui.endMenu();
        }
    }


    /**
     * View menu with Toggle Stealth / Battle Lighting
     */
    private void createViewMenu() {
        if (ImGui.beginMenu("   View   ")) {
            if (ImGui.menuItem("Toggle Stealth / Battle Lighting")) {
                showBattleLighting = !showBattleLighting;

                // Change lighting
                if (showBattleLighting)
                    level.getRayHandler().setAmbientLight(battleLighting[0], battleLighting[1], battleLighting[2], battleLighting[3]);
                else
                    level.getRayHandler().setAmbientLight(stealthLighting[0], stealthLighting[1], stealthLighting[2], stealthLighting[3]);
            }
            ImGui.endMenu();
        }
    }

    /**
     * Popup window for creating new board and setting board size
     */
    private void createNewBoardWindow() {
        ImGui.begin("New Board", showNewBoardWindow);

        if (!showNewBoardWindow.get()) {
            // Must've pressed X
            if (level == null)
                observer.exitScreen(this, GO_MENU);
        }

        ImGui.text("Enter board size (width and height):");
        ImGui.inputInt2("Size", boardSize);

        ImGui.spacing();

        if (ImGui.button("Create")) {
            level = LevelParser.LevelParser().loadEmpty(boardSize[0], boardSize[1]);

            level.hidePlayer();
            board = level.getBoard();

            // Center board on screen
            level.setViewTranslation(
                    -canvas.WorldToScreenX(board.boardToWorldX((float) board.getWidth() / 2)) + canvas.getWidth() / 2,
                    -canvas.WorldToScreenY(board.boardToWorldY((float) board.getHeight() / 2)) + canvas.getHeight() / 2
            );

            stealthLighting = new float[]{0.7f, 0.7f, 0.7f, 1};
            level.getRayHandler().setAmbientLight(stealthLighting[0], stealthLighting[1], stealthLighting[2], stealthLighting[3]);

            battleLighting = new float[]{1, 1, 1, 1};
            moonlightLighting = new float[]{1, 1, 1, 0.2f};
            // Only need to call once since level will hold a reference
            level.setBattleAmbience(battleLighting);
            level.setStealthAmbience(stealthLighting);
            level.setMoonlightColor(moonlightLighting);

            playerPlaced = false;

            showNewBoardWindow.set(false);
        }

        ImGui.end();
    }

    private void cannotSaveWindow() {
        ImGui.begin("Error", new ImBoolean(true));

        ImGui.text("You cannot save.");
        ImGui.spacing();
        ImGui.text("Make sure all tiles are set, the player is placed, and there is at least one moonlight tile!");

        if (ImGui.button("Ok")) {
            showCannotSaveError = false;
        }

        ImGui.end();
    }

    // Used in drawSettings to keep one tree node open at a time
    private int nodeToClose, currentNode = -1;

    /**
     * Draws the ImGui window for the enemy settings editor.
     * Allows the user to modify enemy spawn settings, such as phase length, enemy count, spawn rate, and delay.
     */
    private void drawSettingsWindow() {
        ImGui.text("Spawn Locations:");

        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.text("These are the locations that the enemies spawn in during the battle phase");
            ImGui.endTooltip();
        }

        for (int i = 0; i < level.getSettings().getSpawnLocations().size; i++) {
            Vector2 spawnLocation = level.getSettings().getSpawnLocations().get(i);

            ImGui.pushID(i);

            // Close the node if needed
            if (nodeToClose == i) {
                ImGui.setNextItemOpen(false, ImGuiCond.Always);
                nodeToClose = -1;
            }
            if (ImGui.treeNode("Location " + (i + 1))) {
                if (currentNode == i) {

                    // Draw spawn location outline on board
                    canvas.begin(GameCanvas.DrawPass.SHAPE, level.getView().x, level.getView().y);
                    canvas.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                    canvas.shapeRenderer.setColor(Color.WHITE);
                    canvas.shapeRenderer.rect(
                            spawnLocation.x - 0.5f, spawnLocation.y - 0.2f,
                            0.5f, 0.2f
                    );
                    canvas.shapeRenderer.end();
                    canvas.end();

                    // 2D window widget for position editing
                    ImGui.text("Position Editor:");
                    ImGui.spacing();


                    float height = 300;
                    float width = (float) board.getWidth() / board.getHeight() * height;
                    ImGui.beginChild("Position Editor (Click and drag to move location)", width, height, false);
                    // Invisible button for input handling
                    ImGui.invisibleButton("##positionButton", width, height);

                    float worldToWindowX = width / board.getWorldWidth();
                    float worldToWindowY = height / board.getWorldHeight();

                    // Draw spawn location outline
                    // Translate spawn world position to mouse coordinates
                    ImGui.getWindowDrawList().addRect(
                            ImGui.getItemRectMinX() + spawnLocation.x * worldToWindowX - 20,
                            ImGui.getItemRectMinY() + height - spawnLocation.y * worldToWindowY - 10,
                            ImGui.getItemRectMinX() + spawnLocation.x * worldToWindowX + 20,
                            ImGui.getItemRectMinY() + height - spawnLocation.y * worldToWindowY + 10,
                            ImGui.getColorU32(1, 1, 1, 1));

                    if (ImGui.isItemHovered() && ImGui.isMouseDown(ImGuiMouseButton.Left)) {
                        // Translate mouse position to world coordinates
                        ImVec2 mousePos = ImGui.getMousePos();
                        mousePos.x -= ImGui.getItemRectMinX();
                        mousePos.y = ImGui.getItemRectMaxY() - mousePos.y;
                        mousePos.times(1 / worldToWindowX, 1 / worldToWindowY);

                        spawnLocation.x = mousePos.x;
                        spawnLocation.y = mousePos.y;
                    }
                    ImGui.endChild();

                    // Draw border around the editor window
                    ImVec2 childWindowPos = new ImVec2();
                    ImGui.getItemRectMin(childWindowPos);
                    ImDrawList drawList = ImGui.getWindowDrawList();
                    ImVec2 contentRegionAvail = new ImVec2();
                    ImGui.getContentRegionAvail(contentRegionAvail);
                    drawList.addRect(
                            childWindowPos.x - 5,
                            childWindowPos.y - 5,
                            childWindowPos.x + width + 5,
                            childWindowPos.y + height + 5,
                            ImGui.getColorU32(1, 0.514f, 0.376f, 1),
                            0, ImDrawFlags.None, 10
                    );

                    ImGui.spacing();

                    ImGui.pushItemWidth(120);
                    ImGui.text("Edit Position Manually:");
                    float[] positionX = {spawnLocation.x};
                    if (ImGui.dragFloat("##positionX", positionX, 0.05f, 0, board.getWorldWidth(), "X: %.01f")) {
                        spawnLocation.x = positionX[0];
                    }

                    ImGui.sameLine();
                    float[] positionY = {spawnLocation.y};
                    if (ImGui.dragFloat("##positionY", positionY, 0.05f, 0, board.getWorldHeight(), "Y: %.01f")) {
                        spawnLocation.y = positionY[0];
                    }
                    ImGui.popItemWidth();

                    if (ImGui.button("Remove Location")) {
                        level.getSettings().removeSpawnLocation(i);
                    }
                } else {
                    nodeToClose = currentNode;
                    currentNode = i;
                }

                ImGui.treePop();
            }
            ImGui.popID();
        }

        if (ImGui.button("Add Location")) {
            level.getSettings().addSpawnLocation(0, 0);
        }

        ImGui.spacing();

        ImGui.text("Length of Stealth Phase (seconds):");
        if (ImGui.inputInt("##phaseLength", level.getSettings().phaseLength)) {
            level.getSettings().phaseLength.set(Math.max(0, level.getSettings().phaseLength.get()));
        }

        ImGui.text("Enemy Count:");

        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.text("The total number of new enemies that will spawn during the battle phase");
            ImGui.endTooltip();
        }

        if (ImGui.inputInt("##enemyCount", level.getSettings().enemyCount)) {
            level.getSettings().enemyCount.set(Math.max(0, level.getSettings().enemyCount.get()));
        }

        ImGui.text("Spawn Rate (seconds):");

        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.text("The spawner will spawn an enemy, then waits for a random time between Min to Max seconds before spawning a new one");
            ImGui.endTooltip();
        }

        ImGui.dragFloat("Min##spawnRateMin", level.getSettings().spawnRateMin.getData(), 0.01f, 0, 100);
        ImGui.dragFloat("Max##spawnRateMax", level.getSettings().spawnRateMax.getData(), 0.01f, 0, 100);

        level.getSettings().spawnRateMin.set(
                Math.min(
                        level.getSettings().spawnRateMin.get(),
                        level.getSettings().spawnRateMax.get()
                )
        );

        ImGui.text("Delay (seconds):");

        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.text("The number of seconds after the battle phase begins to start spawning the enemies");
            ImGui.endTooltip();
        }

        if (ImGui.inputInt("##Delay", level.getSettings().delay)) {
            level.getSettings().delay.set(Math.max(0, level.getSettings().delay.get()));
        }
    }

    /**
     * Brush selection window for placing moonlight, werewolf, enemy
     */
    private void createBrushSelection() {
        if (ImGui.button("Moonlight")) {
            removeSelection();
            selected = new Moonlight();
        }

        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.text("Hold Left-Control to remove");
            ImGui.endTooltip();
        }

        ImGui.sameLine();

        FilmStrip playerTex = level.getPlayer().getTexture();
        if (ImGui.imageButton(
                playerTex.getTexture().getTextureObjectHandle(),
                100, 100 * playerTex.getRegionHeight() / playerTex.getRegionWidth()
        )) {
            removeSelection();
            selected = new Player();
            level.getPlayer().setPosition(mouseWorld);
            if (!playerPlaced)
                level.showPlayer();
            playerPlaced = false;

            level.getPlayer().setTint(SELECTED_COLOR);
        }

        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.text("Werewolf");
            ImGui.endTooltip();
        }

        for (EnemyButton enemy : enemySelections) {
            ImGui.sameLine();
            if (ImGui.imageButton(
                    enemy.texture.getTextureObjectHandle(),
                    100, 100 * enemy.texture.getHeight() / enemy.texture.getWidth()
            )) {
                Enemy newEnemy = level.addEnemy(
                        enemy.type,
                        mouseWorld.x, mouseWorld.y,
                        new PatrolPath()
                );
                removeSelection();
                selected = new EnemySelection(newEnemy);

                newEnemy.setTint(SELECTED_COLOR);
            }

            if (ImGui.isItemHovered()) {
                ImGui.beginTooltip();
                String name = enemy.type.toString().toLowerCase();
                ImGui.text(name.substring(0, 1).toUpperCase() + name.substring(1));
                ImGui.endTooltip();
            }
        }
    }

    /**
     * Controller for ambient lighting json values
     */
    private void createAmbientLightingMenu() {
        ImGui.begin("Lighting");
        if (ImGui.colorEdit4("Stealth Phase Lighting", stealthLighting)) {
            if (!showBattleLighting)
                level.getRayHandler().setAmbientLight(stealthLighting[0], stealthLighting[1], stealthLighting[2], stealthLighting[3]);
        }
        ImGui.spacing();

        if (ImGui.colorEdit4("Battle Phase Lighting", battleLighting)) {
            if (showBattleLighting)
                level.getRayHandler().setAmbientLight(battleLighting[0], battleLighting[1], battleLighting[2], battleLighting[3]);
        }
        ImGui.spacing();

        if (ImGui.colorEdit4("Moonlight Lighting", moonlightLighting)) {
            for (PointLight light : board.getPointLights()) {
                light.setColor(moonlightLighting[0], moonlightLighting[1], moonlightLighting[2], moonlightLighting[3]);
            }
        }

        ImGui.spacing();

        ImGui.end();
    }

    /**
     * ImGui initialization. Call after {@link #gatherAssets(AssetDirectory)}.
     */
    public void setupImGui() {
        // ImGui initialization
        this.imGuiGlfw = new ImGuiImplGlfw();
        this.imGuiGl = new ImGuiImplGLES2();

        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);

        // Set up the font
        FileHandle fontFileHandle = Gdx.files.internal("shared/LibreBaskerville-Regular.ttf");
        byte[] fontData = fontFileHandle.readBytes();
        ImFont imFont = io.getFonts().addFontFromMemoryTTF(fontData, 20);
        io.setFontDefault(imFont);
        io.getFonts().build();

        // Set up the style
        ImGuiStyle style = ImGui.getStyle();
        style.setColor(ImGuiCol.TextDisabled, 0.50f, 0.50f, 0.50f, 1.00f);
        style.setColor(ImGuiCol.WindowBg, 0.10f, 0.10f, 0.10f, 1.00f);
        style.setColor(ImGuiCol.ChildBg, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.PopupBg, 0.19f, 0.19f, 0.19f, 0.92f);
        style.setColor(ImGuiCol.Border, 0.19f, 0.19f, 0.19f, 0.29f);
        style.setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.24f);
        style.setColor(ImGuiCol.FrameBg, 0.05f, 0.05f, 0.05f, 0.54f);
        style.setColor(ImGuiCol.FrameBgHovered, 0.19f, 0.19f, 0.19f, 0.54f);
        style.setColor(ImGuiCol.FrameBgActive, 0.20f, 0.22f, 0.23f, 1.00f);
        style.setColor(ImGuiCol.TitleBg, 0.00f, 0.00f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.TitleBgActive, 0.06f, 0.06f, 0.06f, 1.00f);
        style.setColor(ImGuiCol.TitleBgCollapsed, 0.00f, 0.00f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.MenuBarBg, 0.14f, 0.14f, 0.14f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarBg, 0.05f, 0.05f, 0.05f, 0.54f);
        style.setColor(ImGuiCol.ScrollbarGrab, 0.34f, 0.34f, 0.34f, 0.54f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, 0.40f, 0.40f, 0.40f, 0.54f);
        style.setColor(ImGuiCol.ScrollbarGrabActive, 0.56f, 0.56f, 0.56f, 0.54f);
        style.setColor(ImGuiCol.CheckMark, 0.33f, 0.67f, 0.86f, 1.00f);
        style.setColor(ImGuiCol.SliderGrab, 0.34f, 0.34f, 0.34f, 0.54f);
        style.setColor(ImGuiCol.SliderGrabActive, 0.56f, 0.56f, 0.56f, 0.54f);
        style.setColor(ImGuiCol.Button, 0.05f, 0.05f, 0.05f, 0.54f);
        style.setColor(ImGuiCol.ButtonHovered, 0.30f, 0.32f, 0.33f, 0.92f);
        style.setColor(ImGuiCol.ButtonActive, 0.20f, 0.22f, 0.23f, 1.00f);
        style.setColor(ImGuiCol.Header, 0.00f, 0.00f, 0.00f, 0.52f);
        style.setColor(ImGuiCol.HeaderHovered, 0.00f, 0.00f, 0.00f, 0.36f);
        style.setColor(ImGuiCol.HeaderActive, 0.20f, 0.22f, 0.23f, 0.33f);
        style.setColor(ImGuiCol.Separator, 0.28f, 0.28f, 0.28f, 0.29f);
        style.setColor(ImGuiCol.SeparatorHovered, 0.44f, 0.44f, 0.44f, 0.29f);
        style.setColor(ImGuiCol.SeparatorActive, 0.40f, 0.44f, 0.47f, 1.00f);
        style.setColor(ImGuiCol.ResizeGrip, 0.28f, 0.28f, 0.28f, 0.29f);
        style.setColor(ImGuiCol.ResizeGripHovered, 0.44f, 0.44f, 0.44f, 0.29f);
        style.setColor(ImGuiCol.ResizeGripActive, 0.40f, 0.44f, 0.47f, 1.00f);
        style.setColor(ImGuiCol.Tab, 0.00f, 0.00f, 0.00f, 0.52f);
        style.setColor(ImGuiCol.TabHovered, 0.14f, 0.14f, 0.14f, 1.00f);
        style.setColor(ImGuiCol.TabActive, 0.20f, 0.20f, 0.20f, 0.36f);
        style.setColor(ImGuiCol.TabUnfocused, 0.00f, 0.00f, 0.00f, 0.52f);
        style.setColor(ImGuiCol.TabUnfocusedActive, 0.14f, 0.14f, 0.14f, 1.00f);
        style.setColor(ImGuiCol.DockingPreview, 0.33f, 0.67f, 0.86f, 1.00f);
        style.setColor(ImGuiCol.DockingEmptyBg, 1.00f, 0.00f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.PlotLines, 1.00f, 0.00f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.PlotLinesHovered, 1.00f, 0.00f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.PlotHistogram, 1.00f, 0.00f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.PlotHistogramHovered, 1.00f, 0.00f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.TableHeaderBg, 0.00f, 0.00f, 0.00f, 0.52f);
        style.setColor(ImGuiCol.TableBorderStrong, 0.00f, 0.00f, 0.00f, 0.52f);
        style.setColor(ImGuiCol.TableBorderLight, 0.28f, 0.28f, 0.28f, 0.29f);
        style.setColor(ImGuiCol.TableRowBg, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.TableRowBgAlt, 1.00f, 1.00f, 1.00f, 0.06f);
        style.setColor(ImGuiCol.TextSelectedBg, 0.20f, 0.22f, 0.23f, 1.00f);
        style.setColor(ImGuiCol.DragDropTarget, 0.33f, 0.67f, 0.86f, 1.00f);
        style.setColor(ImGuiCol.NavHighlight, 1.00f, 0.00f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.NavWindowingHighlight, 1.00f, 0.00f, 0.00f, 0.70f);
        style.setColor(ImGuiCol.NavWindowingDimBg, 1.00f, 0.00f, 0.00f, 0.20f);
        style.setColor(ImGuiCol.ModalWindowDimBg, 1.00f, 0.00f, 0.00f, 0.35f);

        style.setWindowPadding(8.00f, 8.00f);
        style.setFramePadding(5.00f, 2.00f);
        style.setCellPadding(6.00f, 6.00f);
        style.setItemSpacing(6.00f, 6.00f);
        style.setItemInnerSpacing(6.00f, 6.00f);
        style.setTouchExtraPadding(0.00f, 0.00f);
        style.setIndentSpacing(25);
        style.setScrollbarSize(15);
        style.setGrabMinSize(10);
        style.setWindowBorderSize(1);
        style.setChildBorderSize(1);
        style.setPopupBorderSize(1);
        style.setFrameBorderSize(1);
        style.setTabBorderSize(1);
        style.setWindowRounding(7);
        style.setChildRounding(4);
        style.setFrameRounding(3);
        style.setPopupRounding(4);
        style.setScrollbarRounding(9);
        style.setGrabRounding(3);
        style.setLogSliderDeadzone(4);
        style.setTabRounding(4);

        imGuiGlfw.init(windowHandle, true);
        imGuiGl.init("#version 110");
    }

    /**
     * Asserts everything is set up to save the level.
     *
     * @return true if everything is set up to save the level
     */
    private boolean canSave() {

        // Has the player been placed?
        if (!playerPlaced) {
            return false;
        }

        // Is there at least one moonlight?
        if (board.getRemainingMoonlight() == 0) {
            return false;
        }

        // Is every tile filled?
        return board.assertNoEmptyTiles();
    }

    // UNUSED

    /**
     * Called when a finger was lifted or a mouse button was released. The button parameter will be {@link Buttons#LEFT} on iOS.
     *
     * @param screenX
     * @param screenY
     * @param pointer the pointer for the event.
     * @param button  the button
     * @return whether the input was processed
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Called when a key was released
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed
     */
    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    /**
     * Called when a key was typed
     *
     * @param character The character
     * @return whether the input was processed
     */
    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    /**
     * @param width
     * @param height
     * @see ApplicationListener#resize(int, int)
     */
    @Override
    public void resize(int width, int height) {
    }

    /**
     * @see ApplicationListener#pause()
     */
    @Override
    public void pause() {

    }

    /**
     * @see ApplicationListener#resume()
     */
    @Override
    public void resume() {

    }

}
