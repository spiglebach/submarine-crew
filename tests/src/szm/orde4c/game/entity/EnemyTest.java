package szm.orde4c.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import manifold.ext.api.Jailbreak;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import szm.orde4c.game.entity.stationary.Area;
import szm.orde4c.game.entity.submarine.Submarine;
import szm.orde4c.game.util.AreaObjectType;
import szm.orde4c.game.util.Assets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class EnemyTest {
    Stage stage;
    @Jailbreak Enemy enemy;
    @Jailbreak Submarine submarine;
    Area area;

    @Before
    public void setup() {
        Assets.instance.loadAsset(Assets.BLANK, Texture.class);
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
        enemy = new Enemy(0, 0, stage);
    }

    @Test
    public void queueNextAction_withOverlappingSubmarine_assertEquals() {
        enemy.setPosition(0, 0);
        enemy.targetPositionQueue.clear();
        enemy.queueNextAction();
        assertEquals(1, enemy.targetPositionQueue.size());
    }

    @Test
    public void queueNextAction_withOverlappingVegetation_assertNotEquals() {
        enemy.setPosition(0, 0);
        area = new Area(AreaObjectType.VEGETATION, -50, -50, 100, 100, 0, 20, stage);
        enemy.targetPositionQueue.clear();
        enemy.queueNextAction();
        assertNotEquals(0, enemy.targetPositionQueue.size());
    }

    @Test
    public void queueNextAction_withFarSubmarine_assertEquals() {
        enemy.setPosition(2000, 2000);
        enemy.targetPositionQueue.clear();
        enemy.queueNextAction();
        assertEquals(0, enemy.targetPositionQueue.size());
    }

    @Test
    public void getNextMoveAngle_withFarSubmarine_assertNotEquals() {
        enemy.setPosition(2000, 2000);
        enemy.targetPositionQueue.clear();
        enemy.enemyLastSeenPosition = null;
        enemy.queueNextAction();
        assertNotEquals(0, enemy.getNextMoveAngle(), 0.00001f);
    }

    @Test
    public void queueNextAction_withObstacles_assertEquals() {
        enemy.setPosition(1000, 0);
        area = new Area(AreaObjectType.ROCK, 500, -50, 100, 200, 0, 40, stage);
        enemy.targetPositionQueue.clear();
        enemy.enemyLastSeenPosition = null;
        enemy.queueNextAction();
        assertEquals(0, enemy.targetPositionQueue.size());
        assertNull(enemy.enemyLastSeenPosition);
    }

    @Test
    public void shootProjectile_withSubmarineSeen_assertNotEquals() {
        enemy.setPosition(-1000, 0);
        enemy.queueNextAction();
        int initialProjectileCount = enemy.projectileCount;
        enemy.shootProjectile();
        assertNotEquals(initialProjectileCount, enemy.projectileCount);
    }
}
