package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.LevelContainer;

/**
 * Model class representing an enemy archer.
 */
public class Archer extends Enemy {

    /**
     * Arrow the archer holds
     */
    private Arrow arrow;

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

        arrow = new Arrow(this);
        arrow.initialize(directory, json.get("arrow"), container);
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
        arrow = null;
    }
}
