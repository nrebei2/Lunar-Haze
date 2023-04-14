package infinityx.lunarhaze.graphics;

/*
 * GameCanvas.java
 *
 * To properly follow the model-view-controller separation, we should not have
 * any specific drawing code in GameMode. All of that code goes here.  As
 * with GameEngine, this is a class that you are going to want to copy for
 * your own projects.
 *
 * An important part of this canvas design is that it is loosely coupled with
 * the model classes. All of the drawing methods are abstracted enough that
 * it does not require knowledge of the interfaces of the model classes.  This
 * important, as the model classes are likely to change often.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
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
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;

/**
 * Primary view class for the game, abstracting the basic graphics calls.
 * <p>
 * This version of GameCanvas only supports both rectangular and polygonal Sprite
 * drawing.  It also supports a debug mode that draws polygonal outlines.  However,
 * that mode must be done in a separate begin/end pass.
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

    private final float GAP_DIST = 20f;

    /**
     * Drawing context to handle textures AND POLYGONS as sprites
     */
    private PolygonSpriteBatch spriteBatch;

    /**
     * Rendering context for drawing shapes
     */
    private final ShapeRenderer shapeRenderer;

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
     * Translation cache used for view translation
     * TODO: I dont like this, maybe a better way to restructure
     */
    private final Vector2 viewCache;

    /**
     * Sets the scaling factor for the world to screen transformation
     *
     * @param worldToScreen x
     */
    public void setWorldToScreen(Vector2 worldToScreen) {
        this.worldToScreen = worldToScreen;

        // We have the actual world to screen now
        raycamera = new OrthographicCamera(
                getWidth() / WorldToScreenX(1),
                getHeight() / WorldToScreenY(1)
        );
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
     * Both functions represent a map from screen coordinates to world coordinates.
     * This function also takes into account the view translation from the previous call.
     */
    public float ScreenToWorldX(float s_x) {
        return (s_x - viewCache.x) / worldToScreen.x;
    }

    /**
     * No need to flip y-axis
     */
    public float ScreenToWorldY(float s_y) {
        return ((Gdx.graphics.getHeight() - s_y) - viewCache.y) / worldToScreen.y;
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
        setupCameras();

        spriteBatch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shaderRenderer.setProjectionMatrix(camera.combined);

        // Initialize the cache objects
        holder = new TextureRegion();
        local = new Affine2();
        global = new Matrix4();
        viewCache = new Vector2();
        alphaCache = new Color(1, 1, 1, 1);

        this.layout = new GlyphLayout();
    }

    /**
     * Set up the cameras on the canvas
     */
    private void setupCameras() {
        // Set the projection matrix (for proper scaling)
        camera = new OrthographicCamera(getWidth(), getHeight());
        camera.setToOrtho(false);

        raycamera = new OrthographicCamera(
                getWidth() / WorldToScreenX(1),
                getHeight() / WorldToScreenY(1)
        );
        // Cant do this, would mess up existing UI drawing
        // Center camera at (0, 0)
        //camera.translate(-getWidth()/2, -getHeight()/2);
        //camera.update();
    }

    /**
     * Eliminate any resources that should be garbage collected manually.
     */
    public void dispose() {
        if (active != DrawPass.INACTIVE) {
            Gdx.app.error("GameCanvas", "Cannot dispose while drawing active", new IllegalStateException());
            return;
        }
        spriteBatch.dispose();
        spriteBatch = null;
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
        // Resizing screws up the projection matrix
        setupCameras();

//        Gdx.gl.glViewport(0, 0, getWidth(), getHeight());
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
     * Start a standard drawing sequence the given pass.if(CameraShake.timeLeft() > 0) {
     * CameraShake.update(Gdx.graphics.getDeltaTime());
     * levelContainer.translateView(CameraShake.getShakeOffset().x, CameraShake.getShakeOffset().y);
     * }
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
        viewCache.set(tx, ty);
        global.translate(tx, ty, 0.0f);

        if (pass == DrawPass.LIGHT) {
            // Light uses a separate camera
            global.mulLeft(raycamera.combined);
        } else {
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

        switch (pass) {
            case SPRITE:
                spriteBatch.setProjectionMatrix(camera.combined);
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
        spriteBatch.setColor(tint);
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
     */
    public void draw(TextureRegion region, Color tint, float ox, float oy,
                     float x, float y, float angle, float sx, float sy) {
        if (active != DrawPass.SPRITE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SPRITE", new IllegalStateException());
            return;
        }

        // BUG: The draw command for texture regions does not work properly.
        // There is a workaround, but it will break if the bug is fixed.
        // For now, it is better to set the affine transform directly.
        computeTransform(ox, oy, x, y, angle, sx, sy);
        spriteBatch.setColor(tint);
        spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
    }

    /**
     * Update and render lights
     *
     * @param handler holding lights
     */
    public void drawLights(RayHandler handler) {
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

    public void drawCollectLightBar(float width, float height, float percentage, Werewolf player) {
        if (active != DrawPass.SHAPE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SHAPE", new IllegalStateException());
            return;
        }

        float padding = 5;
        float x = WorldToScreenX(player.getPosition().x) - width / 2;
        float y = WorldToScreenY(player.getPosition().y) + player.getTextureHeight() + padding;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(
                x, y, width, height
        );

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Color yellow = new Color(244.0f / 255.0f, 208.0f / 255.0f, 63.0f / 255.0f, 1.0f);
        shapeRenderer.setColor(yellow);
        shapeRenderer.rect(x, y, width * percentage, height);
        shapeRenderer.end();
    }

    public void drawAttackPow(float x, float y, float width, float height, float attackPow) {
        if (active != DrawPass.SHAPE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SHAPE", new IllegalStateException());
            return;
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Color yellow = new Color(244.0f / 255.0f, 208.0f / 255.0f, 63.0f / 255.0f, 1.0f);
        shapeRenderer.setColor(yellow);
        shapeRenderer.rect(x, y, width * Math.min(attackPow, 1.0f), height);
        shapeRenderer.end();
    }

    public void drawHpBar(float x, float y, float width, float height, float hp) {
        if (active != DrawPass.SHAPE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SHAPE", new IllegalStateException());
            return;
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        Color health;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (hp / Werewolf.INITIAL_HP < 0.5) {
            health = new Color(169.0f / 255.0f, 50.0f / 255.0f, 38.0f / 255.0f, 1.0f);
        } else {
            health = new Color(20.0f / 255.0f, 142.0f / 255.0f, 119.0f / 255.0f, 1.0f);
        }
        shapeRenderer.setColor(health);
        shapeRenderer.rect(x, y, width * hp / Werewolf.MAX_HP, height);
        shapeRenderer.end();

//        draw(icon, Color.WHITE, x - width, y, width, height);
    }

    public void drawAttackRange(float x, float y, float width, float height, float range) {
        if (active != DrawPass.SHAPE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SHAPE", new IllegalStateException());
            return;
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Color blue = new Color(40.0f / 255.0f, 116.0f / 255.0f, 166.0f / 255.0f, 1.0f);
        shapeRenderer.setColor(blue);
        shapeRenderer.rect(x, y, width * (range - 1.0f) / (Werewolf.MAX_RANGE - 1.0f), height);
        shapeRenderer.end();

//        draw(icon, Color.WHITE, x - width, y, width, height);
    }

    public void drawEnemyHpBars(float barWidth, float barHeight, Enemy enemy) {
        if (active != DrawPass.SHAPE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SHAPE", new IllegalStateException());
            return;
        }
        float x = WorldToScreenX(enemy.getPosition().x);

        float y = WorldToScreenY(enemy.getPosition().y);

        // Draw border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x - 0.7f * barWidth, y + 2 * barWidth, barWidth, barHeight);
        shapeRenderer.end();

        // Draw the actual health bar
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);

        shapeRenderer.rect(x - 0.7f * barWidth, y + 2 * barWidth, barWidth * enemy.getHealthPercentage(), barHeight);
        shapeRenderer.end();
    }

    public void drawAttackCooldownBar(float barWidth, float barHeight, float xOffset, Werewolf werewolf) {
        if (active != DrawPass.SHAPE) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin() for SHAPE", new IllegalStateException());
            return;
        }

        float x = WorldToScreenX(werewolf.getPosition().x) + xOffset;
        float y = WorldToScreenY(werewolf.getPosition().y);

        // Draw border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        shapeRenderer.end();

        // Draw the actual cooldown bar
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);

        shapeRenderer.rect(x, y, barWidth, barHeight * werewolf.getCooldownPercent());
        shapeRenderer.end();
    }

    /**
     * Flashes the screen. Normally this draws transparent (alpha = 0) rect, but when screen flashes, draws another
     * colr based on the flash.
     */
    public void drawScreenFlash(Werewolf player) {
        Color flashColor = ScreenFlash.getFlashColor();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(flashColor);

        float x = WorldToScreenX(player.getPosition().x) - Gdx.graphics.getWidth() / 2.0f;
        float y = WorldToScreenY(player.getPosition().y) - Gdx.graphics.getHeight() / 2.0f;
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();
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
     * Draws a solid rectangle at upper right corner with specified idth and height
     */
    public void drawRec(float width, float height) {
        ShapeRenderer barRenderer = new ShapeRenderer();
        barRenderer.begin(ShapeRenderer.ShapeType.Filled);
        barRenderer.setColor(Color.YELLOW);
        float x = getWidth() - width;
        float y = getHeight() - height * 4;
        barRenderer.rect(x, y, width, height);
        barRenderer.end();
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
     * Draws a rectangle outline at the upper right corner with specified width, and height
     */
    public void drawRecLine(float width, float height) {
        ShapeRenderer barRenderer = new ShapeRenderer();
        barRenderer.begin(ShapeRenderer.ShapeType.Line);
        barRenderer.setColor(Color.WHITE);
        float x = getWidth() - width;
        float y = getHeight() - height * 4;
        barRenderer.rect(x, y, width, height);
        barRenderer.end();
    }

    /**
     * Start the debug drawing sequence.
     * <p>
     * Nothing is flushed to the graphics card until the method end() is called.
     * =======
     * Draws a rectangle outline affected by global transform.
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
     * Draws a rectangle outline at the upper right corner
     */
    public void drawRecOutline(float width, float height) {
        drawRecOutline(getWidth() - width, getHeight() - height * 4, width, height, Color.WHITE);
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

    public OrthographicCamera getCamera() {
        return camera;
    }
}