package infinityx.lunarhaze.physics;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import infinityx.lunarhaze.GameObject;

public class RaycastInfo implements RayCastCallback {
    /**The fixture hit by ray*/
    public Fixture fixture;

    /**Collision point*/
    public Vector2 point;

    /**The normal at the point we hit a fixture*/
    public Vector2 normal;

    /**The fraction of the ray that is used to hit the fixture*/
    public float fraction;
    public boolean hit;

    /**The gameobject that is casting the ray*/
    private GameObject requestingObject;

    /**The gameobject that is attached to the fixture hit*/
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

    /**Call back function for raycasting
     *
     * return -1 to filter the current intersection
     * return 0 to terminate
     * returning fraction to clip the ray for closest hit
     * returning 1 to continue
     * */
    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        if (fixture.getUserData() == requestingObject) {
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
