package szm.orde4c.game.entity.stationary;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;

public class Environment extends BaseActor {

    public Environment(float x, float y, Polygon poly, Stage s) {
        super(x, y, s);
        setBoundaryPolygon(poly);
    }
}
