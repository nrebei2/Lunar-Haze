package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.GameplayController.Phase;
import infinityx.lunarhaze.entity.Enemy;

/**
 * This is a class used for drawing player and enemies' game UI state: HP, Stealth, MoonLight
 * Collection
 */
public class UIRender {
    /**
     * Need an ongoing reference to the asset directory
     */
    protected AssetDirectory directory;
    /**
     * Width of the HP bar
     */
    private final static float BAR_WIDTH = 300f;

    /**
     * Height of the HP bar
     */
    private final static float BAR_HEIGHT = 20.0f;

    /**
     * Distance of top stroke to the top of screen
     */
    private final static float TOP_MARGIN = 30.f;

    /**
     * Distance between bars
     */
    private final float GAP_DIST = 20f;

    /**
     * Top stroke width
     */
    private final static float COUNTER_WIDTH = 300f;

    /**
     * Top stroke height
     */
    private final static float COUNTER_HEIGHT = 50.0f;

    /**
     * Health stroke (located at top left corner) width
     */
    private final static float HEALTH_STROKE_WIDTH = 200f;

    /**
     * Health stroke (located at top left corner) height
     */
    private final static float HEALTH_STROKE_HEIGHT = 30f;

    /**
     * Moonlight stroke (located at bottom left corner) width
     */
    private final static float MOON_STROKE_WIDTH = 100f;

    /**
     * Moonlight stroke (located at bottom left corner) height
     */
    private final static float MOON_STROKE_HEIGHT = 30f;

    /**
     * Stealth stroke (located at bottom center) width
     */
    private final static float STEALTH_STROKE_WIDTH = 280f;

    /**
     * Stealth stroke (located at bottom center) height
     */
    private final static float STEALTH_STROKE_HEIGHT = 20f;

    /**
     * Display font for the level number
     */
    protected BitmapFont UIFont_large;

    /**
     * Display font for the counter and cycle indicator
     */
    protected BitmapFont UIFont_small;

    private Texture circle_bar;

    /**
     * Top stroke texture
     */
    private Texture counter;

    private Texture dusk_icon;

    private Texture health_icon;

    private Texture moon_icon;

    private Texture rec_bar;

    private Texture stealth_icon;

    private Texture health_stroke;

    private Texture moonlight_stroke;

    private Texture stealth_stroke;

    /**
     * Create a new UIRender with font and directory assigned.
     *
     * @param font1
     * @param font2
     * @param directory
     */
    public UIRender(BitmapFont font1, BitmapFont font2, AssetDirectory directory) {
        this.UIFont_large = font1;
        this.UIFont_small = font2;
        this.directory = directory;

        // Load the assets for UI interface immediately.
        circle_bar = directory.getEntry("circle-bar", Texture.class);
        counter = directory.getEntry("counter", Texture.class);
        dusk_icon = directory.getEntry("dusk-icon", Texture.class);
        health_icon = directory.getEntry("health-icon", Texture.class);
        moon_icon = directory.getEntry("moon-icon", Texture.class);
        rec_bar = directory.getEntry("rec-bar", Texture.class);
        stealth_icon = directory.getEntry("stealth-icon", Texture.class);
        health_stroke = directory.getEntry("health-stroke", Texture.class);
        moonlight_stroke = directory.getEntry("moonlight-stroke", Texture.class);
        stealth_stroke = directory.getEntry("stealth-stroke", Texture.class);
    }


    /**
     * Draws any needed UI on screen during gameplay
     *
     * @param canvas drawing canvas
     * @param level  container holding all models
     * @param phase  current phase of the game
     */
    public void drawUI(GameCanvas canvas, LevelContainer level, GameplayController.Phase phase, GameplayController gameplayController) {
        UIFont_large.setColor(Color.WHITE);
        UIFont_small.setColor(Color.WHITE);
        canvas.begin(); // DO NOT SCALE
        canvas.drawLightBar(moon_icon, BAR_WIDTH,
                BAR_HEIGHT, level.getPlayer().getLight());
        canvas.drawHpBar(health_icon, BAR_WIDTH,
                BAR_HEIGHT, level.getPlayer().getHp());
        canvas.drawStealthBar(stealth_icon, BAR_WIDTH,
                BAR_HEIGHT, level.getPlayer().getStealth());

        if (phase == Phase.BATTLE) {
            for (Enemy enemy : level.getEnemies()) {
                canvas.drawEnemyHpBars(
                        BAR_WIDTH / 4.0f, BAR_HEIGHT / 4.0f, enemy
                );
            }
        }
        canvas.end();
        canvas.begin();
        // Draw top stroke at the top center of screen
        canvas.draw(counter, Color.WHITE, canvas.getWidth() / 2 - COUNTER_WIDTH / 2, canvas.getHeight() - COUNTER_HEIGHT - TOP_MARGIN/2, COUNTER_WIDTH, COUNTER_HEIGHT);
        canvas.drawText("night", UIFont_small, canvas.getWidth() / 2 - UIFont_small.getCapHeight(), canvas.getHeight() - TOP_MARGIN/2);
        canvas.drawText("1", UIFont_large, canvas.getWidth() / 2, canvas.getHeight() - TOP_MARGIN);
        int remaining_sec = Math.max((int) gameplayController.getRemainingTime(), 0);
        int min = remaining_sec / 60;
        int sec = remaining_sec % 60;
        canvas.drawText(min + ":" + sec + "s", UIFont_small, canvas.getWidth() / 2 - COUNTER_WIDTH / 4, canvas.getHeight() - COUNTER_HEIGHT / 2.0f - TOP_MARGIN/3);
        canvas.draw(dusk_icon, Color.WHITE, 0, 0, canvas.getWidth() / 2 + COUNTER_WIDTH / 4, canvas.getHeight() - COUNTER_HEIGHT / 2.0f - dusk_icon.getHeight()/2 - TOP_MARGIN/3, 0, 0.6f, 0.6f);

        canvas.draw(health_stroke, Color.WHITE, 0, canvas.getHeight() - HEALTH_STROKE_HEIGHT * 2, HEALTH_STROKE_WIDTH, HEALTH_STROKE_HEIGHT);
        canvas.draw(moonlight_stroke, Color.WHITE, MOON_STROKE_WIDTH/3, MOON_STROKE_HEIGHT, MOON_STROKE_WIDTH, MOON_STROKE_HEIGHT);
        canvas.draw(stealth_stroke, Color.WHITE, canvas.getWidth()/2 - STEALTH_STROKE_WIDTH/2, MOON_STROKE_HEIGHT, STEALTH_STROKE_WIDTH, STEALTH_STROKE_HEIGHT);

        canvas.draw(moon_icon, Color.WHITE, moon_icon.getWidth() / 2, moon_icon.getHeight() / 2, canvas.getWidth() - BAR_WIDTH - moon_icon.getWidth() * 1.4f, canvas.getHeight() - BAR_HEIGHT / 2 - GAP_DIST, 0, 0.7f, 0.7f);
        canvas.draw(health_icon, Color.WHITE, moon_icon.getWidth() / 2, moon_icon.getHeight() / 2, canvas.getWidth() - BAR_WIDTH - health_icon.getWidth() * 1.2f, canvas.getHeight() - 4.9f * BAR_HEIGHT / 2 - GAP_DIST, 0, 0.7f, 0.7f);
        canvas.draw(stealth_icon, Color.WHITE, moon_icon.getWidth() / 2, moon_icon.getHeight() / 2, canvas.getWidth() - BAR_WIDTH - stealth_icon.getWidth() * 1.2f, canvas.getHeight() - 7.4f * BAR_HEIGHT / 2 - GAP_DIST, 0, 0.7f, 0.7f);
        canvas.end();
    }

}
