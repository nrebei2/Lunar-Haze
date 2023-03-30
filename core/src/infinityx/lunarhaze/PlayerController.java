package infinityx.lunarhaze;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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

    /**
     * Sound for successfully collect moonlight
     */
    private Sound collect_sound;

    /**
     * Sound for player attacking
     */
    private Sound attack_sound;

    private PlayerAttackHandler attackHandler;

    public enum PlayerState {
        /**
         * The player is standing still
         */
        STILL,
        /**
         * The player is walking
         */
        WALK,
        /**
         * The player is running
         */
        RUN,
        /**
         * The player is collecting moonlight
         */
        COLLECT,
        /**
         * The player is attacking villagers (only happen in battle phase)
         */
        ATTACK,
    }

    /**
     * State of the player. One of PlayerState.
     */
    private PlayerState state;

    /**
     * Return time elapsed on moonlight tile (given player is standing on moonlight)
     */
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
        collect_sound = levelContainer.getDirectory().getEntry("collect", Sound.class);
        attack_sound = levelContainer.getDirectory().getEntry("whip", Sound.class);
        state = PlayerState.STILL;
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
        player.setRunning(input.didRun());
        player.update(delta);

        int px = board.worldToBoardX(player.getPosition().x);
        int py = board.worldToBoardY(player.getPosition().y);

        if (board.isLit(px, py)) {
            player.setOnMoonlight(true);
        }
    }

    /**
     * Change the state of the player.
     * <p>
     * A Finite State Machine (FSM) is just a collection of rules that,
     * given a current state, and given certain observations about the
     * environment, chooses a new state. For example, if we are currently
     * in the WALK state, we may want to switch to the ATTACK state if the
     * player presses SHIFT.
     */
    private void changeStateIfApplicable(InputController input, float delta, Phase currPhase) {
        switch (state) {
            case STILL:
                if (Math.abs(input.getHorizontal()) != 0 || Math.abs(input.getVertical()) != 0){
                    state = PlayerState.WALK;
                } else if ((Math.abs(input.getHorizontal()) != 0 || Math.abs(input.getVertical()) != 0)
                && input.didRun()){
                    state = PlayerState.RUN;
                } else if (player.isOnMoonlight() && currPhase == Phase.STEALTH){
                    state = PlayerState.COLLECT;
                } else if (input.didAttack() && currPhase == Phase.BATTLE){
                    state = PlayerState.ATTACK;
                    attack_sound.play();
                }
                break;
            case WALK:
                if (Math.abs(input.getHorizontal()) == 0 && Math.abs(input.getVertical()) == 0){
                    state = PlayerState.STILL;
                } else if ((Math.abs(input.getHorizontal()) != 0 || Math.abs(input.getVertical()) != 0)
                        && input.didRun()){
                    state = PlayerState.RUN;
                } else if (input.didAttack() && currPhase == Phase.BATTLE){
                    state = PlayerState.ATTACK;
                    attack_sound.play();
                }
                break;
            case RUN:
                if (Math.abs(input.getHorizontal()) == 0 && Math.abs(input.getVertical()) == 0){
                    state = PlayerState.STILL;
                } else if ((Math.abs(input.getHorizontal()) != 0 || Math.abs(input.getVertical()) != 0)
                        && !input.didRun()){
                    state = PlayerState.WALK;
                }
                break;
            case COLLECT:
                if (!player.isOnMoonlight() && !input.didRun()){
                    state = PlayerState.WALK;
                    timeOnMoonlight = 0;
                    player.setOnMoonlight(false);
                    collectingMoonlight = false;
                } else if (!player.isOnMoonlight() && input.didRun()){
                    state = PlayerState.RUN;
                    timeOnMoonlight = 0;
                    player.setOnMoonlight(false);
                    collectingMoonlight = false;
                }
                break;
            case ATTACK:
                if (!player.isAttacking()){
                    state = PlayerState.STILL;
                }
                break;
            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                break;
        }
    }

    /**
     * Player collect one moonlight, including upgrading the stats and status.
     * <p>
     */
    public void collectMoonlight() {
        collectingMoonlight = false;
        player.addMoonlightCollected();
        collect_sound.play(0.8f);
        player.setLight(player.getLight() + (Werewolf.MAX_LIGHT / levelContainer.getTotalMoonlight()));
        PlayerAttackHandler.setAttackPower(player.getAttackPower());
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

        timeOnMoonlight += delta; // Increase variable by time
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
    }

    /**
     * Process the player's stealth value. This depends on the walk/run mode.
     * <p>
     *
     * @param input InputController that controls the player
     */
    public void resolveStealthBar(InputController input) {
        switch (state) {
            case STILL:
                player.setStealth(STILL_STEALTH);
                break;
            case WALK:
                player.setStealth(WALK_STEALTH);
                break;
            case RUN:
                player.setStealth(RUN_STEALTH);
                break;
            case COLLECT:
            case ATTACK:
                player.setStealth(MOON_STEALTH);
                break;
            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                break;
        }
    }

    public boolean isCollectingMoonlight(){
        return collectingMoonlight;
    }

    public void update(InputController input, float delta, Phase currPhase, LightingController lightingController) {
        resolvePlayer(input, delta);
        changeStateIfApplicable(input, delta, currPhase);
        if (state == PlayerState.COLLECT) {
            resolveMoonlight(delta, lightingController);
        }
        if (currPhase == Phase.STEALTH) {
            resolveStealthBar(input);
        } else {
            player.setStealth(MOON_STEALTH);
        }
        attackHandler.update(delta, input, currPhase);
    }
}
