package infinityx.lunarhaze;
/*
 * CollisionController.java
 *
 * This controller implements basic collision detection as described in
 * the instructions.  All objects in this game are treated as circles,
 * and a collision happens when circles intersect.
 *
 * This controller is EXTREMELY ineffecient.  To improve its performance,
 * you will need to use collision cells, as described in the instructions.
 * You should not need to modify any method other than the constructor
 * and processCollisions.  However, you will need to add your own methods.
 *
 * This is the only file that you need to modify as the first part of
 * the lab.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.math.*;
import infinityx.lunarhaze.entity.*;
import java.util.*;

import java.util.Objects;

/**
 * Controller implementing simple game physics.
 *
 * This is a very inefficient physics engine.  Part of this lab is determining
 * how to make it more efficient.
 */
public class CollisionController {

    // 'Bounciness' constants
    /** Restitution for colliding with the (hard coded) box */
    protected static final float BOX_COEFF_REST   = 0.95f;
    /** Restitution for colliding with the (hard coded) bump */
    protected static final float BUMP_COEFF_REST  = 1.95f;
    /** Dampening factor when colliding with floor or shell */
    protected static final float DAMPENING_FACTOR = 0.95f;

    // Geometry of the background image
    /** (Scaled) distance of the floor ledge from bottom */
    protected static final float BOTTOM_OFFSET    = 0.075f;
    /** (Scaled) position of the box center */
    protected static final float BOX_X_POSITION   = 0.141f;
    /** (Scaled) position of half the box width */
    protected static final float BOX_HALF_WIDTH   = 0.133f;
    /** (Scaled) position of the box height from bottom of screen */
    protected static final float BOX_FULL_HEIGHT  = 0.2f;
    /** (Scaled) position of the bump center */
    protected static final float BUMP_X_POSITION  = 0.734f;
    /** (Scaled) position of the bump radius */
    protected static final float BUMP_RADIUS      = 0.11f;

    // These cannot be modified after the controller is constructed.
    // If these change, make a new constructor.
    /** Width of the collision geometry */
    private float width;
    /** Height of the collision geometry */
    private float height;

    // Cache objects for collision calculations
    private Vector2 temp1;
    private Vector2 temp2;

    private int parameter = 36;
    private ArrayList<ArrayList<ArrayList<GameObject>>> cells = new ArrayList<>();


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
     * Returns the height of the floor ledge.
     *
     * The floor ledge supports the player ship, and is what all of the shells
     * bounce off of.  It is raised slightly higher than the bottom of the screen.
     *
     * @return the height of the floor ledge.
     */
    public float getFloorLedge() {
        return BOTTOM_OFFSET*height;
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
     *
     * The box edges are x+/- this width.
     *
     * @return half of the width of the square box
     */
    public float getBoxRadius() {
        return BOX_HALF_WIDTH * width;
    }

    /**
     * Returns height of the square box in the background image
     *
     * Height is measured from the bottom of the screen, not the ledge.
     */
    public float getBoxHeight() {
        return BOX_FULL_HEIGHT * height;
    }

    /**
     * Returns x-coordinate of the center of the semicircular bump in the background image
     *
     * @return x-coordinate of the center of the semicircular bump
     */
    protected float getBumpX() {
        return BUMP_X_POSITION * width;
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
     * @param width   Width of the screen
     * @param height  Height of the screen
     */
    public CollisionController(float width, float height) {
        this.width = width;
        this.height = height;

        // Initialize cache objects
        temp1 = new Vector2();
        temp2 = new Vector2();
    }

    /**
     * This method assign objects currently on the screen into cells.
     *
     * @param objects List of live objects to check
     * @param offset  Offset of the box and bump
     */
    public void AssignCells(Array<GameObject> objects, int offset) {
        // For each shell, check for collisions with the special terrain elements
        float width_divider = this.getWidth()/parameter;
        float height_divider = this.getWidth()/parameter;

        // Initializes cells
        cells.clear();
        for (int ii = 0; ii < parameter; ii++){
            cells.add(new ArrayList<ArrayList<GameObject>>());
        }
        for (int jj = 0; jj < cells.size(); jj++){
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
                cells.get((int) Math.floor(o.getX() / width_divider)).get((int) Math.floor(o.getY()/height_divider)).add(o);
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
     * @param offset  Offset of the box and bump
     */
    public void processCollisions(Array<GameObject> objects, int offset) {
        AssignCells(objects,offset);
        for (int ii = 0; ii < cells.size(); ii++){
            //System.out.println("2");
            for (int jj = 0; jj < cells.get(ii).size(); jj++){
                //System.out.println("3");
                for (GameObject o : cells.get(ii).get(jj)) {
                    //System.out.println("4");
                    for (int kk = 0; kk < cells.get(ii).get(jj).size(); kk++) {
                        //System.out.println("5");
                        if (cells.get(ii).get(jj).get(kk) != o) {
                            //System.out.println("6");
                            processCollision(o,cells.get(ii).get(jj).get(kk));
                        }
                    }
                    if (ii != 0){
                        for (int kk = 0; kk < cells.get(ii - 1).get(jj).size(); kk++) {
                            processCollision(o,cells.get(ii - 1).get(jj).get(kk));
                        }
                    }
                    if (ii != parameter-1){
                        for (int kk = 0; kk < cells.get(ii + 1).get(jj).size(); kk++) {
                            processCollision(o,cells.get(ii + 1).get(jj).get(kk));
                        }
                    }
                    if (jj != 0){
                        for (int kk = 0; kk < cells.get(ii).get(jj - 1).size(); kk++) {
                            processCollision(o,cells.get(ii).get(jj - 1).get(kk));
                        }
                    }
                    if (jj != parameter-1){
                        for (int kk = 0; kk < cells.get(ii).get(jj + 1).size(); kk++) {
                            processCollision(o,cells.get(ii).get(jj + 1).get(kk));
                        }
                    }
                }
            }
        }
    }

    //#endregion

    //#region Collision Handlers (DO NOT MODIFY FOR PART 1)

    /**
     * Check if a GameObject is out of bounds and take action.
     *
     * Obviously an object off-screen is out of bounds.  In the case of shells, the
     * box and bump are also out of bounds.
     *
     * @param o      Object to check
     */
    private void processBounds(GameObject o) {
        // Dispatch the appropriate helper for each type
        switch (o.getType()) {
            case ENEMY:
                handleBounds((Enemy)o);
                break;
            case WEREWOLF:
                handleBounds((Werewolf)o);
                break;
            default:
                break;
        }
    }

    /**
     * Check an enemy for being out-of-bounds.
     *
     * Obviously an enemy off-screen is out of bounds.
     *
     * @param em     Shell to check
     */
    private void handleBounds(Enemy em) {
        //TODO
        // Check if off right side
        if (em.getX() > getWidth() - em.getRadius()) {
            // Set within bounds on right and prevents from moving out of bounds
            em.setX(2 * (getWidth() - em.getRadius()) - em.getX());
            em.setVX(0);
        }
        // Check if off left side
        else if (em.getX() < em.getRadius()) {
            // Set within bounds on left and prevents from moving out of bounds
            em.setX(2 * em.getRadius() - em.getX());
            em.setVX(0);
        }

        // Check for in bounds on bottom
        if (em.getY()-em.getRadius() < getFloorLedge()) {
            // Set within bounds on bottom and swap velocity
            em.setY(getFloorLedge()+em.getRadius());
            em.setVY(-em.getVY());

            //TODO Constrict velocity
            //sh.setVY((float)Math.max(sh.getMinVY(), sh.getVY() * sh.getFriction()));
        }
    }


    /**
     * Check a werewolf for being out-of-bounds.
     *
     * Obviously a werewolf off-screen is out of bounds.
     *
     * @param ww     Werewolf to check
     */
    private void handleBounds(Werewolf ww) {
        //TODO
        // Check if off right side
        if (ww.getX() > getWidth() - ww.getRadius()) {
            // Set within bounds on right and prevents from moving out of bounds
            ww.setX(2 * (getWidth() - ww.getRadius()) - ww.getX());
            ww.setVX(0);
        }
        // Check if off left side
        else if (ww.getX() < ww.getRadius()) {
            // Set within bounds on left and prevents from moving out of bounds
            ww.setX(2 * ww.getRadius() - ww.getX());
            ww.setVX(0);
        }

        // Check for in bounds on bottom
        if (ww.getY()-ww.getRadius() < getFloorLedge()) {
            // Set within bounds on bottom and swap velocity
            ww.setY(getFloorLedge()+ww.getRadius());
            ww.setVY(-ww.getVY());

            //TODO Constrict velocity
            //sh.setVY((float)Math.max(sh.getMinVY(), sh.getVY() * sh.getFriction()));
        }
    }

    /**
     * Check a object for being on a walkable tile.
     *
     *
     * @param ww     Werewolf to check
     */
    private void checkWalkable(Werewolf ww) {
        //TODO
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
            if (o.getX()+o.getRadius() > x+getBoxRadius()) {
                o.setX(x+getBoxRadius()+o.getRadius());
                o.setVX(-o.getVX());
            } else if (o.getX()-o.getRadius() < x-getBoxRadius()) {
                o.setX(x-getBoxRadius()-o.getRadius());
                o.setVX(-o.getVX());
            } else {
                o.setVY(-o.getVY() * BOX_COEFF_REST);
                o.setY(getBoxHeight()+o.getRadius());
            }
        }
    }

    //TODO Not sure whether we need this method but still just gonna keep it here
    /**
     * Detect collision with semicircular bump in the terrain.
     *
     * @param o Object to check
     * @param x Offset of the bump
     */
    public void hitBump(GameObject o, float x) {
        // Make sure to not just change the velocity but also move the
        // object so that it no longer penetrates the terrain.
        float dx = o.getX() - x;
        float dy = o.getY() - getFloorLedge();
        float dist = (float)Math.sqrt(dx * dx + dy * dy);
        if (dist < 0.1f * width) {
            float norm_x = dx / dist;
            float norm_y = Math.abs(dy / dist);
            float tmp = (o.getVX() * norm_x + o.getVY() * norm_y)*BUMP_COEFF_REST;
            o.getVelocity().sub(norm_x * tmp, norm_y * tmp);
            o.setY(getFloorLedge() + norm_y * getBumpRadius());
        }
    }


    /**
     * Detect and resolve collisions between two game objects
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
                        handleCollision((Enemy)o1, (Enemy)o2);
                        break;
                    case WEREWOLF:
                        handleCollision((Enemy)o1, (Werewolf)o2);
                        break;
                    default:
                        break;
                }
                break;
            case WEREWOLF:
                switch (o2.getType()) {
                    case ENEMY:
                        // Reuse shell helper
                        handleCollision((Enemy)o2, (Werewolf)o1);
                        break;
                    case WEREWOLF:
                        handleCollision((Werewolf)o1, (Werewolf)o2);
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
        temp1.set(e1.getPosition()).sub(e2.getPosition());
        float dist = temp1.len();

        // Too far away
        if (dist > e1.getRadius() + e2.getRadius()) {
            return;
        }

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

        // Push the enemies out so that they do not collide
        float distToPush = 0.01f + (en.getRadius() + ww.getRadius() - dist) / 2;
        temp1.nor();
        temp1.scl(distToPush);
        en.getPosition().add(temp1);
        ww.getPosition().sub(temp1);

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
        en.setVX(0);
        en.setVY(0);
        ww.setVX(0);
        ww.setVY(0);
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


    //#endregion
}