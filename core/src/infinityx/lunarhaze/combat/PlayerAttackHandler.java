package infinityx.lunarhaze.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.controllers.GameplayController;
import infinityx.lunarhaze.controllers.InputController;
import infinityx.lunarhaze.controllers.PlayerController;
import infinityx.lunarhaze.controllers.PlayerState;
import infinityx.lunarhaze.models.entity.Werewolf;

/**
 * Handles all attacking for the player by extending the base
 * model class AttackHandler. Compared to AttackHandler, the
 * player has a three-part combo attack system.
 */
public class PlayerAttackHandler extends AttackHandler {

    /**
     * Dash variables
     */
    private static final float DASH_TIME = 0.15f;
    private float dashTimer;
    private Vector2 dashDirection;
    private boolean isDashing;
    public static float DASH_COOLDOWN = 5.0f;
    public static final float DASH_REDUCE_AMOUNT = 0.5f;
    private float dashCooldownCounter;

    private boolean heavyAttacking;
    private boolean windingUpHeavyAttack;
    private float heavyAttackWindupTimer;
    private static final float HEAVY_ATTACK_WINDUP_TIME = 0.5f;

    private StateMachine<PlayerController, PlayerState> stateMachine;

    /**
     * Creates a specialized attack system for the given player
     */
    public PlayerAttackHandler(Werewolf player, StateMachine<PlayerController, PlayerState> stateMachine) {
        super(player);
        this.stateMachine = stateMachine;
        dashDirection = new Vector2();
        isDashing = false;
        dashCooldownCounter = DASH_COOLDOWN;
        heavyAttacking = false;
        windingUpHeavyAttack = false;
        heavyAttackWindupTimer = 0;
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

                if (InputController.getInstance().didAttack() && !player.isAttacking()) {
                    initiateAttack();
                } else if (InputController.getInstance().didHeavyAttack() && !player.isAttacking()) {
                    initiateWindup();
                }

            }

        }

        // Dash logic

        if (isDashing) {
            processDash(dashDirection);
        }
        if (dashCooldownCounter < (phase == GameplayController.Phase.BATTLE ? DASH_COOLDOWN : DASH_COOLDOWN * 2)) {
            dashCooldownCounter += delta;
        }
        // Initiate dash based on input
        if (InputController.getInstance().didRun() && !player.isAttacking() && dashCooldownCounter >= DASH_COOLDOWN) {
            initiateDash(InputController.getInstance());
        }

    }

    public void initiateAttack() {
        // movement component
        entity.getBody().applyLinearImpulse(entity.getLinearVelocity().nor(), entity.getBody().getWorldCenter(), true);
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
        entity.attackDamage *= 2;
        entity.attackKnockback *= 2;
        entity.getAttackHitbox().setHitboxRange(entity.getAttackHitbox().getHitboxRange() * 1.5f);
        heavyAttacking = true;

        initiateAttack();
    }

    @Override
    protected void endAttack() {
        super.endAttack();

        if (heavyAttacking) {
            // Reset damage and knockback to their original values
            entity.attackDamage /= 2;
            entity.attackKnockback /= 2;
            entity.getAttackHitbox().setHitboxRange(entity.getAttackHitbox().getHitboxRange() / 1.5f);

            // Lock the player out after a heavy attack
            Werewolf player = (Werewolf) entity;
            player.setHeavyLockedOut();

            heavyAttacking = false;
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
        }
    }

    private void processDash(Vector2 direction) {
        entity.getBody().applyLinearImpulse(direction.x * 0.3f, direction.y * 0.3f, entity.getX(), entity.getY(), true);
        dashTimer += Gdx.graphics.getDeltaTime();
        if (dashTimer >= DASH_TIME) {
            endDash();
        }
    }

    private void endDash() {
        dashCooldownCounter = 0f;
        isDashing = false;
    }

    public boolean isHeavyAttacking() {
        return heavyAttacking;
    }

    public boolean isWindingUpHeavyAttack() {
        return windingUpHeavyAttack;
    }
}