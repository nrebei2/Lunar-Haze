package infinityx.lunarhaze.combat;

import com.badlogic.gdx.math.Vector2;

/**
 * Base model class for all attack/combat systems.
 */
public abstract class AttackHandler {

    /**
     * Cooldown between attacks
     */
    protected float attackCooldown;

    /**
     * Attack direction as vector
     */
    protected Vector2 attackDirection;

    /**
     * Counter for attacking (used to determine when to set attacking to false)
     */
    protected float attackCounter;

    /**
     * Length of an attack
     */
    protected float attackLength;

    /**
     * Counter for attack cooldowns
     */
    protected float attackCooldownCounter;

    protected AttackHandler(float attackCooldown, float attackLength) {
        this.attackCooldown = attackCooldown;
        attackDirection = new Vector2();
        attackCounter = 0f;
        attackCooldownCounter = attackCooldown;
        this.attackLength = attackLength;
    }

    /**
     * Processes an attack, called every frame while attacking.
     * This implementation just calls endAttack() when the attack
     * should end.
     */
    protected void processAttack(float delta) {
        attackCounter += delta;
        if (attackCounter >= attackLength) {
            endAttack();
        }
    }

    /**
     * Called when an attack ends
     */
    protected void endAttack() {
        attackCounter = 0f;
    }

    /**
     * @return if a new attack can be started
     */
    protected boolean canStartNewAttack() {
        return attackCooldownCounter >= attackCooldown;
    }

    /**
     * Initiates an attack
     */
    protected void initiateAttack() {
        attackCooldownCounter = 0f;
    }

}
