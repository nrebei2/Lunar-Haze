package infinityx.lunarhaze.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.controllers.GameplayController;
import infinityx.lunarhaze.controllers.InputController;
import infinityx.lunarhaze.models.entity.Werewolf;

/**
 * Handles all attacking for the player by extending the base
 * model class AttackHandler. Compared to AttackHandler, the
 * player has a three-part combo attack system. Additionally
 * this model must set the player to be attacking while attacking
 * for collision purposes and determining who should take damage.
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

    /**
     * Reference to the player model
     */
    private Werewolf player;

    private static float attackPower;

    private static float attackRange;

    private static final float DASH_SPEED = 40f;
    private static final float DASH_TIME = 0.25f;
    private float dashTimer;
    private Vector2 dashDirection;
    private boolean isInvincible;
    private boolean isDashing;

    /**
     * Constructor that gets a reference to the player model
     */
    public PlayerAttackHandler(Werewolf p) {
        super(1f, 0.5f);
        player = p;
        comboAttackCooldownCounter = 0f;
        comboStep = 0;
        comboTime = 0f;
        attackPower = Werewolf.INITIAL_POWER;
        attackRange = Werewolf.INITIAL_RANGE;
        dashDirection = new Vector2();
        isInvincible = false;
        isDashing = false;
    }

    //TODO: Make the attack cooldowns and attack lengths decrease with moonlight collected

    /**
     * Called up above in the other update method, handles all attacking related logic
     */
    public void update(float delta, GameplayController.Phase phase) {
        InputController input = InputController.getInstance();
        if (phase == GameplayController.Phase.BATTLE) {
            if (isDashing) {
                processDash(dashDirection);
            }
            if (player.isAttacking()) {
                processAttack(delta, input);
            } else {
                attackCooldownCounter += delta;
            }

            if (comboStep > 0) {
                handleComboTimeout(delta);
            }

            if (canStartNewAttackOrContinueCombo()) {
                player.setDrawCooldownBar(false, 0);
                if (input.didAttack()) {
                    initiateAttack(input);
                }
            } else {
                if (comboStep == 0) {
                    player.setDrawCooldownBar(true, attackCooldownCounter / attackCooldown);
                } else {
                    // Will remove magic numbers later
                    player.setDrawCooldownBar(true, comboAttackCooldownCounter / 0.4f);
                }
            }

            if (input.didRun() && !player.isAttacking()) {
                System.out.println("Initiated dash");
                initiateDash(input);
            }
        }
    }

    /**
     * Processes an attack, called every frame while attacking
     */
    private void processAttack(float delta, InputController input) {
        player.setCanMove(false);
        updateHitboxPosition(input);
        super.processAttack(delta);
    }

    /**
     * Adjusts hitbox based on user input
     */
    private void updateHitboxPosition(InputController input) {
        player.attackHitbox.getBody().setTransform(player.getPosition().x + (input.getHorizontal() / 4.0f), player.getPosition().y + (input.getVertical() / 4.0f) + 1.0f, 0f);
    }

    /**
     * Called when an attack ends
     */
    public void endAttack() {
        super.endAttack();
        player.setAttacking(false);

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
     * Sets the attack cooldown
     */
    public void setAttackCooldown(float attackCooldown) {
        this.attackCooldown = attackCooldown;
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
        return (comboStep == 0 && canStartNewAttack() && !isDashing) // Can start a new attack
                || (comboStep > 0 && comboTime <= MAX_COMBO_TIME && comboAttackCooldownCounter >= 0.4f && !isDashing); // Can continue a combo
    }

    /**
     * Initiates an attack
     */
    private void initiateAttack(InputController input) {
        player.setAttacking(true);
        player.setCanMove(false); // Movement code in player sets velocity to 0 and overrides this so must not be able to move

        // movement component
        attackDirection.set(input.getHorizontal(), input.getVertical()).nor();
        player.getBody().applyLinearImpulse(attackDirection, player.getBody().getWorldCenter(), true);
        comboAttackCooldownCounter = 0f;
        player.setImmune(1f);
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
            isInvincible = true;
            player.setCanMove(false);
        }
    }

    private void processDash(Vector2 direction) {
        player.getBody().setLinearVelocity(direction.x * DASH_SPEED, direction.y * DASH_SPEED);
        dashTimer += Gdx.graphics.getDeltaTime();
        if (dashTimer >= DASH_TIME) {
            endDash();
        }
    }

    private void endDash() {
        player.getBody().setLinearVelocity(0, 0);
        isInvincible = false;
        isDashing = false;
        player.setCanMove(true);
    }

    /**
     * @return the attack power of the player
     */
    public static float getAttackPower() {
        return attackPower;
    }

    /**
     * Sets the attack power of the player
     */
    public static void setAttackPower(float power) {
        attackPower = power;
    }

    public static float getAttackRange() {
        return attackRange;
    }

    public static void setAttackRange(float range) {
        attackRange = range;
    }

}
