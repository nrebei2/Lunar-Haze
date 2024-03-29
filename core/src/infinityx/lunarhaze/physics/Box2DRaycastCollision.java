package infinityx.lunarhaze.physics;

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectSet;
import infinityx.lunarhaze.models.GameObject;

/**
 * Raycast collision detector for Box2D
 */
public class Box2DRaycastCollision implements RaycastCollisionDetector<Vector2> {
    World world;
    RaycastInfo callback;

    /**
     * The gameobject that is attached to the fixture hit
     */
    public GameObject hitObject;

    /**
     * Game object types the collision will ignore
     */
    private final ObjectSet<GameObject.ObjectType> ignore;

    public Box2DRaycastCollision(World world, RaycastInfo raycastInfo) {
        this.world = world;
        this.callback = raycastInfo;
        this.ignore = new ObjectSet<>();
    }

    @Override
    public boolean collides(Ray<Vector2> ray) {
        return findCollision(null, ray);
    }

    @Override
    public boolean findCollision(Collision<Vector2> outputCollision, Ray<Vector2> inputRay) {
        callback.pushIgnores(this);
        callback.hit = false;
        if (!inputRay.start.epsilonEquals(inputRay.end, MathUtils.FLOAT_ROUNDING_ERROR)) {
            callback.outputCollision = outputCollision;
            world.rayCast(callback, inputRay.start, inputRay.end);
            hitObject = callback.hitObject;
        }
        return callback.hit;
    }

    public Box2DRaycastCollision addIgnore(GameObject.ObjectType type) {
        ignore.add(type);
        return this;
    }

    public ObjectSet<GameObject.ObjectType> getIgnore() {
        return ignore;
    }
}

