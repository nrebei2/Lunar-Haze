package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.ScreenObservable;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

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
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;

    /**
     * User requested to go to menu
     */
    public final static int GO_MENU = 0;

    /** ImGui classes */
    private ImGuiImplGlfw imGuiGlfw;
    private ImGuiImplGl3 imGuiGl3;

    /** ImGui initialization */
    public void setupImGui() {
        // ImGui initialization
        this.imGuiGlfw = new ImGuiImplGlfw();
        this.imGuiGl3 = new ImGuiImplGl3();

        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();
        io.getFonts().build();

        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 110");
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
        public Tile(Texture texture, String type) {
            this.type = type;
            this.texture = texture;
        }

        public String type;
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
        availableSelections.add(new Tile(directory.getEntry("grass1", Texture.class), "land"));
        availableSelections.add(new Tile(directory.getEntry("grass2", Texture.class), "land"));
        availableSelections.add(new Tile(directory.getEntry("grass3", Texture.class), "land"));
        availableSelections.add(new Tile(directory.getEntry("grass4", Texture.class), "land"));
        availableSelections.add(new Tile(directory.getEntry("grass5", Texture.class), "land"));
        availableSelections.add(new Tile(directory.getEntry("grass6", Texture.class), "land"));
        //availableSelections.add(new Enemy(directory.getEntry("villager", Texture.class), "villager"));
        //availableSelections.add(new Player(directory.getEntry("werewolf", Texture.class)));
        selected = availableSelections.get(currentSelectionIndex);
    }

    private void placeTile() {
        board.setTileTexture(
                (int) mouseBoard.x, (int) mouseBoard.y,
                selected.texture
        );
        board.setTileType((int) mouseBoard.x, (int) mouseBoard.y, infinityx.lunarhaze.Tile.TileType.Road);
    }

    private void placeSelection() {
        if (selected instanceof Tile) {
            board.removePreview();
            placeTile();
        } else if (selected instanceof Player) {
            selected = null;
        }
    }

    /**
     * Called when this screen becomes the current screen for a {@link com.badlogic.gdx.Game}.
     */
    @Override
    public void show() {
        tiledMap = new TmxMapLoader().load("assets/maps/default.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        level = LevelParser.LevelParser().loadEmpty();
        level.hidePlayer();
        board = level.getBoard();
        selected = new Tile(directory.getEntry("grass2", Texture.class), "land");
        //selected = new Player(level.getPlayer().getTexture().getTexture());
        Gdx.input.setInputProcessor(this);
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
        if(Gdx.input.isKeyPressed(Input.Keys.R)) {
            currentSelectionIndex = (currentSelectionIndex + 1) % availableSelections.size();
            selected = availableSelections.get(currentSelectionIndex);
        }

    }

    /**
     * Draw the status of this editor mode.
     */
    private void draw(float delta) {
        canvas.clear();
        level.drawLevel(canvas);
        canvas.begin(GameCanvas.DrawPass.SHAPE, level.getView().x, level.getView().y);
        //tiledMapRenderer.setView(canvas.getCamera());
        //tiledMapRenderer.render();
        board.drawOutline(canvas);
        canvas.end();

        imGuiGlfw.newFrame();
        ImGui.newFrame();

        // --- ImGUI draw commands go here ---
        ImGui.showDemoWindow();
        ImGui.imageButton(selected.texture.getTextureObjectHandle(), 200, 200 * 3/4);
        // ---

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
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

        if (selected == null) {
            return false;
        }

        placeSelection();
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
        //System.out.printf("mouse world: (%f, %f)\n", mouseWorld.x, mouseWorld.y);

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
            //System.out.printf("board pos: (%d, %d)", boardX, boardY);
        } else if (selected instanceof Player) {
            if (board.inBounds(boardX, boardY)) {
                level.showPlayer();
                level.getPlayer().setPosition(mouseWorld);
            } else {
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
}
