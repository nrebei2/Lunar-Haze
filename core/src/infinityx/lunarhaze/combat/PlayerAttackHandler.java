package infinityx.lunarhaze.combat;

import com.badlogic.gdx.Gdx;
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
     * Current hand for attack sprite
     */
    private int hand;


    /**
     * Dash variables
     */
    private static final float DASH_SPEED = 10f;
    private static final float DASH_TIME = 0.15f;
    private float dashTimer;
    private Vector2 dashDirection;
    private boolean isDashing;
    public static final float DASH_COOLDOWN = 3.0f;
    private float dashCooldownCounter;

    /**
     * Creates a specialized attack system for the given player
     */
    public PlayerAttackHandler(Werewolf player) {
        super(player);

        dashDirection = new Vector2();
        isDashing = false;
        dashCooldownCounter = DASH_COOLDOWN;
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

            if (InputController.getInstance().didAttack() && !player.isAttacking()) {
                    initiateAttack();
            }
        }

        // Dash logic

        if (isDashing) {
            processDash(dashDirection);
        }
        if (dashCooldownCounter < DASH_COOLDOWN) {
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
        entity.getBody().applyLinearImpulse(direction.x * 0.5f, direction.y * 0.5f, entity.getX(), entity.getY(), true);
        dashTimer += Gdx.graphics.getDeltaTime();
        if (dashTimer >= DASH_TIME) {
            endDash();
        }
    }

    private void endDash() {
        dashCooldownCounter = 0f;
        isDashing = false;
    }
}