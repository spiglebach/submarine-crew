package szm.orde4c.game.assets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import szm.orde4c.game.GdxTestRunner;
import szm.orde4c.game.util.Assets;

@RunWith(GdxTestRunner.class)
public class AssetsTest {
    @Test
    public void assetManagerAssetsExist() {
        while(!Assets.instance.update()) {

        }
    }

    @Test(expected = GdxRuntimeException.class)
    public void assetManagerLoadNotExistingAsset() throws GdxRuntimeException {
            Assets.instance.loadAsset("error.error", Texture.class);
    }
}
