package infinityx.lunarhaze.entity;

import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.GameCanvas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class EnemyList implements Iterable<Enemy> {
    /**
     * The list of enemies managed by this object.
     */
    private Enemy[] enemies;

    private float time;
    private EnemyIterator iterator = new EnemyIterator();


    /**
     * Create a new EnemyList with the given number of ships.
     *
     * @param size The number of ships to allocate
     */
    public EnemyList(int size) {
        enemies = new Enemy[size];
        for (int ii = 0; ii < size; ii++) {
            enemies[ii] = new Enemy(ii, 0,0, new ArrayList<Vector2>());
        }
    }


    /**
     * Returns a ship iterator, satisfying the Iterable interface.
     * <p>
     * This method allows us to use this object in for-each loops.
     *
     * @return a ship iterator.
     */
    public Iterator<Enemy> iterator() {
        // Take a snapshot of the current state and return iterator.
        iterator.pos = 0;
        return iterator;
    }

    /**
     * Returns the number of enemies in this list
     *
     * @return the number of enemies in this list
     */
    public int size() {
        return enemies.length;
    }

    /**
     * Returns the ship for the given (unique) id
     * <p>
     * The value given must be between 0 and size-1.
     *
     * @return the ship for the given id
     */
    public Enemy get(int id) {
        return enemies[id];
    }

    /**
     * Returns the number of enemies alive at the end of an update.
     *
     * @return the number of enemies alive at the end of an update.
     */
//    public int numActive() {
//        int enemiesActive = 0;
//        for (Enemy e : this) {
//            if (e.isActive()) {
//                enemiesActive++;
//            }
//        }
//        return enemiesActive;
//    }


    /**
     * Implementation of a custom iterator.
     * <p>
     * Iterators are notorious for making new objects all the time.  We make
     * a custom iterator to cut down on memory allocation.
     */
    private class EnemyIterator implements Iterator<Enemy> {

        public int pos = 0;

        public boolean hasNext() {
            return pos < enemies.length;
        }


        public Enemy next() {
            if (pos >= enemies.length) {
                throw new NoSuchElementException();
            }
            int idx = pos;
            do {
                pos++;
            } while (pos < enemies.length && !enemies[pos].isAlive());
            return enemies[idx];
        }

    }
}
