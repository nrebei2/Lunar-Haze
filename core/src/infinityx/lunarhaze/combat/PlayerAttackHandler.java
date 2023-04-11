package infinityx.lunarhaze.combat;

import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.GameplayController;
import infinityx.lunarhaze.InputController;
import infinityx.lunarhaze.entity.Werewolf;

/** Handles all attacking for the player by extending the base
 *  model class AttackHandler. Compared to AttackHandler, the
 *  player has a three-part combo attack system. Additionally
 *  this model must set the player to be attacking while attacking
 *  for collision purposes and determining who should take damage.
 *
 */
public class PlayerAttackHandler extends AttackHandler {

    /** Counter for delay between combo attacks */
    private float comboAttackCooldownCounter;

    /** Current combo step */
    private int comboStep;

    /** Time since combo started */
    private float comboTime;

    /** Max allowed time before combo timeout */
    private final static float MAX_COMBO_TIME = 1f;

    /** Reference to the player model */
    private Werewolf player;

    private static float attackPower;

    private static float attackRange;

    /** Constructor that gets a reference to the player model */
    public PlayerAttackHandler(Werewolf p) {
        super(4f, 0.5f);
        player = p;
        comboAttackCooldownCounter = 0f;
        comboStep = 0;
        comboTime = 0f;
        attackPower = Werewolf.INITIAL_POWER;
        attackRange = Werewolf.INITIAL_RANGE;
    }

    //TODO: Make the attack cooldowns and attack lengths decrease with moonlight collected

    /** Called up above in the other update method, handles all attacking related logic */
    public void update(float delta, InputController input, GameplayController.Phase phase) {
        if (phase == GameplayController.Phase.BATTLE) {
            //System.out.println("Combo step: " + comboStep);
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
                if(comboStep == 0) {
                    player.setDrawCooldownBar(true, attackCooldownCounter / attackCooldown);
                } else {
                    // Will remove magic numbers later
                    player.setDrawCooldownBar(true, comboAttackCooldownCounter / 0.4f);
                }
            }
        }
    }

    /** Processes an attack, called every frame while attacking */
    private void processAttack(float delta, InputController input) {
        player.setCanMove(false);
        updateHitboxPosition(input);
        super.processAttack(delta);
    }

    /** Adjusts hitbox based on user input */
    private void updateHitboxPosition(InputController input) {
        player.attackHitbox.getBody().setTransform(player.getPosition().x + (input.getHorizontal() / 4.0f), player.getPosition().y + (input.getVertical() / 4.0f) + player.getTextureHeight()/2f, 0f);
    }

    /** Called when an attack ends */
    public void endAttack() {
        super.endAttack();
        player.setAttacking(false);

        // Combo logic
        comboStep++;
        comboTime = 0f;
        // Step 3 is the last attack in the combo
        if (comboStep >= 3) {
            //System.out.println("Combo step is bigger than 3");
            comboStep = 0;
            attackCooldownCounter = 0f;
        }

    }

    /** Sets the attack cooldown */
    public void setAttackCooldown (float attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    /** Handles combo timeouts */
    private void handleComboTimeout(float delta) {
        comboTime += delta;
        comboAttackCooldownCounter += delta;

        if (comboTime >= MAX_COMBO_TIME) {
            //System.out.println("Combo timed out");
            comboStep = 0;
            comboTime = 0f;
            attackCooldownCounter = 0f;
        }
    }

    /** Returns true if the player can start a new attack or continue a combo */
    public boolean canStartNewAttackOrContinueCombo() {
        return (comboStep == 0 && canStartNewAttack()) // Can start a new attack
            || (comboStep > 0 && comboTime <= MAX_COMBO_TIME && comboAttackCooldownCounter >= 0.4f); // Can continue a combo
    }

    /** Initiates an attack */
    private void initiateAttack(InputController input) {
        player.setAttacking(true);
        player.setCanMove(false); // Movement code in player sets velocity to 0 and overrides this so must not be able to move

        // movement component
        attackDirection.set(input.getHorizontal(), input.getVertical()).nor();
        player.getBody().applyLinearImpulse(attackDirection, player.getBody().getWorldCenter(), true);

        comboAttackCooldownCounter = 0f;
        super.initiateAttack();
    }

    /** @return the attack power of the player */
    public static float getAttackPower() {
    	return attackPower;
    }

    /** Sets the attack power of the player */
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
