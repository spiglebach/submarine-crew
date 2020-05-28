package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import manifold.ext.api.Jailbreak;
import org.junit.Before;
import org.junit.Test;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.entity.stationary.Area;
import szm.orde4c.game.entity.stationary.Rock;
import szm.orde4c.game.util.AreaObjectType;
import szm.orde4c.game.util.Assets;
import szm.orde4c.game.util.Direction;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ArmTest {
    @Jailbreak Arm arm;
    @Jailbreak Submarine submarine;
    @Jailbreak ArmStation armStation;
    Stage stage;
    Area area;
    Rock rock;

    @Before
    public void setup() {
        Assets.instance.loadAsset(Assets.LEVEL_ROCK_0, Texture.class);
        Assets.instance.loadAsset(Assets.LEVEL_ROCK_1, Texture.class);
        Assets.instance.loadAsset(Assets.LEVEL_ROCK_2, Texture.class);
        Assets.instance.loadAsset(Assets.LEVEL_ROCK_3, Texture.class);
        Assets.instance.loadAsset(Assets.LEVEL_VEGETATION_0, Texture.class);
        Assets.instance.loadAsset(Assets.LEVEL_VEGETATION_1, Texture.class);
        Assets.instance.loadAsset(Assets.LEVEL_VEGETATION_2, Texture.class);
        Assets.instance.loadAsset(Assets.LEVEL_VEGETATION_3, Texture.class);
        Assets.instance.loadAsset(Assets.LEVEL_VEGETATION_4, Texture.class);
        Assets.instance.loadAsset(Assets.LEVEL_VEGETATION_5, Texture.class);
        Assets.instance.loadAsset(Assets.ENEMY, Texture.class);
        Assets.instance.loadAsset(Assets.SUBMARINE_IMAGE, Texture.class);
        Assets.instance.loadAsset(Assets.SUBMARINE_DRILL, Texture.class);
        Assets.instance.loadAsset(Assets.SUBMARINE_CUTTER, Texture.class);
        Assets.instance.loadAsset(Assets.SUBMARINE_ELEVATOR, Texture.class);
        stage = new Stage(new StretchViewport(1000, 1000), mock(Batch.class));
        submarine = new Submarine(0, 0, stage);
        arm = new Arm(200, 0, Direction.EAST, stage);
        armStation = new ArmStation(0, 0, 100, 100, arm, submarine, stage);
        area = new Area(AreaObjectType.ROCK, 250, -100, 100, 200, 0, 40, stage);
    }

    @Test
    public void processObjectWithSensorPolygon_withObjectsInRange_assertNotEquals() {
        for (BaseActor actor : BaseActor.getList(stage, "szm.orde4c.game.entity.stationary.Rock")) {
            arm.processObjectWithSensorPolygon(actor);
        }
        assertNotEquals(0, arm.objectsInRange);
    }

    @Test
    public void makeAutonomousMove_withDrillableObjectsInRange_assertNotEquals() {
        for (BaseActor actor : BaseActor.getList(stage, "szm.orde4c.game.entity.stationary.Rock")) {
            arm.processObjectWithSensorPolygon(actor);
        }
        arm.makeAutonomousAction();
        assertNotEquals(0, arm.extensionAmount);
    }

    @Test
    public void makeAutonomousMove_withDrillableObjectsInRange_assertTrue() {
        for (BaseActor actor : BaseActor.getList(stage, "szm.orde4c.game.entity.stationary.Rock")) {
            arm.processObjectWithSensorPolygon(actor);
        }
        arm.makeAutonomousAction();
        assertTrue(armStation.isActivated());
    }
}
