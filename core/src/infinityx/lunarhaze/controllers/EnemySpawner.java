package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.Settings;
import infinityx.lunarhaze.models.entity.Enemy;

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
    private float addMin, addMax;

    /**
     * Time for when we add a new enemy in battle mode
     */
    private float enemyAddTime;

    /**
     * Maximum number of enemies this spawner can spawn
     */
    private int count;

    /**
     * Total time in seconds this spawner has been alive
     */
    private float time;

    /**
     * List of locations enemies this spawner can spawn at
     */
    private Array<Vector2> spawnLocations;

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
    public void initialize(Settings settings) {
        this.addMin = settings.getSpawnRateMin();
        this.addMax = settings.getSpawnRateMax();
        this.count = settings.getEnemyCount();
        this.time = 0;

        float delay = settings.getDelay();
        enemyAddTime = delay;

        this.spawnLocations = settings.getSpawnLocations();
    }

    /**
     * Add enemy to level when applicable.
     */
    public void update(float delta) {
        if (count <= 0) return;
        time += delta;
        if (time >= enemyAddTime) {
            Vector2 position = spawnLocations.random();
            Enemy newEnemy = container.addEnemy("villager", position.x, position.y);
            // This spawner is only used in battle phase
            container.getEnemyControllers().get(newEnemy).setInBattle(true);
            container.getEnemyControllers().get(newEnemy).getStateMachine().changeState(EnemyState.ALERT);
            enemyAddTime = MathUtils.random(addMin, addMax);
            time = 0;
            count -= 1;
        }
    }
}
