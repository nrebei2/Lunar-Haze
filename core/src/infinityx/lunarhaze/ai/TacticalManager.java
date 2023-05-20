package infinityx.lunarhaze.ai;

import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.utils.Array;
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
     * Reference of active controllers from container
     */
    private final Array<EnemyController> controllers;

    private final LevelContainer container;


    /**
     * Set of alert enemy controllers
     */
    OrderedSet<EnemyController> enemies = new OrderedSet<>();

    public TacticalManager(LevelContainer container) {
        target = container.getPlayer();
        controllers = container.getActiveControllers();
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
            if (control.isBehind(control.getEnemy(), target) && control.getAttackHandler().canStartNewAttack()) {
                MessageManager.getInstance().dispatchMessage(null, enemy, ATTACK);
            } else if (control.getAttackHandler().canStartNewAttack() && rand.nextFloat() <= 0.2f) {
                //attacking from front
                MessageManager.getInstance().dispatchMessage(null, enemy, ATTACK);
            } else {
                MessageManager.getInstance().dispatchMessage(null, enemy, STRAFE);
            }

            i++;
        }
    }

    /**
     * Alert nearby allies that target is spotted
     */
    public void alertAllies(EnemyController entity) {
        for (EnemyController control : controllers) {
            entity.findCollision(control.getEnemy());
            if (control != entity && (entity.getEnemy().getPosition()).dst(control.getEnemy().getPosition()) <= 7f
                    && entity.communicationCollision.hitObject == control.getEnemy() && !control.getEnemy().isAttacking()) {
//                System.out.println("alerting");
                entity.getEnemy().setAlerting(true);
                StateMachine<EnemyController, EnemyState> machine = control.getStateMachine();
                machine.changeState(EnemyState.ALERT);


            }
        }
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        /** See {@link EnemyState#ALERT} */
        if (msg.message == ADD && !enemies.contains((EnemyController) msg.extraInfo)) enemies.add((EnemyController) msg.extraInfo);
//        if (msg.message == ADD) enemies.add((EnemyController) msg.extraInfo);
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
