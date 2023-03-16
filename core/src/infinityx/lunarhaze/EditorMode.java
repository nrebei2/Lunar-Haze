package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import infinityx.assets.AssetDirectory;
import infinityx.util.ScreenObservable;

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
     * type Selected :=
     *  | Tile of (String, Texture)
     *  | Player of Texture
     *  | Enemy of (String, Texture)
     *  | Object of (String, Texture, float)
     */
    abstract class Selected {
        public Texture texture;
    }
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

    public static final Color SELECTED_COLOR = new Color(0.8f, 0.8f, 0.8f, 1f);


    public EditorMode(GameCanvas canvas) {
        this.canvas = canvas;
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
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    @Override
    public void show() {
        level = LevelParser.LevelParser().loadEmpty();

        //Board board = level.getBoard();
        //
        //board.setTileTexture(0, 0,
        //        directory.getEntry("land1-unlit", Texture.class),
        //        directory.getEntry("land1-lit", Texture.class),
        //        // currently collected tile is same as uncollected ones
        //        // since we have no assets for collected but lit tiles
        //        directory.getEntry("land1-lit", Texture.class)
        //);
        //board.setTileType(0, 0, infinityx.lunarhaze.Tile.TileType.Road);
        ////board.setWalkable(0, y, true);

        selected = new Tile(directory.getEntry("land1-unlit", Texture.class), "land");
    }

    /**
     * Update the status of this editor mode.
     * <p>
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
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

        // Cursor world position
        float curWorldX = canvas.ScreenToWorldX(Gdx.input.getX());
        float curWorldY = canvas.ScreenToWorldY(Gdx.input.getY());

        if (selected == null) {
            return;
        }

        if (selected instanceof Tile) {
            // snap to tile
            int boardX = level.getBoard().worldToBoardX(curWorldX);
            int boardY = level.getBoard().worldToBoardY(curWorldY);

            //System.out.printf("selected texture height: %s\n", selected.texture.toString());

            level.getBoard().setPreviewTile(new Board.PreviewTile(boardX, boardY, selected.texture));
            //System.out.printf("board pos: (%d, %d)", boardX, boardY);
        }

    }

    /**
     * Draw the status of this editor mode.
     */
    private void draw(float delta) {
        canvas.clear();
        level.drawLevel(canvas);

        canvas.beginT(level.getView().x, level.getView().y);
        level.getBoard().drawOutline(canvas);
        canvas.end();
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
        return false;
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
        return false;
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
        return false;
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
