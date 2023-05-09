package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.combat.AttackHitbox;
import infinityx.lunarhaze.models.LevelContainer;

public class Villager extends Enemy {
    /**
     * Hitbox parented to the entity. Only active when {@link #isAttacking}
     */
    public AttackHitbox attackHitbox;

    public Villager() {
        super();
    }

    @Override
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);

        JsonValue attack = json.get("attack");
        JsonValue hitboxInfo = attack.get("hitbox");

        // Create the hitbox
        attackHitbox = new AttackHitbox(this);
        attackHitbox.initialize(directory, hitboxInfo, container);
        updateStrafeDistance();
    }

    @Override
    public float getAttackRange() {
        return attackHitbox.getHitboxRange();
    }

    /**
     * Updates the reach of the entity's attack hitbox
     *
     * @param attackRange new range in world length
     */
    public void setAttackRange(float attackRange) {
        attackHitbox.setHitboxRange(attackRange);
    }

    /**
     * Set whether the entity is currently attacking
     */
    public void setAttacking(boolean value) {
        isAttacking = value;
        attackHitbox.setActive(value);
        attackHitbox.getTexture().setFrame(0);
    }

//    @Override
//    public void updateAttack(float delta) {
//
//    }

    public AttackHitbox getAttackHitbox() {
        return attackHitbox;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
//        this.attackHitbox.updateHitboxPosition();
//        this.attackHitbox.update(delta);

    }

    @Override
    public EnemyType getEnemyType() {
        return EnemyType.Villager;
    }
}
