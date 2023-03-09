package infinityx.lunarhaze;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.lunarhaze.physics.LightSource;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LightingController {

    /** Rayhandler for storing lights */
    protected RayHandler rayHandler;

    /** All light sources in level */
    private Array<LightSource> lights = new Array<LightSource>();

    /** The camera defining the RayHandler view; scale is in physics coordinates */
    protected OrthographicCamera raycamera;

    /** List of enemies to attach to */
    private EnemyList enemies;

    private HashMap<Vector2, Vector2> moonlightPositions;

    private PointLight[][] moonlight;

    int width;
    int height;

    public LightingController(EnemyList e, HashMap<Vector2, Vector2> pos, int w, int h) {
        enemies = e;
        moonlightPositions = pos;
        width = w;
        height = h;
    }

    public RayHandler getRayHandler() { return rayHandler; }
    public void initLights(boolean gamma, boolean diffuse, int blur, World world) {

        int width = 1280;
        int height = 960;

        raycamera = new OrthographicCamera(width, height);
        raycamera.position.set(width/2.0f, height/2.0f, 0);
        raycamera.update();

        RayHandler.setGammaCorrection(gamma);
        RayHandler.useDiffuseLight(diffuse);
        rayHandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
        rayHandler.setCombinedMatrix(raycamera);
        rayHandler.setAmbientLight(0.25f, 0.23f, 0.35f, 0.25f);
        rayHandler.setBlur(blur > 0);
        rayHandler.setBlurNum(blur);
        rayHandler.setShadows(true);

        // Create light for each enemy and attach
        for(Enemy e : enemies) {
            e.setFlashlight(new ConeSource(rayHandler, 512, new Color(0.8f, 0.8f, 0.2f, 0.8f), 3500f, e.getX(), e.getY(), 90, 30));
            e.getFlashlight().attachToBody(e.body, 0, 0, e.getFlashlight().getDirection());
            e.getFlashlight().setActive(true);
        }

        moonlight = new PointLight[width][height];
        for(Map.Entry<Vector2, Vector2> pos : moonlightPositions.entrySet()) {
            Vector2 boardPos = pos.getKey();
            Vector2 worldPos = pos.getValue();
            moonlight[(int) boardPos.x][(int) boardPos.y] = new PointLight(rayHandler, 512, new Color(0.7f, 0.7f, 1f, 0.6f), 200f, worldPos.x, worldPos.y);
            moonlight[(int) boardPos.x][(int) boardPos.y].setActive(true);
        }
    }

    public void removeLightAt(int x, int y) {
        moonlight[x][y].setActive(false);
    }

}
