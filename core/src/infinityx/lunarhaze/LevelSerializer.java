package infinityx.lunarhaze;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
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
        battlecolor.addChild(new JsonValue(0.8f));
        battlecolor.addChild(new JsonValue(0.7f));
        battlecolor.addChild(new JsonValue(1f));
        battlecolor.addChild(new JsonValue(1f));

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
        color.addChild(new JsonValue(0.47f));
        color.addChild(new JsonValue(0.35f));
        color.addChild(new JsonValue(0.55f));
        color.addChild(new JsonValue(0.55f));
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
        ArrayList<Integer[]> moonlightPositions = board.getMoonlightTiles();
        for (Integer[] position : moonlightPositions) {
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
        lighting.addChild("soft", new JsonValue(true));

        moonlight.addChild("lighting", lighting);

        tiles.addChild("moonlight", moonlight);

        currLevel.addChild("tiles", tiles);

        JsonValue scene = new JsonValue(JsonValue.ValueType.object);

        // Player
        JsonValue playerStartPos = new JsonValue(JsonValue.ValueType.array);
        if(level.getPlayerStartPos() != null) {
            playerStartPos.addChild(new JsonValue(level.getPlayerStartPos()[0]));
            playerStartPos.addChild(new JsonValue(level.getPlayerStartPos()[1]));
        }
        scene.addChild("player", playerStartPos);

        // Enemy (todo)

        // Scene objects
        JsonValue objects = new JsonValue(JsonValue.ValueType.array);
        for(SceneObject obj : level.getSceneObjects()) {
            JsonValue currObj = new JsonValue(JsonValue.ValueType.object);
            currObj.addChild("type", new JsonValue(obj.getSceneObjectType()));
            currObj.addChild("scale", new JsonValue(1));
            JsonValue objPos = new JsonValue(JsonValue.ValueType.array);
            objPos.addChild(new JsonValue(board.worldToBoardX(obj.getPosition().x)));
            objPos.addChild(new JsonValue(board.worldToBoardY(obj.getPosition().y)));
            currObj.addChild("position", objPos);
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
