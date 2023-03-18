package infinityx.lunarhaze;

import com.badlogic.gdx.utils.Pool;

public class DustPool extends Pool<Dust> {
    /** Array holding all possible dust particles */
    private Dust[] memory;

    /** The next object in the array available for allocation */
    private int next;

    /**
     * Creates a ParticlePool with the given capacity.
     * Given the capacity, there can only be that amount of dust on the screen at once.
     *
     * @param capacity The number of particles to preallocate
     */
    public DustPool(int capacity) {
        super(capacity);

        // Preallocate objects
        memory = new Dust[capacity];
        for(int ii = 0; ii < capacity; ii++) {
            memory[ii] = new Dust();
        }

        next = 0;
    }

    /**
     * Allocates a new object from the Pool.
     *
     * This is only called when the free pool is empty.
     */
    public Dust newObject() {
        // Fail if we are outside the array
        if (next == memory.length) {
            return null;
        }

        next = (next+1) % memory.length;
        return memory[next];
    }

}
