package szm.orde4c.game.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.util.Assets;

public class LevelSelectorCursor extends BaseActor {
    private LevelStamp nextLevel;
    private Vector2 transitionStartingPosition;
    private float transitionTimeLeft;
    private final float TRANSITION_TIME = 1;

    public LevelSelectorCursor(LevelStamp level, Stage s) {
        super(0, 0, s);
        nextLevel = level;
        loadAnimationFromSheet(Assets.instance.getTexture(Assets.LEVEL_SELECTOR_CURSOR), 1, 4, 0.2f, true);
        setSize(50, 50);
        setPosition(nextLevel.getX(), nextLevel.getY());
        transitionStartingPosition = new Vector2(nextLevel.getX(), nextLevel.getY());
        transitionTimeLeft = 0;
    }

    public void nextLevel(LevelStamp nextLevel) {
        this.nextLevel = nextLevel;
        transitionStartingPosition.set(getX(), getY());
        transitionTimeLeft = TRANSITION_TIME;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        transitionTimeLeft -= delta;
        float transitionAmount = delta / TRANSITION_TIME;

        float targetX = nextLevel.getX();
        float targetY = nextLevel.getY();

        Vector2 transitionStartToTargetVector = new Vector2(targetX, targetY).sub(transitionStartingPosition);
        transitionStartToTargetVector.scl(transitionAmount);
        moveBy(transitionStartToTargetVector.x, transitionStartToTargetVector.y);

        if (transitionTimeLeft <= 0) {
            transitionTimeLeft = 0;
            setPosition(nextLevel.getX(), nextLevel.getY());
            setAnimationPaused(true);
        } else {
            setAnimationPaused(false);
        }
    }
}
