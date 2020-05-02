package szm.orde4c.game.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.entity.submarine.Submarine;

import java.util.*;

public class Fish extends BaseActor implements Damageable {
    Deque<Vector2> targetPositionQueue;
    BaseActor largeProximitySensor;
    boolean largeSensorOverlapsSubmarine;
    BaseActor smallProximitySensor;
    boolean smallSensorOverlapsEnvironment;
    boolean seesSubmarine;
    int health;
    Vector2 idlingPosition;
    Vector2 enemyLastSeenPosition;

    public Fish(float x, float y, Stage s) {
        super(x, y, s);
        loadTexture("fish.png");
        setSize(200, 200);
        setOrigin(getWidth() / 2f, getHeight() / 2f);
        setBoundaryPolygon(10);
        idlingPosition = new Vector2(x + getOriginX(), y + getOriginY());
        enemyLastSeenPosition = null;

        health = 1000;

        setAcceleration(400);
        setMaxSpeed(400);
        setDeceleration(400);

        targetPositionQueue = new ArrayDeque<>();



        smallProximitySensor = new BaseActor(0, 0, s);
        smallProximitySensor.setSize(600, 600);
        smallProximitySensor.loadTexture("platform.png");
        smallProximitySensor.setColor(Color.GREEN);
        smallProximitySensor.setOpacity(0.5f);
        smallProximitySensor.setBoundaryPolygon(10);
        smallProximitySensor.centerAtPosition(getOriginX(), getOriginY());
        addActor(smallProximitySensor);

        largeProximitySensor = new BaseActor(0, 0, s);
        largeProximitySensor.loadTexture("platform.png");
        largeProximitySensor.setSize(1000, 1000);
        largeProximitySensor.setColor(Color.RED);
        largeProximitySensor.setOpacity(0.5f);
        largeProximitySensor.setBoundaryPolygon(10);
        largeProximitySensor.centerAtPosition(getOriginX(), getOriginY());
        addActor(largeProximitySensor);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        evasiveActionIfSensorOverlapsEnvironment();

//        sense();
//        think();
//        act();


        try {
            accelerateAtAngle(targetPositionQueue.removeFirst().angle());
        } catch (Exception e) {
            try {
                accelerateAtAngle(enemyLastSeenPosition.cpy().sub(getX() + getOriginX(), getY() + getOriginY()).angle());
            } catch (Exception e2) {
                accelerateAtAngle(idlingPosition.cpy().sub(getX() + getOriginX(), getY() + getOriginY()).angle());
            }
        }

        applyPhysics(delta);


    }

    private void sense() {

    }

    private boolean evasiveActionIfSensorOverlapsEnvironment() {
        seesSubmarine = true;

        Submarine submarine = (Submarine) BaseActor.getList(getStage(), "szm.orde4c.game.entity.submarine.Submarine").get(0);
        Vector2 submarineOrigin = new Vector2(submarine.getX() + submarine.getOriginX(), submarine.getY() + submarine.getOriginY());
        Vector2 origin = new Vector2(getX() + getOriginX(), getY() + getOriginY());

        int overlapCount = 0;
        Vector2 vectorSum = new Vector2(0, 0);
        List<BaseActor> environmentActors = BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Environment");
        environmentActors.addAll(BaseActor.getList(getStage(), "szm.orde4c.game.entity.stationary.Area"));
        environmentActors.addAll(BaseActor.getList(getStage(), "szm.orde4c.game.entity.submarine.Submarine"));
        for (BaseActor environmentActor : environmentActors) {
            Polygon poly1;
            if (environmentActor instanceof Submarine) {
                poly1 = getLargeProximitySensorBoundaryPolygon();
            } else {
                poly1 = getSmallProximitySensorBoundaryPolygon();
            }
            Polygon poly2 = environmentActor.getBoundaryPolygon();

            if (seesSubmarine && !(environmentActor instanceof Submarine)) {
                seesSubmarine = !Intersector.intersectSegmentPolygon(origin, submarineOrigin, poly2);
            }

            if (!poly1.getBoundingRectangle().overlaps(poly2.getBoundingRectangle())) {
                continue;
            }

            Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
            if (Intersector.overlapConvexPolygons(poly1, poly2, mtv)) {
                vectorSum.add(mtv.normal.scl(mtv.depth));
                overlapCount++;
            }
        }
        if (overlapCount > 0) {
            vectorSum.scl(1f / overlapCount);
            targetPositionQueue.addFirst(vectorSum);
        }
        if (seesSubmarine) {
            enemyLastSeenPosition = submarineOrigin;
            System.out.println("sees submarine");
        }
        return false;
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
    public void damage(int damage) {
        health -= damage;
    }

    @Override
    public boolean isDestroyed() {
        return health <= 0;
    }

    enum State{
        EVADE,
        INVESTIGATE,
        CHARGE,
        FLEE,
        IDLE
    }
}
