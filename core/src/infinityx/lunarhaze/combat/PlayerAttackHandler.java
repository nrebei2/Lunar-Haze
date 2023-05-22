package infinityx.lunarhaze.combat;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.audio.Mp3;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.controllers.GameplayController;
import infinityx.lunarhaze.controllers.InputController;
import infinityx.lunarhaze.models.entity.Werewolf;

import static java.lang.Math.pow;

/**
 * Handles all attacking for the player.
 * The player additionally has a three-part combo attack system.
 */
public class PlayerAttackHandler extends MeleeHandler {

    /**
     * Dash variables
     */
    private static final float DASH_TIME = 0.05f;
    private final Sound dashSound;
    private float dashTimer;
    private Vector2 dashDirection;
    public boolean isDashing;
    public static float DASH_COOLDOWN_BATTLE = 4f;
    public static float DASH_COOLDOWN_STEALTH = 8f;

    public static float DASH_IMPULSE_BATTLE = 3f;
    public static float DASH_IMPULSE_STEALTH = 4f;

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
    public PlayerAttackHandler(Werewolf player, AttackHitbox hitbox, Sound dashSound) {
        super(player, hitbox);
        dashDirection = new Vector2();
        isDashing = false;
        dashCooldownCounter = DASH_COOLDOWN_STEALTH;
        heavyAttacking = false;
        this.dashSound = dashSound;
        windingUpHeavyAttack = false;
        heavyAttackWindupTimer = 0;
        useRightHand = false;
    }

    public float getDashCooldownCounter() {
        return dashCooldownCounter;
    }

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
            processDash(dashDirection, delta);
        }
        if (dashCooldownCounter < dashCD) {
            dashCooldownCounter += delta;
        }
        // Initiate dash based on input
        if (InputController.getInstance().justDash() && !player.isAttacking() && dashCooldownCounter >= dashCD && !player.getLinearVelocity().isZero(0.05f)) {
            initiateDash(phase);
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
        player.setLinearVelocity(Vector2.Zero);
        player.attackDamage *= 1.75f;
        player.attackKnockback *= 1.75f;
        AttackHitbox hitbox = player.getAttackHitbox();
        hitbox.setHitboxRange(player.getAttackHitbox().getHitboxRange() * 1.25f);
        hitbox.setHitboxWidth(player.getAttackHitbox().getHitboxWidth() * 1.25f);

        hitbox.setTexture("hitbox_heavy");
        hitbox.setOrigin(420, 360);
        heavyAttacking = true;
        player.isHeavyAttacking = true;
        player.attackLength *= 1.5f;

        initiateAttack();
    }

    @Override
    protected void endAttack() {
        super.endAttack();
        Werewolf player = (Werewolf) entity;
        if (heavyAttacking) {
            // Reset damage and knockback to their original values
            player.attackDamage /= 1.75f;
            player.attackKnockback /= 1.75f;
            player.attackLength /= 1.5f;
            player.getAttackHitbox().setOrigin(280, 420);
            player.getAttackHitbox().setHitboxRange(player.getAttackHitbox().getHitboxRange() /1.25f);
            player.getAttackHitbox().setHitboxWidth(player.getAttackHitbox().getHitboxWidth() / 1.25f);
            player.getAttackHitbox().setTexture("hitbox");
            player.isHeavyAttacking = false;

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
    private void initiateDash(GameplayController.Phase phase) {
        if (!isDashing) {
            isDashing = true;
            ((Werewolf) entity).isDashing = true;
            dashDirection.set(
                    InputController.getInstance().getHorizontal(),
                    InputController.getInstance().getVertical()
            ).nor().scl(phase == GameplayController.Phase.STEALTH ? DASH_IMPULSE_STEALTH : DASH_IMPULSE_BATTLE);
            dashTimer = 0f;
            entity.setImmune();
            entity.setLockedOut();
            ((Werewolf) entity).setTargetStealth(((Werewolf) entity).getTargetStealth() + 0.2f);
            entity.getBody().applyLinearImpulse(dashDirection.x, dashDirection.y, entity.getX(), entity.getY(), true);
        }
    }

    private void processDash(Vector2 direction, float delta) {
        dashTimer += delta;
        if (dashTimer >= DASH_TIME) {
            endDash();
        }
    }

    private void endDash() {
        dashCooldownCounter = 0f;
        isDashing = false;
        ((Werewolf) entity).isDashing = false;
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