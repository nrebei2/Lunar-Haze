package infinityx.lunarhaze;

import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;

/**
 * Controller to handle gameplay interactions.
 */
public class GameplayController {
    /**
     * Current game phase;
     */
    private Phase gamePhase;

    /**
     * Current game state;
     * */
    private GameState gameState;

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
     */
    enum GameState {
        PLAY,
        /** The werewolf has been killed by an enemy!! */
        OVER,
        /** The player has killed all the enemies. Should only be set when the phase is DAY */
        WIN
    }

    /** Reference to level container, acts as a nice interface for all level models */
    private LevelContainer levelContainer;

    /**
     * Reference to player from container
     */
    private Werewolf player;

    /**
     * Reference to enemies from container
     */
    private EnemyList enemies;

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


    private static final float NIGHT_DURATION = 10f;
    private static final float PHASE_TRANSITION_TIME = 3f;

    private static final float[] DAY_COLOR = {1f, 1f, 1f, 1f};
    private static final float[] NIGHT_COLOR = {0.55f, 0.52f, 0.62f, 0.55f};
    private float ambientLightTransitionTimer;
    private float phaseTimer;


    public GameplayController() {
        player = null;
        enemies = null;
        board = null;
        currentPhase = Phase.STEALTH;
        ambientLightTransitionTimer = PHASE_TRANSITION_TIME;
    }

    /**
     * Returns a reference to the currently active player.
     * <p>
     * This property needs to be modified if you want multiple players.
     *
     * @return a reference to the currently active player.
     */
    public Werewolf getPlayer() {
        return player;
    }

    /**
     * Returns a reference to the current PlayerController.
     */
    public PlayerController getPlayerController() {
        return playerController;
    }

    /**
     * Returns true if the currently active player is alive.
     *
     * @return true if the currently active player is alive.
     */
    public boolean isAlive() {
        return player != null;
    }

    /**
     * Starts a new game.
     * <p>
     * This method creates a single player, but does nothing else.
     */
    public void start(LevelContainer levelContainer) {
        this.levelContainer = levelContainer;
        this.collisionController = new CollisionController(levelContainer);
        player = levelContainer.getPlayer();
        enemies = levelContainer.getEnemies();
        board = levelContainer.getBoard();
        this.playerController = new PlayerController(player, board, levelContainer);
        controls = new EnemyController[enemies.size()];
        phaseTimer = NIGHT_DURATION;


        for (int ii = 0; ii < enemies.size(); ii++) {
            controls[ii] = new EnemyController(ii, player, enemies, board);
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
            playerController.update(input, delta, currentPhase);
            switch (gamePhase) {
                case BATTLE:
                    if (levelContainer.getRemainingMoonlight() == 0 || phaseTimer <= 0) gamePhase = Phase.STEALTH;
                    break;
                case STEALTH:
                    if (enemies.size() == 0) gameState = GameState.WIN;
            }
            if (playerController.getPlayerHp() <= 0) gameState = GameState.OVER;
        }
        // Enemies should still update even when game is outside play
        resolveEnemies();

        if (phaseTimer <= 0) {
            switchPhase();
        }
        updateAmbientLight(delta);
    }

    /**
     * Changes the current phase of the game between NIGHT and DAY.
     * When switching from NIGHT to DAY, it resets the phase timer to the duration
     * of the DAY phase. Similarly, when switching from DAY to NIGHT, it resets
     * the phase timer to the duration of the NIGHT phase.
     */

    public void switchPhase() {
        currentPhase = BAT
        ambientLightTransitionTimer = 0;
    }

    private void updateAmbientLight(float delta) {
        if (ambientLightTransitionTimer < PHASE_TRANSITION_TIME) {
            ambientLightTransitionTimer += delta;
            float progress = Math.min(ambientLightTransitionTimer / PHASE_TRANSITION_TIME, 1);

            float[] startColor = currentPhase == Phase.BATTLE ? NIGHT_COLOR : DAY_COLOR;
            float[] endColor = currentPhase == Phase.BATTLE ? DAY_COLOR : NIGHT_COLOR;

            // LERP performed here
            float r = startColor[0] * (1 - progress) + endColor[0] * progress;
            float g = startColor[1] * (1 - progress) + endColor[1] * progress;
            float b = startColor[2] * (1 - progress) + endColor[2] * progress;
            float a = startColor[3] * (1 - progress) + endColor[3] * progress;

            levelContainer.getRayHandler().setAmbientLight(r, g, b, a);
        }
    }
    /**
     * Resets the game, deleting all objects.
     */
    public void reset() {
        player = null;

    }


    // TODO: THIS SHOULD BE IN ENEMYCONTROLLER, also this code is a mess
    public void resolveEnemies() {
        for (Enemy en : enemies) {
            if (controls[en.getId()] != null) {
                EnemyController curEnemyController = controls[en.getId()];
                int action = curEnemyController.getAction(levelContainer);
                en.update(action);

                // TODO: make more interesting actions                //curEnemyController.setVisibleTiles();
                if (en.getIsAlerted()) {
                    // angle between enemy and player
                    double ang = Math.atan2(player.getPosition().y - en.getPosition().y, player.getPosition().x - en.getPosition().y);
                    en.setFlashLightRot((float) ang);
                } else {
                    en.setFlashLightRotAlongDir();
                }
            } else {
                en.update(EnemyController.CONTROL_NO_ACTION);
            }
        }
    }
}