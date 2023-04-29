package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import infinityx.lunarhaze.models.GameObject;
import infinityx.util.Direction;

/**
 * States for the player's state machine
 */
public enum PlayerState implements State<PlayerController> {

    IDLE() {
        @Override
        public void enter(PlayerController entity) {
            entity.player.setStealth(entity.STILL_STEALTH);
            setTexture(entity, "idle");
        }

        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (entity.isCollectingMoonlight()) {
                entity.getStateMachine().changeState(PlayerState.COLLECT);
            } else if (entity.player.isRunning()) {
                entity.getStateMachine().changeState(PlayerState.RUN);
            } else if (!entity.player.getLinearVelocity().isZero()) {
                entity.getStateMachine().changeState(PlayerState.WALK);
            }
        }
    },

    WALK() {
        /**
         * Cache of player direction. Required so the filmstrip is only updated when direction changes.
         * I'm doing this instead of making separate WALK-L, WALK-R, WALK-F, WALK-B states
         */
        Direction direction;

        @Override
        public void enter(PlayerController entity) {
            direction = entity.player.direction;
            entity.player.setStealth(entity.WALK_STEALTH);
            setTexture(entity, "walk");
            entity.player.texUpdate = 0.1f;
        }

        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (entity.player.getLinearVelocity().isZero()) {
                entity.getStateMachine().changeState(PlayerState.IDLE);
            } else if (entity.player.isRunning()) {
                entity.getStateMachine().changeState(PlayerState.RUN);
            }

            // Animations
            if (entity.player.direction == direction) return;
            setTexture(entity, "walk");
            direction = entity.player.direction;
        }
    },

    RUN() {
        /** Cache of player direction. Required so the filmstrip is only updated when direction changes. */
        Direction direction;

        @Override
        public void enter(PlayerController entity) {
            direction = entity.player.direction;
            entity.player.setStealth(entity.RUN_STEALTH);
            setTexture(entity, "walk");
            // texture update should be proportional to speed
            entity.player.texUpdate = 0.1f * entity.player.walkSpeed / entity.player.runSpeed;

        }

        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (entity.player.getLinearVelocity().isZero()) {
                entity.getStateMachine().changeState(PlayerState.IDLE);
            } else if (!InputController.getInstance().didRun()) {
                entity.getStateMachine().changeState(PlayerState.WALK);
            }

            // Animations
            if (entity.player.direction == direction) return;
            setTexture(entity, "walk");
            direction = entity.player.direction;
        }
    },

    ATTACK() {
        @Override
        public void enter(PlayerController entity) {
            entity.getAttackSound().play();
            setTexture(entity, "attack");
            entity.player.texUpdate = 0.06f;
        }

        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (!entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.IDLE);
            }
        }
    },

    /**
     * The player is currently collecting moonlight
     */
    COLLECT() {
        @Override
        public void enter(PlayerController entity) {
            entity.player.setStealth(entity.MOON_STEALTH);
        }

        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (!entity.isCollectingMoonlight()) {
                entity.getStateMachine().changeState(PlayerState.IDLE);
            }
        }

        @Override
        public void exit(PlayerController entity) {
//            entity.getCollectSound().play();
        }
    },

    /**
     * The player is currently dashing
     */
    DASH() {
        // TODO: Add state transitions to DASH from other states
        @Override
        public void enter(PlayerController entity) {
            // TODO: Update frame logic
        }

        @Override
        public void update(PlayerController entity) {
            // TODO: Add state transitions to other states
        }
    };


    @Override
    public void enter(PlayerController entity) {
    }

    @Override
    public void exit(PlayerController entity) {
    }

    @Override
    public boolean onMessage(PlayerController entity, Telegram telegram) {
        return false;
    }

    /**
     * Sets the filmstrip animation of the player. Assumes there exists filmstrips for each cardinal direction with suffixes "-b", "-f", "-l", "-r".
     *
     * @param entity holding player
     * @param name   Common prefix of filmstrip family. See {@link GameObject#setTexture(String)}.
     */
    protected void setTexture(PlayerController entity, String name) {
        switch (entity.player.direction) {
            case UP:
                entity.player.setTexture(name + "-b");
                entity.player.getAttackHitbox().setTexture("attack-b");
                break;
            case DOWN:
                entity.player.setTexture(name + "-f");
                entity.player.getAttackHitbox().setTexture("attack-f");
                break;
            case LEFT:
                entity.player.setTexture(name + "-l");
                entity.player.getAttackHitbox().setTexture("attack-l");
                break;
            case RIGHT:
                entity.player.setTexture(name + "-r");
                entity.player.getAttackHitbox().setTexture("attack-r");
                break;
        }
    }
}