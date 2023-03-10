package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;

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

    Texture playerTexture, enemyTexture;

    /**
     * tileTextures[2*x], tileTextures[2*x+1] are the unlit and lit textures for tile numbered (x+1) respectively
     **/
    Array<Texture> tileTextures = new Array<Texture>();

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
     * Caches all textures from directory.
     *
     * @param directory asset manager holding list of textures
     */
    public void loadTextures(AssetDirectory directory) {
        if (!directory.isFinished()) {
            throw new RuntimeException("Directory has not finished loaded!!!");
        }

        // Get all textures
        playerTexture = directory.getEntry("werewolf", Texture.class);
        enemyTexture = directory.getEntry("villager", Texture.class);

        for (int i = 1; i <= 6; i++) {
            tileTextures.add(directory.getEntry("land" + i + "-unlit", Texture.class));
            tileTextures.add(directory.getEntry("land" + i + "-lit", Texture.class));
        }
    }

    /**
     * Creates a level given a json value.
     * Json value formatted as in assets/levels.json.
     *
     * @param levelContents json value holding level layout
     */
    public LevelContainer loadData(JsonValue levelContents) {
        // LevelContainer empty at this point
        LevelContainer levelContainer = new LevelContainer();

        // Generate board
        JsonValue tiles = levelContents.get("tiles");
        JsonValue scene = levelContents.get("scene");

        IntArray tileData = new IntArray();
        int numRows = 0;

        for (JsonValue row : tiles) {
            tileData.addAll(row.asIntArray());
            numRows++;
        }
        //Initializes moonlight info
        int numRows2 = 0;
        JsonValue moonlight = scene.get("moonlight");
        IntArray moonlightData = new IntArray();
        for (JsonValue lit : moonlight) {
            moonlightData.addAll(lit.asIntArray());
            numRows2++;
        }

        // TODO: change if we do not want to assume board is square
        // int numCols = tileData.size / numRows;

        Board board = new Board(numRows, numRows);
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                int tileNum = tileData.get((board.getHeight() - y - 1) * board.getWidth() + x);
                board.setTileTexture(x, y, tileTextures.get((tileNum - 1) * 2), tileTextures.get((tileNum - 1) * 2 + 1));
                boolean moonInfo = (moonlightData.get((board.getHeight() - y - 1) * board.getWidth() + x) == 1);
                if (moonInfo) levelContainer.addMoonlight();
                board.setLit(x, y, moonInfo);
                board.setTileType(x, y, tileTypeFromNum(tileNum));
                board.setWalkable(x, y, true);
            }
        }

        levelContainer.setBoard(board);

        // Generate player
        JsonValue player = scene.get("player");
        Vector2 playerWorldPos = getWorldPosition(board, player);

        Werewolf playerModel = new Werewolf(playerWorldPos.x, playerWorldPos.y);
        playerModel.setTexture(playerTexture);

        levelContainer.setPlayer(playerModel);

        // Generate enemies
        JsonValue enemies = scene.get("enemies");
        int curId = 0;
        while (true) {
            JsonValue enemy = enemies.get(curId);
            if (enemy == null) break;

            JsonValue enemyPos = enemy.get("position");
            Vector2 enemyWorldPos = getWorldPosition(board, enemyPos);
            System.out.println("enemyPos: " + enemyPos);

            ArrayList<Vector2> patrol = new ArrayList<>();
            for (JsonValue patrolPos : enemy.get("patrol")) {
                patrol.add(new Vector2(patrolPos.getInt(0), patrolPos.getInt(1)));
            }

            Enemy enemyModel = new Enemy(curId, enemyWorldPos.x, enemyWorldPos.y, patrol);
            enemyModel.setTexture(enemyTexture);

            levelContainer.addEnemy(enemyModel);
            curId++;
        }
        // TODO JsonValue objects = scene.get("objects");
        return levelContainer;
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

    /**
     * Retrieves the world position from Json entry
     *
     * @param board
     * @param entry Json entry holding postion, assumed to be a 2-element array
     * @return center world position
     */
    private Vector2 getWorldPosition(Board board, JsonValue entry) {
        System.out.println("LevelParser GetWorldPosition");
        int boardX = entry.getInt(0);
        int boardY = entry.getInt(1);
        System.out.printf("X: %d, Y: %d, width: %d, height: %d\n", boardX, boardY, board.getWidth(), board.getHeight());

        return board.getTilePosition(boardX, boardY);
    }
}
