package szm.orde4c.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public abstract class MenuLabel extends Label {
    public MenuLabel(CharSequence text, LabelStyle labelStyle) {
        super(text, labelStyle);
        setColor(Color.WHITE);
    }

    public abstract void execute();
}
