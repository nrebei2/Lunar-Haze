package infinityx.lunarhaze.controllers;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.lunarhaze.models.Board;
import infinityx.lunarhaze.models.Dust;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Werewolf;

import java.util.Iterator;

public class LightingController {
    /**
     * Reference of board from container
     */
    private final Board board;

    /**
     * Reference of player from container
     */
    private final Werewolf player;

    /**
     * Contains constants for dust particle system settings
     */
    private final JsonValue dustInfo;

    /**
     * Reference of lamp lights from container
     */
    private final Array<PointLight> lampLights;

    /**
     * dustPool[p] holds the dust pool at tile p. Tile p should have collectable moonlight on it.
     */
    private IntMap<Dust[]> dustPools;

    /**
     * How many dust particles can be on a tile at once
     */
    public static final int POOL_CAPACITY = 40;

    /**
     * How long (in seconds) each lamp light is turned on or off
     */
    private static final float FLASH_INTERVAL = 4.0f;

    /**
     * For use with {@link #FLASH_INTERVAL}
     */
    private float flash_timer;

    /**
     * @param container level
     */
    public LightingController(LevelContainer container) {
        this.board = container.getBoard();
        this.player = container.getPlayer();
        this.lampLights = container.getLampLights();

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
                    for (Dust dust: dustPools.get(map_pos)) {
                        container.addDrawable(dust);
                    }
                }
            }
        }

        this.flash_timer = 0;
    }

    /**
     * Update dust and lamp lights
     */
    public void update(float delta) {
        flash_timer += delta;
        if (flash_timer >= FLASH_INTERVAL) {
            flash_timer = 0;
            // Toggle active for all lamp lights
            for (PointLight lamp : lampLights) {
                lamp.setActive(!lamp.isActive());
            }
        }
        updateDust(delta);
    }

    /**
     * Begin decaying all particles that stray away from its assigned board tile.
     * Reinitialize all dust particles set to destroy.
     */
    public void updateDust(float delta) {
        boolean collecting = false;
        int bx = 0, by = 0;
        if (player.isCollecting) {
            bx = board.worldToBoardX(player.getPosition().x);
            by = board.worldToBoardY(player.getPosition().y);
            collecting = true;
        }

        Iterator<IntMap.Entry<Dust[]>> iterator = dustPools.iterator();
        while (iterator.hasNext()) {
            IntMap.Entry<Dust[]> entry = iterator.next();
            int x = entry.key % board.getWidth();
            int y = (entry.key - x) / board.getWidth();
            boolean collectable = board.isCollectable(x, y);

            boolean allDestroyed = true;
            for (Dust dust : entry.value) {
                dust.update(delta);
                if (!collectable && !dust.inDestruction()) {
                    dust.beginDestruction();
                }
                if (!dust.isDestroyed()) allDestroyed = false;
                if (dust.inDestruction()) continue;

                if (collecting && bx == x && by == y) {
                    dust.setVelocity(
                            player.getX() - dust.getX(),
                            player.getY() - dust.getY(),
                            0
                    ).scl(MathUtils.random(0.5f, 0.6f));
                }

                if (!board.inBoundsTileX(x, dust.getX()) || !board.inBoundsTileY(y, dust.getY())) {
                    dust.beginReset();
                }
                if (dust.shouldReset()) {
                    initializeDust(x, y, dust);
                }
            }
            // Remove from iterator if all destroyed
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
        dust.setZ(Interpolation.pow3In.apply(MathUtils.random()) * 1.3f);

        JsonValue rps = dustInfo.get("rps");
        JsonValue spd = dustInfo.get("speed");
        JsonValue scl = dustInfo.get("scale");

        dust.setRPS(MathUtils.random(rps.getFloat(0), rps.getFloat(1)));
        dust.setVelocity(MathUtils.random() * MathUtils.PI2, MathUtils.random(spd.getFloat(0), spd.getFloat(1)));
        dust.setScale(MathUtils.random(scl.getFloat(0), scl.getFloat(1)));
    }

    /**
     * Destroy all dust particles.
     */
    public void dispose() {
        for (Dust[] pool : dustPools.values()) {
            for (Dust dust : pool) dust.setDestroyed(true);
        }
        dustPools = null;
    }

    public Array<PointLight> getLampLights() {
        return lampLights;
    }
}
