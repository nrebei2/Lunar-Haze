package infinityx.lunarhaze.models.entity;

import infinityx.lunarhaze.models.GameObject;

public class Arrow extends GameObject {

    /**
     * Reference to the archer that drew this arrow
     */
    Archer archer;

    @Override
    public ObjectType getType() {
        return ObjectType.ARROW;
    }

    /**
     * Initialize an arrow
     */
    public Arrow(float x, float y, Archer archer) {
        super(x, y);
        this.archer = archer;
        setLoop(false);
    }

    /**
     * Initialize arrow with dummy position
     */
    public Arrow(Archer archer) {
        this(0, 0, archer);
    }
}
