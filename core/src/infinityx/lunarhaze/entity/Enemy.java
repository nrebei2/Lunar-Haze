package infinityx.lunarhaze.entity;

import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.EnemyController;
import infinityx.lunarhaze.GameCanvas;
import infinityx.lunarhaze.GameObject;
import infinityx.lunarhaze.LevelContainer;
import infinityx.lunarhaze.physics.ConeSource;

import java.util.ArrayList;

public class Enemy extends GameObject implements Pool.Poolable {
// Instance Attributes
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
     * points (in Tile index) in the enemy's patrolPath
     */
    // TODO: if we wanna be fancy use a bezier
    // Not that bad since we can easily compute the tangent and add force on enemy along it
    private ArrayList<Vector2> patrolPath;

    private Direction direction;

    private ConeSource flashlight;

    private float attackKnockback;

    private int attackDamage;

    private float hp;


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

    public ConeSource getFlashlight() {
        return flashlight;
    }

    /**
     * Sets whether the enemy is alerted.
     */
    public void setIsAlerted(Boolean value) {
        isAlerted = value;
    }

    private int currentWayPoint;

    /**
     * Initialize an enemy with dummy position, id, and patrol path
     */
    public Enemy() {
        super(0, 0);
        this.patrolPath = new ArrayList<>();
        animeframe = 0.0f;
        isAlerted = false;
        direction = Direction.NORTH;
        hp = 10.0f;
    }

    /**
     * Resets the object for reuse. Object references should be nulled and fields may be set to default values.
     */
    @Override
    public void reset() {
        hp = 10.0f;
        isAlerted = false;
    }

    /**
     * Parse and initialize specific enemy  attributes.
     *
     * @param json      Json tree holding enemy information
     * @param container LevelContainer which this player is placed in
     */
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        JsonValue p_dim = json.get("collider");
        setDimension(p_dim.get("width").asFloat(), p_dim.get("height").asFloat());

        super.initialize(directory, json, container);

        JsonValue light = json.get("flashlight");
        float[] color = light.get("color").asFloatArray();
        float dist = light.getFloat("distance");
        int rays = light.getInt("rays");
        float degrees = light.getFloat("degrees");

        ConeSource flashLight = new ConeSource(
                container.getRayHandler(), rays, Color.WHITE, dist,
                getX(), getY(), 0f, degrees
        );
        flashLight.setColor(color[0], color[1], color[2], color[3]);
        flashLight.setSoft(light.getBoolean("soft"));

        activatePhysics(container.getWorld());
        setFlashlight(flashLight);

        setFlashlightOn(true);

        JsonValue attack = json.get("attack");
        setAttackKnockback(attack.getFloat("knockback"));
        setAttackDamage(attack.getInt("damage"));
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
     * Attaches light to enemy as a flashlight
     */
    public void setFlashlight(ConeSource cone) {
        flashlight = cone;
        flashlight.attachToBody(getBody(), 0.5f, 0, flashlight.getDirection());
        flashlight.setActive(false);
    }

    /**
     * @param on Whether to turn the flashlight on (true) or off (false)
     */
    public void setFlashlightOn(boolean on) {
        flashlight.setActive(on);
    }

    public float getAttackKnockback() {
        return attackKnockback;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public void setAttackKnockback(float knockback) {
        attackKnockback = knockback;
    }

    public void setAttackDamage(int dmg) {
        attackDamage = dmg;
    }

    public float getHp() { return hp; }
    public void setHp(float value) { hp = value; }

    public float getHealthPercentage() {
        float maxHp = 3f;
        float currenthp = Math.max(0,hp);
        //System.out.println(currenthp);
        //System.out.println(currenthp/maxHp);
        return currenthp/maxHp;
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
        flashlight.getBody().setTransform(body.getPosition(), getDirection().getRotScale() * (float) (Math.PI / 2f));
    }

    /**
     * Sets the specific angle of the flashlight on this enemy
     *
     * @param ang the angle...
     */
    public void setFlashLightRot(float ang) {
        body.setTransform(body.getPosition(), ang);

    }
}
