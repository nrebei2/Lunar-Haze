package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.Board;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Enemy;
import infinityx.lunarhaze.models.entity.SceneObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.Board;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Enemy;
import infinityx.lunarhaze.models.entity.SceneObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is responsible for serializing a {@link LevelContainer} into a JSON format.
 * The resulting JSON object represents a level's configuration, including settings,
 * ambient effects, tiles, and scene elements such as player positions, enemies, and scene objects.
 * The serialized level can be saved to a file and used later for loading and rendering the level within the game.
 *
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
        // Create a JSON object to store the current level
        JsonValue currLevel = new JsonValue(JsonValue.ValueType.object);

        // Add settings to the current level
        currLevel.addChild("settings", createSettings(level));

        // Add ambient settings to the current level
        currLevel.addChild("ambient", createAmbient(level));

        // Add tiles to the current level
        currLevel.addChild("tiles", createBoard(level));

        // Add scene to the current level
        currLevel.addChild("scene", createScene(level));

        return currLevel;
    }

    /**
     * Creates a JSON object with settings for the given level.
     *
     * JSON structure:
     * {
     *   "transition": int,
     *   "phaseLength": int,
     *   "enemy-spawner": {
     *     "count": int,
     *     "add-tick": [int, int],
     *     "delay": int
     *   }
     * }
     */
    private static JsonValue createSettings(LevelContainer level) {
        // TODO: remove hardcoded stuff, should be set in editor
        JsonValue settings = new JsonValue(JsonValue.ValueType.object);
        settings.addChild("transition", new JsonValue(2));
        settings.addChild("phaseLength", new JsonValue(level.getPhaseLength()));

        JsonValue enemySpawner = new JsonValue(JsonValue.ValueType.object);
        enemySpawner.addChild("count", new JsonValue(5));
        JsonValue addTick = new JsonValue(JsonValue.ValueType.array);
        addTick.addChild(new JsonValue(500));
        addTick.addChild(new JsonValue(900));
        enemySpawner.addChild("add-tick", addTick);
        enemySpawner.addChild("delay", new JsonValue(1000));

        settings.addChild("enemy-spawner", enemySpawner);
        return settings;
    }

    /**
     * Creates a JSON object with ambient settings from the given level.
     *
     * JSON structure:
     * {
     *   "stealth-color": [float, float, float, float],
     *   "battle-color": [float, float, float, float],
     *   "gamma": boolean,
     *   "diffuse": boolean,
     *   "blur": int
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
        ambient.addChild("blur", new JsonValue(5));
        return ambient;
    }

    /**
     * Creates a JSON object with board settings from the given level.
     *
     * JSON structure:
     * {
     *   "layout": [
     *     [int, int, ...],
     *     ...
     *   ],
     *   "moonlight": {
     *     "positions": [
     *       [int, int],
     *       ...
     *     ],
     *     "lighting": {
     *       "color": [float, float, float, float],
     *       "distance": float,
     *       "rays": int,
     *       "soft": boolean
     *     }
     *   }
     * }
     */
    private static JsonValue createBoard(LevelContainer level) {
        JsonValue tiles = new JsonValue(JsonValue.ValueType.object);
        Board board = level.getBoard();

        JsonValue layout = new JsonValue(JsonValue.ValueType.array);
        for (int y = 0; y < board.getHeight(); y++) {
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
     *
     * JSON structure:
     * {
     *   "player": [float, float],
     *   "enemies": [
     *     {
     *       "type": string,
     *       "position": [int, int],
     *       "patrol": [
     *         [int, int],
     *         [int, int]
     *       ]
     *     },
     *     ...
     *   ],
     *   "objects": [
     *     {
     *       "type": string,
     *       "scale": int,
     *       "position": [int, int]
     *     },
     *     ...
     *   ]
     * }
     */
    private static JsonValue createScene(LevelContainer level) {
        JsonValue scene = new JsonValue(JsonValue.ValueType.object);

        // Player
        JsonValue playerStartPos = new JsonValue(JsonValue.ValueType.array);
        playerStartPos.addChild(new JsonValue(level.getPlayer().getPosition().x));
        playerStartPos.addChild(new JsonValue(level.getPlayer().getPosition().y));
        scene.addChild("player", playerStartPos);

        // Enemies
        JsonValue enemy = new JsonValue(JsonValue.ValueType.array);
        for (Enemy e : level.getEnemies()) {
            JsonValue currEnemy = new JsonValue(JsonValue.ValueType.object);
            currEnemy.addChild("type", new JsonValue(e.getName()));

            JsonValue pos = new JsonValue(JsonValue.ValueType.array);
            pos.addChild(new JsonValue(e.getPosition().x));
            pos.addChild(new JsonValue(e.getPosition().y));
            currEnemy.addChild("position", pos);

            JsonValue patrol = new JsonValue(JsonValue.ValueType.array);

            if (e.getPatrolPath() != null && e.getPatrolPath().size() > 0) {
                // Bottom left
                JsonValue pos1 = new JsonValue(JsonValue.ValueType.array);
                pos1.addChild(new JsonValue(e.getPatrolPath().get(0).x));
                pos1.addChild(new JsonValue(e.getPatrolPath().get(0).y));
                patrol.addChild(pos1);

                // Top right
                JsonValue pos2 = new JsonValue(JsonValue.ValueType.array);
                pos2.addChild(new JsonValue(e.getPatrolPath().get(1).x));
                pos2.addChild(new JsonValue(e.getPatrolPath().get(1).y));
                patrol.addChild(pos2);
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
     * Serializes the level to a JSON file and appends to levels.json.
     *
     * @param level the level to serialize
     */
    public static void saveBoardToJsonFile(LevelContainer level, AssetDirectory directory) {
        JsonValue levels = directory.getEntry("levels", JsonValue.class);
        if (levels == null) {
            levels = new JsonValue(JsonValue.ValueType.object);
        }
        // The size of the levels is the new index to put the created level, as levels starts at 0
        mostRecentlyCreatedLevel = levels.size;

        // Merge new level with existing levels
        JsonValue newLevel = levelToJson(level);
        levels.addChild(Integer.toString(mostRecentlyCreatedLevel), newLevel);

        // Write to assets/jsons
        // TODO: will this work with the jar?
        String fileName = "assets/jsons/levels.json";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(levels.prettyPrint(JsonWriter.OutputType.json, 10));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the index of the most recently created level. Useful for Save+Test.
     */
    public static int getMostRecent() {
        return mostRecentlyCreatedLevel;
    }
}