package infinityx.lunarhaze.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class EnemyList implements Iterable<Enemy> {
    /**
     * The list of enemies managed by this object.
     */
    private Array<Enemy> enemies;

    private float time;

    /**
     * Create a new EnemyList
     */
    public EnemyList() {
        enemies = new Array<>(false, 5);
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

    /**
     * @param enemy Enemy to append to enemy list
     * @return id of the added enemy
     */
    public int addEnemy(Enemy enemy) {
        System.out.println("EnemyList addEnemy");
        enemies.add(enemy);
        return enemies.size - 1;
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
}
