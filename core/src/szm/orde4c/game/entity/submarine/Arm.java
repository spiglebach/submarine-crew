package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.entity.Cuttable;
import szm.orde4c.game.entity.Damageable;
import szm.orde4c.game.entity.Drillable;
import szm.orde4c.game.entity.Enemy;
import szm.orde4c.game.util.Direction;
import szm.orde4c.game.util.ArmType;
import szm.orde4c.game.util.Assets;
import szm.orde4c.game.util.CustomActions;

import java.util.ArrayList;

public class Arm extends BaseActor {
    public static final float DRILLING_ENERY_COST = 1;
    private float length = 120;
    private float girth = 30;
    private Direction direction;

    private float extensionPercent;
    private final float minExtensionPercent = 0.05f;
    private final float maxExtensionPercent = 0.9f;
    private float extensionAmount;
    private float extensionRate;

    private Station station;

    private ArmTool tool;
    private ArmType armType;

    private Polygon sensorPolygon;
    private ArrayList<BaseActor> objectsInRange;

    public Arm(float x, float y, Direction direction, Stage s) {
        super(x, y, s);
        this.direction = direction;
        this.armType = ArmType.DRILL;
        extensionPercent = 0.5f;
        extensionRate = 5;
        loadTexture(Assets.instance.getTexture(Assets.BLANK));
        setSize(length, girth);
        setPosition(x, y - girth / 2.0f);
        setOrigin(0, girth / 2f);

        float rotation = 0;

        if (direction.equals(Direction.EAST)) {
            // rotation = 0;
        } else if (direction.equals(Direction.NORTHEAST)) {
            rotation = 45;
        } else if (direction.equals(Direction.NORTH)) {
            rotation = 90;
        } else if (direction.equals(Direction.NORTHWEST)) {
            rotation = 135;
        } else if (direction.equals(Direction.WEST)) {
            rotation = 180;
        } else if (direction.equals(Direction.SOUTHWEST)) {
            rotation = 225;
        } else if (direction.equals(Direction.SOUTH)) {
            rotation = 270;
        } else if (direction.equals(Direction.SOUTHEAST)) {
            rotation = 315;
        }
        setInitialRotation(rotation);
        setRotation(rotation);
        setBoundaryRectangle();

        setRotationLimit(30);
        setRotationSpeed(60);

        tool = new ArmTool(this);
        retract();

        float[] vertices = new float[8];
        Vector2 sensorPerimeterVector = new Vector2(length * minExtensionPercent, 0);
        sensorPerimeterVector.rotate(rotationLimit);
        vertices[0] = sensorPerimeterVector.x;
        vertices[1] = sensorPerimeterVector.y;
        sensorPerimeterVector.rotate(-rotationLimit * 2);
        vertices[2] = sensorPerimeterVector.x;
        vertices[3] = sensorPerimeterVector.y;
        sensorPerimeterVector.set(length * maxExtensionPercent, 0);
        sensorPerimeterVector.rotate(-rotationLimit);
        vertices[4] = sensorPerimeterVector.x;
        vertices[5] = sensorPerimeterVector.y;
        sensorPerimeterVector.rotate(rotationLimit * 2);
        vertices[6] = sensorPerimeterVector.x;
        vertices[7] = sensorPerimeterVector.y;
        sensorPolygon = new Polygon(vertices);

        objectsInRange = new ArrayList<>();
    }

    public void operate(Controller controller) {
        float xAxis = 0;
        float yAxis = 0;
        if (controller == null) {
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                xAxis = 1;
            } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                xAxis = -1;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                yAxis = 1;
            } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                yAxis = -1;
            }
        } else {
            xAxis = controller.getAxis(XBoxGamepad.AXIS_LEFT_X);
            yAxis = -controller.getAxis(XBoxGamepad.AXIS_LEFT_Y);
        }

        Vector2 controlVector = new Vector2(xAxis, yAxis);
        controlVector.rotate(-getInitialRotation());

        float rotationControlAmount = Math.abs(controlVector.y);
        float extensionControlAmount = Math.abs(controlVector.x);
        float deadZone = 0.1f;

        if (rotationControlAmount > deadZone) {
            if (controlVector.y > 0) {
                rotate(1);
            } else {
                rotate(-1);
            }
        }

        if (extensionControlAmount > deadZone) {
            extendInDirection(controlVector.x);
        }
    }

    public void extend() {
        addAction(CustomActions.extendToExtensionPercent(this, maxExtensionPercent));
    }


    public void retract() {
        addAction(CustomActions.extendToExtensionPercent(this, minExtensionPercent));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        applyRotation(delta);
        applyExtension(delta);
        if (ArmType.DRILL.equals(armType) && station.isActivated()) {
            tool.switchToDrill();
            tool.setAnimationPaused(false);
        } else if (ArmType.CUTTER.equals(armType)) {
            tool.switchToCutter();

        } else {
            tool.switchToDrill();
            tool.setAnimationPaused(true);
        }
    }

    private void applyExtension(float delta) {
        extensionPercent += extensionAmount * extensionRate * delta;
        extensionPercent = MathUtils.clamp(extensionPercent, minExtensionPercent, maxExtensionPercent);
        setSize(length * extensionPercent, girth);
        setBoundaryRectangle();
        tool.setPosition(length * extensionPercent, tool.getY());
        extensionAmount = 0;
    }

    @Override
    public Vector2 preventOverlap(BaseActor other) {
        Polygon boundaryPolygon;
        if ((armType.equals(ArmType.DRILL) && other instanceof Drillable && station.isActivated())
                || (armType.equals(ArmType.CUTTER) && other instanceof Cuttable)) {
            boundaryPolygon = getTransformedArmBoundaryPolygon();
        } else {
            boundaryPolygon = getTransformedTotalPolygon();
        }

        Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
        if (!Intersector.overlapConvexPolygons(boundaryPolygon, other.getBoundaryPolygon(), mtv)) {
            return null;
        }
        if (isBeingExtended() || isBeingRotated()) {
            if (isBeingExtended()) {
                stopExtension();
            }
            if (isBeingRotated()) {
                stopRotation();
            }
        } else {
            getParent().moveBy(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth);
        }
        return mtv.normal;
    }

    @Override
    public boolean overlaps(BaseActor other) {
        if ((ArmType.DRILL.equals(armType) && station.isActivated() && (other instanceof Drillable || other instanceof Enemy)) || (ArmType.CUTTER.equals(armType) && (other instanceof Cuttable || other instanceof Enemy))) {
            if (Intersector.overlapConvexPolygons(getTransformedToolBoundaryPolygon(), other.getBoundaryPolygon())) {
                if (other instanceof Damageable) {
                    ((Damageable) other).damage(20);
                }
            }
        }
        return false;
    }

    private Polygon getSensorPolygon() {
        Vector2 parentPosition = ((BaseActor) getParent()).getPosition();
        Vector2 parentOrigin = new Vector2(getParent().getOriginX(), getParent().getOriginY());
        Vector2 armPosition = getPosition();

        Vector2 parentOriginToPosition = armPosition.cpy().sub(parentOrigin);
        parentOriginToPosition.rotate(getParent().getRotation());

        Vector2 totalPosition = parentPosition.cpy().add(parentOrigin).add(parentOriginToPosition);

        sensorPolygon.setPosition(totalPosition.x, totalPosition.y);
        sensorPolygon.setRotation(getParent().getRotation() + initialRotation);
        sensorPolygon.setScale(1.2f, 1.2f);

        return sensorPolygon;
    }

    private Polygon getTransformedTotalPolygon() {
        Polygon totalPolygon = new Polygon(new float[]{0, 0, length * extensionPercent + girth, 0, length * extensionPercent + girth, girth, 0, girth});

        Vector2 transformedBoundaryPolygonPosition;
        Vector2 parentPos = new Vector2(getParent().getX(), getParent().getY());
        Vector2 parentOrigin = new Vector2(getParent().getOriginX(), getParent().getOriginY());
        Vector2 position = new Vector2(getX(), getY());

        Vector2 parentOriginToPosition = (position.cpy().sub(parentOrigin)).rotate(getParent().getRotation());

        transformedBoundaryPolygonPosition = parentPos.cpy();
        transformedBoundaryPolygonPosition = transformedBoundaryPolygonPosition.add(parentOrigin);
        transformedBoundaryPolygonPosition = transformedBoundaryPolygonPosition.add(parentOriginToPosition);

        totalPolygon.setPosition(0, 0);
        totalPolygon.setOrigin(0, girth / 2.0f);
        totalPolygon.setRotation(getParent().getRotation() + getRotation());
        totalPolygon.setPosition(transformedBoundaryPolygonPosition.x, transformedBoundaryPolygonPosition.y);

        return totalPolygon;
    }

    private Polygon getTransformedArmBoundaryPolygon() {
        Polygon armPolygon = getBoundaryPolygon();

        Vector2 transformedBoundaryPolygonPosition;
        Vector2 parentPos = new Vector2(getParent().getX(), getParent().getY());
        Vector2 parentOrigin = new Vector2(getParent().getOriginX(), getParent().getOriginY());
        Vector2 position = new Vector2(getX(), getY());

        Vector2 parentOriginToPosition = (position.cpy().sub(parentOrigin)).rotate(getParent().getRotation());

        transformedBoundaryPolygonPosition = parentPos.cpy();
        transformedBoundaryPolygonPosition = transformedBoundaryPolygonPosition.add(parentOrigin);
        transformedBoundaryPolygonPosition = transformedBoundaryPolygonPosition.add(parentOriginToPosition);

        armPolygon.setPosition(0, 0);
        armPolygon.setOrigin(0, girth / 2.0f);
        armPolygon.setRotation(getParent().getRotation() + getRotation());
        armPolygon.setPosition(transformedBoundaryPolygonPosition.x, transformedBoundaryPolygonPosition.y);

        return armPolygon;
    }

    private Polygon getTransformedToolBoundaryPolygon() {
        Polygon armPolygon = getTransformedArmBoundaryPolygon();
        Vector2 transformedBoundaryPolygonPosition = new Vector2(armPolygon.getX(), armPolygon.getY());
        Polygon headPolygon = tool.getBoundaryPolygon();
        transformedBoundaryPolygonPosition.add((new Vector2(length * extensionPercent, 0)).rotate(getParent().getRotation() + getRotation()));

        headPolygon.setPosition(length, 0);
        headPolygon.setOrigin(0, girth / 2.0f);
        headPolygon.setRotation(getParent().getRotation() + getRotation());
        headPolygon.setPosition(transformedBoundaryPolygonPosition.x, transformedBoundaryPolygonPosition.y);

        return headPolygon;
    }

    public void extendInDirection(float direction) {
        extensionAmount = direction;
    }

    public ArmType getType() {
        return armType;
    }

    public float getExtensionPercent() {
        return extensionPercent;
    }

    public float getMinExtensionPercent() {
        return minExtensionPercent;
    }

    public float getMaxExtensionPercent() {
        return maxExtensionPercent;
    }

    public void switchTool() {
        armType = ArmType.DRILL.equals(armType) ? ArmType.CUTTER : ArmType.DRILL;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    private boolean isBeingExtended() {
        return extensionAmount > 0;
    }

    private void stopExtension() {
        extensionAmount = 0;
    }

    private boolean isBeingRotated() {
        return rotationDirection != 0;
    }

    private void stopRotation() {
        rotationDirection = 0;
    }

    private boolean sensorPolygonOverlaps(BaseActor other) {
        Polygon poly1 = getSensorPolygon();
        Polygon poly2 = other.getBoundaryPolygon();
        if (!poly1.getBoundingRectangle().overlaps(poly2.getBoundingRectangle())) {
            return false;
        }
        return Intersector.overlapConvexPolygons(poly1, poly2);
    }

    public void processObjectWithSensorPolygon(BaseActor environmentActor) {
        if (station.getOperatingPlayer() != null) {
            return;
        }
        if (sensorPolygonOverlaps(environmentActor)) {
            objectsInRange.add(environmentActor);
        }
    }

    public void makeAutonomousAction() {
        if (station.getOperatingPlayer() != null) {
            return;
        }
        float extensionDirection = 0;
        int rotationDirection = 0;
        Vector2 parentPosition = ((BaseActor) getParent()).getPosition();
        Vector2 parentOrigin = new Vector2(getParent().getOriginX(), getParent().getOriginY());
        Vector2 armPosition = getPosition();
        Vector2 parentOriginToPosition = armPosition.cpy().sub(parentOrigin);
        parentOriginToPosition.rotate(getParent().getRotation());

        Vector2 armStart = parentPosition.cpy().add(parentOrigin).add(parentOriginToPosition);

        float[] extensionPercents = new float[]{
                minExtensionPercent,
                (minExtensionPercent + extensionPercent) * 0.5f,
                extensionPercent,
                (extensionPercent + maxExtensionPercent) * 0.5f,
                maxExtensionPercent};
        float[] rotations = new float[]{
                getRotation(),
                (initialRotation - rotationLimit + getRotation()) * 0.5f,
                (initialRotation + rotationLimit - getRotation()) * 0.5f,
                initialRotation - rotationLimit,
                initialRotation + rotationLimit
        };
        for (float expectedExtension : extensionPercents) {
            for (float expectedRotation : rotations) {
                for (BaseActor object : objectsInRange) {
                    Polygon otherPolygon = object.getBoundaryPolygon();
                    Vector2 armEnd = new Vector2(length * expectedExtension + girth, 0);
                    Vector2 armBodyEnd = new Vector2(length * expectedExtension, 0);
                    armEnd.rotate(expectedRotation);
                    armBodyEnd.rotate(expectedRotation);
                    armEnd.add(armStart);
                    armBodyEnd.add(armStart);
                    if (!(object instanceof Damageable)) {
                        continue;
                    }
                    if ((object instanceof Drillable && !ArmType.DRILL.equals(armType)) || object instanceof Cuttable && !ArmType.CUTTER.equals(armType)) {
                        switchTool();
                    }
                    if (Intersector.intersectSegmentPolygon(armBodyEnd, armEnd, otherPolygon)) {
                        if (expectedExtension > extensionPercent) {
                            extensionDirection = 1;
                        } else if (expectedExtension < extensionPercent) {
                            extensionDirection = -1;
                        }
                        if (expectedRotation > getRotation()) {
                            rotationDirection = 1;
                        } else if (expectedRotation < getRotation()) {
                            rotationDirection = -1;
                        }
                        break;
                    }
                }
            }
        }
        station.activated = objectsInRange.size() > 0;
        if (!station.activated) {
            retract();
        }
        extendInDirection(extensionDirection);
        rotate(rotationDirection);
        station.activated = extensionDirection != 0 || rotationDirection != 0;
        objectsInRange.clear();
    }
}
