package infinityx.lunarhaze.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.AttackingGameObject;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Werewolf;

/**
 * This class represents a rectangular attack hitbox that is parented to an entity,
 * which is responsible for existing so that the {@link infinityx.lunarhaze.controllers.CollisionController}
 * can handle collisions between this and other entities.
 */
public class AttackHitbox extends GameObject {

    /**
     * The entity that initiated the attack and this hitbox is parented to.
     * Needed for the collision controller.
     */
    private final AttackingGameObject attacker;

    /**
     * @param attacker entity this hitbox is parented to
     */
    public AttackHitbox(AttackingGameObject attacker) {
        super(0, 0);
        this.attacker = attacker;
        setLoop(false);

        // Important! So that rotation rotates around attacker
        setPosition(attacker.getPosition());
    }


    @Override
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);

        float attackRange = json.getFloat("range");
        // width is defaulted to the entity's body diameter
        float attackWidth = json.has("width") ? json.getFloat("width") : getBoundingRadius() * 2;

        // Since I'm setting the position of the hitbox to the position of the attacker, I have to offset it
        // I offset it so that it is externally tangent to the body of the attacker
        addBox(
                //bad name ignore yellow box
                "body", attackRange, attackWidth,
                new Vector2(attacker.getBoundingRadius() + attackRange / 2, 0),
                0
        );

        if (body == null) activatePhysics(container.getWorld());
        this.setActive(false);
    }


    public AttackingGameObject getAttacker() {
        return attacker;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.HITBOX;
    }

    /**
     * @return The current range (width) of this hitbox
     */
    public float getHitboxRange() {
        return getShapeInformation("body").width;
    }

    /**
     * Updates the range of this hitbox. The entire hitbox will be scaled, thus the width will be updated as well.
     *
     * @param range new range in world length
     */
    public void setHitboxRange(float range) {
        setScale(scale * range / getHitboxRange());
    }

    /**
     * @return The current range (width) of this hitbox
     */
    public float getHitboxWidth() {
        return getShapeInformation("body").width;
    }

    /**
     * Updates the range of this hitbox. The entire hitbox will be scaled, thus the width will be updated as well.
     *
     * @param width new range in world length
     */
    public void setHitboxWidth(float width) {
        setScale(scale * width / getHitboxWidth());
    }

    /**
     * Draws the hitbox outline in red using {@link GameCanvas#drawPhysics(PolygonShape, Color, float, float, float, float, float)}
     *
     * @param canvas canvas to draw on
     */
    public void drawHitbox(GameCanvas canvas) {
        canvas.drawPhysics((PolygonShape) getShapeInformation("body").shape, Color.RED,
                getPosition().x, getPosition().y,
                getAngle(), 1, 1
        );
    }

    @Override
    public float getDepth() {
        // Must account offset from position
        return super.getDepth() + MathUtils.sin(getAngle()) * getHitboxRange();
    }

    @Override
    public void draw(GameCanvas canvas) {
        // Could remove/add from container but how many colliders are there going to be at a single time anyway?
        if (!attacker.isAttacking()) {
            return;
        }

        // Add 90 degrees since sprite is facing down
        float angle = getAngle() + MathUtils.PI / 2;
        float offsetX = 0;
        // Fake height
        float offsetY = 0.2f;

        // Heavy attack should face down and should not have fake height
        if (attacker instanceof Werewolf && ((Werewolf) attacker).isHeavyAttacking) {
            angle = 0;
            offsetX = -0.7f;
            offsetY = 0;
        }

        canvas.draw(filmstrip, tint, origin.x, origin.y,
                canvas.WorldToScreenX(getPosition().x + offsetX), canvas.WorldToScreenY(getPosition().y + offsetY), angle,
                textureScale * scale, textureScale * scale);
    }

    /**
     * Adjusts hitbox based on {@link #attacker} transform
     */
    public void updateHitboxPosition() {
        // This is the logic that makes the hitbox "parented" to the entity
        getBody().setTransform(attacker.getPosition(), attacker.getAngle());

    }
}