package infinityx.lunarhaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;

public enum PlayerState implements State<PlayerController> {
    WALK() {
        @Override
        public void enter(PlayerController entity) {
            System.out.println("Player switched to walk state");
        }
        @Override
        public void update(PlayerController entity) {
            entity.resolvePlayer(Gdx.graphics.getDeltaTime());
            entity.resolveStealthBar();
            entity.resolveMoonlight(Gdx.graphics.getDeltaTime(), entity.getLightingController());

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
            System.out.println("Player switched to run state");
        }
        @Override
        public void update(PlayerController entity) {
            InputController input = entity.getInputController();
            entity.resolvePlayer(Gdx.graphics.getDeltaTime());
            entity.resolveStealthBar();
            entity.resolveMoonlight(Gdx.graphics.getDeltaTime(), entity.getLightingController());

            // Handle state transitions
            if (entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.ATTACK);
            } else if (entity.isCollectingMoonlight()) {
                entity.getStateMachine().changeState(PlayerState.COLLECT);
            } else if (!entity.getInputController().didRun()) {
                entity.getStateMachine().changeState(PlayerState.WALK);
            }
        }
    },

    ATTACK() {
        @Override
        public void enter(PlayerController entity) {
            System.out.println("Player switched to attack state");
        }
        @Override
        public void update(PlayerController entity) {
            InputController input = entity.getInputController();
            entity.getAttackHandler().update(Gdx.graphics.getDeltaTime(), input, entity.getPhase());
            entity.getAttackSound().play();

            // Handle state transitions
            if(!entity.isAttacking()) {
                entity.getStateMachine().changeState(PlayerState.WALK);
            }
        }
    },

    COLLECT() {
        @Override
        public void enter(PlayerController entity) {
            System.out.println("Player switched to collect state");
        }
        @Override
        public void update(PlayerController entity) {
            entity.resolveMoonlight(Gdx.graphics.getDeltaTime(), entity.getLightingController());

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
    public void exit(PlayerController entity) {
    }

    @Override
    public boolean onMessage(PlayerController entity, Telegram telegram) {
        return false;
    }
}