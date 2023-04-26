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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
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
import infinityx.lunarhaze.models.Tile;
import infinityx.util.FilmStrip;
import infinityx.util.ScreenObservable;

import java.util.ArrayList;
import java.util.List;

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
     * Boolean that represents whether our selection is moonlight or not
     */
    // TODO: make moonlight part of selected instead
    private boolean placingMoonlight;

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
            TILE, PLAYER, ENEMY, OBJECT
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


    /** Holds the necessary information to place a tile */
    class Tile extends Selected {
        public Tile(int num) {
            this.num = num;
        }

        /** Follows {@link infinityx.lunarhaze.models.Tile#getTileNum()} */
        public int num;

        @Override
        public Type getType() {
            return Type.TILE;
        }
    }

    /** No information needed since container makes its own player */
    class Player extends Selected {
        @Override
        public Type getType() {
            return Type.PLAYER;
        }
    }


    /** Holds the necessary information to place an enemy */
    class Enemy extends Selected {
        public Enemy(String type) {
            this.type = type;
        }

        /** type of Enemy */
        public String type;

        @Override
        public Type getType() {
           return Type.ENEMY;
        }
    }


    /** Holds the necessary information to place an object */
    class SceneObject extends Selected {
        public SceneObject(Texture texture, String type) {
            this.type = type;
            this.texture = texture;
            this.scale = 1;
        }

        /** type of scene object */
        public String type;

        /** Texture for button */
        public Texture texture;
        public float scale;

        @Override
        public Type getType() {
            return Type.OBJECT;
        }
    }

    /**
     * What is on my cursor right now?
     */
    private Selected selected;

    /**
     * Tint for textures before placement
     */
    public static final Color SELECTED_COLOR = new Color(0.65f, 0.65f, 0.65f, 1f);

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

    /** List of possible scene object selections */
    Array<SceneObject> objectSelections;

    /** The game object that is currently selected. Ignore if selected is a tile */
    GameObject selectedObject;

    /**
     * When false, display the stealth ambient lighting, when true, display the battle ambient lighting
     */
    private boolean showBattleLighting;

    public EditorMode(GameCanvas canvas) {
        this.canvas = canvas;
        placingMoonlight = false;
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
        for (JsonValue object: objects) {
            objectSelections.add(
                    new SceneObject(
                            directory.getEntry(
                                    object.get("textures").getString(
                                            object.get("texture").getString("name")
                                    ), FilmStrip.class
                            ).getTexture(), object.name
                    )
            );
        }
    }

    /**
     * Places a tile of the selected type at the current mouse position on the game board
     * Invariant: selected is a Tile
     */
    private void placeTile() {
        // Enforce invariant
        assert selected instanceof Tile;

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
        switch (selected.getType()) {
            case TILE:
                board.removePreview();
                placeTile();
                break;
            case PLAYER:
                level.getPlayer().setPosition(mouseWorld);
                playerPlaced = true;
                break;
            case OBJECT:
                placeSceneObject();
                break;
            case ENEMY:
                placeEnemy();
                break;
        }
    }

    /**
     * Creates a new enemy of the selected type at the current mouse position on the game board,
     * adding it to the level's list of enemies and storing a reference to it in the currEnemyControlled variable.
     */
    private void placeEnemy() {
        Enemy e = (Enemy) selected;
        ArrayList<Vector2> emptyPatrol = new ArrayList<>();
        emptyPatrol.add(new Vector2(0, 0));
        emptyPatrol.add(new Vector2(0, 0));
        level.addEnemy(e.type, mouseWorld.x, mouseWorld.y, emptyPatrol);
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
        if(!board.isLit(x, y)) {
            // PointLight logic
            PointLight light = new PointLight(level.getRayHandler(), 6, new Color(moonlightLighting[0], moonlightLighting[1], moonlightLighting[2], moonlightLighting[3]), 3, board.boardCenterToWorldX(x), board.boardCenterToWorldY(y));
            light.setSoft(true);
            board.setSpotlight(x, y, light);
            pointLights.add(light);

            // Set board tile to lit
            board.setLit(x, y, true);
        }
    }

    /**
     * Places a scene object of the selected type at the current mouse position on the game board
     * Invariant: selected is a SceneObject
     */
    private void placeSceneObject() {
        // Enforce invariant
        assert selected instanceof SceneObject;

        SceneObject curr = (SceneObject) selected;
        level.addSceneObject(
                curr.type,
                mouseWorld.x, mouseWorld.y,
                curr.scale
        );
    }

    /**
     * Tests the level using the Board and LevelContainer references. Switches screens to GameMode.
     */
    private void testLevel() {
        level.setBoard(board);
        observer.exitScreen(this, GO_PLAY);
    }

    /**
     * Called when this screen becomes the current screen for a {@link com.badlogic.gdx.Game}.
     */
    @Override
    public void show() {
        boardSize = new int[] {10, 10};
        patrol1 = new int[]{0, 0};
        patrol2 = new int[]{0, 0};
        objectScale = new float[]{1};
        showNewBoardWindow = true;
        showCannotSaveError = false;
        showEnemyControllerWindow = false;
        showBattleLighting = false;
        stealthLighting = new float[]{1, 1, 1, 1};
        battleLighting = new float[]{1, 1, 1, 1};
        moonlightLighting = new float[]{1, 1, 1, 0.2f};
        stealthLength = new ImFloat(10);
        selected = new Tile(0);

        Gdx.input.setInputProcessor(this);
        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);
    }

    /**
     * Update the status of this editor mode.
     * <p>
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        if (level == null) return;

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

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            observer.exitScreen(this, GO_MENU);
        }

        for (PointLight light : pointLights) {
            light.setColor(new Color(moonlightLighting[0], moonlightLighting[1], moonlightLighting[2], moonlightLighting[3]));
        }
        level.setBattleAmbience(battleLighting);
        level.setStealthAmbience(stealthLighting);
        level.setMoonlightColor(moonlightLighting);
        if (showBattleLighting)
            level.getRayHandler().setAmbientLight(battleLighting[0], battleLighting[1], battleLighting[2], battleLighting[3]);
        else
            level.getRayHandler().setAmbientLight(stealthLighting[0], stealthLighting[1], stealthLighting[2], stealthLighting[3]);
    }

    /**
     * Draw the status of this editor mode.
     */
    private void draw(float delta) {
        canvas.clear();
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        if (showNewBoardWindow) {
            // TODO: background maybe
            createNewBoardWindow();
            ImGui.render();
            imGuiGl.renderDrawData(ImGui.getDrawData());
            return;
        }

        level.drawLevel(canvas);

        canvas.begin(GameCanvas.DrawPass.SHAPE, level.getView().x, level.getView().y);
        board.drawOutline(canvas);
        canvas.end();

        // --- ImGUI draw commands go here ---
        createTileMenu();
        createToolbar();
        createBrushSelection();
        createObjectMenu();

        if (showCannotSaveError) {
            cannotSaveWindow();
        }
        if (showEnemyControllerWindow) {
            createEnemyControllerWindow();
        }

        createAmbientLightingMenu();

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

    /**
     * Called when this screen is no longer the current screen for a {@link Game}.
     */
    @Override
    public void hide() {

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
        if (!placingMoonlight) {
            placeSelection();
        } else {
            placeMoonlightTile();
        }
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
        if (ImGui.getIO().getWantCaptureMouse()) return false;

        mouseWorld.set(canvas.ScreenToWorldX(Gdx.input.getX()), canvas.ScreenToWorldY(Gdx.input.getY()));
        if (selected == null) {
            return false;
        }
        if (selected.getType() == Selected.Type.TILE) {
            int boardX = board.worldToBoardX(mouseWorld.x);
            int boardY = board.worldToBoardY(mouseWorld.y);

            if (!mouseBoard.epsilonEquals(boardX, boardY)) {
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
        if (ImGui.getIO().getWantCaptureMouse()) return false;
        // Cursor world position
        mouseWorld.set(canvas.ScreenToWorldX(Gdx.input.getX()), canvas.ScreenToWorldY(Gdx.input.getY()));

        if (selected == null) {
            return true;
        }

        int boardX = board.worldToBoardX(mouseWorld.x);
        int boardY = board.worldToBoardY(mouseWorld.y);

        switch (selected.getType()) {
            case TILE:
                // snap to tile
                if (!mouseBoard.epsilonEquals(boardX, boardY)) {
                    // mouse is on different tile now
                    mouseBoard.set(boardX, boardY);
                    board.setPreviewTile((int) mouseBoard.x, (int) mouseBoard.y, ((Tile)selected).num);
                }
                break;
            case PLAYER:
                if (playerPlaced) break;
                if (board.inBounds(boardX, boardY)) {
                    level.getPlayer().setPosition(mouseWorld);
                    level.showPlayer();
                } else if (!playerPlaced) {
                    level.hidePlayer();
                }
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
        if (selected.getType() == Selected.Type.OBJECT) {
            float delta = 0.02f * amountY;
            ((SceneObject)selected).scale += delta;
            objectScale[0] += delta;
        }
        return false;
    }

    // IMGUI FUNCTIONS

    /**
     * Popup window for tile selection using ImGui
     */
    private void createTileMenu() {
        ImGui.getStyle().setFramePadding(15, 15);
        ImGui.begin("Tile Selection");

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
                placingMoonlight = false;
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

        ImGui.end();
    }

    /**
     * Popup window for object selection using ImGui
     */
    private void createObjectMenu() {
        ImGui.getStyle().setFramePadding(15, 15);
        ImGui.begin("Object Selection");

        for (SceneObject obj: objectSelections) {
            if (ImGui.imageButton(obj.texture.getTextureObjectHandle(), 100, 100 * 3 / 4)) {
                placingMoonlight = false;
                selected = obj;
                objectScale[0] = 1;
                obj.scale = 1;
            }

            // Position the next tile in the row
            ImGui.sameLine();
        }

        ImGui.spacing();

        // Create a slider for the scale of the object
        if (ImGui.sliderFloat("Object Scale", objectScale, 0.1f, 5.0f)) {
            // Apply the scale to the selected object when the slider is moved
            if (selected.getType() == Selected.Type.OBJECT) {
                SceneObject selectedObj = (SceneObject) selected;
                selectedObj.scale = objectScale[0];
            }
        }

        ImGui.end();
    }

    /**
     * Toolbar at top of window with File, Edit, Enemy, Objects, View
     */
    private void createToolbar() {
        if (ImGui.beginMainMenuBar()) {
            createFileMenu();
            //createEditMenu(); //TODO: implement edit menu
            createEnemyMenu();
            createViewMenu();

            ImGui.endMainMenuBar();
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
                if(canSave()) {
                    LevelSerializer.saveBoardToJsonFile(level, board, directory);
                } else {
                    // Cannot save!
                    showCannotSaveError = true;
                }
            }
            ImGui.spacing();
            ImGui.spacing();
            if (ImGui.menuItem("Test")) {
                if(canSave()) {
                    LevelSerializer.saveBoardToJsonFile(level, board, directory);
                    observer.exitScreen(this, GO_PLAY);
                } else {
                    // Cannot save!
                    showCannotSaveError = true;
                }
            }
            ImGui.endMenu();
        }
    }

    /*
    private void createEditMenu() {
        if (ImGui.beginMenu("   Edit   ")) {
            if (ImGui.menuItem("Undo")) {
                // TODO
                System.out.println("Unimplemented");
            }
            ImGui.spacing();
            ImGui.spacing();
            if (ImGui.menuItem("Redo")) {
                // TODO
                System.out.println("Unimplemented");
            }
            ImGui.spacing();
            ImGui.spacing();
            if (ImGui.menuItem("Clear Board")) {
                // TODO
                System.out.println("Unimplemented");
            }
            ImGui.endMenu();
        }
    }*/

    /**
     * View menu with Toggle Stealth / Battle Lighting
     */
    private void createViewMenu() {
        if (ImGui.beginMenu("View")) {
            if (ImGui.menuItem("Toggle Stealth / Battle Lighting")) {
                showBattleLighting = !showBattleLighting;
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
            showEnemyControllerWindow = false;
            showBattleLighting = false;
            stealthLighting = new float[]{1, 1, 1, 1};
            battleLighting = new float[]{1, 1, 1, 1};
            moonlightLighting = new float[]{1, 1, 1, 0.2f};
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
        ImGui.text("Make sure no tiles are null, the player is set, and there is at least one enemy\nand moonlight tile.");

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
        ImGui.inputInt2("Bottom Left Tile", patrol1);
        ImGui.inputInt2("Top Right Tile", patrol2);
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
        ImGui.getStyle().setFramePadding(10, 10);
        ImGui.begin("Brush Select");
        if (ImGui.button("Moonlight")) {
            placingMoonlight = true;
        }
        ImGui.spacing();
        ImGui.spacing();
        ImGui.spacing();
        if (ImGui.button("Werewolf")) {
            placingMoonlight = false;
            playerPlaced = false;
            selected = new Player();
        }
        ImGui.spacing();
        ImGui.spacing();
        ImGui.spacing();
        if (ImGui.button("Enemy")) {
            placingMoonlight = false;
            selected = new Enemy("villager");
        }
        ImGui.end();
    }

    /**
     * Controller for ambient lighting json values
     */
    private void createAmbientLightingMenu() {
        ImGui.begin("Lighting");
        ImGui.colorEdit4("Stealth Phase Lighting", stealthLighting);
        ImGui.spacing();
        ImGui.colorEdit4("Battle Phase Lighting", battleLighting);
        ImGui.spacing();
        ImGui.colorEdit4("Moonlight Lighting", moonlightLighting);
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
     * Asserts everything is set up to save the board
     * (to avoid nullpointerexception)
     * @return true if everything is set up to save the board
     */
    private boolean canSave() {

        // Has the player been placed?
        if(!playerPlaced) {
            return false;
        }

        // Is there at least one enemy?
        if (level.getEnemies().size == 0) {
            return false;
        }

        // Does every enemy have a patrol?
        for (infinityx.lunarhaze.models.entity.Enemy enemy : level.getEnemies()) {
            if (enemy.getPatrolPath() == null) {
                return false;
            }
        }

        // Is every tile filled?
        return board.assertNoNullTiles();

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
     * Called when a key was pressed
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed
     */
    @Override
    public boolean keyDown(int keycode) {
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

}
