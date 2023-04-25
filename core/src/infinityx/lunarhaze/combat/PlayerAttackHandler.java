package infinityx.lunarhaze.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.controllers.GameplayController;
import infinityx.lunarhaze.controllers.InputController;
import infinityx.lunarhaze.models.entity.Werewolf;

/**
 * Handles all attacking for the player by extending the base
 * model class AttackHandler. Compared to AttackHandler, the
 * player has a three-part combo attack system.
 */
public class PlayerAttackHandler extends AttackHandler {

    /**
     * Counter for delay between combo attacks
     */
    private float comboAttackCooldownCounter;

    /**
     * Current combo step
     */
    private int comboStep;

    /**
     * Time since combo started
     */
    private float comboTime;

    /**
     * Max allowed time before combo timeout
     */
    private final static float MAX_COMBO_TIME = 1f;

    /** Dash variables */
    private static final float DASH_SPEED = 40f;
    private static final float DASH_TIME = 0.25f;
    private float dashTimer;
    private Vector2 dashDirection;
    private boolean isDashing;
    private static final float DASH_COOLDOWN = 3.0f;
    private float dashCooldownCounter;

    /**
     * Creates a specialized attack system for the given player
     */
    public PlayerAttackHandler(Werewolf player) {
        super(player);
        comboAttackCooldownCounter = 0f;
        comboStep = 0;
        comboTime = 0f;

        dashDirection = new Vector2();
        isDashing = false;
        dashCooldownCounter = DASH_COOLDOWN;
    }

    //TODO: Make the attack cooldowns and attack lengths decrease with moonlight collected

    /**
     * See {@link #update(float)}
     * @param phase current phase of the game
     */
    public void update(float delta, GameplayController.Phase phase) {
        if (phase == GameplayController.Phase.BATTLE) {
            super.update(delta);

            if (comboStep > 0) {
                handleComboTimeout(delta);
            }
            // Safe
            Werewolf player = (Werewolf) entity;
            if (canStartNewAttackOrContinueCombo()) {
                player.setDrawCooldownBar(false, 0);
                if (InputController.getInstance().didAttack()) {
                    initiateAttack();
                }
            } else if (!isDashing){
                if (comboStep == 0) {
                    player.setDrawCooldownBar(true, attackCooldownCounter / entity.attackCooldown);
                } else {
                    // Will remove magic numbers later
                    player.setDrawCooldownBar(true, comboAttackCooldownCounter / 0.4f);
                }
            }
            if (dashCooldownCounter < DASH_COOLDOWN) {
                dashCooldownCounter += delta;
            }
            // Initiate dash based on input
            if (InputController.getInstance().didRun() && !player.isAttacking() && dashCooldownCounter >= DASH_COOLDOWN) {
                initiateDash(InputController.getInstance());
            }
        }
    }

    public void endAttack() {
        super.endAttack();

        // Combo logic
        comboStep++;
        comboTime = 0f;
        // Step 3 is the last attack in the combo
        if (comboStep >= 3) {
            comboStep = 0;
            attackCooldownCounter = 0f;
        }
    }

    /**
     * Handles combo timeouts
     */
    private void handleComboTimeout(float delta) {
        comboTime += delta;
        comboAttackCooldownCounter += delta;

        if (comboTime >= MAX_COMBO_TIME) {
            comboStep = 0;
            comboTime = 0f;
            attackCooldownCounter = 0f;
        }
    }

    /**
     * Returns true if the player can start a new attack or continue a combo
     */
    public boolean canStartNewAttackOrContinueCombo() {
        return (comboStep == 0 && canStartNewAttack()) // Can start a new attack
                || (comboStep > 0 && comboTime <= MAX_COMBO_TIME && comboAttackCooldownCounter >= 0.4f); // Can continue a combo
    }

    protected void initiateAttack() {
        // movement component
        entity.getBody().applyLinearImpulse(entity.getLinearVelocity().nor(), entity.getBody().getWorldCenter(), true);
        comboAttackCooldownCounter = 0f;
        super.initiateAttack();
    }

    /**
     * Initiates a dash
     */
    private void initiateDash(InputController input) {
        if(!isDashing) {
            isDashing = true;
            dashDirection = new Vector2(input.getHorizontal(), input.getVertical()).nor();
            dashTimer = 0f;
            entity.setImmune();
            entity.setLockedOut();
        }
    }

    private void processDash(Vector2 direction) {
        entity.getBody().setLinearVelocity(direction.x * DASH_SPEED, direction.y * DASH_SPEED);
        dashTimer += Gdx.graphics.getDeltaTime();
        if (dashTimer >= DASH_TIME) {
            endDash();
        }
    }

    private void endDash() {
        dashCooldownCounter = 0f;
        entity.getBody().setLinearVelocity(0, 0);
        isDashing = false;
    }
}