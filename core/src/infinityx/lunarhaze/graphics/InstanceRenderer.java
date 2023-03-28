package infinityx.lunarhaze.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

import java.nio.FloatBuffer;

/**
 * Renderer holding a Mesh with instanced rendering. Each mesh will be drawn as a quad.
 */
public class InstanceRenderer implements Renderer {
    private final Mesh mesh;
    private boolean drawing;

    private final Matrix4 transformMatrix = new Matrix4();
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 combinedMatrix = new Matrix4();

    private ShaderProgram shader;

    /**
     * Constructs a new InstanceRenderer. Sets the projection matrix to an orthographic projection with y-axis point upwards,
     * x-axis point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect
     * with respect to the current screen resolution.
     * <p>
     * Note: shader is assumed to have a "u_projTrans" uniform for transformations,
     * and "a_position", "a_texCoord" attributes for position and uv coordinates respectively.
     *
     * @param shader The default shader to use. Will be owned by this renderer (i.e., will be disposed when this renderer is disposed).
     */
    public InstanceRenderer(ShaderProgram shader) {
        this.shader = shader;
        projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mesh = new Mesh(true, 6, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE)
        );
        drawing = false;
    }

    /**
     * See {@link InstanceRenderer#InstanceRenderer(ShaderProgram)}.
     * The supplied shader is {@link SpriteBatch#createDefaultShader()}.
     */
    public InstanceRenderer() {
       this(SpriteBatch.createDefaultShader());
    }

    /**
     * Calls {@link Mesh#enableInstancedRendering(boolean, int, VertexAttribute...)}.
     */
    public void initialize(boolean isStatic, int instanceCount, VertexAttribute... attributes) {
        mesh.enableInstancedRendering(isStatic, instanceCount, attributes);
    }

    /**
     * See {@link Mesh#setInstanceData(FloatBuffer)}.
     */
    public void setInstanceData(FloatBuffer data) {
        mesh.setInstanceData(data);
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
        if (!drawing) throw new IllegalStateException("InstanceRenderer.begin must be called before end.");
        mesh.render(shader, GL20.GL_TRIANGLES);
        drawing = false;
        mesh.disableInstancedRendering();

        GL20 gl = Gdx.gl;
        gl.glDepthMask(true);
    }

    /**
     * The mesh will be drawn as a quad with given width and height.
     * The position of the bottom-left corner is set to (0,0); if you want other positions use the fact that the mesh is instanced.
     * uv coordinates span from (0, 0) on the bottom-left to (1, 1) on the top right of the quad mesh.
     *
     * @param height height of quad
     * @param width  width of quad
     */
    public void draw(float width, float height) {
        float[] vertices = new float[]{
                0.0f, 0.0f, 0.0f, 0.0f,
                width, 0.0f, 1.0f, 0.0f,
                0.0f, height, 0.0f, 1.0f,
                width, 0.0f, 1.0f, 0.0f,
                width, height, 1.0f, 1.0f,
                0.0f, height, 0.0f, 1.0f
        };

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
        this.shader.bind();
    }

    @Override
    public ShaderProgram getShader() {
        return shader;
    }
}
