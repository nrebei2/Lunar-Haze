package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.lunarhaze.models.Board;
import infinityx.lunarhaze.models.Dust;
import infinityx.lunarhaze.models.LevelContainer;

import java.util.Iterator;

public class LightingController {
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
    private final IntMap<Dust[]> dustPools;

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
        this.board = container.getBoard();

        dustInfo = container.getDirectory().getEntry("dust", JsonValue.class);
        JsonValue texInfo = dustInfo.get("texture");
        JsonValue fade = dustInfo.get("fade-time");

        // Initialize pools
        dustPools = new IntMap<>();
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                if (board.isCollectable(i, j)) {
                    Dust[] dusts = new Dust[POOL_CAPACITY];
                    for (int ii = 0; ii < POOL_CAPACITY; ii++) {
                        Dust dust = new Dust();
                        // Set main attributes once
                        dust.setTexture(
                                container.getDirectory().getEntry(texInfo.getString("name"), Texture.class)
                        );
                        dust.setTextureScale(texInfo.getFloat("scale"));
                        dust.setFadeRange(fade.getFloat(0), fade.getFloat(1));
                        initializeDust(i, j, dust);
                        dusts[ii] = dust;
                    }
                    int map_pos = i + j * board.getWidth();
                    dustPools.put(map_pos, dusts);

                    // add to level container so they draw
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
        Iterator<IntMap.Entry<Dust[]>> iterator = dustPools.iterator();
        while (iterator.hasNext()) {
            IntMap.Entry<Dust[]> entry = iterator.next();
            int x = entry.key % board.getWidth();
            int y = (entry.key - x) / board.getWidth();

            boolean allDestroyed = true;
            for (Dust dust : entry.value) {
                dust.update(delta);
                if (!dust.isDestroyed()) allDestroyed = false;
                if (dust.inDestruction()) continue;
                if (board.worldToBoardX(dust.getX()) != x || board.worldToBoardY(dust.getY()) != y) {
                    dust.beginReset();
                }
                if (dust.shouldReset()) {
                    initializeDust(x, y, dust);
                }
            }
            // Remove from iterator if all destroyed
            // TODO: maybe add dust to some free list if moonlight updates
            if (allDestroyed) {
                iterator.remove();
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

    /**
     * Begin removal of all dust particles on given board tile
     *
     * @param x board x-position
     * @param y board y-position
     */
    public void removeDust(int x, int y) {
        for (Dust dust : dustPools.get(x + y * board.getWidth())) dust.beginDestruction();
    }
}
