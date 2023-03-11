package infinityx.lunarhaze.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.EnemyController;
import infinityx.lunarhaze.GameObject;
import infinityx.lunarhaze.physics.ConeSource;

import java.util.ArrayList;

public class Enemy extends GameObject {
// Instance Attributes
    /**
     * A unique identifier; used to decouple classes.
     */
    private final int id;
    private static final float MOVE_SPEED = 1.5f;

    /**
     * Movement of the enemy
     **/
    private float movement;

    private Boolean faceRight;

    /**
     * Current animation frame for this werewolf
     */
    private final float animeframe;

    /**
     * Whether the enemy is alerted. Once alerted,
     * the enemy start chasing werewolf
     */
    private Boolean isAlerted;

    /**
     * Whether the enemy is alive.
     */
    private final boolean isAlive;

    /**
     * points (in Tile index) in the enemy's patrolPath
     */
    private final ArrayList<Vector2> patrolPath;

    private Direction direction;

    private ConeSource flashlight;

    public enum Direction {
        NORTH,
        SOUTH,
        WEST,
        EAST
    }


    /**
     * Returns the type of this object.
     * <p>
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

    public Direction getDirection() {
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
        super(x, y);
        this.id = id;
        isAlive = true;
        this.patrolPath = patrolPath;
        animeframe = 0.0f;
        isAlerted = false;
        direction = Direction.NORTH;
    }

    /**
     * get the next patrol point of the enemy
     */
    public Vector2 getNextPatrol() {
        if (currentWayPoint > patrolPath.size() - 1) {
            currentWayPoint = 0;
        }
        Vector2 next = patrolPath.get(currentWayPoint);
        currentWayPoint++;
        return next;
    }

    /**
     * Returns whether or not the ship is alive.
     * <p>
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
     * <p>
     * An inactive ship is one that is either dead or dying.  A ship that has started
     * to fall, but has not fallen past MAX_FALL_AMOUNT is inactive but not dead.
     * Inactive ships are drawn but cannot be targeted or involved in collisions.
     * They are just eye-candy at that point.
     *
     * @return whether or not the ship is active
     */
    public void setTexture(Texture texture) {
        super.setTexture(texture);
    }

    /**
     * Attaches light to enemy as a flashlight
     */
    public void setFlashlight(ConeSource cone) {
        flashlight = cone;
        flashlight.attachToBody(getBody(), 0.2f, 0, flashlight.getDirection());
    }


    /**
     * @param on Whether to turn the flashlight on (true) or off (false)
     */
    public void setFlashlightOn(boolean on) {
        flashlight.setActive(on);
    }

    /**
     * Updates the animation frame and position of this enemy.
     * <p>
     * Notice how little this method does.  It does not actively fire the weapon.  It
     * only manages the cooldown and indicates whether the weapon is currently firing.
     * The result of weapon fire is managed by the GameplayController.
     */
    public void update(int controlCode) {
        boolean movingLeft = (controlCode & EnemyController.CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (controlCode & EnemyController.CONTROL_MOVE_RIGHT) != 0;
        boolean movingDown = (controlCode & EnemyController.CONTROL_MOVE_DOWN) != 0;
        boolean movingUp = (controlCode & EnemyController.CONTROL_MOVE_UP) != 0;

        float xVelocity = 0.0f;
        float yVelocity = 0.0f;
        if (movingLeft) {
            xVelocity = -MOVE_SPEED;
            direction = Direction.WEST;
        } else if (movingRight) {
            xVelocity = MOVE_SPEED;
            direction = Direction.EAST;
        }
        if (movingDown) {
            yVelocity = -MOVE_SPEED;
            direction = Direction.SOUTH;
        } else if (movingUp) {
            yVelocity = MOVE_SPEED;
            direction = Direction.NORTH;
            System.out.println("moving up");
        }
        body.setLinearVelocity(xVelocity, yVelocity);
        rotate(direction);
    }

    private void rotate(Direction direction) {
        float ang = body.getAngle();
        switch (direction) {
            case NORTH:
                if (ang > Math.PI / 2f) body.setAngularVelocity(-4f);
                else if (ang < Math.PI / 2f) body.setAngularVelocity(4f);
                else body.setAngularVelocity(0);
                break;
            case SOUTH:
                if (ang > (3 * Math.PI) / 2f) body.setAngularVelocity(-4f);
                else if (ang < (3 * (Math.PI)) / 2f) body.setAngularVelocity(4f);
                else body.setAngularVelocity(0);
                break;
            case WEST:
                if (ang > Math.PI) body.setAngularVelocity(-4f);
                else if (ang < Math.PI) body.setAngularVelocity(4f);
                else body.setAngularVelocity(0);
                break;
            case EAST:
                if (ang > (2 * Math.PI)) body.setAngularVelocity(-4f);
                else if (ang < (2 * Math.PI)) body.setAngularVelocity(4f);
                else body.setAngularVelocity(0);
                break;
        }
    }

    public int getId() {
        return this.id;
    }
}
