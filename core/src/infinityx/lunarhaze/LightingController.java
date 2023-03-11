package infinityx.lunarhaze;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.lunarhaze.physics.LightSource;

import java.util.HashMap;
import java.util.Map;

public class LightingController {
    /**
     * The camera defining the RayHandler view; scale is in physics coordinates
     */
    protected OrthographicCamera raycamera;

    /**
     * List of enemies to attach to
     */
    private final EnemyList enemies;

    private Board board;

    public static final Color LIGHTCOLOR = new Color(0.7f, 0.7f, 0.9f, 0.7f);

    /**
     * LightingController handles flashlights on enemies and moonlight on tiles
     * @param enemies
     * @param board
     */
    public LightingController(EnemyList enemies, Board board) {
        this.enemies = enemies;
        this.board = board;
    }

}
