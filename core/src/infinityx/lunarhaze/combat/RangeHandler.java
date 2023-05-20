package infinityx.lunarhaze.combat;

import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Archer;
import infinityx.lunarhaze.models.entity.Arrow;
import infinityx.lunarhaze.models.entity.Werewolf;

public class RangeHandler extends AttackHandler {

//    private final float TIME_TO_TRAVEL = 0.8f;

    private final float ARROW_SPEED = 5f;

    /**
     * arrow attached to entity
     */
    private Arrow arrow;

    private Werewolf target;

    private float angleFacing;

    private LevelContainer container;

    private float arrowFlyTime = 0;


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

        arrow = container.addArrow(entity.getX(), entity.getY(), (Archer) entity);
        Vector2 dir = target.getPosition().sub(entity.getPosition()).nor();
        arrow.setLinearVelocity(dir);
        angleFacing = (float) Math.atan2(target.getY() - entity.getY(), target.getX() - entity.getX());
        arrow.setAngle(angleFacing + (float) Math.PI);
        arrow.setInitialAngle(angleFacing + (float) Math.PI);
    }

    @Override
    public void processAttack(float delta) {
        super.processAttack(delta);
        if (arrow != null) {
            arrow.update(delta);
//            System.out.println(arrow.getLinearVelocity());
            arrow.setX(arrow.getX() + ARROW_SPEED * arrow.getLinearVelocity().x * delta );
            arrow.setY(arrow.getY() + ARROW_SPEED * arrow.getLinearVelocity().y * delta );
        }
    }

    @Override
    protected void endAttack() {
        super.endAttack();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

//        arrowFlyTime += delta;
        // IMPORTANT: fly time has to be lower than archer attack cool down due to initialization
//
        if ((arrow != null && arrow.getLinearVelocity().epsilonEquals(Vector2.Zero))) {
            System.out.println(arrowFlyTime >= 2f);
            container.removeArrow(arrow);
            arrow = null;
//            arrowFlyTime = 0f;
        }
    }

    public float getAngleFacing() {
        return angleFacing;
    }

}
