package szm.orde4c.game.assets;

import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.Gdx;
import org.junit.Test;
import org.junit.runner.RunWith;
import szm.orde4c.game.GdxTestRunner;

@RunWith(GdxTestRunner.class)
public class AssetsExistTest {
    @Test
    public void loadingScreenAssetsExist() {
        assertTrue(Gdx.files.internal("loading.jpg").exists());
    }
}
