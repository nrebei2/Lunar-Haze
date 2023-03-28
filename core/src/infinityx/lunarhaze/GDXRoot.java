package infinityx.lunarhaze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.util.ScreenObserver;

/**
 * Owns and handles switching between Screens
 */
public class GDXRoot extends Game implements ScreenObserver {
    // Multiple Modes/Screens (GameMode, LoadingMode, etc.) with GDXRoot handling switching the scenes

    /**
     * AssetManager to load game assets (textures, sounds, etc.)
     */
    AssetDirectory directory;
    /**
     * Drawing context to display graphics
     */
    private GameCanvas canvas;
    /**
     * Asset loading screen
     */
    private LoadingMode loading;
    /**
     * Game Screen and controller
     */
    private GameMode game;
    /**
     * Menu Screen
     */
    private MenuMode menu;

    /**
     * Level editor
     */
    private EditorMode editor;

    /**
     * Creates a new game from the configuration settings.
     * <p>
     * This method configures the asset manager, but does not load any assets
     * or assign any screen.
     */
    public GDXRoot() {
    }

    /**
     * Called when the Application is first created.
     */
    public void create() {
        canvas = new GameCanvas();

        // Initialize each screen
        loading = new LoadingMode("assets.json", canvas, 1);
        game = new GameMode(canvas);
        menu = new MenuMode(canvas);
        editor = new EditorMode(canvas);

        // Set screen observer to this game
        loading.setObserver(this);
        game.setObserver(this);
        menu.setObserver(this);
        editor.setObserver(this);

        setScreen(loading);
    }

    /**
     * Called when the Application is destroyed.
     * <p>
     * This is preceded by a call to pause().
     */
    public void dispose() {
        // Call dispose on our children
        setScreen(null);
        game.dispose();
        menu.dispose();

        canvas.dispose();
        canvas = null;

        // Unload all the resources
        if (directory != null) {
            directory.unloadAssets();
            directory.dispose();
            directory = null;
        }
        super.dispose();
    }

    /**
     * Called when the Application is resized.
     * <p>
     * This can happen at any point during a non-paused state but will never happen
     * before a call to create().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        canvas.resize();
        super.resize(width, height);
    }

    /**
     * The given screen has made a request to exit.
     * <p>
     * The value exitCode can be used to implement menu options.
     *
     * @param screen   The screen requesting to exit
     * @param exitCode The state of the screen upon exit
     */
    public void exitScreen(Screen screen, int exitCode) {
        // Can think of this as a FSM with nodes as screens and the code determining the edges
        if (screen == loading) {
            // All assets are now loaded
            directory = loading.getAssets();
            menu.gatherAssets(directory);
            game.gatherAssets(directory);
            editor.gatherAssets(directory);
            setScreen(menu);

            LevelParser ps = LevelParser.LevelParser();
            ps.loadConstants(directory, canvas);

            loading.dispose();
            loading = null;
        } else if (screen == menu) {
            // TODO: should exitCode be the level?
            switch (exitCode) {
                case MenuMode.GO_EDITOR:
                    setScreen(editor);
                    break;
                case MenuMode.GO_PLAY:
                    game.setLevel(menu.getLevelSelected());
                    setScreen(game);
                    break;
            }
        } else if (screen == game) {
            if (exitCode == GameMode.GO_MENU) {
                setScreen(menu);
            }
        } else if (screen == editor) {
            if (exitCode == EditorMode.GO_MENU) {
                setScreen(menu);
            }
        } else {
            Gdx.app.exit();
        }
    }

}