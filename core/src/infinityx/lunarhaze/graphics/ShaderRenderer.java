package infinityx.lunarhaze.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

/**
 * A simple class which allows rendering a shader on a quad mesh.
 * Only two vertex attributes are supported, "a_position" and "a_texCoord", however you are able to supply uniforms.
 */
public class ShaderRenderer implements Renderer {

    float[] vertices;
    private final Mesh mesh;
    private boolean drawing;

    private final Matrix4 transformMatrix = new Matrix4();
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 combinedMatrix = new Matrix4();

    private ShaderProgram shader;

    /**
     * Constructs a new ShaderRenderer. Sets the projection matrix to an orthographic projection with y-axis point upwards,
     * x-axis point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect
     * with respect to the current screen resolution.
     * <p>
     * Note: shader is assumed to have a "u_projTrans" uniform for transformations,
     * and "a_position", "a_texCoord" attributes for position and uv coordinates respectively.
     */
    public ShaderRenderer() {
        projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mesh = new Mesh(false, 6, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE)
        );
        vertices = new float[]{
                0, 0, 0.0f, 0.0f,
                0, 0, 1.0f, 0.0f,
                0, 0, 0.0f, 1.0f,
                0, 0, 1.0f, 0.0f,
                0, 0, 1.0f, 1.0f,
                0, 0, 0.0f, 1.0f
        };
        drawing = false;
    }

    @Override
    public void begin() {
        if (drawing) throw new IllegalStateException("InstanceRenderer.end must be called before begin.");

        Gdx.gl.glDepthMask(false);
        shader.bind();
        setupMatrices();

        drawing = true;
    }

    @Override
    public void end() {
        if (!drawing) throw new IllegalStateException("ShaderBatch.begin must be called before end.");
        mesh.render(shader, GL20.GL_TRIANGLES);
        drawing = false;
        mesh.disableInstancedRendering();

        GL20 gl = Gdx.gl;
        gl.glDepthMask(true);
    }

    /**
     * The mesh will be drawn as a quad with given width and height.
     * The position of the bottom-left corner is set to (x,y).
     * uv coordinates span from (0, 0) on the bottom-left to (1, 1) on the top right of the quad mesh.
     *
     * @param x x-position
     * @param y y-position
     * @param height height of quad
     * @param width  width of quad
     */
    public void draw(float x, float y, float height, float width) {
         vertices[0] = x;
         vertices[1] = y;
         vertices[4] = x + width;
         vertices[5] = y;
         vertices[8] = x;
         vertices[9] = y + width;
         vertices[12] = x + width;
         vertices[13] = y;
         vertices[16] = x + width;
         vertices[17] = y + height;
         vertices[20] = x;
         vertices[21] = y + height;

        mesh.setVertices(vertices);
    }

    @Override
    public boolean isDrawing() {
        return isDrawing();
    }

    @Override
    public void dispose() {
        mesh.dispose();
        shader.dispose();
    }

    @Override
    public Matrix4 getProjectionMatrix() {
        return projectionMatrix;
    }

    @Override
    public Matrix4 getTransformMatrix() {
        return transformMatrix;
    }

    @Override
    public void setProjectionMatrix(Matrix4 projection) {
        if (drawing) end();
        projectionMatrix.set(projection);
    }

    @Override
    public void setTransformMatrix(Matrix4 transform) {
        if (drawing) end();
        transformMatrix.set(transform);
    }

    protected void setupMatrices() {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        shader.setUniformMatrix("u_projTrans", combinedMatrix);
    }

    @Override
    public void setShader(ShaderProgram shader) {
        if (drawing) {
            end();
        }
        this.shader = shader;
    }

    @Override
    public ShaderProgram getShader() {
        return shader;
    }
}

