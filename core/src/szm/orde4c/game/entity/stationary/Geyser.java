package szm.orde4c.game.entity.stationary;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.effect.GeyserBubblingEffect;
import szm.orde4c.game.util.Assets;

public class Geyser extends BaseActor { // TODO integrate into map
    private final static int GEYSER_TEXTURE_VARIATIONS = 2;
    public Geyser(float x, float y, float width, float height, Stage s) {
        super(x, y, s);
        randomizeTexture();
        setSize(width, height);
        setBoundaryRectangle();
        GeyserBubblingEffect effect = new GeyserBubblingEffect();
        effect.setPosition(getWidth() / 2f - effect.getWidth() / 2f, getHeight());
        addActor(effect);
    }

    private void randomizeTexture() {
        int randomTextureIndex = MathUtils.random(GEYSER_TEXTURE_VARIATIONS);
        loadTexture(Assets.instance.getTexture(String.format("level/geyser_%d.png", randomTextureIndex)));
    }

}
