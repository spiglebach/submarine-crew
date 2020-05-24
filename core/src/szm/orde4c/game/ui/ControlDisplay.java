package szm.orde4c.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.util.Assets;

public class ControlDisplay extends BaseActor {
    public static final String TEXT_PRESS_TO_JOIN_FINALIZE = "Csatlakozás / szín véglegesítése";
    public static final String TEXT_PRESS_TO_LEAVE_CANCEL = "Hely elhagyása / véglegesítés visszavonása";
    public static final String TEXT_PRESS_TO_LOAD = "Mentés / pálya kiválasztása";
    public static final String TEXT_PRESS_TO_DELETE = "Mentés törlése";
    public static final String TEXT_PRESS_TO_SELECT_SAVE = "Mentés választása";
    public static final String TEXT_PRESS_TO_SELECT_LEVEL = "Szint választása";
    public static final String TEXT_PRESS_TO_SELECT_COLOR = "Szín választása";

    public ControlDisplay(TextButtonIndicatorPair[] textButtonIndicatorPairs, float stageWidthScale, float stageHeightScale, float fontScale, Stage stage) {
        super(0, 0, stage);
        loadTexture(Assets.instance.getTexture(Assets.BLANK));
        setSize(stage.getWidth() * stageWidthScale, stage.getHeight() * stageHeightScale);
        setColor(Color.BLACK);

        Table controlsTable = new Table();
        controlsTable.setFillParent(true);
        controlsTable.pad(15);
        for (TextButtonIndicatorPair pair : textButtonIndicatorPairs) {
            Label textLabel = new Label(pair.getText(), BaseGame.labelStyle);
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

            controlsTable.add(textLabel).expand().right();
            controlsTable.add(buttonIndicatorGroup).expand().left();
        }

        addActor(controlsTable);

    }


}

