package infinityx.lunarhaze.ai;

import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedSet;
import infinityx.lunarhaze.EnemyController;
import infinityx.lunarhaze.EnemyState;
import infinityx.lunarhaze.LevelContainer;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;

import java.util.ArrayList;
import java.util.List;

public class TacticalManager implements Telegraph {
    private Werewolf target;

    /** Set of alert enemy controllers */
    OrderedSet<EnemyController> enemies;

    public TacticalManager(LevelContainer container) {
        target = container.getPlayer();
        enemies = new OrderedSet<>();
        MessageManager.getInstance().addListener(this, ADD);
        MessageManager.getInstance().addListener(this, REMOVE);
    }

    public void update() {
        sendFlankMessage();
    }

    /** Send a flank message to all alert enemies */
    public void sendFlankMessage() {
        int i = 0;
        for (EnemyController control : enemies) {
            StateMachine enemy = control.getStateMachine();
            if (!enemy.isInState(EnemyState.ALERT)) continue;

            // Calculate angle step for evenly distributing the enemies around the target
            float angleStep = 360.0f / enemies.size;

            // Calculate the angle for this enemy
            float enemyAngle = angleStep * i;

            // Calculate a flanking position relative to the target
            Vector2 flankingPosition = target.getPosition().cpy().add(new Vector2(1, 0).rotate(enemyAngle));
            MessageManager.getInstance().dispatchMessage(null, enemy, FLANK, flankingPosition);

            i++;
        }
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        /** See {@link EnemyState#ALERT} */
        if (msg.message == ADD) enemies.add((EnemyController) msg.extraInfo);
        if (msg.message == REMOVE) enemies.remove((EnemyController) msg.extraInfo);
        return true;
    }

    public static int ADD = 100;
    public static int FLANK = 10012;
    public static int REMOVE = 100203;
}

//
//import com.badlogic.gdx.math.Vector2;
//import infinityx.lunarhaze.entity.Enemy;
//import infinityx.lunarhaze.entity.Werewolf;
//
//import java.util.List;
//
//public class TacticalManager {
//    private List<Enemy> enemies;
//
//    public TacticalManager(List<Enemy> enemies) {
//        this.enemies = enemies;
//    }
//
//    public void update(Werewolf player) {
//        for (Enemy enemy : enemies) {
//            if (enemy.isAlive()) {
//                // Determine whether to flank or evade based on player position and health
//                boolean flank = shouldFlank(enemy, player);
//                boolean evade = shouldEvade(enemy, player);
//                if (flank) {
//                    flankPlayer(enemy, player);
//                } else if (evade) {
//                    evadePlayer(enemy, player);
//                } else {
//                    attackPlayer(enemy, player);
//                }
//
//                // Avoid collisions with other enemies
//                avoidCollisions(enemy, enemies);
//            }
//        }
//    }
//
//    private boolean shouldFlank(Enemy enemy, Werewolf player) {
//        // Check if player is within a certain distance and health is above a certain threshold
//        double distance = getDistance(enemy.getPosition(), player.getPosition());
//        return distance <= enemy.getFlankDistance() && player.getHealth() > enemy.getFlankHealthThreshold();
//    }
//
//    private boolean shouldEvade(Enemy enemy, Werewolf player) {
//        // Check if player is within a certain distance and health is below a certain threshold
//        double distance = getDistance(enemy.getPosition(), player.getPosition());
//        return distance <= enemy.getEvadeDistance() && player.getHealth() < enemy.getEvadeHealthThreshold();
//    }
//
//    private void flankPlayer(Enemy enemy, Werewolf player) {
//        // Flank player by moving to a position behind them
//        double angle = getAngle(enemy.getPosition(), player.getPosition());
//        double flankAngle = angle + Math.PI / 2;
//        double flankDistance = enemy.getFlankDistance();
//        Vector2 flankPosition = getPosition(enemy.getPosition(), flankAngle, flankDistance);
//        enemy.moveTowards(flankPosition);
//    }
//
//    private void evadePlayer(Enemy enemy, Werewolf player) {
//        // Evade player by moving away from them
//        Vector2 direction = enemy.getPosition().subtract(player.getPosition());
//        direction = direction.normalize().scale(enemy.getEvadeDistance());
//        Vector2 evadePosition = enemy.getPosition().add(direction);
//        enemy.moveTowards(evadePosition);
//    }
//
//    private void attackPlayer(Enemy enemy, Werewolf player) {
//        // Attack player by moving towards them and dealing damage
//        enemy.moveTowards(player.getPosition());
//        player.takeDamage(enemy.getAttackDamage());
//    }
//
//    private void avoidCollisions(Enemy enemy, List<Enemy> enemies) {
//        // Avoid collisions by checking for nearby enemies and adjusting position
//        for (Enemy otherEnemy : enemies) {
//            if (otherEnemy.isAlive() && otherEnemy != enemy) {
//                double distance = getDistance(enemy.getPosition(), otherEnemy.getPosition());
//                if (distance <= enemy.getCollisionDistance()) {
//                    double angle = getAngle(enemy.getPosition(), otherEnemy.getPosition());
//                    double avoidAngle = angle + Math.PI / 2;
//                    double avoidDistance = enemy.getCollisionDistance() - distance;
//                    Vector2D avoidPosition = getPosition(enemy.getPosition(), avoidAngle, avoidDistance);
//                    enemy.moveTowards(avoidPosition);
//                }
//            }
//        }
//    }
//
//    private double getDistance(Vector2 position1, Vector2 position2) {
//        return position1.dst(position2);
//    }
//
//    private double getAngle(Vector2 position1, Vector2 position2) {
//        return Math.atan2(position2.y - position1.y, position2.x - position1.x);
//    }
//
//    private Vector2 getPosition(Vector2 position, double angle,
//}
