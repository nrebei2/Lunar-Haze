package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.steer.utils.Path;
import com.badlogic.gdx.ai.utils.ArithmeticUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.ai.TacticalManager;
import infinityx.lunarhaze.models.entity.Enemy;
import infinityx.util.Box2dLocation;

import java.util.ArrayList;

/**
 * States for each enemy's state machine
 */
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
     * Question mark above enemy, turns towards location
     */
    NOTICED() {
        /** For use in face steering */
        Box2dLocation target;

        // FIXME: this solution is so ass
        boolean start = true;

        @Override
        public void enter(EnemyController entity) {
            target = new Box2dLocation(entity.target);
            entity.getEnemy().setIndependentFacing(true);
            entity.getEnemy().setDetection(Enemy.Detection.NOTICED);
            entity.targetPos.set(target.getPosition());
            entity.faceSB.setTarget(target);
            entity.getEnemy().setSteeringBehavior(entity.faceSB);
        }

        @Override
        public void update(EnemyController entity) {
            if (start) {
                // Zero out velocity on start
                entity.getEnemy().setLinearVelocity(Vector2.Zero);
                entity.getEnemy().setAngularVelocity(0);
                start = true;
            }

            // Check if we faced target
            Vector2 toTarget = (target.getPosition()).cpy().sub(entity.getEnemy().getPosition());
            float orientation = entity.getEnemy().vectorToAngle(toTarget);
            float rotation = ArithmeticUtils.wrapAngleAroundZero(orientation - entity.getEnemy().getOrientation());
            float rotationSize = rotation < 0f ? -rotation : rotation;

            if (rotationSize <= entity.faceSB.getAlignTolerance()) {
                entity.getStateMachine().changeState(INDICATOR);
            }
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);

        }
    },

    /**
     * The enemy is going towards last known player position and indicator bar is (maybe) increasing
     */
    INDICATOR() {
        @Override
        public void enter(EnemyController entity) {
            entity.getEnemy().setDetection(Enemy.Detection.INDICATOR);
            entity.getEnemy().setIndicatorAmount(0);

            // TODO
            entity.target.setStealth(entity.target.getStealth() + 1f);

            entity.updatePath();
            entity.getEnemy().setSteeringBehavior(entity.followPathSB);
        }

        @Override
        public void update(EnemyController entity) {

            // If the indicator is full, the enemy is alerted
            if (entity.getEnemy().getIndicatorAmount() == 1) {
                MessageManager.getInstance().dispatchMessage(TacticalManager.FOUND, entity);
                entity.getStateMachine().changeState(ALERT);
            }


            switch (entity.getDetection()) {
                case NOTICED:
                case ALERT:
                    // Critical!! Set so that targetPos does not reference
                    entity.targetPos.set(entity.target.getPosition());
                    entity.updatePath();

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
                    float dist = entity.getEnemy().getPosition().dst(entity.targetPos);
                    if (dist <= 0.5) {
                        entity.getStateMachine().changeState(LOOK_AROUND);
                    }
            }
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);
            entity.target.setStealth(entity.target.getStealth() - 1f);
        }
    },

    /**
     * Enemy knows where player is (!) and is actively moving towards player
     */
    ALERT() {
        /** Tick count */
        int tick;

        @Override
        public void enter(EnemyController entity) {
            entity.getEnemy().setDetection(Enemy.Detection.ALERT);

            // TODO
            entity.target.setStealth(entity.target.getStealth() + 2);

            entity.targetPos.set(entity.getTarget().getPosition());
            entity.updatePath();
            entity.getEnemy().setSteeringBehavior(entity.followPathSB);

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
                    entity.targetPos.set(entity.target.getPosition());
                    entity.getStateMachine().changeState(INDICATOR);
                    break;
            }

            tick++;
            // Update path every 10 frames
            if (tick % 10 == 0) {
                entity.targetPos.set(entity.getTarget().getPosition());
                entity.updatePath();
            }
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);
            MessageManager.getInstance().dispatchMessage(TacticalManager.REMOVE, entity);
            entity.target.setStealth(entity.target.getStealth() - 2);
        }

        @Override
        public boolean onMessage(EnemyController control, Telegram telegram) {
            if (telegram.message == TacticalManager.FLANK) {
                Vector2 flank_pos = (Vector2) telegram.extraInfo;
                Vector2 cur_pos = control.getEnemy().getPosition();
                Path path = control.pathfinder.findPath(cur_pos, flank_pos);
                control.followPathSB.setPath(path);
                control.getEnemy().setSteeringBehavior(control.followPathSB);
            }
            return true;
        }
    },

    PATROL() {
        @Override
        public void enter(EnemyController entity) {
            Vector2 patrol = entity.getPatrolTarget();
            ArrayList<Vector2> pat = entity.getEnemy().getPatrolPath();
            System.out.println(pat.get(0).x + " " + pat.get(0).y);
            System.out.println(pat.get(1).x + " " + pat.get(1).y);
            while (entity.pathfinder.map.getNodeAtWorld(patrol.x, patrol.y).isObstacle) {
                patrol = entity.getPatrolTarget();
            }
            entity.targetPos.set(patrol);
            entity.updatePath();
            entity.getEnemy().setSteeringBehavior(entity.followPathSB);
            entity.getEnemy().setDetection(Enemy.Detection.NONE);
        }

        @Override
        public void update(EnemyController entity) {

            // Check if have arrived to patrol position
            float dist = entity.getEnemy().getPosition().dst(entity.targetPos);
            if (dist <= 0.1f) entity.getStateMachine().changeState(LOOK_AROUND);

            switch (entity.getDetection()) {
                case NOTICED:
                case ALERT:
                    entity.getStateMachine().changeState(NOTICED);
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
            // not using one since forces are a pain in the ass to deal with for something as simple as this
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
            } else if (entity.time >= lookTime + restTime && entity.time < 3 * lookTime + restTime) {
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
}