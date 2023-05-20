package infinityx.lunarhaze.controllers;

import box2dLight.PointLight;
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
    public static final float MOONLIGHT_COLLECT_TIME = 1.5f;

    /**
     * Stealth value if the player is walking
     */
    public static final float WALK_STEALTH = 0.35f;

    /**
     * Stealth value if the player is on the moonlight
     */
    public static final float MOON_STEALTH = 0.95f;

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
    public float timeOnMoonlight;

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
     * Sound for player attacked
     */
    private Sound attacked_sound;
    /**
     * Sound for player heaby attacking
     */
    private Sound attack_heavy_sound;

    /**
     * Sound for player dashing
     */
    private Sound dash_sound;

    /**
     * Sound for player walking on grass
     */
    private Sound walk_sound;

    /**
     * Handles attacking logic
     */
    private final PlayerAttackHandler attackHandler;

    /**
     * Indicate whether player has done with allocating moonlight
     */
    private Boolean allocateReady;

    /**
     * Whether the walk_grass sound is playing
     */
    private boolean isWalkGrassPlaying;

    /**
     * Whether the dash_sound sound is playing
     */
    private boolean isDashPlaying;

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

    public boolean isWindingUp() {
        return attackHandler.isWindingUpHeavyAttack();
    }

    public boolean isHeavyAttacking() {
        return player.isAttacking() && attackHandler.isHeavyAttacking();
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

    public Sound getAttackedSound() {
        return attacked_sound;
    }


    public Sound getHeavyAttackSound() {
        return attack_heavy_sound;
    }

    public GameSetting getSetting() {
        return setting;
    }


    /**
     * Initializes
     *
     * @param levelContainer
     */
    public PlayerController(LevelContainer levelContainer, GameSetting setting) {
        this.player = levelContainer.getPlayer();
        this.board = levelContainer.getBoard();
        collect_sound = levelContainer.getDirectory().getEntry("collect", Sound.class);
        attack_sound = levelContainer.getDirectory().getEntry("whip", Sound.class);
        attacked_sound = levelContainer.getDirectory().getEntry("werewolf-get-hit", Sound.class);
        attack_heavy_sound = levelContainer.getDirectory().getEntry("whip2", Sound.class);
        dash_sound = levelContainer.getDirectory().getEntry("dash", Sound.class);
        walk_sound = levelContainer.getDirectory().getEntry("walking-on-soil", Sound.class);
        stateMachine = new DefaultStateMachine<>(this, PlayerState.IDLE);
        attackHandler = new PlayerAttackHandler(player, player.getAttackHitbox(), dash_sound);
        allocateReady = false;
        isWalkGrassPlaying = false;
        isDashPlaying = false;
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
        player.update(delta);

        if (setting.isSoundEnabled()) {
            if (!player.isAttacked() && inputController.justDash() && attackHandler.isDashing) {
                dash_sound.play(setting.getSoundVolume());
            } else if (getStateMachine().isInState(PlayerState.WALK) && !isWalkGrassPlaying) {
                long soundId = walk_sound.loop();
                walk_sound.setLooping(soundId, true);
                walk_sound.play(setting.getSoundVolume());
                isWalkGrassPlaying = true;
            }
            if (!getStateMachine().isInState(PlayerState.WALK)) {
                walk_sound.stop();
                isWalkGrassPlaying = false;
            }
        }
    }

    /**
     * Process stealth value change.
     * <p>
     *
     * @param delta Number of seconds since last animation frame
     */
    public void resolveStealth(float delta) {
        float proportion = player.getStealth();
        if (player.getTargetStealth() > proportion) {
            if (player.getTargetStealth() - proportion >= CHANGE_STEALTH_RATE / 1.0f * delta) {
                proportion = proportion + CHANGE_STEALTH_RATE / 1.0f * delta;
            } else {
                proportion = player.getTargetStealth();
            }
        } else if (player.getTargetStealth() < proportion) {
            if (proportion - player.getTargetStealth() >= CHANGE_STEALTH_RATE / 1.0f * delta) {
                proportion = proportion - CHANGE_STEALTH_RATE / 1.0f * delta;
            } else {
                proportion = player.getTargetStealth();
            }
        }
        player.setStealth(proportion);
    }

    /**
     * Player collect one moonlight, including upgrading the stats and status.
     * <p>
     */
    public void collectMoonlight() {
        int bx = board.worldToBoardX(player.getPosition().x);
        int by = board.worldToBoardY(player.getPosition().y);
        board.setCollected(bx, by);
        player.addMoonlightCollected();

        if (setting.isSoundEnabled()) {
            collect_sound.play(setting.getSoundVolume());
        }
    }

    /**
     * Process the player's interaction with moonlight tile.
     * <p>
     *
     * @param lightingController lighting controller to update moonlight particles
     */
    public void resolveMoonlight(LightingController lightingController) {
        if (player.isCollecting || player.isAttacked()) return;
        boolean isOn = false;

        // Check if player is within range of a lamp
        for (PointLight light : lightingController.getLampLights()) {
            if (!light.isActive()) continue;
            if (player.getPosition().dst(light.getPosition()) <= (light.getDistance() / 2.5f)) {
                isOn = true;
            }
        }

        int px = board.worldToBoardX(player.getPosition().x);
        int py = board.worldToBoardY(player.getPosition().y);
        if (board.isLit(px, py)) {
            isOn = true;
        }
        if (board.isCollectable(px, py) && InputController.getInstance().didCollect()) {
            stateMachine.changeState(PlayerState.COLLECT);
        }

        setOnMoonlight(isOn);
    }

    /**
     * Set whether the player is on moonlight or not
     */
    private void setOnMoonlight(boolean isOn) {
        if (player.isOnMoonlight == isOn) return;
        player.setTargetStealth(player.getTargetStealth() + (isOn ? 1 : -1) * MOON_STEALTH);
        player.isOnMoonlight = isOn;
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
     * Player allocates one moonlight to reduce dash cooldown
     */
    public void allocateDashCooldown() {
        player.reduceMoonlightCollected();
        attackHandler.DASH_COOLDOWN_BATTLE = attackHandler.DASH_COOLDOWN_BATTLE - attackHandler.DASH_REDUCE_AMOUNT;
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
    public void update(float delta, Phase currPhase, LightingController lightingController) {
        attackHandler.update(delta, currPhase);
        resolvePlayer(delta);
        if (currPhase == Phase.STEALTH) {
            resolveStealth(delta);
        }
        if (currPhase == GameplayController.Phase.STEALTH) {
            resolveMoonlight(lightingController);
        }

        // Process the FSM
        stateMachine.update();
    }

    public PlayerAttackHandler getAttackHandler() {
        return attackHandler;
    }
}