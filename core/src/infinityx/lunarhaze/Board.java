package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

/**
 * Class represents a 2D grid of tiles.
 * Wrapper around tile data
 */
public class Board {
    // Instance attributes
    /** The board width (in number of tiles) */
    private int width;
    /** The board height (in number of tiles) */
    private int height;
    /** Height/width of the tiles, in pixels */
    private float radius;
    /** The tile grid (with above dimensions) */
    private Tile[] tiles;

    /** The world dimensions of a single tile
     *  Every tile is 1 unit (meter)
     *  Make sure player movement respects this length
     */
    private static final int TILE_WIDTH = 1;

    /**
     * Creates a new board of the given size
     *
     * @param width Board width in tiles
     * @param height Board height in tiles
     */
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        tiles = new Tile[width * height];
        for (int ii = 0; ii < tiles.length; ii++) {
            tiles[ii] = new Tile();
        }
    }

    /**
     * Returns the tile for the given position (INTERNAL USE ONLY)
     *
     * Returns null if that position is out of bounds.
     *
     * @return the tile for the given position
     */
    private Tile getTile(int x, int y) {
        if (!inBounds(x, y)) {
            return null;
        }
        return tiles[x * height + y];
    }

    /**
     * Returns the number of tiles horizontally across the board.
     *
     * @return the number of tiles horizontally across the board.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the number of tiles vertically across the board.
     *
     * @return the number of tiles vertically across the board.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the size of the tile.
     *
     * @return the size of the tile.
     */
    public int getTileSize() {
        return TILE_WIDTH;
    }

    // Drawing information
    /**
     * Returns the textured mesh for a tile.
     *
     * Gives either lit or unlit texture depending on tile state.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return the textured mesh for a given tile.
     */
    public Texture getTileTexture(int x, int y) {
        Tile tile = getTile(x, y);
        if (tile == null) {
            return null;
        }
        if (tile.isLit()) {
            return tile.getTileTextureLit();
        } else {
            return tile.getTileTextureUnlit();
        }
    }

    /**
     * Sets both lit and unlit textures for a tile.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     */
    public void setTileMesh(int x, int y, Texture unlitTex, Texture litTex) {
        Tile tile = getTile(x, y);
        if (tile == null) {
            return;
        }
        tile.setTileTextureUnlit(unlitTex);
        tile.setTileTextureLit(litTex);
    }

    /**
     * Returns the board cell index for a world position.
     *
     * While all positions are 2-dimensional, the dimensions to
     * the board are symmetric. This allows us to use the same
     * method to convert an x coordinate or a y coordinate to
     * a cell index.
     *
     * @param f Screen position coordinate
     *
     * @return the board cell index for a screen position.
     */
    public int worldToBoard(float f) {
        return (int)(f / getTileSize());
    }

    /**
     * Returns true if a world location is in bounds of the board
     *
     * @param x The x value in screen coordinates
     * @param y The y value in screen coordinates
     *
     * @return true if a world location is safe
     */
    public boolean isBoundsAtWorld(float x, float y) {
        int bx = worldToBoard(x);
        int by = worldToBoard(y);
        return inBounds(bx, by);
    }

    /**
     * Draws the board to the given canvas.
     *
     * This method draws all of the tiles in this board. It should be the first drawing
     * pass in the GameEngine.
     *
     * @param canvas the drawing context
     */
    public void draw(GameCanvas canvas) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                drawTile(x, y, canvas);
            }
        }
    }

    /**
     * Draws the individual tile at position (x,y).
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    private void drawTile(int x, int y, GameCanvas canvas) {
        // TODO
    }

    /**
     * Returns the world position coordinate for a board cell index.
     *
     * Note this is the coordinate of the bottom left corner of the tile.
     *
     * While all positions are 2-dimensional, the dimensions to
     * the board are symmetric. This allows us to use the same
     * method to convert an x coordinate or a y coordinate to
     * a cell index.
     *
     * @param n Tile cell index
     *
     * @return the screen position coordinate for a board cell index.
     */
    public float boardToWorld(int n) {
        return (float) (n * getTileSize());
    }

    /**
     * Returns true if the tile is Walkable.
     *
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if the tile is walkable.
     */
    public boolean isWalkable(int x, int y) {
        if (!inBounds(x, y)) {
            return false;
        }

        return getTile(x, y).isWalkable();
    }

    /**
     * Sets a tile as walkable or not.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setWalkable(int x, int y, boolean walkable) {
        if (!inBounds(x,y)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return;
        }
        getTile(x, y).setWalkable(true);
    }

    /**
     * Returns true if the tile is lit by moonlight.
     *
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if the tile is lit.
     */
    public boolean isLit(int x, int y) {
        if (!inBounds(x, y)) {
            return false;
        }

        return getTile(x, y).isLit();
    }

    /**
     * Sets a tile as lit or not.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setLit(int x, int y, boolean lit) {
        if (!inBounds(x,y)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return;
        }
        getTile(x, y).setLit(lit);
    }

    /**
     * Returns the type of a tile.
     *
     * Null if out of bounds
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if the tile is walkable.
     */
    public Tile.TileType getTileType (int x, int y) {
        Tile tile = getTile(x, y);

        if (tile == null) {
            return null;
        } else {
            return tile.getType();
        }
    }

    /**
     * Sets the type of a tile.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setTileType(int x, int y, Tile.TileType type) {
        if (!inBounds(x,y)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return;
        }
        getTile(x, y).setType(type);
    }

    /**
     * Returns true if the given position is a valid tile
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if the given position is a valid tile
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /**
     * Returns true if the tile has been visited.
     *
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if the tile has been visited.
     */
    public boolean isVisited(int x, int y) {
        if (!inBounds(x, y)) {
            return false;
        }

        return getTile(x, y).isVisited();
    }

    /**
     * Marks a tile as visited.
     *
     * A marked tile will return true for isVisited(), until a call to clearMarks().
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setVisited(int x, int y) {
        if (!inBounds(x,y)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return;
        }
        getTile(x, y).setVisited(true);
    }

    /**
     * Returns true if the tile is a goal.
     *
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if the tile is a goal.
     */
    public boolean isGoal(int x, int y) {
        if (!inBounds(x, y)) {
            return false;
        }

        return getTile(x, y).isGoal();
    }

    /**
     * Marks a tile as a goal.
     *
     * A marked tile will return true for isGoal(), until a call to clearMarks().
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setGoal(int x, int y) {
        if (!inBounds(x,y)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return;
        }
        getTile(x, y).setGoal(true);
    }

    /** Returns the radius (height or width, should be the same) of
     * the tiles, in pixels
     *
     * @return the radius of tiles
     */
    public float getRadius() {
        return radius;
    }

    /** Sets the radius (height or width, should be the same) of
     * the tiles, in pixels
     *
     * @param radius the value to set
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * Returns the position of the given tile (center pixel)
     *
     * Returns null if that position is out of bounds.
     *
     * @param x x info for the tile
     * @param y y info for the tile
     *
     * @return the position of the given tile
     */
    public Vector2 getTilePosition(int x, int y) {
        if (!inBounds(x, y)) {
            return null;
        }
        return getTile(x,y).getPosition();
    }

    /**
     * Clears all marks on the board.
     *
     * This method should be done at the beginning of any pathfinding round.
     */
    public void clearMarks() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile tile = getTile(x, y);
                tile.setVisited(false);
                tile.setGoal(false);
            }
        }
    }
}