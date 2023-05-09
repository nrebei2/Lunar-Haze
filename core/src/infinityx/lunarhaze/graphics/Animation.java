package infinityx.lunarhaze.graphics;

import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

/**
 * Animation class that manages a collection of named animation and frame durations.
 * <p>
 * This class provides the ability to set the current FilmStrip for animation by name and supports
 * different play modes for the animation. It is inspired by LibGdx Animation class API.
 */
public class Animation {
    /**
     * Enum representing different play modes for the animation.
     */
    public enum PlayMode {
        NORMAL,
        LOOP
    }

    /**
     * AnimationData class that holds a FilmStrip, frame durations, and precalculated total duration.
     * <p>
     * This inner class is designed to manage the animation data for each named animation in the
     * parent Animation class. If frameDurations is null, the animation is a static texture.
     */
    private static class AnimationData {
        /**
         * The FilmStrip instance for this animation.
         */
        FilmStrip filmStrip;
        /**
         * The array of frame durations for this animation. If null, the animation is a static texture.
         */
        float[] frameDurations;
        /**
         * The precalculated total duration of this animation.
         */
        float totalDuration;

        /**
         * Creates a new AnimationData instance with the given FilmStrip and frame durations.
         *
         * @param filmStrip      The FilmStrip instance for this animation.
         * @param frameDurations The array of frame durations for this animation. If null, the animation is a static texture.
         */
        AnimationData(FilmStrip filmStrip, float[] frameDurations) {
            this.filmStrip = filmStrip;
            this.frameDurations = frameDurations;
            this.totalDuration = 0;
            if (frameDurations != null) {
                for (float duration : frameDurations) {
                    totalDuration += duration;
                }
            }
        }
    }

    /**
     * Map holding named animations.
     */
    private Map<String, AnimationData> animations;
    /**
     * Name of the current active FilmStrip.
     */
    private AnimationData currentAnimation;
    /**
     * Current play mode of the animation.
     */
    private PlayMode playMode;
    /**
     * Elapsed time for the animation.
     */
    private float elapsedTime;

    /**
     * Creates a new Animation instance.
     */
    public Animation() {
        animations = new HashMap<>();
        elapsedTime = 0;
    }

    /**
     * Adds an animation to the collection with a single frame duration for all frames.
     *
     * @param name          The name of the FilmStrip.
     * @param filmStrip     The FilmStrip instance.
     * @param frameDuration The frame duration for all frames in the FilmStrip.
     */
    public void addAnimation(String name, FilmStrip filmStrip, float frameDuration) {
        float[] durations = new float[filmStrip.getSize()];
        for (int i = 0; i < durations.length; i++) {
            durations[i] = frameDuration;
        }
        animations.put(name, new AnimationData(filmStrip, durations));
    }

    /**
     * Adds an animation to the collection with an array of frame durations.
     *
     * @param name           The name of the FilmStrip.
     * @param filmStrip      The FilmStrip instance.
     * @param frameDurations The array of frame durations for the FilmStrip.
     */
    public void addAnimation(String name, FilmStrip filmStrip, float[] frameDurations) {
        if (filmStrip.getSize() != frameDurations.length) {
            throw new IllegalArgumentException("Frame durations length must match the filmstrip size.");
        }
        animations.put(name, new AnimationData(filmStrip, frameDurations));
    }

    /**
     * Adds a static animation with a single Texture.
     *
     * @param name    The name of the static animation.
     * @param texture The Texture instance for the static animation.
     */
    public void addStaticAnimation(String name, Texture texture) {
        FilmStrip staticStrip = new FilmStrip(texture, 1, 1);
        animations.put(name, new AnimationData(staticStrip, null));
    }

    /**
     * Sets the current FilmStrip for animation by name.
     *
     * @param name The name of the FilmStrip to set as active.
     */
    public void setCurrentAnimation(String name) {
        if (!animations.containsKey(name)) {
            throw new IllegalArgumentException("Animation not found: " + name);
        }
        currentAnimation = animations.get(name);
        elapsedTime = 0;
    }

    /**
     * Gets the frame durations array for the specified Animation.
     *
     * @param name The name of the Animation.
     * @return The frame durations array for the specified Animation.
     */
    public float[] getFrameDurations(String name) {
        if (!animations.containsKey(name)) {
            throw new IllegalArgumentException("Animation not found: " + name);
        }
        AnimationData animationData = animations.get(name);
        return animationData.frameDurations;
    }

    /**
     * Sets the frame durations array for the specified Animation.
     *
     * @param name           The name of the Animation.
     * @param frameDurations The frame durations array to set.
     */
    public void setFrameDurations(String name, float[] frameDurations) {
        if (!animations.containsKey(name)) {
            throw new IllegalArgumentException("Animation not found: " + name);
        }
        AnimationData animationData = animations.get(name);
        if (animationData.filmStrip.getSize() != frameDurations.length) {
            throw new IllegalArgumentException("Frame durations length must match the filmstrip size.");
        }
        animationData.frameDurations = frameDurations;
    }

    /**
     * Gets the current play mode of the animation.
     *
     * @return The current play mode of the animation.
     */
    public PlayMode getPlayMode() {
        return playMode;
    }

    /**
     * Sets the play mode of the animation.
     *
     * @param playMode The play mode to set.
     */
    public void setPlayMode(PlayMode playMode) {
        this.playMode = playMode;
    }

    /**
     * Returns the key frame of the current FilmStrip based on the elapsed time.
     *
     * @param deltaTime The time in seconds since the last frame.
     * @return The current key frame as a TextureRegion.
     */
    public FilmStrip getKeyFrame(float deltaTime) {
        elapsedTime += deltaTime;

        if (currentAnimation.frameDurations == null) {
            return currentAnimation.filmStrip;
        }

        int frameIndex;
        if (playMode == PlayMode.NORMAL) {
            frameIndex = getKeyFrameIndexNormal(elapsedTime, currentAnimation.frameDurations);
        } else {
            frameIndex = getKeyFrameIndexLoop(elapsedTime, currentAnimation.frameDurations);
        }

        currentAnimation.filmStrip.setFrame(frameIndex);
        return currentAnimation.filmStrip;
    }

    /**
     * Gets the key frame index for the NORMAL play mode.
     *
     * @param elapsedTime The elapsed time for the animation.
     * @param durations   The frame durations array for the current FilmStrip.
     * @return The index of the key frame.
     */
    private int getKeyFrameIndexNormal(float elapsedTime, float[] durations) {
        float time = 0;
        for (int i = 0; i < durations.length; i++) {
            time += durations[i];
            if (elapsedTime < time) {
                return i;
            }
        }
        return durations.length - 1;
    }

    /**
     * Gets the key frame index for the LOOP play mode.
     *
     * @param elapsedTime The elapsed time for the animation.
     * @param durations   The frame durations array for the current FilmStrip.
     * @return The index of the key frame.
     */
    private int getKeyFrameIndexLoop(float elapsedTime, float[] durations) {
        elapsedTime %= currentAnimation.totalDuration;
        float time = 0;
        for (int i = 0; i < durations.length; i++) {
            time += durations[i];
            if (elapsedTime < time) {
                return i;
            }
        }
        return 0;
    }

    public void clearFrames(){
        animations.clear();
    }
}