package infinityx.lunarhaze;

import com.badlogic.gdx.physics.box2d.*;

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



        if( obj1.getType() == GameObject.ObjectType.WEREWOLF && obj2.getType() == GameObject.ObjectType.ENEMY ||
                (obj1.getType() == GameObject.ObjectType.ENEMY && obj2.getType() == GameObject.ObjectType.WEREWOLF)) {
            System.out.println("SKDHAHDSKAHDKSAHDSAKDH:AKHDKSAHDKASHDKSAHDKASHDKASHDKSAHKDASDHKSAD");

        }




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
