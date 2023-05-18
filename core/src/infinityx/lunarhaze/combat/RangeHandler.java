package infinityx.lunarhaze.combat;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.models.AttackingGameObject;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Archer;
import infinityx.lunarhaze.models.entity.Arrow;
import infinityx.lunarhaze.models.entity.Werewolf;

public class RangeHandler extends AttackHandler{
    /**
     * arrow attached to entity
     */
    private Arrow arrow;

    private Werewolf target;

    /**
     * @param archer the archer this class is controlling
     * @param target target entity is trying to hit
     */
    public RangeHandler(Archer archer, Werewolf target){
        super(archer);
        this.target = target;
        this.arrow = archer.getArrow();

    }

    @Override
    public void initiateAttack() {
        System.out.println("archers initate attack");
        super.initiateAttack();

//        arrow = new Arrow(entity.getX(), entity.getY(), (Archer) entity);
        arrow.setActive(true);
//        arrow.setArcher((Archer) entity);
        arrow.setX(entity.getX());
        arrow.setY(entity.getY());
        Vector2 dir = target.getPosition().sub(entity.getPosition());
        System.out.println("Linear velocity is: " + dir);
        System.out.println("Arrow position is: " + entity.getX() + ", " + entity.getY());
        arrow.setLinearVelocity(dir);
    }

    @Override
    public void processAttack(float delta) {
        super.processAttack(delta);
        arrow.update(delta);
        System.out.println("Update position to: " + arrow.getX() + ", " + arrow.getY());
    }

    @Override
    protected void endAttack() {
        super.endAttack();
        arrow.setActive(false);
    }

}
