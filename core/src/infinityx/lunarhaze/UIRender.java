package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.GameplayController.Phase;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;

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
    private final static float BAR_HEIGHT = 40.0f;

    /**
     * The font for giving messages to the player
     */
    protected BitmapFont displayFont;

    /**
     * Create a new UIRender with font and directory assigned.
     * @param font
     * @param directory
     */
    public UIRender(BitmapFont font, AssetDirectory directory) {
        this.displayFont = font;
        this.directory = directory;
    }


    /**
     * Draws any needed UI on screen during gameplay
     * @param canvas drawing canvas
     * @param level container holding all models
     * @param phase current phase of the game
     * */
    public void drawUI(GameCanvas canvas, LevelContainer level, GameplayController.Phase phase ) {
        displayFont.setColor(Color.YELLOW);
        canvas.begin(); // DO NOT SCALE
        canvas.drawLightBar(BAR_WIDTH,
                BAR_HEIGHT, level.getPlayer().getLight());
        canvas.drawHpBar(BAR_WIDTH,
                BAR_HEIGHT, level.getPlayer().getHp());
        canvas.drawStealthBar(BAR_WIDTH,
                BAR_HEIGHT, level.getPlayer().getStealth());
        if (phase == Phase.BATTLE) {
            EnemyList enemyList = level.getEnemies();
            for(Enemy enemy : enemyList) {
                canvas.drawEnemyHpBars(
                        BAR_WIDTH / 4.0f, BAR_HEIGHT / 4.0f, enemy
                );
            }
        }
        canvas.end();
    }

}