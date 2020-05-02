package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.util.ControlType;
import szm.orde4c.game.util.PlayerInfo;

public class Player extends BaseActor {
    private float walkDeceleration;
    private float walkAcceleration;
    private float maxHorizontalSpeed;
    private float maxVerticalSpeed;
    private float gravity;
    private float jumpSpeed;
    private Ladder climbedLadder;
    private BaseActor belowSensor;

    private Animation animationIdle;
    private Animation animationMoving;
    private Animation animationClimbing;
    private Animation animationJumping;
    private Animation animationOperatingStation;

    private Controller controller;

    private boolean climbing;

    private boolean keyboardPlayer;

    private Station station;

    public Player(float x, float y, PlayerInfo info, Stage s) {
        super(x, y, s);
        animationMoving = loadAnimationFromSheet("player/player.png", 1, 4, 0.1f, true);
        setSize(80, 110);
        setBoundaryRectangle();

        maxHorizontalSpeed = 300;
        maxVerticalSpeed = 500;
        walkAcceleration = 300;
        walkDeceleration = 600;
        gravity = 700;
        jumpSpeed = 450;
        climbing = false;
        station = null;

        keyboardPlayer = ControlType.KEYBOARD.equals(info.getControlType());

        if (!keyboardPlayer) {
            controller = info.getAssignedController();
        }
        setColor(info.getColor());


        belowSensor = new BaseActor(0, 0, s);
        belowSensor.loadTexture("platform.png");
        belowSensor.setSize(this.getWidth() - 8, 8);
        belowSensor.setBoundaryRectangle();
        belowSensor.setVisible(true);
        addActor(belowSensor);
        belowSensor.setPosition(getWidth() / 2.0f - belowSensor.getWidth() / 2.0f,- belowSensor.getHeight() - 4);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (station != null) {
            station.operate();
            return;
        }

        boolean moved = false;

        float xAxis = 0;
        float yAxis = 0;

        if (controller != null) {
            xAxis = controller.getAxis(XBoxGamepad.AXIS_LEFT_X);
            yAxis = -controller.getAxis(XBoxGamepad.AXIS_LEFT_Y);
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                xAxis += 1.0f;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                xAxis += -1.0f;
            }
        }

        Vector2 direction = new Vector2(xAxis, yAxis);
        float deadZone = 0.2f;


        if (climbing) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                accelerationVector.add(0, walkAcceleration);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                accelerationVector.add(0, -walkAcceleration);
            }
        } else {
            accelerationVector.add(0, -gravity);
            if (Math.abs(direction.x) > deadZone) {
                accelerationVector.add(walkAcceleration * direction.x, 0);
                moved = true;
            }
        }

        accelerationVector.rotate(-getParent().getRotation());

        velocityVector.add(accelerationVector.x * delta, accelerationVector.y * delta);

        if (!Gdx.input.isKeyPressed(Input.Keys.A) && !Gdx.input.isKeyPressed(Input.Keys.D) && !moved) {
            float decelerationAmount = walkDeceleration * delta;

            float walkDirection;

            if (velocityVector.x > 0) {
                walkDirection = 1;
            } else {
                walkDirection = -1;
            }

            float walkSpeed = Math.abs(velocityVector.x);
            walkSpeed -= decelerationAmount;

            if (walkSpeed < 0) {
                walkSpeed = 0;
            }
            velocityVector.x = walkSpeed * walkDirection;
        }

        // TODO Climbing
        if (climbing && !Gdx.input.isKeyPressed(Input.Keys.W) && !Gdx.input.isKeyPressed(Input.Keys.S)) {
            stopMovementY();
        }

        velocityVector.x = MathUtils.clamp(velocityVector.x, -maxHorizontalSpeed, maxHorizontalSpeed);
        velocityVector.y = MathUtils.clamp(velocityVector.y, -maxVerticalSpeed, maxVerticalSpeed);
        moveBy(velocityVector.x * delta, velocityVector.y * delta);
        accelerationVector.set(0, 0);

        if (climbing) {
            if (getY() <= climbedLadder.getY() || getY() >= climbedLadder.getY() + climbedLadder.getHeight()) {
                stopClimbing();
            }
        }
    }

    public void jump() {
        velocityVector.y = jumpSpeed;
    }

    public boolean isClimbing() {
        return climbing;
    }

    public void setClimbing(boolean climbing) {
        this.climbing = climbing;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public boolean isOperatingStation() {
        return station != null;
    }

    public Controller getController() {
        return controller;
    }

    public void buttonPressed(int buttonCode) {
        if (isOperatingStation()) {
            if (buttonCode == XBoxGamepad.BUTTON_B) {
                station.setOperatingPlayer(null);
                station = null;
            } else {
                station.buttonPressed(buttonCode);
            }
            return;
        }
        if (climbing) {
            if (buttonCode == XBoxGamepad.BUTTON_B) {
                stopClimbing();
            }
            return;
        }
        if (buttonCode == XBoxGamepad.BUTTON_A) {
            jump();
            return;
        }
    }

    public void keyDown(int keycode) {
        if (isOperatingStation()) {
            if (keycode == Input.Keys.F) {
                station.setOperatingPlayer(null);
                station = null;
            } else {
                station.keyDown(keycode);
            }
            return;
        }
        if (climbing) {
            if (keycode == Input.Keys.F || keycode == Input.Keys.A || keycode == Input.Keys.D) {
                stopClimbing();
            }
            return;
        }
        if (keycode == Input.Keys.W || keycode == Input.Keys.S) {
            climb();
        }
        if (keycode == Input.Keys.SPACE) {
            jump();
            return;
        }
    }

    public void axisMoved(int axisCode, float value) {
        if (isOperatingStation()) {
            station.axisMoved(axisCode, value);
        }
    }

    private void climb() {
        if (!climbing) {
            for (BaseActor ladderActor : BaseActor.getList(getParent(), "szm.orde4c.game.entity.submarine.Ladder")) {
                if (overlaps(ladderActor) || belowOverlaps(ladderActor)) {
                    stopMovementX();
                    velocityVector.y = MathUtils.clamp(velocityVector.y, 0, maxVerticalSpeed);
                    climbedLadder = (Ladder) ladderActor;
                    climbing = true;
                    boolean above = getY() >= ladderActor.getY() + ladderActor.getHeight() - this.getHeight();
                    if (above) {
                        setPosition(ladderActor.getX() + ladderActor.getWidth() / 2.0f - this.getWidth() / 2.0f, ladderActor.getY() + ladderActor.getHeight() - getHeight());
                    } else {
                        setPosition(ladderActor.getX() + ladderActor.getWidth() / 2.0f - this.getWidth() / 2.0f, this.getY());
                    }
                }
            }
        }
    }

    private void stopClimbing() {
        climbing = false;
        climbedLadder = null;
    }

    public boolean isKeyboardPlayer() {
        return keyboardPlayer;
    }

    public boolean belowOverlaps(BaseActor other) {
        Polygon belowPolygon = belowSensor.getBoundaryPolygon();
        belowPolygon.setPosition(getX() + belowSensor.getX(), getY() + belowSensor.getY());

        return Intersector.overlapConvexPolygons(belowPolygon, other.getBoundaryPolygon());
    }
}
