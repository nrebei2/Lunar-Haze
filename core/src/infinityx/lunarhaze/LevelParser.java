package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;
import org.w3c.dom.ranges.RangeException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;

public class LevelParser {
    /**
     * Creates a level given a json value.
     * Json value formatted as in assets/levels.json.
     *
     * @param json
     */


    public LevelContainer loadData(AssetDirectory directory, int level) {
        // Gets json data from directory
        JsonValue json = directory.getEntry( "levels", JsonValue.class );

        // Get all textures
        Texture playerTexture = directory.getEntry("werewolf", Texture.class);
        Texture enemyTexture = directory.getEntry("villager", Texture.class);
        /** tileTextures[2*x], tileTextures[2*x+1] are the unlit and lit textures for tile x respectively **/
        Array<Texture> tileTextures = new Array<Texture>();

        for (int i = 1; i <= 6; i++) {
            tileTextures.add(directory.getEntry("land"+i+"-unlit", Texture.class));
            tileTextures.add(directory.getEntry("land"+i+"-lit", Texture.class));
        }

        // LevelContainer empty at this point
        LevelContainer levelContainer = new LevelContainer();
        JsonValue levelContents = json.get(String.valueOf(level));
        // Generate board
        JsonValue tiles = levelContents.get("tiles");
        IntArray tileData = new IntArray();
        int numRows = 0;

        for (JsonValue row : tiles) {
            tileData.addAll(row.asIntArray());
            numRows++;
        }

        // TODO: change if we do not want to assume board is square
        // int numCols = tileData.size / numRows;

        Board board = new Board(numRows, numRows);
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                int tileNum = tileData.get((board.getHeight() - y - 1)*board.getWidth() + x);
                board.setTileTexture(x, y, tileTextures.get(tileNum*2), tileTextures.get(tileNum*2+1));
                board.setLit(x, y, true);
                board.setTileType(x, y, tileTypeFromNum(tileNum));
                board.setWalkable(x, y, true);
            }
        }

        levelContainer.setBoard(board);

        JsonValue scene = levelContents.get("scene");

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
     *
     * @param num
     * @return Type
     */
    private Tile.TileType tileTypeFromNum(int num) {
        switch (num) {
            case 1:
            case 2:
            case 3: return Tile.TileType.Grass;
            case 4: return Tile.TileType.Road;
            case 5:
            case 6: return Tile.TileType.Dirt;
            default: return Tile.TileType.Grass;
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
