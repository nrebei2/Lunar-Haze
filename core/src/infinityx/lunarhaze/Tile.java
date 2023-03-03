package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Texture;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Represents a background tile in the scene
 */
public class Tile {
    public Tile (TileType type) {
        this.type = type;
    }

    public enum TileType {
        Grass, Road, Dirt,
        // TODO: Add more types
    }
    public TileType type;
    /** Is there a scene object on this tile?  */
    public boolean walkable = false;
    /** Is this a goal tile? (used for AI) */
    public boolean goal = false;
    /** Has this tile been visited (used for pathfinding)? */
    public boolean visited = false;
    /** Is there moonlight on this tile? */
    public boolean lighted = false;

    /** Texture of tile **/
    private Texture TileTexture;

    /**
     * Returns the image texture for the tile
     *
     * May be null, must be set before get
     *
     * @return the image texture for the tile
     */
    public Texture getTileTexture() {
        return TileTexture;
    }

    public void setTileTexture(Texture texture) {

    }

    public void setWalkable() {

    }

    public void setGoal() {

    }

    public void setVisited() {

    }
}
