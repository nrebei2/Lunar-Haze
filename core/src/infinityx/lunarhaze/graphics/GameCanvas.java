package infinityx.lunarhaze.graphics;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;

/**
 * Primary view class for the game, abstracting the basic graphics calls.
 * <p>
 * This version of GameCanvas supports both rectangular and polygonal Sprite
 * drawing using {@link PolygonSpriteBatch}, shape drawing using {@link ShapeRenderer},
 * and shader rendering using {@link ShaderRenderer}.
 */
public class GameCanvas {

    /**
     * Enumeration to track which pass we are in
     */
    public enum DrawPass {
        /**
         * We are not drawing
         */
        INACTIVE,
        /**
         * We are drawing sprites
         */
        SPRITE,
        /**
         * We are drawing shapes
         */
        SHAPE,
        /**
         * We are drawing a shader
         */
        SHADER,
        /**
         * We are drawing Box2D lights
         */
        LIGHT
    }

    /**
     * Enumeration of supported BlendStates.
     */
    public enum BlendState {
        /**
         * Alpha blending on, assuming the colors have pre-multipled alpha (DEFAULT)
         */
        ALPHA_BLEND,
        /**
         * Alpha blending on, assuming the colors have no pre-multipled alpha
         */
        NO_PREMULT,
        /**
         * Color values are added together, causing a white-out effect
         */
        ADDITIVE,
        /**
         * Color values are draw on top of one another with no transparency support
         */
        OPAQUE
    }

    /**
     * Drawing context to handle textures AND POLYGONS as sprites
     */
    private PolygonSpriteBatch spriteBatch;

    public Color SHADE = new Color(0.2125f, 0.7154f, 0.0721f, 1).mul(0.25f);

    /**
     * Rendering context for drawing shapes
     */
    public ShapeRenderer shapeRenderer;

    /**
     * Rendering context for drawing shaders
     */
    private ShaderRenderer shaderRenderer;

    /**
     * Track the current drawing pass (for error checking)
     */
    private DrawPass active;

    /**
     * The current color blending mode
     */
    private BlendState blend;

    /**
     * Camera for the underlying renderers
     */
    private OrthographicCamera camera;

    /**
     * Camera for the ui rendering
     */
    private OrthographicCamera uiCamera;

    /**
     * Camera for the light renderer
     */
    private OrthographicCamera raycamera;

    /**
     * Value to cache window width (if we are currently full screen)
     */
    int width;
    /**
     * Value to cache window height (if we are currently full screen)
     */
    int height;

    // CACHE OBJECTS
    /**
     * Color cache for setting alpha on sprites
     */
    private final Color alphaCache;
    /**
     * Affine cache for current sprite to draw
     */
    private Affine2 local;
    /**
     * Affine cache for all sprites this drawing pass
     */
    private Matrix4 global;

    /**
     * Cache object to handle raw textures
     */
    private TextureRegion holder;

    /**
     * Cache object to handle drawing text
     */
    private final GlyphLayout layout;

    /**
     * Scaling factors for world to screen translation
     */
    private Vector2 worldToScreen;

    /**
     * The current zoom of the camera. A zoom of 1 is unchanged.
     * A zoom >1 will zoom in, and a zoom of <1 will zoom out
     */
    private float zoom;

    /**
     * Screen coords of player, used to make scene objects transparent
     */
    public Vector2 playerCoords;

    /**
     * The width of the camera view.
     */
    private float camWidth;

    /**
     * The height of the camera view.
     */
    private float camHeight;

    /**
     * The x-coordinate of the bottom-left corner of the camera view.
     */
    private float camX;

    /**
     * The y-coordinate of the bottom-left corner of the camera view.
     */
    private float camY;

    /**
     * Standard window size to fit
     */
    private static final int STANDARD_WIDTH = 2112;
    /**
     * Standard window height to fit
     */
    private static final int STANDARD_HEIGHT = 1188;


    /**
     * Sets the scaling factor for the world to screen transformation
     *
     * @param worldToScreen x
     */
    public void setWorldToScreen(Vector2 worldToScreen) {
        this.worldToScreen = worldToScreen;

        // We have the actual world to screen now
        setupCameras();
    }

    /**
     * Both functions represent a linear map from world coordinates to screen coordinates
     */
    public float WorldToScreenX(float w_x) {
        return w_x * worldToScreen.x;
    }

    public float WorldToScreenY(float w_y) {
        return w_y * worldToScreen.y;
    }

    /**
     * represent a map from screen coordinates to world coordinates.
     *
     * @param view world view translation
     */
    public float ScreenToWorldX(float s_x, Vector2 view) {
        // Will have to match offset in setupCameras
        float offset = (getWidth() - (float) getWidth() / zoom) / 2;
        return ((s_x / zoom) + offset - view.x) / (worldToScreen.x);
    }

    /**
     * No need to flip y-axis. Represent a map from screen coordinates to world coordinates.
     *
     * @param view world view translation
     */
    public float ScreenToWorldY(float s_y, Vector2 view) {
        // Will have to match offset in setupCameras
        float offset = (getHeight() - (float) getHeight() / zoom) / 2;
        return ((Gdx.graphics.getHeight() - s_y) / zoom + offset - view.y) / (worldToScreen.y);
    }

    /**
     * Creates a new GameCanvas determined by the application configuration.
     * <p>
     * Width, height, and fullscreen are taken from the LWGJApplicationConfig
     * object used to start the application.  This constructor initializes all
     * of the necessary graphics objects.
     */
    public GameCanvas() {
        active = DrawPass.INACTIVE;
        spriteBatch = new PolygonSpriteBatch();
        shapeRenderer = new ShapeRenderer();
        shaderRenderer = new ShaderRenderer();

        worldToScreen = new Vector2();
        camera = new OrthographicCamera();
        uiCamera = new OrthographicCamera();
        zoom = 1;
        resize();

        spriteBatch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shaderRenderer.setProjectionMatrix(camera.combined);

        playerCoords = new Vector2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);

        // Initialize the cache objects
        holder = new TextureRegion();
        local = new Affine2();
        global = new Matrix4();
        alphaCache = new Color(1, 1, 1, 1);

        this.layout = new GlyphLayout();
    }

    /**
     * Set up the cameras used for this canvas
     */
    private void setupCameras() {
        // Set the projection matrix (for proper scaling)
        float width = (float) getWidth();
        float height = (float) getHeight();
        camera.setToOrtho(false, width / zoom, height / zoom);
        //Center camera at (width/2, height/2)
        camera.translate(
                (width - width / zoom) / 2, (height - height / zoom) / 2
        );
        camera.update();

        // Different transform for light camera sadly
        // This one is alot easier to work with tho
        raycamera = new OrthographicCamera(
                width / (WorldToScreenX(1) * zoom),
                height / (WorldToScreenY(1) * zoom)
        );

        // ui camera should not be affected by zoom
        uiCamera.setToOrtho(false, width, height);

    }

    /**
     * Updates the camera bounds based on the current zoom factor, width, and height.
     *
     * @param zoom the zoom factor to consider
     */
    private void updateCameraBounds(float zoom) {
        camWidth = getWidth() / zoom;
        camHeight = getHeight() / zoom;
        camX = (getWidth() - camWidth) / 2;
        camY = (getHeight() - camHeight) / 2;
    }

    /**
     * Updates the camera {@link #zoom}. Will clamp zoom to be positive.
     *
     * @param zoom the new camera zoom to set
     */
    public void setZoom(float zoom) {
        this.zoom = Math.max(0.01f, zoom);
        setupCameras();
    }

    public float getZoom() {
        return zoom;
    }

    /**
     * Eliminate any resources that should be garbage collected manually.
     */
    public void dispose() {
        if (active != DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas", "Cannot dispose while drawing active", new IllegalStateException());
            return;
        }
        shapeRenderer.dispose();
        if (shaderRenderer != null) {
            shaderRenderer.dispose();
        }
        spriteBatch.dispose();
        spriteBatch = null;
        shapeRenderer = null;
        shaderRenderer = null;
        local = null;
        global = null;
        holder = null;
    }

    /**
     * Returns the width of this canvas
     * <p>
     * This currently gets its value from Gdx.graphics.getWidth()
     *
     * @return the width of this canvas
     */
    public int getWidth() {
        return Gdx.graphics.getWidth();
    }

    /**
     * Changes the width of this canvas
     * <p>
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * @param width the canvas width
     */
    public void setWidth(int width) {
        if (active != DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
            return;
        }
        this.width = width;
        if (!isFullscreen()) {
            Gdx.graphics.setWindowedMode(width, getHeight());
        }
        resize();
    }

    /**
     * Returns the height of this canvas
     * <p>
     * This currently gets its value from Gdx.graphics.getHeight()
     *
     * @return the height of this canvas
     */
    public int getHeight() {
        return Gdx.graphics.getHeight();
    }

    /**
     * Changes the height of this canvas
     * <p>
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * @param height the canvas height
     */
    public void setHeight(int height) {
        if (active != DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
            return;
        }
        this.height = height;
        if (!isFullscreen()) {
            Gdx.graphics.setWindowedMode(getWidth(), height);
        }
        resize();
    }

    /**
     * Returns the dimensions of this canvas
     *
     * @return the dimensions of this canvas
     */
    public Vector2 getSize() {
        return new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * Changes the width and height of this canvas
     * <p>
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * @param width  the canvas width
     * @param height the canvas height
     */
    public void setSize(int width, int height) {
        if (active != DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
            return;
        }
        this.width = width;
        this.height = height;
        if (!isFullscreen()) {
            Gdx.graphics.setWindowedMode(width, height);
        }
        resize();

    }

    /**
     * Returns whether this canvas is currently fullscreen.
     *
     * @return whether this canvas is currently fullscreen.
     */
    public boolean isFullscreen() {
        return Gdx.graphics.isFullscreen();
    }

    /**
     * Sets whether or not this canvas should change to fullscreen.
     * <p>
     * If desktop is true, it will use the current desktop resolution for
     * fullscreen, and not the width and height set in the configuration
     * object at the start of the application. This parameter has no effect
     * if fullscreen is false.
     * <p>
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * @param value   Whether this canvas should change to fullscreen.
     * @param desktop Whether to use the current desktop resolution
     */
    public void setFullscreen(boolean value, boolean desktop) {
        if (active != DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
            return;
        }
        if (value) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(width, height);
        }
    }

    /**
     * Resets the SpriteBatch camera when this canvas is resized.
     * <p>
     * If you do not call this when the window is resized, you will get
     * weird scaling issues.
     */
    public void resize() {
        float sx = ((float) getWidth()) / STANDARD_WIDTH;
        float sy = ((float) getHeight()) / STANDARD_HEIGHT;
        setZoom(sx < sy ? sy : sx);
//        setZoom(1);
        // Gdx.gl.glViewport(0, 0, getWidth(), getHeight());
    }

    /**
     * Returns the current color blending state for this canvas.
     * <p>
     * Textures draw to this canvas will be composited according
     * to the rules of this blend state.
     *
     * @return the current color blending state for this canvas
     */
    public BlendState getBlendState() {
        return blend;
    }

    /**
     * Sets the color blending state for this canvas.
     * <p>
     * Any texture draw subsequent to this call will use the rules of this blend
     * state to composite with other textures.  Unlike the other setters, if it is
     * perfectly safe to use this setter while  drawing is active (e.g. in-between
     * a begin-end pair).
     *
     * @param state the color blending rule
     */
    public void setBlendState(BlendState state) {
        if (state == blend) {
            return;
        }
        switch (state) {
            case NO_PREMULT:
                spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case ALPHA_BLEND:
                spriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case ADDITIVE:
                spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                break;
            case OPAQUE:
                spriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ZERO);
                break;
        }
        blend = state;
    }

    /**
     * Clear the screen so we can start a new animation frame
     */
    public void clear() {
        // Clear the screen
        clear(Color.BLACK);
    }

    /**
     * Clears the screen with the given color so we can start a new animation frame
     */
    public void clear(Color c) {
        // Clear the screen
        Gdx.gl.glClearColor(c.r, c.g, c.b, c.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /**
     * Start a standard drawing sequence with the given pass.
     * In order to draw with a different pass, you must call {@link #end()}.
     * <p>
     * Nothing is flushed to the graphics card until the method end() is called.
     *
     * @param pass What pass you would like to draw in
     * @param tx   the amount to translate on the x-axis
     * @param ty   the amount to translate on the y-axis
     */
    public void begin(DrawPass pass, float tx, float ty) {
        if (pass == DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas", "What do you mean by that?", new IllegalStateException());
            return;
        }
        global.idt();

        updateCameraBounds(zoom);
        // Consider the view translation for the camera bounds
        camX -= tx;
        camY -= ty;

        if (pass == DrawPass.LIGHT || pass == DrawPass.SHAPE) {
            // Mimic same view transform with different projection matrix
            global.translate(
                    (tx - getWidth() / 2) / WorldToScreenX(1),
                    (ty - getHeight() / 2) / WorldToScreenY(1),
                    0.0f
            );

            // Light uses a separate camera
            global.mulLeft(raycamera.combined);
        } else {
            global.translate(tx, ty, 0.0f);
            global.mulLeft(camera.combined);
        }

        switch (pass) {
            case SPRITE:
                spriteBatch.setProjectionMatrix(global);
                setBlendState(BlendState.NO_PREMULT);
                spriteBatch.begin();
                break;
            case SHAPE:
                shapeRenderer.setProjectionMatrix(global);
                break;
            case SHADER:
                shaderRenderer.setProjectionMatrix(global);
                break;
            case LIGHT:

        }
        active = pass;
    }

    /**
     * Start a standard drawing sequence the given pass.
     * In order to draw with a different pass, you must call {@link #end()}.
     * <p>
     * Nothing is flushed to the graphics card until the method end() is called.
     *
     * @param pass What pass you would like to draw in
     */
    public void begin(DrawPass pass) {
        if (pass == DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas", "What do you mean by that?", new IllegalStateException());
            return;
        }

        updateCameraBounds(zoom);

        switch (pass) {
            case SPRITE:
                spriteBatch.setProjectionMatrix(camera.combined);
                setBlendState(BlendState.NO_PREMULT);
                spriteBatch.begin();
                break;
            case SHAPE:
                // Enable blending
                Gdx.gl.glEnable(GL20.GL_BLEND);
                // Using raycamera as it has a nicer projection
                shapeRenderer.setProjectionMatrix(raycamera.combined);
                break;
            case SHADER:
                shaderRenderer.setProjectionMatrix(camera.combined);
                break;
        }
        active = pass;
    }

    /**
     * Similar to {@link #begin(DrawPass)}, however the canvas is not affected by zoom.
     * i.e., the canvas is always left anchored at (0,0) with width and height of {@link Graphics#getWidth()}
     * and {@link Graphics#getHeight()} respectively.
     */
    public void beginUI(DrawPass pass) {
        if (pass == DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas", "What do you mean by that?", new IllegalStateException());
            return;
        }

        // Do not zoom in for UI
        updateCameraBounds(1);

        switch (pass) {
            case SPRITE:
                spriteBatch.setProjectionMatrix(uiCamera.combined);
                setBlendState(BlendState.NO_PREMULT);
                spriteBatch.begin();
                break;
            case SHAPE:
                // Enable blending
                Gdx.gl.glEnable(GL20.GL_BLEND);
                shapeRenderer.setProjectionMatrix(camera.combined);
                break;
            case SHADER:
                shaderRenderer.setProjectionMatrix(camera.combined);
                break;
        }
        active = pass;
    }

    /**
     * Ends a drawing sequence for the current pass, flushing stuff to the graphics card.
     */
    public void end() {
        if (active == DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas", "Please begin before end as the naming implies...", new IllegalStateException());
            return;
        }
        switch (active) {
            case SPRITE:
                spriteBatch.end();
                break;
            case SHAPE:
                Gdx.gl.glDisable(GL20.GL_BLEND);
                break;
        }
        active = DrawPass.INACTIVE;
    }

    /**
     * Draw an stretched overlay image tinted by the given color.
     * <p>
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     * <p>
     * The image will be drawn starting at the bottom right corner, and will
     * be stretched to fill the whole screen if appropriate.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * @param image Texture to draw as an overlay
     * @param tint  The color tint
     * @param fill  Whether to stretch the image to fill the screen
     */
    public void drawOverlay(Texture image, Color tint, boolean fill) {
        if (active != DrawPass.SPRITE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SPRITE", new IllegalStateException());
            return;
        }
        float w, h;
        if (fill) {
            w = getWidth();
            h = getHeight();
        } else {
            w = image.getWidth();
            h = image.getHeight();
        }

        image.getHeight();
        image.getWidth();

        spriteBatch.setColor(tint);
        // Should match bottom left and top right of camera
        spriteBatch.draw(image, 0, 0, w, h);
    }

    /**
     * Draws the texture at the given position.
     * <p>
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param image The texture to draw
     * @param x     The x-coordinate of the bottom left corner
     * @param y     The y-coordinate of the bottom left corner
     */
    public void draw(Texture image, float x, float y) {
        draw(image, Color.WHITE, x, y);
    }

    /**
     * Draws the tinted texture at the given position.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param image The texture to draw
     * @param tint  The color tint
     * @param x     The x-coordinate of the bottom left corner
     * @param y     The y-coordinate of the bottom left corner
     */
    public void draw(Texture image, Color tint, float x, float y) {
        if (active != DrawPass.SPRITE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SPRITE", new IllegalStateException());
            return;
        }

        // Unlike Lab 1, we can shortcut without a master drawing method
        spriteBatch.setColor(tint);
        spriteBatch.draw(image, x, y);
    }

    /**
     * Draws the tinted texture at the given position.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param image  The texture to draw
     * @param tint   The color tint
     * @param x      The x-coordinate of the bottom left corner
     * @param y      The y-coordinate of the bottom left corner
     * @param width  The texture width
     * @param height The texture height
     */
    public void draw(Texture image, Color tint, float x, float y, float width, float height) {
        if (active != DrawPass.SPRITE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SPRITE", new IllegalStateException());
            return;
        }

        // Unlike Lab 1, we can shortcut without a master drawing method
        spriteBatch.setColor(tint);
        spriteBatch.draw(image, x, y, width, height);
    }


    /**
     * Draws the tinted texture with the given transformations
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     * <p>
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param image The texture to draw
     * @param tint  The color tint
     * @param ox    The x-coordinate of texture origin (in pixels)
     * @param oy    The y-coordinate of texture origin (in pixels)
     * @param x     The x-coordinate of the texture origin (on screen)
     * @param y     The y-coordinate of the texture origin (on screen)
     * @param angle The rotation angle (in degrees) about the origin.
     * @param sx    The x-axis scaling factor
     * @param sy    The y-axis scaling factor
     */
    public void draw(Texture image, Color tint, float ox, float oy,
                     float x, float y, float angle, float sx, float sy) {
        // Call the master drawing method (more efficient that base method)
        holder.setRegion(image);
        draw(holder, tint, ox, oy, x, y, angle, sx, sy);
    }

    /**
     * Draws the tinted texture with the given transformations
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     * <p>
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param image The texture to draw
     * @param alpha The alpha tint
     * @param ox    The x-coordinate of texture origin (in pixels)
     * @param oy    The y-coordinate of texture origin (in pixels)
     * @param x     The x-coordinate of the texture origin (on screen)
     * @param y     The y-coordinate of the texture origin (on screen)
     * @param angle The rotation angle (in degrees) about the origin.
     * @param sx    The x-axis scaling factor
     * @param sy    The y-axis scaling factor
     */
    public void draw(Texture image, float alpha, float ox, float oy,
                     float x, float y, float angle, float sx, float sy) {
        // Call the master drawing method (more efficient that base method)
        holder.setRegion(image);
        alphaCache.a = alpha;
        draw(holder, alphaCache, ox, oy, x, y, angle, sx, sy);
    }

    /**
     * Draws the texture at the given position.
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     * region
     *
     * @param region The texture to draw
     * @param x      The x-coordinate of the bottom left corner
     * @param y      The y-coordinate of the bottom left corner
     */
    public void draw(TextureRegion region, float x, float y) {
        if (active != DrawPass.SPRITE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SPRITE", new IllegalStateException());
            return;
        }

        // Unlike Lab 1, we can shortcut without a master drawing method
        spriteBatch.draw(region, x, y);
    }


    /**
     * Draws the tinted texture at the given position.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     * region
     *
     * @param region The texture to draw
     * @param tint   The color tint
     * @param x      The x-coordinate of the bottom left corner
     * @param y      The y-coordinate of the bottom left corner
     * @param width  The texture width
     * @param height The texture height
     */
    public void draw(TextureRegion region, Color tint, float x, float y, float width, float height) {
        if (active != DrawPass.SPRITE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SPRITE", new IllegalStateException());
            return;
        }

        // Unlike Lab 1, we can shortcut without a master drawing method
        spriteBatch.setColor(tint);
        spriteBatch.draw(region, x, y, width, height);
    }

    /**
     * Draws the tinted texture region (filmstrip) with the given transformations
     * <p>
     * A texture region is a single texture file that can hold one or more textures.
     * It is used for filmstrip animation.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     * <p>
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param region The texture to draw
     * @param tint   The color tint
     * @param ox     The x-coordinate of texture origin (in pixels)
     * @param oy     The y-coordinate of texture origin (in pixels)
     * @param x      The x-coordinate of the texture origin (on screen)
     * @param y      The y-coordinate of the texture origin (on screen)
     * @param angle  The rotation angle (in degrees) about the origin.
     * @param sx     The x-axis scaling factor
     * @param sy     The y-axis scaling factor
     * @return Whether the texture was drawn or not
     */
    public boolean draw(TextureRegion region, Color tint, float ox, float oy,
                     float x, float y, float angle, float sx, float sy) {
        if (active != DrawPass.SPRITE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SPRITE", new IllegalStateException());
            return false;
        }

        computeTransform(ox, oy, x, y, angle, sx, sy);

        // Draw if any vertices are inside the camera view
        float x1 = local.m02;
        float y1 = local.m12;
        if (x1 >= camX && x1 <= camX + camWidth && y1 >= camY && y1 <= camY + camHeight) {
            spriteBatch.setColor(tint);
            spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
            return true;
        }

        float regionHeight = region.getRegionHeight();
        float x2 = local.m01 * regionHeight + local.m02;
        float y2 = local.m11 * regionHeight + local.m12;
        if (x2 >= camX && x2 <= camX + camWidth && y2 >= camY && y2 <= camY + camHeight) {
            spriteBatch.setColor(tint);
            spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
            return true;
        }

        float regionWidth = region.getRegionWidth();
        float x3 = local.m00 * regionWidth + local.m01 * regionHeight + local.m02;
        float y3 = local.m10 * regionWidth + local.m11 * regionHeight + local.m12;
        if (x3 >= camX && x3 <= camX + camWidth && y3 >= camY && y3 <= camY + camHeight) {
            spriteBatch.setColor(tint);
            spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
            return true;
        }

        float x4 = local.m00 * regionWidth + local.m02;
        float y4 = local.m10 * regionWidth + local.m12;
        if (x4 >= camX && x4 <= camX + camWidth && y4 >= camY && y4 <= camY + camHeight) {
            spriteBatch.setColor(tint);
            spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
            return true;
        }

        // There is a possibility the texture covers the whole screen
        // This check will only work if the angle is 0
        if ((x1 <= camX && x3 >= camX + camWidth) || (y1 <= camY && y3 >= camY + camHeight)) {
            spriteBatch.setColor(tint);
            spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
            return true;
        }

        // Otherwise, clip
        return false;
    }

    /**
     * Draws the tinted texture region (filmstrip) with the given transformations
     * <p>
     * A texture region is a single texture file that can hold one or more textures.
     * It is used for filmstrip animation.
     * <p>
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     * <p>
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     * <p>
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param region The texture to draw
     * @param tint   The color tint
     * @param ox     The x-coordinate of texture origin (in pixels)
     * @param oy     The y-coordinate of texture origin (in pixels)
     * @param x      The x-coordinate of the texture origin (on screen)
     * @param y      The y-coordinate of the texture origin (on screen)
     * @param angle  The rotation angle (in degrees) about the origin.
     * @param sx     The x-axis scaling factor
     * @param sy     The y-axis scaling factor
     * @return Whether the texture was drawn or not
     */
    public boolean draw(TextureRegion region, Color tint, float ox, float oy,
                        float x, float y, float angle, float sx, float sy, float shx, float shy) {
        if (active != DrawPass.SPRITE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SPRITE", new IllegalStateException());
            return false;
        }

        computeTransform(ox, oy, x, y, angle, sx, sy, shx, shy);

        // Draw if any vertices are inside the camera view
        float x1 = local.m02;
        float y1 = local.m12;
        if (x1 >= camX && x1 <= camX + camWidth && y1 >= camY && y1 <= camY + camHeight) {
            spriteBatch.setColor(tint);
            spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
            return true;
        }

        float regionHeight = region.getRegionHeight();
        float x2 = local.m01 * regionHeight + local.m02;
        float y2 = local.m11 * regionHeight + local.m12;
        if (x2 >= camX && x2 <= camX + camWidth && y2 >= camY && y2 <= camY + camHeight) {
            spriteBatch.setColor(tint);
            spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
            return true;
        }

        float regionWidth = region.getRegionWidth();
        float x3 = local.m00 * regionWidth + local.m01 * regionHeight + local.m02;
        float y3 = local.m10 * regionWidth + local.m11 * regionHeight + local.m12;
        if (x3 >= camX && x3 <= camX + camWidth && y3 >= camY && y3 <= camY + camHeight) {
            spriteBatch.setColor(tint);
            spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
            return true;
        }

        float x4 = local.m00 * regionWidth + local.m02;
        float y4 = local.m10 * regionWidth + local.m12;
        if (x4 >= camX && x4 <= camX + camWidth && y4 >= camY && y4 <= camY + camHeight) {
            spriteBatch.setColor(tint);
            spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
            return true;
        }

        // There is a possibility the texture covers the whole screen
        // This check will only work if the angle is 0
        if ((x1 <= camX && x3 >= camX + camWidth) || (y1 <= camY && y3 >= camY + camHeight)) {
            spriteBatch.setColor(tint);
            spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
            return true;
        }

        // Otherwise, clip
        return false;
    }


    /**
     * Draws the outline of the given shape in the specified color
     *
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x     The x-coordinate of the shape position
     * @param y     The y-coordinate of the shape position
     * @param angle The shape angle of rotation
     * @param sx    The amount to scale the x-axis
     * @param sx    The amount to scale the y-axis
     */
    public void drawPhysics(PolygonShape shape, Color color, float x, float y, float angle, float sx, float sy) {
        if (active != DrawPass.SHAPE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin(SHAPE)", new IllegalStateException());
            return;
        }

        local.setToScaling(sx, sy);
        local.translate(x, y);
        local.rotateRad(angle);

        float x0, y0, x1, y1;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color);
        Vector2 vertex = new Vector2();
        for (int ii = 0; ii < shape.getVertexCount() - 1; ii++) {
            shape.getVertex(ii, vertex);
            local.applyTo(vertex);
            x0 = vertex.x;
            y0 = vertex.y;
            shape.getVertex(ii + 1, vertex);
            local.applyTo(vertex);
            x1 = vertex.x;
            y1 = vertex.y;
            shapeRenderer.line(x0, y0, x1, y1);
        }
        // Close the loop
        shape.getVertex(shape.getVertexCount() - 1, vertex);
        local.applyTo(vertex);
        x0 = vertex.x;
        y0 = vertex.y;
        shape.getVertex(0, vertex);
        local.applyTo(vertex);
        x1 = vertex.x;
        y1 = vertex.y;
        shapeRenderer.line(x0, y0, x1, y1);
        shapeRenderer.end();
    }

    public void drawPhysicsFill(PolygonShape shape, Color color, float x, float y, float angle, float sx, float sy) {
        if (active != DrawPass.SHAPE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin(SHAPE)", new IllegalStateException());
            return;
        }

        local.setToScaling(sx, sy);
        local.translate(x, y);
        local.rotateRad(angle);

        float x0, y0;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        Vector2 vertex = new Vector2();
        float[] vertices = {};
        float[] newVertices;
        for (int ii = 0; ii < shape.getVertexCount(); ii++) {
            shape.getVertex(ii, vertex);
            local.applyTo(vertex);
            x0 = vertex.x;
            y0 = vertex.y;
            newVertices = new float[vertices.length + 2];
            System.arraycopy(vertices, 0, newVertices, 0, vertices.length);
            newVertices[newVertices.length - 2] = x0;
            newVertices[newVertices.length - 1] = y0;
            vertices = newVertices;
        }
        shapeRenderer.polygon(vertices);
        shapeRenderer.end();
    }


    /**
     * Update and render lights
     *
     * @param handler holding lights
     */
    public void drawLights(RayHandler handler) {
        if (active != DrawPass.LIGHT) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for LIGHT", new IllegalStateException());
            return;
        }
        handler.setCombinedMatrix(global);
        handler.updateAndRender();
    }

    /**
     * Draws text on the screen.
     *
     * @param text The string to draw
     * @param font The font to use
     * @param x    The x-coordinate of the lower-left corner
     * @param y    The y-coordinate of the lower-left corner
     */
    public void drawText(String text, BitmapFont font, float x, float y) {
        if (active != DrawPass.SPRITE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SPRITE", new IllegalStateException());
            return;
        }
        layout.setText(font, text);
        font.draw(spriteBatch, layout, x, y);
    }

    /**
     * Draws text centered on the screen.
     *
     * @param text   The string to draw
     * @param font   The font to use
     * @param offset The y-value offset from the center of the screen.
     */
    public void drawTextCentered(String text, BitmapFont font, float offset) {
        if (active != DrawPass.SPRITE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SPRITE", new IllegalStateException());
            return;
        }

        layout.setText(font, text);
        float x = (getWidth() - layout.width) / 2.0f;
        float y = (getHeight() + layout.height) / 2.0f;

        font.draw(spriteBatch, layout, x, y + offset);
    }

    /**
     * Draws a shader instanced on quads with given width and height.
     *
     * @param shader   Sets the shader which will draw
     * @param width    width of quad
     * @param height   height of quad
     * @param uniforms uniforms for which will be passed in to shader
     */
    public void drawShader(ShaderProgram shader, float x, float y, float width, float height, ShaderUniform... uniforms) {
        if (active != DrawPass.SHADER) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SHADER", new IllegalStateException());
            return;
        }

        shaderRenderer.setShader(shader);
        shaderRenderer.draw(x, y, width, height);
        shaderRenderer.begin();
        for (ShaderUniform uniform : uniforms) {
            uniform.apply(shaderRenderer.getShader());
        }
        shaderRenderer.end();
    }

    /**
     * Draws text on the upper right corner of the screen.
     *
     * @param text   The string to draw
     * @param font   The font to use
     * @param offset The y-value offset from the center of the screen.
     */
    public void drawTextUpperRight(String text, BitmapFont font, float offset) {
        if (active != DrawPass.SPRITE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SPRITE", new IllegalStateException());
            return;
        }

        GlyphLayout layout = new GlyphLayout(font, text);
        float x = getWidth() - layout.width;
        float y = getHeight() - layout.height / 2.0f;
        font.draw(spriteBatch, layout, x, y + offset);
    }

    /**
     * Draws a solid rectangle at the specified position with the specified width and height
     *
     * @param x      The x-coordinate of the rectangle's lower left corner
     * @param y      The y-coordinate of the rectangle's lower left corner
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     */
    public void drawRecAt(float x, float y, float width, float height) {
        ShapeRenderer barRenderer = new ShapeRenderer();
        barRenderer.begin(ShapeRenderer.ShapeType.Filled);
        barRenderer.setColor(Color.YELLOW);
        barRenderer.rect(x, y, width, height);
        barRenderer.end();
    }

    /**
     * Draws a rectangle outline affected by global transform.
     *
     * @param x bottom-left screen x
     * @param y bottom-left screen y
     */
    public void drawBlackFilter() {
        if (active != DrawPass.SHAPE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SHAPE", new IllegalStateException());
            return;
        }
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.5f); // 设置颜色为半透明黑色（50%透明度）
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();
    }

    /**
     * Draws a black filter.
     *
     * @param x bottom-left screen x
     * @param y bottom-left screen y
     */
    public void drawRecOutline(float x, float y, float width, float height, Color color) {
        if (active != DrawPass.SHAPE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SHAPE", new IllegalStateException());
            return;
        }
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
    }

    /**
     * Compute the affine transform (and store it in local) for this image.
     *
     * @param ox    The x-coordinate of texture origin (in pixels)
     * @param oy    The y-coordinate of texture origin (in pixels)
     * @param x     The x-coordinate of the texture origin (on screen)
     * @param y     The y-coordinate of the texture origin (on screen)
     * @param angle The rotation angle (in degrees) about the origin.
     * @param sx    The x-axis scaling factor
     * @param sy    The y-axis scaling factor
     */
    private void computeTransform(float ox, float oy, float x, float y, float angle, float sx, float sy) {
        local.setToTranslation(x, y);
        local.rotate(180.0f * angle / (float) Math.PI);
        local.scale(sx, sy);
        local.translate(-ox, -oy);
    }

    private void computeTransform(float ox, float oy, float x, float y, float angle, float sx, float sy, float shx, float shy) {
        local.setToTranslation(x, y);
        local.rotate(180.0f * angle / (float) Math.PI);
        local.scale(sx, sy);
        local.shear(shx, shy); // Add shear transformation here
        local.translate(-ox, -oy);
    }

}