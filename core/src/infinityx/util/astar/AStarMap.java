package infinityx.util.astar;

/**
 * Grid (tiled) map
 */
public class AStarMap {

    /**
     * The node grid
     */
    private Node[][] map;

    /**
     * Map width in grids
     */
    private final int width;

    /**
     * Map height in grids
     */
    private final int height;
    private final float gridSize;

    /**
     * Creates a new tiled map of the given size
     *
     * @param width    Map width in grids
     * @param height   Map height in grids
     * @param gridSize width and height of each grid in world size
     */
    public AStarMap(int width, int height, float gridSize) {
        this.width = width;
        this.height = height;
        this.gridSize = gridSize;

        map = new Node[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float wx = (float) ((x + 0.5) * gridSize);
                float wy = (float) ((y + 0.5) * gridSize);
                map[x][y] = new Node(this, x, y, wx, wy);
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
     * @param x Number of grids along horizontal
     * @param y Number of grids along vertical
     * @return Node at position (x, y)
     */
    public Node getNodeAt(int x, int y) {
        return map[x][y];
    }

    /**
     * @param x World x
     * @param y World y
     * @return Node in which (x, y) is inside. Null if out of bounds.
     */
    public Node getNodeAtWorld(float x, float y) {
        if (worldToGridX(x) < 0 || worldToGridX(x) >= width) return null;
        if (worldToGridY(y) < 0 || worldToGridY(y) >= height) return null;
        return map[worldToGridX(x)][worldToGridY(y)];
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                stringBuilder.append(map[x][y].isObstacle ? "#" : "O");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
