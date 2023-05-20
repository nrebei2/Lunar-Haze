package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.Billboard;
import infinityx.lunarhaze.models.Board;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Enemy;
import infinityx.lunarhaze.models.entity.SceneObject;

import java.util.ArrayList;

/**
 * This class is responsible for serializing a {@link LevelContainer} into a JSON format.
 * The resulting JSON object represents a level's configuration, including information for settings,
 * ambient lighting, board, and scene elements including the player, enemies, and scene objects.
 * The serialized level can be saved to a file and used later for loading and rendering the level within the game.
 * <p>
 * The reverse of {@link LevelParser}.
 */
public class LevelSerializer {

    /**
     * index of the most recently created level. Useful for Save+Test
     */
    private static int mostRecentlyCreatedLevel;

    /**
     * Converts the given level to a JSON object.
     */
    private static JsonValue levelToJson(LevelContainer level) {
        JsonValue currLevel = new JsonValue(JsonValue.ValueType.object);

        currLevel.addChild("settings", createSettings(level));
        currLevel.addChild("ambient", createAmbient(level));
        currLevel.addChild("tiles", createBoard(level));
        currLevel.addChild("scene", createScene(level));

        return currLevel;
    }

    /**
     * Creates a JSON object with settings for the given level.
     * <p>
     * JSON structure:
     * {
     * "transition": int,
     * "phaseLength": int,
     * "shadow-shear": float,
     * "shadow-scale": float,
     * "enemy-spawner": {
     * "count": int,
     * "add-tick": [float, float],
     * "delay": int,
     * "spawn-locations": [[int, int], ...]
     * }
     * }
     */
    private static JsonValue createSettings(LevelContainer level) {
        JsonValue settings = new JsonValue(JsonValue.ValueType.object);
        settings.addChild("transition", new JsonValue(4));
        settings.addChild("phaseLength", new JsonValue(level.getSettings().getPhaseLength()));
        settings.addChild("shadow-shear", new JsonValue(level.getSettings().getShadowShear()));
        settings.addChild("shadow-scale", new JsonValue(level.getSettings().getShadowScale()));

        JsonValue enemySpawner = new JsonValue(JsonValue.ValueType.object);
        enemySpawner.addChild("villager-count", new JsonValue(level.getSettings().getVillagerCount()));
        enemySpawner.addChild("archer-count", new JsonValue(level.getSettings().getArcherCount()));
        JsonValue addTick = new JsonValue(JsonValue.ValueType.array);
        addTick.addChild(new JsonValue(level.getSettings().getSpawnRateMin()));
        addTick.addChild(new JsonValue(level.getSettings().getSpawnRateMax()));
        enemySpawner.addChild("add-tick", addTick);
        enemySpawner.addChild("delay", new JsonValue(level.getSettings().getDelay()));

        JsonValue spawnLocations = new JsonValue(JsonValue.ValueType.array);
        for (Vector2 loc : level.getSettings().getSpawnLocations()) {
            JsonValue location = new JsonValue(JsonValue.ValueType.array);
            location.addChild(new JsonValue(loc.x));
            location.addChild(new JsonValue(loc.y));
            spawnLocations.addChild(location);
        }

        enemySpawner.addChild("spawn-locations", spawnLocations);

        settings.addChild("enemy-spawner", enemySpawner);
        return settings;
    }

    /**
     * Creates a JSON object with ambient settings from the given level.
     * <p>
     * JSON structure:
     * {
     * "stealth-color": [float, float, float, float],
     * "battle-color": [float, float, float, float],
     * "gamma": boolean,
     * "diffuse": boolean,
     * }
     */
    private static JsonValue createAmbient(LevelContainer level) {
        JsonValue ambient = new JsonValue(JsonValue.ValueType.object);

        JsonValue stealthColor = new JsonValue(JsonValue.ValueType.array);
        float ambientArray[] = level.getStealthAmbience();
        stealthColor.addChild(new JsonValue(ambientArray[0]));
        stealthColor.addChild(new JsonValue(ambientArray[1]));
        stealthColor.addChild(new JsonValue(ambientArray[2]));
        stealthColor.addChild(new JsonValue(ambientArray[3]));
        ambient.addChild("stealth-color", stealthColor);

        JsonValue battleColor = new JsonValue(JsonValue.ValueType.array);
        float[] battleArray = level.getBattleAmbience();
        battleColor.addChild(new JsonValue(battleArray[0]));
        battleColor.addChild(new JsonValue(battleArray[1]));
        battleColor.addChild(new JsonValue(battleArray[2]));
        battleColor.addChild(new JsonValue(battleArray[3]));
        ambient.addChild("battle-color", battleColor);

        ambient.addChild("gamma", new JsonValue(true));
        ambient.addChild("diffuse", new JsonValue(true));
        return ambient;
    }

    /**
     * Creates a JSON object with board settings from the given level.
     * <p>
     * JSON structure:
     * {
     * "layout": [
     * [int, int, ...],
     * ...
     * ],
     * "moonlight": {
     * "positions": [
     * [int, int],
     * ...
     * ],
     * "lighting": {
     * "color": [float, float, float, float],
     * "distance": float,
     * "rays": int,
     * "soft": boolean
     * }
     * }
     * }
     */
    private static JsonValue createBoard(LevelContainer level) {
        JsonValue tiles = new JsonValue(JsonValue.ValueType.object);
        Board board = level.getBoard();

        JsonValue layout = new JsonValue(JsonValue.ValueType.array);
        for (int y = board.getHeight() - 1; y >= 0; y--) {
            JsonValue row = new JsonValue(JsonValue.ValueType.array);
            for (int x = 0; x < board.getWidth(); x++) {
                row.addChild(new JsonValue(board.getTileNum(x, y)));
            }
            layout.addChild(row);
        }
        tiles.addChild("layout", layout);

        JsonValue moonlight = new JsonValue(JsonValue.ValueType.object);
        JsonValue positions = new JsonValue(JsonValue.ValueType.array);
        ArrayList<int[]> moonlightPositions = board.getMoonlightTiles();
        for (int[] position : moonlightPositions) {
            JsonValue pos = new JsonValue(JsonValue.ValueType.array);
            pos.addChild(new JsonValue(position[0]));
            pos.addChild(new JsonValue(position[1]));
            positions.addChild(pos);
        }
        moonlight.addChild("positions", positions);

        JsonValue lighting = new JsonValue(JsonValue.ValueType.object);
        JsonValue moonlightColor = new JsonValue(JsonValue.ValueType.array);
        float[] moonlightArray = level.getMoonlightColor();
        moonlightColor.addChild(new JsonValue(moonlightArray[0]));
        moonlightColor.addChild(new JsonValue(moonlightArray[1]));
        moonlightColor.addChild(new JsonValue(moonlightArray[2]));
        moonlightColor.addChild(new JsonValue(moonlightArray[3]));
        lighting.addChild("color", moonlightColor);

        lighting.addChild("distance", new JsonValue(2.5f));
        lighting.addChild("rays", new JsonValue(40));
        lighting.addChild("soft", new JsonValue(true));
        moonlight.addChild("lighting", lighting);

        tiles.addChild("moonlight", moonlight);

        return tiles;
    }

    /**
     * Creates a JSON object with scene settings from the given level.
     * <p>
     * JSON structure:
     * {
     * "player": [float, float],
     * "enemies": [
     * {
     * "type": string,
     * "scale": float
     * "position": [int, int],
     * "patrol": [
     * [int, int],
     * [int, int],
     * ...
     * ]
     * },
     * ...
     * ],
     * "objects": [
     * {
     * "type": string,
     * "scale": float,
     * "flip": boolean
     * "position": [int, int]
     * },
     * ...
     * ]
     * }
     */
    private static JsonValue createScene(LevelContainer level) {
        JsonValue scene = new JsonValue(JsonValue.ValueType.object);

        // Player
        JsonValue playerStartPos = new JsonValue(JsonValue.ValueType.array);
        playerStartPos.addChild(new JsonValue(level.getPlayer().getPosition().x));
        playerStartPos.addChild(new JsonValue(level.getPlayer().getPosition().y));
        scene.addChild("player", playerStartPos);

        // Tutorial boards
        JsonValue boards = new JsonValue(JsonValue.ValueType.array);
        for (Billboard billboard : level.getBillboards()) {
            JsonValue currBoard = new JsonValue(JsonValue.ValueType.object);
            currBoard.addChild("type", new JsonValue(billboard.getName()));

            currBoard.addChild("scale", new JsonValue(billboard.getScale()));
            JsonValue pos = new JsonValue(JsonValue.ValueType.array);
            pos.addChild(new JsonValue(billboard.getPosition().x));
            pos.addChild(new JsonValue(billboard.getPosition().y));
            pos.addChild(new JsonValue(billboard.getPosition().z));
            currBoard.addChild("position", pos);

            boards.addChild(currBoard);
        }
        scene.addChild("billboards", boards);

        // Enemies
        JsonValue enemy = new JsonValue(JsonValue.ValueType.array);
        for (Enemy e : level.getEnemies()) {
            JsonValue currEnemy = new JsonValue(JsonValue.ValueType.object);
            currEnemy.addChild("type", new JsonValue(e.getEnemyType().toString()));

            currEnemy.addChild("scale", new JsonValue(e.getScale()));
            JsonValue pos = new JsonValue(JsonValue.ValueType.array);
            pos.addChild(new JsonValue(e.getPosition().x));
            pos.addChild(new JsonValue(e.getPosition().y));
            currEnemy.addChild("position", pos);

            JsonValue patrol = new JsonValue(JsonValue.ValueType.array);
            for (Vector2 loc : e.getPatrolPath().getPath()) {
                JsonValue location = new JsonValue(JsonValue.ValueType.array);
                location.addChild(new JsonValue(loc.x));
                location.addChild(new JsonValue(loc.y));
                patrol.addChild(location);
            }

            currEnemy.addChild("patrol", patrol);
            enemy.addChild(currEnemy);
        }
        scene.addChild("enemies", enemy);

        // Scene objects
        JsonValue objects = new JsonValue(JsonValue.ValueType.array);
        for (SceneObject obj : level.getSceneObjects()) {
            JsonValue currObj = new JsonValue(JsonValue.ValueType.object);

            currObj.addChild("type", new JsonValue(obj.getSceneObjectType()));
            currObj.addChild("scale", new JsonValue(obj.getScale()));
            currObj.addChild("flip", new JsonValue(obj.isFlipped()));

            JsonValue objPos = new JsonValue(JsonValue.ValueType.array);
            objPos.addChild(new JsonValue(obj.getPosition().x));
            objPos.addChild(new JsonValue(obj.getPosition().y));
            currObj.addChild("position", objPos);

            objects.addChild(currObj);
        }
        scene.addChild("objects", objects);

        return scene;
    }

    /**
     * Serializes the given level to a JSON file and saves it to the specified level index in levels.json.
     * If the level index already exists and force is set to true, the existing level will be overwritten.
     * If force is set to false and the level index already exists, the function will return false and not save the level.
     *
     * @param levelContainer the level to serialize
     * @param directory      the asset directory containing the levels.json file
     * @param level          the level index to save the serialized level to
     * @param force          if true, overwrite an existing level at the specified index; if false, do not save if a level exists at the index
     * @return true if the level was successfully saved, false otherwise
     */
    public static boolean saveLevel(LevelContainer levelContainer, AssetDirectory directory, int level, boolean force) {
        JsonValue levels = directory.getEntry("levels", JsonValue.class);

        mostRecentlyCreatedLevel = level;
        String levelIndexStr = Integer.toString(level);
        if (levels.hasChild(levelIndexStr)) {
            if (!force) {
                return false;
            }
        }
        // Merge new level with existing levels
        JsonValue newLevel = levelToJson(levelContainer);
        JsonValue updatedLevels = new JsonValue(JsonValue.ValueType.object);
        boolean levelInserted = false;

        // Cache children to avoid mutation issues during iteration
        Array<JsonValue> cachedChildren = new Array<>(levels.size);
        for (JsonValue child : levels) {
            cachedChildren.add(child);
        }

        // Add levels in sorted order
        for (JsonValue child : cachedChildren) {
            int curLevel = Integer.valueOf(child.name);

            if (level == curLevel) {
                // If key already exists, overwrite the value
                // Safe since at this point we are forcing
                updatedLevels.addChild(child.name, newLevel);
                levelInserted = true;
                continue;
            } else if (level < curLevel && !levelInserted) {
                // Insert the new level before the current child
                updatedLevels.addChild(levelIndexStr, newLevel);
                levelInserted = true;
            }

            updatedLevels.addChild(child.name, child);
            // Clear the next reference to avoid circular references
            child.next = null;
        }

        // If the new key is the largest, add it at the end
        if (!levelInserted) {
            updatedLevels.addChild(levelIndexStr, newLevel);
        }

        // Mutate the actual asset entry itself
        // I do this so the levelFormat in GameMode updates too
        levels.child = updatedLevels.child;
        levels.size = updatedLevels.size;

        // Write back to levels.json
        FileHandle outputFile = Gdx.files.local("save-data/levels.json");
        System.out.println(outputFile.file().getAbsolutePath());
        try {
            outputFile.writeString(levels.prettyPrint(JsonWriter.OutputType.json, 10), false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Returns the index of the most recently created level. Useful for Save+Test.
     */
    public static int getMostRecent() {
        return mostRecentlyCreatedLevel;
    }
}