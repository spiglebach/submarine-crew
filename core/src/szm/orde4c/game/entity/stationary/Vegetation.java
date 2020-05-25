package szm.orde4c.game.entity.stationary;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.entity.Cuttable;
import szm.orde4c.game.util.Assets;

public class Vegetation extends RandomizedAreaObject implements Cuttable {
    private static final float MINIMUM_SIZE = 100;
    private static final float MAXIMUM_SIZE = 250;
    private static final int MAXIMUM_HEALTH = 100;
    private static final int MINIMUM_HEALTH = 50;
    private static final String[] FILE_NAMES = new String[]{
            Assets.LEVEL_VEGETATION_0,
            Assets.LEVEL_VEGETATION_1,
            Assets.LEVEL_VEGETATION_2,
            Assets.LEVEL_VEGETATION_3,
            Assets.LEVEL_VEGETATION_4,
            Assets.LEVEL_VEGETATION_5
    };

    public Vegetation(Area area, Stage s) {
        super(area, MINIMUM_SIZE, MAXIMUM_SIZE, MINIMUM_HEALTH, MAXIMUM_HEALTH, s);
    }

    @Override
    protected void randomizeTexture() {
        int randomVegetationTextureIndex = MathUtils.random(FILE_NAMES.length - 1);
        loadTexture(Assets.instance.getTexture(FILE_NAMES[randomVegetationTextureIndex]));
    }
}
