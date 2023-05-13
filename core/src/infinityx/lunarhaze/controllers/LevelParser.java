package infinityx.lunarhaze.controllers;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.FilmStrip;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.Board;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.Tile;
import infinityx.lunarhaze.models.entity.Enemy;
import infinityx.util.PatrolPath;

/**
 * This class is responsible for parsing JSON objects representing a level's configuration,
 * following the structure generated by the {@link LevelSerializer} class. The JSON objects are
 * deserialized into a complete {@link LevelContainer}, which can then be used to render the level within the game.
 * <p>
 * Follows the singleton pattern, there is at most one instance of this class alive at any time
 */
public class LevelParser {

    /**
     * Singleton pattern
     */
    private static LevelParser instance = null;

    /**
     * private as not to conflict with singleton pattern
     */
    private LevelParser() {
    }

    /**
     * Load instance of LevelParser class (Singleton)
     */
    public static LevelParser LevelParser() {
        if (instance == null) {
            instance = new LevelParser();
        }
        return instance;
    }

    /**
     * tile world size
     */
    float[] wSize;

    /**
     * tile screen size
     */
    int[] sSize;

    /**
     * Sprite sheet holding all tile textures
     */
    private FilmStrip tileSheet;

    /**
     * Access to current container since it can be flushed upon re-initialization.
     */
    private LevelContainer levelContainer;

    /**
     * Caches all constants (between levels) from directory
     *
     * @param directory asset manager holding list of ... assets
     * @param canvas
     */
    public void loadConstants(AssetDirectory directory, GameCanvas canvas) {
        JsonValue dimensions = directory.getEntry("dimensions", JsonValue.class);
        wSize = dimensions.get("tileWorldSize").asFloatArray();
        sSize = dimensions.get("tileScreenSize").asIntArray();

        canvas.setWorldToScreen(new Vector2(sSize[0] / wSize[0], sSize[1] / wSize[1]));

        levelContainer = new LevelContainer(
                directory,
                directory.getEntry("enemies", JsonValue.class),
                directory.getEntry("objects", JsonValue.class),
                directory.getEntry("player", JsonValue.class)
        );

        this.tileSheet = directory.getEntry("tile.sheet", FilmStrip.class);
    }

    /**
     * Creates a level given a json value.
     * You gotta call loadConstants before calling this method.
     *
     * @param levelContents json value holding level layout
     * @param directory     asset manager holding list of textures
     */
    public LevelContainer loadLevel(AssetDirectory directory, JsonValue levelContents) {
        // LevelContainer empty at this point
        levelContainer.flush();

        // Ambient light
        parseAmbient(levelContents.get("ambient"), levelContainer.getRayHandler());

        // Generate board
        JsonValue tiles = levelContents.get("tiles");
        Board board = parseBoard(directory, tiles, levelContainer.getRayHandler());
        levelContainer.setBoard(board);

        JsonValue scene = levelContents.get("scene");

        // Generate player
        JsonValue player = scene.get("player");
        levelContainer.getPlayer().setPosition(player.getFloat(0), player.getFloat(1));

        // Generate scene objects
        if (scene.has("objects")) {
            JsonValue objects = scene.get("objects");
            for (JsonValue objInfo : objects) {
                JsonValue objPos = objInfo.get("position");
                float objScale = objInfo.getFloat("scale");

                boolean flip = objInfo.has("flip") ? objInfo.getBoolean("flip") : false;

                levelContainer.addSceneObject(
                        objInfo.getString("type"), objPos.getFloat(0),
                        objPos.getFloat(1), objScale, flip
                );
            }
        }

        // create pathfinder
        float playerSize = levelContainer.getPlayer().getBoundingRadius();
        levelContainer.createPathFinder(new Vector2(playerSize, playerSize));

        // Generate enemies
        JsonValue enemies = scene.get("enemies");
        int curId = 0;
        while (true) {
            JsonValue enemyInfo = enemies.get(curId);
            if (enemyInfo == null) break;

            JsonValue enemyPos = enemyInfo.get("position");

            JsonValue patrolInfo = enemyInfo.get("patrol");
            Array<Vector2> patrolPath = new Array<>();
            for (JsonValue patrolPos : patrolInfo) {
                patrolPath.add(new Vector2(patrolPos.getInt(0), patrolPos.getInt(1)));
            }

            Enemy newEnemy;
            newEnemy = levelContainer.addEnemy(
                    Enemy.EnemyType.fromString(enemyInfo.getString("type")),
                    enemyPos.getFloat(0),
                    enemyPos.getFloat(1),
                    new PatrolPath(patrolPath)
            );

            if (enemyInfo.has("scale"))
                newEnemy.setScale(enemyInfo.getFloat("scale"));

            curId++;
        }

        // Generate level settings
        JsonValue settings = levelContents.get("settings");
        parseSettings(settings);

        return levelContainer;
    }

    /**
     * Creates an empty level to be filled in the level editor.
     * You gotta call loadConstants before calling this method.
     */
    public LevelContainer loadEmpty(int width, int height) {
        // LevelContainer empty at this point
        levelContainer.flush();

        Board board = new Board(width, height);
        board.setTileSheet(tileSheet);

        board.setTileScreenDim(sSize[0], sSize[1]);
        board.setTileWorldDim(wSize[0], wSize[1]);

        levelContainer.setBoard(board);

        return levelContainer;
    }

    /**
     * Creates the Board for the _specific_ level.
     *
     * @param boardFormat the JSON tree defining the board
     */
    private Board parseBoard(AssetDirectory directory, JsonValue boardFormat, RayHandler rayHandler) {
        JsonValue tiles = boardFormat.get("layout");
        JsonValue moonlightData = boardFormat.get("moonlight");

        IntArray tileData = new IntArray();
        int numRows = 0;

        for (JsonValue row : tiles) {
            tileData.addAll(row.asIntArray());
            numRows++;
        }

        int numCols = tileData.size / numRows;

        Board board = new Board(numCols, numRows);
        board.setTileSheet(tileSheet);

        board.setTileScreenDim(sSize[0], sSize[1]);
        board.setTileWorldDim(wSize[0], wSize[1]);

        // moonlight stuff
        JsonValue light = moonlightData.get("lighting");
        float[] color = light.get("color").asFloatArray();
        levelContainer.setMoonlightColor(color);
        float dist = light.getFloat("distance");
        int rays = light.getInt("rays");

        // board layout stuff
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                int tileNum = tileData.get((board.getHeight() - y - 1) * board.getWidth() + x);
                board.setTileNum(x, y, tileNum);
                board.setTileType(x, y, tileTypeFromNum(tileNum));
                PointLight point = new PointLight(
                        rayHandler, rays, Color.WHITE, dist,
                        board.boardCenterToWorldX(x), board.boardCenterToWorldY(y)
                );
                point.setColor(color[0], color[1], color[2], color[3]);
                point.setSoft(light.getBoolean("soft"));
                point.setXray(true);
                board.setSpotlight(x, y, point);
                board.setLit(x, y, false);
            }
        }

        for (JsonValue moonlightPos : moonlightData.get("positions")) {
            int t_x = moonlightPos.getInt(0);
            int t_y = moonlightPos.getInt(1);

            board.setLit(t_x, t_y, true);
            board.setCollectable(t_x, t_y, true);
        }

        return board;
    }


    /**
     * Creates the ambient lighting for the _specific_ level
     *
     * @param light the JSON tree defining the light
     */
    private void parseAmbient(JsonValue light, RayHandler rayhandler) {
        RayHandler.setGammaCorrection(light.getBoolean("gamma"));
        RayHandler.useDiffuseLight(light.getBoolean("diffuse"));

        float[] stealth = light.get("stealth-color").asFloatArray();
        float[] battle = light.get("battle-color").asFloatArray();
        // Initially in stealth phase
        rayhandler.setAmbientLight(stealth[0], stealth[1], stealth[2], stealth[3]);
        levelContainer.setStealthAmbience(stealth);
        levelContainer.setBattleAmbience(battle);

        int blur = light.getInt("blur");
        rayhandler.setBlur(blur > 0);
        rayhandler.setBlurNum(blur);
    }

    /**
     * Creates the settings for the _specific_ level
     *
     * @param settings the JSON tree defining the settings
     */
    private void parseSettings(JsonValue settings) {
        int transitionTime = settings.getInt("transition");
        int phaseLength = settings.getInt("phaseLength");
        levelContainer.getSettings().setPhaseLength(phaseLength);
        levelContainer.getSettings().setTransition(transitionTime);

        JsonValue enemySpawnerSettings = settings.get("enemy-spawner");

        float[] addInfo = enemySpawnerSettings.get("add-tick").asFloatArray();
        levelContainer.getSettings().setSpawnRateMin(addInfo[0]);
        levelContainer.getSettings().setSpawnRateMax(addInfo[1]);
        levelContainer.getSettings().setEnemyCount(enemySpawnerSettings.getInt("count"));
        levelContainer.getSettings().setDelay(enemySpawnerSettings.getInt("delay"));

        for (JsonValue spawnPos : enemySpawnerSettings.get("spawn-locations")) {
            int x = spawnPos.getInt(0);
            int y = spawnPos.getInt(1);

            levelContainer.getSettings().addSpawnLocation(x, y);
        }

        levelContainer.getEnemySpawner().initialize(levelContainer.getSettings());
    }

    /**
     * @param num
     * @return Type
     */
    private Tile.TileType tileTypeFromNum(int num) {
        switch (num) {
            case 1:
            case 2:
            case 3:
                return Tile.TileType.Grass;
            case 4:
                return Tile.TileType.Road;
            case 5:
            case 6:
                return Tile.TileType.Dirt;
            default:
                return Tile.TileType.Grass;
        }
    }
}