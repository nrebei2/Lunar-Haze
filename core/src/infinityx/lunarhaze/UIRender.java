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

    private final float GAP_DIST = 20f;

    private final static float COUNTER_WIDTH = 300f;

    private final static float COUNTER_HEIGHT = 70.0f;

    protected BitmapFont UIFont_large;

    protected BitmapFont UIFont_small;

    private Texture circle_bar;

    private Texture counter;

    private Texture dusk_icon;

    private Texture health_icon;

    private Texture moon_icon;

    private Texture rec_bar;

    private Texture stealth_icon;

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
        canvas.draw(counter, Color.WHITE, canvas.getWidth() / 2 - COUNTER_WIDTH / 2, canvas.getHeight() - COUNTER_HEIGHT, COUNTER_WIDTH, COUNTER_HEIGHT);
        canvas.drawText("Level", UIFont_small, canvas.getWidth() / 2 - 13, canvas.getHeight() - COUNTER_HEIGHT / 4);
        canvas.drawText("1", UIFont_large, canvas.getWidth() / 2, canvas.getHeight() - COUNTER_HEIGHT / 2);
        canvas.drawText(String.valueOf(Math.max((int) gameplayController.getRemainingTime(), 0)), UIFont_large, canvas.getWidth() / 2 - COUNTER_WIDTH / 3, canvas.getHeight() - COUNTER_HEIGHT / 2.5f);
        canvas.draw(dusk_icon, canvas.getWidth() / 2 + COUNTER_WIDTH / 2 - dusk_icon.getWidth() * 1.5f, canvas.getHeight() - COUNTER_HEIGHT * 2 / 3);

        canvas.draw(moon_icon, Color.WHITE, moon_icon.getWidth() / 2, moon_icon.getHeight() / 2, canvas.getWidth() - BAR_WIDTH - moon_icon.getWidth() * 1.4f, canvas.getHeight() - BAR_HEIGHT / 2 - GAP_DIST, 0, 0.7f, 0.7f);
        canvas.draw(health_icon, Color.WHITE, moon_icon.getWidth() / 2, moon_icon.getHeight() / 2, canvas.getWidth() - BAR_WIDTH - health_icon.getWidth() * 1.2f, canvas.getHeight() - 4.9f * BAR_HEIGHT / 2 - GAP_DIST, 0, 0.7f, 0.7f);
        canvas.draw(stealth_icon, Color.WHITE, moon_icon.getWidth() / 2, moon_icon.getHeight() / 2, canvas.getWidth() - BAR_WIDTH - stealth_icon.getWidth() * 1.2f, canvas.getHeight() - 7.4f * BAR_HEIGHT / 2 - GAP_DIST, 0, 0.7f, 0.7f);
        canvas.end();
    }

}
