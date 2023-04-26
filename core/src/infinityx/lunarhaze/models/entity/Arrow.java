package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.models.GameObject;

public class Arrow extends GameObject {

    /**
     * Linear velocity
     */
    private Vector2 velocity;

    /**
     * Angular velocity
     */
    private float angularVelocity;

    /**
     * Attack knockback of the arrow
     */
    private float attackKnockback;

    /**
     * Damage to the player
     */
    private int attackDamage;

    public Vector2 getVelocity(){
        return velocity;
    }

    public void setVelocity(Vector2 vec){
        velocity = vec;
    }

    public float getVelocityX(){
        return velocity.x;
    }

    public void setVelocityX(float val){
        velocity.x = val;
    }

    public float getVelocityY(){
        return velocity.y;
    }

    public void setVelocityY(float val){
        velocity.y = val;
    }

    public float getAngularVelocity(){
        return angularVelocity;
    }

    public void setAngularVelocity(float val){
        angularVelocity = val;
    }

    public float getAttackKnockback(){
        return attackKnockback;
    }

    public void setAttackKnockback(float kb){
        attackKnockback = kb;
    }

    public int getAttackDamage(){
        return attackDamage;
    }

    public void setAttackDamage(int dm){
        attackDamage = dm;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.ARROW;
    }

    /**
     * Initialize an arrow
     */
    public Arrow(float x, float y) {
        super(x, y);
    }

    /**
     * Initialize arrow with dummy position
     */
    public Arrow() {
        this(0, 0);
    }
}
