package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import manifold.ext.api.Jailbreak;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import szm.orde4c.game.GdxTestRunner;
import szm.orde4c.game.util.Assets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(GdxTestRunner.class)
public class ReflectorStationTest {
    @Jailbreak ReflectorStation station;
    @Jailbreak Submarine submarine;
    Stage stage;

    @Before
    public void setup() {
        Assets.instance.loadAsset(Assets.SUBMARINE_IMAGE, Texture.class);
        Assets.instance.loadAsset(Assets.SUBMARINE_REFLECTOR_LIGHT_INACTIVE, Texture.class);
        Assets.instance.loadAsset(Assets.SUBMARINE_REFLECTOR_LIGHT_ACTIVE, Texture.class);
        Assets.instance.loadAsset(Assets.SUBMARINE_CUTTER, Texture.class);
        Assets.instance.loadAsset(Assets.SUBMARINE_DRILL, Texture.class);
        Assets.instance.loadAsset(Assets.SUBMARINE_ELEVATOR, Texture.class);
        stage = new Stage(new StretchViewport(1000, 1000), mock(SpriteBatch.class));
        submarine = new Submarine(0, 0, stage);
        station = new ReflectorStation(0, 0, 100, 100, submarine, stage);
    }

    @Test
    public void act_withRotatedSubmarineAndUnrotatedReflector_assertEquals() {
        submarine.setRotation(30);
        station.act(10);
        assertEquals(submarine.getRotation(), station.reflectorActor.getRotation(), 0.000001);
    }

    @Test
    public void act_withRotatedSubmarineAndRotatedReflector_assertEquals() {
        submarine.setRotation(30);
        station.reflectorRotation = -30;
        station.act(10);
        assertEquals(0, station.reflectorActor.getRotation(), 0.000001);
    }

    @Test
    public void continiousEnergyConsumption_withEnoughEnergy_assertTrue() {
        submarine.energy = 100;
        station.activated = true;
        station.continiousEnergyConsumption(10);
        assertTrue(submarine.getEnergy() < 100);
        assertTrue(station.activated);
    }

    @Test
    public void continiousEnergyConsumption_withoutEnoughEnergy_assertTrue() {
        submarine.energy = 5;
        station.activated = true;
        station.continiousEnergyConsumption(10);
        assertTrue(submarine.getEnergy() < 100);
        assertFalse(station.activated);
    }
}
