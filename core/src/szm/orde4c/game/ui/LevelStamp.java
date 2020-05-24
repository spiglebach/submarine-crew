package szm.orde4c.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.util.Assets;

public class LevelStamp extends BaseActor {
    private Animation completedStamp;
    private Animation lockedStamp;
    private Animation nextStamp;
    private final Color COLOR_COMPLETED = Color.GREEN;
    private final Color COLOR_NEXT = Color.VIOLET;
    private final Color COLOR_LOCKED = Color.LIGHT_GRAY;
    private Label levelTitle;

    public LevelStamp(float x, float y, String title, Stage s) {
        super(x, y, s);
        completedStamp = loadTexture(Assets.instance.getTexture(Assets.LEVEL_SELECTOR_MAPSIGN_COMPLETE));
        nextStamp = loadTexture(Assets.instance.getTexture(Assets.LEVEL_SELECTOR_MAPSIGN_NEXT));
        lockedStamp = loadTexture(Assets.instance.getTexture(Assets.LEVEL_SELECTOR_MAPSIGN_LOCKED));
        setSize(75, 75);

        levelTitle = new Label(title, BaseGame.labelStyle);
        levelTitle.setFontScale(0.7f);
        Table layoutTable = new Table();
        layoutTable.setPosition(0, getHeight());
        layoutTable.setSize(getWidth(), getHeight() * 0.2f);
        layoutTable.add(levelTitle).expand();
        addActor(layoutTable);
    }

    public void completed() {
        setAnimation(completedStamp);
        levelTitle.setColor(COLOR_COMPLETED);
        setColor(COLOR_COMPLETED);
    }

    public void next() {
        setAnimation(nextStamp);
        levelTitle.setColor(COLOR_NEXT);
        setColor(COLOR_NEXT);
    }

    public void locked() {
        setAnimation(lockedStamp);
        levelTitle.setColor(COLOR_LOCKED);
        setColor(COLOR_LOCKED);
    }

    @Override
    public void setAnimation(Animation<TextureRegion> animation) {
        float width = getWidth();
        float height = getHeight();
        super.setAnimation(animation);
        setSize(width, height);
    }
}
