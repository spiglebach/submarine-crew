package szm.orde4c.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import manifold.ext.api.Jailbreak;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import szm.orde4c.game.GdxTestRunner;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.entity.Damageable;
import szm.orde4c.game.entity.Torpedo;
import szm.orde4c.game.entity.stationary.Area;
import szm.orde4c.game.util.AreaObjectType;
import szm.orde4c.game.util.Assets;

@RunWith(GdxTestRunner.class)
public class TorpedoTest {
    @Jailbreak Torpedo torpedo;
    Stage stage;
    Area rockArea;

    @Before
    public void setup() {
        Assets.instance.loadAsset(Assets.LEVEL_ROCK_0, Texture.class);
        Assets.instance.loadAsset(Assets.LEVEL_ROCK_1, Texture.class);
        Assets.instance.loadAsset(Assets.LEVEL_ROCK_2, Texture.class);
        Assets.instance.loadAsset(Assets.LEVEL_ROCK_3, Texture.class);
        stage = new Stage(new StretchViewport(1000, 1000), mock(Batch.class));
        torpedo = new Torpedo(0, 0, 0, stage);
        rockArea = new Area(AreaObjectType.ROCK, -50, -50, 100, 100, 0, 10, stage);
    }

    @Test
    public void explode_withAdjacentRocks_assertEquals() {
        torpedo.explode();
        for (BaseActor actor : BaseActor.getList(stage, "szm.orde4c.game.entity.stationary.Rock")) {
            Damageable damageable = (Damageable) actor;
            if (damageable.isDestroyed()) {
                actor.remove();
            }
        }
        assertEquals(0, BaseActor.count(stage, "szm.orde4c.game.entity.stationary.Rock"));
    }
}
