package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyPool;
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
        BATTLE
    }
    private Phase currentPhase;

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

    /** Reference to level container, acts as a nice interface for all level models */
    private LevelContainer levelContainer;

    /**
     * Reference to player from container
     */
    private Werewolf player;

    /**
     * Reference to active enemies from container
     */
    private ObjectSet<Enemy> enemies;

    /** Reference to board from container */
    public Board board;

    /**
     * Owns the enemy controllers, controls[id] controls the enemy with that id
     */
    private EnemyController[] controls;

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
     * Timer for the phase transition
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
     * Starts a new game, reinitializing relevant controllers.
     * <p>
     * @param levelContainer container holding model objects in level
     */
    public void start(LevelContainer levelContainer) {
        this.gameState = GameState.PLAY;
        this.currentPhase = Phase.STEALTH;
        this.levelContainer = levelContainer;
        this.collisionController = new CollisionController(levelContainer);
        player = levelContainer.getPlayer();
        enemies = levelContainer.getEnemies();
        board = levelContainer.getBoard();
        this.playerController = new PlayerController(player, board, levelContainer);
        controls = new EnemyController[enemies.size];
        phaseTimer = levelContainer.getPhaseLength();
        ambientLightTransitionTimer = levelContainer.getPhaseTransitionTime();

        int i = 0;
        for (Enemy enemy: enemies) {
            controls[i] = new EnemyController(player, enemies, board, enemy);
            i++;
        }

        lightingController = new LightingController(levelContainer);
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
                    if (levelContainer.getBoard().getRemainingMoonlight() == 0 || phaseTimer <= 0) switchPhase();
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
     *
     */
    public void switchPhase() {
        currentPhase = Phase.BATTLE;
        ambientLightTransitionTimer = 0;
    }

    /**
     * ADD DOMUNEMTAION DONNY IDK WHAT THIS DOES
     * @param delta
     */
    private void updateAmbientLight(float delta) {
        if (ambientLightTransitionTimer < levelContainer.getPhaseTransitionTime()) {
            ambientLightTransitionTimer += delta;
            float progress = Math.min(ambientLightTransitionTimer / levelContainer.getPhaseTransitionTime(), 1);

            float[] startColor = currentPhase == Phase.BATTLE ? levelContainer.getBattleAmbience() : levelContainer.getStealthAmbience();
            float[] endColor = currentPhase == Phase.BATTLE ? levelContainer.getStealthAmbience() : levelContainer.getBattleAmbience();

            // LERP performed here
            float r = startColor[0] * (1 - progress) + endColor[0] * progress;
            float g = startColor[1] * (1 - progress) + endColor[1] * progress;
            float b = startColor[2] * (1 - progress) + endColor[2] * progress;
            float a = startColor[3] * (1 - progress) + endColor[3] * progress;

            levelContainer.getRayHandler().setAmbientLight(r, g, b, a);
        }
    }

    // TODO: THIS SHOULD BE IN ENEMYCONTROLLER, also this code is a mess
    public void resolveEnemies() {
        for (Enemy en : enemies) {
            if (controls[en.getId()] != null) {
                EnemyController curEnemyController = controls[en.getId()];
//                int action = curEnemyController.getAction(levelContainer);
                Vector2 movement = curEnemyController.getMovement(levelContainer);
                en.update(movement);
                // TODO: make more interesting actions                //curEnemyController.setVisibleTiles();
//                if (en.getIsAlerted()) {
//                    // angle between enemy and player
//                    double ang = Math.atan2(player.getPosition().y - en.getPosition().y, player.getPosition().x - en.getPosition().y);
//                    en.setFlashLightRot((float) ang);
//                } else {
//                    en.setFlashLightRot();
//                }
            } else {
                en.update(new Vector2());
            }
        }
    }
}