package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.audio.Sound;
import infinityx.lunarhaze.combat.PlayerAttackHandler;
import infinityx.lunarhaze.controllers.GameplayController.Phase;
import infinityx.lunarhaze.models.Board;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Werewolf;
import infinityx.lunarhaze.screens.GameSetting;


/**
 * Controller class, handles logic for the player
 */
public class PlayerController {

    /**
     * The time required to collect moonlight
     */
    private static final float MOONLIGHT_COLLECT_TIME = 1.5f;

    /**
     * Stealth value if the player is standing still
     */
    public static final float STILL_STEALTH = 0.0f;

    /**
     * Stealth value if the player is walking
     */
    public static final float WALK_STEALTH = 0.3f;

    /**
     * Stealth value if the player is running
     */
    public static final float RUN_STEALTH = 0.6f;

    /**
     * Stealth value if the player is on the moonlight
     */
    public static final float MOON_STEALTH = 1.0f;

    /**
     * Hp increase for each moonlight allocated during phase ALLOCATE
     */
    public static final int ADD_HP_AMOUNT = 1;

    /**
     * Attack power proportion increase for each moonlight allocated during phase ALLOCATE
     */
    public static final float ADD_ATTACK_AMOUNT = 0.2f;

    /**
     * Attack range distance proportion increase for each moonlight allocated during phase ALLOCATE
     */
    public static final float ADD_RANGE_AMOUNT = 0.2f;

    /**
     * Change of stealth rate, percentage per second
     */
    private final static float CHANGE_STEALTH_RATE = 0.75f;

    /**
     * The player being controlled by this AIController
     */
    public Werewolf player;

    /**
     * Reference game board
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
     * If the player is collecting moonlight then true, false otherwise
     */
    public boolean collectingMoonlight;

    /**
     * Number of times power is allocated
     */
    private int numPowerPress = 0;

    /**
     * Number of times range is allocated
     */
    private int numRangePress = 0;

    /**
     * Sound for successfully collect moonlight
     */
    private Sound collect_sound;

    /**
     * Sound for player attacking
     */
    private Sound attack_sound;

    /**
     * Handles attacking logic
     */
    private final PlayerAttackHandler attackHandler;

    /**
     * Indicate whether player has done with allocating moonlight
     */
    private Boolean allocateReady;

    /**
     * Stealth value of the state that player is going to enter
     */
    private float target;

    private GameSetting setting;

    /**
     * Player state machine, mostly for animation purposes and setting stealth.
     */
    private StateMachine<PlayerController, PlayerState> stateMachine;

    public float getTimeOnMoonlightPercentage() {
        return timeOnMoonlight / MOONLIGHT_COLLECT_TIME;
    }

    public Werewolf getPlayer() {
        return player;
    }

    public StateMachine<PlayerController, PlayerState> getStateMachine() {
        return stateMachine;
    }

    public boolean isAttacking() {
        return player.isAttacking();
    }

    public boolean isAttacked() {
        return player.isAttacked();
    }

    public int getNumPowerPress() {
        return numPowerPress;
    }

    public int getNumRangePress() {
        return numRangePress;
    }

    public void setNumPowerPress(int n) {
        numPowerPress = n;
    }

    public void setNumRangePress(int n) {
        numRangePress = n;
    }

    public Sound getAttackSound() {
        return attack_sound;
    }

    public float getTargetStealth(){
        return target;
    }

    public void setTargetStealth(float t){
        target = t;
    }


    /**
     * Initializes
     *
     * @param levelContainer
     */
    public PlayerController(LevelContainer levelContainer, GameSetting setting) {
        this.player = levelContainer.getPlayer();
        this.board = levelContainer.getBoard();
        this.levelContainer = levelContainer;
        collectingMoonlight = false;
        attackHandler = new PlayerAttackHandler(player);
        collect_sound = levelContainer.getDirectory().getEntry("collect", Sound.class);
        attack_sound = levelContainer.getDirectory().getEntry("whip", Sound.class);
        stateMachine = new DefaultStateMachine<>(this, PlayerState.IDLE);
        allocateReady = false;
        target = STILL_STEALTH;
        this.setting = setting;
    }

    /**
     * Process the player's movement.
     * <p>
     *
     * @param delta Number of seconds since last animation frame
     */
    public void resolvePlayer(float delta) {
        InputController inputController = InputController.getInstance();

        // Button may be pressed, but player may not be moving!
        /*player.setRunning(
                inputController.didRun() && (
                        InputController.getInstance().getHorizontal() != 0 || InputController.getInstance().getVertical() != 0
                )
        );*/
        player.update(delta);
    }

    /**
     * Process stealth value change.
     * <p>
     *
     * @param delta Number of seconds since last animation frame
     */
    public void resolveStealth(float delta) {
        if (player.getLinearVelocity().isZero() && player.isOnMoonlight == false){
            target = PlayerController.STILL_STEALTH;
        }
        float proportion = player.getStealth();
        if (target > proportion) {
            if (target - proportion >= CHANGE_STEALTH_RATE / 1.0f * delta) {
                proportion = proportion + CHANGE_STEALTH_RATE / 1.0f * delta;
            } else {
                proportion = target;
            }
        } else if (target < proportion){
            if (proportion - target >= CHANGE_STEALTH_RATE / 1.0f * delta) {
                proportion = proportion - CHANGE_STEALTH_RATE / 1.0f * delta;
            } else {
                proportion = target;
            }
        }
        player.setStealth(proportion);
    }

    /**
     * Player collect one moonlight, including upgrading the stats and status.
     * <p>
     */
    public void collectMoonlight() {
        collectingMoonlight = false;
        player.addMoonlightCollected();
        if (setting.isSoundEnabled()) {
            collect_sound.play(0.8f);
        }
    }

    /**
     * Process the player's interaction with moonlight tile.
     * <p>
     *
     * @param delta              Number of seconds since last animation frame
     * @param lightingController lighting controller to update moonlight particles
     */
    public void resolveMoonlight(float delta, LightingController lightingController) {
        int px = board.worldToBoardX(player.getPosition().x);
        int py = board.worldToBoardY(player.getPosition().y);

        if (board.isLit(px, py)) {
            timeOnMoonlight += delta; // Increase variable by time
            collectingMoonlight = true;
            //Fixme move collecting field moonlight to enemy
            player.isOnMoonlight = true;
            if (board.isCollectable(px, py) && (timeOnMoonlight > MOONLIGHT_COLLECT_TIME)) {
                collectMoonlight();
                collectingMoonlight = false;
                timeOnMoonlight = 0;
                board.setCollected(px, py);
                lightingController.removeDust(px, py);
            }
            if (!board.isCollectable(px, py)) {
                collectingMoonlight = false;
            }
        } else {
            timeOnMoonlight = 0;
            collectingMoonlight = false;
            player.isOnMoonlight = false;
        }
    }

    /**
     * Whether the player is currently collecting moonlight
     * (Need to stand on a moonlight tile and in IDLE state)
     */
    public boolean isCollectingMoonlight() {
        return collectingMoonlight;
    }

    /**
     * Player allocates one moonlight to increase hp by ADD_HP_AMOUNT
     */
    public void allocateHp() {
        player.reduceMoonlightCollected();
        player.hp += ADD_HP_AMOUNT;
        player.maxHp += ADD_HP_AMOUNT;
    }

    /**
     * Player allocates one moonlight to increase attack power by ADD_ATTACK_AMOUNT
     */
    public void allocateAttackPow() {
        player.reduceMoonlightCollected();
        player.attackDamage = player.attackDamage + ADD_ATTACK_AMOUNT;
    }

    /**
     * Player allocates one moonlight to multiply attack range by (1 + ADD_RANGE_AMOUNT)
     */
    public void allocateAttackRange() {
        player.reduceMoonlightCollected();
        player.setAttackRange(player.getAttackRange() + ADD_RANGE_AMOUNT);
    }

    /**
     * Returns if player finished allocating the attibutes
     */
    public boolean getAllocateReady() {
        return allocateReady;
    }

    /**
     * Sets whether player finished allocating the attibutes
     */
    public void setAllocateReady(boolean b) {
        allocateReady = b;
    }

    /**
     * Process the update logic for the player
     *
     * @param currPhase          Current phase of the game
     * @param lightingController lighting controller to update moonlight particles
     */
    public void update(Phase currPhase, LightingController lightingController) {
        attackHandler.update(Gdx.graphics.getDeltaTime(), currPhase);
        resolvePlayer(Gdx.graphics.getDeltaTime());
        if (currPhase == Phase.STEALTH) {
            resolveStealth(Gdx.graphics.getDeltaTime());
        }
        if (currPhase == GameplayController.Phase.STEALTH) {
            resolveMoonlight(Gdx.graphics.getDeltaTime(), lightingController);
        }

        // Process the FSM
        stateMachine.update();
    }

    public PlayerAttackHandler getAttackHandler() {
        return attackHandler;
    }
}