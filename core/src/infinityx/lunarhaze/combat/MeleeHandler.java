package infinityx.lunarhaze.combat;

import com.badlogic.gdx.Gdx;
import infinityx.lunarhaze.models.AttackingGameObject;

/**
 * AttackHandler specialized for an attacker holding an attack hitbox
 */
public class MeleeHandler extends AttackHandler {
    /**
     * Hitbox attached to entity
     */
    private AttackHitbox hitbox;

    /**
     * @param entity attacking entity this class is controlling
     * @param hitbox hitbox attached to entity
     */
    public MeleeHandler(AttackingGameObject entity, AttackHitbox hitbox) {
        super(entity);
        this.hitbox = hitbox;
    }

    @Override
    public void initiateAttack() {
        super.initiateAttack();
        hitbox.animation.reset();
        hitbox.setActive(true);
        hitbox.updateHitboxPosition();
    }

    @Override
    public void processAttack(float delta) {
        super.processAttack(delta);
        hitbox.updateHitboxPosition();
        hitbox.update(delta);
    }

    @Override
    protected void endAttack() {
        super.endAttack();
        hitbox.setActive(false);
    }
}
