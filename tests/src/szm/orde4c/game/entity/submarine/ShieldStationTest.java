package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Input;
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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(GdxTestRunner.class)
public class ShieldStationTest {
    @Jailbreak ShieldStation station;
    @Jailbreak Submarine submarine;
    Stage stage;

    @Before
    public void setup() {
        Assets.instance.loadAsset(Assets.SUBMARINE_IMAGE, Texture.class);
        Assets.instance.loadAsset(Assets.SUBMARINE_REFLECTOR_LIGHT_INACTIVE, Texture.class);
        Assets.instance.loadAsset(Assets.SUBMARINE_REFLECTOR_LIGHT_ACTIVE, Texture.class);
        Assets.instance.loadAsset(Assets.SUBMARINE_ELEVATOR, Texture.class);
        stage = new Stage(new StretchViewport(1000, 1000), mock(SpriteBatch.class));
        submarine = new Submarine(0, 0, stage);
        station = new ShieldStation(0, 0, 100, 100, submarine, stage);
    }

    @Test
    public void keyDown_withKeyE_assertTrue() {
        submarine.shield = 50;
        station.keyDown(Input.Keys.E);
        assertTrue(submarine.shield > 50);
    }

    @Test
    public void chargeSubmarineShields_simpleUsage_assertTrue() {
        submarine.energy = 50;
        submarine.shield = 50;
        station.chargeSubmarineShields();
        assertTrue(submarine.shield > 50);
        assertTrue(submarine.energy < 50);
    }
}
