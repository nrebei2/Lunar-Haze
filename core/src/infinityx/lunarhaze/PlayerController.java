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

    public Boolean isGameWon() {
        return gameWon;
    }

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
     * Process the player's actions.
     * <p>
     * Notice that firing bullets allocates memory to the heap.  If we were REALLY
     * worried about performance, we would use a memory pool here.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void resolvePlayer(InputController input, float delta) {
        player.setMovementH(input.getHorizontal());
        player.setMovementV(input.getVertical());
        player.update(delta);
    }

    public void collectMoonlight() {
        player.addMoonlightCollected();
        remainingMoonlight--;
        player.setLight(player.getLight() + (Werewolf.MAX_LIGHT / levelContainer.getTotalMoonlight()));
        System.out.println(levelContainer.getRemainingMoonlight());
    }

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

    public void resolveSealthBar(InputController input, float delta) {
        // TODO
    }

    public void loseHp() {
        player.setHp(player.getHp() - 1);
    }



}
