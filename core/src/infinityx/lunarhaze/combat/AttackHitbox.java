package infinityx.lunarhaze.combat;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import infinityx.lunarhaze.GameObject;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class AttackHitbox extends GameObject {
    private final float hitboxSize;
    private final GameObject attacker;

    public AttackHitbox(float hitboxSize, GameObject attacker) {
        super(0, 0);
        this.hitboxSize = hitboxSize;
        this.attacker = attacker;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.HITBOX;
    }

    public boolean activatePhysics(World world) {
        setBodyType(BodyDef.BodyType.DynamicBody);
        setPosition(attacker.getX(), attacker.getY());

        // Create shape
        addBox("body", hitboxSize, hitboxSize, new Vector2(), 0);

        super.activatePhysics(world);
        body.setBullet(true); // For better collision detection

        return false;
    }
}