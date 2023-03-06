package infinityx.lunarhaze.entity;

import infinityx.lunarhaze.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class Enemy extends GameObject{

    private static final float MOVE_SPEED = 6.5f;
    private static final float TURN_SPEED = 15.0f;
    private static final float SPEED_DAMPNING = 0.75f;
// Instance Attributes
    /** A unique identifier; used to decouple classes. */
    private int id;
    /** Ship velocity */
    private Vector2 velocity;
    /** Movement of the enemy **/
    private float movement;

    /** Current animation frame for this werewolf */
    private float animeframe;

    /** Whether the enemy is alerted. Once alerted,
     * the enemy start chasing werewolf */
    private Boolean isAlerted;

    /** Whether the enemy is alive. */
    private boolean isAlive;

    /** points (in Tile index) in the enemy's patrolPath */
    private ArrayList<Vector2> patrolPath;

    private Direction direction;

    public enum Direction{
        NORTH,
        SOUTH,
        WEST,
        EAST
    }


    /**
     * Returns the type of this object.
     *
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    public ObjectType getType() {
        return ObjectType.ENEMY;
    }


    /**
     * Returns whether the enemy is alerted.
     */
    public Boolean getIsAlerted() {
        return isAlerted;
    }

    public Direction getDirection(){
        return this.direction;
    }

    /**
     * Sets whether the enemy is alerted.
     */
    public void setIsAlerted(Boolean value) {
        isAlerted = value;
    }

    private int currentWayPoint;

    /**
     * Initialize an enemy not alerted.
     */
    public Enemy(int id, float x, float y, ArrayList<Vector2> patrolPath) {
        super(x,y);
        this.id = id;
        isAlive = true;
        this.patrolPath = patrolPath;
        animeframe = 0.0f;
        isAlerted = false;
        direction = Direction.NORTH;
    }
    /** get the next patrol point of the enemy */
    public Vector2  getNextPatrol() {
        if (currentWayPoint > patrolPath.size()){
            currentWayPoint = 0;
        }
        Vector2 next =  patrolPath.get(currentWayPoint);
        currentWayPoint++;
        return next;
    }

    /**
     * Returns whether or not the ship is alive.
     *
     * A ship is dead once it has fallen past MAX_FALL_AMOUNT. A dead ship cannot be
     * targeted, involved in collisions, or drawn.  For all intents and purposes, it
     * does not exist.
     *
     * @return whether or not the ship is alive
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Returns whether or not the ship is active.
     *
     * An inactive ship is one that is either dead or dying.  A ship that has started
     * to fall, but has not fallen past MAX_FALL_AMOUNT is inactive but not dead.
     * Inactive ships are drawn but cannot be targeted or involved in collisions.
     * They are just eye-candy at that point.
     *
     * @return whether or not the ship is active
     */
    public void setTexture(Texture texture) {
        throw new NotImplementedException();
    }

    /**
     * Updates the animation frame and position of this enemy.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {
        // Call superclass's update
        super.update(delta);
    }

    public void draw(GameCanvas canvas) {

    }

    public int getId() {
        return this.id;
    }
}
