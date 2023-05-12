package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.Gdx;
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
            setTexture(entity, "idle");
        }

        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (entity.isWindingUp()) {
                entity.getStateMachine().changeState(PlayerState.HEAVY_ATTACK_WINDUP);
            } else if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (entity.isAttacked()) {
                entity.getStateMachine().changeState(PlayerState.ATTACKED);
            } else if (!entity.player.getLinearVelocity().isZero()) {
                entity.getStateMachine().changeState(PlayerState.WALK);
            }
        }
    },

    ATTACKED() {
        @Override
        public void enter(PlayerController entity) {
            setTexture(entity, "attacked");
        }

        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (!entity.isAttacked()) {
                entity.getStateMachine().changeState(PlayerState.IDLE);
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
            setTexture(entity, "walk");
            entity.player.setTargetStealth(entity.player.getTargetStealth() + PlayerController.WALK_STEALTH);
        }

        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (entity.isWindingUp()) {
                entity.getStateMachine().changeState(PlayerState.HEAVY_ATTACK_WINDUP);
            } else if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (entity.isAttacked()) {
                entity.getStateMachine().changeState(PlayerState.ATTACKED);
            } else if (entity.player.getLinearVelocity().isZero()) {
                entity.getStateMachine().changeState(PlayerState.IDLE);
            }


            // Animations
            if (entity.player.direction == direction) return;
            setTexture(entity, "walk");
            direction = entity.player.direction;
        }

        @Override
        public void exit(PlayerController entity) {
            entity.player.setTargetStealth(entity.player.getTargetStealth() - PlayerController.WALK_STEALTH);
        }
    },

    ATTACK() {
        @Override
        public void enter(PlayerController entity) {
            entity.getAttackSound().play(0.8f);
            setTexture(entity, entity.getAttackHandler().useRightHand() ? "attack1" : "attack2");
        }

        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (!entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.IDLE);
            }
        }
    },

    HEAVY_ATTACK() {
        @Override
        public void enter(PlayerController entity) {
            entity.getAttackSound().play(0.8f);
            setTexture(entity, "heavyattack");
        }

        @Override
        public void update(PlayerController entity) {
            if (!entity.isHeavyAttacking()) {
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
            entity.player.setTexture("collect");
            entity.timeOnMoonlight = 0;
            entity.player.isCollecting = true;
        }

        @Override
        public void update(PlayerController entity) {
            entity.timeOnMoonlight += Gdx.graphics.getDeltaTime();

            if (entity.timeOnMoonlight > PlayerController.MOONLIGHT_COLLECT_TIME) {
                entity.collectMoonlight();
                entity.getStateMachine().changeState(IDLE);
            }

            // Handle state transitions
            if (entity.isWindingUp()) {
                entity.getStateMachine().changeState(PlayerState.HEAVY_ATTACK_WINDUP);
            } else if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (entity.isAttacked()) {
                entity.getStateMachine().changeState(PlayerState.ATTACKED);
            } else if (!InputController.getInstance().didCollect()) {
                entity.getStateMachine().changeState(PlayerState.IDLE);
            }
        }

        @Override
        public void exit(PlayerController entity) {
            entity.player.isCollecting = false;
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
    },

    /**
     * The player is currently winding up for heavy attack
     */
    HEAVY_ATTACK_WINDUP() {
        @Override
        public void enter(PlayerController entity) {
            setTexture(entity, "windup");
            entity.player.setWindingUp(true);
        }

        @Override
        public void update(PlayerController entity) {
            if (!entity.isWindingUp()) {
                entity.getStateMachine().changeState(PlayerState.HEAVY_ATTACK);
            }
        }

        @Override
        public void exit(PlayerController entity) {
            entity.player.setWindingUp(false);
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
                break;
            case DOWN:
                entity.player.setTexture(name + "-f");
                break;
            case LEFT:
                entity.player.setTexture(name + "-l");
                break;
            case RIGHT:
                entity.player.setTexture(name + "-r");
                break;
        }
    }
}