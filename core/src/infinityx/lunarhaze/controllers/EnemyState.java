package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.utils.ArithmeticUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.ai.TacticalManager;
import infinityx.lunarhaze.models.entity.Enemy;
import infinityx.util.AngleUtils;

/**
 * States for each enemy's state machine
 */
public enum EnemyState implements State<EnemyController> {

    ANY_STATE() {
        @Override
        public void update(EnemyController entity) {
            // All states other than attack use walk/idle animations
            if (!entity.getStateMachine().isInState(ATTACK)) {
                if (entity.getEnemy().getLinearVelocity().isZero(0.01f)) {
                    entity.getEnemy().setFilmstripPrefix("idle");
                } else {
                    entity.getEnemy().setFilmstripPrefix("walk");
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
        @Override
        public void enter(EnemyController entity) {
            entity.getEnemy().setDetection(Enemy.Detection.NOTICED);

            // Again steering behaviors are ass
            entity.getEnemy().setSteeringBehavior(null);
        }

        @Override
        public void update(EnemyController entity) {
            entity.getEnemy().setLinearVelocity(Vector2.Zero);
            entity.targetPos.set(entity.target.getPosition());

            // Check if we faced target
            Vector2 toTarget = entity.getEnemy().getPosition().sub(entity.targetPos).scl(-1, -1);
            float orientation = entity.getEnemy().vectorToAngle(toTarget);
            float rotation = ArithmeticUtils.wrapAngleAroundZero(orientation - entity.getEnemy().getOrientation());

            // Mimic face steering behavior
            entity.getEnemy().setAngularVelocity(rotation < 0 ? -1.6f : 1.6f);

            float rotationSize = rotation < 0f ? -rotation : rotation;
            if (rotationSize <= MathUtils.degreesToRadians * 10) {
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
            entity.getEnemy().setMaxLinearSpeed(1f);
            entity.getEnemy().setDetection(Enemy.Detection.INDICATOR);
            entity.getEnemy().setIndicatorAmount(0);

            entity.updatePath();
            entity.getEnemy().setSteeringBehavior(entity.followPathAvoid);
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
            entity.getAttackHandler().initiateAttack();
            entity.getEnemy().setSteeringBehavior(null);

            // Face towards target and don't move
            entity.getEnemy().setOrientation(AngleUtils.vectorToAngle(entity.target.getPosition().sub(entity.getEnemy().getPosition())));
            entity.getEnemy().setLinearVelocity(Vector2.Zero);
            entity.getEnemy().setAngularVelocity(0);
        }

        @Override
        public void update(EnemyController entity) {
            // Handle state transitions
            if (!entity.getEnemy().isAttacking()) {
                entity.getStateMachine().changeState(ALERT);
            }
        }

        @Override
        public void exit(EnemyController entity) {}
    },

    /**
     * Enemy knows where player is (!) and is actively moving towards player
     */
    ALERT() {

        @Override
        public void enter(EnemyController entity) {
            entity.getEnemy().setMaxLinearSpeed(1.63f);
            entity.getEnemy().setDetection(Enemy.Detection.ALERT);

            entity.targetPos.set(entity.getTarget().getPosition());
            entity.updatePath();
            entity.getEnemy().setSteeringBehavior(entity.followPathAvoid);

            //TODO fix the first part of the and statement, this breaks if enemy attack aand switches to battle. Make
            //each enemy is added in only once
            if (entity.getEnemy().isInBattle()) {
                MessageManager.getInstance().dispatchMessage(TacticalManager.ADD, entity);
            }
        }

        @Override
        public void update(EnemyController entity) {
            if (!entity.getEnemy().isActive()){
                MessageManager.getInstance().dispatchMessage(TacticalManager.REMOVE, entity);
            }

            switch (entity.getDetection()) {
                case NONE:
                    entity.targetPos.set(entity.target.getPosition());
                    entity.getStateMachine().changeState(INDICATOR);
                    break;
            }


            Vector2 enemyToTarget = entity.target.getPosition().sub(entity.getEnemy().getPosition());
            float targetDst = enemyToTarget.len();
            entity.getEnemy().getFlashlight().setDirection(enemyToTarget.angleDeg());

            //if in stealth just walk towards target and attack if close enough
            if (!entity.getEnemy().isInBattle()) {
                if (targetDst <= entity.getEnemy().getAttackRange() && entity.getAttackHandler().canStartNewAttack()) {
                    entity.getStateMachine().changeState(ATTACK);
                    return;
                }
                entity.getEnemy().setIndependentFacing(false);
                entity.targetPos.set(entity.getTarget().getPosition());
                entity.getEnemy().setSteeringBehavior(entity.followPathAvoid);
                // Update path every 0.1 seconds
                if (entity.time >= 0.1) {
                    entity.updatePath();
                    entity.time = 0;
                }
            } else {
                if (targetDst <= entity.getEnemy().getAttackRange() && entity.getAttackHandler().canStartNewAttack()) {
                    entity.getStateMachine().changeState(ATTACK);
                    return;
                }
                entity.rayCache.set(entity.getEnemy().getPosition(), entity.getTarget().getPosition());

                entity.pathCollision.findCollision(entity.collCache, entity.rayCache);
                // use Astar to target if there is obstacle in the way or farther than strafe distance from target
                if (entity.raycast.hit || targetDst > entity.getEnemy().getStrafeDistance()) {
                    entity.getEnemy().setMaxLinearSpeed(1.11f);
                    entity.targetPos.set(entity.getTarget().getPosition());
                    entity.getEnemy().setSteeringBehavior(entity.followPathAvoid);
                    // Update path every 0.1 seconds
                    if (entity.time >= 0.1) {
                        entity.updatePath();
                        entity.time = 0;
                    }
                } else {
                    //go to battle mode
                    // Always face towards target
                    entity.getEnemy().setIndependentFacing(true);
                    Vector2 dir = entity.target.getPosition().sub(entity.getEnemy().getPosition());
                    entity.getEnemy().setOrientation(AngleUtils.vectorToAngle(dir));
                    entity.getEnemy().setSteeringBehavior(entity.battleSB);
                    entity.getEnemy().setMaxLinearSpeed(0.8f);
                }
            }

        }

        @Override
        public void exit(EnemyController entity) {
            if (!entity.getEnemy().isInBattle()) {
                MessageManager.getInstance().dispatchMessage(TacticalManager.REMOVE, entity);
            }
        }

        @Override
        public boolean onMessage(EnemyController control, Telegram telegram) {
            if (telegram.message == TacticalManager.ATTACK) {
                control.attack.setEnabled(true);
                control.strafe.setEnabled(false);
                control.evade.setEnabled(false);
            }
            if (telegram.message == TacticalManager.STRAFE) {
                control.strafe.setEnabled(true);
                control.evade.setEnabled(true);
                control.attack.setEnabled(false);
            }

            return true;
        }
    },

    PATROL() {
        @Override
        public void enter(EnemyController entity) {
            entity.getEnemy().setMaxLinearSpeed(0.8f);
            Vector2 patrol = entity.getPatrolTarget();
            entity.targetPos.set(patrol);
            entity.updatePath();
            entity.getEnemy().setSteeringBehavior(entity.followPathSB);
            entity.getEnemy().setDetection(Enemy.Detection.NONE);
        }

        @Override
        public void update(EnemyController entity) {

            // Check if have arrived to patrol position
            float dist = entity.getEnemy().getPosition().dst(entity.targetPos);
            if (dist <= 0.3f) {
                if (entity.getEnemy().rand.nextFloat() <= 0.3f) {
                    entity.getStateMachine().changeState(LOOK_AROUND);
                } else {
                    Vector2 patrol = entity.getPatrolTarget();
                    entity.targetPos.set(patrol);
                    entity.updatePath();
                }
            }

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
            if (entity.getStateMachine().getPreviousState() == INDICATOR)
                entity.getEnemy().setDetection(Enemy.Detection.NOTICED);
            else
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
