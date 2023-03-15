package infinityx.lunarhaze.physics;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import infinityx.lunarhaze.GameObject;

public class RaycastInfo implements RayCastCallback {
    public Fixture fixture;
    public Vector2 point;
    public Vector2 normal;
    public float fraction;
    public boolean hit;
    private GameObject requestingObject;
    public GameObject hitObject;

    public RaycastInfo(GameObject obj){
        fixture = null;
        point = new Vector2();
        normal = new Vector2();
        fraction = 0.0f;
        hit = false;
        hitObject = null;
        this.requestingObject = obj;
    }
    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        System.out.println(fixture.getUserData());
        if (fixture.getUserData() == requestingObject){
            return 1;
        }
        this.fixture = fixture;
        this.point = new Vector2(point.x, point.y);
        this.normal = new Vector2(normal.x, normal.y);
        this.fraction = fraction;
        this.hit = fraction != 0;
        this.hitObject = (GameObject) fixture.getUserData();
        return fraction;
    }
}
