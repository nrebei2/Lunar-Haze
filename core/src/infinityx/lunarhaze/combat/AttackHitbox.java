package infinityx.lunarhaze.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.AttackingGameObject;
import infinityx.lunarhaze.models.GameObject;

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
     * @param initialSize holds initial reach and width of hitbox respectively
     * @param attacker    entity this hitbox is parented to
     */
    public AttackHitbox(Vector2 initialSize, AttackingGameObject attacker) {
        super(0, 0);
        this.attacker = attacker;

        // Important! So that rotation rotates around attacker
        setPosition(attacker.getPosition());

        // Since I'm setting the position of the hitbox to the position of the attacker, I have to offset it
        // I offset it so that it is externally tangent to the body of the attacker
        addBox(
                "body", initialSize.x, initialSize.y,
                new Vector2(attacker.getBoundingRadius() + initialSize.x / 2, 0),
                0
        );
    }

    public AttackingGameObject getAttacker() {
        return attacker;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.HITBOX;
    }

    @Override
    public boolean activatePhysics(World world) {
        super.activatePhysics(world);

        // Enable better collision detection for fast-moving objects
        setBullet(true);
        // Make the hitbox a sensor, so it doesn't push around other objects
        setSensor(true);
        return true;
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
     * Draws the hitbox outline in red using {@link GameCanvas#drawPhysics(PolygonShape, Color, float, float, float, float, float)}
     *
     * @param canvas canvas to draw on
     */
    public void drawHitbox(GameCanvas canvas) {
        canvas.drawPhysics((PolygonShape) getShapeInformation("body").shape, Color.RED,
                getPosition().x, getPosition().y,
                getAngle(), canvas.WorldToScreenX(1), canvas.WorldToScreenY(1)
        );
    }

    @Override
    public float getDepth() {
        // Must account offset from position
        return super.getDepth() + MathUtils.sin(getAngle()) * getHitboxRange();
    }

    @Override
    public void draw(GameCanvas canvas) {
        // This is an OK solution
        // Could remove/add from container but how many colliders are there going to be at a single time anyway?
        if (!isActive()) {
            return;
        }
        // Add 90 degrees since sprite is facing down
        canvas.draw(filmstrip, tint, origin.x, origin.y,
                canvas.WorldToScreenX(getPosition().x), canvas.WorldToScreenY(getPosition().y), getAngle() + MathUtils.PI / 2,
                textureScale * scale, textureScale * scale);
    }
}