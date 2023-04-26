package infinityx.lunarhaze.models;

import box2dLight.PointLight;

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
     * Is the moonlight collectable? Should be true only if lit is true.
     */
    private boolean collectable = false;

    /**
     * Frame index into tile sprite sheet held by Board
     */
    private int tileNum;

    /**
     * The moonlight pointing on this tile, possibly null
     */
    private PointLight spotLight;

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
     * Whether the spotlight on this tile is active
     */
    public boolean isLit() {
        if (spotLight == null) {
            return false;
        }
        return spotLight.isActive();
    }

    /**
     * Assumes {@link #spotLight} is not null to enable/disable.
     *
     * @param lit
     */
    public void setLit(boolean lit) {
        spotLight.setActive(lit);
    }


    public Boolean isCollectable() {
        return collectable;
    }

    public void setCollectable(boolean collectable) {
        this.collectable = collectable;
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

    public int getTileNum() {
        return tileNum;
    }

    public void setTileNum(int num) {
        tileNum = num;
    }
}
