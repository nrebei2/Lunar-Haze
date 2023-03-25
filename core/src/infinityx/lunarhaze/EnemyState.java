package infinityx.lunarhaze;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import infinityx.lunarhaze.entity.Enemy;
import infinityx.lunarhaze.entity.Werewolf;
import infinityx.lunarhaze.physics.ConeSource;
import infinityx.lunarhaze.physics.RaycastInfo;

public enum EnemyState implements State<EnemyController> {

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

    PATROL() {
        @Override
        public void enter(EnemyController entity) {

        }

        @Override
        public void update(EnemyController control) {


        }

        @Override
        public void exit(EnemyController entity) {

        }
    };
    @Override
    public boolean onMessage(EnemyController control, Telegram telegram) {
        return false;
    }

}
