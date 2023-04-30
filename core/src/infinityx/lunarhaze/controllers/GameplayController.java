package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import infinityx.lunarhaze.ai.TacticalManager;
import infinityx.lunarhaze.models.Board;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Enemy;
import infinityx.lunarhaze.models.entity.Werewolf;
import infinityx.lunarhaze.screens.GameSetting;

/**
 * Controller to handle gameplay interactions.
 */
public class GameplayController {
    /**
     * We are separating our main game into four phases:
     * - Stealth: Collect moonlight
     * - TRANSITION: Moon rise cutscene
     * - Allocate: Use collected moonlight to upgrade stats
     * - Battle: Hack-n-slash
     */
    public enum Phase {
        STEALTH,
        BATTLE,
        TRANSITION,
        ALLOCATE,
    }

    /**
     * The current phase of the game
     */
    public Phase phase;

    /**
     * Track the current state of the game for the update loop.
     * - Play: The game is in play
     * - Over: The werewolf has been killed by an enemy!!
     * - Paused: The game is on the pause screen
     * - Win: The player has killed all the enemies. Should only be set when the current phase is BATTLE
     */
    public enum GameState {
        PLAY,
        OVER,
        PAUSED,
        WIN
    }

    /**
     * The current game state
     */
    private GameState gameState;

    /**
     * Reference to level container, acts as a nice interface for all level models
     */
    private LevelContainer container;

    /**
     * Reference to player from container
     */
    private Werewolf player;

    /**
     * Reference to active enemies from container
     */
    private Array<Enemy> enemies;

    /**
     * Reference to board from container
     */
    public Board board;

    /**
     * Reference of enemy model-controller map from container
     */
    private ObjectMap<Enemy, EnemyController> controls;

    /**
     * Owns the lighting controller
     */
    private LightingController lightingController;

    /**
     * Owns the player controller
     */
    private PlayerController playerController;

    /**
     * Owns the tactical manager
     */
    private TacticalManager tacticalManager;

    /**
     * Owns the collision controller, handles collisions.
     * Never accessed due to all methods in the controller being callbacks.
     */
    private CollisionController collisionController;

    /**
     * Timer for the ambient light transition
     */
    private float ambientLightTransitionTimer;

    /**
     * Timer for the phase transition, goes down
     */
    private float phaseTimer;

    /**
     * Number of ticks (frames) since battle began
     */
    private int battleTicks;

    /**
     * Sound of winning a level
     */
    private Sound win_sound;

    private GameSetting setting;


    /**
     * Creates a new GameplayController with no active elements.
     */
    public GameplayController(GameSetting setting) {
        player = null;
        enemies = null;
        board = null;
        this.setting = setting;
    }

    public GameState getState() {
        return gameState;
    }

    public void setState(GameState s) {
        gameState = s;
    }

    public float getTimeOnMoonlightPercentage() {
        return playerController.getTimeOnMoonlightPercentage();
    }

    public boolean getCollectingMoonlight() {
        return playerController.isCollectingMoonlight();
    }

    /**
     * @return the current phase of the game
     */
    public Phase getPhase() {
        return phase;
    }

    /**
     * Set the current phase of the game
     */
    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    /**
     * Starts a new game, (re)initializing relevant controllers and attributes.
     * <p>
     *
     * @param levelContainer container holding model objects in level
     */
    public void start(LevelContainer levelContainer) {
        this.gameState = GameState.PLAY;
        this.phase = Phase.STEALTH;
        this.container = levelContainer;
        this.collisionController = new CollisionController(levelContainer.getWorld());

        lightingController = new LightingController(levelContainer);

        board = levelContainer.getBoard();
        player = levelContainer.getPlayer();
        this.playerController = new PlayerController(levelContainer, setting);

        phaseTimer = levelContainer.getPhaseLength();
        ambientLightTransitionTimer = 0;

        enemies = levelContainer.getEnemies();
        controls = levelContainer.getEnemyControllers();
        battleTicks = 0;

        win_sound = levelContainer.getDirectory().getEntry("level-passed", Sound.class);
//        allocateScreen = new AllocateScreen(canvas, playerController);

        tacticalManager = new TacticalManager(container);
    }

    /**
     * Resolve the actions of all game objects and controllers.
     * This includes the lighting, player, and enemy controllers.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void resolveActions(float delta) {
        // Update the phase timer
        lightingController.updateDust(delta);

        // FSM for state and phase
        if (gameState == GameState.PLAY) {
            // Process the player only when the game is in play
            playerController.update(phase, lightingController);
            switch (phase) {
                case STEALTH:
                    phaseTimer -= delta;
                    if (container.getBoard().getRemainingMoonlight() == 0 || phaseTimer <= 0)
                        phase = Phase.TRANSITION;
                    break;
                case BATTLE:
                    battleTicks += 1;
                    if (enemies.size == 0) {
                        gameState = GameState.WIN;
                        if (setting.isSoundEnabled()) {
                            win_sound.play();
                        }
                    }
                    break;
                case TRANSITION:
                    switchPhase(delta);
                    break;
                case ALLOCATE:
                    // TODO: Somehow pause the game before drawing allocating screen
                    if (playerController.getAllocateReady()) {
                        phase = Phase.BATTLE;
                    }
                    break;
            }
            if (player.hp <= 0) gameState = GameState.OVER;
        }
        // Enemies should still update even when game is outside play
        if (!(phase == Phase.TRANSITION || phase == Phase.ALLOCATE)) {resolveEnemies(delta);}


        // TODO: for convenience, remove later
        if (Gdx.input.isKeyPressed(Input.Keys.PERIOD)) {
            gameState = GameState.WIN;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.COMMA)) {
            gameState = GameState.OVER;
        }
    }

    /**
     * Returns remaining time for the STEALTH phase until TRANSITION
     */
    public float getRemainingTime() {
        return phaseTimer;
    }

    /**
     * Returns remaining enemies for BATTLE phase
     */
    public int getRemainingEnemies() {
        return enemies.size;
    }

    /**
     * Returns a reference to playerController associated with this gameplayController
     */
    public PlayerController getPlayerController() {
        return playerController;
    }

    /**
     * Performs all necessary computations to change the current phase of the game from STEALTH to BATTLE.
     */
    public void switchPhase(float delta) {
        updateAmbientLight(delta);
        if (ambientLightTransitionTimer >= container.getPhaseTransitionTime()) {
            phase = Phase.ALLOCATE;
            for (int i = 0; i < enemies.size; i++) {
                controls.get(enemies.get(i)).setInBattle(true);
            }
        }
    }

    /**
     * Interpolate between stealth ambiance and battle ambiance
     *
     * @param delta delta time
     */
    private void updateAmbientLight(float delta) {
        if (ambientLightTransitionTimer < container.getPhaseTransitionTime()) {
            ambientLightTransitionTimer += delta;
            float progress = Math.min(ambientLightTransitionTimer / container.getPhaseTransitionTime(), 1);

            float[] startColor = container.getStealthAmbience();
            float[] endColor = container.getBattleAmbience();

            // Interpolation function
            Interpolation erp = Interpolation.fade;

            float r = erp.apply(startColor[0], endColor[0], progress);
            float g = erp.apply(startColor[1], endColor[1], progress);
            float b = erp.apply(startColor[2], endColor[2], progress);
            float a = erp.apply(startColor[3], endColor[3], progress);

            container.getRayHandler().setAmbientLight(r, g, b, a);
        }
    }

    /**
     * Resolve any updates relating to enemies.
     */
    public void resolveEnemies(float delta) {
        // add enemies during battle stage and in play
        if (getPhase() == Phase.BATTLE && gameState == GameState.PLAY) {
            container.getEnemySpawner().update(battleTicks);
            if (battleTicks % 60 == 0) {
                tacticalManager.update();
            }
        }
        for (int i = 0; i < enemies.size; i++) {
            controls.get(enemies.get(i)).update(container, delta);
        }
    }
}