package infinityx.lunarhaze;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Queue;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.EnemyList;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.lunarhaze.physics.RaycastInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

// TODO: move all this stuff into AI controller, EnemyController should hold other enemy actions
public class EnemyController {

    // Constants for the control codes
    // We would normally use an enum here, but Java enums do not bitmask nicely
    /**
     * Do not do anything
     */
    public static final int CONTROL_NO_ACTION = 0x00;
    /**
     * Move the ship to the left
     */
    public static final int CONTROL_MOVE_LEFT = 0x01;
    /**
     * Move the ship to the right
     */
    public static final int CONTROL_MOVE_RIGHT = 0x02;
    /**
     * Move the ship to the up
     */
    public static final int CONTROL_MOVE_UP = 0x04;
    /**
     * Move the ship to the down
     */
    public static final int CONTROL_MOVE_DOWN = 0x08;
    /**
     * Fire the ship weapon
     */
    public static final int CONTROL_ATTACK = 0x10;

    private static final float DETECT_DIST = 2;
    private static final float DETECT_DIST_MOONLIGHT = 5;
    private static final float CHASE_DIST = 3f;
    private static final int ATTACK_DIST = 1;

    /**
     * Enumeration to encode the finite state machine.
     */
    public enum FSMState {
        /**
         * The enemy just spawned
         */
        SPAWN,
        /**
         * The enemy is patrolling around a set path
         */
        PATROL,
        /**
         * The enemy is wandering around where target is last seen
         */
        WANDER,
        /**
         * The enemy has a target, but must get closer
         */
        CHASE,
        /**
         * The enemy has a target and is attacking it
         */
        ATTACK,
        /**
         * The enemy lost its target and is returnng to its patrolling path
         */
        RETURN,
    }

    private enum Detection {
        /**
         * The enemy can only detect the player in a straight line
         */
        LIGHT,
        /**
         * The enemy can detect the player in an area
         * The enemy can detect the player in an area
         */
        AREA,
        /**
         * The enemy can  detect the player in a half circle
         */
        MOON
    }
    /**
     * Enumeration to describe what direction the enemy is facing
     */


    // Instance Attributes
    /**
     * The enemy being controlled by this AIController
     */
    private final Enemy enemy;

    private Detection detection;

    /**
     * The other enemies; used to find targets
     */
    private final EnemyList enemies;

    /**
     * The game board; used for pathfinding
     */
    private final Board board;
    /**
     * The ship's current state in the FSM
     */
    private FSMState state;
    /**
     * The target ship (to chase or attack).
     */
    private final Werewolf target;


    /**
     * The enemy next action (may include firing).
     */
    private int move; // A ControlCode

    /** The direction the enemy is facing*/

    /**
     * The number of ticks since we started this controller
     */
    private long ticks;


    /**
     * Creates an EnemyController for the enemy with the given id.
     *
     * @param id      The unique ship identifier
     * @param board   The game board (for pathfinding)
     * @param enemies The list of enemies (for detection)
     */
    public EnemyController(int id, Werewolf target, EnemyList enemies, Board board) {
        this.enemy = enemies.get(id);
        this.board = board;
        this.target = target;
        this.state = FSMState.SPAWN;
        this.enemies = enemies;
        this.detection = Detection.LIGHT;

    }

    /**
     * Returns the distance between two board coordinates given two world coordinates
     *
     * @param x  x-coord in screen coord of first ship
     * @param y  y-coord in screen coord of first ship
     * @param tx x-coord in screen coord of second ship
     * @param tx y-coord in screen coord of second ship
     * @return
     */
    private float boardDistance(float x, float y, float tx, float ty) {
        int dx = board.worldToBoardX(tx) - board.worldToBoardX(x);
        int dy = board.worldToBoardX(ty) - board.worldToBoardX(y);
        return (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }

    public RaycastInfo raycast(GameObject requestingObject, Vector2 point1, Vector2 point2, World world) {
        RaycastInfo callback = new RaycastInfo(requestingObject);
        world.rayCast(callback, new Vector2(point1.x, point1.y), new Vector2(point2.x, point2.y));
        return callback;
    }

    public boolean lightDetect(LevelContainer container){
        Werewolf player = container.getPlayer();

        Vector2 point1 = enemy.getPosition();
        ConeSource flashlight = enemy.getFlashlight();
        float light_dis = flashlight.getDistance();

        float vx = enemy.getVX();
        float vy = enemy.getVY();
        Vector2 enemy_direction = new Vector2(vx,vy).nor();


        Vector2 direction = new Vector2(player.getX()-point1.x, player.getY()-point1.y).nor();
        Vector2 point2 = new Vector2(point1.x+light_dis*direction.x, player.getY()+light_dis*direction.y);
        RaycastInfo info = raycast(enemy, point1, point2, container.getWorld());

//        System.out.println(enemy_direction.dot(direction));
        double degree = Math.toDegrees(Math.acos(enemy_direction.dot(direction)));


        return info.hit && info.hitObject == player && degree <= flashlight.getConeDegree();
    }


    /**
     * Returns true if we detected the player (when the player walks in line of sight)
     *
     * @return true if we can both fire and hit our target
     */
    private boolean detectedPlayer(LevelContainer container) {
        Enemy.Direction direction = enemy.getDirection();
        if (direction == null) {
            return false;
        }

        /**Enemy must be in a line and less than detection distance away*/
        if (detection == Detection.LIGHT) {
            return lightDetect(container);
        }
        /**Enemy only need to be less than detection distance away, enemy can see in an area*/
        else if (detection == Detection.AREA) {
            return boardDistance(enemy.getX(), enemy.getY(), target.getX(),
                    target.getY()) <= DETECT_DIST;
        } else if (detection == Detection.MOON) {
            System.out.println("detection moon");
//            switch (direction) {
//                case NORTH:
//                    blind = target.getY() < enemy.getY();
//                    break;
//                case SOUTH:
//                    blind = target.getY() > enemy.getY();
//                    break;
//                case EAST:
//                    blind = target.getX() < enemy.getX();
//                    break;
//                case WEST:
//                    blind = target.getX() > enemy.getX();
//                    break;
//                default:
//                    throw new NotImplementedException();
//            }
            return boardDistance(enemy.getX(), enemy.getY(), target.getX(),
                    target.getY()) <= DETECT_DIST_MOONLIGHT;
        } else {
            return false;
        }
    }

    /**
     * Sets the tiles seen by this enemy's line of sight as visible.
     */
    public void setVisibleTiles() {
        int x = board.worldToBoardX(enemy.getPosition().x);
        int y = board.worldToBoardY(enemy.getPosition().y);

        switch (enemy.getDirection()) {
            case NORTH:
                board.setVisible(x, y - 1, true);
                board.setVisible(x, y - 2, true);
                break;
            case SOUTH:
                board.setVisible(x, y + 1, true);
                board.setVisible(x, y + 2, true);
                break;
            case WEST:
                board.setVisible(x - 1, y, true);
                board.setVisible(x - 2, y, true);
                break;
            case EAST:
                board.setVisible(x + 1, y, true);
                board.setVisible(x + 2, y, true);
                break;
            default:
                break;
        }
    }

    /**
     * Returns true if we can hit a target from here.
     *
     * @param x The x-index of the source tile
     * @param y The y-index of the source tile
     * @return true if we can hit a target from here.
     */
    private boolean canHitTargetFrom(int x, int y) {
        if (!board.isWalkable(x, y) || target == null) {
            return false;
        }
        int pos_x = board.worldToBoardX(target.getX());
        int pos_y = board.worldToBoardX(target.getY());
        return boardDistance(x, y, pos_x, pos_y) <= ATTACK_DIST;
    }

    /**
     * Returns true if we can both fire and hit our target
     * <p>
     * If we can fire now, and we could hit the target from where we are,
     * we should hit the target now.
     *
     * @return true if we can both fire and hit our target
     */
    private boolean canHitTarget() {
        return canHitTargetFrom(board.worldToBoardX(enemy.getX()), board.worldToBoardX(enemy.getY()));
    }

    private boolean canChase() {
        return (boardDistance(enemy.getX(), enemy.getY(), target.getX(), target.getY()) <= CHASE_DIST);
    }

    /**
     * Returns the action selected by this InputController
     * <p>
     * The returned int is a bit-vector of more than one possible input
     * option. This is why we do not use an enumeration of Control Codes;
     * Java does not (nicely) provide bitwise operation support for enums.
     * <p>
     * This function tests the environment and uses the FSM to chose the next
     * action of the ship. This function SHOULD NOT need to be modified.  It
     * just contains code that drives the functions that you need to implement.
     *
     * @return the action selected by this InputController
     */
    public int getAction(LevelContainer container) {
        // Increment the number of ticks.
        ticks++;

        // Do not need to rework ourselves every frame. Just every 10 ticks.
        if ((enemy.getId() + ticks) % 10 == 0) {
            // Process the FSM
            changeStateIfApplicable(container);
            changeDetectionIfApplicable();

            // Pathfinding
            markGoalTiles();
            move = getMoveAlongPathToGoalTile();
        }

        int action = move;

        // If we're attacking someone and we can shoot him now, then do so.
        if (state == FSMState.ATTACK && canHitTarget()) {
            action |= CONTROL_ATTACK;
        }

        return action;
    }

    /**
     * Change the state of the enemy.
     * <p>
     * A Finite State Machine (FSM) is just a collection of rules that,
     * given a current state, and given certain observations about the
     * environment, chooses a new state. For example, if we are currently
     * in the ATTACK state, we may want to switch to the CHASE state if the
     * target gets out of range.
     */
    private void changeStateIfApplicable(LevelContainer container) {
        switch (state) {
            case SPAWN:
                state = FSMState.PATROL;
                break;
            case PATROL:
                if (detectedPlayer(container)) {
                    state = FSMState.CHASE;
                    enemy.setIsAlerted(true);
                }
                break;
            case CHASE:
//                if (canHitTarget()){
//                    state = FSMState.ATTACK;
//                }
                if (!canChase() && !target.isOnMoonlight()) {
                    state = FSMState.PATROL;
                    enemy.setIsAlerted(false);
                }
                break;
//            case WANDER:
//                break;
            case ATTACK:
                if (!canHitTarget()) {
                    state = FSMState.CHASE;
                }
                break;
            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                state = FSMState.WANDER; // If debugging is off
                break;
        }
    }

    private void changeDetectionIfApplicable() {
        if (target.isOnMoonlight()) {
            detection = Detection.MOON;
        } else if (state == FSMState.CHASE) {
            detection = Detection.AREA;
        } else {
            detection = Detection.LIGHT;
        }


    }

    private void markGoalTiles() {
        board.clearMarks();
        Vector2 cur_pos = enemy.getPosition();
        int curr_x = board.worldToBoardX(cur_pos.x);
        int curr_y = board.worldToBoardY(cur_pos.y);
        boolean setGoal = false; // Until we find a goal
        switch (state) {
            case SPAWN:
                break;
            case PATROL:
                Vector2 pos;
                if (enemy.getCurrentPatrol().x == curr_x && enemy.getCurrentPatrol().y == curr_y) {
                    pos = enemy.getNextPatrol();
                } else {
                    pos = enemy.getCurrentPatrol();
                }
                board.setGoal((int) pos.x, (int) pos.y);
                setGoal = true;
                break;
//            case WANDER:
//
//                pos = board.worldToBoard(enemy.getX(), enemy.getY());
//                int x = (int)pos.x;
//                int y = (int)pos.y;
//                //#endregion
//                for (int i = -1; i < 2; i++ ){
//                    for (int j = -1 ; j <2 ; j++){
//                        if (board.isWalkable(x+i, y+j) && Math.random() <= (double)(1/9) && !setGoal && board.inBounds(x +i, y+j) ){
//                            board.setGoal(x+i, y+j);
//                            setGoal = true;
//                        }
//                    }
//                }
//                break;
            case CHASE:
                if (target != null) {
                    board.setGoal(board.worldToBoardX(target.getX()), board.worldToBoardX(target.getY()));
                    setGoal = true;
                }
                break;
            case ATTACK:
                if (target != null) {
                    int target_x = board.worldToBoardX(target.getX());
                    int target_y = board.worldToBoardX(target.getY());
                    //#endregion
                    for (int i = -4; i < 5; i++) {
                        for (int j = -4; j < 5; j++) {
                            if (board.isWalkable(target_x + i, target_y + j) && canHitTargetFrom(target_x + i, target_y + i)
                                    && board.inBounds(target_x + i, target_y + j)) {
                                board.setGoal(target_x + i, target_y + j);
                                setGoal = true;
                            }
                        }
                    }

                }
                break;
        }
        if (!setGoal) {
            board.setGoal(board.worldToBoardX(enemy.getX()), board.worldToBoardX(enemy.getY()));
        }
    }


    /**
     * Returns a movement direction that moves towards a goal tile.
     * <p>
     * This is one of the longest parts of the assignment. Implement
     * breadth-first search (from 2110) to find the best goal tile
     * to move to. However, just return the movement direction for
     * the next step, not the entire path.
     * <p>
     * The value returned should be a control code.  See PlayerController
     * for more information on how to use control codes.
     *
     * @return a movement direction that moves towards a goal tile.
     */
    private int getMoveAlongPathToGoalTile() {
        //#region PUT YOUR CODE HERE
        int x = board.worldToBoardX(enemy.getX());
        int y = board.worldToBoardY(enemy.getY());
        if (board.isGoal(x, y)) {
            return CONTROL_NO_ACTION;
        }

        Queue<TileData> queue = new Queue<>();


        board.setVisited(x, y);
        queue.addLast(new TileData(x, y));
        while (!queue.isEmpty()) {
            TileData head = queue.first();
            queue.removeFirst();
            if (board.isGoal(head.x, head.y)) {
                return getDirection(head);
            }
            for (int[] direction : directions) {
                if (!board.isVisited(head.x + direction[0], head.y + direction[1]) && board.isWalkable(head.x + direction[0], head.y + direction[1])) {
                    TileData next = new TileData(head.x + direction[0], head.y + direction[1]);
                    board.setVisited(next.getX(), next.getY());
                    next.setPrevDirection(new int[]{direction[0], direction[1]});
                    next.setPrev(head);
                    queue.addLast(next);
                }
            }
        }
        return CONTROL_NO_ACTION;
        //#endregion
    }
//
//    private int getMoveAlongPathToGoalTile() {
//        //#region PUT YOUR CODE HERE
//        int x = board.worldToBoardX(enemy.getX());
//        int y = board.worldToBoardY(enemy.getY());
//        if (board.isGoal(x, y)) {
//            return CONTROL_NO_ACTION;
//        }
//
//        PriorityQueue<TileData> openSet = new PriorityQueue<TileData>();
//        HashSet<TileData> closedSet = new HashSet<TileData>();
//        HashMap<TileData, TileData> cameFrom = new HashMap<TileData, TileData>();
//        HashMap<TileData, Double> gScore = new HashMap<TileData, Double>();
//        HashMap<TileData, Double> fScore = new HashMap<TileData, Double>();
//
//
//        board.setVisited(x, y);
//        gScore.put(, 0.0);
//        fScore.put(start, heuristic(start, end));
//        openSet.add(start);
//
//        while (!openSet.isEmpty()) {
//            Node current = openSet.poll();
//
//            if (current == end) {
//                return reconstructPath(cameFrom, end);
//            }
//
//            closedSet.add(current);
//
//            for (Node neighbor : current.getNeighbors()) {
//                if (closedSet.contains(neighbor)) {
//                    continue;
//                }
//
//                double tentativeGScore = gScore.get(current) + current.distanceTo(neighbor);
//
//                if (!openSet.contains(neighbor) || tentativeGScore < gScore.get(neighbor)) {
//                    cameFrom.put(neighbor, current);
//                    gScore.put(neighbor, tentativeGScore);
//                    fScore.put(neighbor, tentativeGScore + heuristic(neighbor, end) + hidingSpotHeuristic(neighbor));
//
//                    if (!openSet.contains(neighbor)) {
//                        openSet.add(neighbor);
//                    }
//                }
//            }
//        }
//
//        return null;
//    }
//
//    private double heuristic(Node a, Node b) {
//        return ;
//    }


    private int getDirection(TileData t) {
        while (t.prev.prev != null) {
            t = t.prev;
        }
        if (t.prevDirection[0] == -1) {
            return CONTROL_MOVE_LEFT;
        } else if (t.prevDirection[0] == 1) {
            return CONTROL_MOVE_RIGHT;
        } else if (t.prevDirection[1] == -1) {
            return CONTROL_MOVE_DOWN;
        } else if (t.prevDirection[1] == 1) {
            return CONTROL_MOVE_UP;
        }
        return CONTROL_NO_ACTION;
    }

    // Add any auxiliary methods or data structures here
    //#region PUT YOUR CODE HERE

    private class TileData {
        private final int x;
        private final int y;

        public int[] prevDirection = null;

        public TileData prev = null;

        private TileData(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setPrevDirection(int[] prev_direction) {
            this.prevDirection = prev_direction;
        }

        public void setPrev(TileData prev) {
            this.prev = prev;
        }
    }

    public final int[][] directions = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

}


