package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Color;
import infinityx.lunarhaze.entity.EnemyList;

public class LightingController {
    /**
     * List of enemies
     */
    private final EnemyList enemies;

    private final Board board;

    /**
     * TODO: This is a controller class, so it should handle interactions between lights and objects
     * Maybe the light should update throughout the lifetime of the level??
     *
     * @param enemies
     * @param board
     */
    public LightingController(EnemyList enemies, Board board) {
        this.enemies = enemies;
        this.board = board;
    }

}
