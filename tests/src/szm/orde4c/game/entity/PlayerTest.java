package szm.orde4c.game.entity;

import com.badlogic.gdx.graphics.Color;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import szm.orde4c.game.GdxTestRunner;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.entity.submarine.Player;
import szm.orde4c.game.util.ControlType;
import szm.orde4c.game.util.PlayerInfo;

@RunWith(GdxTestRunner.class)
public class PlayerTest {
    PlayerInfo playerInfo;

    private Player player;

    private BaseActor otherActor;

    @Before
    public void setup() {
        playerInfo = new PlayerInfo(Color.BLACK, ControlType.KEYBOARD, null);
        player = new Player(0, 50, playerInfo, null);

        otherActor = new BaseActor(0, 0, null);
        otherActor.setSize(50, 50);
        otherActor.setBoundaryRectangle();
    }

    @Test
    public void belowOvelaps_assertTrue() {
        Assert.assertTrue(player.belowOverlaps(otherActor));
    }
}
