package infinityx.lunarhaze;

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
    /** The tile grid (with above dimensions) */
    private Tile[] tiles;

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
    private Tile getTileState(int x, int y) {
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
//        return width;
    }

    /**
     * Returns the number of tiles vertically across the board.
     *
     * @return the number of tiles vertically across the board.
     */
    public int getHeight() {
//        return height;

    }

    /**
     * Returns the size of the tile texture.
     *
     * @return the size of the tile texture.
     */
    public int getTileSize() {
//        return TILE_WIDTH;
    }

    /**
     * Returns the amount of spacing between tiles.
     *
     * @return the amount of spacing between tiles.
     */
    public float getTileSpacing() {
//        return TILE_SPACE;
    }


    // Drawing information
    /**
     * Returns the textured mesh for each tile.
     *
     * We only need one mesh, as all tiles look (mostly) the same.
     *
     * @return the textured mesh for each tile.
     */
    public TexturedMesh getTileMesh() {
        return tileMesh;
    }

    /**
     * Sets the textured mesh for each tile.
     *
     * We only need one mesh, as all tiles look (mostly) the same.
     *
     * @param mesh the textured mesh for each tile.
     */
    public void setTileMesh(TexturedMesh mesh) {
//        tileMesh = mesh;
    }


    // COORDINATE TRANSFORMS
    // The methods are used by the physics engine to coordinate the
    // Ships and Photons with the board. You should not need them.

    /**
     * Returns true if a screen location is safe (i.e. there is a tile there)
     *
     * @param x The x value in screen coordinates
     * @param y The y value in screen coordinates
     *
     * @return true if a screen location is safe
     */
    public boolean isSafeAtScreen(float x, float y) {
//        int bx = screenToBoard(x);
//        int by = screenToBoard(y);
//        return x >= 0 && y >= 0
//                && x < width * (getTileSize() + getTileSpacing()) - getTileSpacing()
//                && y < height * (getTileSize() + getTileSpacing()) - getTileSpacing()
//                && !getTileState(bx, by).falling;
    }

    /**
     * Returns true if a tile location is safe (i.e. there is a tile there)
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if a screen location is safe
     */
    public boolean isSafeAt(int x, int y) {
//        return x >= 0 && y >= 0 && x < width && y < height
//                && !getTileState(x, y).falling;
    }

    // GAME LOOP
    // This performs any updates local to the board (e.g. animation)

    /**
     * Draws the board to the given canvas.
     *
     * This method draws all of the tiles in this board. It should be the first drawing
     * pass in the GameEngine.
     *
     * @param canvas the drawing context
     */
    public void draw(GameCanvas canvas) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                drawTile(x, y, canvas);
            }
        }
    }

    /**
     * Draws the individual tile at position (x,y).
     *
     * Fallen tiles are not drawn.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    private void drawTile(int x, int y, GameCanvas canvas) {
//        TileState tile = getTileState(x, y);
//
//        // Don't draw tile if it's fallen off the screen
//        if (tile.fallAmount >= 0.95f * MAX_FALL_AMOUNT) {
//            return;
//        }
//
//        // Compute drawing coordinates
//        float sx = boardToScreen(x);
//        float sy = boardToScreen(y);
//        float sz = tile.fallAmount;
//        float a = 0.1f * tile.fallAmount;
//
//        // You can modify the following to change a tile's highlight color.
//        // BASIC_COLOR corresponds to no highlight.
//        ///////////////////////////////////////////////////////
//
//        tileMesh.setColor(BASIC_COLOR);
//        if (tile.power) {
//            tileMesh.setColor(POWER_COLOR);
//        }
//        //if (tile.goal) {
//        //	tileMesh.setColor(Color.BLACK);
//        //}
//
//        ///////////////////////////////////////////////////////
//
//        // Draw
//        canvas.drawTile(tileMesh, sx, sy, sz, a);
    }

    // METHODS FOR LAB 2

    // CONVERSION METHODS (OPTIONAL)
    // Use these methods to convert between tile coordinates (int) and
    // world coordinates (float).

    /**
     * Returns the board cell index for a screen position.
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
    public int screenToBoard(float f) {
//        return (int)(f / (getTileSize() + getTileSpacing()));
    }

    /**
     * Returns the screen position coordinate for a board cell index.
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
    public float boardToScreen(int n) {
//        return (float) (n + 0.5f) * (getTileSize() + getTileSpacing());
    }

    /**
     * Returns the distance to the tile center in screen coordinates.
     *
     * This method is an implicit coordinate transform. It takes a position (either
     * x or y, as the dimensions are symmetric) in screen coordinates, and determines
     * the distance to the nearest tile center.
     *
     * @param f Screen position coordinate
     *
     * @return the distance to the tile center
     */
    public float centerOffset(float f) {
//        float paddedTileSize = getTileSize() + getTileSpacing();
//        int cell = screenToBoard(f);
//        float nearestCenter = (cell + 0.5f) * paddedTileSize;
//        return f - nearestCenter;
    }

    // PATHFINDING METHODS (REQUIRED)
    // Use these methods to implement pathfinding on the board.

    /**
     * Returns true if the given position is a valid tile
     *
     * It does not check whether the tile is live or not.  Dead tiles are still valid.
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
//        if (!inBounds(x, y)) {
//            return false;
//        }
//
//        return getTileState(x, y).visited;
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
//        if (!inBounds(x,y)) {
//            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
//            return;
//        }
//        getTileState(x, y).visited = true;
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
//        if (!inBounds(x, y)) {
//            return false;
//        }
//
//        return getTileState(x, y).goal;
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
//        if (!inBounds(x,y)) {
//            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
//            return;
//        }
//        getTileState(x, y).goal = true;
    }

    /**
     * Clears all marks on the board.
     *
     * This method should be done at the beginning of any pathfinding round.
     */
    public void clearMarks() {
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                TileState state = getTileState(x, y);
//                state.visited = false;
//                state.goal = false;
//            }
//        }
    }
}