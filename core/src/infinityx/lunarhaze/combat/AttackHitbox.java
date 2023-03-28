package infinityx.lunarhaze.combat;

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
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(attacker.getX(), attacker.getY());

        // Create shape
        PolygonShape hitboxShape = new PolygonShape();
        hitboxShape.setAsBox(hitboxSize / 2, hitboxSize / 2);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = hitboxShape;
        fixtureDef.isSensor = true;

        body = world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        body.setUserData(this);
        body.setActive(false);
        body.setBullet(true); // For better collision detection

        hitboxShape.dispose();
        return false;
    }
}