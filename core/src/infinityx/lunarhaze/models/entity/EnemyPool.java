package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import infinityx.lunarhaze.controllers.EnemyController;

import java.lang.reflect.InvocationTargetException;


/**
 * Pre-allocated pool of enemy objects.
 * Unfortunately, since enemies and their controller are tightly coupled (one-to-one),
 * it also handles their controllers too.
 * <p>
 * Note there is no limit on the number of enemies you can obtain, but dead enemies should be freed
 * for later reuse.
 */
public class EnemyPool<T extends Enemy> extends Pool<T> {
    /**
     * The pre-allocated arraylist of enemies. All instantiated enemies must be from this list.
     */
    private final Array<T> enemies;

    /**
     * controls[e] is the controller for enemy e. The domain of this map is enemies.
     */
    public ObjectMap<T, EnemyController> controls;

    /**
     * The next object in the array available for allocation
     */
    private int next;

    /**
     * Creates a EnemyList with the given capacity.
     *
     * @param capacity The number of particles to preallocate
     */
    public EnemyPool(int capacity, Class<T> enemyType) {
        super(capacity);
        assert capacity > 0;
        controls = new ObjectMap<>(capacity);
        // Preallocate objects
        enemies = new Array<>();
        for (int ii = 0; ii < capacity; ii++) {
            try {
                T e = enemyType.getDeclaredConstructor().newInstance();
                enemies.add(e);
                controls.put(e, new EnemyController(e));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                throw new RuntimeException("Failed to create enemy instance of type " + enemyType.getSimpleName(), ex);
            }
        }
        next = 0;
    }

    /**
     * Allocates a new empty enemy from the Pool. This function will only be called if the free list is empty.
     *
     * @return A new enemy
     */
    protected T newObject() {
        // Add if we are outside the array
        // This will only happen if the number of enemies on the screen is higher than capacity set during init
        if (next == enemies.size) {
            try {
                T enemy = (T) enemies.first().getClass().getDeclaredConstructor().newInstance();
                enemies.add(enemy);
                controls.put(enemy, new EnemyController(enemy));
                return enemy;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                throw new RuntimeException("Failed to create enemy instance of type " + enemies.first().getClass().getSimpleName(), ex);
            }
        }
        // Otherwise, take from main list of enemies
        next++;
        return enemies.get(next - 1);
    }
}
