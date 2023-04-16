package infinityx.lunarhaze;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.SceneObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class LevelSerializer {

    private static String levelToJson(LevelContainer level, Board board) {
        JsonValue root = new JsonValue(JsonValue.ValueType.object);

        JsonValue currLevel = new JsonValue(JsonValue.ValueType.object);

        JsonValue settings = new JsonValue(JsonValue.ValueType.object);
        settings.addChild("transition", new JsonValue(2));
        settings.addChild("phaseLength", new JsonValue(2));

        JsonValue battlecolor = new JsonValue(JsonValue.ValueType.array);
        float battleArray[] = level.getStealthAmbience();
        battlecolor.addChild(new JsonValue(battleArray[0]));
        battlecolor.addChild(new JsonValue(battleArray[1]));
        battlecolor.addChild(new JsonValue(battleArray[2]));
        battlecolor.addChild(new JsonValue(battleArray[3]));

        settings.addChild("battle-ambiance", battlecolor);

        JsonValue enemySpawner = new JsonValue(JsonValue.ValueType.object);
        enemySpawner.addChild("count", new JsonValue(5));
        JsonValue addTick = new JsonValue(JsonValue.ValueType.array);
        addTick.addChild(new JsonValue(500));
        addTick.addChild(new JsonValue(900));
        enemySpawner.addChild("add-tick", addTick);
        enemySpawner.addChild("delay", new JsonValue(1000));

        settings.addChild("enemy-spawner", enemySpawner);
        currLevel.addChild("settings", settings);

        JsonValue ambient = new JsonValue(JsonValue.ValueType.object);
        JsonValue color = new JsonValue(JsonValue.ValueType.array);
        float ambientArray[] = level.getStealthAmbience();
        color.addChild(new JsonValue(ambientArray[0]));
        color.addChild(new JsonValue(ambientArray[1]));
        color.addChild(new JsonValue(ambientArray[2]));
        color.addChild(new JsonValue(ambientArray[3]));
        ambient.addChild("color", color);
        ambient.addChild("gamma", new JsonValue(true));
        ambient.addChild("diffuse", new JsonValue(true));
        ambient.addChild("blur", new JsonValue(5));
        ambient.addChild("transition", new JsonValue(12));
        currLevel.addChild("ambient", ambient);


        JsonValue tiles = new JsonValue(JsonValue.ValueType.object);
        tiles.addChild("type", new JsonValue("grass"));

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
        moonlightColor.addChild(new JsonValue(0.7f));
        moonlightColor.addChild(new JsonValue(0.7f));
        moonlightColor.addChild(new JsonValue(0.9f));
        moonlightColor.addChild(new JsonValue(0.7f));
        lighting.addChild("color", moonlightColor);
        lighting.addChild("distance", new JsonValue(4));
        lighting.addChild("rays", new JsonValue(10));
        lighting.addChild("soft", new JsonValue(false));

        moonlight.addChild("lighting", lighting);

        tiles.addChild("moonlight", moonlight);

        currLevel.addChild("tiles", tiles);

        JsonValue scene = new JsonValue(JsonValue.ValueType.object);

        // Player
        JsonValue playerStartPos = new JsonValue(JsonValue.ValueType.array);
        if (level.getPlayerStartPos() != null) {
            playerStartPos.addChild(new JsonValue(level.getPlayerStartPos()[0]));
            playerStartPos.addChild(new JsonValue(level.getPlayerStartPos()[1]));
        }
        scene.addChild("player", playerStartPos);

        // Enemy (todo)
        JsonValue enemy = new JsonValue(JsonValue.ValueType.array);
        for(Enemy e : level.getEnemies()) {
            JsonValue currEnemy = new JsonValue(JsonValue.ValueType.object);
            currEnemy.addChild("type", new JsonValue(e.getName()));
            JsonValue pos = new JsonValue(JsonValue.ValueType.array);
            pos.addChild(new JsonValue(board.worldToBoardX(e.getPosition().x)));
            pos.addChild(new JsonValue(board.worldToBoardY(e.getPosition().y)));
            currEnemy.addChild("position", pos);

            JsonValue patrol = new JsonValue(JsonValue.ValueType.array);


            if(e.getPatrolPath() != null && e.getPatrolPath().size() > 0) {
                // Tile 1
                JsonValue pos1 = new JsonValue(JsonValue.ValueType.array);
                pos1.addChild(new JsonValue(board.worldToBoardX(e.getPatrolPath().get(0).x)));
                pos1.addChild(new JsonValue(board.worldToBoardY(e.getPatrolPath().get(0).y)));
                patrol.addChild(pos1);

                // Tile 2
                JsonValue pos2 = new JsonValue(JsonValue.ValueType.array);
                pos2.addChild(new JsonValue(board.worldToBoardX(e.getPatrolPath().get(1).x)));
                pos2.addChild(new JsonValue(board.worldToBoardY(e.getPatrolPath().get(1).y)));
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
            currObj.addChild("scale", new JsonValue(1));
            JsonValue objPos = new JsonValue(JsonValue.ValueType.array);
            objPos.addChild(new JsonValue(board.worldToBoardX(obj.getPosition().x)));
            objPos.addChild(new JsonValue(board.worldToBoardY(obj.getPosition().y)));
            currObj.addChild("position", objPos);
            objects.addChild(currObj);
        }
        scene.addChild("objects", objects);

        currLevel.addChild("scene", scene);
        root.addChild("1", currLevel);

        return root.prettyPrint(JsonWriter.OutputType.json, 10);
    }

    public static void saveBoardToJsonFile(LevelContainer level, Board board, String name) {
        String jsonString = levelToJson(level, board);
        String fileName = "assets/jsons/" + name + ".json";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
