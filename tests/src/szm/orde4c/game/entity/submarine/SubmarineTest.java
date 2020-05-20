package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import manifold.ext.api.Jailbreak;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import szm.orde4c.game.GdxTestRunner;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.TileMapActor;
import szm.orde4c.game.util.Assets;
import szm.orde4c.game.util.ControlType;
import szm.orde4c.game.util.PlayerInfo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(GdxTestRunner.class)
public class SubmarineTest {
    Stage stage;
    BaseActor otherActor;
    TileMapActor tileMapActor;
    PlayerInfo playerInfo;
    @Jailbreak Submarine submarine;

    @Before
    public void setup() {
        stage = new Stage(new StretchViewport(1000, 1000), mock(SpriteBatch.class));
        tileMapActor = new TileMapActor("submarine/submarine.tmx");
        Assets.instance.loadAsset(Assets.SUBMARINE_IMAGE, Texture.class);
        submarine = new Submarine(0, 0, stage);
        playerInfo = new PlayerInfo(Color.BLACK, ControlType.CONTROLLER, null);
        otherActor = new BaseActor(0, 0, stage);
        otherActor.setSize(400, 400);
        otherActor.setBoundaryRectangle();
    }

    @Test
    public void loadSubmarineBoundaryPolygon_liveSubmarine_pass() {
        submarine.loadSubmarineBoundaryPolygon(tileMapActor);
        assertNotNull(submarine.boundaryPolygon);
    }

    @Test
    public void loadSubmarineSolids_liveSubmarine_pass() {
        submarine.loadSubmarineSolids(tileMapActor);
        assertFalse(BaseActor.getList(submarine, "szm.orde4c.game.entity.stationary.Solid").isEmpty());
    }

    @Test
    public void loadSubmarineLadders_liveSubmarine_pass() {
        submarine.loadSubmarineLadders(tileMapActor);
        assertFalse(BaseActor.getList(submarine, "szm.orde4c.game.entity.submarine.Ladder").isEmpty());
    }

    @Test
    public void accelerateForward_simpleUsage_assertNotSame() {
        Vector2 accelerationVector = submarine.accelerationVector.cpy();
        submarine.accelerateForward();
        assertNotSame(accelerationVector, submarine.accelerationVector);
    }

    @Test
    public void accelerateBackward_simpleUsage_assertNotSame() {
        Vector2 accelerationVector = submarine.accelerationVector.cpy();
        submarine.accelerateBackward();
        assertNotSame(accelerationVector, submarine.accelerationVector);
    }

    @Test
    public void liftNose_simpleUsage_assertEquals() {
        submarine.liftNose();
        assertEquals(1, submarine.rotationDirection);
    }

    @Test
    public void lowerNose_simpleUsage_assertEquals() {
        submarine.lowerNose();
        assertEquals(-1, submarine.rotationDirection);
    }

    @Test
    public void descend_simpleUsage_assertNotSame() {
        Vector2 accelerationVector = submarine.accelerationVector.cpy();
        submarine.descend();
        assertNotSame(accelerationVector, submarine.accelerationVector);
    }

    @Test
    public void ascend_simpleUsage_assertNotSame() {
        Vector2 accelerationVector = submarine.accelerationVector.cpy();
        submarine.ascend();
        assertNotSame(accelerationVector, submarine.accelerationVector);
    }

    @Test
    public void damage_withEnoughShields_assertEquals() {
        submarine.shield = 33;
        submarine.health = 10;
        submarine.damage(10);
        assertEquals(10, submarine.health, 0.00001f);
    }

    @Test
    public void damage_withoutEnoughShieldsAndLessDamageThanHealth_assertEquals() {
        submarine.shield = 32;
        submarine.health = 10;
        submarine.damage(8);
        assertEquals(2, submarine.health, 0.00001f);
    }

    @Test
    public void damage_withoutEnoughShieldsAndMoreDamageThanHealth_assertEquals() {
        submarine.shield = 32;
        submarine.health = 10;
        submarine.damage(1000);
        assertEquals(0, submarine.health, 0.00001f);
    }

    @Test
    public void damage_withEnoughShieldsAndMoreDamageThanHealth_assertEquals() {
        submarine.shield = 40;
        submarine.health = 10;
        submarine.damage(1000);
        assertEquals(10, submarine.health, 0.00001f);
    }

    @Test
    public void getBoundaryPolygon_withOffset_assertEquals() {
        submarine.setBoundaryPolygonOffset(30, 40);
        Polygon processedSubmarineBoundaryPolygon = submarine.getBoundaryPolygon();
        float calculatedSubmarineBoundaryPolygonX = submarine.getX() + 30;
        float calculatedSubmarineBoundaryPolygonY = submarine.getY() + 40;
        assertEquals(processedSubmarineBoundaryPolygon.getX(), calculatedSubmarineBoundaryPolygonX, 0.00001f);
        assertEquals(processedSubmarineBoundaryPolygon.getY(), calculatedSubmarineBoundaryPolygonY, 0.00001f);
    }

    @Test
    public void getBoundaryPolygon_withoutOffset_assertEquals() {
        submarine.setBoundaryPolygonOffset(0, 0);
        Polygon processedSubmarineBoundaryPolygon = submarine.getBoundaryPolygon();
        float calculatedSubmarineBoundaryPolygonX = submarine.getX();
        float calculatedSubmarineBoundaryPolygonY = submarine.getY();
        assertEquals(processedSubmarineBoundaryPolygon.getX(), calculatedSubmarineBoundaryPolygonX, 0.00001f);
        assertEquals(processedSubmarineBoundaryPolygon.getY(), calculatedSubmarineBoundaryPolygonY, 0.00001f);
    }

    @Test
    public void isDestroyed_withMoreThanZeroHealth_assertFalse() {
        submarine.health = 10;
        assertFalse(submarine.isDestroyed());
    }

    @Test
    public void isDestroyed_withZeroHealth_assertTrue() {
        submarine.health = 0;
        assertTrue(submarine.isDestroyed());
    }

    @Test
    public void getEnergy_with10Energy_assertEquals() {
        submarine.energy = 10;
        assertEquals(10, submarine.getEnergy(), 0.000001f);
    }

    @Test
    public void getEnergyPercent_withZeroPercent_assertEquals() {
        submarine.energy = 0;
        assertEquals(0, submarine.getEnergyPercent(), 0.000001f);
    }

    @Test
    public void getEnergyPercent_withMoreThanZeroPercent_assertNotEquals() {
        submarine.energy = 32;
        assertNotEquals(0, submarine.getEnergyPercent(), 0.00001f);
    }

    @Test
    public void getHealthPercent_withZeroPercent_assertEquals() {
        submarine.health = 0;
        assertEquals(0, submarine.getHealthPercent(), 0.000001f);
    }

    @Test
    public void getHealthPercent_withMoreThanZeroPercent_assertNotEquals() {
        submarine.health = 32;
        assertNotEquals(0, submarine.getHealthPercent(), 0.00001f);
    }

    @Test
    public void getShieldPercent_withZeroPercent_assertEquals() {
        submarine.shield = 0;
        assertEquals(0, submarine.getShieldPercent(), 0.000001f);
    }

    @Test
    public void getShieldPercent_withMoreThanZeroPercent_assertNotEquals() {
        submarine.shield = 32;
        assertNotEquals(0, submarine.getShieldPercent(), 0.00001f);
    }

    @Test
    public void increaseHealth_withMoreThanMax_assertEquals() {
        submarine.health = 80;
        submarine.increaseHealth(40);
        assertEquals(100, submarine.health, 0.00001f);
    }

    @Test
    public void increaseShield_withMoreThanMax_assertEquals() {
        submarine.shield = 80;
        submarine.increaseShield(55);
        assertEquals(100, submarine.shield, 0.00001f);
    }

    @Test
    public void applyPhysics_withAcceleratingForwardAndUpward_assertNotEquals() {
        Vector2 initialPosition = submarine.getPosition();
        submarine.accelerateForward();
        submarine.ascend();
        submarine.applyPhysics(20);
        assertNotEquals(initialPosition, submarine.getPosition());
    }

    @Test
    public void applyPhysics_withoutAcceleratingOrDecelerating_assertEquals() {
        Vector2 initialPosition = submarine.getPosition();
        submarine.applyPhysics(20);
        assertEquals(initialPosition, submarine.getPosition());
    }

    @Test
    public void applyPhysics_withDecelarating_assertNotEquals() {
        submarine.velocityVector = new Vector2(20, 20);
        submarine.applyPhysics(20);
        assertNotEquals(new Vector2(20, 20), submarine.velocityVector.cpy());
    }

    @Test
    public void applyStationContiniousEnergyConsumption_withInactiveStations_assertEquals() {
        float initialSubmarineEnergy = submarine.getEnergy();
        for (BaseActor stationActor : BaseActor.getList(submarine, "szm.orde4c.game.entity.submarine.Station")) {
            Station station = (Station) stationActor;
            station.activated = false;
        }
        submarine.applyStationContiniousEnergyConsumption(20);
        assertEquals(initialSubmarineEnergy, submarine.getEnergy(), 0.00001f);
    }

    @Test
    public void applyStationContiniousEnergyConsumption_withActiveStations_assertNotEquals() {
        float initialSubmarineEnergy = submarine.getEnergy();
        for (BaseActor stationActor : BaseActor.getList(submarine, "szm.orde4c.game.entity.submarine.Station")) {
            Station station = (Station) stationActor;
            station.activated = true;
        }
        submarine.applyStationContiniousEnergyConsumption(20);
        assertNotEquals(initialSubmarineEnergy, submarine.getEnergy(), 0.00001f);
    }

    @Test
    public void rechargeEnergy_withZeroEnergyAndNonZeroDelta() {
        submarine.energy = 0;
        submarine.rechargeEnergy(20);
        assertNotEquals(0, submarine.getEnergy(), 0.00001f);
    }

    @Test
    public void bump_withGreaterHorizontalVelocity_assertEquals() {
        Vector2 bumpVector = new Vector2(0, -10);
        submarine.velocityVector = new Vector2(30, 20);
        submarine.bump(bumpVector.cpy());
        assertEquals(bumpVector.angle(), submarine.velocityVector.angle(), 0.000001f);
    }

    @Test
    public void bump_withGreaterVerticalVelocity_assertEquals() {
        Vector2 bumpVector = new Vector2(0, -10);
        submarine.velocityVector = new Vector2(30, 55);
        submarine.bump(bumpVector.cpy());
        assertEquals(bumpVector.angle(), submarine.velocityVector.angle(), 0.000001f);
    }

    @Test
    public void bump_withGreatEnoughVelocityToDamageSubmarineShields_assertNotEquals() {
        submarine.shield = 40;
        Vector2 bumpVector = new Vector2(0, -10);
        submarine.velocityVector = new Vector2(submarine.verticalMaxSpeed, submarine.horizontalMaxSpeed);
        submarine.bump(bumpVector.cpy());
        assertNotEquals(40, submarine.shield, 0.000001f);
    }

    @Test
    public void preventOverlap_withOverlappingOtherActor_assertNotEquals() {
        Vector2 initialPosition = submarine.getPosition().cpy();
        otherActor.setPosition(0, 0);
        submarine.preventOverlap(otherActor);
        assertNotEquals(initialPosition, submarine.getPosition());
    }

    @Test
    public void preventOverlap_withNotOverlappingOtherActor_assertEquals() {
        Vector2 initialPosition = submarine.getPosition().cpy();
        otherActor.setPosition(-500, -500);
        submarine.preventOverlap(otherActor);
        assertEquals(initialPosition, submarine.getPosition());
    }

    @Test
    public void keyDown_withKeyboardPlayerKeypressEOnStation_assertTrue() {
        submarine.initializePlayers(new PlayerInfo[]{playerInfo});
        @Jailbreak Player player = submarine.players.get(0);
        player.keyboardPlayer = true;
        Station station = (Station) BaseActor.getList(submarine, "szm.orde4c.game.entity.submarine.Station").get(0);
        player.setPosition(station.getX(), station.getY());
        assertTrue(submarine.keyDown(Input.Keys.E));
    }

    @Test
    public void keyDown_withKeyboardPlayerKeypressENotOnStation_assertFalse() {
        submarine.initializePlayers(new PlayerInfo[]{playerInfo});
        @Jailbreak Player player = submarine.players.get(0);
        player.keyboardPlayer = true;
        player.setPosition(-100, -100);
        assertFalse(submarine.keyDown(Input.Keys.E));
    }
}
