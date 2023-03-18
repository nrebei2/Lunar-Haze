package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;
import infinityx.lunarhaze.entity.EnemyList;

public class LightingController {
    /**
     * List of enemies
     */
    private final EnemyList enemies;

    /** Reference of board from container */
    private final Board board;

    /** dustPool[p] holds the dust pool at tile p. Tile p should have collectable moonlight on it. */
    private IntMap<Dust[]> dustPools;

    /** How many dust particles can be on a tile at once */
    public static final int POOL_CAPACITY = 10;

    /**
     * TODO: This is a controller class, so it should handle interactions between lights and objects
     * Maybe the light should update throughout the lifetime of the level??
     *
     * @param container level
     */
    public LightingController(LevelContainer container) {
        this.enemies = container.getEnemies();
        this.board = container.getBoard();

        // Initialize pools
        dustPools = new IntMap<>();
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                if (board.isCollectable(i, j)) {
                    Dust[] dusts = new Dust[POOL_CAPACITY];
                    for(int ii = 0; ii < POOL_CAPACITY; ii++) {
                        dusts[ii] = new Dust();
                    }
                    dustPools.put(i + j* board.getWidth(), dusts);
                }
            }
        }
    }

    public void updateDust() {

    }
}
