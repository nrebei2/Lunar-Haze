package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import infinityx.lunarhaze.combat.AttackHitbox;
import infinityx.lunarhaze.graphics.CameraShake;
import infinityx.lunarhaze.graphics.ModelFlash;
import infinityx.lunarhaze.models.AttackingGameObject;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.models.entity.*;

/**
 * Controller to handle Box2D body interactions.
 * </summary>
 */
public class CollisionController implements ContactListener {

    /**
     * @param world World to register this contact listener to
     */
    public CollisionController(World world) {
        world.setContactListener(this);
    }

    @Override
    public void beginContact(Contact contact) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        // Safe since all entities that hold a Box2D body in our game are GameObjects
        GameObject obj1 = (GameObject) body1.getUserData();
        GameObject obj2 = (GameObject) body2.getUserData();

        processCollision(obj1, obj2);
    }

    /**
     * Detect and resolve collisions between two game objects
     *
     * @param o1 First object
     * @param o2 Second object
     */
    private void processCollision(GameObject o1, GameObject o2) {
        switch (o1.getType()) {
            case ENEMY:
                switch (o2.getType()) {
                    case WEREWOLF:
                        handleCollision((Enemy) o1, (Werewolf) o2);
                        break;
                    case HITBOX:
                        handleCollision(
                                ((AttackHitbox) o2).getAttacker(),
                                (AttackingGameObject) o1
                        );
                        break;
                    default:
                        break;
                }
                break;
            case ARCHER:
                switch (o2.getType()) {
                    case WEREWOLF:
                        handleCollision((Archer) o1, (Werewolf) o2);
                        break;
                    case HITBOX:
                        handleCollision(
                                ((AttackHitbox) o2).getAttacker(),
                                (AttackingGameObject) o1
                        );
                        break;
                    default:
                        break;
                }
                break;
            case ARROW:
                switch (o2.getType()) {
                    case WEREWOLF:
                        handleCollision((Arrow) o1, (Werewolf) o2);
                        break;
                    case HITBOX:
                        handleCollision(
                                ((AttackHitbox) o2).getAttacker(),
                                (AttackingGameObject) o1
                        );
                        break;
                    default:
                        break;
                }
                break;
            case WEREWOLF:
                switch (o2.getType()) {
                    case ENEMY:
                        handleCollision((Enemy) o2, (Werewolf) o1);
                        break;
                    case HITBOX:
                        handleCollision(
                                ((AttackHitbox) o2).getAttacker(),
                                (AttackingGameObject) o1
                        );
                        break;
                    case SCENE:
                        handleCollision(
                                (SceneObject) o2,
                                (Werewolf) o1
                        );
                    default:
                        break;
                }
                break;
            case HITBOX:
                switch (o2.getType()) {
                    // Could do something interesting when two hitboxes connect
                    case ENEMY:
                    case WEREWOLF:
                        handleCollision(
                                ((AttackHitbox) o1).getAttacker(),
                                (AttackingGameObject) o2
                        );
                        break;
                    default:
                        break;
                }
                break;
            case SCENE:
                switch (o2.getType()) {
                    case WEREWOLF:
                        handleCollision(
                                (SceneObject) o1,
                                (Werewolf) o2
                        );
                }

            default:
                break;
        }
    }

    /**
     * Collision logic between two {@link AttackingGameObject}.
     *
     * @param attacker The entity that attacked
     * @param attacked The entity that was attacked
     */
    private void handleCollision(AttackingGameObject attacker, AttackingGameObject attacked) {
        if (attacker == attacked) return;
        boolean immune = attacked.isImmune();
        if (!immune) {
            // Immunity frames for being attacked and when attacking
            attacker.setImmune();
            attacked.setImmune();
            attacked.setLockedOut();

            // Knock back attacked entity
            Vector2 direction = attacked.getPosition().sub(attacker.getPosition()).nor();
            attacked.getBody().applyLinearImpulse(direction.scl(attacker.attackKnockback), attacked.getBody().getWorldCenter(), true);

            attacked.hp -= attacker.attackDamage;
            if (attacked.hp < 0) attacked.hp = 0;

            CameraShake.shake(attacker.attackKnockback * 5f, 0.3f);
            if (attacked.getType() == GameObject.ObjectType.WEREWOLF) {
                attacked.setAttacked();
                // This class now flashes the werewolf only
                ModelFlash.flash(new Color(1f, 0f, 0f, 1), 0.7f, 0.2f, 0.2f, 1f);
            }
        }
    }

    /**
     * Collision logic between an enemy and the player.
     *
     * @param enemy  The enemy
     * @param player The player
     */
    private void handleCollision(Enemy enemy, Werewolf player) {
        // For now, dont do anything
        // Maybe they can push each other, idk
    }

    /**
     * Collision logic between a scene object and the player.
     *
     * @param obj    The scene object
     * @param player The player
     */
    private void handleCollision(SceneObject obj, Werewolf player) {
        if (obj.isSensor()) {
            // For now, we can assume obj is tall grass
            player.inTallGrass = obj;
        }
    }

    @Override
    public void endContact(Contact contact) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        // Safe since all entities that hold a Box2D body in our game are GameObjects
        GameObject obj1 = (GameObject) body1.getUserData();
        GameObject obj2 = (GameObject) body2.getUserData();

        processEndCollision(obj1, obj2);
    }


    /**
     * Detect and resolve end of collisions between two game objects
     *
     * @param o1 First object
     * @param o2 Second object
     */
    private void processEndCollision(GameObject o1, GameObject o2) {
        switch (o1.getType()) {
            case WEREWOLF:
                switch (o2.getType()) {
                    case SCENE:
                        endCollision(
                                (SceneObject) o2,
                                (Werewolf) o1
                        );
                    default:
                        break;
                }
                break;
            case SCENE:
                switch (o2.getType()) {
                    case WEREWOLF:
                        endCollision(
                                (SceneObject) o1,
                                (Werewolf) o2
                        );
                }

            default:
                break;
        }
    }

    /**
     * End of collision logic between a scene object and the player.
     *
     * @param obj    The scene object
     * @param player The player
     */
    private void endCollision(SceneObject obj, Werewolf player) {
        if (obj.isSensor()) {
            // For now, we can assume obj is tall grass
            if (obj == player.inTallGrass)
                player.inTallGrass = null;
        }
    }


    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}