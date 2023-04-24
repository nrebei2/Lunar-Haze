package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.LevelContainer;

/**
 * Model class representing an enemy.
 */
public class Archer extends Enemy implements Pool.Poolable {
    public enum Detection {
        /**
         * The enemy is alerted (Exclamation point!)
         */
        ALERT,
        /**
         * The enemy has noticed sometime amiss (Question mark?)
         */
        NOTICED,
        /**
         * The enemy indicator is increasing
         */
        INDICATOR,
        /**
         * Neither heard nor seen anything
         */
        NONE
    }

    /**
     * Arrow the archer holds
     */
//    private Arrow arrow;

    /**
     * Distance (number of tiles) at which archer's arrow can hit player
     */
    private int shoot_dist;

    private float attackKnockback;

    private int attackDamage;

    /**
     * The maximum amount of hit-points for this enemy
     */
    private float maxHp;

    /**
     * The current amount of hit-points for this enemy
     */
    private float hp;

    /**
     * Set knockback for the archer type
     */
    public void setAttackKnockback(float attackKnockback) {
        this.attackKnockback = attackKnockback;
    }

    /**
     * Set maximum hp for the archer type
     */
    public void setMaxHp(float maxHp) {
        this.maxHp = maxHp;
    }

    /**
     * Set attack damage for the archer type
     */
    public void setAttackDamage(int attackDamage) {
        this.attackDamage = attackDamage;
    }

    /**
     * Set shoot distance for the archer type
     */
    public void setShootDist(int shoot_dist) {
        this.shoot_dist = shoot_dist;
    }

    /**
     * Parse and initialize specific enemy  attributes.
     *
     * @param json      Json tree holding enemy information
     * @param container LevelContainer which this player is placed in
     */
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);

        // TODO: initialize shoot_dist from JSON
        // TODO: reinitialize hp, attack damage knockback
        JsonValue archerJson = json.get("archer");
        JsonValue attack = archerJson.get("attack");
        setMaxHp(archerJson.getFloat("hp"));
        setAttackKnockback(attack.getFloat("knockback"));
        setAttackDamage(attack.getInt("damage"));
        setShootDist(attack.getInt("shootdist"));
    }

    /**
     * Initialize an enemy with dummy position, id, and patrol path
     */
    public Archer() {
        super();
    }
}
