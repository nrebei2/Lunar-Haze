package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.GameplayController.Phase;

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
     * This is a class used for drawing player and enemies' game state: HP, Stealth, MoonLight
     * Collection
     * @param font
     * @param directory
     */
    public UIRender(BitmapFont font, AssetDirectory directory) {
        this.displayFont = font;
        this.directory = directory;
    }


    /**  */
    public void drawUI(GameCanvas canvas, LevelContainer level, GameplayController.Phase phase ) {
        System.out.println(phase);
        displayFont.setColor(Color.YELLOW);
        canvas.begin(); // DO NOT SCALE
        canvas.drawLightBar(BAR_WIDTH,
                BAR_HEIGHT, level.getPlayer().getLight());
        canvas.drawHpBar(BAR_WIDTH,
                BAR_HEIGHT, level.getPlayer().getHp());
        canvas.drawStealthBar(BAR_WIDTH,
                BAR_HEIGHT, level.getPlayer().getStealth());
        if (phase == Phase.BATTLE) {
            canvas.drawEnemyHpBars(BAR_WIDTH / 4.0f, BAR_HEIGHT / 4.0f,
                    level.getEnemies());
        }
        canvas.end();
    }

}
