package infinityx.lunarhaze;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;

/**
 * Controller to handle gameplay interactions.
 * </summary>
 */
public class CollisionController implements ContactListener {

    /**
     * Reference to level container holding all game objects
     */
    private LevelContainer level;


    public CollisionController(LevelContainer level){
        this.level = level;
        level.getWorld().setContactListener(this);
    }

    @Override
    public void beginContact(Contact contact) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        GameObject obj1 = (GameObject) body1.getUserData();
        GameObject obj2 = (GameObject) body2.getUserData();

        if(obj1.getType() == GameObject.ObjectType.WEREWOLF && obj2.getType() == GameObject.ObjectType.ENEMY) {
            resolveAttack(obj1, obj2, ((Enemy)obj2).getAttackDamage(), ((Enemy)obj2).getAttackKnockback());
        }
        else if (obj1.getType() == GameObject.ObjectType.ENEMY && obj2.getType() == GameObject.ObjectType.WEREWOLF) {
            resolveAttack(obj2, obj1, ((Enemy)obj1).getAttackDamage(), ((Enemy)obj1).getAttackKnockback());
        }

    }

    public void resolveAttack(GameObject player, GameObject enemy, float damage, float knockback) {

        Body body = player.getBody();
        Body enemyBody = enemy.getBody();
        Vector2 pos = body.getPosition();
        Vector2 enemyPos = enemyBody.getPosition();

        // Get direction
        Vector2 direction = pos.sub(enemyPos).nor();

        ((Werewolf)player).setCanMove(false);
        body.applyLinearImpulse(direction.scl(knockback), body.getWorldCenter(), true);
        ((Werewolf)player).setHp((int) (((Werewolf)player).getHp() - damage));
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}