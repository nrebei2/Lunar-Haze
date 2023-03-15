package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import infinityx.util.ScreenObservable;

public class EditorMode extends ScreenObservable implements Screen {
    /**
     * Reference to GameCanvas created by the root
     */
    private GameCanvas canvas;

    private Stage stage;

    public EditorMode(GameCanvas canvas) {
        this.canvas = canvas;
        stage = new Stage(new ScreenViewport());
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        //TextButton button = new TextButton("Hello!", TextButton.TextButtonStyle);
    }

    /**
     * Called when the screen should render itself.
     *
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        //canvas.clear(Color.BLACK);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();

        // Hold a levelContainer
        // From LevelParser thru loadEmpty
        // Tile can have no current texture
        // In board draw if tile is empty draw red outline
        // Buttons on right for tiles, player, enemy, lights, scene objects
        // Simple enough dont both using scene2d
        // Set size of font using displayFont.getData().setScale(height / displayFont.getXHeight());

    }

    /**
     * @param width
     * @param height
     * @see ApplicationListener#resize(int, int)
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
}
