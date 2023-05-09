package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.LevelContainer;

/**
 * Model class representing an enemy.
 */
public class Archer extends Enemy implements Pool.Poolable {

    /**
     * Arrow the archer holds
     */
    private Arrow arrow;

    /**
     * Distance (number of tiles) at which archer's arrow can hit player
     */
    private int shoot_dist;

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
        // FIXME FIXME FIXME FIXME
//        JsonValue archerJson = json.get("archer");
//        JsonValue attack = archerJson.get("attack");
//        maxHp = archerJson.getFloat("hp");
//        setShootDist(attack.getInt("shootdist"));
    }

    @Override
    public float getAttackRange() {
        return 0;
    }

    @Override
    public void setAttackRange(float attackRange) {

    }

    @Override
    public void setAttacking(boolean value) {

    }

//    @Override
//    public void updateAttack(float delta) {
//
//    }

    /**
     * Returns the type of this object.
     * <p>
     * We use this instead of runtime-typing for performance reasons.
     *
     * @return the type of this object.
     */
    public ObjectType getType() {
        return ObjectType.ARCHER;
    }

    /**
     * Initialize an enemy with dummy position, id, and patrol path
     */
    public Archer() {
        super();
        arrow = null;
    }
}
