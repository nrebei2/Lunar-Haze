package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.LevelContainer;

/**
 * Model class representing an enemy archer.
 */
public class Archer extends Enemy {

    /**
     * At what distance to attack from
     */
    private float attackRange;

    /**
     * Parse and initialize specific enemy  attributes.
     *
     * @param json      Json tree holding enemy information
     * @param container LevelContainer which this player is placed in
     */
    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);

        float range = json.get("attack").get("range").asFloat();
        setAttackRange(range);

        updateStrafeDistance();
    }

    @Override
    public float getAttackRange() {
        return attackRange;
    }

    @Override
    public void setAttackRange(float attackRange) {
        this.attackRange = attackRange;
    }

    public EnemyType getEnemyType() {
        return EnemyType.Archer;
    }

    /**
     * Initialize an enemy with dummy position, id, and patrol path
     */
    public Archer() {
        super();
//        arrow = new Arrow(this.getX(), this.getY(), this);
    }

}
