package infinityx.lunarhaze.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.utils.Path;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import infinityx.lunarhaze.models.entity.Werewolf;

public class WeightedFollowPath extends FollowPath{
    public float distToTarget;

    public Location<Vector2> target;

    public WeightedFollowPath(Steerable owner, Path path, float pathOffset, float predictionTime, Location<Vector2> target, float distToTarget) {
        super(owner, path);
        this.distToTarget = distToTarget;
        this.target = target;
    }

    @Override
    protected SteeringAcceleration calculateRealSteering (SteeringAcceleration steering) {
        SteeringAcceleration s = super.calculateRealSteering(steering);
        if (target.getPosition().sub((Vector2) owner.getPosition()).len() <= distToTarget){
            s.setZero();
        }

        return s;
    }
}
