package infinityx.lunarhaze.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.controllers.GameplayController;
import infinityx.lunarhaze.controllers.InputController;
import infinityx.lunarhaze.models.entity.Werewolf;

/**
 * Handles all attacking for the player.
 * The player additionally has a three-part combo attack system.
 */
public class PlayerAttackHandler extends MeleeHandler {

    /**
     * Dash variables
     */
    private static final float DASH_TIME = 0.05f;
    private float dashTimer;
    private Vector2 dashDirection;
    public boolean isDashing;
    public static float DASH_COOLDOWN_BATTLE = 5.0f;
    public static float DASH_COOLDOWN_STEALTH = 10.0f;

    public static final float DASH_REDUCE_AMOUNT = 0.5f;
    private float dashCooldownCounter;

    private boolean heavyAttacking;
    private boolean windingUpHeavyAttack;
    private float heavyAttackWindupTimer;
    private static final float HEAVY_ATTACK_WINDUP_TIME = 0.5f;

    private boolean useRightHand;

    /**
     * Creates a specialized attack system for the given player
     */
    public PlayerAttackHandler(Werewolf player, AttackHitbox hitbox) {
        super(player, hitbox);
        dashDirection = new Vector2();
        isDashing = false;
        dashCooldownCounter = DASH_COOLDOWN_STEALTH;
        heavyAttacking = false;
        windingUpHeavyAttack = false;
        heavyAttackWindupTimer = 0;
        useRightHand = false;
    }

    public float getDashCooldownCounter() {
        return dashCooldownCounter;
    }

    //TODO: Make the attack cooldowns and attack lengths decrease with moonlight collected

    /**
     * See {@link #update(float)}
     *
     * @param phase current phase of the game
     */
    public void update(float delta, GameplayController.Phase phase) {
        // Safe
        Werewolf player = (Werewolf) entity;
        if (phase == GameplayController.Phase.BATTLE) {
            super.update(delta);
            //update hitbox
            if (player.isAttacking()) {
                processAttack(delta);
            }

            // Winding up logic
            if (windingUpHeavyAttack) {
                heavyAttackWindupTimer += delta;
                if (heavyAttackWindupTimer >= HEAVY_ATTACK_WINDUP_TIME) {
                    System.out.println("Starting heavy attack");
                    initiateHeavyAttack();
                    windingUpHeavyAttack = false;
                }
            }

            // Do not attack when locked out
            else if (!player.isLockedOut() && !player.isHeavyLockedOut()) {

                if (InputController.getInstance().didAttack() && !player.isAttacking() && canStartNewAttack()) {
                    initiateAttack();

                } else if (InputController.getInstance().didHeavyAttack() && !player.isAttacking()) {
                    initiateWindup();
                }

            }

        }

        // Dash logic
        float dashCD = (phase == GameplayController.Phase.BATTLE ? DASH_COOLDOWN_BATTLE : DASH_COOLDOWN_STEALTH);

        if (isDashing) {
            processDash(dashDirection);
        }
        if (dashCooldownCounter < dashCD) {
            dashCooldownCounter += delta;
        }
        // Initiate dash based on input
        if (InputController.getInstance().didDash() && !player.isAttacking() && dashCooldownCounter >= dashCD) {
            initiateDash(InputController.getInstance());
        }

    }

    public void initiateAttack() {
        // movement component
//        entity.getBody().applyLinearImpulse(entity.getLinearVelocity().nor(), entity.getBody().getWorldCenter(), true);
        super.initiateAttack();
    }

    /**
     * Initiates windup component/channel time of heavy attack
     */
    public void initiateWindup() {
        heavyAttackWindupTimer = 0;
        windingUpHeavyAttack = true;
    }

    public void initiateHeavyAttack() {
        Werewolf player = (Werewolf) entity;
        player.attackDamage *= 2;
        player.attackKnockback *= 2;
        player.getAttackHitbox().setHitboxRange(player.getAttackHitbox().getHitboxRange() * 1.5f);
        heavyAttacking = true;

        initiateAttack();
    }

    @Override
    protected void endAttack() {
        super.endAttack();
        Werewolf player = (Werewolf) entity;
        if (heavyAttacking) {
            // Reset damage and knockback to their original values
            player.attackDamage /= 2;
            player.attackKnockback /= 2;
            player.getAttackHitbox().setHitboxRange(player.getAttackHitbox().getHitboxRange() / 1.5f);

            // Lock the player out after a heavy attack
            player.setHeavyLockedOut();

            heavyAttacking = false;
        } else {
            // If light attacking toggle hand
            useRightHand = !useRightHand;
        }
    }

    /**
     * Initiates a dash
     */
    private void initiateDash(InputController input) {
        if (!isDashing) {
            isDashing = true;
            dashDirection = new Vector2(input.getHorizontal(), input.getVertical()).nor();
            dashTimer = 0f;
            entity.setImmune();
            entity.setLockedOut();
            ((Werewolf) entity).setTargetStealth(((Werewolf) entity).getTargetStealth() + 0.2f);
        }
    }

    private void processDash(Vector2 direction) {
        entity.getBody().applyLinearImpulse(direction.x, direction.y, entity.getX(), entity.getY(), true);
        dashTimer += Gdx.graphics.getDeltaTime();
        if (dashTimer >= DASH_TIME) {
            endDash();
        }
    }

    private void endDash() {
        dashCooldownCounter = 0f;
        isDashing = false;
        ((Werewolf) entity).setTargetStealth(((Werewolf) entity).getTargetStealth() - 0.2f);
    }

    public boolean isHeavyAttacking() {
        return heavyAttacking;
    }

    public boolean isWindingUpHeavyAttack() {
        return windingUpHeavyAttack;
    }

    public boolean useRightHand() {
        return useRightHand;
    }
}