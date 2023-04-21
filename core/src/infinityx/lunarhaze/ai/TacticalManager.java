package infinityx.lunarhaze.ai;

import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedSet;
import infinityx.lunarhaze.controllers.EnemyController;
import infinityx.lunarhaze.controllers.EnemyState;
import infinityx.lunarhaze.models.LevelContainer;
import infinityx.lunarhaze.models.entity.Enemy;
import infinityx.lunarhaze.models.entity.Werewolf;

import java.util.Random;

/**This class used to send instructions to each enemy in the level. It should handle strategic decisions in the
 * battle phase including flank or evade for example*/
public class TacticalManager implements Telegraph {

    /** The target of an enemy*/
    private Werewolf target;
    Random rand = new Random();

    /** The list of current active enemies*/
    private Array<Enemy> activeEnemies;

    /** A map of enemies to their corresponding controllers*/
    private ObjectMap<Enemy, EnemyController> enemyMap;

    private LevelContainer container;


    /**
     * Set of alert enemy controllers
     */
    OrderedSet<EnemyController> enemies = new OrderedSet<>();

    public TacticalManager(LevelContainer container) {
        target = container.getPlayer();
        activeEnemies = container.getEnemies();
        enemyMap = container.getEnemyControllers();
        this.container = container;
        MessageManager.getInstance().addListener(this, ADD);
        MessageManager.getInstance().addListener(this, REMOVE);
        MessageManager.getInstance().addListener(this, FOUND);
    }

    public void update() {
        sendFlankMessage();
    }

    /**
     * Send a flank message to all alert enemies
     */
    public void sendFlankMessage() {
        int i = 0;
        for (EnemyController control : enemies) {
            StateMachine<EnemyController, EnemyState> enemy = control.getStateMachine();
            if (!enemy.isInState(EnemyState.ALERT)) continue;
            if (rand.nextFloat() <= 0.3) {
                // Calculate angle step for evenly distributing the enemies around the target
                float angleStep = 360.0f / enemies.size;

                // Calculate the angle for this enemy
                float enemyAngle = angleStep * i;

                // Calculate a flanking position relative to the target
                /* TODO: remove new  (make own rotate Deg)*/
                Vector2 flankingPosition = target.getPosition().cpy().add(rotateDegreeX(enemyAngle, 1,0),rotateDegreeY(enemyAngle, 1,0) );
                if (container.pathfinder.map.getNodeAtWorld(flankingPosition.x, flankingPosition.y) == null) {
                    continue;
                }
                MessageManager.getInstance().dispatchMessage(null, enemy, FLANK, flankingPosition);
            } else {
                MessageManager.getInstance().dispatchMessage(null, enemy, ATTACK);
            }

            i++;
        }
    }
    /**Alert nearby allies that target is spotted*/
    public void alertAllies(EnemyController entity) {
        for (Enemy enemy : activeEnemies) {
            EnemyController control = enemyMap.get(enemy);
            entity.findCollision(control.getEnemy());
            // FIXME: Should only call an enemy that is visible from entity.enemy
            if (control != entity && (entity.getEnemy().getPosition()).cpy().dst(control.getEnemy().getPosition()) <= 5f
                && entity.communicationCollision.hitObject == control.getEnemy()) {
                System.out.println("alerting");
                StateMachine<EnemyController, EnemyState> machine = control.getStateMachine();
                machine.changeState(EnemyState.ALERT);
            }
        }
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        /** See {@link EnemyState#ALERT} */
        if (msg.message == ADD) enemies.add((EnemyController) msg.extraInfo);
        if (msg.message == REMOVE) enemies.remove((EnemyController) msg.extraInfo);
        if (msg.message == FOUND) {
            alertAllies((EnemyController) msg.extraInfo);
        }
        return true;
    }
    /** Rotate a vector by degree and returns the x component
     *
     * Params:
     * degree - degree to rotate
     * x - x component of vector
     * y - y component of vector
     * */
    public float rotateDegreeX (float degree, float x, float y) {

        float radians = degree * MathUtils.degreesToRadians;
        float cos = (float)Math.cos(radians);
        float sin = (float)Math.sin(radians);

        float newX = x * cos - y * sin;
        return newX;
    }
    /** Rotate a vector by degree and returns the y component
     *
     * Params:
     * degree - degree to rotate
     * x - x component of vector
     * y - y component of vector
     * */
    public float rotateDegreeY(float degree, float x, float y) {

        float radians = degree * MathUtils.degreesToRadians;
        float cos = (float)Math.cos(radians);
        float sin = (float)Math.sin(radians);

        float newY = x * sin + y * cos;

        return newY;
    }

    public static int ADD = 100;

    public static int FOUND = 10;
    public static int FLANK = 10012;
    public static int REMOVE = 100203;

    public static int ATTACK = 1001001;

}
