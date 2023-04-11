package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import infinityx.lunarhaze.ai.TacticalManager;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemySpawner;
import infinityx.lunarhaze.entity.Werewolf;

/**
 * Controller to handle gameplay interactions.
 */
public class GameplayController {
    /**
     * We are separating our main game into two phases:
     * - Stealth: Collect moonlight
     * - Battle: Hack-n-slash
     */
    public enum Phase {
        STEALTH,
        BATTLE,
        TRANSITION,
        ALLOCATE,
    }

    public Phase currentPhase;

    /**
     * Track the current state of the game for the update loop.
     * - Play: The player could play the game
     * - Over: The werewolf has been killed by an enemy!!
     * - Win: The player has killed all the enemies. Should only be set when the phase is DAY
     */
    public enum GameState {
        PLAY,
        OVER,
        PAUSED,
        WIN
    }

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
     * Reference of model-contoller map originially from EnemyPool
     */
    private ObjectMap<Enemy, EnemyController> controls;

    /**
     * Owns the lighting controller
     */
    private LightingController lightingController;

    /**
     * Owns the enemy spawner, used for battle phase
     */
    private EnemySpawner enemySpawner;

    /**
     * Owns the player controller
     */
    private PlayerController playerController;

    private TacticalManager tacticalManager;

    /**
     * Owns the collision controller, handles collisions
     */
    private CollisionController collisionController;

//    private AllocateScreen allocateScreen;

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


    /**
     * Creates a new GameplayController with no active elements.
     */
    public GameplayController() {
        player = null;
        enemies = null;
        board = null;
    }

    /**
     * @return the current state of the game
     */
    public GameState getState() {
        return gameState;
    }
    public void setState(GameState s) { gameState = s; }

    public float getTimeOnMoonlightPercentage(){return playerController.getTimeOnMoonlightPercentage();}

    public boolean getCollectingMoonlight(){return playerController.isCollectingMoonlight();}

    /**
     * @return the current phase of the game
     */
    public Phase getPhase() {
        return currentPhase;
    }

    /**
     * Set the current phase of the game
     */
    public void setPhase(Phase p) {
        currentPhase = p;
    }

    /**
     * Starts a new game, (re)initializing relevant controllers and attributes.
     * <p>
     *
     * @param levelContainer container holding model objects in level
     * @param jsonValue      json value holding level layout
     */
    public void start(LevelContainer levelContainer, JsonValue jsonValue) {

        System.out.println("GameplayController start method called");
        this.gameState = GameState.PLAY;
        this.currentPhase = Phase.STEALTH;
        this.container = levelContainer;
        this.collisionController = new CollisionController(levelContainer);
        this.enemySpawner = new EnemySpawner(levelContainer);
        enemySpawner.initialize(jsonValue.get("settings").get("enemy-spawner"));

        lightingController = new LightingController(levelContainer);

        board = levelContainer.getBoard();
        player = levelContainer.getPlayer();
        this.playerController = new PlayerController(player, board, levelContainer, lightingController);

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
     * @param input Reference to the input controller
     * @param delta Number of seconds since last animation frame
     */
    public void resolveActions(InputController input, float delta) {
        // Update the phase timer
        lightingController.updateDust(delta);

        // FSM for state and phase
        if (gameState == GameState.PLAY) {
            // Process the player only when the game is in play
            playerController.update(input, delta, currentPhase);
            switch (currentPhase) {
                case STEALTH:
                    phaseTimer -= delta;
                    if (container.getBoard().getRemainingMoonlight() == 0 || phaseTimer <= 0)
                        currentPhase = Phase.TRANSITION;
                    break;
                case BATTLE:
                    battleTicks += 1;
                    if (enemies.size == 0) {
                        gameState = GameState.WIN;
                        win_sound.play();
                    }
                    break;
                case TRANSITION:
                    switchPhase(delta);
                    break;
                case ALLOCATE:
                    // TODO: Somehow pause the game before drawing allocating screen
                    if (playerController.getAllocateReady()){
                        currentPhase = Phase.BATTLE;
                    }
                    break;
            }
            if (player.getHp() <= 0) gameState = GameState.OVER;
        }
        // Enemies should still update even when game is outside play
        resolveEnemies(delta);

        // TODO: for convenience, remove later
        if (Gdx.input.isKeyPressed(Input.Keys.PERIOD)) {
            gameState = GameState.WIN;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.COMMA)) {
            gameState = GameState.OVER;
        }
    }

    public float getRemainingTime() {
        return phaseTimer;
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    public void setPlayerController(PlayerController pc) {
        playerController = pc;
    }

    /**
     * Performs all necessary computations to change the current phase of the game from STEALTH to BATTLE.
     */
    public void switchPhase(float delta) {
        updateAmbientLight(delta);
        if (ambientLightTransitionTimer >= container.getPhaseTransitionTime()) currentPhase = Phase.ALLOCATE;
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
            enemySpawner.update(battleTicks);
            if (battleTicks % 60 == 0) {
                tacticalManager.update();
            }
        }
        for (int i = 0; i < enemies.size; i++) {
            controls.get(enemies.get(i)).update(container, currentPhase, delta);
        }
    }
}