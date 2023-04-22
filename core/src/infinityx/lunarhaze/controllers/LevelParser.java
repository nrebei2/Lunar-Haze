package infinityx.lunarhaze.controllers;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.Board;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.Tile;

import java.util.ArrayList;

/**
 * Controller class, parses jsons to create a level container
 * <p>
 * Follows the singleton pattern, there is at most one instance of this class alive at any time
 */
public class LevelParser {

    /**
     * Singleton pattern
     */
    private static LevelParser instance = null;

//    public static final short SCENE_BIT = 1;
//    public static final short PLAYER_BIT = 1 << 1;
//    public static final short ENEMY_BIT = 1 << 3;

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

    float[] wSize;
    int[] sSize;

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
    }

    /**
     * Creates a level given a json value.
     * Json value formatted as in assets/levels.json.
     * You gotta call loadConstants before calling this method.
     *
     * @param levelContents json value holding level layout
     * @param directory     asset manager holding list of textures
     */
    public LevelContainer loadLevel(AssetDirectory directory, JsonValue levelContents) {
        // LevelContainer empty at this point
        levelContainer.flush();

        // Ambient light
        parseLighting(levelContents.get("ambient"), levelContainer.getRayHandler());

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
            int objId = 0;
            while (true) {
                JsonValue objInfo = objects.get(objId);
                if (objInfo == null) break;

                JsonValue objPos = objInfo.get("position");
                float objScale = objInfo.getFloat("scale");

                levelContainer.addSceneObject(
                        objInfo.getString("type"), objPos.getFloat(0),
                        objPos.getFloat(1), objScale
                );

                objId++;
            }
        }

        // create pathfinder
        float playerSize = levelContainer.getPlayer().getBoundingRadius();
        levelContainer.createPathFinder(playerSize);

        // Generate enemies
        JsonValue enemies = scene.get("enemies");
        int curId = 0;
        while (true) {
            JsonValue enemyInfo = enemies.get(curId);
            if (enemyInfo == null) break;

            JsonValue enemyPos = enemyInfo.get("position");

            ArrayList<Vector2> patrol = new ArrayList<>();
            for (JsonValue patrolPos : enemyInfo.get("patrol")) {
                patrol.add(new Vector2(patrolPos.getInt(0), patrolPos.getInt(1)));
            }

            levelContainer.addEnemy(
                    enemyInfo.getString("type"),
                    enemyPos.getFloat(0),
                    enemyPos.getFloat(1),
                    patrol
            );

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
        String texType = boardFormat.get("type").asString();

        IntArray tileData = new IntArray();
        int numRows = 0;

        for (JsonValue row : tiles) {
            tileData.addAll(row.asIntArray());
            numRows++;
        }

        int numCols = tileData.size / numRows;

        Board board = new Board(numRows, numCols);

        board.setTileScreenDim(sSize[0], sSize[1]);
        board.setTileWorldDim(wSize[0], wSize[1]);

        // moonlight stuff
        JsonValue light = moonlightData.get("lighting");
        float[] color = light.get("color").asFloatArray();
        float dist = light.getFloat("distance");
        int rays = light.getInt("rays");

        // board layout stuff
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                int tileNum = tileData.get((board.getHeight() - y - 1) * board.getWidth() + x);
                board.setTileTexture(x, y,
                        directory.getEntry(texType + tileNum, Texture.class), tileNum
                );
                board.setTileType(x, y, tileTypeFromNum(tileNum));
                board.setWalkable(x, y, true);
                PointLight point = new PointLight(
                        rayHandler, rays, Color.WHITE, dist,
                        board.boardCenterToWorldX(x), board.boardCenterToWorldY(y)
                );
                point.setColor(color[0], color[1], color[2], color[3]);
                point.setSoft(light.getBoolean("soft"));
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
    private void parseLighting(JsonValue light, RayHandler rayhandler) {
        RayHandler.setGammaCorrection(light.getBoolean("gamma"));
        RayHandler.useDiffuseLight(light.getBoolean("diffuse"));

        float[] color = light.get("color").asFloatArray();
        rayhandler.setAmbientLight(color[0], color[1], color[2], color[3]);
        levelContainer.setStealthAmbience(color);

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
        float transitionTime = settings.getFloat("transition");
        float phaseLength = settings.getFloat("phaseLength");
        levelContainer.setPhaseLength(phaseLength);
        levelContainer.setPhaseTransitionTime(transitionTime);
        levelContainer.setBattleAmbience(settings.get("battle-ambiance").asFloatArray());
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