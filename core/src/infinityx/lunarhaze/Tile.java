package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Texture;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Represents a background tile in the scene
 */
public class Tile {
    private enum TileType {
        Grass, Road, Dirt,
        // TODO: Add more types
    }
    private TileType type;
    /** Is there a scene object on this tile?  */
    private boolean walkable = false;
    /** Is this a goal tile? (used for AI) */
    private boolean goal = false;
    /** Has this tile been visited (used for pathfinding AI)? */
    private boolean visited = false;
    /** Is there moonlight on this tile? */
    private boolean lit = false;

    /** Texture of tile (Lit/Unlit from moonlight) **/
    private Texture TileTextureUnlit;
    private Texture TileTextureLit;

    // No need for constructor, levelContainer will set attributes of all tiles through Board

    /**
     * Type of tile should match with sprite texture
     *
     * @return type of this background tile (Grass, Road, etc.)
     */
    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
    }

    /**
     * Used for collision detection (player/enemies should not be able to walk on this tile!)
     *
     * Should be set to
     *
     * @return true if the tile can be walked over
     */
    public boolean isWalkable() {
        return walkable;
    }

    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }

    public boolean isGoal() {
        return goal;
    }

    public void setGoal(boolean goal) {
        this.goal = goal;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean isLit() {
        return lit;
    }

    public void setLit(boolean lit) {
        this.lit = lit;
    }

    /**
     * Returns the unlit image texture for the tile. Will be drawn if lit is false.
     *
     * May be null, must be set before get
     *
     * @return the unit image texture for the tile
     */
    public Texture getTileTextureUnlit() {
        return TileTextureUnlit;
    }

    /**
     * Returns the lit image texture for the tile. Will be drawn if lit is true.
     *
     * May be null, must be set before get
     *
     * @return the lit image texture for the tile
     */
    public Texture getTileTextureLit() {
        return TileTextureLit;
    }

    public void setTileTextureUnlit(Texture unlitTex) {
        this.TileTextureUnlit = unlitTex;
    }

    public void setTileTextureLit(Texture litTex) {
        this.TileTextureLit = litTex;
    }

}
