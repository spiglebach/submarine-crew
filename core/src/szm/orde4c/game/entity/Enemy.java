package szm.orde4c.game.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.entity.submarine.Submarine;
import szm.orde4c.game.util.Assets;

import java.util.*;

public class Enemy extends BaseActor implements Damageable {
    private float maximumDistanceFromSubmarineBeforeDeletion;
    private final float MAX_COLLISTION_DAMAGE_COOLDOWN = 3f;
    private float collisionDamageCooldown;
    private final int MAX_HEALTH = 500;

    private final int PROJECTILE_COUNT = 5;
    private final float PROJECTILE_COOLDOWN = 5;
    private int projectileCount;
    private float projectileCooldown;


    private boolean seesSubmarine;
    private Vector2 idlingPosition;
    private Vector2 enemyLastSeenPosition;
    private Deque<Vector2> targetPositionQueue;
    private BaseActor largeProximitySensor;
    private BaseActor smallProximitySensor;

    private int health;
    private BaseActor healthBar;
    private float healthBarMaxWidth;

    public Enemy(float x, float y, Stage s) {
        super(x, y, s);
        maximumDistanceFromSubmarineBeforeDeletion = getStage().getWidth() * 2;
        loadAnimationFromSheet("enemy/enemy.png", 1, 5, 0.05f, true); // TODO use assetmanager
        setSize(75, 75);
        setOrigin(getWidth() / 2f, getHeight() / 2f);
        setBoundaryPolygon(10);
        idlingPosition = new Vector2(x + getOriginX(), y + getOriginY());
        enemyLastSeenPosition = null;
        projectileCount = PROJECTILE_COUNT;
        projectileCooldown = 10;
        collisionDamageCooldown = 0;

        health = MAX_HEALTH;

        setAcceleration(100);
        setMaxSpeed(100);
        setDeceleration(100);

        targetPositionQueue = new ArrayDeque<>();

        smallProximitySensor = new BaseActor(0, 0, s);
        smallProximitySensor.setSize(200, 200);
        smallProximitySensor.loadTexture("circle.png"); // TODO use assetmanager
        smallProximitySensor.setColor(Color.GREEN);
        smallProximitySensor.setOpacity(0.5f);
        smallProximitySensor.setVisible(false);
        smallProximitySensor.setBoundaryPolygon(10);
        smallProximitySensor.centerAtPosition(getOriginX(), getOriginY());
        addActor(smallProximitySensor);

        largeProximitySensor = new BaseActor(0, 0, s);
        largeProximitySensor.loadTexture("circle.png"); // TODO use assetmanager
        largeProximitySensor.setSize(300, 300);
        largeProximitySensor.setColor(Color.RED);
        largeProximitySensor.setOpacity(0.5f);
        largeProximitySensor.setVisible(false);
        largeProximitySensor.setBoundaryPolygon(10);
        largeProximitySensor.centerAtPosition(getOriginX(), getOriginY());
        addActor(largeProximitySensor);

        BaseActor healthBarFrame = new BaseActor(0, 0, s);
        healthBarFrame.loadTexture(Assets.instance.getTexture(Assets.BLANK));
        healthBarFrame.setSize(getWidth() * 0.75f, 30);
        healthBarFrame.setColor(Color.WHITE);
        healthBar = new BaseActor(0, 0, s);
        healthBar.loadTexture(Assets.instance.getTexture(Assets.BLANK));
        healthBarMaxWidth = getWidth() * 0.75f - 20;
        healthBar.setSize(healthBarMaxWidth, 20);
        healthBar.setColor(Color.RED);
        healthBarFrame.addActor(healthBar);
        healthBar.setPosition(10, 10);

        addActor(healthBarFrame);
        healthBarFrame.setPosition(0, getHeight());

    }

    @Override
    public void act(float delta) {
        super.act(delta);
        collisionDamageCooldown -= delta;
        projectileCooldown -= delta;



        shootProjectile();
        evasiveActionIfSensorOverlapsEnvironment();

        float angle = 0;
        try {
            angle = targetPositionQueue.removeFirst().angle();
        } catch (Exception e) {
            try {
                angle = enemyLastSeenPosition.cpy().sub(getX() + getOriginX(), getY() + getOriginY()).angle();
            } catch (Exception e2) {
                angle = idlingPosition.cpy().sub(getX() + getOriginX(), getY() + getOriginY()).angle();
            }
        }
        accelerateAtAngle(angle);
        applyPhysics(delta);

        healthBar.setSize(MathUtils.clamp(healthBarMaxWidth * health / MAX_HEALTH, 0, MAX_HEALTH), healthBar.getHeight());
    }

    private void shootProjectile() { // TODO FIX TO SHOOT FROM CENTRE
        if (projectileCooldown <= 0 && projectileCount > 0 && seesSubmarine) {
            Projectile projectile = new Projectile(0, 0, enemyLastSeenPosition.cpy().sub(getX() + getOriginX(), getY() + getOriginY()).angle(), getStage());
            projectileCooldown = PROJECTILE_COOLDOWN;
            projectileCount--;
            projectile.centerAtActor(this);
        }
    }

    private void evasiveActionIfSensorOverlapsEnvironment() {
        seesSubmarine = true;

        Submarine submarine = (Submarine) BaseActor.getList(getStage(), "szm.orde4c.game.entity.submarine.Submarine").get(0);
        Vector2 submarineOrigin = new Vector2(submarine.getX() + submarine.getOriginX(), submarine.getY() + submarine.getOriginY());
        Vector2 origin = new Vector2(getX() + getOriginX(), getY() + getOriginY());

        Polygon submarineProximitySensorPolygon = getLargeProximitySensorBoundaryPolygon();
        Polygon submarineBoundaryPolygon = BaseActor.getList(getStage(), "szm.orde4c.game.entity.submarine.Submarine").get(0).getBoundaryPolygon();
        if (submarineProximitySensorPolygon.getBoundingRectangle().overlaps(submarineBoundaryPolygon.getBoundingRectangle())) {
            Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
            if (Intersector.overlapConvexPolygons(submarineProximitySensorPolygon, submarineBoundaryPolygon, mtv)) {
                targetPositionQueue.add(mtv.normal.scl(mtv.depth));
            }
        }

        int overlapCount = 0;
        Vector2 vectorSum = new Vector2(0, 0);
        List<BaseActor> collisionActors = BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Environment");
        collisionActors.addAll(BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Rock"));
        collisionActors.addAll(BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Vegetation"));
        List<BaseActor> otherFish = BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Vegetation");
        otherFish.remove(this);
        collisionActors.addAll(otherFish);
        for (BaseActor environmentActor : collisionActors) {
            Polygon poly1 = getSmallProximitySensorBoundaryPolygon();
            Polygon poly2 = environmentActor.getBoundaryPolygon();

            seesSubmarine = seesSubmarine && !Intersector.intersectSegmentPolygon(origin, submarineOrigin, poly2);

            if (poly1.getBoundingRectangle().overlaps(poly2.getBoundingRectangle())) {
                Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
                if (Intersector.overlapConvexPolygons(poly1, poly2, mtv)) {
                    vectorSum.add(mtv.normal.scl(mtv.depth));
                    overlapCount++;
                    Vector2 offset = mtv.normal.scl(mtv.depth);
                    moveBy(offset.x, offset.y);
                }
            }
        }
        if (overlapCount > 0) {
            vectorSum.scl(1f / overlapCount);
            targetPositionQueue.addFirst(vectorSum);
        }
        if (seesSubmarine) {
            enemyLastSeenPosition = submarineOrigin;
        }

        if (submarineOrigin.cpy().sub(origin).len() > maximumDistanceFromSubmarineBeforeDeletion) {
            remove();
        }
    }

    private Polygon getSmallProximitySensorBoundaryPolygon() {
        float x = getX() + smallProximitySensor.getX();
        float y = getY() + smallProximitySensor.getY();
        float originX = smallProximitySensor.getOriginX();
        float originY = smallProximitySensor.getOriginY();

        Polygon proximitySensorPolygon = smallProximitySensor.getBoundaryPolygon();
        proximitySensorPolygon.setPosition(x, y);
        proximitySensorPolygon.setOrigin(originX, originY);
        proximitySensorPolygon.setRotation(smallProximitySensor.getRotation());
        return proximitySensorPolygon;
    }

    private Polygon getLargeProximitySensorBoundaryPolygon() {
        float x = getX() + largeProximitySensor.getX();
        float y = getY() + largeProximitySensor.getY();
        float originX = largeProximitySensor.getOriginX();
        float originY = largeProximitySensor.getOriginY();

        Polygon proximitySensorPolygon = largeProximitySensor.getBoundaryPolygon();
        proximitySensorPolygon.setPosition(x, y);
        proximitySensorPolygon.setOrigin(originX, originY);
        proximitySensorPolygon.setRotation(largeProximitySensor.getRotation());
        return proximitySensorPolygon;
    }

    @Override
    public void damage(float damage) {
        health -= damage;
    }

    @Override
    public boolean isDestroyed() {
        return health <= 0;
    }
}
