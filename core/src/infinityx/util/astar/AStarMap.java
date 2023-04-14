package infinityx.util.astar;

import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.LevelContainer;

/** Tiled map */
public class AStarMap {
    private Node[][] map;

    private final int width;
    private final int height;
    private final float gridSize;

    /**
     * Creates a new tiled map of the given size
     * @param width Map width in grids
     * @param height Map height in grids
     * @param gridSize width and height of each grid in world size
     */
    public AStarMap(int width, int height, float gridSize) {
        this.width = width;
        this.height = height;
        this.gridSize = gridSize;

        map = new Node[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float wx = (float) ((x + 0.5) * gridSize);
                float wy = (float) ((y + 0.5) * gridSize);
                map[y][x] = new Node(this, x, y, wx, wy);
            }
        }
    }


    /**
     * @return the map grid cell x-index for a world x-position
     */
    public int worldToGridX(float x) {
        return (int) (x / gridSize);
    }

    /**
     * @return the map grid cell y-index for a world y-position
     */
    public int worldToGridY(float y) {
        return (int) (y / gridSize);
    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * @param x units along horizontal
     * @param y units along vertical
     * @return Node at position (x, y)
     */
    public Node getNodeAt(int x, int y) {
        return map[y][x];
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                stringBuilder.append(map[y][x].isObstacle ? "#" : " ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
