package infinityx.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ShaderProgramLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * This class parses a JSON entry into a {@link ShaderProgram}.
 * <p>
 * Shaders are defined inside of a shader entry. The name of the shader entry is the name to use
 * when retrieving from the asset directory. Currently, they have the following entries and are REQUIRED:
 * <p>
 * * "vert": The source of the vertex shader
 * * "frag": The source of the fragment shader
 * <p>
 */
public class ShaderParser implements AssetParser<ShaderProgram> {
    /**
     * The parent of the current shader
     */
    private JsonValue root;

    @Override
    public Class<ShaderProgram> getType() {
        return ShaderProgram.class;
    }

    @Override
    public void reset(JsonValue directory) {
        root = directory;
        root = root.getChild("shaders");
    }

    @Override
    public boolean hasNext() {
        return root != null;
    }

    @Override
    public void processNext(AssetManager manager, ObjectMap<String, String> keymap) {
        ShaderProgramLoader.ShaderProgramParameter params = new ShaderProgramLoader.ShaderProgramParameter();

        params.fragmentFile = root.getString("frag");
        params.vertexFile = root.getString("vert");

        manager.load(root.name(), ShaderProgram.class, params);
        root = root.next();
    }
}
