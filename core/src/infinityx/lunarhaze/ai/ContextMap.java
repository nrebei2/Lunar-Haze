package infinityx.lunarhaze.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import infinityx.util.AngleUtils;

import java.util.Arrays;

/**
 * {@code ContextMap} is the output of a {@link ContextBehavior}.
 * It is made up of two components, the {@link #dangerMap} and {@link #interestMap}.
 */
public class ContextMap {
    /**
     * The number of elements in {@link #dangerMap} and {@link #interestMap}, i.e. the number of
     * headings the context behavior will consider.
     */
    private int resolution;

    /**
     * Context map holding the intensities for each heading the
     * behavior would like to stay away from
     */
    public float[] dangerMap;

    /**
     * Context map holding the intensities for each heading the
     * behavior would like to move toward
     */
    public float[] interestMap;


    /**
     * Creates a default ContextMap.
     * The default resolution is 8, for each diagonal and cardinal direction.
     */
    public ContextMap() {
        this(8);
    }

    /**
     * Creates a ContextMap with the given resolution.
     *
     * @param resolution See {@link #resolution}
     */
    public ContextMap(int resolution) {
        dangerMap = new float[resolution];
        interestMap = new float[resolution];
        this.resolution = resolution;
    }

    private Vector2 dirCache = new Vector2();

    /**
     * Context map slot to heading.
     *
     * @param slot index into {@link #dangerMap} or {@link #interestMap}
     * @return unit vector pointing towards heading.
     */
    public Vector2 dirFromSlot(int slot) {
        float angle = MathUtils.PI2 * ((float)slot / resolution);
        System.out.println(angle);
        return AngleUtils.angleToVector(dirCache, angle);
    }

    /**
     * Zeros the danger and interest maps
     */
    public ContextMap setZero() {
        Arrays.fill(dangerMap, 0);
        Arrays.fill(interestMap, 0);
        return this;
    }

    public int getResolution() {
        return resolution;
    }
}
