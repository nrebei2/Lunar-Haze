package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.AttackingGameObject;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.models.LevelContainer;

public class Arrow extends AttackingGameObject {

    /**
     * Reference to the archer that drew this arrow
     */
    Archer archer;

    private float attackRange;

    @Override
    public ObjectType getType() {
        return ObjectType.ARROW;
    }

    /**
     * Initialize an arrow
     */
    public Arrow(float x, float y, Archer archer) {
        super(x, y);
        this.archer = archer;
        setLoop(false);
    }

    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);
        this.setActive(false);

        float range = json.get("attack").get("range").asFloat();
        setAttackRange(range);
    }

    /**
     * Initialize arrow with dummy position
     */
    public Arrow(Archer archer) {
        this(0, 0, archer);
    }

    @Override
    public float getAttackRange() {
        return attackRange;
    }

    @Override
    public void setAttackRange(float attackRange) {
        this.attackRange = attackRange;
    }

    public Archer getArcher(){
        return archer;
    }

    public void setArcher(Archer archer){
        this.archer = archer;
    }
}
