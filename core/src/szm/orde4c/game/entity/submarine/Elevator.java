package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.util.Assets;

public class Elevator extends BaseActor {
    public Elevator(float x, float y, float width, float height, Stage s) {
        super(x, y, s);
        loadTexture(Assets.instance.getTexture(Assets.SUBMARINE_ELEVATOR));
        setSize(width, height);
        setBoundaryRectangle();
    }
}
