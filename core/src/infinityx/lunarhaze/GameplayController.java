package infinityx.lunarhaze;

import box2dLight.RayHandler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.RaycastInfo;

import java.awt.*;


/**
 * Controller to handle gameplay interactions.
 */
public class GameplayController {
    /**
     * Reference to player (need to change to allow multiple players)
     */
    private Werewolf player;

    private EnemyList enemies;

    public enum Phase {
        NIGHT,
        DAY
    }

    /**
     * The currently active object
     */
    private final Array<GameObject> objects;

    private EnemyController[] controls;

    private LightingController lightingController;

    private PlayerController playerController;

    public Board board;

    private boolean gameWon;

    private boolean gameLost;
    private LevelContainer levelContainer;

    private Phase currentPhase;

    private static final float NIGHT_DURATION = 10f;
    private static final float DAY_DURATION = 10f;

    private static final float PHASE_TRANSITION_TIME = 3f;

    private static final float[] DAY_COLOR = {1f, 1f, 1f, 1f};
    private static final float[] NIGHT_COLOR = {0.55f, 0.52f, 0.62f, 0.55f};
    private float ambientLightTransitionTimer;
    private float phaseTimer;

    private Color ambientLight;

    /**This is the collision controller (handels collisions between all objects in our world*/

    private CollisionController collisionController;


    public GameplayController() {
        player = null;
        enemies = null;
        board = null;
        objects = new Array<GameObject>();
        currentPhase = Phase.NIGHT;
        ambientLightTransitionTimer = PHASE_TRANSITION_TIME;
        ambientLight = new Color(0.55f, 0.52f, 0.62f, 0.55f);
    }

    /**
     * Returns the list of the currently active (not destroyed) game objects
     * <p>
     * As this method returns a reference and Lists are mutable, other classes can
     * technical modify this list.  That is a very bad idea.  Other classes should
     * only mark objects as destroyed and leave list management to this class.
     *
     * @return the list of the currently active (not destroyed) game objects
     */
    public Array<GameObject> getObjects() {
        return objects;
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
        objects.add(player);
        board = levelContainer.getBoard();
        this.playerController = new PlayerController(player, board, levelContainer);
        controls = new EnemyController[enemies.size()];
        phaseTimer = NIGHT_DURATION;

        gameWon = false;
        gameLost = false;

        for (int ii = 0; ii < enemies.size(); ii++) {
            Enemy curr = enemies.get(ii);
            controls[ii] = new EnemyController(ii, player, enemies, board);
            objects.add(enemies.get(ii));
        }

        // Intialize lighting
        lightingController = new LightingController(levelContainer);

        /*PointLight light = new PointLight(getRayHandler(), 512, new Color(0.5f, 0.5f, 1f, 0.3f), 2000f, 0, 0);
         */
    }

    /**
     * Resets the game, deleting all objects.
     */
    public void reset() {
        player = null;
        objects.clear();
    }

    /**
     * Resolve the actions of all game objects (player and shells)
     * <p>
     * You will probably want to modify this heavily in Part 2.
     *
     * @param input Reference to the input controller
     * @param delta Number of seconds since last animation frame
     */
    public void resolveActions(InputController input, float delta) {
        // Process the player only when the game is in play
        lightingController.updateDust(delta);
        if (player != null && !(gameLost || gameWon)) {
            playerController.update(input, delta, currentPhase);
            gameWon = playerController.isGameWon();
            gameLost = playerController.getPlayerHp() <= 0;
        }
        EnemyController.resolveEnemies(controls,player,enemies);

        // Update the phase timer and switch phases if necessary
        phaseTimer -= delta;
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
        if (currentPhase == Phase.NIGHT) {
            currentPhase = Phase.DAY;
            phaseTimer = DAY_DURATION;
        } else {
            currentPhase = Phase.NIGHT;
            phaseTimer = NIGHT_DURATION;
        }
        ambientLightTransitionTimer = 0;
    }

    private void updateAmbientLight(float delta) {
        if (ambientLightTransitionTimer < PHASE_TRANSITION_TIME) {
            ambientLightTransitionTimer += delta;
            float progress = Math.min(ambientLightTransitionTimer / PHASE_TRANSITION_TIME, 1);

            float[] startColor = currentPhase == Phase.DAY ? NIGHT_COLOR : DAY_COLOR;
            float[] endColor = currentPhase == Phase.DAY ? DAY_COLOR : NIGHT_COLOR;

            // LERP performed here
            float r = startColor[0] * (1 - progress) + endColor[0] * progress;
            float g = startColor[1] * (1 - progress) + endColor[1] * progress;
            float b = startColor[2] * (1 - progress) + endColor[2] * progress;
            float a = startColor[3] * (1 - progress) + endColor[3] * progress;

            levelContainer.getRayHandler().setAmbientLight(r, g, b, a);
        }
    }


    public RaycastInfo raycast(GameObject requestingObject, Vector2 point1, Vector2 point2){
        RaycastInfo callback = new RaycastInfo(requestingObject);
        World world = levelContainer.getWorld();
        world.rayCast(callback, new Vector2(point1.x, point1.y), new Vector2(point2.x, point2.y));
        return callback;
    }

    public boolean detectPlayer(Enemy enemy){
        Vector2 point1 = enemy.getPosition();
        float dist = enemy.getFlashlight().getDistance();
        float vx = enemy.getVX();
        float vy = enemy.getVY();
        if (vx == 0 && vy ==0) return false;
        Vector2 direction = new Vector2(vx, vy).nor();
        Vector2 point2 = new Vector2(point1.x + dist*direction.x, point1.y + dist*direction.y);
        RaycastInfo info = raycast(enemy, point1, point2);
        return info.hit && info.hitObject == player;
    }
    
    public void resolvePlayer(InputController input, float delta) {
        player.setMovementH(input.getHorizontal());

        player.setMovementV(input.getVertical());
        player.update(delta);
    }

    public void resolveMoonlight(float delta) {
        int px = board.worldToBoardX(player.getPosition().x);
        int py = board.worldToBoardX(player.getPosition().y);

        if (board.isLit(px, py)) {
            if (board.isCollectable(px, py)) {
                timeOnMoonlight += delta; // Increase variable by time
            }
            player.setOnMoonlight(true);
            if (board.isCollectable(px, py) && timeOnMoonlight > MOONLIGHT_COLLECT_TIME) {
                player.collectMoonlight(levelContainer);
                remainingMoonlight--;
                timeOnMoonlight = 0;
                board.setCollected(px, py);
            }
            // Check if game is won here
            if (remainingMoonlight == 0) gameWon = true;
        } else {
            timeOnMoonlight = 0;
            player.setOnMoonlight(false);
        }
    }

    // TODO: THIS SHOULD BE IN ENEMYCONTROLLER, also this code is a mess
    public void resolveEnemies() {
        //board.clearVisibility();
        for (Enemy en : enemies) {
            if (controls[en.getId()] != null) {
                EnemyController curEnemyController = controls[en.getId()];
                int action = curEnemyController.getAction(levelContainer);
//                boolean attacking = (action & EnemyController.CONTROL_ATTACK) != 0;
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

    public boolean isGameWon() {
        return gameWon;
    }

    public Phase getCurrentPhase() { return currentPhase; }

    public void setWin(boolean win) {
        if (win) this.gameWon = true;
        else this.gameLost = true;
    }

    public boolean isGameLost() {
        return gameLost;
    }


    /**
     * Garbage collects all deleted objects.
     *
     * This method works on the principle that it is always cheaper to copy live objects
     * than to delete dead ones.  Deletion restructures the list and is O(n^2) if the
     * number of deletions is high.  Since Add() is O(1), copying is O(n).
     *
     public void garbageCollect() {
     // INVARIANT: backing and objects are disjoint
     for (GameObject o : objects) {
     if (o.isDestroyed()) {
     destroy(o);
     } else {
     backing.add(o);
     }
     }

     // Swap the backing store and the objects.
     // This is essentially stop-and-copy garbage collection
     Array<GameObject> tmp = backing;
     backing = objects;
     objects = tmp;
     backing.clear();
     }
     */


}