package infinityx.lunarhaze;
/*
 * CollisionController.java
 *
 * This controller implements basic collision detection as described in
 * the instructions.  All objects in this game are treated as circles,
 * and a collision happens when circles intersect.
 *
 * Three main methods are used here to check for collisions:
 * -processBounds: checks whether a game object walks out of the game window
 * -processCollisions: checks whether collision occurs between two game objects' shadows
 * -processTiles: checks whether a game object's shadow attempts to walk on a non-walkable tile
 *
 * Author: Sicheng A. Ma
 * Based on Optimization Lab by Walker M. White, 2023
 * LibGDX version, 2/2/2015
 */

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;

import java.util.ArrayList;

/**
 * Controller implementing simple game physics.
 * <p>
 * This is a very inefficient physics engine.  Part of this lab is determining
 * how to make it more efficient.
 */
public class CollisionController {
    /**
     * Storage space for level details, used to detect collision on walkable/non-walkable tiles.
     */
    private final LevelContainer curr_level;
    // 'Bounciness' constants
    /**
     * Restitution for colliding with the (hard coded) box
     */
    protected static final float BOX_COEFF_REST = 0.95f;
    /**
     * Restitution for colliding with the (hard coded) bump
     */
    protected static final float BUMP_COEFF_REST = 1.95f;
    /**
     * Dampening factor when colliding with floor or shell
     */
    protected static final float DAMPENING_FACTOR = 0.95f;

    // Geometry of the background image
    /**
     * (Scaled) position of the box center
     */
    protected static final float BOX_X_POSITION = 0.141f;
    /**
     * (Scaled) position of half the box width
     */
    protected static final float BOX_HALF_WIDTH = 0.133f;
    /**
     * (Scaled) position of the box height from bottom of screen
     */
    protected static final float BOX_FULL_HEIGHT = 0.2f;


    // These cannot be modified after the controller is constructed.
    // If these change, make a new constructor.
    /**
     * Width of the collision geometry
     */
    private final float width;
    /**
     * Height of the collision geometry
     */
    private final float height;

    // Cache objects for collision calculations
    private final Vector2 temp1;
    private final Vector2 temp2;

    private final int parameter = 36;
    private final ArrayList<ArrayList<ArrayList<GameObject>>> cells = new ArrayList<>();


    /// ACCESSORS

    /**
     * Returns width of the game window (necessary to detect out of bounds)
     *
     * @return width of the game window
     */
    public float getWidth() {
        return width;
    }

    /**
     * Returns height of the game window (necessary to detect out of bounds)
     *
     * @return height of the game window
     */
    public float getHeight() {
        return height;
    }

    /**
     * Returns x-coordinate of the center of the square box in the background image.
     *
     * @return x-coordinate of the center of the square bo
     */
    public float getBoxX() {
        return BOX_X_POSITION * width;
    }

    /**
     * Returns half of the width of the square box in the background image.
     * <p>
     * The box edges are x+/- this width.
     *
     * @return half of the width of the square box
     */
    public float getBoxRadius() {
        return BOX_HALF_WIDTH * width;
    }

    /**
     * Returns height of the square box in the background image
     * <p>
     * Height is measured from the bottom of the screen, not the ledge.
     */
    public float getBoxHeight() {
        return BOX_FULL_HEIGHT * height;
    }


    /**
     * Returns radius of the semicircular bump in the background image
     *
     * @return radius of the semicircular bump
     */
    protected float getBumpRadius() {
        return 0.11f * width;
    }

    //#region Initialization (MODIFY THIS CODE)

    /**
     * Creates a CollisionController for the given screen dimensions.
     *
     * @param width  Width of the screen
     * @param height Height of the screen
     */
    public CollisionController(float width, float height, LevelContainer lvl) {
        this.width = width;
        this.height = height;
        this.curr_level = lvl;

        // Initialize cache objects
        temp1 = new Vector2();
        temp2 = new Vector2();
    }

    /**
     * This method assign objects currently on the screen into cells.
     *
     * @param objects List of live objects to check
     */
    public void AssignCells(Array<GameObject> objects) {
        // For each shell, check for collisions with the special terrain elements
        float width_divider = this.getWidth() / parameter;
        float height_divider = this.getWidth() / parameter;

        // Initializes cells
        cells.clear();
        for (int ii = 0; ii < parameter; ii++) {
            cells.add(new ArrayList<ArrayList<GameObject>>());
        }
        for (int jj = 0; jj < cells.size(); jj++) {
            for (int kk = 0; kk < parameter; kk++) {
                cells.get(jj).add(new ArrayList<GameObject>());
            }
        }

        for (GameObject o : objects) {
            // Make sure object is in bounds.
            // For shells, this handles the box and bump.
            processBounds(o);

            //#region REPLACE THIS CODE
            /* This is the slow code that must be replaced. */
            /**for (int ii = 0; ii < objects.size; ii++) {
             if (objects.get(ii) != o) {
             processCollision(o,objects.get(ii));
             }
             }*/

            //sorts objects into different cells
            if (o.getX() >= 0 && o.getX() <= getWidth() && o.getY() >= 0 && o.getY() <= getHeight()) {
                cells.get((int) Math.floor(o.getX() / width_divider)).get((int) Math.floor(o.getY() / height_divider)).add(o);
            }

            //#endregion
        }
    }
    //#endregion

    //#region Cell Management (INSERT CODE HERE)

    /**
     * This is the main (incredibly unoptimized) collision detetection method.
     *
     * @param objects List of live objects to check
     */
    public void processCollisions(Array<GameObject> objects) {
        for (GameObject o : objects) {
            // Make sure object is in bounds.
            // For shells, this handles the box and bump.
            processBounds(o);
            //TODO FIX THIS
            //processTiles(o);

            //#region REPLACE THIS CODE
            /* This is the slow code that must be replaced. */
            for (int ii = 0; ii < objects.size; ii++) {
                if (objects.get(ii) != o) {
                    processCollision(o, objects.get(ii));
                }
            }
            //#endregion
        }
    }

    //#endregion

    //#region Collision Handlers (DO NOT MODIFY FOR PART 1)

    /**
     * Check if a GameObject is out of bounds and take action.
     * <p>
     * Obviously an object off-screen is out of bounds.  In the case of shells, the
     * box and bump are also out of bounds.
     *
     * @param o Object to check
     */
    private void processBounds(GameObject o) {
        // Dispatch the appropriate helper for each type
        switch (o.getType()) {
            case ENEMY:
                handleBounds((Enemy) o);
                break;
            case WEREWOLF:
                handleBounds((Werewolf) o);
                break;
            default:
                break;
        }
    }

    /**
     * Check an enemy for being out-of-bounds.
     * TODO Probably unnecessary
     * <p>
     * Obviously an enemy off-screen is out of bounds.
     *
     * @param em Shell to check
     */
    private void handleBounds(Enemy em) {
        // Check if off right side
        if (em.getX() > getWidth() - em.getRadius()) {
            // Set within bounds on right and prevents from moving out of bounds
            em.setX(2 * (getWidth() - em.getRadius()) - em.getX());
            em.setVX(0);
            System.out.println("----------------Enemy hits bounds. NOT GOOD!");
        }
        // Check if off left side
        else if (em.getX() < em.getRadius()) {
            // Set within bounds on left and prevents from moving out of bounds
            em.setX(2 * em.getRadius() - em.getX());
            em.setVX(0);
            System.out.println("----------------Enemy hits bounds. NOT GOOD!");
        }

        // Check for in bounds on bottom
        if (em.getY() < em.getRadius()) {
            // Set within bounds on bottom and swap velocity
            em.setY(2 * em.getRadius() - em.getY());
            em.setVY(0);
            System.out.println("----------------Enemy hits bounds. NOT GOOD!");
        }

        // Check for in bounds on bottom
        else if (em.getY() > getHeight() - em.getRadius()) {
            // Set within bounds on bottom and swap velocity
            em.setY(2 * (getHeight() - em.getRadius()) - em.getY());
            em.setVY(0);
            System.out.println("----------------Enemy hits bounds. NOT GOOD!");
        }
        //TODO Constrict velocity
        //sh.setVY((float)Math.max(sh.getMinVY(), sh.getVY() * sh.getFriction()));
    }


    /**
     * Check a werewolf for being out-of-bounds.
     * <p>
     * Obviously a werewolf off-screen is out of bounds.
     *
     * @param ww Werewolf to check
     */
    private void handleBounds(Werewolf ww) {
        // Check if off right side
        if (ww.getX() > getWidth() - ww.getRadius()) {
            // Set within bounds on right and prevents from moving out of bounds
            ww.setX(2 * (getWidth() - ww.getRadius()) - ww.getX());
            ww.setVX(0);
            System.out.println("----------------Werewolf hits bounds. NOT GOOD!");
        }
        // Check if off left side
        else if (ww.getX() < ww.getRadius()) {
            // Set within bounds on left and prevents from moving out of bounds
            ww.setX(2 * ww.getRadius() - ww.getX() + ww.getRadius());
            ww.setVX(0);
            System.out.println("----------------Werewolf hits bounds. NOT GOOD!");
        }

        // Check for in bounds on bottom
        if (ww.getY() < ww.getRadius()) {
            // Set within bounds on bottom and swap velocity
            ww.setY(2 * ww.getRadius() - ww.getY() + ww.getRadius());
            ww.setVY(0);
            System.out.println("----------------Werewolf hits bounds. NOT GOOD!");
        }

        // Check for in bounds on bottom
        else if (ww.getY() > getHeight() - ww.getRadius()) {
            // Set within bounds on bottom and swap velocity
            ww.setY(2 * (getHeight() - ww.getRadius()) - ww.getY() - ww.getRadius());
            ww.setVY(0);
            System.out.println("----------------Werewolf hits bounds. NOT GOOD!");
        }
    }


    //TODO Not sure whether we need this method but still just gonna keep it here

    /**
     * Detect collision with rectangle step in the terrain.
     *
     * @param o Object to check
     * @param x Offset of the box
     */
    private void hitBox(GameObject o, float x) {
        if (Math.abs(o.getX() - x) < getBoxRadius() && o.getY() < getBoxHeight()) {
            if (o.getX() + o.getRadius() > x + getBoxRadius()) {
                o.setX(x + getBoxRadius() + o.getRadius());
                o.setVX(-o.getVX());
            } else if (o.getX() - o.getRadius() < x - getBoxRadius()) {
                o.setX(x - getBoxRadius() - o.getRadius());
                o.setVX(-o.getVX());
            } else {
                o.setVY(-o.getVY() * BOX_COEFF_REST);
                o.setY(getBoxHeight() + o.getRadius());
            }
        }
    }


    /**
     * Detect and resolve collisions between two game objects' shadows
     *
     * @param o1 First object
     * @param o2 Second object
     */
    private void processCollision(GameObject o1, GameObject o2) {
        // Dispatch the appropriate helper for each type
        switch (o1.getType()) {
            case ENEMY:
                switch (o2.getType()) {
                    case ENEMY:
                        handleCollision((Enemy) o1, (Enemy) o2);
                        break;
                    case WEREWOLF:
                        handleCollision((Enemy) o1, (Werewolf) o2);
                        break;
                    default:
                        break;
                }
                break;
            case WEREWOLF:
                switch (o2.getType()) {
                    case ENEMY:
                        // Reuse shell helper
                        handleCollision((Enemy) o2, (Werewolf) o1);
                        break;
                    case WEREWOLF:
                        handleCollision((Werewolf) o1, (Werewolf) o2);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * Collide an enemy with an enemy.
     *
     * @param e1 First enemy
     * @param e2 Second enemy
     */
    private void handleCollision(Enemy e1, Enemy e2) {
        if (e1.isDestroyed() || e2.isDestroyed()) {
            return;
        }

        // Find the axis of "collision"
        temp1.set(e1.getShadowposition()).sub(e2.getShadowposition());
        float dist = temp1.len();

        // Too far away
        if (dist > e1.getRadius() + e2.getRadius()) {
            return;
        }

        System.out.println("----------------Enemy collides with enemy. NOT GOOD!");
        // Push the enemies out so that they do not collide
        float distToPush = 0.01f + (e1.getRadius() + e2.getRadius() - dist) / 2;
        temp1.nor();
        temp1.scl(distToPush);
        e1.getPosition().add(temp1);
        e2.getPosition().sub(temp1);

        // TODO Compute the new velocities->not necessary, just set them back to 0
        //temp1.set(e2.getPosition()).sub(e1.getPosition()).nor(); // Unit vector for w1
        //temp2.set(e1.getPosition()).sub(e2.getPosition()).nor(); // Unit vector for w2

        //temp1.scl(temp1.dot(e1.getVelocity())); // Scaled to w1
        //temp2.scl(temp2.dot(e2.getVelocity())); // Scaled to w2

        // You can remove this to add friction.  We find friction has nasty feedback.
        //temp1.scl(s1.getFriction());
        //temp2.scl(s2.getFriction());

        // Apply to the objects
        // Vector2 zero_velocity = new Vector2(0,0);
        e1.setVX(0);
        e1.setVY(0);
        e2.setVX(0);
        e2.setVY(0);
    }

    /**
     * Collide an enemy with a werewolf.
     *
     * @param en The enemy
     * @param ww The werewolf
     */
    private void handleCollision(Enemy en, Werewolf ww) {
        // Prevent them from moving into each other
        if (en.isDestroyed() || ww.isDestroyed()) {
            return;
        }

        // Find the axis of "collision"
        temp1.set(en.getPosition()).sub(ww.getPosition());
        float dist = temp1.len();

        // Too far away
        if (dist > en.getRadius() + ww.getRadius()) {
            return;
        }

        // Push the ships out so that they do not collide
        float distToPush = 0.01f + (en.getRadius() + ww.getRadius() - dist) / 2;
        temp1.nor();
        temp1.scl(distToPush);
        en.getPosition().add(temp1);
        ww.getPosition().sub(temp1);

        // Compute the new velocities
        temp1.set(ww.getPosition()).sub(en.getPosition()).nor(); // Unit vector for w1
        temp2.set(en.getPosition()).sub(ww.getPosition()).nor(); // Unit vector for w2

        temp1.scl(temp1.dot(en.getLinearVelocity())); // Scaled to w1
        temp2.scl(temp2.dot(ww.getLinearVelocity())); // Scaled to w2

        // Apply to the objects
        //en. getVelocity().sub(temp1).add(temp2);
        //ww.getVelocity().sub(temp2).add(temp1);
//        if (en.isDestroyed() || ww.isDestroyed()) {
//            return;
//        }
//
//        // Find the axis of "collision"
//        temp1.set(en.getShadowposition()).sub(ww.getShadowposition());
//        float dist = temp1.len();
//
//        // Too far away
//        if (dist > en.getRadius() + ww.getRadius()) {
//            return;
//        }
//
//        System.out.println("----------------Enemy collides with enemy. NOT GOOD!");
//        // Push the enemies out so that they do not collide
//        float distToPush = 0.01f + (en.getRadius() + ww.getRadius() - dist) / 2;
//        temp1.nor();
//        temp1.scl(distToPush);
//        en.getPosition().add(temp1);
//        ww.getPosition().sub(temp1);
//
//        en.setVX(0);
//        en.setVY(0);
//        ww.setVX(0);
//        ww.setVY(0);

        // Find the axis of "collision"
        //TODO POSITION -> SHADOWPOSITION
//        temp1.set(en.getShadowposition()).sub(ww.getShadowposition());
//        float dist = temp1.len();
//
//        // Too far away
//        if (dist > en.getRadius() + ww.getRadius()) {
//            return;
//        }
//
//        System.out.println("----------------Enemy collides with Werewolf. NOT GOOD!");
//        // Push the enemies out so that they do not collide
//        float distToPush = 0.01f + (en.getRadius() + ww.getRadius() - dist) / 2;
//        temp1.nor();
//        temp1.scl(distToPush);
//        en.getPosition().add(temp1);
//        ww.getPosition().sub(temp1);
//
//        // TODO Compute the new velocities->not necessary, just set them back to 0
//        //temp1.set(e2.getPosition()).sub(e1.getPosition()).nor(); // Unit vector for w1
//        //temp2.set(e1.getPosition()).sub(e2.getPosition()).nor(); // Unit vector for w2
//
//        //temp1.scl(temp1.dot(e1.getVelocity())); // Scaled to w1
//        //temp2.scl(temp2.dot(e2.getVelocity())); // Scaled to w2
//
//        // You can remove this to add friction.  We find friction has nasty feedback.
//        //temp1.scl(s1.getFriction());
//        //temp2.scl(s2.getFriction());
//
//        // Apply to the objects
//        // Vector2 zero_velocity = new Vector2(0,0);
//        en.setVX(0);
//        en.setVY(0);
//        ww.setVX(0);
//        ww.setVY(0);
    }


    /**
     * Collide a werewolf with a werewolf.
     *
     * @param w1 The first werewolf
     * @param w2 The second werewolf
     */
    private void handleCollision(Werewolf w1, Werewolf w2) {
        //Does Nothing. There is only one werewolf!
    }

    /**
     * Check if a GameObject is out of walkable tiles and take action.
     *
     * @param o Object to check
     */
    private void processTiles(GameObject o) {
        // Dispatch the appropriate helper for each type
        switch (o.getType()) {
            case ENEMY:
                handleTiles((Enemy) o);
                break;
            case WEREWOLF:
                handleTiles((Werewolf) o);
                break;
            default:
                break;
        }
    }

    /**
     * Check if a Werewolf is out of walkable tiles and take action.
     *
     * @param ww Werewolf to check
     */
    private void handleTiles(Werewolf ww) {
        if (ww.isDestroyed()) {
            return;
        }
        //get the tile info for current object position
        Vector2 tile_position = curr_level.getBoard().worldToBoard(ww.getShadowposition().x, ww.getShadowposition().y);
        //int tile_x = curr_level.getBoard().worldToBoard(ww.getShadowposition().x);
        //int tile_y = curr_level.getBoard().worldToBoard(ww.getShadowposition().y);
        // Vector2 tile_position = curr_level.getBoard().getTilePosition(tile_x,tile_y);

        //checks whether the tile is walkable; if it is, do nothing
        if (curr_level.getBoard().isWalkable((int) tile_position.x, (int) tile_position.y)) {
        } else {
            // Find the axis of "collision"
            temp1.set(ww.getShadowposition()).sub(tile_position);
            float dist = temp1.len();
            // Push the enemies out so that they do not collide
            float distToPush = 0.01f + (curr_level.getBoard().getRadius() + ww.getRadius() - dist) / 2;
            temp1.nor();
            temp1.scl(distToPush);
            ww.getPosition().add(temp1);
            // Set the werewolf velocity back to 0
            ww.setVX(0);
            ww.setVY(0);
        }
    }

    /**
     * Check if an Enemy is out of walkable tiles and take action.
     *
     * @param en Enemy to check
     */
    private void handleTiles(Enemy en) {
        if (en.isDestroyed()) {
            return;
        }
        //get the tile info for current object position
        Vector2 tile_position = curr_level.getBoard().worldToBoard(en.getShadowposition().x, en.getShadowposition().y);
        //int tile_y = curr_level.getBoard().worldToBoard(en.getShadowposition().y);
        //Vector2 tile_position = curr_level.getBoard().getTilePosition(tile_x,tile_y);

        //checks whether the tile is walkable; if it is, do nothing
        if (curr_level.getBoard().isWalkable((int) tile_position.x, (int) tile_position.y)) {
        } else {
            System.out.println("----------------Enemy hits unwalkable tiles. NOT GOOD!");
            // Find the axis of "collision"
            temp1.set(en.getShadowposition()).sub(tile_position);
            float dist = temp1.len();
            // Push the enemies out so that they do not collide
            float distToPush = 0.01f + (curr_level.getBoard().getRadius() + en.getRadius() - dist) / 2;
            temp1.nor();
            temp1.scl(distToPush);
            en.getPosition().add(temp1);
            // Set the werewolf velocity back to 0
            en.setVX(0);
            en.setVY(0);
        }
    }

    //#endregion
}