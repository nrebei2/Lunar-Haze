package infinityx.lunarhaze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import infinityx.lunarhaze.combat.AttackHitbox;
import infinityx.lunarhaze.combat.PlayerAttackHandler;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.graphics.CameraShake;
import infinityx.lunarhaze.graphics.ScreenFlash;

/**
 * Controller to handle gameplay interactions.
 * </summary>
 */
public class CollisionController implements ContactListener {

    /**
     * Maximum killing power of the player
     */
    private final float MAX_KILL_POWER = 5.0f;

    public CollisionController(LevelContainer level) {
        level.getWorld().setContactListener(this);
    }

    @Override
    public void beginContact(Contact contact) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        GameObject obj1 = (GameObject) body1.getUserData();
        GameObject obj2 = (GameObject) body2.getUserData();

        if (obj1.getType() == GameObject.ObjectType.WEREWOLF && obj2.getType() == GameObject.ObjectType.ENEMY) {
            Werewolf player = (Werewolf) obj1;
            Enemy enemy = (Enemy) obj2;
            resolveEnemyAttack(player, enemy, enemy.getAttackDamage(), enemy.getAttackKnockback());
        } else if (obj1.getType() == GameObject.ObjectType.ENEMY && obj2.getType() == GameObject.ObjectType.WEREWOLF) {
            Werewolf player = (Werewolf) obj2;
            Enemy enemy = (Enemy) obj1;
            resolveEnemyAttack(player, enemy, enemy.getAttackDamage(), enemy.getAttackKnockback());
        } else if (obj1.getType() == GameObject.ObjectType.HITBOX && obj2.getType() == GameObject.ObjectType.ENEMY) {
            AttackHitbox hitbox = (AttackHitbox) body1.getUserData();
            Enemy enemy = (Enemy) body2.getUserData();
            resolvePlayerAttack(hitbox, enemy);
        } else if (obj1.getType() == GameObject.ObjectType.ENEMY && obj2.getType() == GameObject.ObjectType.HITBOX) {
            AttackHitbox hitbox = (AttackHitbox) body2.getUserData();
            Enemy enemy = (Enemy) body1.getUserData();
            resolvePlayerAttack(hitbox, enemy);
        }

    }

    public void resolveEnemyAttack(GameObject player, GameObject enemy, float damage, float knockback) {

        Body body = player.getBody();
        Body enemyBody = enemy.getBody();
        Vector2 pos = body.getPosition();
        Vector2 enemyPos = enemyBody.getPosition();

        // Get direction
        Vector2 direction = pos.sub(enemyPos).nor();

        ((Werewolf) player).setCanMove(false);
        body.applyLinearImpulse(direction.scl(knockback), body.getWorldCenter(), true);
        ((Werewolf) player).setHp((int) (((Werewolf) player).getHp() - damage));
        CameraShake.shake(knockback * 3f, 0.3f);
        ScreenFlash.flash(new Color(1f, 0.2f, 0.2f, 1), 0.15f, 0.05f, 0.05f, 0.15f);
    }

    public void resolvePlayerAttack(AttackHitbox hitbox, Enemy enemy) {
        // Apply damage to the enemy
        enemy.setHp(enemy.getHp() - MAX_KILL_POWER * PlayerAttackHandler.getAttackPower());

        // Apply knockback to the enemy
        Body enemyBody = enemy.getBody();
        Vector2 playerPos = hitbox.getBody().getPosition();
        Vector2 enemyPos = enemyBody.getPosition();
        Vector2 knockbackDirection = enemyPos.sub(playerPos).nor();
        enemyBody.applyLinearImpulse(knockbackDirection.scl(2f * PlayerAttackHandler.getAttackRange()), enemyBody.getWorldCenter(), true);
        CameraShake.shake(3, 1);
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