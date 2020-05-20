package szm.orde4c.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.util.Assets;

public class TextDisplayScreen extends BaseActor {
    public TextDisplayScreen(String text, Color color, float backgroundOpacity, Stage s) {
        super(0, 0, s);
        setSize(s.getWidth(), s.getHeight());
        BaseActor background = new BaseActor(0, 0, s);
        background.loadTexture(Assets.instance.getTexture(Assets.BLANK));
        background.setSize(s.getWidth(), s.getHeight());
        background.setColor(Color.BLACK);
        background.setOpacity(backgroundOpacity);
        background.centerAtActor(this);
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        Label textLabel = new Label(text, BaseGame.labelStyle);
        textLabel.setColor(color);
        uiTable.add(textLabel).expand();
        addActor(background);
        addActor(uiTable);
        setVisible(false);
    }
}
