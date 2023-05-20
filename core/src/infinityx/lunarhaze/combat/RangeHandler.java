package infinityx.lunarhaze.combat;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.AttackingGameObject;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Archer;
import infinityx.lunarhaze.models.entity.Arrow;
import infinityx.lunarhaze.models.entity.Werewolf;

import java.util.logging.Level;

public class RangeHandler extends AttackHandler{

    private final float TIME_TO_TRAVEL = 1.2f;

    /**
     * arrow attached to entity
     */
    private Arrow arrow;

    private Werewolf target;

    private float angleFacing;

    private LevelContainer container;


    /**
     * @param archer the archer this class is controlling
     * @param target target entity is trying to hit
     */
    public RangeHandler(Archer archer, Werewolf target, LevelContainer container){
        super(archer);
        this.target = target;
        this.container = container;
    }

    @Override
    public void initiateAttack() {
        System.out.println("archers initate attack");
        super.initiateAttack();

        arrow = container.addArrow(entity.getX(), entity.getY(), (Archer) entity);
        Vector2 dir = target.getPosition().sub(entity.getPosition());
        arrow.setLinearVelocity(dir);
        angleFacing = (float) Math.atan2(target.getY() - entity.getY(), target.getX() - entity.getX());
        arrow.setAngle(angleFacing + (float) Math.PI);
        arrow.setInitialAngle(angleFacing + (float) Math.PI);
    }

    @Override
    public void processAttack(float delta) {
        super.processAttack(delta);
        arrow.update(delta);
        arrow.setX(arrow.getX() + arrow.getLinearVelocity().x * delta / TIME_TO_TRAVEL);
        arrow.setY(arrow.getY() + arrow.getLinearVelocity().y * delta / TIME_TO_TRAVEL);
    }

    @Override
    protected void endAttack() {
        super.endAttack();
    }

    public float getAngleFacing(){
        return angleFacing;
    }

}
