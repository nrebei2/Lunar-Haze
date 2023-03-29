package infinityx.lunarhaze.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Model supporting multiple circle and box shaped colliders.
 * Note every shape has a name which you supply, these names can be used to edit their respective shape.
 */
public class MultiShapeObstacle extends SimpleObstacle {

    /** Holds information regarding a shape on this object */
    public static class ShapeCache {
        /** Attached fixture, may be null if activatePhysics has not been called yet */
        public Fixture fixture;

        /** Actual shape */
        public Shape shape;

        /** Width of square if shape is a box, diameter if shape is a circle */
        public float width;
        public float height;
        public Vector2 offset;
        public float angle;
    }

    /** Maps names of shapes to information regarding it */
    private ObjectMap<String, ShapeCache> geometries;


    /**
     * Creates a new simple physics object with no initial shapes
     *
     * @param x Initial x position in world coordinates
     * @param y Initial y position in world coordinates
     */
    public MultiShapeObstacle(float x, float y) {
        super(x, y);
        geometries = new ObjectMap<>();
    }

    /**
     * Return may be null if there is no shape with the given name.
     * @return ShapeCache object holding some information regarding the shape
     */
    protected ShapeCache getShapeInformation(String name) {
        return geometries.get(name);
    }

    @Override
    public void setScale(float s) {
        super.setScale(s);
        for (ObjectMap.Entry<String, ShapeCache> entry: geometries) {
            ShapeCache cache = entry.value;
            switch (cache.shape.getType()) {
                case Polygon:
                    resizeBox(entry.key, s * cache.width, s * cache.height, cache.offset.scl(s));
                    break;
                case Circle:
                    resizeCircle(entry.key, s * cache.width / 2, cache.offset.scl(s));
                    break;
                default:
                    break;
            }
        }
    }

    /** Add an oriented box shape to this object */
    protected void addBox(String name, float width, float height, Vector2 offset, float angle) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2, offset, angle);

        ShapeCache cache = new ShapeCache();
        cache.shape = shape;
        cache.width = width;
        cache.height = height;
        cache.offset = offset;
        cache.angle = angle;

        createFixture(name, cache);

    }

    /**
     * Reset the polygon vertices in the shape to match the dimension.
     */
    protected void resizeBox(String name, float width, float height) {
        if (!geometries.containsKey(name)) {
            Gdx.app.error("MultiShapeObstacle", "Cannot resize a shape which doesnt exist", new IllegalStateException());
            return;
        }

        ShapeCache cache = geometries.get(name);
        resizeBox(name, width, height, cache.offset, cache.angle);
    }

    /**
     * Reset the polygon vertices in the shape to match the dimension.
     */
    protected void resizeBox(String name, float width, float height, Vector2 offset) {
        if (!geometries.containsKey(name)) {
            Gdx.app.error("MultiShapeObstacle", "Cannot resize a shape which doesnt exist", new IllegalStateException());
            return;
        }
        ShapeCache cache = geometries.get(name);
        resizeBox(name, width, height, offset, cache.angle);
    }

    /**
     * Reset the polygon vertices in the shape to match the dimension.
     */
    protected void resizeBox(String name, float width, float height, Vector2 offset, float angle) {
        if (!geometries.containsKey(name)) {
            Gdx.app.error("MultiShapeObstacle", "Cannot resize a shape which doesnt exist", new IllegalStateException());
            return;
        }

        ShapeCache cache = geometries.get(name);

        if (cache.shape.getType() != Shape.Type.Polygon) {
            Gdx.app.error("MultiShapeObstacle", "Shape is not a box", new IllegalStateException());
            return;
        }

        releaseFixture(name);
        addBox(name, width, height, offset, angle);
    }

    /** Add a circle shape to this object */
    protected void addCircle(String name, float radius, Vector2 offset) {
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        shape.setPosition(offset);

        ShapeCache cache = new ShapeCache();
        cache.shape = shape;
        cache.width = radius*2;
        cache.offset = offset;

        createFixture(name, cache);
    }

    /**
     * Reset the circle in the shape to match the dimension.
     */
    protected void resizeCircle(String name, float radius, Vector2 offset) {
        if (!geometries.containsKey(name)) {
            Gdx.app.error("MultiShapeObstacle", "Cannot resize a shape which doesnt exist", new IllegalStateException());
            return;
        }

        ShapeCache cache = geometries.get(name);

        if (cache.shape.getType() != Shape.Type.Circle) {
            Gdx.app.error("MultiShapeObstacle", "Shape is not a circle", new IllegalStateException());
            return;
        }

        releaseFixture(name);
        addCircle(name, radius, offset);
    }

    protected void removeShape(String name) {
        if (!geometries.containsKey(name)) {
            Gdx.app.error("MultiShapeObstacle", "Cannot remove a shape which doesnt exist", new IllegalStateException());
            return;
        }
        releaseFixture(name);
        geometries.remove(name);
    }


    protected void createFixture(String name, ShapeCache cache) {
        geometries.put(name, cache);

        if (body == null) {
            return;
        }

        fixture.shape = cache.shape;
        Fixture geometry = body.createFixture(fixture);
        cache.fixture = geometry;
        geometry.setUserData(this);
    }

    protected void createFixtures() {
        releaseFixtures();

        for (ObjectMap.Entry<String, ShapeCache> entry: geometries) {
            createFixture(entry.key, entry.value);
        }
    }

    protected void releaseFixture(String name) {
        if (body == null) {
            return;
        }
        if (geometries.get(name).fixture == null) {
            return;
        }
        body.destroyFixture(geometries.get(name).fixture);
    }

    protected void releaseFixtures() {
        for (String name: geometries.keys()) {
            releaseFixture(name);
        }
    }
}
