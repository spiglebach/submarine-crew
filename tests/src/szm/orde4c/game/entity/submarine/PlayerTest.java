package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import manifold.ext.api.Jailbreak;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import szm.orde4c.game.GdxTestRunner;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.util.ControlType;
import szm.orde4c.game.util.PlayerInfo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(GdxTestRunner.class)
public class PlayerTest {
    PlayerInfo playerInfo;

    @Jailbreak private Player player;
    private Stage stage;
    private Ladder ladder;
    private Station station;

    private BaseActor otherActor;

    @Before
    public void setup() {
        stage = new Stage(new StretchViewport(1000, 1000), mock(SpriteBatch.class));
        ladder = new Ladder(0, 0, 100, 200, stage);
        station = new EngineStation(0, 0, 100, 100, mock(Submarine.class), stage);
        playerInfo = new PlayerInfo(Color.BLACK, ControlType.KEYBOARD, null);
        player = new Player(0, 0, playerInfo, mock(Submarine.class), stage);

        otherActor = new BaseActor(0, 0, null);
        otherActor.setSize(50, 50);
        otherActor.setBoundaryRectangle();
    }

    @Test
    public void belowOvelaps_withPlayerExactlyOnOther_assertTrue() {
        player.setPosition(otherActor.getX(), otherActor.getHeight());
        assertTrue(player.belowOverlaps(otherActor));
    }

    @Test
    public void belowOvelaps_withPlayerInsideOther_assertTrue() {
        player.setPosition(otherActor.getX(), otherActor.getHeight() / 2f);
        assertTrue(player.belowOverlaps(otherActor));
    }

    @Test
    public void belowOvelaps_withPlayerNotNearOther_assertFalse() {
        player.setPosition(otherActor.getX() - player.getWidth() * 4, 0);
        assertFalse(player.belowOverlaps(otherActor));
    }

    @Test
    public void jump_withSimpleUsage_assertNotEquals() {
        Vector2 initialVelocity = player.velocityVector.cpy();
        player.jump();
        assertNotEquals(initialVelocity, player.velocityVector);
    }

    @Test
    public void climb_withUpwardAndPlayerOnBaseOfLadder_assertTrue() {
        player.stopClimbing();
        player.setPosition(0, 0);
        player.climbUp();
        assertTrue(player.isClimbing());
    }

    @Test
    public void climb_withDownwardAndPlayerOnBaseOfLadder_assertFalse() {
        player.stopClimbing();
        player.setPosition(0, 0);
        player.climbDown();
        assertFalse(player.isClimbing());
    }

    @Test
    public void climb_withDownwardAndPlayerOnTopOfLadder_assertTrue() {
        player.stopClimbing();
        player.setPosition(ladder.getX(), ladder.getY() + ladder.getHeight());
        player.climbDown();
        assertTrue(player.isClimbing());
    }

}
