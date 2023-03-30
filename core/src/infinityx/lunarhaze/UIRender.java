package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.GameplayController.Phase;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.graphics.ShaderUniform;
import infinityx.lunarhaze.graphics.ScreenFlash;

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
     * Distance between centers pf two hearts drawn on the health stroke
     */
    private final static float HEART_SEP = HEALTH_STROKE_WIDTH / 8;

    /**
     * Health stroke (located at top left corner) height
     */
    private final static float HEALTH_STROKE_HEIGHT = 40f;

    /**
     * Moonlight stroke (located at bottom left corner) width
     */
    private final static float MOON_STROKE_WIDTH = 80f;

    /**
     * Moonlight stroke (located at bottom left corner) height
     */
    private final static float MOON_STROKE_HEIGHT = 35f;

    /**
     * Stealth stroke (located at bottom center) width
     */
    private final static float STEALTH_STROKE_WIDTH = 270f;

    /**
     * Stealth stroke (located at bottom center) height
     */
    private final static float STEALTH_STROKE_HEIGHT = 18f;

    /**
     * Display font for the level number
     */
    protected BitmapFont UIFont_large;

    /**
     * Display font for the counter and cycle indicator
     */
    protected BitmapFont UIFont_small;

    /**
     * Top stroke texture
     */
    private final Texture counter;

    /**
     * Texture to indicate dusk (stealth phase)
     */
    private final Texture dusk_icon;

    /**
     * Texture to indicate fullmoon (battle phase)
     */
    private final Texture fullmoon_icon;

    /**
     * Texture to indicate player health
     */
    private final Texture health_icon;

    /**
     * Texture to indicate moonlight collected
     */
    private final Texture moon_icon;

    /**
     * Texture to indicate stealth value
     */
    private final Texture stealth_icon;

    /**
     * Texture of background stroke for health stats
     */
    private final Texture health_stroke;

    /**
     * Texture of background stroke for moonlight stats
     */
    private final Texture moonlight_stroke;

    /**
     * Texture of background stroke for stealth stats
     */
    private final Texture stealth_stroke;

    /** shader program which draws the enemy notice meter */
    private final ShaderProgram meter;
    private final VertexAttribute[] meterAttributes;

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
        counter = directory.getEntry("counter", Texture.class);
        dusk_icon = directory.getEntry("dusk-icon", Texture.class);
        fullmoon_icon = directory.getEntry("fullmoon-icon", Texture.class);
        health_icon = directory.getEntry("health-icon", Texture.class);
        moon_icon = directory.getEntry("moon-icon", Texture.class);
        stealth_icon = directory.getEntry("stealth-icon", Texture.class);
        health_stroke = directory.getEntry("health-stroke", Texture.class);
        moonlight_stroke = directory.getEntry("moonlight-stroke", Texture.class);
        stealth_stroke = directory.getEntry("stealth-stroke", Texture.class);

        // shaders
        this.meter = directory.get("meter", ShaderProgram.class);
        this.meterAttributes = new VertexAttribute[]{
                new VertexAttribute(VertexAttributes.Usage.Position, 2, "i_offset"),
                new VertexAttribute(VertexAttributes.Usage.Position, 1, "i_amount")
        };
    }


    /**
     * Draws any needed UI on screen during gameplay
     *
     * @param canvas drawing canvas
     * @param level  container holding all models
     * @param phase  current phase of the game
     */
    public void drawUI(GameCanvas canvas, LevelContainer level, GameplayController.Phase phase, GameplayController gameplayController) {
        setFontColor(Color.WHITE);

        // Draw with view transform considered
        canvas.begin(GameCanvas.DrawPass.SHAPE, level.getView().x, level.getView().y);

        if(gameplayController.getCollectingMoonlight()) {
             canvas.drawCollectLightBar(BAR_WIDTH / 2, BAR_HEIGHT / 2,
                 gameplayController.getTimeOnMoonlightPercentage(), level.getPlayer());
        }

        if (phase == Phase.BATTLE) {
            for (Enemy enemy : level.getEnemies()) {
                canvas.drawEnemyHpBars(
                        BAR_WIDTH / 4.0f, BAR_HEIGHT / 4.0f, enemy
                );
            }
            if (level.getPlayer().drawCooldownBar()) {
                canvas.drawAttackCooldownBar(10f, 60f, 65f, level.getPlayer());
            }
        }
        canvas.end();

        // Draw with view transform not considered
        canvas.begin(GameCanvas.DrawPass.SPRITE);
        // Draw top stroke at the top center of screen
        drawLevelStats(canvas, phase, gameplayController);
        drawHealthStats(canvas, level);
        drawMoonlightStats(canvas, level);
        drawStealthStats(canvas, level);
        canvas.end();

        canvas.begin(GameCanvas.DrawPass.SHAPE);
        // If necessary draw screen flash
        ScreenFlash.update(Gdx.graphics.getDeltaTime());
        canvas.drawScreenFlash(level.getPlayer());
        canvas.end();

        canvas.begin(GameCanvas.DrawPass.SHADER, level.getView().x, level.getView().y);
        drawStealthIndicator(canvas, level);
        canvas.end();
    }

    public void setFontColor(Color color){
        UIFont_large.setColor(color);
        UIFont_small.setColor(color);
    }

    /** Draw the level stroke and level status of the player */
    public void drawLevelStats(GameCanvas canvas, GameplayController.Phase phase, GameplayController gameplayController){
        canvas.draw(counter, Color.WHITE, canvas.getWidth() / 2 - COUNTER_WIDTH / 2, canvas.getHeight() - COUNTER_HEIGHT - TOP_MARGIN/2, COUNTER_WIDTH, COUNTER_HEIGHT);
        String text;
        Texture icon;
        if (phase == Phase.STEALTH){
            text = "night";
            icon = dusk_icon;
        } else {
            text = "full moon";
            icon = fullmoon_icon;
        }
        canvas.drawText(text, UIFont_small, canvas.getWidth() / 2 - UIFont_small.getCapHeight() * text.length()/3, canvas.getHeight() - TOP_MARGIN/2.5f);
        canvas.drawText("1", UIFont_large, canvas.getWidth() / 2, canvas.getHeight() - TOP_MARGIN);
        int remaining_sec = Math.max((int) gameplayController.getRemainingTime(), 0);
        int min = remaining_sec / 60;
        int sec = remaining_sec % 60;
        canvas.drawText(min + ":" + sec + "s", UIFont_small, canvas.getWidth() / 2 - COUNTER_WIDTH / 4, canvas.getHeight() - COUNTER_HEIGHT / 2.0f - TOP_MARGIN/3);
        if (icon == dusk_icon) {
            canvas.draw(icon, Color.WHITE, 0, 0, canvas.getWidth() / 2 + COUNTER_WIDTH / 4, canvas.getHeight() - COUNTER_HEIGHT / 2.0f - dusk_icon.getHeight() / 2 - TOP_MARGIN / 3, 0, 0.6f, 0.6f);
        } else {
            canvas.draw(icon, Color.WHITE, 0, 0, canvas.getWidth() / 2 + COUNTER_WIDTH / 4, canvas.getHeight() - COUNTER_HEIGHT / 2.0f - dusk_icon.getHeight() / 2 - TOP_MARGIN / 3, 0, 0.3f, 0.3f);
        }
    }

    /** Draw the health stroke and health status of the player */
    public void drawHealthStats(GameCanvas canvas, LevelContainer level){
        canvas.draw(health_stroke, Color.WHITE, 0, canvas.getHeight() - HEALTH_STROKE_HEIGHT * 2, HEALTH_STROKE_WIDTH, HEALTH_STROKE_HEIGHT);
        for(int i = 1; i <= Werewolf.INITIAL_HP; i++){
            if (level.getPlayer().getHp() >= i){
                // Draw a filled heart for the ith heart
                Color full = new Color(138f / 255.0f, 25f / 255.0f, 45f / 255.0f, 1f);
                canvas.draw(health_icon, full, health_icon.getWidth() / 2, health_icon.getHeight() / 2, HEALTH_STROKE_WIDTH/8 + HEART_SEP * i, canvas.getHeight() - HEALTH_STROKE_HEIGHT * 1.6f, 0, 0.6f, 0.6f);
            } else {
                // Draw an empty heart for the ith heart
                Color empty = new Color(41f / 255.0f, 41f / 255.0f, 41f / 255.0f, 0.8f);
                canvas.draw(health_icon, empty, health_icon.getWidth() / 2, health_icon.getHeight() / 2, HEALTH_STROKE_WIDTH/8 + HEART_SEP * i, canvas.getHeight() - HEALTH_STROKE_HEIGHT * 1.6f, 0, 0.6f, 0.6f);
            }
        }
    }

    /** Draw the moonlight stroke and moonlight status */
    public void drawMoonlightStats(GameCanvas canvas, LevelContainer level){
        canvas.draw(moonlight_stroke, Color.WHITE, MOON_STROKE_WIDTH/3, MOON_STROKE_HEIGHT, MOON_STROKE_WIDTH, MOON_STROKE_HEIGHT);
        canvas.draw(moon_icon, Color.WHITE, moon_icon.getWidth() / 2, moon_icon.getHeight() / 2, MOON_STROKE_WIDTH / 2 + moon_icon.getWidth()/4, MOON_STROKE_HEIGHT + moon_icon.getHeight()*2/3, 0, 0.5f, 0.5f);
        canvas.drawText(level.getPlayer().getMoonlightCollected() + "/" + ((int) level.getTotalMoonlight()), UIFont_small, MOON_STROKE_WIDTH * 4/5, MOON_STROKE_HEIGHT * 2 - UIFont_small.getCapHeight());
    }

    /** Draw the stealth stroke and stealth status of the player */
    public void drawStealthStats(GameCanvas canvas, LevelContainer level){
        canvas.draw(stealth_stroke, Color.WHITE, canvas.getWidth()/2 - STEALTH_STROKE_WIDTH/2, MOON_STROKE_HEIGHT, STEALTH_STROKE_WIDTH, STEALTH_STROKE_HEIGHT);
        float proportion = level.getPlayer().getStealth();
        canvas.draw(stealth_icon, Color.WHITE, stealth_icon.getWidth() / 2, stealth_icon.getHeight() / 2, canvas.getWidth()/2 - STEALTH_STROKE_WIDTH/2 + stealth_icon.getWidth(), MOON_STROKE_HEIGHT + stealth_icon.getHeight()*3/5, (float) (13f/180f * Math.PI), 0.7f, 0.7f);
        Color stealth_fill = new Color(255f / 255.0f, 255f / 255.0f, 255f / 255.0f, 1f);
        canvas.draw(stealth_stroke, stealth_fill, canvas.getWidth()/2 - STEALTH_STROKE_WIDTH/2, MOON_STROKE_HEIGHT, STEALTH_STROKE_WIDTH * proportion, STEALTH_STROKE_HEIGHT);
    }

    /** Draw the stealth indicator above enemies */
    public void drawStealthIndicator(GameCanvas canvas, LevelContainer level) {
        ShaderUniform uniform = new ShaderUniform("u_amount");
        Array<Enemy> enemies = level.getEnemies();
        for (Enemy enemy : enemies) {
            uniform.setValues(0.1f);
            canvas.drawShader(
                    meter,
                    canvas.WorldToScreenX(enemy.getPosition().x) - 38,
                    canvas.WorldToScreenY(enemy.getPosition().y) + enemy.getTextureHeight() - 25,
                    50, 50,
                    uniform);
        }
    }

}
