package infinityx.lunarhaze.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.controllers.GameplayController;
import infinityx.lunarhaze.controllers.GameplayController.GameState;
import infinityx.lunarhaze.controllers.GameplayController.Phase;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Enemy;
import infinityx.lunarhaze.models.entity.Werewolf;

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
     * Height of moon center as a percentage of transition screen before rise
     */
    private final static float MOON_CENTER_LOW = 1 / 7f;

    /**
     * Height of moon center as a percentage of transition screen after rise
     */
    private final static float MOON_CENTER_HIGH = 3 / 5f;

    /**
     * Width and height of moon as a ratio of canvas height
     */
    private final static float MOON_SIZE_RATIO = 350 / 982f;

    /**
     * Center X of the moon as a ratio of canvas width
     */
    private final static float MOON_CENTERX_RATIO = 11.8f / 16;

    private float moon_centerY_ratio = MOON_CENTER_LOW;

    /**
     * Number of updates in transition phase
     */
    private int num_updates = 0;

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

    /**
     * Texture to represent enemy noticed
     */
    private final Texture noticed;

    /**
     * Texture to represent transition phase background
     */
    private final Texture transition_background;

    /**
     * Texture to represent moon for transition phase
     */
    private final Texture moon;

    /**
     * Texture to represent trees for transition phase
     */
    private final Texture trees;

    /**
     * Whether heart indicator length has been changed
     */
    boolean changed = false;

    /**
     * Store HP value of last frame
     */
    int last_hp = Werewolf.INITIAL_HP;

    /**
     * Store moonlight collected of last frame
     */
    int last_moon = 0;

    /**
     * Texture of represent enemy alert
     */
    private final Texture alert;

    /**
     * shader program which draws the enemy indicator meter
     */
    private final ShaderProgram meter;

    private ShaderUniform meterUniform;


    /**
     * current time (in seconds) transition screen has been alive
     */
    private float elapsed;

    /**
     * time (in seconds) it should take this screen to fade-in and fade-out
     */
    private static final float FADE_TIME_PROP = 1 / 8f;

    /**
     * Easing in function, easing out is reversed
     */
    private static final Interpolation EAS_FN = Interpolation.exp5Out;

    /**
     * alpha tint, rgb should be 1 as we are only changing transparency
     */
    private final Color alphaTint = new Color(1, 1, 1, 1);

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
        alert = directory.getEntry("alert", Texture.class);
        noticed = directory.getEntry("noticed", Texture.class);
        transition_background = directory.getEntry("transition-background", Texture.class);
        moon = directory.getEntry("moon", Texture.class);
        trees = directory.getEntry("trees", Texture.class);

        // shaders
        this.meter = directory.get("meter", ShaderProgram.class);
        this.meterUniform = new ShaderUniform("u_amount");

    }

    /**
     * @return Elapsed time of transition phase
     */
    public float getElapsed() {
        return elapsed;
    }


    /**
     * Draws any needed UI on screen during gameplay
     *
     * @param canvas drawing canvas
     * @param level  container holding all models
     * @param phase  current phase of the game
     */
    public void drawUI(GameCanvas canvas, LevelContainer level, Phase phase,
                       GameplayController gameplayController, float delta) {
        if (gameplayController.getState() == GameState.PLAY) {
            setFontColor(Color.WHITE);

            // Draw with view transform considered
            canvas.begin(GameCanvas.DrawPass.SHAPE, level.getView().x, level.getView().y);

            if (gameplayController.getCollectingMoonlight() && phase == Phase.STEALTH) {
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
            if (phase == Phase.STEALTH) {
                drawHealthStats(canvas, level, phase, delta);
                drawMoonlightStats(canvas, level, delta);
                drawStealthStats(canvas, level);
            } else if (phase == Phase.BATTLE) {
                drawHealthStats(canvas, level, phase, delta);
                drawPowerStats(canvas, level);
                drawRangeStats(canvas, level);
            }
            canvas.end();

            canvas.begin(GameCanvas.DrawPass.SHAPE);
            // If necessary draw screen flash
            ScreenFlash.update(Gdx.graphics.getDeltaTime());
            canvas.drawScreenFlash(level.getPlayer());
            canvas.end();

            drawStealthIndicator(canvas, level);

            if (phase == Phase.TRANSITION) {
                drawTransitionScreen(canvas, level, delta);
            } else if (phase == Phase.STEALTH) {
                moon_centerY_ratio = MOON_CENTER_LOW;
                elapsed = 0;
            }
        }
    }

    public void setFontColor(Color color) {
        UIFont_large.setColor(color);
        UIFont_small.setColor(color);
    }

    /**
     * Draw the transition animation
     *
     * @param canvas drawing canvas
     * @param level  container holding all models
     * @param delta  Number of seconds since last animation frame
     */
    public void drawTransitionScreen(GameCanvas canvas, LevelContainer level, float delta) {
        elapsed = elapsed + delta;

        float fade_time = FADE_TIME_PROP * level.getPhaseTransitionTime();
        if (level.getPhaseTransitionTime() - elapsed <= fade_time) {
            float outProg = Math.min(1f, elapsed - (level.getPhaseTransitionTime() - fade_time) / fade_time);
            alphaTint.a = EAS_FN.apply(1 - outProg);
        }

        canvas.begin(GameCanvas.DrawPass.SPRITE);
        canvas.drawOverlay(transition_background, Color.BLACK, true);
        canvas.drawOverlay(transition_background, alphaTint, true);

        float moon_low = canvas.getHeight() * MOON_CENTER_LOW;
        float moon_high = canvas.getHeight() * MOON_CENTER_HIGH;
        float moon_rise_dist = moon_high - moon_low;
        float moon_size = canvas.getHeight() * MOON_SIZE_RATIO;
        float moon_centerX = canvas.getWidth() * MOON_CENTERX_RATIO;

        float transition_sec = level.getPhaseTransitionTime() * 2 / 3;
        float total_num_updates = transition_sec / delta;
        float rise_amount = moon_rise_dist / total_num_updates;
        moon_centerY_ratio = moon_centerY_ratio + rise_amount / canvas.getHeight();
        float moon_centerY = moon_centerY_ratio * canvas.getHeight();
        if (moon_centerY > moon_high) {
            moon_centerY = moon_high;
        }

        canvas.draw(moon, alphaTint, moon_centerX - moon_size / 2,
                moon_centerY - moon_size / 2, moon_size, moon_size);

        float tree_width = canvas.getWidth();
        float tree_height = (float) trees.getHeight() / trees.getWidth() * tree_width;
        canvas.draw(trees, alphaTint, 0, 0, tree_width, tree_height);
        canvas.end();
    }

    /**
     * Draw the level stroke and level status of the player
     */
    public void drawLevelStats(GameCanvas canvas, GameplayController.Phase phase, GameplayController gameplayController) {
        canvas.draw(counter, Color.WHITE, canvas.getWidth() / 2 - COUNTER_WIDTH / 2, canvas.getHeight() - COUNTER_HEIGHT - TOP_MARGIN / 2, COUNTER_WIDTH, COUNTER_HEIGHT);
        String text;
        Texture icon;
        if (phase == Phase.STEALTH || phase == Phase.TRANSITION) {
            text = "night";
            icon = dusk_icon;
        } else {
            text = "full moon";
            icon = fullmoon_icon;
        }
        canvas.drawText(text, UIFont_small, canvas.getWidth() / 2 - UIFont_small.getCapHeight() * text.length() / 3, canvas.getHeight() - TOP_MARGIN / 2.5f);
        canvas.drawText("1", UIFont_large, canvas.getWidth() / 2, canvas.getHeight() - TOP_MARGIN);
        int remaining_sec = Math.max((int) gameplayController.getRemainingTime(), 0);
        int min = remaining_sec / 60;
        int sec = remaining_sec % 60;
        canvas.drawText(min + ":" + sec + "s", UIFont_small, canvas.getWidth() / 2 - COUNTER_WIDTH / 4, canvas.getHeight() - COUNTER_HEIGHT / 2.0f - TOP_MARGIN / 3);
        if (icon == dusk_icon) {
            canvas.draw(icon, Color.WHITE, 0, 0, canvas.getWidth() / 2 + COUNTER_WIDTH / 4, canvas.getHeight() - COUNTER_HEIGHT / 2.0f - dusk_icon.getHeight() / 2 - TOP_MARGIN / 3, 0, 0.6f, 0.6f);
        } else {
            canvas.draw(icon, Color.WHITE, 0, 0, canvas.getWidth() / 2 + COUNTER_WIDTH / 4, canvas.getHeight() - COUNTER_HEIGHT / 2.0f - dusk_icon.getHeight() / 2 - TOP_MARGIN / 3, 0, 0.3f, 0.3f);
        }
    }

    /**
     * Draw the health stroke and health status of the player
     */
    public void drawHealthStats(GameCanvas canvas, LevelContainer level, Phase phase, float delta) {
        float stroke_width = HEALTH_STROKE_WIDTH;
        int max_hp = Werewolf.INITIAL_HP;
        if (phase == Phase.STEALTH) {
            stroke_width = HEALTH_STROKE_WIDTH;
            max_hp = Werewolf.INITIAL_HP;
        } else if (phase == Phase.BATTLE) {
            stroke_width = HEALTH_STROKE_WIDTH * (level.getPlayer().getHp() + 1) / Werewolf.INITIAL_HP;
            max_hp = Werewolf.MAX_HP;
        }
        canvas.draw(health_stroke, Color.WHITE, 0, canvas.getHeight() - HEALTH_STROKE_HEIGHT * 2, stroke_width, HEALTH_STROKE_HEIGHT);
        for (int i = 1; i <= max_hp; i++) {
            if (level.getPlayer().getHp() >= i) {
                // Draw a filled heart for the ith heart
                Color full = new Color(138f / 255.0f, 25f / 255.0f, 45f / 255.0f, 1f);
                canvas.draw(health_icon, full, health_icon.getWidth() / 2, health_icon.getHeight() / 2, HEALTH_STROKE_WIDTH / 8 + HEART_SEP * i, canvas.getHeight() - HEALTH_STROKE_HEIGHT * 1.6f, 0, 0.6f, 0.6f);
            } else {
                // Draw an empty heart for the ith heart
                if (phase == Phase.STEALTH) {
                    Color empty = new Color(41f / 255.0f, 41f / 255.0f, 41f / 255.0f, 0.8f);
                    canvas.draw(health_icon, empty, health_icon.getWidth() / 2, health_icon.getHeight() / 2, HEALTH_STROKE_WIDTH / 8 + HEART_SEP * i, canvas.getHeight() - HEALTH_STROKE_HEIGHT * 1.6f, 0, 0.6f, 0.6f);
                }
            }
        }
        if (level.getPlayer().getHp() < last_hp) {
            drawHealthLose(canvas, delta);
        }
        last_hp = level.getPlayer().getHp();
    }

    /**
     * Draw the moonlight stroke and moonlight status
     */
    public void drawMoonlightStats(GameCanvas canvas, LevelContainer level, float delta) {
        canvas.draw(moonlight_stroke, Color.WHITE, MOON_STROKE_WIDTH / 3 + HEALTH_STROKE_WIDTH, canvas.getHeight() - HEALTH_STROKE_HEIGHT - MOON_STROKE_HEIGHT, MOON_STROKE_WIDTH, MOON_STROKE_HEIGHT);
        canvas.draw(moon_icon, Color.WHITE, moon_icon.getWidth() / 2, moon_icon.getHeight() / 2,
                MOON_STROKE_WIDTH / 2 + moon_icon.getWidth() / 4 + HEALTH_STROKE_WIDTH, canvas.getHeight() - HEALTH_STROKE_HEIGHT - MOON_STROKE_HEIGHT + moon_icon.getHeight() / 2, 0, 0.5f, 0.5f);
        canvas.drawText(level.getPlayer().getMoonlightCollected() + "/" + ((int) level.getTotalMoonlight()), UIFont_small,
                MOON_STROKE_WIDTH * 4 / 5 + HEALTH_STROKE_WIDTH, canvas.getHeight() - HEALTH_STROKE_HEIGHT * 2 + UIFont_small.getCapHeight() * 2.5f);
        if (level.getPlayer().getMoonlightCollected() > last_moon) {
            drawMoonCollect(canvas, delta);
        }
        last_moon = level.getPlayer().getMoonlightCollected();
    }

    /**
     * Draw the stealth stroke and stealth status of the player
     */
    public void drawStealthStats(GameCanvas canvas, LevelContainer level) {
        canvas.draw(stealth_stroke, Color.WHITE, canvas.getWidth() / 2 - STEALTH_STROKE_WIDTH / 2, MOON_STROKE_HEIGHT, STEALTH_STROKE_WIDTH, STEALTH_STROKE_HEIGHT);
        float proportion = level.getPlayer().getStealth();
        canvas.draw(stealth_icon, Color.WHITE, stealth_icon.getWidth() / 2, stealth_icon.getHeight() / 2, canvas.getWidth() / 2 - STEALTH_STROKE_WIDTH / 2 + stealth_icon.getWidth(), MOON_STROKE_HEIGHT + stealth_icon.getHeight() * 3 / 5, (float) (13f / 180f * Math.PI), 0.7f, 0.7f);
        Color stealth_fill = new Color(255f / 255.0f, 255f / 255.0f, 255f / 255.0f, 1f);
        canvas.draw(stealth_stroke, stealth_fill, canvas.getWidth() / 2 - STEALTH_STROKE_WIDTH / 2, MOON_STROKE_HEIGHT, STEALTH_STROKE_WIDTH * proportion, STEALTH_STROKE_HEIGHT);
    }

    /**
     * Draw the lose of 1 HP
     */
    public void drawHealthLose(GameCanvas canvas, float delta) {
        // TODO: Make this draw over multiple frames
        Color healthColor = new Color(202f / 255.0f, 139f / 255.0f, 139f / 255.0f, 1);
        setFontColor(healthColor);
        canvas.drawText("-1", UIFont_small, HEALTH_STROKE_WIDTH / 2, canvas.getHeight() - HEALTH_STROKE_HEIGHT * 3);
    }

    /**
     * Draw the collect of 1 moon
     */
    public void drawMoonCollect(GameCanvas canvas, float delta) {
        // TODO: Make this draw over multiple frames
        Color moonColor = new Color(248f / 255.0f, 228f / 255.0f, 184f / 255.0f, 1);
        setFontColor(moonColor);
        canvas.drawText("-1", UIFont_small, HEALTH_STROKE_WIDTH + MOON_STROKE_WIDTH / 2, canvas.getHeight() - HEALTH_STROKE_HEIGHT * 3);
    }

    /**
     * Draw attack power stats
     */
    public void drawPowerStats(GameCanvas canvas, LevelContainer level) {
        canvas.end();
        canvas.begin(GameCanvas.DrawPass.SHAPE);
        canvas.drawAttackPow(canvas.getWidth() - BAR_WIDTH, canvas.getHeight() - BAR_HEIGHT * 2,
                BAR_WIDTH, BAR_HEIGHT, level.getPlayer().getAttackPower());
        canvas.end();

        canvas.begin(GameCanvas.DrawPass.SPRITE);
        canvas.drawText("Attack power ", UIFont_small,
                canvas.getWidth() - BAR_WIDTH - UIFont_small.getAscent() * ("Attack power ".length()) * 2,
                canvas.getHeight() - BAR_HEIGHT);
        canvas.end();
    }

    /**
     * Draw attack range stats
     */
    public void drawRangeStats(GameCanvas canvas, LevelContainer level) {
        canvas.begin(GameCanvas.DrawPass.SPRITE);
        canvas.drawText("Attack range ", UIFont_small,
                canvas.getWidth() - BAR_WIDTH - UIFont_small.getAscent() * ("Attack range ".length()) * 2,
                canvas.getHeight() - BAR_HEIGHT * 2 - GAP_DIST);
        canvas.end();

        canvas.begin(GameCanvas.DrawPass.SHAPE);
        canvas.drawAttackRange(canvas.getWidth() - BAR_WIDTH, canvas.getHeight() - BAR_HEIGHT * 3 - GAP_DIST,
                BAR_WIDTH, BAR_HEIGHT, level.getPlayer().getAttackRange());
        canvas.end();
        canvas.begin(GameCanvas.DrawPass.SPRITE);
    }

    /**
     * Draw the stealth indicator above enemies
     */
    public void drawStealthIndicator(GameCanvas canvas, LevelContainer level) {
        Array<Enemy> enemies = level.getEnemies();
        for (Enemy enemy : enemies) {
            switch (enemy.getDetection()) {
                case ALERT:
                case NOTICED:
                    // Draw with view transform considered
                    canvas.begin(GameCanvas.DrawPass.SPRITE, level.getView().x, level.getView().y);
                    Texture tex = enemy.getDetection() == Enemy.Detection.ALERT ? alert : noticed;
                    canvas.draw(
                            tex,
                            Color.WHITE,
                            tex.getWidth() / 2, tex.getHeight() / 2,
                            canvas.WorldToScreenX(enemy.getPosition().x) - 10,
                            canvas.WorldToScreenY(enemy.getPosition().y) + enemy.getTextureHeight(), 0,
                            0.5f, 0.5f
                    );
                    canvas.end();
                    break;
                case INDICATOR:
                    if (enemy.getIndicatorAmount() == 0) break;
                    meterUniform.setValues(enemy.getIndicatorAmount());
                    canvas.begin(GameCanvas.DrawPass.SHADER, level.getView().x, level.getView().y);
                    canvas.drawShader(
                            meter,
                            canvas.WorldToScreenX(enemy.getPosition().x) - 38,
                            canvas.WorldToScreenY(enemy.getPosition().y) + enemy.getTextureHeight() - 25,
                            50, 50,
                            meterUniform);
                    canvas.end();
                    break;
            }
        }
    }

}