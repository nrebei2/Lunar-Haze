package infinityx.lunarhaze;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.graphics.GameCanvas;

import java.util.ArrayList;


/**
 * Class represents a 2D grid of tiles.
 * Wrapper around tile data
 * TODO: make this extend TiledMap? Useful if we will need layers later on. Tile can then extend StaticTiledMapTile.
 * Ref: https://libgdx.com/wiki/graphics/2d/tile-maps
 */
public class Board {
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
     * Tile height and width in world length.
     * Should be overwritten.
     */
    private final Vector2 tileWorldDim = new Vector2(1, 1);

    /**
     * Tile height and width in screen (pixel) length.
     * Should be overwritten.
     */
    private final Vector2 tileScreenDim = new Vector2(128, 96);

    /**
     * Cache holding the number of tiles with collectable moonlight
     */
    private int moonlightTiles;

    /**
     * Used in editor
     */
    private PreviewTile previewTile;

    public static class PreviewTile {
        // Board (x, y)
        int b_x;
        int b_y;
        // Preview texture
        Texture texture;


        public PreviewTile(int b_x, int b_y, Texture texture) {
            this.b_x = b_x;
            this.b_y = b_y;
            this.texture = texture;
        }
    }

    /**
     * Creates a new board of the given size
     *
     * @param width  Board width in tiles
     * @param height Board height in tiles
     */
    public Board(int width, int height) {
        moonlightTiles = 0;
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
     * Sets tile height and width in screen (pixel) length.
     * Needed for drawing.
     */
    public void setTileScreenDim(int width, int height) {
        this.tileScreenDim.set(width, height);
    }

    /**
     * Sets tile height and width in world length.
     * Needed for drawing.
     */
    public void setTileWorldDim(float width, float height) {
        this.tileWorldDim.set(width, height);
    }

    /**
     * @return tile height and width in world length.
     */
    public Vector2 getTileWorldDim() {
        return this.tileWorldDim;
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
        return tile.getTileTexture();
    }

    /**
     * Sets both lit and unlit textures for a tile.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setTileTexture(int x, int y, Texture tex) {
        Tile tile = getTile(x, y);
        if (tile == null) {
            return;
        }
        tile.setTileTexture(tex);
    }

    /**
     * Returns the board cell index for a world x-position.
     *
     * @return the board cell index for a world x-position.
     */
    public int worldToBoardX(float x) {
        return (int) (x / tileWorldDim.x);
    }

    /**
     * Returns the board cell index for a world y-position.
     *
     * @return the board cell index for a world y-position.
     */
    public int worldToBoardY(float y) {
        return (int) (y / tileWorldDim.y);
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
                drawTile(x, y, canvas);
            }
        }

        if (this.previewTile != null) {
            drawPreview(canvas);
        }
    }

    /**
     * Draws the individual tile at position (x,y).
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    private void drawTile(int x, int y, GameCanvas canvas) {
        // Used for level editor
        if (getTileType(x, y) == Tile.TileType.EMPTY) {
            return;
        }

        Texture tiletexture = getTileTexture(x, y);
        canvas.draw(
                tiletexture, Color.WHITE, tiletexture.getWidth() / 2, tiletexture.getHeight() / 2,
                canvas.WorldToScreenX(boardCenterToWorldX(x)), canvas.WorldToScreenY(boardCenterToWorldY(y)), 0.0f,
                tileScreenDim.x / tiletexture.getWidth(), tileScreenDim.y / tiletexture.getHeight()
        );
    }

    /**
     * Draws red outline for tile placement. Used in level editor.
     *
     * @param canvas
     */
    public void drawOutline(GameCanvas canvas) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                canvas.drawRecOutline(
                        canvas.WorldToScreenX(boardToWorldX(x)), canvas.WorldToScreenY(boardToWorldY(y)),
                        tileScreenDim.x, tileScreenDim.y, Color.RED
                );
            }
        }
    }

    /**
     * Sets the preview tile. Used for the level editor.
     *
     * @param x       The x index for the Tile cell
     * @param y       The y index for the Tile cell
     * @param texture The texture used for preview
     */
    void setPreviewTile(int x, int y, Texture texture) {
        if (!inBounds(x, y)) {
            removePreview();
            return;
        }
        this.previewTile = new PreviewTile(x, y, texture);
    }

    void removePreview() {
        previewTile = null;
    }

    /**
     * Draws a preview texture at tile position (x, y). Used for the level editor.
     *
     * @param canvas
     */
    private void drawPreview(GameCanvas canvas) {
        Texture preview = this.previewTile.texture;
        int x = this.previewTile.b_x;
        int y = this.previewTile.b_y;
        canvas.draw(
                preview, EditorMode.SELECTED_COLOR, preview.getWidth() / 2, preview.getHeight() / 2,
                canvas.WorldToScreenX(boardCenterToWorldX(x)), canvas.WorldToScreenY(boardCenterToWorldY(y)), 0.0f,
                tileScreenDim.x / preview.getWidth(), tileScreenDim.y / preview.getHeight()
        );
    }

    /**
     * @param x The x index for the Tile cell
     * @return the world x-position coordinates of the bottom left corner.
     */
    public float boardToWorldX(int x) {
        return x * tileWorldDim.x;
    }

    /**
     * @param y The y index for the Tile cell
     * @return the world y-position coordinates of the bottom left corner.
     */
    public float boardToWorldY(int y) {
        return y * tileWorldDim.y;
    }

    /**
     * @param x The x index for the Tile cell
     * @return the center world position of the given tile
     */
    public float boardCenterToWorldX(int x) {
        return boardToWorldX(x) + 0.5f * tileWorldDim.x;
    }

    /**
     * @param y The y index for the Tile cell
     * @return the center world position of the given tile
     */
    public float boardCenterToWorldY(int y) {
        return boardToWorldY(y) + 0.5f * tileWorldDim.y;
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
     * <p>
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setLit(int x, int y, boolean lit) {
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return;
        }
        Tile t = getTile(x, y);
        if (t.getSpotLight() == null) {
            Gdx.app.error("Board", "This should never happen! Talk to me if this pops up for you.");
            return;
        }
        getTile(x, y).setLit(lit);
    }

    /**
     * Returns true if the moonlight tile is collectable.
     * <p>
     * A tile position that is not on the board will always evaluate to false.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     * @return true if the moonlight tile is collectable.
     */
    public Boolean isCollectable(int x, int y) {
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return false;
        }
        Tile t = getTile(x, y);

        return t.isCollectable();
    }

    /**
     * Sets a tile as collected (still lit).
     * <p>
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setCollected(int x, int y) {
        setCollectable(x, y, false);
    }

    /**
     * Sets a tile as (moonlight) collectable or not.
     * <p>
     *
     * @param x           The x index for the Tile cell
     * @param y           The y index for the Tile cell
     * @param collectable the player can collect moonlight on this tile (true) or not (false)
     */
    public void setCollectable(int x, int y, boolean collectable) {
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return;
        }
        Tile t = getTile(x, y);

        // update number of collectable moonlight tiles
        // as a safeguard only change if it changes the status of the tile
        if (t.isCollectable() != collectable) moonlightTiles += collectable ? 1 : -1;
        t.setCollectable(collectable);
    }

    /**
     * Attaches light to tile, represents the moonlight on the tile
     * <p>
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setSpotlight(int x, int y, PointLight light) {
        if (!inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
            return;
        }
        getTile(x, y).setSpotLight(light);
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
     * @return the number of tiles on this board with collectable moonlight
     */
    public int getRemainingMoonlight() {
        return moonlightTiles;
    }
}