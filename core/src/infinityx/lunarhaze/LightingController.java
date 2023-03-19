package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.lunarhaze.entity.EnemyList;

public class LightingController {
    /**
     * List of enemies
     */
    private final EnemyList enemies;

    /**
     * Reference of board from container
     */
    private final Board board;

    /**
     * Contains constants for dust particle system settings
     */
    private final JsonValue dustInfo;

    /**
     * dustPool[p] holds the dust pool at tile p. Tile p should have collectable moonlight on it.
     */
    private IntMap<Dust[]> dustPools;

    /**
     * How many dust particles can be on a tile at once
     */
    public static final int POOL_CAPACITY = 20;

    /**
     * TODO: This is a controller class, so it should handle interactions between lights and objects
     * Maybe the light should update throughout the lifetime of the level??
     *
     * @param container level
     */
    public LightingController(LevelContainer container) {
        this.enemies = container.getEnemies();
        this.board = container.getBoard();

        // Parses specific GameObject (collider info, etc.) attributes.

        dustInfo = container.getDirectory().getEntry("dust", JsonValue.class);
        JsonValue texInfo = dustInfo.get("texture");

        // Initialize pools
        dustPools = new IntMap<>();
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                if (board.isCollectable(i, j)) {
                    Dust[] dusts = new Dust[POOL_CAPACITY];
                    for (int ii = 0; ii < POOL_CAPACITY; ii++) {
                        Dust dust = new Dust();
                        dust.setTexture(
                                container.getDirectory().getEntry(texInfo.getString("name"), Texture.class)
                        );
                        dust.setTextureScale(texInfo.getFloat("scale"));
                        initializeDust(i, j, dust);
                        dusts[ii] = dust;
                    }
                    int map_pos = i + j * board.getWidth();
                    dustPools.put(map_pos, dusts);

                    // add to level container so they draw
                    System.out.println("Adding pool");
                    container.addDrawables(dustPools.get(map_pos));
                }
            }
        }
    }

    /**
     * Begin decaying all particles that stray away from its assigned board tile.
     * Reinitialize all dust particles set to destroy.
     */
    public void updateDust(float delta) {
        for (IntMap.Entry<Dust[]> entry : dustPools) {
            int x = entry.key % board.getWidth();
            int y = (entry.key - x) / board.getWidth();
            for (Dust dust : entry.value) {
                dust.update(delta);
                if (board.worldToBoardX(dust.getX()) != x || board.worldToBoardY(dust.getY()) != y) {
                    dust.beginDecay();
                }
                if (dust.isDestroyed()) {
                    initializeDust(x, y, dust);
                }
            }
        }
    }

    /**
     * (Re)Initialize attributes of dust particle
     *
     * @param x board x-position
     * @param y board y-position
     */
    public void initializeDust(int x, int y, Dust dust) {
        // set random position inside tile
        dust.reset();
        dust.setX(board.boardToWorldX(x) + MathUtils.random() * board.getTileWorldDim().x);
        dust.setY(board.boardToWorldY(y) + MathUtils.random() * board.getTileWorldDim().y);

        JsonValue rps = dustInfo.get("rps");
        JsonValue spd = dustInfo.get("speed");
        JsonValue scl = dustInfo.get("scale");

        dust.setRPS(MathUtils.random(rps.getFloat(0), rps.getFloat(1)));
        dust.setVelocity(MathUtils.random() * MathUtils.PI2, MathUtils.random(spd.getFloat(0), spd.getFloat(1)));
        dust.setScale(MathUtils.random(scl.getFloat(0), scl.getFloat(1)));
    }
}
