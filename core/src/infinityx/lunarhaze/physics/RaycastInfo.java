package infinityx.lunarhaze.physics;

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.ObjectSet;
import infinityx.lunarhaze.models.GameObject;

public abstract class RaycastInfo implements RayCastCallback {
    /**
     * The fixture hit by ray
     */
    public Fixture fixture;

    /**
     * Holds collision point and normal
     */
    public Collision<Vector2> outputCollision;

    /**
     * The fraction of the ray that is used to hit the fixture
     */
    public float fraction;
    public boolean hit;

    /**
     * The gameobject that is casting the ray
     */
    protected final GameObject requestingObject;

    /**
     * Game object types the ray will ignore
     */
    protected ObjectSet<GameObject.ObjectType> ignore;

    /**
     * The gameobject that is attached to the fixture hit
     */
    public GameObject hitObject;

    /**
     * @param obj The requesting object. Use null if none.
     */
    public RaycastInfo(GameObject obj) {
        fixture = null;
        fraction = 0.0f;
        hit = false;
        hitObject = null;
        this.requestingObject = obj;
        ignore = new ObjectSet<>();
    }

    /**
     * The ray will ignore objects that the Box2DRaycastCollision ignores
     *
     * @param raycastCollision Box2DRayCastCollision holding ignore list
     */
    public void pushIgnores(Box2DRaycastCollision raycastCollision) {
        ignore = raycastCollision.getIgnore();
    }
}
