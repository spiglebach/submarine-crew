package szm.orde4c.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.entity.submarine.Submarine;
import szm.orde4c.game.util.Assets;

import java.util.function.Function;

public class AttributeBar extends BaseActor {
    private static final float BAR_WIDTH = 175;
    private static final float BAR_HEIGHT = 14;
    private static final float FRAME_PADDING = 2;

    private BaseActor bar;
    private Function<Submarine, Float> attributePercentFunction;
    private String text;
    private Label attributePercentLabel;

    public AttributeBar(int segments,String text, Color barColor, Function<Submarine, Float> attributePercentFunction, Stage s) {
        super(0, 0, s);
        loadTexture(Assets.instance.getTexture(Assets.BLANK));
        setSize(BAR_WIDTH + 2 * FRAME_PADDING, BAR_HEIGHT + 2 * FRAME_PADDING);
        setColor(Color.LIGHT_GRAY);
        this.text = text;
        this.attributePercentFunction = attributePercentFunction;

        BaseActor baseBar = new BaseActor(0, 0, s);
        baseBar.loadTexture(Assets.instance.getTexture(Assets.BLANK));
        baseBar.setSize(BAR_WIDTH, BAR_HEIGHT);
        baseBar.centerAtActor(this);
        addActor(baseBar);

        bar = new BaseActor(0, 0, s);
        bar.loadTexture(Assets.instance.getTexture(Assets.BLANK));
        bar.setSize(BAR_WIDTH, BAR_HEIGHT);
        bar.setColor(barColor);
        bar.centerAtActor(this);
        addActor(bar);

        for (int i = 1; i < segments; i++) {
            BaseActor barSeparator = new BaseActor(0, 0, s);
            barSeparator.loadTexture(Assets.instance.getTexture(Assets.BLANK));
            barSeparator.setSize(FRAME_PADDING * 2, getHeight());
            barSeparator.setPosition(FRAME_PADDING / 4f + BAR_WIDTH / segments * i, 0);
            barSeparator.setColor(Color.LIGHT_GRAY);
            addActor(barSeparator);
        }

        Table layoutTable = new Table();
        layoutTable.setFillParent(true);
        attributePercentLabel = new Label(text + "100%", BaseGame.labelStyle);
        attributePercentLabel.setFontScale(0.4f);
        attributePercentLabel.setColor(Color.BLACK);
        layoutTable.add(attributePercentLabel).expand();
        addActor(layoutTable);
    }

    public void update(Submarine submarine) {
        float attributePercent = attributePercentFunction.apply(submarine);
        bar.setWidth(MathUtils.clamp(BAR_WIDTH * attributePercent, 0, BAR_WIDTH));
        attributePercentLabel.setText(text + (attributePercent * 100) + "%");
    }
}
