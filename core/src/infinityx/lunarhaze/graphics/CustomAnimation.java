package infinityx.lunarhaze.graphics;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * A custom Animation class that extends libGDX's Animation class to allow
 * for setting frame durations using a list of frame durations.
 */
public class CustomAnimation extends Animation {

    private float[] frameDurations;

    /**
     * Creates a new CustomAnimation with the specified frame duration and key frames.
     * @param frameDuration The duration of each frame in seconds.
     * @param keyFrames An array of key frames.
     */
    public CustomAnimation(float frameDuration, TextureRegion... keyFrames) {
        super(frameDuration, keyFrames);
    }

    /**
     * Creates a new CustomAnimation with the specified frame duration and key frames.
     * @param frameDuration The duration of each frame in seconds.
     * @param keyFrames An array of key frames.
     */
    public CustomAnimation(float frameDuration, Array<? extends TextureRegion> keyFrames) {
        super(frameDuration, keyFrames);
    }

    /**
     * Creates a new CustomAnimation with the specified key frames and frame durations.
     * @param keyFrames An array of key frames.
     * @param frameDurations An array of frame durations.
     */
    public CustomAnimation(Array<? extends TextureRegion> keyFrames, float[] frameDurations) {
        super(0f, keyFrames);
        setFrameDurations(frameDurations);
    }

    /**
     * Sets the frame durations for this animation.
     * @param frameDurations An array of frame durations.
     * @throws IllegalArgumentException if the frame durations array is null or has a different length than the key frames array.
     */
    public void setFrameDurations(float[] frameDurations) throws IllegalArgumentException {
        if (frameDurations == null) {
            throw new IllegalArgumentException("Frame durations cannot be null.");
        }
        if (frameDurations.length != getKeyFrames().length) {
            throw new IllegalArgumentException("Frame durations array must have the same length as the key frames array.");
        }
        for (int i = 0; i < frameDurations.length; i++) {
            setFrameDuration(i, frameDurations[i]);
        }
    }

    private void setFrameDuration(int i, float frameDuration) {
        this.frameDurations[i] = frameDuration;
    }
}
