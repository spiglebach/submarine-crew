package szm.orde4c.game.base;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class SceneSegment {
    private Actor actor;
    private Action action;

    public SceneSegment(Actor actor, Action action) {
        this.actor = actor;
        this.action = action;
    }

    public void start() {
        actor.clearActions();
        actor.addAction(action);
    }

    public boolean isFinished() {
        return actor.getActions().size == 0;
    }

    public void finish() {
        if (actor.hasActions()) {
            actor.getActions().first().act(100000);
        }
        actor.clearActions();
    }
}
