package infinityx.lunarhaze.models;

import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;

/**
 * A game object that can and can be attack
 */
public abstract class AttackingGameObject extends GameObject {

    /**
     * The maximum amount of hit-points for this entity
     */
    public float maxHp;

    /**
     * The current amount of hit-points for this entity
     */
    public float hp;

    /**
     * Whether the entity can move or not; the entity can't move
     * if its being knocked back by an attack or is attacking.
     **/
    public boolean canMove;

    /**
     * How long (in seconds) the entity cannot move after being attacked.
     */
    public float lockout;

    /**
     * Whether the entity is locked out from an attack
     */
    public boolean lockedOut;

    /**
     * For use with {@link #lockout}
     */
    protected float lockoutTime;

    /**
     * Whether the entity is currently attacking
     */
    protected boolean isAttacking;

    /**
     * Whether the entity is currently attacked
     */
    protected boolean isAttacked;

    /**
     * Total time duration (in seconds) for attacked frames.
     */
    protected float isAttackedLength;

    /**
     * Current time duration for attacked frames
     */
    protected float attackedTime;

    /**
     * Impulse applied to entity hit
     */
    public float attackKnockback;

    /**
     * Cooldown (in seconds) between attacks
     */
    public float attackCooldown;

    /**
     * Length (in seconds) of an attack
     */
    public float attackLength;

    /**
     * Number of hit-points an attack of this entity does
     */
    public float attackDamage;

    /**
     * Whether the entity is immune from other attacks
     */
    protected boolean isImmune;

    /**
     * Total time duration (in seconds) for immunity frames.
     * Immunity begins when hit or attacking.
     */
    protected float immunityLength;

    /**
     * Current time duration for immunity frames
     */
    protected float immunityTime;

    /**
     * Initialize attacking attributes for this entity
     */
    public AttackingGameObject() {
        super();
        isAttacking = false;
        canMove = true;
        isImmune = false;
        lockedOut = false;
        isAttacked = false;
    }

    public AttackingGameObject(float x, float y) {
        super(x, y);
        isAttacking = false;
        canMove = true;
        isImmune = false;
        lockedOut = false;
        isAttacked = false;
    }


    /**
     * Further parses specific attacking attributes.
     */
    @Override
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);
        maxHp = hp = json.getFloat("health");

        JsonValue attack = json.get("attack");
        attackKnockback = attack.getFloat("knockback");
        attackDamage = attack.getFloat("damage");
        attackCooldown = attack.getFloat("cooldown");
        attackLength = attack.getFloat("length");
        immunityLength = attack.getFloat("immunity");
        isAttackedLength = attack.getFloat("lockout");
        lockout = attack.getFloat("lockout");

    }

    /**
     * Begin immunity frames for this entity.
     */
    public void setImmune() {
        isImmune = true;
        immunityTime = immunityLength;
    }

    public boolean isImmune() {
        return isImmune;
    }


    /**
     * Begin lock out for this entity. Should be called when attacked.
     */
    public void setLockedOut() {
        lockedOut = true;
        lockoutTime = lockout;
    }

    public boolean isLockedOut() {
        return lockedOut;
    }

    /**
     * @return reach of attack hitbox in world length
     */
    abstract public float getAttackRange();

    /**
     * Updates the reach of the entity's attack hitbox
     *
     * @param attackRange new range in world length
     */
    abstract public void setAttackRange(float attackRange);

    /**
     * Set whether the entity is currently attacking
     */
    public void setAttacking(boolean value) {
        isAttacking = value;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    /**
     * Begin attacked frames for this entity.
     */
    public void setAttacked() {
        isAttacked = true;
        attackedTime = isAttackedLength;
    }

    public boolean isAttacked() {
        return isAttacked;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        canMove = !isAttacking && !lockedOut;

        // Update counters for immunity and lockout
        if (isImmune) {
            immunityTime -= delta;
            if (immunityTime <= 0) {
                isImmune = false;
            }
        }

        if (isAttacked) {
            attackedTime -= delta;
            if (attackedTime <= 0) {
                isAttacked = false;
            }
        }
        if (lockedOut) {
            lockoutTime -= delta;
            if (lockoutTime <= 0)
                lockedOut = false;
        }
    }
}
