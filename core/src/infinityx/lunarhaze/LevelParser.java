package infinityx.lunarhaze;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class LevelParser {

    /**
     * Creates a level given a json value.
     * Json value formatted as in assets/levels.json.
     *
     * @param json
     */
    public LevelContainer loadData(JsonValue json, int level) {
        JsonValue levelContents = json.get(String.valueOf(level));
        JsonValue scene = levelContents.get("scene");


        LevelContainer levelContainer = new LevelContainer(json.);
        levelContainer.
        json.get();
        return null;
    }
}
