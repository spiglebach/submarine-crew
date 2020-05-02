package szm.orde4c.game.entity.stationary;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.entity.Drillable;

public class Rock extends RandomizedAreaObject implements Drillable {
    private static final int ROCK_VARIATIONS = 3;
    private static final float COLOR_RED = 1;
    private static final float COLOR_BLUE = 0;
    private static final float COLOR_GREEN_MINIMUM = 0.5f;
    private static final float COLOR_GREEN_MAXIMUM= 1;
    private static final float MINIMUM_SIZE = 100;
    private static final float MAXIMUM_SIZE = 300;
    private static final int MINIMUM_HEALTH = 20;
    private static final int MAXIMUM_HEALTH = 40;

    public Rock(Area area, Stage s) {
        super(area, MINIMUM_SIZE, MAXIMUM_SIZE, MINIMUM_HEALTH, MAXIMUM_HEALTH, s);
    }

    @Override
    protected void randomizeTexture() {
        int randomRockTextureNumber = MathUtils.random(ROCK_VARIATIONS);
        loadTexture("level/rock_" + randomRockTextureNumber + ".png");

        float randomGreenColor = MathUtils.random(COLOR_GREEN_MINIMUM, COLOR_GREEN_MAXIMUM);
        setColor(COLOR_RED, randomGreenColor, COLOR_BLUE, 1);
    }
}
