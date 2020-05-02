package szm.orde4c.game.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;

public class LevelStamp extends BaseActor {
    private Animation checkmarkStamp;
    private Animation questionmarkStamp;
    private Animation xStamp;

    public LevelStamp(float x, float y, Stage s) {
        super(x, y, s);
        checkmarkStamp = loadTexture("mapsign-checkmark.png");
        xStamp = loadTexture("mapsign-xmark.png");
        questionmarkStamp = loadTexture("mapsign-questionmark.png");
        setSize(400, 400);
    }

    public void completed() {
        setAnimation(checkmarkStamp);
    }

    public void next() {
        setAnimation(xStamp);
    }

    public void locked() {
        setAnimation(questionmarkStamp);
    }
}
