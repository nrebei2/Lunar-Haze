package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Color;
import infinityx.lunarhaze.entity.EnemyList;

public class LightingController {
    /**
     * List of enemies to attach to
     */
    private final EnemyList enemies;

    private final Board board;

    public static final Color LIGHTCOLOR = new Color(0.7f, 0.7f, 0.9f, 0.7f);

    /**
     * LightingController handles flashlights on enemies and moonlight on tiles
     *
     * @param enemies
     * @param board
     */
    public LightingController(EnemyList enemies, Board board) {
        this.enemies = enemies;
        this.board = board;
    }

}
