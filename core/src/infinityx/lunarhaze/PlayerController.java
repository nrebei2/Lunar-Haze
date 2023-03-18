package infinityx.lunarhaze;

import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;

// TODO: Rainney needs to add stealth bar and hp bar
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
     * Remaining moonlight on the map
     */
    private int remainingMoonlight;

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

    /**
     * Whether the player won the game
     */
    private Boolean gameWon;

    /**
     * Get the player HP in PlayerController to enforce encapsulation.
     *
     * Return HP of the player, int ranging from 0~5.
     */
    public int getPlayerHp(){
        return player.getHp();
    }

    /**
     * Get the player light in PlayerController to enforce encapsulation.
     *
     * Return light collected of the player, float ranging from 0~100.
     */
    public float getPlayerLight(){
        return player.getLight();
    }

    /**
     * Get the player stealth in PlayerController to enforce encapsulation.
     *
     * Return stealth of the player, float ranging from 0~1.
     */
    public float getPlayerStealth(){
        return player.getStealth();
    }

    /**
     * Get whether player achieves the condition to win.
     */
    public Boolean isGameWon() {
        return gameWon;
    }

    /**
     * Set whether player achieves the condition to win.
     */
    public void setGameWon(Boolean value) {
        gameWon = value;
    }

    /**
     * Initializer of a PlayerController
     */
    public PlayerController(Werewolf player, Board board, LevelContainer levelContainer) {
        this.player = player;
        this.board = board;
        timeOnMoonlight = 0;
        this.levelContainer = levelContainer;
        remainingMoonlight = levelContainer.getRemainingMoonlight();
        gameWon = false;
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
        player.update(delta);
    }

    /**
     * Player collect one moonlight, including upgrading the stats and status.
     * <p>
     */
    public void collectMoonlight() {
        player.addMoonlightCollected();
        remainingMoonlight--;
        player.setLight(player.getLight() + (Werewolf.MAX_LIGHT / levelContainer.getTotalMoonlight()));
        System.out.println(levelContainer.getRemainingMoonlight());
    }

    /**
     * Process the player's interaction with moonlight tile.
     * <p>
     *
     * @param delta Number of seconds since last animation frame
     */
    public void resolveMoonlight(float delta) {
        int px = board.worldToBoardX(player.getPosition().x);
        int py = board.worldToBoardX(player.getPosition().y);

        if (board.isLit(px, py)) {
            timeOnMoonlight += delta; // Increase variable by time
            player.setOnMoonlight(true);
            if (board.isCollectable(px, py) && (timeOnMoonlight > MOONLIGHT_COLLECT_TIME)) {
                collectMoonlight();
                System.out.println(player.getLight());
                System.out.println("Remaining moonlight: " + remainingMoonlight);
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

    /**
     * Process the player's stealth value. This depends on the walk/run mode.
     * <p>
     *
     * @param input InputController that controls the player
     */
    public void resolveSealthBar(InputController input) {
        if (Math.abs(input.getHorizontal()) == input.getWalkSpeed() ||
                Math.abs(input.getVertical()) == input.getWalkSpeed()){
            player.setStealth(WALK_STEALTH);
        } else if (Math.abs(input.getHorizontal()) == input.getRunSpeed() ||
                Math.abs(input.getVertical()) == input.getRunSpeed()){
            player.setStealth(RUN_STEALTH);
        } else if (input.getHorizontal() == 0 || input.getVertical() == 0 ){
            player.setStealth(STILL_STEALTH);
        }
    }

    public void update(InputController input, float delta){
        resolvePlayer(input, delta);
        resolveMoonlight(delta);
        resolveSealthBar(input);
    }

    /**
     * Let the player lose 1 HP point. This function should be called by CollisionController
     * when the player collides with one of the enemies.
     */
    public void loseHp() {
        player.setHp(player.getHp() - 1);
    }

}
