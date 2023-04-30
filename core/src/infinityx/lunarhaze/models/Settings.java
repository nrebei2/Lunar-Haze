package infinityx.lunarhaze.models;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import imgui.type.ImFloat;
import imgui.type.ImInt;

/**
 * Holds the battle phase enemy settings.
 */
public class Settings {
    // public and imgui for use in level editor
    public ImInt phaseLength = new ImInt(0);
    public ImInt enemyCount = new ImInt(0);
    public ImFloat spawnRateMin = new ImFloat(0);
    public ImFloat spawnRateMax = new ImFloat(0);
    public ImInt delay = new ImInt(0);
    public ImInt transition = new ImInt(4);

    /** List of locations enemies can spawn during battle phase */
    private Array<Vector2> spawnLocations = new Array<>();

    /**
     * Returns the phase length in seconds for the stealth phase.
     *
     * @return The phase length.
     */
    public int getPhaseLength() {
        return phaseLength.get();
    }

    /**
     * Returns the total number of enemies that will spawn.
     *
     * @return The enemy count.
     */
    public int getEnemyCount() {
        return enemyCount.get();
    }

    /**
     * Returns the minimum spawn rate in seconds.
     *
     * @return The minimum spawn rate.
     */
    public float getSpawnRateMin() {
        return spawnRateMin.get();
    }

    /**
     * Returns the maximum spawn rate in seconds.
     *
     * @return The maximum spawn rate.
     */
    public float getSpawnRateMax() {
        return spawnRateMax.get();
    }

    /**
     * Returns the delay in seconds after the battle mode begins to spawn the enemies.
     *
     * @return The delay.
     */
    public float getDelay() {
        return delay.get();
    }

    /**
     * Sets the total number of enemies that will spawn.
     *
     * @param enemyCount The enemy count to set.
     */
    public void setEnemyCount(int enemyCount) {
        this.enemyCount.set(enemyCount);
    }

    /**
     * Sets the minimum spawn rate in seconds.
     *
     * @param spawnRateMin The minimum spawn rate to set.
     */
    public void setSpawnRateMin(float spawnRateMin) {
        this.spawnRateMin.set(spawnRateMin);
    }

    /**
     * Sets the maximum spawn rate in seconds.
     *
     * @param spawnRateMax The maximum spawn rate to set.
     */
    public void setSpawnRateMax(float spawnRateMax) {
        this.spawnRateMax.set(spawnRateMax);
    }

    /**
     * Sets the phase length in seconds for the stealth phase.
     * @param phaseLength the length to set
     */
    public void setPhaseLength(int phaseLength) {
        this.phaseLength.set(phaseLength);
    }

    /**
     * Sets the delay in seconds after the battle mode begins to spawn the enemies.
     */
    public void setDelay(int delay) {
        this.delay.set(delay);
    }

    public Array<Vector2> getSpawnLocations() {
        return spawnLocations;
    }

    /**
     * Adds a new spawn location to the list of spawn locations with the given position.
     *
     * @param x The x-coordinate of the spawn location.
     * @param y The y-coordinate of the spawn location.
     */
    public void addSpawnLocation(float x, float y) {
        spawnLocations.add(new Vector2(x, y));
    }

    /**
     * Removes the spawn location at the specified index.
     *
     * @param index The index of the spawn location to remove.
     */
    public void removeSpawnLocation(int index) {
        if (index >= 0 && index < spawnLocations.size) {
            spawnLocations.removeIndex(index);
        }
    }

    /** @return The transition in seconds between stealth and battle ambient lighting */
    public int getTransition() {
        return transition.get();
    }

    /** Sets the number of seconds between transitioning from stealth and battle ambient lighting */
    public void setTransition(int transition) {
        this.transition.set(transition);
    }
}
