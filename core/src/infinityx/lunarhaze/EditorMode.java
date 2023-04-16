package infinityx.lunarhaze;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.graphics.ImGuiImplGLES2;
import infinityx.util.ScreenObservable;

import java.awt.*;
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
    private final GameCanvas canvas;

    /**
     * Contains level details!
     */
    private LevelContainer level;

    /**
     * Reference to levels board
     */
    private Board board;

    private int[] boardSize;
    private TiledMap tiledMap;

    private ImFont font;

    /**
     * User requested to go to menu
     */
    public final static int GO_MENU = 0;
    public final static int GO_PLAY = 1;

    /**
     * ImGui classes
     */
    private ImGuiImplGlfw imGuiGlfw;
    private ImGuiImplGLES2 imGuiGl;

    private boolean placingMoonlight;

    private boolean showNewBoardWindow;
    private boolean showEnemyControllerWindow;

    private boolean playerPlaced;

    private ArrayList<PointLight> pointLights;

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
        font = io.getFonts().addFontFromFileTTF("fonts/font.ttf", 24);
        io.setFontDefault(font);
        io.getFonts().build();

        imGuiGlfw.init(windowHandle, true);
        imGuiGl.init("#version 110");
    }

    /**
     * type Selected :=
     * | Tile of (String, Texture)
     * | Player of Texture
     * | Enemy of (String, Texture)
     * | Object of (String, Texture, float)
     */
    abstract class Selected {
        public Texture texture;
    }

    private List<Selected> availableSelections;
    private int currentSelectionIndex;

    class Tile extends Selected {
        public Tile(Texture texture, String type, int num) {
            this.type = type;
            this.texture = texture;
            this.num = num;
        }

        public String type;
        public int num;
    }

    class Player extends Selected {
        public Player(Texture texture) {
            this.texture = texture;
        }
    }

    class Enemy extends Selected {
        public Enemy(Texture texture, String type) {
            this.type = type;
            this.texture = texture;
        }

        public String type;
    }

    class SceneObject extends Selected {
        public SceneObject(Texture texture, String type) {
            this.type = type;
            this.texture = texture;
            this.scale = 1;
        }

        public String type;
        public float scale;
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

    private float[] stealthLighting;
    private float[] battleLighting;
    private float[] moonlightLighting;

    /* Holds a reference to the enemies, so that the enemy menu can let you modify them */
    private ArrayList<infinityx.lunarhaze.entity.Enemy> enemies;

    private infinityx.lunarhaze.entity.Enemy currEnemyControlled;
    private int[] patrol1;
    private int[] patrol2;
    private boolean showBattleLighting;

    // Scene graph is just a tree of length 1 with root `EditorMode` and leaves buttons
    // therefore no need to have children for buttons
    // we can edit this later if we want
    // scene2d uses actors as nodes and scene as root
    //private ArrayList<Button>

    // class Button should hold
    // float x, y: bottom left position
    // float width, height
    // draw function given Game Canvas
    // onClick : should update color
    // onHover : should update color
    // onRelease : should update _selected_
    // boolean hit(x, y) : is mouse hitting this right now?

    // editor should should check if hit every mouse movement
    // and call the relevant methods on buttons

    public EditorMode(GameCanvas canvas) {
        this.canvas = canvas;
        availableSelections = new ArrayList<>();
        currentSelectionIndex = 0;
        placingMoonlight = false;
        pointLights = new ArrayList<>();
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
        availableSelections.add(new Tile(directory.getEntry("grass1", Texture.class), "land", 1));
        availableSelections.add(new Tile(directory.getEntry("grass2", Texture.class), "land", 2));
        availableSelections.add(new Tile(directory.getEntry("grass3", Texture.class), "land", 3));
        availableSelections.add(new Tile(directory.getEntry("grass4", Texture.class), "land", 4));
        availableSelections.add(new Tile(directory.getEntry("grass5", Texture.class), "land", 5));
        availableSelections.add(new Tile(directory.getEntry("grass6", Texture.class), "land", 6));

        availableSelections.add(new SceneObject(directory.getEntry("house1", Texture.class), "house"));
        availableSelections.add(new SceneObject(directory.getEntry("fence1", Texture.class), "fencex"));
        availableSelections.add(new SceneObject(directory.getEntry("fence2", Texture.class), "fencey"));
        availableSelections.add(new SceneObject(directory.getEntry("tree1", Texture.class), "tree"));
        availableSelections.add(new SceneObject(directory.getEntry("stone", Texture.class), "stone"));

        //availableSelections.add(new Enemy(directory.getEntry("villager", Texture.class), "villager"));
        //availableSelections.add(new Player(directory.getEntry("werewolf", Texture.class)));
        selected = availableSelections.get(currentSelectionIndex);
    }

    private void placeTile() {
        Tile curr = (Tile) selected;
        board.setTileTexture(
                (int) mouseBoard.x, (int) mouseBoard.y,
                curr.texture, curr.num
        );
        board.setTileType((int) mouseBoard.x, (int) mouseBoard.y, infinityx.lunarhaze.Tile.TileType.Road);
    }

    private void placeSelection() {
        if (selected instanceof Tile) {
            board.removePreview();
            placeTile();
        } else if (selected instanceof Player) {
            level.setPlayerStartPos(new int[]{(int) mouseBoard.x, (int) mouseBoard.y});
            level.getPlayer().setPosition(mouseWorld);
            playerPlaced = true;
            System.out.println("Player placed at " + (int) mouseBoard.x + ", " + (int) mouseBoard.y);
        } else if (selected instanceof SceneObject) {
            placeSceneObject();
        } else if (selected instanceof Enemy) {
            placeEnemy();
        }
    }

    private void placeEnemy() {
        Enemy e = (Enemy) selected;
        currEnemyControlled = level.addEnemy(e.type, mouseBoard.x, mouseBoard.y, null);
        enemies.add(currEnemyControlled);
    }

    private void placeMoonlightTile() {
        int x = (int) mouseBoard.x;
        int y = (int) mouseBoard.y;
        PointLight light = new PointLight(level.getRayHandler(), 10, new Color(moonlightLighting[0], moonlightLighting[1], moonlightLighting[2], moonlightLighting[3]), 4, board.boardCenterToWorldX(x), board.boardCenterToWorldY(y));
        light.setSoft(true);
        board.setSpotlight(x, y, light);
        board.setLit(x, y, true);
        pointLights.add(light);
        System.out.println(x + ", " + y + " is " + board.isLit(x, y));
    }

    private void placeSceneObject() {
        SceneObject curr = (SceneObject) selected;
        level.addSceneObject(
                curr.type,
                board.boardToWorldX((int) mouseBoard.x), board.boardToWorldY((int) mouseBoard.y),
                1
        );
        System.out.println(curr.type + " placed at " + (int) mouseBoard.x + ", " + (int) mouseBoard.y);
    }

    /**
     * Called when this screen becomes the current screen for a {@link com.badlogic.gdx.Game}.
     */
    @Override
    public void show() {
        boardSize = new int[]{10, 10};
        patrol1 = new int[]{0,0};
        patrol2 = new int[]{0,0};
        level = LevelParser.LevelParser().loadEmpty(boardSize[0], boardSize[1]);
        level.hidePlayer();
        playerPlaced = false;
        board = level.getBoard();
        showNewBoardWindow = false;
        showEnemyControllerWindow = false;
        showBattleLighting = false;
        enemies = new ArrayList<infinityx.lunarhaze.entity.Enemy>();
        stealthLighting = new float[]{1, 1, 1, 1};
        battleLighting = new float[]{1, 1, 1, 1};
        moonlightLighting = new float[]{1, 1, 1, 1};
        selected = new Tile(directory.getEntry("grass2", Texture.class), "land", 2);
        //selected = new Player(level.getPlayer().getTexture().getTexture());
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

        for(PointLight light : pointLights) {
            light.setColor(new Color (moonlightLighting[0], moonlightLighting[1], moonlightLighting[2], moonlightLighting[3]));
        }
        level.setBattleAmbience(battleLighting);
        level.setStealthAmbience(stealthLighting);
        if(showBattleLighting) level.getRayHandler().setAmbientLight(battleLighting[0], battleLighting[1], battleLighting[2], battleLighting[3]);
        else level.getRayHandler().setAmbientLight(stealthLighting[0], stealthLighting[1], stealthLighting[2], stealthLighting[3]);
    }

    /**
     * Draw the status of this editor mode.
     */
    private void draw(float delta) {
        canvas.clear();
        level.drawLevel(canvas);

        canvas.begin(GameCanvas.DrawPass.SHAPE, level.getView().x, level.getView().y);
        board.drawOutline(canvas);
        canvas.end();

        imGuiGlfw.newFrame();
        ImGui.newFrame();

        // --- ImGUI draw commands go here ---
        createTileMenu();
        createToolbar();
        createBrushSelection();
        createObjectMenu();

        if (showNewBoardWindow) {
            createNewBoardWindow();
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

        // Buttons on right for tiles, player, enemy, lights, scene objects
        // Simple enough dont bother using scene2d
        // Set pixel size of height of font using displayFont.getData().setScale(height / displayFont.getXHeight());

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

    }

    /**
     * Called when a key was pressed
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed
     */
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.R) {
            currentSelectionIndex = (currentSelectionIndex + 1) % availableSelections.size();
            selected = availableSelections.get(currentSelectionIndex);
            return true;
        }
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
     * Called when a finger or the mouse was dragged.
     *
     * @param screenX
     * @param screenY
     * @param pointer the pointer for the event.
     * @return whether the input was processed
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (ImGui.getIO().getWantCaptureMouse()) return false;

        mouseWorld.set(canvas.ScreenToWorldX(Gdx.input.getX()), canvas.ScreenToWorldY(Gdx.input.getY()));
        if (selected == null) {
            return false;
        }
        if (selected instanceof Tile) {
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
        if (ImGui.getIO().getWantCaptureMouse()) return false;
        // Cursor world position
        mouseWorld.set(canvas.ScreenToWorldX(Gdx.input.getX()), canvas.ScreenToWorldY(Gdx.input.getY()));

        if (selected == null) {
            return true;
        }

        int boardX = board.worldToBoardX(mouseWorld.x);
        int boardY = board.worldToBoardY(mouseWorld.y);

        if (selected instanceof Tile) {
            // snap to tile
            if (!mouseBoard.epsilonEquals(boardX, boardY)) {
                // mouse is on different tile now
                mouseBoard.set(boardX, boardY);
                board.setPreviewTile((int) mouseBoard.x, (int) mouseBoard.y, selected.texture);
            }
        } else if (selected instanceof Player && !playerPlaced) {
            if (board.inBounds(boardX, boardY)) {
                level.getPlayer().setPosition(mouseWorld);
                level.showPlayer();
            } else if (!playerPlaced) {
                level.hidePlayer();
            }
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
        return false;
    }

    // IMGUI FUNCTIONS

    private void createTileMenu() {
        ImGui.getStyle().setFramePadding(15, 15);
        ImGui.begin("Tile Selection");

        for (int i = 0; i < availableSelections.size(); i++) {
            if (availableSelections.get(i) instanceof Tile) {
                Tile tile = (Tile) availableSelections.get(i);

                if (ImGui.imageButton(tile.texture.getTextureObjectHandle(), 100, 100 * 3 / 4)) {
                    placingMoonlight = false;
                    selected = availableSelections.get(i);
                }

                // Position the next tile in the row
                ImGui.sameLine();
            }
        }

        ImGui.end();
    }

    private void createObjectMenu() {
        ImGui.getStyle().setFramePadding(15, 15);
        ImGui.begin("Object Selection");

        for (int i = 0; i < availableSelections.size(); i++) {
            if (availableSelections.get(i) instanceof SceneObject) {
                SceneObject obj = (SceneObject) availableSelections.get(i);

                if (ImGui.imageButton(obj.texture.getTextureObjectHandle(), 100, 100 * 3 / 4)) {
                    placingMoonlight = false;
                    selected = availableSelections.get(i);
                }

                // Position the next tile in the row
                ImGui.sameLine();
            }
        }

        ImGui.end();
    }

    private void createToolbar() {
        if (ImGui.beginMainMenuBar()) {
            createFileMenu();
            //createEditMenu(); //TODO: implement edit menu
            createEnemyMenu();
            createViewMenu();

            ImGui.endMainMenuBar();
        }
    }

    private void createFileMenu() {
        if (ImGui.beginMenu("   File   ")) {
            if (ImGui.menuItem("New")) {
                showNewBoardWindow = true;
            }
            ImGui.spacing();
            ImGui.spacing();
            if (ImGui.menuItem("Save")) {
                LevelSerializer.saveBoardToJsonFile(level, board, "newLevel");
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

    private void createViewMenu () {
        if (ImGui.beginMenu("View")) {
            if (ImGui.menuItem("Toggle Stealth / Battle Lighting")) {
                showBattleLighting = !showBattleLighting;
            }
            ImGui.endMenu();
        }
    }

    private void createEnemyMenu() {
        if(ImGui.beginMenu("   Enemy   ")) {

            // Iterate through enemies and create options for menu
            for(int i = 0; i < enemies.size(); i++) {
                if(ImGui.menuItem("Show Enemy " + i + " Menu")) {
                    currEnemyControlled = enemies.get(i);
                    showEnemyControllerWindow = true;
                }
            }

            ImGui.endMenu();
        }
    }

    private void createNewBoardWindow() {
        ImGui.begin("New Board", new ImBoolean(true));

        ImGui.text("Enter board size (width and height):");
        ImGui.inputInt2("Size", boardSize);

        if (ImGui.button("Create")) {
            level = LevelParser.LevelParser().loadEmpty(boardSize[0], boardSize[1]);
            level.hidePlayer();
            board = level.getBoard();
            showEnemyControllerWindow = false;
            showBattleLighting = false;
            enemies = new ArrayList<infinityx.lunarhaze.entity.Enemy>();
            stealthLighting = new float[]{1, 1, 1, 1};
            battleLighting = new float[]{1, 1, 1, 1};
            moonlightLighting = new float[]{1, 1, 1, 1};
            playerPlaced = false;
            showNewBoardWindow = false;
        }

        ImGui.end();
    }

    private void createEnemyControllerWindow() {
        ImGui.begin("Enemy Controller", new ImBoolean(true));
        ImGui.text("Enter patrol region:");
        ImGui.inputInt2("Bottom Left Tile", patrol1);
        ImGui.inputInt2("Top Right Tile", patrol2);
        ArrayList<Vector2> patrolPath = new ArrayList<>();
        patrolPath.add(new Vector2(patrol1[0], patrol1[1]));
        patrolPath.add(new Vector2(patrol2[0], patrol2[1]));

        if (ImGui.button("Set Patrol Region")) {
            currEnemyControlled.setPatrolPath(new ArrayList<Vector2>());
            patrol1 = new int[]{0, 0};
            patrol2 = new int[]{0, 0};
            showEnemyControllerWindow = false;
        }
        ImGui.end();
    }

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
            selected = new Player(directory.getEntry("player", Texture.class));
        }
        ImGui.spacing();
        ImGui.spacing();
        ImGui.spacing();
        if (ImGui.button("Enemy")) {
            placingMoonlight = false;
            selected = new Enemy(directory.getEntry("villager", Texture.class), "villager");
        }
        ImGui.end();
    }

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

}
