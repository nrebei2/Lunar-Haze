/*
 *  GameObject.java
 *
 * In this application, we have a lot more model classes than we did in
 * previous labs.  Because these classes have a lot in common, we have
 * put their features into a base class, just as you learned in OO programming.
 *
 * With that said, you have to be very careful when subclassing your models.
 * Your hierarchy can get deep and complicated very fast if you are not
 * careful.  In fact, we have a later lecture about how subclassing is
 * not always a good idea. But it is okay in this instance because we are
 * only subclass one-level deep.
 *
 * This class continues our policy of using "passive" models. It does not
 * access the methods or fields of any other Model class.  It also
 * does not store any other model object as a field. This allows us
 * to prevent the models from being tightly coupled.  All of the coupled
 * behavior has been moved to GameplayController.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package infinityx.lunarhaze.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.physics.MultiShapeObstacle;
import infinityx.util.Drawable;
import infinityx.util.FilmStrip;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for most model objects in the game.
 */
public abstract class GameObject extends MultiShapeObstacle implements Drawable {

    /**
     * Enum specifying the type of this game object.
     */
    public enum ObjectType {
        ENEMY,
        WEREWOLF,
        SCENE,
        HITBOX
    }

    // Attributes for all game objects
    /**
     * Reference to texture origin
     */
    protected Vector2 origin;

    /**
     * Current filmstrip which will render
     */
    protected FilmStrip filmstrip;

    /**
     * Collection of named filmstrips
     */
    public Map<String, FilmStrip> filmstrips;

    /**
     * How much the texture of this object should be scaled when drawn
     */
    protected float textureScale;

    /**
     * Whether the object should be drawn at next timestep.
     */
    protected boolean destroyed;

    /**
     * Counter with texUpdate to update frame
     */
    private float texTime;

    /**
     * Time (in seconds) texture frame should increment
     */
    public float texUpdate;

    /**
     * Creates game object at (0, 0)
     */
    public GameObject() {
        this(0, 0);
    }

    /**
     * Creates a new game object with degenerate collider settings.
     * <p>
     * All units (position, velocity, etc.) are in world units.
     *
     * @param x The object x-coordinate in world
     * @param y The object y-coordinate in world
     */
    public GameObject(float x, float y) {
        super(x, y);
        filmstrips = new HashMap<>();
        this.origin = new Vector2();
    }

    /**
     * Further parses specific GameObject (collider info, etc.) attributes.
     *
     * @param directory Asset manager holding textures
     * @param json      Json tree holding information
     * @param container LevelContainer which this player is placed in
     */
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        // TODO: bother with error checking?
        setBodyType(json.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setLinearDamping(json.get("damping").asFloat());
        setDensity(json.get("density").asFloat());
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());
        JsonValue textures = json.get("textures");
        for (JsonValue tex : textures) {
            filmstrips.put(tex.name(), directory.getEntry(tex.asString(), FilmStrip.class));
        }

        JsonValue texInfo = json.get("texture");
        setTexture(texInfo.get("name").asString());
        int[] texOrigin = texInfo.get("origin").asIntArray();
        setOrigin(texOrigin[0], texOrigin[1]);
        textureScale = texInfo.getFloat("scale");
        texUpdate = -1;

        // Add shape colliders
        JsonValue p_dim = json.get("colliders");
        for (JsonValue coll : p_dim) {
            String name = coll.name();

            Vector2 offset = new Vector2();
            if (coll.has("offset")) {
                offset.set(coll.get("offset").getFloat(0), coll.get("offset").getFloat(1));
            }

            if (coll.getString("type").equals("box")) {
                addBox(
                        name, coll.getFloat("width"), coll.getFloat("height"),
                        offset, coll.has("angle") ? coll.getFloat("angle") : 0
                );
            } else if (coll.getString("type").equals("circle")) {
                addCircle(name, coll.getFloat("radius"), offset);
            }
        }
    }

    /**
     * Returns the bounding radius of the body of this object. Assumes the name of the collider is "body".
     */
    public float getBoundingRadius() {
        return getShapeInformation("body").width / 2;
    }

    @Override
    public void setDestroyed() {
        this.destroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Sets the filmstrip texture of this game object.
     *
     * @param name Name of filmstrip in {@link #filmstrips} to set.
     */
    public void setTexture(String name) {
        this.filmstrip = filmstrips.get(name);
        if (filmstrip == null) {
            Gdx.app.error("GameObject", "Could not find a texture with the name: " + name, new IllegalStateException());
            return;
        }
        filmstrip.setFrame(0);
        this.texTime = 0;
    }

    /**
     * The texture origin should correspond to the texture pixel represents the objects world position.
     * For example, if the object is a human then the origin would be the pixel between their feet.
     * <p>
     * Note the bottom left of the texture corresponds to (0, 0), not the top left.
     *
     * @param x Where to place the origin on the u axis
     * @param y Where to place the origin on the v axis
     */
    public void setOrigin(int x, int y) {
        this.origin.set(x, y);
    }

    public FilmStrip getTexture() {
        return filmstrip;
    }

    /**
     * @return width of texture that will be drawn on screen
     */
    public float getTextureWidth() {
        return filmstrip.getRegionWidth() * textureScale * scale;
    }

    /**
     * @return height of texture that will be drawn on screen
     */
    public float getTextureHeight() {
        return filmstrip.getRegionHeight() * textureScale * scale;
    }

    /**
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    public abstract ObjectType getType();

    /**
     * Updates the (local) state of this object.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {
        if (texUpdate == -1) return;
        this.texTime += delta;
        if (filmstrip != null) {
            if (texTime >= texUpdate) {
                int next = (filmstrip.getFrame() + 1) % filmstrip.getSize();
                filmstrip.setFrame(next);
                texTime = 0;
            }
        }
    }

    public float getDepth() {
        return this.getY();
    }

    public void draw(GameCanvas canvas) {
        canvas.draw(filmstrip, Color.WHITE, origin.x, origin.y,
                canvas.WorldToScreenX(getPosition().x), canvas.WorldToScreenY(getPosition().y), 0.0f,
                textureScale * scale, textureScale * scale);
    }
}