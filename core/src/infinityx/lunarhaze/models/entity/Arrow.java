package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.models.LevelContainer;

public class Arrow extends GameObject {

    /**
     * Reference to the archer that drew this arrow
     */
    Archer archer;

    /** Whether the arrow is currently stuck to the player or scene object */
    private boolean isSticking = false;

    /// Used when is sticking
    private Werewolf stickLocation;
    private Vector2 cache;
    private float offset;
    private float fadeTime = 0f;
    private static final float FADE_DURATION = 10f;

    @Override
    public ObjectType getType() {
        return ObjectType.ARROW;
    }

    /**
     * Initialize an arrow
     */
    public Arrow(float x, float y, Archer archer) {
        super(x, y);
        this.archer = archer;
        setLoop(false);
        cache = new Vector2();
    }

    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);
        this.setActive(false);

    }

    public Archer getArcher() {
        return archer;
    }

    public void setArcher(Archer archer) {
        this.archer = archer;
    }

    @Override
    public void draw(GameCanvas canvas) {
        // updating in draw idGAF
        update(Gdx.graphics.getDeltaTime());
        canvas.draw(filmstrip, tint, origin.x, origin.y,
                canvas.WorldToScreenX(getPosition().x), canvas.WorldToScreenY(getPosition().y + 0.55f), getAngle() + MathUtils.PI,
                textureScale * scale, textureScale * scale);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (isSticking) {
            if (isActive()) {
                setActive(false);
            }
            // Stick to werewolf
            if (stickLocation != null) {
                float angle = -(stickLocation.getDirection().toAngle() + offset);
                setAngle(angle);

                setPosition(
                        stickLocation.angleToVector(cache, angle)
                                .scl(-0.08f)
                                .sub(0.4f * MathUtils.cos(angle), 0.4f * MathUtils.sin(angle) * 4 / 3)
                                .add(stickLocation.getPosition())
                );
            }

            // Fade
            fadeTime += delta;
            float progress = Interpolation.exp10In.apply(fadeTime / FADE_DURATION);
            if (progress >= 1f) {
                // Once we reach or exceed our fade duration
                setDestroyed(true);
            } else {
                tint.a = 1f - progress;
            }
        }
    }

    /**
     * Stick to werewolf and begin fading out
     */
    public void beginDestruction(Werewolf o2) {
        this.offset = -getAngle() - o2.getDirection().toAngle();
        stickLocation = o2;

        beginDestruction();
    }

    /**
     * Stop moving and fade out
     */
    public void beginDestruction() {
        setLinearVelocity(Vector2.Zero);
        isSticking = true;
    }
}
