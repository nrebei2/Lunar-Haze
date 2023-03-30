package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;

public enum PlayerState implements State<PlayerController> {

    ANY_STATE() {
        @Override
        public void update(PlayerController entity) {
            InputController input = entity.getInputController();
            entity.getAttackHandler().update(Gdx.graphics.getDeltaTime(), input, entity.getPhase());
            entity.resolvePlayer(Gdx.graphics.getDeltaTime());
            entity.resolveMoonlight(Gdx.graphics.getDeltaTime(), entity.getLightingController());
        }
    },

    IDLE() {
        @Override
        public void enter(PlayerController entity) {
           entity.player.setStealth(entity.STILL_STEALTH);
        }

        @Override
        public void update(PlayerController entity) {

        }
    },

    WALK() {
        @Override
        public void enter(PlayerController entity) {
            //System.out.println("Player switched to walk state");
            entity.player.setStealth(entity.WALK_STEALTH);
        }
        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (entity.isCollectingMoonlight()) {
                entity.getStateMachine().changeState(PlayerState.COLLECT);
            } else if (entity.getInputController().didRun()) {
                entity.getStateMachine().changeState(PlayerState.RUN);
            }
        }
    },

    RUN() {
        @Override
        public void enter(PlayerController entity) {
            //System.out.println("Player switched to run state");
            entity.player.setStealth(entity.RUN_STEALTH);

        }
        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (entity.isCollectingMoonlight()) {
                entity.getStateMachine().changeState(PlayerState.COLLECT);
            } else if (!entity.getInputController().didRun()) {
                if (entity.player.getLinearVelocity().isZero()) {
                    entity.getStateMachine().changeState(PlayerState.IDLE);
                } else {
                    entity.getStateMachine().changeState(PlayerState.WALK);
                }
            }
        }
    },

    ATTACK() {
        @Override
        public void enter(PlayerController entity) {
            //System.out.println("Player switched to attack state");
            entity.getAttackSound().play();
        }
        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if(!entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.WALK);
            }
        }
    },

    COLLECT() {
        @Override
        public void enter(PlayerController entity) {
            //System.out.println("Player switched to collect state");
            entity.player.setStealth(entity.MOON_STEALTH);
        }
        @Override
        public void update(PlayerController entity) {
            // Handle state transitions
            if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (!entity.isCollectingMoonlight()){
                entity.getStateMachine().changeState(PlayerState.WALK);
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
}