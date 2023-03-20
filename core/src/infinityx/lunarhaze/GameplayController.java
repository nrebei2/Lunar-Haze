package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;

import java.util.ArrayList;
import java.util.Arrays;

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
        BATTLE
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
     * Owns the player controller
     */
    private PlayerController playerController;

    /**
     * Owns the collision controller, handles collisions
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
     * Creates a new GameplayController with no active elements.
     */
    public GameplayController() {
        player = null;
        enemies = null;
        board = null;
        currentPhase = Phase.STEALTH;
        gameState = GameState.PLAY;
    }

    /**
     * @return the current state of the game
     */
    public GameState getState() {
        return gameState;
    }

    /**
     * @return the current phase of the game
     */
    public Phase getPhase() {
        return currentPhase;
    }

    /**
     * Starts a new game, (re)initializing relevant controllers and attributes.
     * <p>
     *
     * @param levelContainer container holding model objects in level
     */
    public void start(LevelContainer levelContainer) {
        this.gameState = GameState.PLAY;
        this.currentPhase = Phase.STEALTH;
        this.container = levelContainer;
        this.collisionController = new CollisionController(levelContainer);
        lightingController = new LightingController(levelContainer);

        board = levelContainer.getBoard();
        player = levelContainer.getPlayer();
        this.playerController = new PlayerController(player, board, levelContainer);

        phaseTimer = levelContainer.getPhaseLength();
        ambientLightTransitionTimer = levelContainer.getPhaseTransitionTime();

        enemies = levelContainer.getEnemies();
        controls = levelContainer.getEnemyControllers();
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
        phaseTimer -= delta;
        lightingController.updateDust(delta);

        // FSM for state and phase
        if (gameState == GameState.PLAY) {
            // Process the player only when the game is in play
            playerController.update(input, delta, currentPhase, lightingController);
            switch (currentPhase) {
                case STEALTH:
                    if (container.getBoard().getRemainingMoonlight() == 0 || phaseTimer <= 0) switchPhase();
                    break;
                case BATTLE:
                    if (enemies.size == 0) gameState = GameState.WIN;
            }
            if (player.getHp() <= 0) gameState = GameState.OVER;
        }
        // Enemies should still update even when game is outside play
        resolveEnemies();
        updateAmbientLight(delta);

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

    /**
     * Performs all necessary computations to change the current phase of the game from STEALTH to BATTLE.
     */
    public void switchPhase() {
        currentPhase = Phase.BATTLE;
        ambientLightTransitionTimer = 0;
    }

    /**
     * ADD DOMUNEMTAION DONNY IDK WHAT THIS DOES
     *
     * @param delta
     */
    private void updateAmbientLight(float delta) {
        if (ambientLightTransitionTimer < container.getPhaseTransitionTime()) {
            ambientLightTransitionTimer += delta;
            float progress = Math.min(ambientLightTransitionTimer / container.getPhaseTransitionTime(), 1);

            float[] startColor = currentPhase == Phase.BATTLE ? container.getBattleAmbience() : container.getStealthAmbience();
            float[] endColor = currentPhase == Phase.BATTLE ? container.getStealthAmbience() : container.getBattleAmbience();

            // LERP performed here
            float r = startColor[0] * (1 - progress) + endColor[0] * progress;
            float g = startColor[1] * (1 - progress) + endColor[1] * progress;
            float b = startColor[2] * (1 - progress) + endColor[2] * progress;
            float a = startColor[3] * (1 - progress) + endColor[3] * progress;

            container.getRayHandler().setAmbientLight(r, g, b, a);
        }
    }

    /**
     * Resolve any updates for the active enemies through their controllers.
      */
    public void resolveEnemies() {
        if (getPhase() == Phase.BATTLE) {
            if (MathUtils.random() <= 0.01) {
                // TODO: enemies in battle phase should not use patrol path
                container.addEnemy("villager", 0, 0,
                        new ArrayList<>(Arrays.asList(new Vector2(), new Vector2()))
                );
            }
        }
        for (int i = 0; i < enemies.size; i++) {
            controls.get(enemies.get(i)).update(container, currentPhase);
        }
    }
}