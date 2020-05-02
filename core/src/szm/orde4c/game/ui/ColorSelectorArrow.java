package szm.orde4c.game.ui;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import szm.orde4c.game.base.BaseActor;

public class ColorSelectorArrow extends BaseActor {
    public ColorSelectorArrow(float x, float y, float width, float height, boolean pointingLeft, Stage s) {
        super(x, y, s);
        loadTexture("arrow_left.png");
        setSize(width, height);
        setOrigin(Align.center);
        setRotation(pointingLeft ? 0 : 180);
    }

    public void click() {
        clearActions();
        Action click = Actions.sequence(Actions.scaleTo(0.90f, 0.90f), Actions.delay(0.2f),
                Actions.scaleTo(1.0f, 1.0f));
        addAction(click);
    }
}
