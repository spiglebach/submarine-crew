package szm.orde4c.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.BaseGame;

public class ControlDisplay extends BaseActor {
    public static final String TEXT_PRESS_TO_JOIN_FINALIZE = "Press to join / finalize";
    public static final String TEXT_PRESS_TO_LEAVE_CANCEL = "Press to leave / cancel";
    public static final String TEXT_PRESS_TO_LOAD = "Press to load game";
    public static final String TEXT_PRESS_TO_DELETE = "Press to delete save";

    public ControlDisplay(TextButtonIndicatorPair[] textButtonIndicatorPairs, float stageWidthScale, float stageHeightScale, float fontScale, Stage stage) {
        super(0, 0, stage);
        loadTexture("platform.png");
        setSize(stage.getWidth() * stageWidthScale, stage.getHeight() * stageHeightScale);
        setColor(Color.PURPLE);

        Table controlsTable = new Table();
        controlsTable.setFillParent(true);
        for (TextButtonIndicatorPair pair : textButtonIndicatorPairs) {
            Label textLabel = new Label(pair.getText(), BaseGame.largeLabelStyle);
            textLabel.setFontScale(fontScale);

            float totalButtonIndicatorHeight = 0;
            float totalButtonIndicatorWidth = 0;
            BaseActor buttonIndicatorGroup = new BaseActor(0, 0, stage);
            for (int id : pair.getButtonIndicatorIds()) {
                ButtonIndicator indicator = new ButtonIndicator(getHeight(), id, stage);
                buttonIndicatorGroup.addActor(indicator);
                indicator.setPosition(totalButtonIndicatorWidth, 0);
                totalButtonIndicatorHeight = indicator.getHeight();
                totalButtonIndicatorWidth += indicator.getWidth();
            }
            buttonIndicatorGroup.setSize(totalButtonIndicatorWidth * totalButtonIndicatorHeight / getHeight(), getHeight());

            controlsTable.add(textLabel).expandY();
            controlsTable.add(buttonIndicatorGroup).expand().left();
        }

        addActor(controlsTable);

    }


}

