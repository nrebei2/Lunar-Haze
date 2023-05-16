package infinityx.util;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

/**
 * This class represents an ordered map where keys are integers and values are arrays of Drawables.
 */
public class DrawableContainer {
    /**
     * An array of integer keys for the map.
     */
    private IntArray keys;

    /**
     * An array of Drawable arrays, serving as values for the map.
     */
    private Array<Array<Drawable>> drawables;

    public DrawableContainer() {
        keys = new IntArray();
        drawables = new Array<>();
    }

    /**
     * Retrieves the array of Drawables for a given key.
     *
     * @param key the key to search for
     * @return the array of Drawables associated with the key, or null if the key is not in the map
     */
    public Array<Drawable> get(int key) {
        int index = keys.indexOf(key);
        if (index == -1) {
            return null;
        }
        return drawables.get(index);
    }

    /**
     * Inserts a new key into the map that maps to an empty array. Assumes the key does not exist in the map.
     */
    public void add(int key) {
        keys.add(key);
        Array<Drawable> newArray = new Array<>();
        drawables.add(newArray);
    }

    /**
     * Inserts a new Drawable into the map.
     * If the Drawable's ID is already in the map, adds the Drawable to the associated array.
     * If the Drawable's ID is not in the map, creates a new array for it and adds it to the map.
     *
     * @param drawable the Drawable to insert
     * @return Whether a new array was not needed to be allocated
     */
    public boolean put(Drawable drawable) {
        int id = drawable.getID();
        int index = keys.indexOf(id);
        if (index == -1) {
            keys.add(id);
            Array<Drawable> newArray = new Array<>();
            newArray.add(drawable);
            drawables.add(newArray);
            return false;
        } else {
            drawables.get(index).add(drawable);
            return true;
        }
    }

    /**
     * @return The backing structure for this map
     */
    public Array<Array<Drawable>> getBacking() {
        return drawables;
    }
}
