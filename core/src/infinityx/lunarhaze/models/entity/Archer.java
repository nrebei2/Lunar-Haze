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

    public EnemyType getEnemyType() {
        return EnemyType.Archer;
    }

    /**
     * Initialize an enemy with dummy position, id, and patrol path
     */
    public Archer() {
        super();
        arrow = null;
    }
}
