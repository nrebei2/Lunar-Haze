package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.steer.behaviors.ReachOrientation;
import com.badlogic.gdx.ai.utils.ArithmeticUtils;
import com.badlogic.gdx.ai.utils.Location;
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
            System.out.println("NOTICED");
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
            Vector2 toTarget = (target.getPosition()).cpy().sub(entity.getEnemy().getPosition());
            float orientation = entity.getEnemy().vectorToAngle(toTarget);
            float rotation = ArithmeticUtils.wrapAngleAroundZero(orientation - entity.getEnemy().getOrientation());
            float rotationSize = rotation < 0f ? -rotation : rotation;

            if (rotationSize <= entity.faceSB.getAlignTolerance()) {
                INDICATOR.setTarget(target.getPosition());
                entity.getStateMachine().changeState(INDICATOR);
            }
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);

        }
    },

    INDICATOR() {
        Box2dLocation target = new Box2dLocation();
        @Override
        public void enter(EnemyController entity) {
            System.out.println("INDICATOR");
            entity.getEnemy().setDetection(Enemy.Detection.INDICATOR);
            entity.getEnemy().setIndicatorAmount(0);
            entity.arriveSB.setTarget(target);
            entity.getEnemy().setSteeringBehavior(entity.patrolSB);
        }

        @Override
        public void update(EnemyController entity) {

            if (entity.getEnemy().getIndicatorAmount() == 1) {
                entity.getStateMachine().changeState(ALERT);
            }

            // Check if have arrived to target position
            float dist = entity.getEnemy().getPosition().dst(entity.arriveSB.getTarget().getPosition());
            if (dist <= entity.arriveSB.getArrivalTolerance()) entity.getStateMachine().changeState(LOOK_AROUND);

            switch (entity.getDetection()) {
                case NOTICED:
                    target.setPosition(entity.target.getPosition());
                    entity.getEnemy().setIndicatorAmount(
                            MathUtils.clamp(entity.getEnemy().getIndicatorAmount() + Gdx.graphics.getDeltaTime() / 2, 0, 1)
                    );
                    break;
                case ALERT:
                    entity.getStateMachine().changeState(ALERT);
                    break;
                case NONE:
                    entity.getEnemy().setIndicatorAmount(
                            MathUtils.clamp(entity.getEnemy().getIndicatorAmount() - Gdx.graphics.getDeltaTime() / 3, 0, 1)
                    );
            }
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);
        }

        @Override
        public void setTarget(Vector2 pos) {
            this.target.setPosition(pos);
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
                    INDICATOR.setTarget(entity.target.getPosition());
                    entity.getStateMachine().changeState(INDICATOR);
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
//            entity.arriveSB.setTarget(new Box2dLocation(entity.getPatrolTarget()));
            System.out.println("Patrolling now...");

            Vector2 patrol = entity.getPatrolTarget();
            patrol = worldToGrid(patrol);
            while (entity.pathfinder.map.getNodeAt((int)patrol.x, (int)patrol.y).isObstacle){
                patrol = entity.getPatrolTarget();
                patrol = worldToGrid(patrol);
            }
            entity.nextNode = entity.pathfinder.map.getNodeAt((int)patrol.x, (int)patrol.y);

            System.out.println(entity.nextNode);
//            System.out.println("current location" + entity.getEnemy().getPosition());

        }

        @Override
        public void update(EnemyController entity) {

//            System.out.println(entity.getDetection());
            //check if spotted target
            if (entity.getDetection() == EnemyController.Detection.ALERT){
                System.out.println("found target");
                entity.getStateMachine().changeState(CHASE);
            }

            //             Check if have arrived to patrol position
            Vector2 cur_pos = worldToGrid(entity.getEnemy().getPosition());
            float dist = cur_pos.dst(entity.nextNode.x, entity.nextNode.y);
            if (dist <= 0.2f) entity.getStateMachine().changeState(LOOK_AROUND);
            else{
                Node target = entity.pathfinder.findNextNode(cur_pos, new Vector2(entity.nextNode.x, entity.nextNode.y));
                if (target != null) {
                    Vector2 target_world = gridToWorld(new Vector2(target.x, target.y));
//                    System.out.println("moving to next node" + target.x + ", " + target.y);
                    entity.setArriveSB(entity.getEnemy(), new Box2dLocation(target_world));
                    entity.getEnemy().setSteeringBehavior(entity.arriveSB);
//                System.out.println(target.x + ", "+target.y);
                }
                else{
//                    System.out.println("target is null");
                    entity.setArriveSB(entity.getEnemy(), new Box2dLocation(entity.getEnemy().getPosition()));
                    entity.getEnemy().setSteeringBehavior(entity.arriveSB);
//                System.out.println(entity.getEnemy().getPosition());
//                entity.getStateMachine().changeState(LOOK_AROUND);
                }

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

            if (entity.lookAroundSB.isFinished()) entity.getStateMachine().changeState(PATROL);
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);
        }
    },

    CHASE(){
        @Override
        public void update(EnemyController entity) {
//            Vector2 cur_pos = worldToGrid(entity.getEnemy().getPosition());
//            Node target = entity.pathfinder.findNextNode(cur_pos, new Vector2(entity.getTarget().getPosition()));
//            if (target != null) {
//                Vector2 target_world = gridToWorld(new Vector2(target.x, target.y));
//                entity.setArriveSB(entity.getEnemy(), new Box2dLocation(target_world));
//                entity.getEnemy().setSteeringBehavior(entity.arriveSB);
//            }
//            else{
//                entity.setArriveSB(entity.getEnemy(), new Box2dLocation(entity.getEnemy().getPosition()));
//                entity.getEnemy().setSteeringBehavior(entity.arriveSB);
//            }
            entity.setArriveSB(entity.getEnemy(), entity.getTarget());
            entity.getEnemy().setSteeringBehavior(entity.arriveSB);

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

    public Vector2 worldToGrid(Vector2 world){
        return new Vector2(world.x/LevelContainer.gridSize, world.y/LevelContainer.gridSize);
    }

    public Vector2 gridToWorld(Vector2 grid){
        return new Vector2(grid.x*LevelContainer.gridSize, grid.y*LevelContainer.gridSize);
    }

    protected void setTarget(Vector2 pos) {};
}
