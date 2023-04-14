package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.utils.Path;
import com.badlogic.gdx.ai.utils.ArithmeticUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.ai.TacticalManager;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.util.Box2dLocation;

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
            entity.getEnemy().setDetection(Enemy.Detection.INDICATOR);
            entity.getEnemy().setIndicatorAmount(0);
            entity.target.setStealth(entity.target.getStealth() + 1f);
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
                case ALERT:
                    target.setPosition(entity.target.getPosition());
                    entity.getEnemy().setIndicatorAmount(
                            MathUtils.clamp(entity.getEnemy().getIndicatorAmount() + Gdx.graphics.getDeltaTime() / 2, 0, 1)
                    );
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
            entity.target.setStealth(entity.target.getStealth() - 1f);
        }

        @Override
        public void setTarget(Vector2 pos) {
            this.target.setPosition(pos);
        }
    },

    ALERT() {
        /** Tick count */
        int tick;

        @Override
        public void enter(EnemyController entity) {
            entity.getEnemy().setDetection(Enemy.Detection.ALERT);
            entity.target.setStealth(entity.target.getStealth() + 1);

            Vector2 cur_pos = worldToGrid(entity.getEnemy().getPosition());
            Vector2 player_pos = worldToGrid(entity.getTarget().getPosition());
            Path path = entity.pathfinder.findPath(cur_pos, player_pos, entity.raycastCollisionDetector);
            if (path != null) {
                entity.followPathSB = new FollowPath(entity.getEnemy(), path, 0.05f, 0.5f);
                entity.getEnemy().setSteeringBehavior(entity.followPathSB);
            }
            MessageManager.getInstance().dispatchMessage(TacticalManager.ADD, entity);

            tick = 0;
        }

        @Override
        public void update(EnemyController entity) {
            // Check if have arrived to target
            //float dist = entity.getEnemy().getPosition().dst(entity.arriveSB.getTarget().getPosition());
            //if (dist <= entity.arriveSB.getArrivalTolerance()) entity.getStateMachine().changeState(ATTACK);
            switch (entity.getDetection()) {
                case NONE:
                    INDICATOR.setTarget(entity.target.getPosition());
                    entity.getStateMachine().changeState(NOTICED);
                    break;
            }

            tick++;
            // Update path every 30 frames
            if (tick % 30 == 0) {
                Vector2 cur_pos = worldToGrid(entity.getEnemy().getPosition());
                Vector2 player_pos = worldToGrid(entity.getTarget().getPosition());
                Path path = entity.pathfinder.findPath(cur_pos, player_pos, entity.raycastCollisionDetector);
                if (path != null) {
                    entity.followPathSB.setPath(path);
                }
            }
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);
            MessageManager.getInstance().dispatchMessage(TacticalManager.REMOVE, entity);
            entity.target.setStealth(entity.target.getStealth() - 1);
        }

        @Override
        public boolean onMessage(EnemyController control, Telegram telegram) {
            if (telegram.message == TacticalManager.FLANK) {
                Vector2 flank_pos = worldToGrid((Vector2) telegram.extraInfo);
                Vector2 cur_pos = worldToGrid(control.getEnemy().getPosition());
                Path path = control.pathfinder.findPath(cur_pos, flank_pos, control.raycastCollisionDetector);
                if (path != null) {
                    control.followPathSB = new FollowPath(control.getEnemy(), path, 0.05f, 0.5f);
                    control.getEnemy().setSteeringBehavior(control.followPathSB);
                }
//                Vector2 flank_pos = (Vector2) telegram.extraInfo;
//                control.setArriveSB(control.getEnemy(), new Box2dLocation(flank_pos));
//                control.getEnemy().setSteeringBehavior(control.arriveSB);
            } else {
                control.getStateMachine().changeState(ALERT);
            }
            return true;
        }
    },

    PATROL() {
        @Override
        public void enter(EnemyController entity) {
            Vector2 patrol = entity.getPatrolTarget();
            patrol = worldToGrid(patrol);
            while (entity.pathfinder.map.getNodeAt((int) patrol.x, (int) patrol.y).isObstacle) {
                patrol = entity.getPatrolTarget();
                patrol = worldToGrid(patrol);
            }
            entity.nextNode = entity.pathfinder.map.getNodeAt((int) patrol.x, (int) patrol.y);
            Vector2 cur_pos = worldToGrid(entity.getEnemy().getPosition());


            Path path = entity.pathfinder.findPath(cur_pos, new Vector2(entity.nextNode.x, entity.nextNode.y), entity.raycastCollisionDetector);
            if (path != null) {
                entity.followPathSB = new FollowPath(entity.getEnemy(), path, 0.05f, 1f);
                entity.getEnemy().setSteeringBehavior(entity.followPathSB);
            }


            entity.getEnemy().setDetection(Enemy.Detection.NONE);

        }

        @Override
        public void update(EnemyController entity) {

            // Check if have arrived to patrol position
            float dist = entity.getEnemy().getPosition().dst(entity.nextNode.wx, entity.nextNode.wy);
            if (dist <= 0.1f) entity.getStateMachine().changeState(LOOK_AROUND);

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
            entity.lookAroundSB.reset();
            entity.getEnemy().setIndependentFacing(true);
            entity.getEnemy().setLinearVelocity(Vector2.Zero);
            entity.getEnemy().setAngularVelocity(0);
            entity.getEnemy().setSteeringBehavior(entity.lookAroundSB);

            entity.getEnemy().setDetection(Enemy.Detection.NONE);
        }

        @Override
        public void update(EnemyController entity) {
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

    public Vector2 worldToGrid(Vector2 world) {
        return new Vector2(world.x / LevelContainer.gridSize, world.y / LevelContainer.gridSize);
    }

    protected void setTarget(Vector2 pos) {
    }

    ;
}
