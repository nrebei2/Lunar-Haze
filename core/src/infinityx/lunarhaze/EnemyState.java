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

    /** Question mark above enemy, turns towards location */
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

    /** The enemy is going towards last known player position and indicator bar is (maybe) increasing */
    INDICATOR() {
        /** Last known player position */
        Box2dLocation target = new Box2dLocation();

        @Override
        public void enter(EnemyController entity) {
            entity.getEnemy().setDetection(Enemy.Detection.INDICATOR);
            entity.getEnemy().setIndicatorAmount(0);

            // TODO
            entity.target.setStealth(entity.target.getStealth() + 1f);

            Vector2 cur_pos = worldToGrid(entity.getEnemy().getPosition());
            Vector2 target_pos = worldToGrid(target.getPosition());
            Path path = entity.pathfinder.findPath(cur_pos, target_pos, entity.raycastCollisionDetector);
            if (path != null) {
                entity.followPathSB = new FollowPath(entity.getEnemy(), path, 0.05f, 0.5f);
                entity.getEnemy().setSteeringBehavior(entity.followPathSB);
            }
        }

        @Override
        public void update(EnemyController entity) {

            // If the indicator is full, the enemy is alerted
            if (entity.getEnemy().getIndicatorAmount() == 1) {
                entity.getStateMachine().changeState(ALERT);
            }

            switch (entity.getDetection()) {
                case NOTICED:
                case ALERT:
                    // Update target and thus path
                    target.setPosition(entity.target.getPosition());
                    Vector2 cur_pos = worldToGrid(entity.getEnemy().getPosition());
                    Vector2 target_pos = worldToGrid(target.getPosition());
                    Path path = entity.pathfinder.findPath(cur_pos, target_pos, entity.raycastCollisionDetector);
                    if (path != null) {
                        entity.followPathSB.setPath(path);
                    }
                    // Increase indicator
                    entity.getEnemy().setIndicatorAmount(
                            MathUtils.clamp(entity.getEnemy().getIndicatorAmount() + Gdx.graphics.getDeltaTime() / 2, 0, 1)
                    );
                    break;
                case NONE:
                    // Decrease indicator
                    entity.getEnemy().setIndicatorAmount(
                            MathUtils.clamp(entity.getEnemy().getIndicatorAmount() - Gdx.graphics.getDeltaTime() / 4, 0, 1)
                    );
                    // If the enemy has arrived to target and there is no detection, go back to looking around
                    float dist = entity.getEnemy().getPosition().dst(target.getPosition());
                    if (dist <= 0.5) entity.getStateMachine().changeState(LOOK_AROUND);
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

    /** Enemy knows where player is (!) and is actively moving towards player */
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
            entity.time = 0;

            entity.getEnemy().setLinearVelocity(Vector2.Zero);

            entity.getEnemy().setSteeringBehavior(null);
            entity.getEnemy().setDetection(Enemy.Detection.NONE);
        }

        @Override
        public void update(EnemyController entity) {
            switch (entity.getDetection()) {
                case NOTICED:
                case ALERT:
                    entity.getStateMachine().changeState(NOTICED);
                    break;
            }

            float lookTime = 1.5f;
            float restTime = 0.9f;

            // Look, rest, look, rest, then back to patrol
            if (entity.time < lookTime) {
                entity.getEnemy().setAngularVelocity(1);
            } else if (entity.time >= lookTime + restTime && entity.time < 3*lookTime + restTime) {
                entity.getEnemy().setAngularVelocity(-1);
            } else if ((entity.time >= lookTime && entity.time < lookTime + restTime) || (entity.time >= 3 * lookTime + restTime && entity.time < 3 * lookTime + 2 * restTime)) {
                entity.getEnemy().setAngularVelocity(0);
            } else {
                entity.getStateMachine().changeState(PATROL);
            }

            entity.time += Gdx.graphics.getDeltaTime();
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

    protected void setTarget(Vector2 pos) {
    }

    ;
}
