package infinityx.lunarhaze.screens;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.controllers.LevelParser;
import infinityx.lunarhaze.controllers.LevelSerializer;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.graphics.ImGuiImplGLES2;
import infinityx.lunarhaze.models.Board;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.util.FilmStrip;
import infinityx.util.ScreenObservable;

import java.util.ArrayList;

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

    private ImFont font;

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
    private boolean showNewBoardWindow;

    /**
     * Whether to display the error when you cannot save
     */
    private boolean showCannotSaveError;

    /**
     * Whether to display the Enemy->Enemy X popup to select patrol path, etc
     */
    private boolean showEnemyControllerWindow;

    /**
     * Whether the player has been placed on the board
     */
    private boolean playerPlaced;

    /**
     * List of moonlight point lights placed on level (for modifying color after placing lights)
     */
    private ArrayList<PointLight> pointLights;

    /**
     * Background texture for the editor
     */
    private Texture background;

    /**
     * type Selected :=
     * | Tile of Int
     * | Player
     * | Enemy of String
     * | Object of (String, Texture, float)
     */
    abstract static class Selected {
        /**
         * Enum specifying the type of the selected object.
         */
        public enum Type {
            TILE, PLAYER, ENEMY, OBJECT, MOONLIGHT
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
     * Holds the necessary information to place moonlight
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
    class Enemy extends Selected {
        public Enemy(String type) {
            this.type = type;
        }

        /**
         * type of Enemy
         */
        public String type;

        @Override
        public Type getType() {
            return Type.ENEMY;
        }
    }


    /**
     * Holds the necessary information to place an object
     */
    class SceneObject extends Selected {
        public SceneObject(Texture texture, String type) {
            this.type = type;
            this.texture = texture;
            this.scale = 1;
        }

        /**
         * type of scene object
         */
        public String type;

        /**
         * Texture for button
         */
        public Texture texture;
        public float scale;

        @Override
        public Type getType() {
            return Type.OBJECT;
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
            OBJECT
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
        private GameObject gameObject;

        /**
         * Constructs a PlaceObject action with the given GameObject.
         *
         * @param gameObject A GameObject to be placed.
         */
        public PlaceObject(GameObject gameObject) {
            this.gameObject = gameObject;
        }

        /**
         * Returns the associated GameObject for this action.
         *
         * @return The GameObject to be placed.
         */
        public GameObject getGameObject() {
            return gameObject;
        }

        /**
         * Sets the associated GameObject for this action.
         *
         * @param gameObject The GameObject to be placed.
         */
        public void setGameObject(GameObject gameObject) {
            this.gameObject = gameObject;
        }

        @Override
        public Type getType() {
            return Type.OBJECT;
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
     * What is on my cursor right now?
     */
    private Selected selected;

    /**
     * Tint for textures before placement
     */
    public static final Color SELECTED_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.9f);

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
     * Float holding stealth phase length
     */
    private ImFloat stealthLength;

    /**
     * Current enemy being changed in the Enemy->Enemy x new window
     */
    private infinityx.lunarhaze.models.entity.Enemy currEnemyControlled;

    /**
     * Bottom left patrol corner
     */
    private int[] patrol1;
    /**
     * Top right patrol corner
     */
    private int[] patrol2;

    /**
     * List of possible scene object selections
     */
    Array<SceneObject> objectSelections;

    /**
     * The game object that is currently selected. Ignore if selected is a tile or moonlight.
     * Otherwise, it should match the gameobject represented in {@link #selected}.
     */
    GameObject selectedObject;

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

    public EditorMode(GameCanvas canvas) {
        this.canvas = canvas;
        pointLights = new ArrayList<>();
        objectSelections = new Array<>();
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

        // Add all objects in json
        for (JsonValue object : objects) {
            objectSelections.add(
                    new SceneObject(
                            directory.getEntry(
                                    object.get("textures").getString(
                                            object.get("texture").getString("name")
                                    ), Texture.class
                            ), object.name
                    )
            );
        }

        this.background = directory.getEntry("bkg_allocate", Texture.class);
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
     * Places the currently selected object (Tile, Player, SceneObject, or Enemy) at the current mouse
     * position on the game board, handling each object type with the appropriate method (placeTile(),
     * placeSceneObject(), etc.).
     */
    private void placeSelection() {
        if (overlapped) return;
        switch (selected.getType()) {
            case TILE:
                board.removePreview();
                placeTile();
                break;
            case PLAYER:
                level.getPlayer().setPosition(mouseWorld);
                playerPlaced = true;
                level.getPlayer().setTint(Color.WHITE);
                removeSelectedObject();
                break;
            case OBJECT:
                // Perform action
                doneActions.add(new PlaceObject(placeSceneObject()));
                undoneActions.clear();
                break;
            case ENEMY:
                // Perform action
                doneActions.add(new PlaceObject(placeEnemy()));
                undoneActions.clear();
                break;
            case MOONLIGHT:
                placeMoonlightTile();
                break;
        }
    }

    /**
     * Creates a new enemy of the selected type at the current mouse position on the game board,
     * adding it to the level's list of enemies and storing a reference to it in the currEnemyControlled variable.
     */
    private infinityx.lunarhaze.models.entity.Enemy placeEnemy() {
        Enemy e = (Enemy) selected;
        ArrayList<Vector2> emptyPatrol = new ArrayList<>();
        emptyPatrol.add(new Vector2(0, 0));
        emptyPatrol.add(new Vector2(0, 0));
        return level.addEnemy(e.type, mouseWorld.x, mouseWorld.y, emptyPatrol);
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
        if (!board.isLit(x, y)) {
            System.out.printf("(%d, %d)\n", x, y);
            // PointLight logic
            PointLight light = new PointLight(
                    level.getRayHandler(),
                    40,
                    new Color(moonlightLighting[0], moonlightLighting[1], moonlightLighting[2], moonlightLighting[3]),
                    2.5f, board.boardCenterToWorldX(x), board.boardCenterToWorldY(y)
            );

            light.setSoft(true);
            board.setSpotlight(x, y, light);
            pointLights.add(light);

            // Set board tile to lit
            board.setLit(x, y, true);
            // So moonlightTiles in board can update
            board.setCollectable(x, y, true);
            // Perform action
            doneActions.add(new PlaceMoonlight(x, y));
            undoneActions.clear();
        }
    }

    /**
     * Places a scene object of the selected type at the current mouse position on the game board
     * Invariant: selected is a SceneObject
     */
    private infinityx.lunarhaze.models.entity.SceneObject placeSceneObject() {
        // Enforce invariant
        assert selected instanceof SceneObject;

        SceneObject curr = (SceneObject) selected;
        return level.addSceneObject(
                curr.type,
                mouseWorld.x, mouseWorld.y,
                curr.scale
        );
    }

    /**
     * Set the game object to be represented as {@link #selectedObject}.
     * The object will render and follow the mouse cursor until placed.
     * <p>
     * The current {@link #selected} will determine which object to add
     */
    private void setSelectedObject() {
        if (selectedObject != null) {
            removeSelectedObject();
        }
        if (selected == null) return;

        switch (selected.getType()) {
            case ENEMY:
                selectedObject = placeEnemy();
                break;
            case OBJECT:
                selectedObject = placeSceneObject();
                break;
            case PLAYER:
                selectedObject = level.getPlayer();
                selectedObject.setPosition(mouseWorld);
                level.showPlayer();
            default:
                break;
        }
        selectedObject.setTint(SELECTED_COLOR);
        selectedObject.setSensor(true);
    }

    /**
     * Reset and stop drawing the selected object
     */
    private void removeSelectedObject() {
        if (selectedObject == null) return;
        switch (selectedObject.getType()) {
            case ENEMY:
                level.removeEnemy((infinityx.lunarhaze.models.entity.Enemy) selectedObject);
                break;
            case SCENE:
                level.removeSceneObject((infinityx.lunarhaze.models.entity.SceneObject) selectedObject);
                break;
            case WEREWOLF:
                if (!playerPlaced)
                    level.hidePlayer();
            default:
                break;
        }
        selectedObject = null;
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
                case OBJECT:
                    GameObject obj = ((PlaceObject) lastAction).gameObject;
                    switch (obj.getType()) {
                        case ENEMY:
                            level.removeEnemy((infinityx.lunarhaze.models.entity.Enemy) obj);
                            break;
                        case SCENE:
                            level.removeSceneObject((infinityx.lunarhaze.models.entity.SceneObject) obj);
                            break;
                    }
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
                case OBJECT:
                    GameObject obj = ((PlaceObject) lastUndoneAction).gameObject;
                    switch (obj.getType()) {
                        case ENEMY:
                            level.addEnemy((infinityx.lunarhaze.models.entity.Enemy) obj);
                            break;
                        case SCENE:
                            level.addSceneObject((infinityx.lunarhaze.models.entity.SceneObject) obj);
                            break;
                    }
            }
            doneActions.add(lastUndoneAction);
        }
    }


    /**
     * Tests the level using the Board and LevelContainer references. Switches screens to GameMode.
     */
    private void testLevel() {
        observer.exitScreen(this, GO_PLAY);
    }

    /**
     * Called when this screen becomes the current screen for a {@link com.badlogic.gdx.Game}.
     */
    @Override
    public void show() {
        boardSize = new int[]{10, 10};
        patrol1 = new int[]{0, 0};
        patrol2 = new int[]{0, 0};
        objectScale = new float[]{1};
        showNewBoardWindow = true;
        showCannotSaveError = false;
        showEnemyControllerWindow = false;
        showBattleLighting = false;
        stealthLength = new ImFloat(10);
        selected = new Tile(0);

        Gdx.input.setInputProcessor(this);
        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);

        doneActions = new Array<>();
        undoneActions = new Array<>();
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
            if (fixture.getBody().getUserData() == selectedObject) return true;
            overlapped = true;
            return false;
        }
    };

    /**
     * Update the status of this editor mode.
     * <p>
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            observer.exitScreen(this, GO_MENU);
        }

        if (level == null) return;
        level.getWorld().step(delta, 6, 2);
        overlapped = false;

        // Determine if selected object is overlapping
        // I was forced to do this instead of contact listener since the scene objects are static
        if (selectedObject != null) {
            getAABB(selectedObject.getShapeInformation("body").fixture, lowerCache, upperCache);
            level.getWorld().QueryAABB(queryCallback, lowerCache.x, lowerCache.y, upperCache.x, upperCache.y);

            if (selectedObject.getType() == GameObject.ObjectType.WEREWOLF || selectedObject.getType() == GameObject.ObjectType.ENEMY) {
                // Don't place werewolf or enemy outside board
                if (!board.inBoundsWorld(selectedObject.getX(), selectedObject.getY())) {
                    overlapped = true;
                }
            }
            // Change tint of object depending on overlapping
            if (overlapped) {
                selectedObject.setTint(Color.RED);
            } else {
                selectedObject.setTint(SELECTED_COLOR);
            }
        }

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

        changed = true;
    }

    /**
     * Draw the status of this editor mode.
     */
    private void draw(float delta) {
        canvas.clear();
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        canvas.begin(GameCanvas.DrawPass.SPRITE);
        canvas.drawOverlay(background, Color.WHITE, true);
        canvas.end();

        if (showNewBoardWindow) {
            // Center create window
            ImGui.setNextWindowPos(ImGui.getMainViewport().getCenterX() - 175, ImGui.getMainViewport().getCenterY() - 75);
            ImGui.setNextWindowSize(350, 150);

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
        if (ImGui.beginTabBar("blah")) {
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

        // Position window right below Selection
        ImGui.setNextWindowPos(ImGui.getWindowPosX(), ImGui.getWindowPosY() + 270, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(800, 120, ImGuiCond.FirstUseEver);
        createAmbientLightingMenu();

        createToolbar();
        if (showCannotSaveError) {
            cannotSaveWindow();
        }
        if (showEnemyControllerWindow) {
            createEnemyControllerWindow();
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
     * Called when this screen is no longer the current screen for a {@link Game}.
     */
    @Override
    public void hide() {
        canvas.setZoom(1);
    }

    /**
     * Called when this screen should release all resources.
     */
    @Override
    public void dispose() {
        canvas.dispose();
        imGuiGlfw.dispose();
        imGuiGl.dispose();
        pointLights = null;
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
        if (selected == null) {
            return false;
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

        if (selected == null) {
            return false;
        }
        if (selected.getType() == Selected.Type.TILE) {
            int boardX = board.worldToBoardX(mouseWorld.x);
            int boardY = board.worldToBoardY(mouseWorld.y);

            if (!mouseBoard.epsilonEquals(boardX, boardY) || !board.hasPreview()) {
                // mouse is on different tile now
                mouseBoard.set(boardX, boardY);
                placeTile();
            }
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
        // Cursor world position

        if (selected == null) {
            return true;
        }

        switch (selected.getType()) {
            case TILE:
                // snap to tile
                int boardX = board.worldToBoardX(mouseWorld.x);
                int boardY = board.worldToBoardY(mouseWorld.y);
                System.out.printf("(x, y): (%d, %d)\n", boardX, boardY);

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
            case ENEMY:
            case OBJECT:
                if (selectedObject == null) break;
                selectedObject.setPosition(mouseWorld);
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
        if (ImGui.getIO().getWantCaptureMouse()) return false;
        if (selected.getType() == Selected.Type.OBJECT) {
            // Update the scale to the selected object
            float delta = 0.02f * amountY;
            objectScale[0] += delta;
            ((SceneObject) selected).scale = objectScale[0];
            selectedObject.setScale(objectScale[0]);
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
        if (keycode == Input.Keys.ESCAPE) {
            selected = null;
            removeSelectedObject();
        }

        if (!changed) {
            return true;
        }
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


        return false;
    }

    // IMGUI FUNCTIONS

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
        int i = 0;
        for (SceneObject obj : objectSelections) {
            if (ImGui.imageButton(obj.texture.getTextureObjectHandle(), 100, 100 * 3 / 4)) {
                selected = obj;
                objectScale[0] = 1;
                obj.scale = 1;
                setSelectedObject();
            }

            // Make object menu have 6 columns
            if ((i + 1) % 6 == 0) {
                ImGui.newLine();
            } else {
                ImGui.sameLine();
            }
            i++;
        }

        ImGui.spacing();

        // Create a slider for the scale of the object
        if (ImGui.sliderFloat("Object Scale", objectScale, 0.1f, 5.0f)) {
            // Apply the scale to the selected object when the slider is moved
            if (selected.getType() == Selected.Type.OBJECT) {
                SceneObject selectedObj = (SceneObject) selected;
                selectedObj.scale = objectScale[0];
                selectedObject.setScale(objectScale[0]);
            }
        }
    }

    /**
     * Toolbar at top of window with File, Edit, Enemy, Objects, View
     */
    private void createToolbar() {
        if (ImGui.beginMainMenuBar()) {
            createFileMenu();
            createEditMenu();
            createEnemyMenu();
            createViewMenu();
            createControlsMenu();

            ImGui.endMainMenuBar();
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
                ImGui.text("Mouse scroll");
                ImGui.tableNextColumn();
                ImGui.text("Scale scene selection");

                ImGui.endTable();
            }
            ImGui.endMenu();
        }
    }

    /**
     * File menu with New, Save, Load, Test and Exit
     */
    private void createFileMenu() {
        if (ImGui.beginMenu("   File   ")) {
            if (ImGui.menuItem("New")) {
                showNewBoardWindow = true;
            }
            ImGui.spacing();
            ImGui.spacing();
            if (ImGui.menuItem("Save")) {
                if (canSave()) {
                    LevelSerializer.saveBoardToJsonFile(level, directory);
                } else {
                    // Cannot save!
                    showCannotSaveError = true;
                }
            }
            ImGui.spacing();
            ImGui.spacing();
            if (ImGui.menuItem("Test")) {
                if (canSave()) {
                    LevelSerializer.saveBoardToJsonFile(level, directory);
                    observer.exitScreen(this, GO_PLAY);
                } else {
                    // Cannot save!
                    showCannotSaveError = true;
                }
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
            //ImGui.spacing();
            //ImGui.spacing();
            //if (ImGui.menuItem("Clear Board")) {
            //
            //}
            ImGui.endMenu();
        }
    }


    /**
     * View menu with Toggle Stealth / Battle Lighting
     */
    private void createViewMenu() {
        if (ImGui.beginMenu("View")) {
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
     * Enemy menu with options for each enemy
     */
    private void createEnemyMenu() {
        if (ImGui.beginMenu("   Enemy   ")) {

            // Iterate through enemies and create options for menu
            for (int i = 0; i < level.getEnemies().size; i++) {
                if (ImGui.menuItem("Show Enemy " + i + " Menu")) {
                    currEnemyControlled = level.getEnemies().get(i);
                    showEnemyControllerWindow = true;
                }
            }

            ImGui.endMenu();
        }
    }

    /**
     * Popup window for creating new board and setting board size
     */
    private void createNewBoardWindow() {
        ImGui.begin("New Board", new ImBoolean(true));

        ImGui.text("Enter board size (width and height):");
        ImGui.inputInt2("Size", boardSize);

        ImGui.spacing();

        ImGui.text("Enter stealth phase length:");
        ImGui.inputFloat("Length", stealthLength);

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

            showEnemyControllerWindow = false;
            showBattleLighting = false;

            stealthLighting = new float[]{1, 1, 1, 1};
            level.getRayHandler().setAmbientLight(stealthLighting[0], stealthLighting[1], stealthLighting[2], stealthLighting[3]);

            battleLighting = new float[]{1, 1, 1, 1};
            moonlightLighting = new float[]{1, 1, 1, 0.2f};
            // Only need to call once since level will hold a reference
            level.setBattleAmbience(battleLighting);
            level.setStealthAmbience(stealthLighting);
            level.setMoonlightColor(moonlightLighting);

            playerPlaced = false;
            level.setPhaseLength(stealthLength.floatValue());
            showNewBoardWindow = false;
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

    /**
     * Popup window for controlling enemy and setting patrols
     */
    private void createEnemyControllerWindow() {
        ImGui.begin("Enemy Controller", new ImBoolean(true));
        ImGui.text("Enter patrol region:");
        ImGui.inputInt2("Bottom Left Position", patrol1);
        ImGui.inputInt2("Top Right Position", patrol2);
        ArrayList<Vector2> patrolPath = new ArrayList<>();
        patrolPath.add(new Vector2(patrol1[0], patrol1[1]));
        patrolPath.add(new Vector2(patrol2[0], patrol2[1]));

        if (ImGui.button("Set Patrol Region")) {
            currEnemyControlled.setPatrolPath(patrolPath);
            patrol1 = new int[]{0, 0};
            patrol2 = new int[]{0, 0};
            showEnemyControllerWindow = false;
        }
        ImGui.end();
    }

    /**
     * Brush selection window for placing moonlight, werewolf, enemy
     */
    private void createBrushSelection() {
        if (ImGui.button("Moonlight")) {
            removeSelectedObject();
            selected = new Moonlight();
        }
        ImGui.spacing();
        ImGui.spacing();
        ImGui.spacing();
        if (ImGui.button("Werewolf")) {
            playerPlaced = false;
            selected = new Player();
            setSelectedObject();
        }
        ImGui.spacing();
        ImGui.spacing();
        ImGui.spacing();
        if (ImGui.button("Enemy")) {
            selected = new Enemy("villager");
            setSelectedObject();
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
            for (PointLight light : pointLights) {
                light.setColor(new Color(moonlightLighting[0], moonlightLighting[1], moonlightLighting[2], moonlightLighting[3]));
            }
        }
        ImGui.spacing();

        ImGui.end();
    }

    /**
     * ImGui initialization
     */
    public void setupImGui() {
        // ImGui initialization
        this.imGuiGlfw = new ImGuiImplGlfw();
        this.imGuiGl = new ImGuiImplGLES2();


        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        // TODO: why doesn't font work?
        // font = directory.getEntry("libre-small", BitmapFont.class);
        //font = io.getFonts().addFontFromFileTTF(directory.getDirectory() + "shared/LibreBaskerville-Regular.ttf", 16);
//        ImFont
        //io.setFontDefault(font);
        //io.getFonts().build();

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
