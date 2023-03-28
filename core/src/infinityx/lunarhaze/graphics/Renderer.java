package infinityx.lunarhaze.graphics;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/**
 * Similar to Batch, however very dumbed down as to not force drawing calls to have a texture.
 * Should be used if the shader is very simple.
 */
public interface Renderer extends Disposable {

    /**
     * Sets up the renderer for drawing.
     */
    void begin();

    /**
     * Finishes off rendering.
     */
    void end();

    /**
     * Returns the current projection matrix. Changing this within {@link #begin()}/{@link #end()} results in undefined
     * behaviour.
     */
    Matrix4 getProjectionMatrix();

    /**
     * Returns the current transform matrix. Changing this within {@link #begin()}/{@link #end()} results in undefined
     * behaviour.
     */
    Matrix4 getTransformMatrix();

    /**
     * Sets the projection matrix to be used.
     * If this is called inside a {@link #begin()}/{@link #end()} block, end will be called.
     */
    void setProjectionMatrix(Matrix4 projection);

    /**
     * Sets the transform matrix to be used.
     */
    void setTransformMatrix(Matrix4 transform);

    /**
     * Sets the shader. This method will end the batch before setting the new shader.
     *
     * @param shader the {@link ShaderProgram}.
     */
    void setShader(ShaderProgram shader);

    /**
     * @return the current {@link ShaderProgram}.
     */
    ShaderProgram getShader();

    /**
     * @return true if currently between begin and end.
     */
    boolean isDrawing();

}
