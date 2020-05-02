package szm.orde4c.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.entity.submarine.Submarine;

import java.util.function.Function;

public class AttributeBar extends BaseActor {
    private static final float BAR_WIDTH = 700;
    private static final float BAR_HEIGHT = 50;
    private static final float FRAME_PADDING = 10;

    private Function<Submarine, Float> attributePercentFunction;

    private BaseActor bar;

    public AttributeBar(int segments, Color barColor, Function<Submarine, Float> attributePercentFunction, Stage s) {
        super(0, 0, s);
        loadTexture("platform.png");
        setSize(BAR_WIDTH + 2 * FRAME_PADDING, BAR_HEIGHT + 2 * FRAME_PADDING);

        this.attributePercentFunction = attributePercentFunction;

        bar = new BaseActor(0, 0, s);
        bar.loadTexture("platform.png");
        bar.setSize(BAR_WIDTH, BAR_HEIGHT);
        bar.setColor(barColor);
        bar.centerAtActor(this);
        addActor(bar);

        for (int i = 1; i < segments; i++) {
            BaseActor barSeparator = new BaseActor(0, 0, s);
            barSeparator.loadTexture("platform.png");
            barSeparator.setSize(FRAME_PADDING / 2.0f, getHeight());
            barSeparator.setColor(Color.BLACK);
            barSeparator.setPosition(FRAME_PADDING + BAR_WIDTH / segments * i, 0);
            addActor(barSeparator);
        }
    }

    public void update(Submarine submarine) {
        float attributePercent = attributePercentFunction.apply(submarine);
        bar.setWidth(MathUtils.clamp(BAR_WIDTH * attributePercent, 0, BAR_WIDTH));
    }
}
