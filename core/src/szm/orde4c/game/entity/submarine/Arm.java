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
import szm.orde4c.game.entity.Fish;
import szm.orde4c.game.ui.Direction;
import szm.orde4c.game.util.ArmType;
import szm.orde4c.game.util.CustomActions;

public class Arm extends BaseActor {
    private float length = 300;
    private float girth = 60;
    private Direction direction;

    private float extensionPercent;
    private final float minExtensionPercent = 0.2f;
    private final float maxExtensionPercent = 0.9f;
    private float extensionAmount;
    private float extensionRate;

    private Station station;

    private ArmTool tool;
    private ArmType armType;

    public Arm(float x, float y, Direction direction, Stage s) {
        super(x, y, s);
        this.direction = direction;
        this.armType = ArmType.DRILL;
        extensionPercent = 0.5f;
        extensionRate = 5;
        loadTexture("platform.png");
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
        setRotationSpeed(40);

        tool = new ArmTool(this);
        retract();
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
            tool.setColor(Color.GREEN);
        } else if (ArmType.CUTTER.equals(armType)) {
            tool.setColor(Color.YELLOW);
        } else {
            tool.setColor(Color.RED);
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

        getParent().moveBy(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth);

        return mtv.normal;
    }

    @Override
    public boolean overlaps(BaseActor other) {
        if ((ArmType.DRILL.equals(armType) && station.isActivated() && (other instanceof Drillable || other instanceof Fish)) || (ArmType.CUTTER.equals(armType) && (other instanceof Cuttable || other instanceof Fish))) {
            if (Intersector.overlapConvexPolygons(getTransformedToolBoundaryPolygon(), other.getBoundaryPolygon())) {
                if (other instanceof Damageable) {
                    ((Damageable) other).damage(20);
                }
            }

        }
        // TODO


        /*if ((armType.equals(ArmType.DRILL) && station.isActivated() && other instanceof Drillable) || (armType.equals(ArmType.CUTTER) && other instanceof Cuttable)) {
            return Intersector.overlapConvexPolygons(getTransformedToolBoundaryPolygon(), other.getBoundaryPolygon());
        }*/
        return false;
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
}
