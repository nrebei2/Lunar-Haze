package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.lunarhaze.controllers.EnemyState;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Enemy;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Basically a monster spawner. Used for the battle phase.
 */
public class EnemySpawner {

    /**
     * level container to spawn enemies in
     */
    private final LevelContainer container;

    /**
     * Minimum and Maximum time (in ticks) to spawn a new enemy
     */
    private int addMin;
    private int addMax;

    /**
     * Tick number for when we add a new enemy in battle mode
     */
    private int enemyAddTick;

    /**
     * Maximum number of enemies this spawner can spawn
     */
    private int count;

    /**
     * @param levelContainer providing API to add enemies
     */
    public EnemySpawner(LevelContainer levelContainer) {
        this.container = levelContainer;
    }

    /**
     * Initialize attributes of this spawner
     *
     * @param settings json tree holding spawner info
     */
    public void initialize(JsonValue settings) {
        int[] addInfo = settings.get("add-tick").asIntArray();
        this.addMin = addInfo[0];
        this.addMax = addInfo[1];
        this.count = settings.getInt("count");

        // delay (in ticks) to begin spawning
        int delay = settings.getInt("delay");
        enemyAddTick = delay;
    }

    /**
     * Add enemy to level when applicable.
     *
     * @param tick notion of number of ticks elapsed
     */
    public void update(int tick) {
        if (count <= 0) return;
        if (tick % enemyAddTick == 0) {
            // TODO: enemies in battle phase should not use patrol path
            // TODO: smart placement for enemies
            Enemy newEnemy = container.addEnemy("villager", 6, 5,
                    new ArrayList<>(Arrays.asList(new Vector2(), new Vector2()))
            );
            // This spawner is only used in battle phase
            container.getEnemyControllers().get(newEnemy).setInBattle(true);
            container.getEnemyControllers().get(newEnemy).getStateMachine().changeState(EnemyState.ALERT);
            enemyAddTick = MathUtils.random(addMin, addMax);
            this.count--;
        }
    }
}
