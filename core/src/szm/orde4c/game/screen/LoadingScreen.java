package szm.orde4c.game.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.base.BaseScreen;
import szm.orde4c.game.util.Assets;

public class LoadingScreen extends BaseScreen {
    private Label currentStateLabel;
    private Label stateCountLabel;
    private BaseActor loadingBar;
    private float totalBarWidth;

    @Override
    public void initialize() {
        BaseActor background = new BaseActor(0, 0, uiStage);
        background.loadTexture(Assets.instance.getTexture(Assets.LOADING_SCREEN));
        background.setSize(uiStage.getWidth(), uiStage.getHeight());
        background.setOpacity(0.5f);
        BaseActor frame = new BaseActor(0, 0, uiStage);
        frame.loadTexture(Assets.instance.getTexture(Assets.BLANK));
        frame.setSize(uiStage.getWidth(), uiStage.getHeight() * 0.1f);
        frame.setColor(Color.GOLD);

        float padding = frame.getHeight() * 0.05f;
        float totalBarHeight = frame.getHeight() - 2 * padding;
        totalBarWidth = uiStage.getWidth() - 2 * padding;

        BaseActor innerFrame = new BaseActor(padding, padding, uiStage);
        innerFrame.loadTexture(Assets.instance.getTexture(Assets.BLANK));
        innerFrame.setSize(totalBarWidth, totalBarHeight);
        innerFrame.setColor(Color.WHITE);

        loadingBar = new BaseActor(padding, padding, uiStage);
        loadingBar.loadTexture(Assets.instance.getTexture(Assets.BLANK));
        loadingBar.setSize(padding, totalBarHeight);
        loadingBar.setColor(Color.NAVY);

        currentStateLabel = new Label("Töltés...", BaseGame.labelStyle);
        currentStateLabel.setColor(Color.RED);

        Table stateCountTable = new Table();
        stateCountTable.setPosition(innerFrame.getX(), innerFrame.getY());
        stateCountTable.setSize(innerFrame.getWidth(), innerFrame.getHeight());
        stateCountLabel = new Label(String.format("%d / %d", Assets.instance.getStateIndex(), Assets.instance.getStateCount()), BaseGame.labelStyle);
        stateCountTable.add(stateCountLabel).expand();
        stateCountLabel.setColor(Color.RED);

        frame.addActor(innerFrame);
        frame.addActor(loadingBar);
        frame.addActor(stateCountTable);

        uiTable.add(currentStateLabel).expand().bottom();
        uiTable.row();
        uiTable.add(frame).expandX();
    }

    @Override
    public void update(float dt) {
        if (Assets.instance.update()) {
            BaseGame.setActiveScreen(new MainMenuScreen());
        }
        currentStateLabel.setText(Assets.instance.getState());
        stateCountLabel.setText(String.format("%d / %d", Assets.instance.getStateIndex(), Assets.instance.getStateCount()));
        loadingBar.setSize(totalBarWidth * Assets.instance.getProgress(), loadingBar.getHeight());
    }
}
