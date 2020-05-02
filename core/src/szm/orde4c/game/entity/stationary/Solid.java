package szm.orde4c.game.entity.stationary;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;

public class Solid extends BaseActor {

    public Solid(float x, float y, float width, float height, float rotation, Stage s) {
        super(x, y, s);
        setSize(width, height);
        setOrigin(0, height);
        setRotation(rotation);
        setBoundaryRectangle();
    }

    public Solid(float x, float y, Polygon polygon, Stage s) {
        super(x, y, s);
        setBoundaryPolygon(polygon);
    }
}
