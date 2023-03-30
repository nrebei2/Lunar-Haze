package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.steer.behaviors.ReachOrientation;
import com.badlogic.gdx.ai.utils.ArithmeticUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.entity.Enemy;
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

    NOTICED() {
        Box2dLocation target;
        @Override
        public void enter(EnemyController entity) {
            System.out.println("?");
            target = new Box2dLocation(entity.target);
            entity.getEnemy().setIndependentFacing(true);
            entity.getEnemy().setLinearVelocity(Vector2.Zero);
            entity.getEnemy().setDetection(Enemy.Detection.NOTICED);
            entity.faceSB.setTarget(target);
            entity.getEnemy().setSteeringBehavior(entity.faceSB);
        }

        @Override
        public void update(EnemyController entity) {

            // Check if we faced target
            Vector2 toTarget = (target.getPosition()).sub(entity.getEnemy().getPosition());
            float orientation = entity.getEnemy().vectorToAngle(toTarget);
            float rotation = ArithmeticUtils.wrapAngleAroundZero(orientation - entity.getEnemy().getOrientation());
            float rotationSize = rotation < 0f ? -rotation : rotation;

            if (rotationSize <= entity.faceSB.getAlignTolerance()) {
                INDICATOR.setTarget(target);
                entity.getStateMachine().changeState(INDICATOR);
            }
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);

        }
    },

    INDICATOR() {
        Box2dLocation target;
        @Override
        public void enter(EnemyController entity) {
            System.out.println("Circle increasing");
            entity.getEnemy().setDetection(Enemy.Detection.INDICATOR);
            entity.getEnemy().setIndicatorAmount(0);
            entity.arriveSB.setTarget(target);
            entity.getEnemy().setSteeringBehavior(entity.patrolSB);
        }

        @Override
        public void update(EnemyController entity) {

            entity.getEnemy().setIndicatorAmount(
                    MathUtils.clamp(entity.getEnemy().getIndicatorAmount() + Gdx.graphics.getDeltaTime(), 0, 1)
            );

            if (entity.getEnemy().getIndicatorAmount() == 1) {
                entity.getStateMachine().changeState(ALERT);
            }

            // Check if have arrived to target position
            float dist = entity.getEnemy().getPosition().dst(entity.arriveSB.getTarget().getPosition());
            if (dist <= entity.arriveSB.getArrivalTolerance()) entity.getStateMachine().changeState(LOOK_AROUND);

            switch (entity.getDetection()) {
                case NOTICED:
                    target.setPosition(entity.target.getPosition());
                    break;
                case ALERT:
                    entity.getStateMachine().changeState(ALERT);
                    break;
            }
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);
        }

        @Override
        public void setTarget(Box2dLocation loc) {
            this.target = loc;
        }
    },

    ALERT() {
       @Override
       public void enter(EnemyController entity) {
            System.out.println("!");
            entity.getEnemy().setDetection(Enemy.Detection.ALERT);
            entity.arriveSB.setTarget(entity.target);
            entity.getEnemy().setSteeringBehavior(entity.patrolSB);
        }

        @Override
        public void update(EnemyController entity) {
            // Check if have arrived to target
            //float dist = entity.getEnemy().getPosition().dst(entity.arriveSB.getTarget().getPosition());
            //if (dist <= entity.arriveSB.getArrivalTolerance()) entity.getStateMachine().changeState(ATTACK);

            switch (entity.getDetection()) {
                case NONE:
                    entity.getStateMachine().changeState(NOTICED);
                    break;
            }
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);
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

            switch (entity.getDetection()) {
                case NOTICED:
                    entity.getStateMachine().changeState(NOTICED);
                    break;
                case ALERT:
                    entity.getStateMachine().changeState(ALERT);
                    break;
            }
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);
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

            switch (entity.getDetection()) {
                case NOTICED:
                    entity.getStateMachine().changeState(NOTICED);
                    break;
                case ALERT:
                    entity.getStateMachine().changeState(ALERT);
                    break;
            }

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

    protected void setTarget(Box2dLocation loc) {};
}
