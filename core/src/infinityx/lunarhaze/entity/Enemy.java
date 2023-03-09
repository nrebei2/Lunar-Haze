package infinityx.lunarhaze.entity;

import com.badlogic.gdx.physics.box2d.Body;
import com.sun.org.apache.xpath.internal.operations.Bool;
import infinityx.audio.EffectFactory;
import infinityx.lunarhaze.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import infinityx.lunarhaze.physics.ConeSource;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class Enemy extends GameObject{
// Instance Attributes
    /** A unique identifier; used to decouple classes. */
    private int id;
    private static final float MOVE_SPEED = 200f;

    /** Movement of the enemy **/
    private float movement;

    private Boolean faceRight;

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

    private ConeSource flashlight;

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

    public Body body;

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
        if (currentWayPoint > patrolPath.size() - 1){
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
        super.setTexture(texture);
    }

    public void setFlashlight(ConeSource cone) { flashlight = cone; }
    public ConeSource getFlashlight() { return flashlight; }

    public void changeFlashlightDirection() {
        System.out.println(getDirection());
        switch(getDirection()) {
            case NORTH:
                flashlight.setDirection(90f);
                break;
            case SOUTH:
                flashlight.setDirection(270f);
                break;
            case EAST:
                flashlight.setDirection(0f);
                break;
            case WEST:
                flashlight.setDirection(180f);
                break;
        }
        flashlight.update();
    }

    /**
     * Updates the animation frame and position of this enemy.
     *
     * Notice how little this method does.  It does not actively fire the weapon.  It
     * only manages the cooldown and indicates whether the weapon is currently firing.
     * The result of weapon fire is managed by the GameplayController.
     *
     */
    public void update(int controlCode) {
        boolean movingLeft  = (controlCode & EnemyController.CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (controlCode & EnemyController.CONTROL_MOVE_RIGHT) != 0;
        boolean movingUp    = (controlCode & EnemyController.CONTROL_MOVE_UP) != 0;
        boolean movingDown  = (controlCode & EnemyController.CONTROL_MOVE_DOWN) != 0;

        /* Process movement command.
        if (movingLeft) {
            velocity.x = -MOVE_SPEED;
            velocity.y = 0;
            direction = Direction.WEST;
        } else if (movingRight) {
            velocity.x = MOVE_SPEED;
            velocity.y = 0;
            direction = Direction.EAST;
        } else if (movingUp) {
            velocity.y = -MOVE_SPEED;
            velocity.x = 0;
            direction = Direction.NORTH;
        } else if (movingDown) {
            velocity.y = MOVE_SPEED;
            velocity.x = 0;
            direction = Direction.SOUTH;
        } else {
            // NOT MOVING, SO SLOW DOWN
            velocity.x = 0;
            velocity.y = 0;
        }

        position = position.add(velocity);*/

        float xVelocity = 0.0f;
        float yVelocity = 0.0f;
        if (movingLeft) {
            xVelocity = -MOVE_SPEED;
            direction = Direction.WEST;
        } else if (movingRight) {
            xVelocity = MOVE_SPEED;
            direction = Direction.EAST;
        }
        if (movingUp) {
            yVelocity = -MOVE_SPEED;
            direction = Direction.NORTH;
        } else if (movingDown) {
            yVelocity = MOVE_SPEED;
            direction = Direction.SOUTH;
        }
        body.setLinearVelocity(xVelocity, yVelocity);

        // Update position based on Box2D body
        position = body.getPosition();
    }

    public void draw(GameCanvas canvas) {
//        float effect = faceRight ? 1.0f : -1.0f;
//        float ox = 0.5f * texture.getRegionWidth();
//        float oy = 0.5f * werewolfSprite.getRegionHeight()
//        TODO 1.0f is NOT OKAY
        canvas.draw(texture,Color.WHITE, origin.x, origin.y, position.x, position.y, 0.0f, 1.0f , 1.0f);
    }

    public int getId() {
        return this.id;
    }
}
