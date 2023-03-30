package infinityx.lunarhaze.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/** Represents a uniform in a shader file */
public class ShaderUniform {
    private final String name;
    private float[] values;

    /**
     * Creates a ShaderUniform that can be applied to a shader.
     * @param name of attribute. Must match name in shader.
     */
    public ShaderUniform(String name) {
        this.name = name;
    }

    /**
     * @param values values to be passed into attribute. Must be between 1-4.
     */
    public void setValues(float... values) {
        if (values.length > 4 || values.length == 0) {
            Gdx.app.error("ShaderUniform", "number of values must be between 1-4");
        }
        this.values = values;
    }

    /**
     * Applies the uniform to the given shader. Note the shader must be bound.
     */
    public void apply(ShaderProgram shader) {
        switch (values.length) {
            case 1:
                shader.setUniformf(name, values[0]);
                break;
            case 2:
                shader.setUniformf(name, values[0], values[1]);
                break;
            case 3:
                shader.setUniformf(name, values[0], values[1], values[2]);
                break;
            case 4:
                shader.setUniformf(name, values[0], values[1], values[2], values[3]);
                break;
        }
    }
}
