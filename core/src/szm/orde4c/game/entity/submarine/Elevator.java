package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;

public class Elevator extends BaseActor {
    private String group;
    private String id;
    public Elevator(float x, float y, float width, float height, Stage s) {
        super(x, y, s);
        loadTexture("platform.png"); // TODO textúrát hozzáadni
        setColor(Color.YELLOW);
        setSize(width, height);
        setBoundaryRectangle();
    }
}
