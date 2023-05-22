package infinityx.lunarhaze.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
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

            if (entity.getSetting().isSoundEnabled()) {
                entity.getAttackedSound().play(entity.getSetting().getSoundVolume());
            }
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

        /** Cache of previous frame, used to match step with animation */
        int frame;

        @Override
        public void enter(PlayerController entity) {
            direction = entity.player.direction;
            setTexture(entity, "walk");
            entity.player.setTargetStealth(entity.player.getTargetStealth() + PlayerController.WALK_STEALTH);
            frame = -1;
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
            if (entity.player.direction != direction) {
                setTexture(entity, "walk");
                direction = entity.player.direction;
            }

            // Walk sound
            if (!entity.getSetting().isSoundEnabled() || frame == entity.player.animation.getCurFrame()) return;
            frame = entity.player.animation.getCurFrame();
            // match sound with frames
            if (entity.player.isWerewolf()) {
                if (frame == 2 || frame == 5) {
                    entity.walk_sound.play();
                }
            } else {
                if (direction == Direction.DOWN) {
                    // special case
                    if (frame == 0 || frame == 4) {
                        entity.walk_sound.play();
                    }
                } else {
                    if (frame == 0 || frame == 2) {
                        entity.walk_sound.play();
                    }
                }
            }
        }

        @Override
        public void exit(PlayerController entity) {
            entity.player.setTargetStealth(entity.player.getTargetStealth() - PlayerController.WALK_STEALTH);
        }
    },

    ATTACK() {
        @Override
        public void enter(PlayerController entity) {
            if (entity.getSetting().isSoundEnabled()) {
                entity.getAttackSound().play(entity.getSetting().getSoundVolume());
            }
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
            if (entity.getSetting().isSoundEnabled()) {
                entity.getHeavyAttackSound().play(entity.getSetting().getSoundVolume());
            }
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
            entity.player.setLinearVelocity(Vector2.Zero);
        }

        @Override
        public void update(PlayerController entity) {
            entity.timeOnMoonlight += Gdx.graphics.getDeltaTime();

            Color tint = entity.player.getTint();
            tint.r = Interpolation.circleIn.apply(Color.WHITE.r, Color.GOLD.r, entity.getTimeOnMoonlightPercentage() / 1.1f);
            tint.g = Interpolation.circleIn.apply(Color.WHITE.g, Color.GOLD.g, entity.getTimeOnMoonlightPercentage() / 1.1f);
            tint.b = Interpolation.circleIn.apply(Color.WHITE.b, Color.GOLD.b, entity.getTimeOnMoonlightPercentage() / 1.1f);
            tint.a = Interpolation.circleIn.apply(Color.WHITE.a, Color.GOLD.a, entity.getTimeOnMoonlightPercentage() / 1.1f);

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
            entity.player.setTint(Color.WHITE);
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