package szm.orde4c.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.util.Save;

public class SaveSlot extends BaseActor {
    private boolean emptySlot;

    public SaveSlot(float width, float height, int maximumLevelIndex, Save save, Stage s) {
        super(0, 0, s);
        emptySlot = save == null;
        loadTexture("saveslot.jpg");
        setSize(width, height);
        if (save != null) {
            int completedLevels = save.getCompletedLevels();
            float completionPercent = (float) completedLevels * 100 / (float) maximumLevelIndex;

            Label saveNameLabel = new Label(String.format("%d. Mentés", save.getId()), BaseGame.largeLabelStyle);
            saveNameLabel.setFontScale(0.5f);

            Label completionPercentLabel = new Label("Haladás: " + (int) Math.ceil(completionPercent) + "%", BaseGame.largeLabelStyle);
            completionPercentLabel.setFontScale(0.5f);
            Label completedLevelsLabel = new Label(String.format("Végigvitt szintek: %d", completedLevels), BaseGame.largeLabelStyle);
            completedLevelsLabel.setFontScale(0.5f);
            Table layoutTable = new Table();
            layoutTable.setFillParent(true);
            addActor(layoutTable);

            layoutTable.add(saveNameLabel).expand();
            layoutTable.row();
            layoutTable.add(completionPercentLabel).expand();
            layoutTable.row();
            layoutTable.add(completedLevelsLabel).expand();
        } else {
            setColor(Color.FIREBRICK);
        }
    }

    public void saveDeleted() {
        clear();
        emptySlot = true;
        setColor(Color.FIREBRICK);
    }

    public boolean isEmptySlot() {
        return emptySlot;
    }
}
