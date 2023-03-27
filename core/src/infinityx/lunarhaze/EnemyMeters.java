package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.graphics.InstanceRenderer;

import java.nio.FloatBuffer;

/**
 * View class holding information for drawing the enemy stealth meters
 */
public class EnemyMeters {
    /**
     * Renderer to draw meters
     */
    InstanceRenderer render;

    /**
     * Reference of active enemies from container
     */
    Array<Enemy> enemies;

    /**
     * Parse and initialize specific attributes.
     *
     * @param directory asset directory holding shader info
     * @param container LevelContainer holding active enemy list
     */
    public void initialize(AssetDirectory directory, LevelContainer container) {
        enemies = container.getEnemies();
        // TODO: make shaderparser/loader
        ShaderProgram program = directory.getEntry("meter", ShaderProgram.class);
        render = new InstanceRenderer(program);
        render.draw(10, 10);
        // TODO: assuming here that the max amount of enemies on a screen is 20
        render.initialize(false, 20,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, "i_offset"),
                new VertexAttribute(VertexAttributes.Usage.Position, 1, "i_amount"));
    }

    /**
     * Draw meters for each active enemy on the screen.
     */
    public void drawMeters() {
        FloatBuffer offsets = BufferUtils.newFloatBuffer(enemies.size * 3);
        for (Enemy enemy : enemies) {
            offsets.put(new float[]{
                    //TODO
            });
        }
        offsets.position(0);
        render.setInstanceData(offsets);
        render.begin();
        render.end();
    }
}
