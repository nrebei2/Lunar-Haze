package infinityx.lunarhaze;

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

    /**
     * Attacks the player has already made
     */
    private float attackCounter;

    /**
     * Maximum attacks player can make before cooldown
     */
    private float attackLength;

    /**
     *
     */
    private float attackCooldownCounter;

    /**
     *
     */
    private float attackCooldown;

    private Vector2 attackDirection;

    /**
     * Initializer of a PlayerController
     */
    public PlayerController(Werewolf player, Board board, LevelContainer levelContainer) {
        this.player = player;
        this.board = board;
        timeOnMoonlight = 0;
        this.levelContainer = levelContainer;
        attackCounter = 0f;
        attackLength = 0.6f;
        attackCooldown = 4f;
        attackCooldownCounter = 0f;
        attackDirection = new Vector2();
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
            if (board.isCollectable(px, py) && (timeOnMoonlight > MOONLIGHT_COLLECT_TIME)) {
                collectMoonlight();
                timeOnMoonlight = 0;
                board.setCollected(px, py);
                lightingController.removeDust(px, py);
            }
        } else {
            timeOnMoonlight = 0;
            player.setOnMoonlight(false);
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

    public void attack(float delta, InputController input, Phase phase) {
        if (phase == GameplayController.Phase.BATTLE) {
            if (player.isAttacking()) {
                player.setCanMove(false);
                attackCounter += delta;
                if (attackCounter >= attackLength) {
                    player.setAttacking(false);
                    player.setCanMove(true);
                    attackCounter = 0f;
                }
            } else {
                attackCooldownCounter += delta;
                if (attackCooldownCounter >= attackCooldown) {
                    attackCooldownCounter = 0f;
                }
            }
            if (!player.isAttacking() && attackCooldownCounter >= 0f && input.didAttack()) {
                player.setAttacking(true);
                attackCooldownCounter = attackCooldown;
                attackDirection.set(input.getHorizontal(), input.getVertical()).nor().scl(125);
                player.setLinearVelocity(attackDirection);
            }
        }
    }

    public void update(InputController input, float delta, Phase currPhase, LightingController lightingController) {
        resolvePlayer(input, delta);
        resolveMoonlight(delta, lightingController);
        resolveStealthBar(input);
        attack(delta, input, currPhase);
    }
}
