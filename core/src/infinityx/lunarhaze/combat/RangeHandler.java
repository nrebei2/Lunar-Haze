package infinityx.lunarhaze.combat;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Archer;
import infinityx.lunarhaze.models.entity.Arrow;
import infinityx.lunarhaze.models.entity.Werewolf;

public class RangeHandler extends AttackHandler {

    private final float ARROW_SPEED = 5f;

    private Werewolf target;

    private float angleFacing;

    private LevelContainer container;

    /**
     * @param archer the archer this class is controlling
     * @param target target entity is trying to hit
     */
    public RangeHandler(Archer archer, Werewolf target, LevelContainer container) {
        super(archer);
        this.target = target;
        this.container = container;
    }

    @Override
    public void initiateAttack() {
        super.initiateAttack();

        Arrow arrow = container.addArrow(entity.getX(), entity.getY(), (Archer) entity);
        Vector2 dir = target.getPosition().sub(entity.getPosition()).nor();
        arrow.setLinearVelocity(dir.scl(ARROW_SPEED));
        arrow.setAngle(target.vectorToAngle(dir));
    }

    @Override
    public void processAttack(float delta) {
        super.processAttack(delta);
    }

    @Override
    protected void endAttack() {
        super.endAttack();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

}
