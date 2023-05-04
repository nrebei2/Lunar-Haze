package infinityx.lunarhaze.physics;

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.ObjectSet;
import infinityx.lunarhaze.models.GameObject;

public class RaycastInfo implements RayCastCallback {
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
    private final GameObject requestingObject;

    /**
     * Game object types the ray will ignore
     */
    private final ObjectSet<GameObject.ObjectType> ignore;

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
     * Add types The ray will ignore objects of given types
     *
     * @param types of game objects ignore
     */
    public void addIgnores(GameObject.ObjectType... types) {
        ignore.addAll(types);
    }

    /**
     * Call back function for raycasting
     * <p>
     * return -1 to filter the current intersection
     * return 0 to terminate
     * returning fraction to clip the ray for closest hit
     * returning 1 to continue
     */
    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        // Right now, all hit bodies are contained in GameObjects
        GameObject objHit = (GameObject) fixture.getBody().getUserData();

        if (objHit == requestingObject || ignore.contains(objHit.getType())) {
            return 1;
        }

        this.fixture = fixture;
//        if (outputCollision != null) {
        outputCollision.set(point, normal);
//        }
        this.fraction = fraction;
        this.hit = fraction != 0;
        if (this.hit) {
            this.hitObject = objHit;
        }
        return fraction;
    }
}
