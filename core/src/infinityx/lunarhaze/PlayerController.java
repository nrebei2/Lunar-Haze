package infinityx.lunarhaze;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.GameplayController.Phase;
import infinityx.lunarhaze.combat.AttackHandler;
import infinityx.lunarhaze.combat.PlayerAttackHandler;
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

    private PlayerAttackHandler attackHandler;

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
        attackHandler = new PlayerAttackHandler(player);
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
            if(!board.isCollectable(px, py)){
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
