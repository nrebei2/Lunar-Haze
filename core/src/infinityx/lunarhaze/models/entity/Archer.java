package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.SteeringGameObject;
import infinityx.lunarhaze.physics.ConeSource;

import java.util.ArrayList;

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
     * Distance at which archer prepares to shoot
     */
    private float shoot_dist;

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
     * Parse and initialize specific enemy  attributes.
     *
     * @param json      Json tree holding enemy information
     * @param container LevelContainer which this player is placed in
     */
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);

        // TODO: initialize shoot_dist from JSON
        // TODO: reinitialize hp, attack damage knockback
//        JsonValue attack = json.get("attack");
//        setAttackKnockback(attack.getFloat("knockback"));
//        setAttackDamage(attack.getInt("damage"));
//        setMaxHp(json.getFloat("hp"));
    }

    /**
     * Initialize an enemy with dummy position, id, and patrol path
     */
    public Archer() {
        super();
    }
}
