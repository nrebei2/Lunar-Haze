package infinityx.lunarhaze;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.util.FilmStrip;

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
     * Constants from asset directory
     */
    JsonValue enemiesJson;
    JsonValue objectsJson;
    JsonValue playerJson;
    float[] wSize;
    int[] sSize;

    /**
     * Caches all constants (between levels) from directory
     * @param directory asset manager holding list of ... assets
     */
    public void loadConstants(AssetDirectory directory) {
        // TODO: create and cache player and board? not sure if that would do much
        // There is not a lot constant between levels
        JsonValue boardJson = directory.getEntry("board", JsonValue.class);
        wSize = boardJson.get("tileWorldSize").asFloatArray();
        sSize = boardJson.get("tileScreenSize").asIntArray();

        enemiesJson = directory.getEntry("enemies", JsonValue.class);
        objectsJson = directory.getEntry("objects", JsonValue.class);
        playerJson = directory.getEntry("player", JsonValue.class);
    }

    ///**
    // * Caches all textures from directory.
    // *
    // * @param directory
    // */
    //public void loadTextures(AssetDirectory directory) {
    //    if (!directory.isFinished()) {
    //        throw new RuntimeException("Directory has not finished loaded!!!");
    //    }
    //
    //    // Get all textures
    //    playerTexture = directory.getEntry("werewolf", Texture.class);
    //    enemyTexture = directory.getEntry("villager", Texture.class);
    //
    //    for (int i = 1; i <= 6; i++) {
    //        tileTextures.add(directory.getEntry("land" + i + "-unlit", Texture.class));
    //        tileTextures.add(directory.getEntry("land" + i + "-lit", Texture.class));
    //    }
    //}

    /**
     * Creates a level given a json value.
     * Json value formatted as in assets/levels.json.
     * You gotta call loadConstants before calling this method.
     *
     * @param levelContents json value holding level layout
     * @param directory asset manager holding list of textures
     */
    public LevelContainer loadLevel(AssetDirectory directory, JsonValue levelContents) {
        // LevelContainer empty at this point
        LevelContainer levelContainer = new LevelContainer();
        levelContainer.worldToScreen.set(sSize[0]/wSize[0], sSize[1]/wSize[1]);

        // Ambient light
        parseLighting(levelContents.get("ambient"), levelContainer.getRayHandler());

        // Generate board
        JsonValue tiles = levelContents.get("tiles");
        Board board = parseBoard(directory, tiles, levelContainer.getRayHandler());
        levelContainer.setBoard(board);

        JsonValue scene = levelContents.get("scene");

        // Generate player
        JsonValue player = scene.get("player");
        Werewolf playerModel = parsePlayer(directory, player, levelContainer);
        levelContainer.setPlayer(playerModel);

        // Generate enemies
        JsonValue enemies = scene.get("enemies");
        int curId = 0;
        while (true) {
            JsonValue enemyInfo = enemies.get(curId);
            if (enemyInfo == null) break;

            Enemy enemy = parseEnemy(curId, directory, enemyInfo, levelContainer);

            levelContainer.addEnemy(enemy);
            curId++;
        }
        // TODO JsonValue objects = scene.get("objects");
        return levelContainer;
    }

    private void parseGameObject(GameObject object, AssetDirectory directory, JsonValue json) {
        JsonValue p_dim = json.get("collider");
        object.setDimension(p_dim.get("width").asFloat(), p_dim.get("height").asFloat());

        // TODO: bother with error checking?
        object.setBodyType(json.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        object.setDensity(json.get("density").asFloat());
        object.setFriction(json.get("friction").asFloat());
        object.setRestitution(json.get("restitution").asFloat());
        object.setSpeed(json.get("speed").asFloat());
        //object.setStartFrame(json.get("startframe").asInt());
        JsonValue texInfo = json.get("texture");
        object.setTexture(directory.getEntry(texInfo.get("name").asString(), FilmStrip.class));
        int[] texOrigin  = texInfo.get("origin").asIntArray();
        object.setOrigin(texOrigin[0], texOrigin[1]);
    }

    /**
     * Creates a player for the level
     *
     * @param  playerFormat the JSON tree defining the player
     */
    private Werewolf parsePlayer(AssetDirectory directory, JsonValue playerFormat, LevelContainer container) {
        System.out.printf("in json: (%f, %f)\n", playerFormat.get(0).asFloat(), playerFormat.get(1).asFloat());
        Werewolf player = new Werewolf(playerFormat.get(0).asFloat(), playerFormat.get(1).asFloat());
        System.out.printf("after init: (%f, %f)\n", player.getPosition().x, player.getPosition().y);

        parseGameObject(player, directory, playerJson);

        JsonValue light = playerJson.get("spotlight");
        float[] color = light.get("color").asFloatArray();
        float dist  = light.getFloat("distance");
        int rays = light.getInt("rays");

        PointLight spotLight = new PointLight(
                container.getRayHandler(), rays, Color.WHITE, dist,
                0, 0
        );
        spotLight.setColor(color[0],color[1],color[2],color[3]);
        spotLight.setSoft(light.getBoolean("soft"));

        player.activatePhysics(container.getWorld());
        player.setSpotLight(spotLight);

        return player;
    }

    /**
     * Creates an enemy for the level
     *
     * @param  enemyFormat the JSON tree defining the enemy
     */
    private Enemy parseEnemy(int id, AssetDirectory directory, JsonValue enemyFormat, LevelContainer container) {
        JsonValue enemyPos = enemyFormat.get("position");

        ArrayList<Vector2> patrol = new ArrayList<>();
        for (JsonValue patrolPos : enemyFormat.get("patrol")) {
            patrol.add(new Vector2(patrolPos.getInt(0), patrolPos.getInt(1)));
        }

        Enemy enemy = new Enemy(id, enemyPos.get(0).asFloat(), enemyPos.get(1).asFloat(), patrol);

        JsonValue json = enemiesJson.get(enemyFormat.get("type").asString());
        parseGameObject(enemy, directory, json);

        enemy.activatePhysics(container.getWorld());

        JsonValue light = json.get("flashlight");
        float[] color = light.get("color").asFloatArray();
        float dist  = light.getFloat("distance");
        int rays = light.getInt("rays");
        float degrees = light.getFloat("degrees");

        ConeSource flashLight = new ConeSource(
                container.getRayHandler(), rays, Color.WHITE, dist,
                enemy.getX(), enemy.getY(), 0f, degrees
        );
        flashLight.setColor(color[0],color[1],color[2],color[3]);
        flashLight.setSoft(light.getBoolean("soft"));

        enemy.setFlashlight(flashLight);
        enemy.setFlashlightOn(true);

        return enemy;
    }

    /**
     * Creates the Board for the level
     *
     * @param  boardFormat the JSON tree defining the board
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

        // TODO: change if we do not want to assume board is square
        // int numCols = tileData.size / numRows;

        Board board = new Board(numRows, numRows);

        board.setTileScreenDim(sSize[0], sSize[1]);
        board.setTileWorldDim(wSize[0], wSize[1]);

        // moonlight stuff
        JsonValue light = moonlightData.get("lighting");
        float[] color = light.get("color").asFloatArray();
        float dist  = light.getFloat("distance");
        int rays = light.getInt("rays");

        // board layout stuff
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                int tileNum = tileData.get((board.getHeight() - y - 1) * board.getWidth() + x);
                board.setTileTexture(x, y,
                        directory.getEntry(texType + tileNum + "-unlit", Texture.class),
                        directory.getEntry(texType + tileNum + "-lit", Texture.class)
                );
                board.setTileType(x, y, tileTypeFromNum(tileNum));
                board.setWalkable(x, y, true);
                PointLight point = new PointLight(
                        rayHandler, rays, Color.WHITE, dist,
                        board.boardCenterToWorld(x, y).x, board.boardCenterToWorld(x, y).y
                );
                point.setColor(color[0],color[1],color[2],color[3]);
                point.setSoft(light.getBoolean("soft"));
                board.setSpotlight(x, y, point);
                board.setLit(x, y, false);
            }
        }

        for (JsonValue moonlightPos : moonlightData.get("positions")) {
            int t_x = moonlightPos.get(0).asInt();
            int t_y = moonlightPos.get(1).asInt();

            board.setLit(t_x, t_y, true);
        }

        return board;
    }


    /**
     * Creates the ambient lighting for the level
     *
     * @param  light the JSON tree defining the light
     */
    private void parseLighting(JsonValue light, RayHandler rayhandler) {
        RayHandler.setGammaCorrection(light.getBoolean("gamma"));
        RayHandler.useDiffuseLight(light.getBoolean("diffuse"));

        float[] color = light.get("color").asFloatArray();
        rayhandler.setAmbientLight(color[0], color[1], color[2], color[3]);
        int blur = light.getInt("blur");
        rayhandler.setBlur(blur > 0);
        rayhandler.setBlurNum(blur);
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
