package infinityx.lunarhaze.ai;

import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
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

/**
 * This class used to send instructions to each enemy in the level. It should handle strategic decisions in the
 * battle phase including flank or evade for example
 */
public class TacticalManager implements Telegraph {

    /**
     * The target of an enemy
     */
    private final Werewolf target;
    Random rand = new Random();

    /**
     * The list of current active enemies
     */
    private final Array<Enemy> activeEnemies;

    /**
     * A map of enemies to their corresponding controllers
     */
    private final ObjectMap<Enemy, EnemyController> enemyMap;

    private final LevelContainer container;


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
        sendAction();
    }

    /**
     * Send a flank message to all alert enemies
     */
    public void sendAction() {
        int i = 0;
        for (EnemyController control : enemies) {
            StateMachine<EnemyController, EnemyState> enemy = control.getStateMachine();
            if (!enemy.isInState(EnemyState.ALERT)) continue;
            //change strafe rotation
            if (rand.nextFloat() <= 0.2f) {
                control.strafe.changeRotation();
            }
//            if (rand.nextFloat() <= 0.3f){
//                control.getEnemy().updateStrafeDistance();
//            }
            //if behind enemy go attack
            if (control.isBehind(control.getEnemy(), target) && control.canStartNewAttack()) {
                MessageManager.getInstance().dispatchMessage(null, enemy, ATTACK);
            }
            else if (control.canStartNewAttack() && rand.nextFloat() <= 0.1f){
                //attacking from front
                MessageManager.getInstance().dispatchMessage(null, enemy, ATTACK);
            }
            else {
                MessageManager.getInstance().dispatchMessage(null, enemy, STRAFE);
            }

            i++;
        }
    }

    /**
     * Alert nearby allies that target is spotted
     */
    public void alertAllies(EnemyController entity) {
        for (Enemy enemy : activeEnemies) {
            EnemyController control = enemyMap.get(enemy);
            entity.findCollision(control.getEnemy());
            // FIXME: Should only call an enemy that is visible from entity.enemy
            if (control != entity && (entity.getEnemy().getPosition()).dst(control.getEnemy().getPosition()) <= 5f
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

    public static int ADD = 100;

    public static int FOUND = 10;
    public static int REMOVE = 100203;

    public static int ATTACK = 1001001;

    public static int STRAFE = 1001;

}
