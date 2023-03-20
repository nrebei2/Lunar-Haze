package infinityx.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class AStar {

    public static List<Node> findPath(Node startNode, Node goalNode) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        List<Node> closedSet = new ArrayList<>();

        startNode.gScore = 0;
        startNode.fScore = startNode.getHeuristicCost(goalNode);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current == goalNode) {
                return reconstructPath(current);
            }

            closedSet.add(current);

            for (Edge neighborEdge : current.edges) {
                Node neighbor = neighborEdge.to;
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double tentativeGScore = current.gScore + neighborEdge.cost;

                if (!openSet.contains(neighbor) || tentativeGScore < neighbor.gScore) {
                    neighbor.cameFrom = current;
                    neighbor.gScore = tentativeGScore;
                    neighbor.fScore = tentativeGScore + neighbor.getHeuristicCost(goalNode);

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null;  // Path not found
    }

    private static List<Node> reconstructPath(Node current) {
        List<Node> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = current.cameFrom;
        }
        Collections.reverse(path);
        return path;
    }

}

class Node implements Comparable<Node> {
    public List<Edge> edges;
    public Node cameFrom;
    public double gScore = Double.POSITIVE_INFINITY;
    public double fScore = Double.POSITIVE_INFINITY;

    public float x;

    public float y;

    public Node(float x, float y, List<Edge> edges ) {
        this.x = x;
        this.y = y;
        this.edges = edges;
    }

    public double getHeuristicCost(Node goal) {
        // This implementation uses Euclidean distance as the heuristic
        double dx = this.x - goal.x;
        double dy = this.y - goal.y;
        return Math.sqrt(dx*dx + dy*dy);
    }

    // Compare nodes based on fScore
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.fScore, other.fScore);
    }
}

class Edge {
    public Node to;
    public double cost;

    public Edge(Node to, double cost) {
        this.to = to;
        this.cost = cost;
    }
}



