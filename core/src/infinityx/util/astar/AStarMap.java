package infinityx.util.astar;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.graphics.GameCanvas;

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
    private final Vector2 gridSize;

    /**
     * Creates a new tiled map of the given size
     *
     * @param width    Map width in grids
     * @param height   Map height in grids
     * @param gridSize width and height of each grid in world size
     */
    public AStarMap(int width, int height, Vector2 gridSize) {
        this.width = width;
        this.height = height;
        this.gridSize = gridSize;

        map = new Node[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float wx = (float) ((x + 0.5) * gridSize.x);
                float wy = (float) ((y + 0.5) * gridSize.y);
                map[x][y] = new Node(this, x, y, wx, wy);
            }
        }
    }


    /**
     * @return the map grid cell x-index for a world x-position
     */
    public int worldToGridX(float x) {
        return (int) (x / gridSize.x);
    }

    /**
     * @return the map grid cell y-index for a world y-position
     */
    public int worldToGridY(float y) {
        return (int) (y / gridSize.y);
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
        return map[MathUtils.clamp(x, 0, width - 1)][MathUtils.clamp(y, 0, height - 1)];
    }

    /**
     * @param x World x
     * @param y World y
     * @return Node in which (x, y) is inside. Clamps to edge if out of bounds.
     */
    public Node getNodeAtWorld(float x, float y) {
        return map[MathUtils.clamp(worldToGridX(x), 0, width - 1)][MathUtils.clamp(worldToGridY(y), 0, height - 1)];
    }

    /**
     * DEBUG. Draws each node in the map, red if obstructed, blue otherwise.
     */
    public void drawMap(GameCanvas canvas) {
        canvas.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Node[] row : map) {
            for (Node node : row) {
                canvas.shapeRenderer.setColor(node.isObstacle ? Color.RED : Color.BLUE);
                canvas.shapeRenderer.rect(
                        node.position.x - gridSize.x / 2,
                        node.position.y - gridSize.y / 2,
                        gridSize.x, gridSize.y
                );
                canvas.shapeRenderer.setColor(Color.WHITE);
                for (Connection conn : node.getConnections()) {
                    canvas.shapeRenderer.line(node.position, ((Node) conn.getToNode()).position);
                }
            }
        }
        canvas.shapeRenderer.end();
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
