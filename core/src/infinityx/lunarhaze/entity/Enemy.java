package infinityx.lunarhaze.entity;

import box2dLight.PointLight;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.EnemyController;
import infinityx.lunarhaze.GameObject;
import infinityx.lunarhaze.LevelContainer;
import infinityx.lunarhaze.physics.ConeSource;

import java.util.ArrayList;

public class Enemy extends GameObject {
// Instance Attributes
    /**
     * A unique identifier; used to decouple classes.
     */
    private int id;
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
    // TODO: if we wanna be fancy use a bezier
    // Not that bad since we can easily compute the tangent and add force on enemy along it
    private ArrayList<Vector2> patrolPath;

    private Direction direction;

    private ConeSource flashlight;

    private float attackKnockback;

    private float attackDamage;

    public enum Direction {
        NORTH(1), SOUTH(3), WEST(2), EAST(0);

        private final int scale;

        private Direction(int scale) {
            this.scale = scale;
        }

        public int getRotScale() {
            return scale;
        }
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
     * Initialize an enemy with dummy attributes
     */
    public Enemy() {
        isAlive = true;
        this.patrolPath = new ArrayList<>();
        animeframe = 0.0f;
        isAlerted = false;
        direction = Direction.NORTH;
    }

    /**
     * Deep clones enemy, can be used independently of this
     * @return new enemy
     */
    public Enemy deepClone(LevelContainer container) {
        Enemy enemy = new Enemy();
        enemy.setSpeed(speed);
        enemy.setTexture(getTexture());
        enemy.setOrigin((int)origin.x, (int)origin.y);
        ConeSource flashLight = new ConeSource(
                container.getRayHandler(), this.flashlight.getRayNum(), this.flashlight.getColor(), this.flashlight.getDistance(),
                0, 0, this.flashlight.getDirection(), this.flashlight.getConeDegree()
        );
        enemy.setBodyState(body);
        flashLight.setSoft(this.flashlight.isSoft());
        enemy.activatePhysics(container.getWorld());
        enemy.setFlashlight(flashlight);

        enemy.setDimension(getDimension().x, getDimension().y);
        enemy.setPositioned(positioned);
        return enemy;
    }

    /**
     * get the next patrol point of the enemy
     */
    public Vector2 getNextPatrol() {
        Vector2 next = patrolPath.get(currentWayPoint);
        currentWayPoint++;
        if (currentWayPoint > patrolPath.size() - 1) {
            currentWayPoint = 0;
        }
        return next;
    }

    /**
     * get the patrol point this enemy is currently moving to
     */
    public Vector2 getCurrentPatrol() {
        return patrolPath.get(currentWayPoint);
    }

    public void setPatrolPath(ArrayList<Vector2> path) {
        this.patrolPath = path;
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
     * Attaches light to enemy as a flashlight
     */
    public void setFlashlight(ConeSource cone) {
        flashlight = cone;
        flashlight.attachToBody(getBody(), 0.5f, 0, flashlight.getDirection());
    }


    /**
     * @param on Whether to turn the flashlight on (true) or off (false)
     */
    public void setFlashlightOn(boolean on) {
        flashlight.setActive(on);
    }

    public float getAttackKnockback() { return attackKnockback; }

    public float getAttackDamage() { return attackDamage; }

    public void setAttackKnockback(float knockback) { attackKnockback = knockback; }

    public void setAttackDamage(float dmg) { attackDamage = dmg; }

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
            xVelocity = -speed;
            direction = Direction.WEST;
        } else if (movingRight) {
            xVelocity = speed;
            direction = Direction.EAST;
        }
        if (movingDown) {
            yVelocity = -speed;
            direction = Direction.SOUTH;
        } else if (movingUp) {
            yVelocity = speed;
            direction = Direction.NORTH;
        }
        body.setLinearVelocity(xVelocity, yVelocity);
    }

    /**
     * As the name suggests.
     * Can someone think of a better name? Im too tired for this rn
     */
    public void setFlashLightRotAlongDir() {
        body.setTransform(body.getPosition(), getDirection().getRotScale() * (float) (Math.PI / 2f));
    }

    /**
     * Sets the specific angle of the flashlight on this enemy
     *
     * @param ang the angle...
     */
    public void setFlashLightRot(float ang) {
        body.setTransform(body.getPosition(), ang);

    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
