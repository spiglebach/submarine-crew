package szm.orde4c.game.entity.stationary;

import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.util.AreaObjectType;

public class Area extends BaseActor {
    public Area(AreaObjectType areaObjectType, float x, float y, float width, float height, float rotation, int objectCount, Stage s) {
        super(x, y, s);
        loadTexture("platform.png");    //TODO remove
        setOpacity(0.5f);    //TODO remove
        setSize(width, height);
        setOrigin(0, height);
        setRotation(rotation);
        setBoundaryRectangle();

        if (areaObjectType.equals(AreaObjectType.ROCK)) {
            for (int i = 0; i < objectCount; i++) {
                new Rock(this, s);
            }
        } else if (areaObjectType.equals(AreaObjectType.VEGETATION)) {
            for (int i = 0; i < objectCount; i++) {
                new Vegetation(this, s);
            }
        }
    }


}
