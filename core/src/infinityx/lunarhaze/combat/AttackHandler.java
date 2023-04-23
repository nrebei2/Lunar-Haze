package infinityx.lunarhaze.combat;

import infinityx.lunarhaze.models.AttackingGameObject;

/**
 * Base model class for all attack/combat systems.
 */
public abstract class AttackHandler {

    /**
     * Counter for attacking (used to determine when to set attacking to false)
     */
    protected float attackCounter;

    /**
     * Counter for attack cooldowns
     */
    protected float attackCooldownCounter;


    /**
     * Attacking entity this class is controlling
     */
    protected AttackingGameObject entity;


    /**
     * @param entity attacking entity this class is controlling
     */
    protected AttackHandler(AttackingGameObject entity) {
        attackCounter = 0f;
        attackCooldownCounter = entity.attackCooldown;
        this.entity = entity;
    }


    /**
     * @return whether a new attack can be started
     */
    protected boolean canStartNewAttack() {
        return attackCooldownCounter >= entity.attackCooldown;
    }

    /**
     * Initiates an attack
     */
    protected void initiateAttack() {
        entity.setAttacking(true);
        entity.setImmune();
        attackCooldownCounter = 0f;
    }

    /**
     * Processes an attack, called every frame while attacking.
     */
    protected void processAttack(float delta) {
        attackCounter += delta;
        updateHitboxPosition();
        if (attackCounter >= entity.attackLength) {
            endAttack();
        }
    }

    /**
     * Called when an attack ends
     */
    protected void endAttack() {
        entity.setAttacking(false);
        attackCounter = 0f;
    }

    /**
     * Adjusts hitbox based on {@link #entity} transform
     */
    private void updateHitboxPosition() {
        // This is the logic that makes the hitbox "parented" to the entity
        entity.getAttackHitbox()
                .getBody()
                .setTransform(
                        entity.getPosition(),
                        entity.getAngle()
                );
    }
}
