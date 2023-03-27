package infinityx.lunarhaze;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.GameplayController.Phase;
import infinityx.lunarhaze.entity.Werewolf;

public class PlayerController {

    /**
     * The time required to collect moonlight
     */
    private static final float MOONLIGHT_COLLECT_TIME = 1.5f;

    /**
     * Stealth value if the player is standing still
     */
    private static final float STILL_STEALTH = 0.0f;

    /**
     * Stealth value if the player is walking
     */
    private static final float WALK_STEALTH = 0.3f;

    /**
     * Stealth value if the player is running
     */
    private static final float RUN_STEALTH = 0.6f;

    /**
     * Stealth value if the player is on the moonlight
     */
    private static final float MOON_STEALTH = 1.0f;

    /**
     * The player being controlled by this AIController
     */
    private final Werewolf player;

    /**
     * The game board; used for pathfinding
     */
    private final Board board;

    /**
     * Time on current lit tile
     */
    private float timeOnMoonlight;

    /**
     * LevelContainer that contains moonlight information
     */
    private LevelContainer levelContainer;

    /**If the player is collecting moonlight then true, false otherwise */
    private boolean collectingMoonlight;

    private AttackHandler attackHandler;

    public float getTimeOnMoonlightPercentage(){
        return timeOnMoonlight/MOONLIGHT_COLLECT_TIME;
    }

    /**
     * Initializer of a PlayerController
     */
    public PlayerController(Werewolf player, Board board, LevelContainer levelContainer) {
        this.player = player;
        this.board = board;
        timeOnMoonlight = 0;
        this.levelContainer = levelContainer;
        collectingMoonlight = false;
        attackHandler = new AttackHandler(player);
    }

    /**
     * Process the player's movement according to input controller.
     * <p>
     *
     * @param input InputController that controls the player
     * @param delta Number of seconds since last animation frame
     */
    public void resolvePlayer(InputController input, float delta) {
        player.setMovementH(input.getHorizontal());
        player.setMovementV(input.getVertical());
        player.update(delta);
    }

    /**
     * Player collect one moonlight, including upgrading the stats and status.
     * <p>
     */
    public void collectMoonlight() {
        collectingMoonlight = false;
        player.addMoonlightCollected();
        player.setLight(player.getLight() + (Werewolf.MAX_LIGHT / levelContainer.getTotalMoonlight()));
    }

    /**
     * Process the player's interaction with moonlight tile.
     * <p>
     *
     * @param delta              Number of seconds since last animation frame
     * @param lightingController
     */
    public void resolveMoonlight(float delta, LightingController lightingController) {
        int px = board.worldToBoardX(player.getPosition().x);
        int py = board.worldToBoardY(player.getPosition().y);

        if (board.isLit(px, py)) {
            timeOnMoonlight += delta; // Increase variable by time
            player.setOnMoonlight(true);
            collectingMoonlight = true;
            if (board.isCollectable(px, py) && (timeOnMoonlight > MOONLIGHT_COLLECT_TIME)) {
                collectMoonlight();
                collectingMoonlight = false;
                timeOnMoonlight = 0;
                board.setCollected(px, py);
                lightingController.removeDust(px, py);
            }
            if(board.isCollectable(px, py)==false){
                collectingMoonlight = false;
            }
        } else {
            timeOnMoonlight = 0;
            player.setOnMoonlight(false);
            collectingMoonlight = false;
        }
    }

    /**
     * Process the player's stealth value. This depends on the walk/run mode.
     * <p>
     *
     * @param input InputController that controls the player
     */
    public void resolveStealthBar(InputController input) {
        if (Math.abs(input.getHorizontal()) == input.getWalkSpeed() ||
                Math.abs(input.getVertical()) == input.getWalkSpeed()) {
            player.setStealth(WALK_STEALTH);
        } else if (Math.abs(input.getHorizontal()) == input.getRunSpeed() ||
                Math.abs(input.getVertical()) == input.getRunSpeed()) {
            player.setStealth(RUN_STEALTH);
        } else if (input.getHorizontal() == 0 || input.getVertical() == 0) {
            player.setStealth(STILL_STEALTH);
        }

        if (player.isOnMoonlight()) {
            player.setStealth(MOON_STEALTH);
        }
    }

    public boolean isCollectingMoonlight(){
        return collectingMoonlight;
    }


    public void update(InputController input, float delta, Phase currPhase, LightingController lightingController) {
        resolvePlayer(input, delta);
        resolveMoonlight(delta, lightingController);
        resolveStealthBar(input);
        attackHandler.update(delta, input, currPhase);
    }
}

/** Handles player attacks */
class AttackHandler {
    //TODO: Remove magic numbers and add to LevelParser and jsons
    /** Cooldown between combos */
    private float attackCooldown;

    /** Counter for delay between combo attacks */
    private float comboAttackCooldownCounter;

    /** Attack direction as vector */
    private Vector2 attackDirection;

    /** Current combo step */
    private int comboStep;

    /** Time since combo started */
    private float comboTime;

    /** Max allowed time before combo timeout */
    private final static float MAX_COMBO_TIME = 1f;

    /**
     * Counter for attacking (used to determine when to set attacking to false)
     */
    private float attackCounter;

    /**
     * Maximum attacks player can make before cooldown
     */
    private float attackLength;

    /** Counter for attack cooldowns*/
    private float attackCooldownCounter;

    /** Reference to the player model */
    private Werewolf player;

    /** Constructor that gets a reference to the player model */
    AttackHandler(Werewolf p) {
        player = p;
        attackCooldown = 3f;
        comboAttackCooldownCounter = 0f;
        attackDirection = new Vector2();
        comboStep = 0;
        comboTime = 0f;
        attackCounter = 0f;
        attackLength = 0.5f;
        attackCooldownCounter = 3f;
    }

    //TODO: Make the attack cooldowns and attack lengths decrease with moonlight collected

    /** Called up above in the other update method, handles all attacking related logic */
    public void update(float delta, InputController input, Phase phase) {
        if (phase == GameplayController.Phase.BATTLE) {
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
        System.out.println("Attacking: combo " + (comboStep + 1));

        attackCounter += delta;

        if (attackCounter >= attackLength) {
            endAttack();
        }
    }

    /** Adjusts hitbox based on user input */
    private void updateHitboxPosition(InputController input) {
        player.attackHitbox.setTransform(player.getPosition().x + (input.getHorizontal() / 4.0f), player.getPosition().y + (input.getVertical() / 4.0f) + player.getTexture().getSize()/3.5f, 0f);
    }

    /** Called when an attack ends */
    private void endAttack() {
        System.out.println("Attack ended");
        player.setAttacking(false);
        attackCounter = 0f;
        comboStep++;
        comboTime = 0f;

        if (comboStep >= 3) {
            comboStep = 0;
            attackCooldownCounter = 0f;
        }
    }

    /** Handles combo timeouts */
    private void handleComboTimeout(float delta) {
        comboTime += delta;
        comboAttackCooldownCounter += delta;

        if (comboTime >= 1) {
            System.out.println("Combo timeout");
            comboStep = 0;
            comboTime = 0f;
            attackCooldownCounter = 0f;
        }
    }

    /** Returns true if the player can start a new attack or continue a combo */
    public boolean canStartNewAttackOrContinueCombo() {
        return (comboStep == 0 && attackCooldownCounter >= attackCooldown)
            || (comboStep > 0 && comboTime <= 1 && comboAttackCooldownCounter >= 0.4f);
    }

    /** Initiates an attack */
    private void initiateAttack(InputController input) {
        player.setAttacking(true);
        player.setCanMove(false);
        attackDirection.set(input.getHorizontal(), input.getVertical()).nor();
        player.getBody().applyLinearImpulse(attackDirection, player.getBody().getWorldCenter(), true);
        attackCooldownCounter = 0f;
        comboAttackCooldownCounter = 0f;
    }

}
