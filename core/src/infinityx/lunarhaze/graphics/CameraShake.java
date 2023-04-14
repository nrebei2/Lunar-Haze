package infinityx.lunarhaze.graphics;

import com.badlogic.gdx.math.Vector2;

/**
 * Static utility class for shaking the camera.
 * When updated, generates a random offset to move the camera by.
 * The offset is then applied to the camera in the LevelContainer.
 * The 'shake()' method is called when a player attack makes
 * contact with an enemy.
 */
public class CameraShake {

    private static Vector2 shakeOffset;
    private static float shakeIntensity;
    private static float shakeDuration;
    private static float shakeElapsedTime;

    public static void shake(float intensity, float duration) {
        shakeIntensity = intensity;
        shakeDuration = duration;
        shakeElapsedTime = 0;
        shakeOffset = new Vector2();
    }

    public static void update(float delta) {
        if (shakeElapsedTime < shakeDuration) {
            float currentIntensity = shakeIntensity * (1f - (shakeElapsedTime / shakeDuration));
            float shakeX = (float) (Math.random() * 2 - 1) * currentIntensity;
            float shakeY = (float) (Math.random() * 2 - 1) * currentIntensity;

            shakeOffset.set(shakeX, shakeY);
            shakeElapsedTime += delta;
        } else {
            shakeDuration = 0;
        }
    }

    public static float timeLeft() {
        return shakeDuration - shakeElapsedTime;
    }

    public static Vector2 getShakeOffset() {
        return shakeOffset;
    }

}