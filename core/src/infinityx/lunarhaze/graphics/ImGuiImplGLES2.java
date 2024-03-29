package infinityx.lunarhaze.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import imgui.*;
import imgui.callback.ImPlatformFuncViewport;
import imgui.flag.ImGuiBackendFlags;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiViewportFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.type.ImInt;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_MAJOR_VERSION;
import static org.lwjgl.opengl.GL30.GL_MINOR_VERSION;

/**
 * This class is just a simple rewrite of {@link ImGuiImplGl3} to work with GL ES 2.0.
 * To work nicely with LibGDX, it uses a {@link Mesh} to render.
 */
public class ImGuiImplGLES2 {
    // OpenGL Data
    private int glVersion = 0;
    private String glslVersion = "";
    private int gFontTexture = 0;

    // Used to store tmp renderer data
    private final ImVec2 displaySize = new ImVec2();
    private final ImVec2 framebufferScale = new ImVec2();
    private final ImVec2 displayPos = new ImVec2();
    private final ImVec4 clipRect = new ImVec4();
    private final float[] orthoProjMatrix = new float[4 * 4];

    // Variables used to backup GL state before and after the rendering of Dear ImGui
    private final int[] lastViewport = new int[4];
    private final int[] lastScissorBox = new int[4];
    private final int[] lastBlendSrcRgb = new int[1];
    private final int[] lastBlendDstRgb = new int[1];
    private final int[] lastBlendSrcAlpha = new int[1];
    private final int[] lastBlendDstAlpha = new int[1];
    private final int[] lastBlendEquationRgb = new int[1];
    private final int[] lastBlendEquationAlpha = new int[1];
    private boolean lastEnableBlend = false;
    private boolean lastEnableCullFace = false;
    private boolean lastEnableDepthTest = false;
    private boolean lastEnableStencilTest = false;
    private boolean lastEnableScissorTest = false;


    /**
     * Shader to render ImGui output
     */
    private ShaderProgram program;

    /**
     * Mesh holding ImGui vertex and index buffer dump
     */
    private Mesh mesh;

    /**
     * Cache to store imgui vertex data dump
     */
    FloatArray vertzCache;

    /**
     * Cache to store imgui index data dump
     */
    ShortArray indzCache;

    /**
     * Method to do an initialization of the {@link ImGuiImplGl3} state.
     * It SHOULD be called before calling of the {@link ImGuiImplGl3#renderDrawData(ImDrawData)} method.
     * <p>
     * Unlike in the {@link #init(String)} method, here the glslVersion argument is omitted.
     * Thus a "#version 130" string will be used instead.
     */
    public void init() {
        init(null);
    }

    /**
     * Method to do an initialization of the {@link ImGuiImplGl3} state.
     * It SHOULD be called before calling of the {@link ImGuiImplGl3#renderDrawData(ImDrawData)} method.
     * <p>
     * Method takes an argument, which should be a valid GLSL string with the version to use.
     * <pre>
     * ----------------------------------------
     * OpenGL    GLSL      GLSL
     * version   version   string
     * ---------------------------------------
     *  2.0       110       "#version 110"
     *  2.1       120       "#version 120"
     *  3.0       130       "#version 130"
     *  3.1       140       "#version 140"
     *  3.2       150       "#version 150"
     *  3.3       330       "#version 330 core"
     *  4.0       400       "#version 400 core"
     *  4.1       410       "#version 410 core"
     *  4.2       420       "#version 410 core"
     *  4.3       430       "#version 430 core"
     *  ES 3.0    300       "#version 300 es"   = WebGL 2.0
     * ---------------------------------------
     * </pre>
     * <p>
     * If the argument is null, then a "#version 130" string will be used by default.
     *
     * @param glslVersion string with the version of the GLSL
     */
    public void init(final String glslVersion) {
        this.vertzCache = new FloatArray();
        this.indzCache = new ShortArray();

        readGlVersion();
        setupBackendCapabilitiesFlags();

        if (glslVersion == null) {
            this.glslVersion = "#version 130";
        } else {
            this.glslVersion = glslVersion;
        }

        createDeviceObjects();

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            initPlatformInterface();
        }
    }


    /**
     * Method to render {@link ImDrawData} into current OpenGL context.
     *
     * @param drawData draw data to render
     */
    public void renderDrawData(final ImDrawData drawData) {
        if (drawData.getCmdListsCount() <= 0) {
            return;
        }

        // Will project scissor/clipping rectangles into framebuffer space
        drawData.getDisplaySize(displaySize);           // (0,0) unless using multi-viewports
        drawData.getDisplayPos(displayPos);
        drawData.getFramebufferScale(framebufferScale); // (1,1) unless using retina display which are often (2,2)

        final float clipOffX = displayPos.x;
        final float clipOffY = displayPos.y;
        final float clipScaleX = framebufferScale.x;
        final float clipScaleY = framebufferScale.y;

        // Avoid rendering when minimized, scale coordinates for retina displays (screen coordinates != framebuffer coordinates)
        final int fbWidth = (int) (displaySize.x * framebufferScale.x);
        final int fbHeight = (int) (displaySize.y * framebufferScale.y);

        if (fbWidth <= 0 || fbHeight <= 0) {
            return;
        }

        backupGlState();
        bind(fbWidth, fbHeight);

        // Render command lists
        for (int cmdListIdx = 0; cmdListIdx < drawData.getCmdListsCount(); cmdListIdx++) {

            // Get vertex buffer
            FloatBuffer vertBuf = drawData.getCmdListVtxBufferData(cmdListIdx).asFloatBuffer();

            vertzCache.clear();
            while (vertBuf.remaining() > 0) {
                vertzCache.add(vertBuf.get());
            }

            // Get index buffer
            ShortBuffer indBuf = drawData.getCmdListIdxBufferData(cmdListIdx).asShortBuffer();

            indzCache.clear();
            while (indBuf.remaining() > 0) {
                indzCache.add(indBuf.get());
            }

            mesh.setVertices(vertzCache.items, 0, vertzCache.size);
            mesh.setIndices(indzCache.items, 0, indzCache.size);

            for (int cmdBufferIdx = 0; cmdBufferIdx < drawData.getCmdListCmdBufferSize(cmdListIdx); cmdBufferIdx++) {
                drawData.getCmdListCmdBufferClipRect(cmdListIdx, cmdBufferIdx, clipRect);

                final float clipMinX = (clipRect.x - clipOffX) * clipScaleX;
                final float clipMinY = (clipRect.y - clipOffY) * clipScaleY;
                final float clipMaxX = (clipRect.z - clipOffX) * clipScaleX;
                final float clipMaxY = (clipRect.w - clipOffY) * clipScaleY;

                if (clipMaxX <= clipMinX || clipMaxY <= clipMinY) {
                    continue;
                }

                // Apply scissor/clipping rectangle (Y is inverted in OpenGL)
                glScissor((int) clipMinX, (int) (fbHeight - clipMaxY), (int) (clipMaxX - clipMinX), (int) (clipMaxY - clipMinY));

                // Bind texture, Draw
                final int textureId = drawData.getCmdListCmdBufferTextureId(cmdListIdx, cmdBufferIdx);
                final int elemCount = drawData.getCmdListCmdBufferElemCount(cmdListIdx, cmdBufferIdx);
                final int idxBufferOffset = drawData.getCmdListCmdBufferIdxOffset(cmdListIdx, cmdBufferIdx);

                Gdx.gl20.glBindTexture(GL_TEXTURE_2D, textureId);
                mesh.render(program, GL20.GL_TRIANGLES, idxBufferOffset, elemCount);
            }
        }

        restoreModifiedGlState();
    }

    /**
     * Call this method in the end of your application cycle to dispose resources used by {@link ImGuiImplGl3}.
     */
    public void dispose() {
        glDeleteTextures(gFontTexture);
        shutdownPlatformInterface();
        mesh.dispose();
        program.dispose();
    }

    /**
     * Method rebuilds the font atlas for Dear ImGui. Could be used to update application fonts in runtime.
     */
    public void updateFontsTexture() {
        glDeleteTextures(gFontTexture);

        final ImFontAtlas fontAtlas = ImGui.getIO().getFonts();
        final ImInt width = new ImInt();
        final ImInt height = new ImInt();
        final ByteBuffer buffer = fontAtlas.getTexDataAsRGBA32(width, height);

        gFontTexture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, gFontTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        fontAtlas.setTexID(gFontTexture);
    }

    private void readGlVersion() {
        final int[] major = new int[1];
        final int[] minor = new int[1];
        glGetIntegerv(GL_MAJOR_VERSION, major);
        glGetIntegerv(GL_MINOR_VERSION, minor);
        glVersion = major[0] * 100 + minor[0] * 10;
    }

    private void setupBackendCapabilitiesFlags() {
        final ImGuiIO io = ImGui.getIO();
        io.setBackendRendererName("imgui_java_impl_opengl3");

        // We can honor the ImDrawCmd::VtxOffset field, allowing for large meshes.
        if (glVersion >= 320) {
            io.addBackendFlags(ImGuiBackendFlags.RendererHasVtxOffset);
        }

        // We can create multi-viewports on the Renderer side (optional)
        io.addBackendFlags(ImGuiBackendFlags.RendererHasViewports);
    }

    private void createDeviceObjects() {
        createShaders();
        createMesh();
        updateFontsTexture();
    }

    private void createMesh() {
        // Safe bet for max Vertices/Indices
        this.mesh = new Mesh(Mesh.VertexDataType.VertexArray, false, 20000, 20000 * 2,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, "Position"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "UV"),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "Color"));
    }

    private void createShaders() {
        final int glslVersionValue = parseGlslVersionString();

        // Select shaders matching our GLSL versions
        final String vertShaderSource;
        final String fragShaderSource;

        if (glslVersionValue < 130) {
            vertShaderSource = getVertexShaderGlsl120();
            fragShaderSource = getFragmentShaderGlsl120();
        } else if (glslVersionValue == 300) {
            vertShaderSource = getVertexShaderGlsl300es();
            fragShaderSource = getFragmentShaderGlsl300es();
        } else if (glslVersionValue >= 410) {
            vertShaderSource = getVertexShaderGlsl410Core();
            fragShaderSource = getFragmentShaderGlsl410Core();
        } else {
            vertShaderSource = getVertexShaderGlsl130();
            fragShaderSource = getFragmentShaderGlsl130();
        }

        this.program = new ShaderProgram(vertShaderSource, fragShaderSource);
    }

    private int parseGlslVersionString() {
        final Pattern p = Pattern.compile("\\d+");
        final Matcher m = p.matcher(glslVersion);

        if (m.find()) {
            return Integer.parseInt(m.group());
        } else {
            throw new IllegalArgumentException("Invalid GLSL version string: " + glslVersion);
        }
    }

    private void backupGlState() {
        glGetIntegerv(GL_VIEWPORT, lastViewport);
        glGetIntegerv(GL_SCISSOR_BOX, lastScissorBox);
        glGetIntegerv(GL_BLEND_SRC_RGB, lastBlendSrcRgb);
        glGetIntegerv(GL_BLEND_DST_RGB, lastBlendDstRgb);
        glGetIntegerv(GL_BLEND_SRC_ALPHA, lastBlendSrcAlpha);
        glGetIntegerv(GL_BLEND_DST_ALPHA, lastBlendDstAlpha);
        glGetIntegerv(GL_BLEND_EQUATION_RGB, lastBlendEquationRgb);
        glGetIntegerv(GL_BLEND_EQUATION_ALPHA, lastBlendEquationAlpha);
        lastEnableBlend = glIsEnabled(GL_BLEND);
        lastEnableCullFace = glIsEnabled(GL_CULL_FACE);
        lastEnableDepthTest = glIsEnabled(GL_DEPTH_TEST);
        lastEnableStencilTest = glIsEnabled(GL_STENCIL_TEST);
        lastEnableScissorTest = glIsEnabled(GL_SCISSOR_TEST);
    }

    private void restoreModifiedGlState() {
        glBlendEquationSeparate(lastBlendEquationRgb[0], lastBlendEquationAlpha[0]);
        glBlendFuncSeparate(lastBlendSrcRgb[0], lastBlendDstRgb[0], lastBlendSrcAlpha[0], lastBlendDstAlpha[0]);
        // @formatter:off CHECKSTYLE:OFF
        if (lastEnableBlend) glEnable(GL_BLEND);
        else glDisable(GL_BLEND);
        if (lastEnableCullFace) glEnable(GL_CULL_FACE);
        else glDisable(GL_CULL_FACE);
        if (lastEnableDepthTest) glEnable(GL_DEPTH_TEST);
        else glDisable(GL_DEPTH_TEST);
        if (lastEnableStencilTest) glEnable(GL_STENCIL_TEST);
        else glDisable(GL_STENCIL_TEST);
        if (lastEnableScissorTest) glEnable(GL_SCISSOR_TEST);
        else glDisable(GL_SCISSOR_TEST);
        // @formatter:on CHECKSTYLE:ON
        glViewport(lastViewport[0], lastViewport[1], lastViewport[2], lastViewport[3]);
        glScissor(lastScissorBox[0], lastScissorBox[1], lastScissorBox[2], lastScissorBox[3]);
    }

    // Setup desired GL state
    private void bind(final int fbWidth, final int fbHeight) {
        // Setup render state: alpha-blending enabled, no face culling, no depth testing, scissor enabled, polygon fill
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_STENCIL_TEST);
        glEnable(GL_SCISSOR_TEST);

        // Setup viewport, orthographic projection matrix
        // Our visible imgui space lies from draw_data->DisplayPos (top left) to draw_data->DisplayPos+data_data->DisplaySize (bottom right).
        // DisplayPos is (0,0) for single viewport apps.
        glViewport(0, 0, fbWidth, fbHeight);
        final float left = displayPos.x;
        final float right = displayPos.x + displaySize.x;
        final float top = displayPos.y;
        final float bottom = displayPos.y + displaySize.y;

        // Orthographic matrix projection
        orthoProjMatrix[0] = 2.0f / (right - left);
        orthoProjMatrix[5] = 2.0f / (top - bottom);
        orthoProjMatrix[10] = -1.0f;
        orthoProjMatrix[12] = (right + left) / (left - right);
        orthoProjMatrix[13] = (top + bottom) / (bottom - top);
        orthoProjMatrix[15] = 1.0f;

        // Bind shader
        program.bind();
        program.setUniformi("Texture", 0);
        program.setUniformMatrix("ProjMtx", new Matrix4(orthoProjMatrix));
    }

    //--------------------------------------------------------------------------------------------------------
    // MULTI-VIEWPORT / PLATFORM INTERFACE SUPPORT
    // This is an _advanced_ and _optional_ feature, allowing the back-end to create and handle multiple viewports simultaneously.
    // If you are new to dear imgui or creating a new binding for dear imgui, it is recommended that you completely ignore this section first..
    //--------------------------------------------------------------------------------------------------------

    private void initPlatformInterface() {
        ImGui.getPlatformIO().setRendererRenderWindow(new ImPlatformFuncViewport() {
            @Override
            public void accept(final ImGuiViewport vp) {
                if (!vp.hasFlags(ImGuiViewportFlags.NoRendererClear)) {
                    glClearColor(0, 0, 0, 0);
                    glClear(GL_COLOR_BUFFER_BIT);
                }
                renderDrawData(vp.getDrawData());
            }
        });
    }

    private void shutdownPlatformInterface() {
        ImGui.destroyPlatformWindows();
    }

    private int createAndCompileShader(final int type, final CharSequence source) {
        final int id = glCreateShader(type);

        glShaderSource(id, source);
        glCompileShader(id);

        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new IllegalStateException("Failed to compile shader:\n" + glGetShaderInfoLog(id));
        }

        return id;
    }

    private String getVertexShaderGlsl120() {
        return glslVersion + "\n"
                + "uniform mat4 ProjMtx;\n"
                + "attribute vec2 Position;\n"
                + "attribute vec2 UV;\n"
                + "attribute vec4 Color;\n"
                + "varying vec2 Frag_UV;\n"
                + "varying vec4 Frag_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Frag_UV = UV;\n"
                + "    Frag_Color = Color;\n"
                + "    gl_Position = ProjMtx * vec4(Position.xy,0,1);\n"
                + "}\n";
    }

    private String getVertexShaderGlsl130() {
        return glslVersion + "\n"
                + "uniform mat4 ProjMtx;\n"
                + "in vec2 Position;\n"
                + "in vec2 UV;\n"
                + "in vec4 Color;\n"
                + "out vec2 Frag_UV;\n"
                + "out vec4 Frag_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Frag_UV = UV;\n"
                + "    Frag_Color = Color;\n"
                + "    gl_Position = ProjMtx * vec4(Position.xy,0,1);\n"
                + "}\n";
    }

    private String getVertexShaderGlsl300es() {
        return glslVersion + "\n"
                + "precision highp float;\n"
                + "layout (location = 0) in vec2 Position;\n"
                + "layout (location = 1) in vec2 UV;\n"
                + "layout (location = 2) in vec4 Color;\n"
                + "uniform mat4 ProjMtx;\n"
                + "out vec2 Frag_UV;\n"
                + "out vec4 Frag_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Frag_UV = UV;\n"
                + "    Frag_Color = Color;\n"
                + "    gl_Position = ProjMtx * vec4(Position.xy,0,1);\n"
                + "}\n";
    }

    private String getVertexShaderGlsl410Core() {
        return glslVersion + "\n"
                + "layout (location = 0) in vec2 Position;\n"
                + "layout (location = 1) in vec2 UV;\n"
                + "layout (location = 2) in vec4 Color;\n"
                + "uniform mat4 ProjMtx;\n"
                + "out vec2 Frag_UV;\n"
                + "out vec4 Frag_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Frag_UV = UV;\n"
                + "    Frag_Color = Color;\n"
                + "    gl_Position = ProjMtx * vec4(Position.xy,0,1);\n"
                + "}\n";
    }

    private String getFragmentShaderGlsl120() {
        return glslVersion + "\n"
                + "#ifdef GL_ES\n"
                + "    precision mediump float;\n"
                + "#endif\n"
                + "uniform sampler2D Texture;\n"
                + "varying vec2 Frag_UV;\n"
                + "varying vec4 Frag_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    gl_FragColor = Frag_Color * texture2D(Texture, Frag_UV.st);\n"
                + "}\n";
    }

    private String getFragmentShaderGlsl130() {
        return glslVersion + "\n"
                + "uniform sampler2D Texture;\n"
                + "in vec2 Frag_UV;\n"
                + "in vec4 Frag_Color;\n"
                + "out vec4 Out_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Out_Color = Frag_Color * texture(Texture, Frag_UV.st);\n"
                + "}\n";
    }

    private String getFragmentShaderGlsl300es() {
        return glslVersion + "\n"
                + "precision mediump float;\n"
                + "uniform sampler2D Texture;\n"
                + "in vec2 Frag_UV;\n"
                + "in vec4 Frag_Color;\n"
                + "layout (location = 0) out vec4 Out_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Out_Color = Frag_Color * texture(Texture, Frag_UV.st);\n"
                + "}\n";
    }

    private String getFragmentShaderGlsl410Core() {
        return glslVersion + "\n"
                + "in vec2 Frag_UV;\n"
                + "in vec4 Frag_Color;\n"
                + "uniform sampler2D Texture;\n"
                + "layout (location = 0) out vec4 Out_Color;\n"
                + "void main()\n"
                + "{\n"
                + "    Out_Color = Frag_Color * texture(Texture, Frag_UV.st);\n"
                + "}\n";
    }
}