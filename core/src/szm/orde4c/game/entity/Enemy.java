package szm.orde4c.game.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.entity.submarine.Submarine;
import szm.orde4c.game.util.Assets;

import java.util.*;

public class Enemy extends BaseActor implements Damageable {
    private float maximumDistanceFromSubmarineBeforeDeletion;
    private final float DAMAGE_COOLDOWN = 0.5f;
    private float damageCooldown;
    private final int MAX_HEALTH = 150;

    private final int PROJECTILE_COUNT = 5;
    private final float PROJECTILE_COOLDOWN = 10;
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
        maximumDistanceFromSubmarineBeforeDeletion = getStage().getWidth() * 3;
        loadAnimationFromSheet(Assets.instance.getTexture(Assets.ENEMY), 1, 5, 0.05f, true);
        setSize(75, 75);
        setOrigin(getWidth() / 2f, getHeight() / 2f);
        setBoundaryPolygon(10);
        idlingPosition = new Vector2(x + getOriginX(), y + getOriginY());
        enemyLastSeenPosition = null;
        projectileCount = PROJECTILE_COUNT;
        projectileCooldown = 10;
        damageCooldown = 0;

        health = MAX_HEALTH;

        setAcceleration(100);
        setMaxSpeed(100);
        setDeceleration(100);

        targetPositionQueue = new ArrayDeque<>();

        smallProximitySensor = new BaseActor(0, 0, s);
        smallProximitySensor.setSize(200, 200);
        smallProximitySensor.setVisible(false);
        smallProximitySensor.setBoundaryPolygon(10);
        smallProximitySensor.centerAtPosition(getOriginX(), getOriginY());
        addActor(smallProximitySensor);

        largeProximitySensor = new BaseActor(0, 0, s);
        largeProximitySensor.setSize(300, 300);
        largeProximitySensor.setVisible(false);
        largeProximitySensor.setBoundaryPolygon(10);
        largeProximitySensor.centerAtPosition(getOriginX(), getOriginY());
        addActor(largeProximitySensor);

        initializeHealthBar();
    }

    private void initializeHealthBar() {
        BaseActor healthBarFrame = new BaseActor(0, 0, getStage());
        healthBarFrame.loadTexture(Assets.instance.getTexture(Assets.BLANK));
        healthBarFrame.setSize(getWidth() * 0.75f, 14);
        healthBarFrame.setColor(Color.WHITE);
        healthBar = new BaseActor(0, 0, getStage());
        healthBar.loadTexture(Assets.instance.getTexture(Assets.BLANK));
        healthBarMaxWidth = getWidth() * 0.75f - 4;
        healthBar.setSize(healthBarMaxWidth, 10);
        healthBar.setColor(Color.RED);
        healthBarFrame.addActor(healthBar);
        healthBar.setPosition(2, 2);

        addActor(healthBarFrame);
        healthBarFrame.setPosition(0, getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getStage() == null) {
            return;
        }
        damageCooldown -= delta;
        projectileCooldown -= delta;

        if (projectileCooldown <= 0 && projectileCount > 0 && seesSubmarine) {
            shootProjectile();
        }

        queueNextAction();

        float angle = getNextMoveAngle();
        accelerateAtAngle(angle);
        applyPhysics(delta);
        updateHealthBar();
        removeIfTooFarFromSubmarine();
    }

    private float getNextMoveAngle() {
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
        return angle;
    }

    private void updateHealthBar() {
        healthBar.setSize(MathUtils.clamp(healthBarMaxWidth * health / MAX_HEALTH, 0, MAX_HEALTH), healthBar.getHeight());
    }

    private void removeIfTooFarFromSubmarine() {
        Submarine submarine = (Submarine) BaseActor.getList(getStage(), "szm.orde4c.game.entity.submarine.Submarine").get(0);
        Vector2 submarineOrigin = new Vector2(submarine.getX() + submarine.getOriginX(), submarine.getY() + submarine.getOriginY());
        Vector2 origin = new Vector2(getX() + getOriginX(), getY() + getOriginY());
        if (submarineOrigin.cpy().sub(origin).len() > maximumDistanceFromSubmarineBeforeDeletion) {
            addAction(Actions.removeActor());
        }
    }

    private void shootProjectile() {
        Projectile projectile = new Projectile(0, 0, enemyLastSeenPosition.cpy().sub(getX() + getOriginX(), getY() + getOriginY()).angle(), getStage());
        projectileCooldown = PROJECTILE_COOLDOWN;
        projectileCount--;
        projectile.setPosition(getX() + getOriginX(), getY() + getOriginY());
    }

    private void queueNextAction() {
        seesSubmarine = true;

        Submarine submarine = (Submarine) BaseActor.getList(getStage(), "szm.orde4c.game.entity.submarine.Submarine").get(0);
        Vector2 submarineOrigin = new Vector2(submarine.getX() + submarine.getOriginX(), submarine.getY() + submarine.getOriginY());
        Vector2 origin = new Vector2(getX() + getOriginX(), getY() + getOriginY());
        setRotation((submarineOrigin.cpy().sub(origin)).angle());

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
        List<BaseActor> damageableActors = BaseActor.getList(getStage(), "szm.orde4c.game.entity.Damageable");
        collisionActors.addAll(damageableActors);
        for (BaseActor environmentActor : collisionActors) {
            if (environmentActor instanceof Submarine || environmentActor.equals(this)) {
                continue;
            }
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
        if (damageCooldown <= 0) {
            health -= damage;
            damageCooldown = DAMAGE_COOLDOWN;
        }
    }

    @Override
    public boolean isDestroyed() {
        return health <= 0;
    }
}
