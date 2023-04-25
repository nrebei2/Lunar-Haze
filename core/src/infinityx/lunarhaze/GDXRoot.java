package infinityx.lunarhaze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.controllers.InputController;
import infinityx.lunarhaze.controllers.LevelParser;
import infinityx.lunarhaze.controllers.LevelSerializer;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.screens.*;
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
     * Level Selection Screen
     */
    private LevelSelectionMode selection;
    /**
     * Setting Screen
     */
    private SettingMode setting;
    /**
     * Setting Screen
     */
    private GameSetting setting_preference;
    /**
     * About us Screen
     */
    private AboutUsMode aboutUs;
    /**
     * Pause Screen
     */
    private PauseMode pause;

    /**
     * Allocate Screen
     */
    private AllocateMode allocate;

    /**
     * Level editor
     */
    private EditorMode editor;

    /**
     * The game's previous screen
     */
    private String previousScreen;

    public String getPreviousScreen() {
        return previousScreen;
    }

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
        selection = new LevelSelectionMode(canvas);
        setting_preference = new GameSetting();
        setting = new SettingMode(canvas, this, setting_preference);
        aboutUs = new AboutUsMode(canvas);
        pause = new PauseMode(canvas);
        allocate = new AllocateMode(canvas, game);
        editor = new EditorMode(canvas);

        // Set screen observer to this game
        loading.setObserver(this);
        game.setObserver(this);
        menu.setObserver(this);
        selection.setObserver(this);
        setting.setObserver(this);
        aboutUs.setObserver(this);
        pause.setObserver(this);
        allocate.setObserver(this);
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
        allocate.dispose();
        menu.dispose();
        selection.dispose();
        setting.dispose();
        aboutUs.dispose();
        pause.dispose();

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
            selection.gatherAssets(directory);
            setting.gatherAssets(directory);
            aboutUs.gatherAssets(directory);
            pause.gatherAssets(directory);
            allocate.gatherAssets(directory);
            game.gatherAssets(directory);
            InputController.getInstance().loadConstants(directory);

            editor.gatherAssets(directory);
            editor.setupImGui();

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
                    setScreen(selection);
                    break;
                case MenuMode.GO_SETTING:
                    previousScreen = "menu";
                    setScreen(setting);
                    break;
                case MenuMode.GO_ABOUT_US:
                    setScreen(aboutUs);
                    break;
                case MenuMode.GO_EXIT:
                    Gdx.app.exit();
                    break;
            }
        } else if (screen == selection) {
            switch (exitCode){
                case LevelSelectionMode.GO_Play:
                    game.setLevel(selection.getLevelSelected());
                    game.setupLevel();
                    setScreen(game);
                    break;
                case LevelSelectionMode.GO_BACK:
                    setScreen(menu);
                    break;
            }
        } else if (screen == setting) {
            switch (exitCode) {
                case SettingMode.GO_MENU:
                    setScreen(menu);
                    break;
                case SettingMode.GO_PAUSE:
                    setScreen(pause);
                    break;
            }
        } else if (screen == aboutUs) {
            switch (exitCode) {
                case AboutUsMode.GO_MENU:
                    setScreen(menu);
                    break;
            }
        } else if (screen == game) {
            switch (exitCode) {
                case GameMode.GO_PAUSE:
                    setScreen(pause);
                    break;
                case GameMode.GO_ALLOCATE:
                    allocate.setGameMode(game);
                    allocate.setCanvas(canvas);
                    setScreen(allocate);
                    break;
            }
        } else if (screen == pause) {
            switch (exitCode) {
                case PauseMode.GO_RESUME:
                    setScreen(game);
                    break;
                case PauseMode.GO_MENU:
                    setScreen(menu);
                    break;
                case PauseMode.GO_SETTING:
                    previousScreen = "pause";
                    setScreen(setting);
                    break;
                case PauseMode.GO_EXIT:
                    Gdx.app.exit();
                    break;
                case PauseMode.GO_RESTART:
                    game.setupLevel();
                    setScreen(game);
                    break;
            }
        } else if (screen == allocate) {
            if (exitCode == AllocateMode.GO_PLAY) {
                System.out.println("Exit code switch to GO_PLAY");
                game.setGameplayController(allocate.getGameplayController());
                System.out.println("Current phase is " + game.getGameplayController().getPhase());
                System.out.println("Current hp is " + game.getGameplayController().getPlayerController().getPlayer().hp);
                setScreen(game);
            }
        } else if (screen == editor) {
            if (exitCode == EditorMode.GO_MENU) {
                setScreen(menu);
            } else if (exitCode == EditorMode.GO_PLAY) {
                game.setLevel(LevelSerializer.getMostRecent());
                game.setupLevel();
                setScreen(game);
            }
        } else {
            Gdx.app.exit();
        }
    }

}
