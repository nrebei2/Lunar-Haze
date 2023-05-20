package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.Animation;
import infinityx.lunarhaze.graphics.FilmStrip;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.util.Drawable;

/**
 * Represents a foreground object.
 * Buildings, trees, large rocks etc.
 */
public class SceneObject extends GameObject implements Drawable {

    /**
     * Name of object from json
     */
    private final String type;

    /**
     * Whether the texture is flipped horizontally
     */
    private boolean flipped;

    /**
     * Whether you can see past this object
     */
    private boolean seeThru;

    /**
     * Cache for position since static (other than in editor)
     */
    private Vector2 cachedPosition;

    /**
     * dirty bit for position
     */
    private boolean dirty;

    public Leaf[] leaves;

    private LevelContainer container;

    private float emitTimer = 0;
    private static final float MIN_EMIT_DELAY = 1f;
    private static final float MAX_EMIT_DELAY = 1.5f;

    /**
     * Initialize a scene object.
     */
    public SceneObject(float x, float y, String type) {
        super(x, y);
        this.type = type;
        this.flipped = false;
        this.seeThru = false;
        this.emitTimer = MathUtils.random(MIN_EMIT_DELAY, MAX_EMIT_DELAY);
    }

    /**
     * Set whenever the position of this object changes
     */
    public void setDirty() {
        this.dirty = true;
    }

    @Override
    public Vector2 getPosition() {
        if (!dirty)
            return positionCache;
        positionCache.set(super.getPosition());
        dirty = false;
        return positionCache;
    }

    @Override
    public float getY() {
        return getPosition().y;
    }

    /**
     * Initialize scene object with dummy position
     */
    public SceneObject(String type) {
        this(0, 0, type);
    }

    @Override
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);
        this.container = container;
        if (json != null) {
            if (json.has("overlook")) {
                seeThru = json.getBoolean("overlook");
            }
            if (json.has("leaves")) {
                int count = Math.min(Leaf.MAX, 5);
                if (count == 0) return;
                leaves = new Leaf[count];
                for (int i = 0; i < count; i++) {
                    Leaf leaf = new Leaf();
                    leaves[i] = leaf;
                    Animation animation = new Animation();
                    animation.addAnimation("main", directory.getEntry(json.getString("leaves"), FilmStrip.class), 0.12f);
                    animation.setCurrentAnimation("main");
                    leaf.setTexture(animation);
                    leaf.setDestroyed(true);
                    leaf.setTextureScale(1);
                    Leaf.MAX--;
                }
            }
        }
    }

    public void emitLeaf(float deltaTime) {
        emitTimer -= deltaTime;
        if (emitTimer <= 0) {
            for (Leaf leaf : leaves) {
                if (!leaf.active && leaf.isDestroyed()) {
                    leaf.reset();
                    leaf.active = true;
                    leaf.setX(getX() + getBoundingRadius() * 4 * (2 * MathUtils.random() - 1));
                    leaf.setY(getY());
                    leaf.setZ(MathUtils.random(2, 5));
                    leaf.setVelocity(MathUtils.random(0.1f, 0.2f), MathUtils.random(-0.3f, -0.2f), MathUtils.random(-1.25f, -0.75f));
                    leaf.setScale(MathUtils.random(0.5f, 1));
                    leaf.setFadeRange(0.5f, 1f);
                    container.addDrawables(leaf);
                    // Reset timer with a new random delay
                    emitTimer = MathUtils.random(MIN_EMIT_DELAY, MAX_EMIT_DELAY);
                    break;
                }
            }
        }
    }

    /**
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    @Override
    public ObjectType getType() {
        return ObjectType.SCENE;
    }

    public String getSceneObjectType() {
        return type;
    }

    /**
     * Call after {@link #initialize(AssetDirectory, JsonValue, LevelContainer)}.
     *
     * @param flipped whether the texture should be flipped horizontally
     */
    public void setFlipped(boolean flipped) {
        if (this.flipped == flipped) return;
        this.flipped = flipped;

        // Flip the collider offset
        ShapeCache bodyInfo = getShapeInformation("body");
        if (bodyInfo == null) return; // Prolly a better way to do this
        if (bodyInfo.shape.getType() == Shape.Type.Polygon && !bodyInfo.offset.isZero())
            resizeBox("body", bodyInfo.width, bodyInfo.height, bodyInfo.offset.scl(-1, 1));
    }

    public boolean isFlipped() {
        return flipped;
    }

    public boolean isSeeThru() {
        return seeThru;
    }

    @Override
    public void draw(GameCanvas canvas) {
        // looks kinda ass
        if (!getSceneObjectType().equalsIgnoreCase("fencey"))
            drawShadow(canvas);

        // updating in draw, idgaf
        if (leaves != null) {
            for (Leaf leaf: leaves) {
                leaf.update(Gdx.graphics.getDeltaTime());
            }
            emitLeaf(Gdx.graphics.getDeltaTime());
        }

        Vector2 pos = getPosition();
        filmstrip = animation.getKeyFrame(Gdx.graphics.getDeltaTime());
        boolean drawn = canvas.draw(filmstrip, tint, origin.x, origin.y,
                canvas.WorldToScreenX(pos.x), canvas.WorldToScreenY(pos.y), 0.0f,
                (flipped ? -1 : 1) * textureScale * scale, textureScale * scale);

        if (drawn && !canvas.playerCoords.epsilonEquals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)) {
            // ugly but it is what it is
            float recX = canvas.WorldToScreenX(pos.x)
                    - origin.x * (flipped ? -1 : 1) * textureScale * scale
                    - (flipped ? 1 : 0) * getTextureWidth();
            float recY = canvas.WorldToScreenY(pos.y) - origin.y * textureScale * scale;
            float width = getTextureWidth();
            float height = getTextureHeight();

            boolean playerInside =
                    recX <= canvas.playerCoords.x
                            && recX + width >= canvas.playerCoords.x
                            && recY <= canvas.playerCoords.y
                            && recY + height >= canvas.playerCoords.y;

            if (playerInside) {
                tint.a = Math.max(0.65f, tint.a - 0.01f);
            } else {
                tint.a = Math.min(1f, tint.a + 0.01f);
            }
        }

    }

    /**
     * Draws a nice shadow for the object
     */
    private void drawShadow(GameCanvas canvas) {
        Vector2 pos = getPosition();

        canvas.draw(filmstrip, canvas.SHADE, flipped ? filmstrip.getRegionWidth() - origin.x : origin.x, origin.y,
                canvas.WorldToScreenX(pos.x), canvas.WorldToScreenY(pos.y), 0.0f,
                textureScale * scale, textureScale * scale * canvas.shadowScale, canvas.shadowShear, 0, flipped);

    }
}
