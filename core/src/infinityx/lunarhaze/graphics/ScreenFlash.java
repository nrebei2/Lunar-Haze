package infinityx.lunarhaze.graphics;

import com.badlogic.gdx.graphics.Color;

public class ScreenFlash {

    private static Color flashColor = new Color();
    private static float flashDuration;
    private static float flashElapsedTime;
    private static float fadeIn;
    private static float fadeOut;
    private static float maxAlpha;

    public static void flash(Color color, float duration, float fadeInTime, float fadeOutTime, float maxAlpha) {
        flashColor = new Color(color);
        flashDuration = duration;
        flashElapsedTime = 0;
        fadeIn = fadeInTime;
        fadeOut = fadeOutTime;
        ScreenFlash.maxAlpha = maxAlpha;
    }

    public static void update(float delta) {
        if (flashElapsedTime < flashDuration) {
            flashElapsedTime += delta;

            if (flashElapsedTime <= fadeIn) {
                flashColor.a = Math.min((flashElapsedTime / fadeIn), maxAlpha);
            } else if (flashElapsedTime >= flashDuration - fadeOut) {
                flashColor.a = Math.min(((flashDuration - flashElapsedTime) / fadeOut), maxAlpha);
            } else {
                flashColor.a = maxAlpha;
            }
        } else {
            flashDuration = 0;
            flashColor.a = 0;
        }
    }

    public static float timeLeft() {
        return flashDuration - flashElapsedTime;
    }

    public static Color getFlashColor() {
        return flashColor;
    }
}