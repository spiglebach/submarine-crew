package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;

public class Ladder extends BaseActor {

    public Ladder(float x, float y, float width, float height, Stage s) {
        super(x, y, s);
        setSize(width, height);
        setBoundaryRectangle();
    }
}
