package infinityx.lunarhaze;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.GameplayController.Phase;
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

    private StateMachine<PlayerController, PlayerState> stateMachine;

    private InputController inputController;

    private LightingController lightingController;

    private GameplayController.Phase phase;

    public float getTimeOnMoonlightPercentage(){
        return timeOnMoonlight/MOONLIGHT_COLLECT_TIME;
    }

    public InputController getInputController() {
        return inputController;
    }

    public PlayerAttackHandler getAttackHandler() {
        return attackHandler;
    }

    public GameplayController.Phase getPhase() {
        return phase;
    }

    public boolean isOnMoonlight(){
        return player.isOnMoonlight();
    }

    public StateMachine<PlayerController, PlayerState> getStateMachine() {
        return stateMachine;
    }

    public LightingController getLightingController(){
        return lightingController;
    }

    public boolean isAttacking(){
        return player.isAttacking();
    }

    public Sound getAttackSound () {
        return attack_sound;
    }

    public Sound getCollectSound () {
        return collect_sound;
    }
    /**
     * Initializer of a PlayerController
     */
    public PlayerController(Werewolf player, Board board, LevelContainer levelContainer, LightingController lighting) {
        this.player = player;
        this.board = board;
        this.levelContainer = levelContainer;
        collectingMoonlight = false;
        attackHandler = new PlayerAttackHandler(player);
        collect_sound = levelContainer.getDirectory().getEntry("collect", Sound.class);
        attack_sound = levelContainer.getDirectory().getEntry("whip", Sound.class);
        stateMachine = new DefaultStateMachine<>(this, PlayerState.WALK);
        lightingController = lighting;
    }

    /**
     * Process the player's movement according to input controller.
     * <p>
     *
     * @param delta Number of seconds since last animation frame
     */
    public void resolvePlayer(float delta) {
        player.setMovementH(inputController.getHorizontal());
        player.setMovementV(inputController.getVertical());
        player.setRunning(inputController.didRun());
        player.update(delta);
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
     */
    public void resolveStealthBar() {
        if (Math.abs(inputController.getHorizontal()) == inputController.getWalkSpeed() ||
                Math.abs(inputController.getVertical()) == inputController.getWalkSpeed()) {
            player.setStealth(WALK_STEALTH);
        } else if (Math.abs(inputController.getHorizontal()) == inputController.getRunSpeed() ||
                Math.abs(inputController.getVertical()) == inputController.getRunSpeed()) {
            player.setStealth(RUN_STEALTH);
        } else if (inputController.getHorizontal() == 0 || inputController.getVertical() == 0) {
            player.setStealth(STILL_STEALTH);
        }

        if (player.isOnMoonlight()) {
            player.setStealth(MOON_STEALTH);
        }
    }

    public boolean isCollectingMoonlight(){
        return collectingMoonlight;
    }

    public void update(InputController input, float delta, Phase currPhase) {
        inputController = inputController == null ? input : inputController;
        phase = currPhase;
        stateMachine.update();
    }
}