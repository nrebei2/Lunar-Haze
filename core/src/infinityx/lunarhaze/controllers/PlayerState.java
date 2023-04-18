package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import infinityx.util.Direction;

public enum PlayerState implements State<PlayerController> {

    ANY_STATE() {
        @Override
        public void update(PlayerController entity) {
            InputController input = entity.getInputController();
            entity.getAttackHandler().update(Gdx.graphics.getDeltaTime(), input, entity.getPhase());
            entity.resolvePlayer(Gdx.graphics.getDeltaTime());
            if (entity.getPhase() == GameplayController.Phase.STEALTH) {
                entity.resolveMoonlight(Gdx.graphics.getDeltaTime(), entity.getLightingController());
            }
        }
    },

    IDLE() {
        @Override
        public void enter(PlayerController entity) {
            entity.player.setStealth(entity.STILL_STEALTH);
            setTexture(entity, "idle");
        }

        @Override
        public void update(PlayerController entity) {
            if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (entity.isCollectingMoonlight()) {
                entity.getStateMachine().changeState(PlayerState.COLLECT);
            } else if (entity.getInputController().didRun() &&
                    (entity.getInputController().getHorizontal() != 0 || entity.getInputController().getVertical() != 0)) {
                entity.getStateMachine().changeState(PlayerState.RUN);
            } else if (!entity.getInputController().didRun() &&
                    (entity.getInputController().getHorizontal() != 0 || entity.getInputController().getVertical() != 0)) {
                entity.getStateMachine().changeState(PlayerState.WALK);
            }
        }
    },

    WALK() {
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
            } else if (entity.getInputController().getHorizontal() == 0 && entity.getInputController().getVertical() == 0) {
                entity.getStateMachine().changeState(PlayerState.IDLE);
            } else if (entity.getInputController().didRun()) {
                entity.getStateMachine().changeState(PlayerState.RUN);
            }

            // Animations
            if (entity.player.direction == direction) return;
            setTexture(entity, "walk");
            direction = entity.player.direction;
        }
    },

    RUN() {
        Direction direction;

        @Override
        public void enter(PlayerController entity) {
            direction = entity.player.direction;
            entity.player.setStealth(entity.RUN_STEALTH);
            setTexture(entity, "walk");
            entity.player.texUpdate = 0.1f * entity.player.walkSpeed / entity.player.runSpeed;

        }

        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (entity.player.getLinearVelocity().isZero()) {
                entity.getStateMachine().changeState(PlayerState.IDLE);
            } else if (!entity.getInputController().didRun()) {
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
            entity.getCollectSound().play();
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

    protected void setTexture(PlayerController entity, String name) {
        switch (entity.player.direction) {
            case UP:
                entity.player.setTexture(entity.player.filmstrips.get(name + "-b"));
                break;
            case DOWN:
                entity.player.setTexture(entity.player.filmstrips.get(name + "-f"));
                break;
            case LEFT:
                entity.player.setTexture(entity.player.filmstrips.get(name + "-l"));
                break;
            case RIGHT:
                entity.player.setTexture(entity.player.filmstrips.get(name + "-r"));
                break;
        }
    }
}