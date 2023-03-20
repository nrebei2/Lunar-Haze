package infinityx.lunarhaze.entity;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import java.util.Iterator;

public class EnemyPool extends Pool<Enemy> {
    /**
     * The pre-allocated arraylist of enemies. All instantiated enemies must be from this list.
     */
    private Array<Enemy> enemies;

    /** The next object in the array available for allocation */
    private int next;

    /**
     * Creates a EnemyList with the given capacity.
     *
     * @param capacity  The number of particles to preallocate
     */
    public EnemyPool(int capacity) {
        super(capacity);
        assert capacity > 0;

        // Preallocate objects
        enemies = new Array<>();
        for(int ii = 0; ii < capacity; ii++) {
            Enemy e = new Enemy();
            enemies.add(e);
        }

        next = 0;
    }

    /**
     * Allocates a new object from the Pool.
     *
     * INVARIANT: If this method is called, then the free list is empty.
     *
     * This is the lone method that you must implement to create a memory
     * pool.  This is where you "seed" the memory pool by allocating objects
     * when the free list is empty.  We preallocated, so we just return
     * the next object from the list.  But you could put a "new" in here if
     * you wanted to; in that case the free list is how you manage everything.
     *
     * @return A new particle object
     */
    protected Enemy newObject() {
        // Add if we are outside the array
        if (next == enemies.size) {
            return null;
        }
        Enemy enemy = new Enemy();
        next++;
        return enemies.get(next-1);
    }

    /**
     * Returns a ship iterator, satisfying the Iterable interface.
     * <p>
     * This method allows us to use this object in for-each loops.
     *
     * @return a ship iterator.
     */
    public Iterator<Enemy> iterator() {
        return enemies.iterator();
    }

    /**
     * Returns the number of enemies in this list
     *
     * @return the number of enemies in this list
     */
    public int size() {
        return enemies.size;
    }

    /**
     * Returns the ship for the given (unique) id
     * <p>
     * The value given must be between 0 and size-1.
     *
     * @return the ship for the given id
     */
    public Enemy get(int id) {
        return enemies.get(id);
    }

}
