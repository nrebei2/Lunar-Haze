package infinityx.lunarhaze;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Texture;

/**
 * Represents a background tile in the scene
 */
public class Tile {
    public enum TileType {
        Grass, Road, Dirt,
        // Used for Level Editor if no texture has been placed
        EMPTY
    }

    private TileType type = TileType.EMPTY;
    /**
     * Is there a scene object on this tile?
     */
    private boolean walkable = false;
    /**
     * Is this a goal tile? (used for AI)
     */
    private boolean goal = false;
    /**
     * Has this tile been visited (used for pathfinding AI)?
     */
    private boolean visited = false;

    /**
     * Use for debugging
     */
    private boolean visible = false;

    /**
     * Is the moonlight collected?
     */
    private boolean collected = false;

    /**
     * Texture of tile
     * TODO: Should be a TextureRegion taken from a sprite sheet of tiles to optimize rendering.
     * Right now, the spritebatch is unable to batch much geometry since texture changes when drawing board.
     **/
    private Texture TileTexture;

    /**
     * The moonlight pointing on this tile, possibly null
     */
    private PointLight spotLight;

    // No need for constructor, levelContainer will set attributes of all tiles through Board
    public Tile() {
    }

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
     * Used for collision detection (player/enemies should not be able to walk on this tile!
     * <p>
     * Should be set to false only when there is an object on this tile
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
        if (spotLight == null) {
            return false;
        }
        return spotLight.isActive();
    }

    public void setLit(boolean lit) {
        spotLight.setActive(lit);
    }


    public Boolean isCollected() {
        return collected;
    }

    public void setCollected() {
        collected = true;
    }

    /**
     * Attaches light to tile, represents the moonlight on the tile
     */
    public void setSpotLight(PointLight light) {
        spotLight = light;
    }

    public PointLight getSpotLight() {
        return spotLight;
    }

    /**
     * Use for debugging
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible() {
        return this.visible;
    }

    /**
     * Returns the image texture for the tile.
     * <p>
     * May be null, must be set before get
     *
     * @return the unit image texture for the tile
     */
    public Texture getTileTexture() {
        return TileTexture;
    }

    public void setTileTexture(Texture tex) {
        this.TileTexture = tex;
    }

}
