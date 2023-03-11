package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntSet;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Class represents a 2D grid of tiles.
 * Wrapper around tile data
 */
public class Board {
    // Instance attributes
    /**
     * The board width (in number of tiles)
     */
    private final int width;
    /**
     * The board height (in number of tiles)
     */
    private final int height;
    /**
     * Height/width of the tiles, in pixels
     */
    private float radius;
    /**
     * The tile grid (with above dimensions)
     */
    private final Tile[] tiles;

    /**
     * Tile height and width in world length (it is actually a square)
     */
    public static final float TILE_HEIGHT = 1;
    public static final float TILE_WIDTH = TILE_HEIGHT;

    /**
     * Tile height and width in screen (pixel) length
     */
    public static final int TILE_WIDTH_SCREEN = 128;
    public static final int TILE_HEIGHT_SCREEN = TILE_WIDTH_SCREEN * 3 / 4;

    /** Cache holding set of moonlight tiles, (n, m) is lit iff m*width + n is in moonlightTiles */
    private IntSet moonlightTiles;

    /**
     * Creates a new board of the given size
     *
     * @param width  Board width in tiles
     * @param height Board height in tiles
     */
    public Board(int width, int height) {
        moonlightTiles = new IntSet();
        this.width = width;
        this.height = height;
        tiles = new Tile[width * height];
        for (int ii = 0; ii < tiles.length; ii++) {
            tiles[ii] = new Tile();

        }
    }

    /**
     * Returns the tile for the given position (INTERNAL USE ONLY)
     * <p>
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

    // Drawing information

    /**
     * Returns the textured mesh for a tile.
     * <p>
     * Gives either lit or unlit texture depending on tile state.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     * @return the textured mesh for a given tile.
     */
    public Texture getTileTexture(int x, int y) {
        Tile tile = getTile(x, y);
        if (tile == null) {
            return null;
        }
        /*if (tile.isLit()) {
            return tile.getTileTextureLit();
        }*/

        // Commented the above out because we added ambient lighting. LMK if it should be changed back
        return tile.getTileTextureUnlit();

    }

    /**
     * Sets both lit and unlit textures for a tile.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setTileTexture(int x, int y, Texture unlitTex, Texture litTex) {
        Tile tile = getTile(x, y);
        if (tile == null) {
            return;
        }
        tile.setTileTextureUnlit(unlitTex);
        tile.setTileTextureLit(litTex);
    }

    /**
     * Returns the board cell index for a world position.
     * <p>
     * While all positions are 2-dimensional, the dimensions to
     * the board are symmetric. This allows us to use the same
     * method to convert an x coordinate or a y coordinate to
     * a cell index.
     *
     * @return the board cell index for a screen position.
     */
    public Vector2 worldToBoard(float x, float y) {
        return new Vector2((int) (x / TILE_WIDTH), (int) (y / TILE_HEIGHT));
    }

    /**
     * Returns true if a world location is in bounds of the board
     *
     * @param x The x value in screen coordinates
     * @param y The y value in screen coordinates
     * @return true if a world location is safe
     */
    public boolean isBoundsAtWorld(float x, float y) {
        Vector2 boardPos = worldToBoard(x, y);
        return inBounds(Math.round(boardPos.x), Math.round(boardPos.y));
    }

    /**
     * Returns true if a world location is in bounds of the board
     *
     * @param x The x value in screen coordinates
     * @param y The y value in screen coordinates
     * @return true if a world location is safe
     */
    private boolean search(float x, float y, ArrayList<Vector2> tinted_tiles) {
        for (Vector2 vec : tinted_tiles) {
            if (vec.x == x && vec.y == y) return true;
        }
        return false;
    }

    /**
     * Draws the board to the given canvas.
     * <p>
     * This method draws all of the tiles in this board. It should be the first drawing
     * pass in the GameEngine.
     *
     * @param canvas the drawing context
     */
    public void draw(GameCanvas canvas) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                drawTile(x, y, canvas, Color.WHITE);
            }
        }
    }

    /**
     * Draws the individual tile at position (x,y).
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    private void drawTile(int x, int y, GameCanvas canvas, Color tint) {
        Texture tiletexture = getTileTexture(x, y);
        canvas.draw(
                tiletexture, Color.WHITE, tiletexture.getWidth() / 2, tiletexture.getHeight() / 2,
                GameCanvas.WorldToScreenX(boardCenterToWorld(x, y).x), GameCanvas.WorldToScreenY(boardCenterToWorld(x, y).y), 0.0f,
                TILE_WIDTH_SCREEN / tiletexture.getWidth(), TILE_HEIGHT_SCREEN / tiletexture.getHeight()
        );
    }

    /**
     * Returns null if that position is out of bounds.
     * <p>
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     * @return the world position coordinates of the bottom left corner of the tile.
     */
    public Vector2 boardToWorld(int x, int y) {
        if (!inBounds(x, y)) {
            return null;
        }
        return new Vector2(x * TILE_WIDTH, y * TILE_HEIGHT);
    }

    /**
     * Returns null if that position is out of bounds.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     * @return the center world position of the given tile
     */
    public Vector2 boardCenterToWorld(int x, int y) {
        if (!inBounds(x, y)) {
            return null;
        }
        return boardToWorld(x, y).add(0.5f * TILE_WIDTH, 0.5f * TILE_HEIGHT);
    }


    /**
     * Returns true if the tile is Walkable.
     * <p>
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
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
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return;
        }
        getTile(x, y).setWalkable(true);
    }

    /**
     * Returns true if the tile is lit by moonlight.
     * <p>
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
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
     * You should probably be calling setLit in LevelContainer instead if you want light to react as well.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setLit(int x, int y, boolean lit) {
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return;
        }
        getTile(x, y).setLit(lit);

        if (lit) {
            moonlightTiles.add(x + y * width);
        } else {
            moonlightTiles.remove(x + y * width);
        }
    }

    /**
     * Returns the type of a tile.
     * <p>
     * Null if out of bounds
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     * @return true if the tile is walkable.
     */
    public Tile.TileType getTileType(int x, int y) {
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
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return;
        }
        getTile(x, y).setType(type);
    }

    /**
     * Returns true if the given position is a valid tile
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     * @return true if the given position is a valid tile
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /**
     * Returns true if the tile has been visited.
     * <p>
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
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
     * <p>
     * A marked tile will return true for isVisited(), until a call to clearMarks().
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setVisited(int x, int y) {
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return;
        }
        getTile(x, y).setVisited(true);
    }

    /**
     * Returns true if the tile is a goal.
     * <p>
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
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
     * <p>
     * A marked tile will return true for isGoal(), until a call to clearMarks().
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setGoal(int x, int y) {
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return;
        }
        getTile(x, y).setGoal(true);
    }

    /**
     * Sets a tile as visible or not.
     *
     * @param x       The x index for the Tile cell
     * @param y       The y index for the Tile cell
     * @param visible to be or not to be
     */
    public void setVisible(int x, int y, boolean visible) {
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return;
        }
        getTile(x, y).setVisible(visible);
    }

    /**
     * Returns the radius (height or width, should be the same) of
     * the tiles, in pixels
     *
     * @return the radius of tiles
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Sets the radius (height or width, should be the same) of
     * the tiles, in pixels
     *
     * @param radius the value to set
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }


    /**
     * Clears all marks on the board.
     * <p>
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

    /**
     * Sets all tiles on the board to not visible by enemies.
     * <p>
     * This method should be done before setting tiles as visible every update.
     */
    public void clearVisibility() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                setVisible(x, y, false);
            }
        }
    }

    /**
     * Please don't modify the return value
     *
     * @return set of moonlight tile indices
     */
    public IntSet getMoonlightTiles() {
       return moonlightTiles;
    }
}