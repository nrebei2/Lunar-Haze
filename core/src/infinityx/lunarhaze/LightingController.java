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

public class LightingController {

    /** Rayhandler for storing lights */
    protected RayHandler rayHandler;

    /** All light sources in level */
    private Array<LightSource> lights = new Array<LightSource>();

    /** The camera defining the RayHandler view; scale is in physics coordinates */
    protected OrthographicCamera raycamera;

    /** List of enemies to attach to */
    private EnemyList enemies;

    public LightingController(EnemyList e) {
        enemies = e;
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
            ConeSource cone = new ConeSource(rayHandler, 512, Color.WHITE, 100f, e.getX(), e.getY(), 90, 100);
            cone.attachToBody(e.body, 0, 0, cone.getDirection());
            cone.setActive(true);
            System.out.println("Added a light at " + cone.getX());
        }
    }
}
