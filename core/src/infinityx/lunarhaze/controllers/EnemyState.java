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
import infinityx.util.AngleUtils;
import infinityx.util.Box2dLocation;

/**
 * States for each enemy's state machine
 */
public enum EnemyState implements State<EnemyController> {

    ANY_STATE() {
        @Override
        public void update(EnemyController entity) {
            // FIXME: dont call setFilmstripPrefix every frame
            // All states other than attack use walk/idle animations
            if (!entity.getStateMachine().isInState(ATTACK)) {
                if (entity.getEnemy().getLinearVelocity().isZero(0.01f)) {
                    entity.getEnemy().setFilmstripPrefix("idle");
                } else {
                    entity.getEnemy().setFilmstripPrefix("walk");
                    // Texture update is proportional to velocity
//                    entity.getEnemy().texUpdate = 1 / (entity.getEnemy().getLinearVelocity().len() * 5);
                }
            }
        }
    },

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

        @Override
        public void enter(EnemyController entity) {
            target = new Box2dLocation(entity.target);
            entity.getEnemy().setDetection(Enemy.Detection.NOTICED);
            entity.targetPos.set(target.getPosition());

            entity.faceSB.setTarget(target);

            // Again steering behaviors are ass
            entity.getEnemy().setSteeringBehavior(null);
        }

        @Override
        public void update(EnemyController entity) {
            // Check if we faced target
            Vector2 toTarget = (target.getPosition()).cpy().sub(entity.getEnemy().getPosition());
            float orientation = entity.getEnemy().vectorToAngle(toTarget);
            float rotation = ArithmeticUtils.wrapAngleAroundZero(orientation - entity.getEnemy().getOrientation());

            // Mimic face sb
            entity.getEnemy().setAngularVelocity(rotation < 0 ? -1.2f : 1.2f);

            float rotationSize = rotation < 0f ? -rotation : rotation;
            if (rotationSize <= entity.faceSB.getAlignTolerance()) {
                entity.getStateMachine().changeState(INDICATOR);
            }
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

            entity.updatePath();
            entity.getEnemy().setSteeringBehavior(entity.followPathSB);
        }

        @Override
        public void update(EnemyController entity) {

            // If the indicator is full, the enemy is alerted
            if (entity.getEnemy().getIndicatorAmount() == 1) {
                // only call for help if first to notice target
                if (entity.getStateMachine().getPreviousState() == NOTICED) {
                    MessageManager.getInstance().dispatchMessage(TacticalManager.FOUND, entity);
                }
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
        }
    },

    ATTACK() {
        @Override
        public void enter(EnemyController entity) {
            //entity.getAttackSound().play();
            entity.getEnemy().setFilmstripPrefix("attack");
            entity.getEnemy().setIndependentFacing(true);
            entity.initiateAttack();
        }

        @Override
        public void update(EnemyController entity) {
            Vector2 enemyToTarget = entity.target.getPosition().sub(entity.getEnemy().getPosition());
            entity.getEnemy().setOrientation(AngleUtils.vectorToAngle(enemyToTarget));

            // Handle state transitions
            if (!entity.getEnemy().isAttacking()) {
                // Go back to whatever it was doing before. It may always be ALERT.
                entity.getStateMachine().changeState(ALERT);
            }
        }

        @Override
        public void exit(EnemyController entity) {
            entity.getEnemy().setIndependentFacing(false);
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

            entity.targetPos.set(entity.getTarget().getPosition());
            entity.updatePath();
            entity.getEnemy().setSteeringBehavior(entity.followPathSB);
            if (!(entity.getStateMachine().getPreviousState() == ATTACK || entity.getStateMachine().getCurrentState() == FLANK)
                    && entity.isInBattle()) {
                MessageManager.getInstance().dispatchMessage(TacticalManager.ADD, entity);
            }
            entity.time = 0;
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
            entity.time += Gdx.graphics.getDeltaTime();

            Vector2 enemyToTarget = entity.target.getPosition().sub(entity.getEnemy().getPosition());

            //if in stealth just walk towards target and attack if close enough
            if (!entity.isInBattle()) {
                if (enemyToTarget.len() <= entity.getEnemy().getAttackRange() && entity.canStartNewAttack()) {
                    entity.getStateMachine().changeState(ATTACK);
                }
                entity.getEnemy().setIndependentFacing(false);
                entity.targetPos.set(entity.getTarget().getPosition());
                entity.getEnemy().setSteeringBehavior(entity.followPathSB);
                // Update path every 0.1 seconds
                if (entity.time >= 0.1) {
                    entity.updatePath();
                    entity.time = 0;
                }
            } else {
                if (enemyToTarget.len() <= entity.getEnemy().getAttackRange() && entity.canStartNewAttack()) {
                    entity.getStateMachine().changeState(ATTACK);
                }
                // Switch to battle behavior when close enough
                else if (enemyToTarget.len() <= entity.getEnemy().getStrafeDistance()) {
                    // Always face towards target
//                    entity.getEnemy().setIndependentFacing(true);
//                    entity.getEnemy().setOrientation(AngleUtils.vectorToAngle(enemyToTarget));
                    entity.getEnemy().setSteeringBehavior(entity.battleSB);
                } else {
                    // go back to chase (follow path)

                    entity.getEnemy().setIndependentFacing(false);
                    entity.targetPos.set(entity.getTarget().getPosition());
                    entity.getEnemy().setSteeringBehavior(entity.followPathSB);
                    // Update path every 0.1 seconds
                    if (entity.time >= 0.1) {
                        entity.updatePath();
                        entity.time = 0;
                    }
                }
            }

        }

        @Override
        public void exit(EnemyController entity) {
            if (!entity.isInBattle()) {
                MessageManager.getInstance().dispatchMessage(TacticalManager.REMOVE, entity);
            }
        }

        @Override
        public boolean onMessage(EnemyController control, Telegram telegram) {
            if (telegram.message == TacticalManager.FLANK) {

                Vector2 flank_pos = (Vector2) telegram.extraInfo;
                control.flank_pos = flank_pos;
                Vector2 cur_pos = control.getEnemy().getPosition();
                Path path = control.pathfinder.findPath(cur_pos, flank_pos);
                control.followPathSB.setPath(path);
                control.getEnemy().setSteeringBehavior(control.followPathSB);
                control.getStateMachine().changeState(FLANK);
            }
            return true;
        }
    },

    FLANK() {
        @Override
        public void update(EnemyController entity) {
            Vector2 enemyToTarget = entity.target.getPosition().sub(entity.getEnemy().getPosition());
            // Switch to battle behavior when close enough
            //check if got to flank position
            Vector2 distToFlank = entity.getEnemy().getPosition().sub(entity.flank_pos);
            if (distToFlank.len() <= 0.2f) {
                entity.getStateMachine().changeState(ALERT);
            }
            if (enemyToTarget.len() <= entity.getEnemy().getAttackRange() && entity.canStartNewAttack()) {
                entity.getStateMachine().changeState(ATTACK);
            }
        }
    },

    PATROL() {
        @Override
        public void enter(EnemyController entity) {
            Vector2 patrol = entity.getPatrolTarget();
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
            if (dist <= 0.2f) entity.getStateMachine().changeState(LOOK_AROUND);

            switch (entity.getDetection()) {
                case NOTICED:
                case ALERT:
                    entity.getStateMachine().changeState(NOTICED);
                    break;
            }
        }
    },

    LOOK_AROUND() {
        @Override
        public void enter(EnemyController entity) {
            entity.time = 0;

            // not using one since forces are a pain in the ass to deal with for something as simple as this
            entity.getEnemy().setSteeringBehavior(null);
            entity.getEnemy().setDetection(Enemy.Detection.NONE);
        }

        @Override
        public void update(EnemyController entity) {
            entity.getEnemy().setLinearVelocity(Vector2.Zero);
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
