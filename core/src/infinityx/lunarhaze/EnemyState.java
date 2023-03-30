package infinityx.lunarhaze;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import infinityx.util.Box2dLocation;
import infinityx.lunarhaze.LevelContainer;
import infinityx.util.astar.Node;

public enum EnemyState implements State<EnemyController> {

    /**
     * Initial state, used oth. enter on patrol would not be called
     */
    INIT() {
        @Override
        public void update(EnemyController entity) {
            entity.getStateMachine().changeState(PATROL);
        }
    },

    /**
     * Similar to unity's any state
     */
    ANY_STATE() {
        @Override
        public void enter(EnemyController entity) {

        }

        @Override
        public void update(EnemyController entity) {

        }

        @Override
        public void exit(EnemyController entity) {

        }
    },

    PATROL() {
        @Override
        public void enter(EnemyController entity) {
            System.out.println("Patrolling now...");
            entity.arriveSB.setTarget(new Box2dLocation(entity.getPatrolTarget()));
            entity.getEnemy().setSteeringBehavior(entity.patrolSB);
        }

        @Override
        public void update(EnemyController entity) {
            // Check if have arrived to patrol position
            float dist = entity.getEnemy().getPosition().dst(entity.arriveSB.getTarget().getPosition());
            if (dist <= entity.arriveSB.getArrivalTolerance()) entity.getStateMachine().changeState(LOOK_AROUND);
        }

        @Override
        public void exit(EnemyController entity) {

        }
    },

    LOOK_AROUND() {
        @Override
        public void enter(EnemyController entity) {
            System.out.println("Looking around now...");
            entity.lookAroundSB.reset();
            entity.getEnemy().setIndependentFacing(true);
            entity.getEnemy().setLinearVelocity(Vector2.Zero);
            entity.getEnemy().setSteeringBehavior(entity.lookAroundSB);
        }

        @Override
        public void update(EnemyController entity) {
            //System.out.printf("angular: %f\n", entity.getEnemy().getSteeringAngular());
            if (entity.lookAroundSB.isFinished()) entity.getStateMachine().revertToPreviousState();
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);
        }
    };

    @Override
    public void enter(EnemyController entity) {
    }

    @Override
    public void exit(EnemyController entity) {
    }

    @Override
    public boolean onMessage(EnemyController control, Telegram telegram) {
        return false;
    }

}
