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

    private final float TIME_TO_TRAVEL = 2.0f;

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

        arrow.setActive(true);
        arrow.setX(entity.getX());
        arrow.setY(entity.getY());
        Vector2 dir = target.getPosition().sub(entity.getPosition());
        System.out.println("Linear velocity is: " + dir);
        System.out.println("Arrow position is: " + entity.getX() + ", " + entity.getY());
        arrow.setLinearVelocity(dir);
        arrow.setAngle((float) Math.atan((target.getY() - entity.getY()) / (target.getX() - entity.getX())));
        arrow.canMove = true;
        System.out.println("Archer associated with the arrow is: " + arrow.getArcher());
        if (arrow.hp != 0){
            System.out.println("Arrow is initialized");
        }
        System.out.println("Arrow dimension is: " + arrow.getTextureWidth() + ", " + arrow.getTextureHeight());
    }

    @Override
    public void processAttack(float delta) {
        super.processAttack(delta);
        arrow.update(delta);
        arrow.setX(arrow.getX() + arrow.getLinearVelocity().x * delta / TIME_TO_TRAVEL);
        arrow.setY(arrow.getY() + arrow.getLinearVelocity().y * delta / TIME_TO_TRAVEL);
//        System.out.println("Update position to: " + arrow.getX() + ", " + arrow.getY());
    }

    @Override
    protected void endAttack() {
        super.endAttack();
        arrow.setActive(false);
    }

}
