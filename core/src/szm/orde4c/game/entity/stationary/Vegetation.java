package szm.orde4c.game.entity.stationary;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.entity.Cuttable;

public class Vegetation extends RandomizedAreaObject implements Cuttable {
    private static final int VEGETATION_VARIATIONS = 5;
    private static final float MINIMUM_SIZE = 100;
    private static final float MAXIMUM_SIZE = 300;
    private static final int MAXIMUM_HEALTH = 100;
    private static final int MINIMUM_HEALTH = 50;

    public Vegetation(Area area, Stage s) {
        super(area, MINIMUM_SIZE, MAXIMUM_SIZE, MINIMUM_HEALTH, MAXIMUM_HEALTH, s);
    }

    @Override
    protected void randomizeTexture() {
        int randomVegetationTextureNumber = MathUtils.random(VEGETATION_VARIATIONS);
        loadTexture("level/vegetation_" + randomVegetationTextureNumber + ".png");
    }
}
