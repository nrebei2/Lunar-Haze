package infinityx.lunarhaze.models.entity;

import com.badlogic.gdx.utils.JsonValue;
import infinityx.assets.AssetDirectory;
import infinityx.lunarhaze.graphics.GameCanvas;
import infinityx.lunarhaze.models.AttackingGameObject;
import infinityx.lunarhaze.models.GameObject;
import infinityx.lunarhaze.models.LevelContainer;

public class Arrow extends GameObject {

    /**
     * Reference to the archer that drew this arrow
     */
    Archer archer;

    private float initialAngle;

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
        setLoop(true);
    }

    public void initialize(AssetDirectory directory, JsonValue json, LevelContainer container) {
        super.initialize(directory, json, container);
        this.setActive(false);

    }

    /**
     * Initialize arrow with dummy position
     */
    public Arrow(Archer archer) {
        this(0, 0, archer);
    }

    public Archer getArcher(){
        return archer;
    }

    public void setArcher(Archer archer){
        this.archer = archer;
    }

    @Override
    public void draw(GameCanvas canvas) {
        canvas.draw(filmstrip, tint, origin.x, origin.y,
                canvas.WorldToScreenX(getPosition().x), canvas.WorldToScreenY(getPosition().y), getInitialAngle(),
                textureScale * scale, textureScale * scale);
    }

    public float getInitialAngle(){
        return initialAngle;
    }

    public void setInitialAngle(float a){
        initialAngle = a;
    }
}
