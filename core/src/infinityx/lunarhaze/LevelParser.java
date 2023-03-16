package infinityx.lunarhaze;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.SceneObject;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.BoxObstacle;
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

    private LevelContainer levelContainer = new LevelContainer();

    /**
     * Caches all constants (between levels) from directory
     *
     * @param directory asset manager holding list of ... assets
     * @param canvas
     */
    public void loadConstants(AssetDirectory directory, GameCanvas canvas) {
        // TODO: create and cache player and board? not sure if that would do much
        // There is not a lot constant between levels
        JsonValue boardJson = directory.getEntry("board", JsonValue.class);
        wSize = boardJson.get("tileWorldSize").asFloatArray();
        sSize = boardJson.get("tileScreenSize").asIntArray();

        canvas.setWorldToScreen(new Vector2(sSize[0] / wSize[0], sSize[1] / wSize[1]));

        enemiesJson = directory.getEntry("enemies", JsonValue.class);
        objectsJson = directory.getEntry("objects", JsonValue.class);
        playerJson = directory.getEntry("player", JsonValue.class);

        // Cache player
        levelContainer.setPlayer(parsePlayer(directory, levelContainer));

        // Cache enemies
        for (JsonValue enemy : enemiesJson) {
            System.out.printf("Parsing %s\n", enemy.name());
            levelContainer.cacheEnemies.put(enemy.name(), parseEnemy(directory, levelContainer, enemy.name()));
        }

        // Cache scene objects
        for (JsonValue object : enemiesJson) {
            System.out.printf("Parsing %s\n", object.name());
            levelContainer.cacheSceneObj.put(object.name(), parseSceneObject(directory, levelContainer, object.name()));
        }
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

        // Generate enemies
        JsonValue enemies = scene.get("enemies");
        int curId = 0;
        while (true) {
            JsonValue enemyInfo = enemies.get(curId);
            if (enemyInfo == null) break;

            Enemy enemy = levelContainer.cacheEnemies.get(enemyInfo.getString("type")).deepClone(levelContainer);
            enemy.setId(curId);
            JsonValue enemyPos = enemyInfo.get("position");

            ArrayList<Vector2> patrol = new ArrayList<>();
            for (JsonValue patrolPos : enemyInfo.get("patrol")) {
                patrol.add(new Vector2(patrolPos.getInt(0), patrolPos.getInt(1)));
            }
            enemy.setPatrolPath(patrol);
            enemy.setPosition(enemyPos.getFloat(0), enemyPos.getFloat(1));

            levelContainer.addEnemy(enemy);
            curId++;
        }

        // Generate scene objects
        JsonValue objects = scene.get("objects");
        int objId = 0;
        while (true) {
            JsonValue objInfo = objects.get(objId);
            if (objInfo == null) break;

            SceneObject object = levelContainer.cacheSceneObj.get(objInfo.getString("type")).deepClone(levelContainer);
            JsonValue objPos = objInfo.get("position");
            object.setPosition(objPos.getFloat(0), objPos.getFloat(1));

            float objScale = objInfo.getFloat("scale");
            object.setScale(objScale, objScale);

            levelContainer.addSceneObject(object);
            objId++;
        }
        return levelContainer;
    }

    /**
     * Creates an empty level to be filled in the level editor.
     * You gotta call loadConstants before calling this method.
     */
    public LevelContainer loadEmpty() {
        // LevelContainer empty at this point
        levelContainer.flush();

        Board board = new Board(10, 10);

        board.setTileScreenDim(sSize[0], sSize[1]);
        board.setTileWorldDim(wSize[0], wSize[1]);

        levelContainer.setBoard(board);

        return levelContainer;
    }

    /**
     * Creates a general scene object to be placed in level
     *
     * @param name the name of the scene object (e.g. tree)
     */
    private SceneObject parseSceneObject(AssetDirectory directory, LevelContainer container, String name) {
        JsonValue json = objectsJson.get(name);
        SceneObject object = new SceneObject();
        parseGameObject(object, directory, json);

        object.activatePhysics(container.getWorld());

        return object;
    }

    /**
     * Further parses specific GameObject (collider info, etc.) attributes.
     *
     * @param json Json tree holding information
     */
    private void parseGameObject(GameObject object, AssetDirectory directory, JsonValue json) {
        JsonValue p_dim = json.get("collider");
        object.setDimension(p_dim.get("width").asFloat(), p_dim.get("height").asFloat());

        // TODO: bother with error checking?
        object.setBodyType(json.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        object.setLinearDamping(json.get("damping").asFloat());
        object.setDensity(json.get("density").asFloat());
        object.setFriction(json.get("friction").asFloat());
        object.setRestitution(json.get("restitution").asFloat());
        if (!json.has("speed")) {
            object.setSpeed(0);
        } else {
            object.setSpeed(json.get("speed").asFloat());
        }
        //object.setStartFrame(json.get("startframe").asInt());
        JsonValue texInfo = json.get("texture");
        object.setTexture(directory.getEntry(texInfo.get("name").asString(), FilmStrip.class));
        int[] texOrigin = texInfo.get("origin").asIntArray();
        object.setOrigin(texOrigin[0], texOrigin[1]);
        if (texInfo.has("positioned")) {
            object.setPositioned(
                    texInfo.getString("positioned").equals("bottom-left") ?
                            BoxObstacle.POSITIONED.BOTTOM_LEFT : BoxObstacle.POSITIONED.CENTERED
            );
        }
    }

    /**
     * Creates a general player with dummy position
     *
     * @param container LevelContainer which this player is placed in
     */
    private Werewolf parsePlayer(AssetDirectory directory, LevelContainer container) {
        Werewolf player = new Werewolf();
        System.out.printf("after init: (%f, %f)\n", player.getPosition().x, player.getPosition().y);

        parseGameObject(player, directory, playerJson);

        JsonValue light = playerJson.get("spotlight");
        float[] color = light.get("color").asFloatArray();
        float dist = light.getFloat("distance");
        int rays = light.getInt("rays");

        float health = playerJson.getFloat("health");
        float lockout = playerJson.getFloat("lockout");

        player.initHp(health);
        player.initLockout(lockout);

        PointLight spotLight = new PointLight(
                container.getRayHandler(), rays, Color.WHITE, dist,
                0, 0
        );
        spotLight.setColor(color[0], color[1], color[2], color[3]);
        spotLight.setSoft(light.getBoolean("soft"));
        player.activatePhysics(container.getWorld());
        player.setSpotLight(spotLight);

        return player;
    }

    /**
     * Creates an enemy given name. Attributed are fetched from enemies.json.
     *
     * @param name the name of the enemy as specified in assets
     */
    private Enemy parseEnemy(AssetDirectory directory, LevelContainer container, String name) {
        Enemy enemy = new Enemy();
        JsonValue json = enemiesJson.get(name);
        parseGameObject(enemy, directory, json);

        enemy.activatePhysics(container.getWorld());

        JsonValue light = json.get("flashlight");
        float[] color = light.get("color").asFloatArray();
        float dist = light.getFloat("distance");
        int rays = light.getInt("rays");
        float degrees = light.getFloat("degrees");

        ConeSource flashLight = new ConeSource(
                container.getRayHandler(), rays, Color.WHITE, dist,
                enemy.getX(), enemy.getY(), 0f, degrees
        );
        flashLight.setColor(color[0], color[1], color[2], color[3]);
        flashLight.setSoft(light.getBoolean("soft"));

        enemy.setFlashlight(flashLight);
        enemy.setFlashlightOn(true);

        JsonValue attack = json.get("attack");
        enemy.setAttackKnockback(attack.getFloat("knockback"));
        enemy.setAttackDamage(attack.getFloat("damage"));

        return enemy;
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

        // TODO: change if we do not want to assume board is square
        // int numCols = tileData.size / numRows;

        Board board = new Board(numRows, numRows);

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
                        directory.getEntry(texType + tileNum + "-unlit", Texture.class),
                        directory.getEntry(texType + tileNum + "-lit", Texture.class),
                        // currently collected tile is same as uncollected ones
                        // since we have no assets for collected but lit tiles
                        directory.getEntry(texType + tileNum + "-lit", Texture.class)
                );
                board.setTileType(x, y, tileTypeFromNum(tileNum));
                board.setWalkable(x, y, true);
                PointLight point = new PointLight(
                        rayHandler, rays, Color.WHITE, dist,
                        board.boardCenterToWorld(x, y).x, board.boardCenterToWorld(x, y).y
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
