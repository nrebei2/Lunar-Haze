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
     * Can an enemy see this tile?
     */
    private boolean visible = false;

    /**
     * Is the moonlight collected?
     */
    private boolean collected = false;

    /**
     * Texture of tile (Lit/Unlit from moonlight)
     **/
    private Texture TileTextureUnlit;
    private Texture TileTextureLit;

    private Texture TileTextureLitButCollected;

    /** The moonlight pointing on this tile, possibly null */
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

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Returns the unlit image texture for the tile. Will be drawn if lit is false.
     * <p>
     * May be null, must be set before get
     *
     * @return the unit image texture for the tile
     */
    public Texture getTileTextureUnlit() {
        return TileTextureUnlit;
    }

    /**
     * Returns the lit image texture for the tile. Will be drawn if lit is true.
     * <p>
     * May be null, must be set before get
     *
     * @return the lit image texture for the tile
     */
    public Texture getTileTextureLit() {
        return TileTextureLit;
    }

    public Texture getTileTextureLitButCollected() { return TileTextureLitButCollected; }

    public void setTileTextureUnlit(Texture unlitTex) {
        this.TileTextureUnlit = unlitTex;
    }

    public void setTileTextureLit(Texture litTex) {
        this.TileTextureLit = litTex;
    }

    public void setTileTextureLitButCollected(Texture litTex) {
        this.TileTextureLitButCollected = litTex;
    }

}
