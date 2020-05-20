package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.util.ControlType;
import szm.orde4c.game.util.PlayerInfo;

public class Player extends BaseActor implements ControllerListener, InputProcessor {
    private static final float ELEVATOR_CONTROLLER_DEADZONE = 0.6f;
    private static final float PLAYER_MOVEMENT_CONTROLLER_DEADZONE = 0.5f;
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
    private Submarine submarine;

    public Player(float x, float y, PlayerInfo info, Submarine submarine, Stage s) {
        super(x, y, s);
        this.submarine = submarine;
        animationMoving = loadAnimationFromSheet("player/player.png", 1, 4, 0.1f, true);
        setSize(18, 23);
        setBoundaryRectangle();

        maxHorizontalSpeed = 300;
        maxVerticalSpeed = 500;
        walkAcceleration = 300;
        walkDeceleration = 70000;
        gravity = 700;
        jumpSpeed = 450;
        climbing = false;
        station = null;

        keyboardPlayer = ControlType.KEYBOARD.equals(info.getControlType());
        setColor(info.getColor());

        belowSensor = new BaseActor(0, 0, s);
        belowSensor.setSize(this.getWidth() - 4, 8);
        belowSensor.setBoundaryRectangle();
        belowSensor.setVisible(false);
        addActor(belowSensor);
        belowSensor.setPosition(getWidth() / 2.0f - belowSensor.getWidth() / 2.0f, -belowSensor.getHeight() - 4);
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
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                yAxis = 1.0f;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                yAxis = -1.0f;
            }
        }
        Vector2 direction = new Vector2(xAxis, yAxis);

        if (climbing) {
            if (Math.abs(direction.y) > PLAYER_MOVEMENT_CONTROLLER_DEADZONE) {
                accelerationVector.add(0, walkAcceleration * direction.y);
            }
        } else {
            accelerationVector.add(0, -gravity);
            if (Math.abs(direction.x) > PLAYER_MOVEMENT_CONTROLLER_DEADZONE) {
                accelerationVector.add(walkAcceleration * direction.x, 0);
                moved = true;
            }
        }

        accelerationVector.rotate(-getParent().getRotation());

        velocityVector.add(accelerationVector.x * delta, accelerationVector.y * delta);

        if (!moved) {
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

        if (yAxis > 0 && Math.abs(yAxis) > PLAYER_MOVEMENT_CONTROLLER_DEADZONE) {
            climbUp();
        }
        if (yAxis < 0 && Math.abs(yAxis) > PLAYER_MOVEMENT_CONTROLLER_DEADZONE) {
            climbDown();
        }
        if (climbing && !Gdx.input.isKeyPressed(Input.Keys.W) && !Gdx.input.isKeyPressed(Input.Keys.S) && Math.abs(direction.y) < PLAYER_MOVEMENT_CONTROLLER_DEADZONE) {
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

    public void setStation(Station station) {
        this.station = station;
    }

    public boolean isOperatingStation() {
        return station != null;
    }

    public Controller getController() {
        return controller;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            return false;
        }
        if (isOperatingStation()) {
            if (keycode == Input.Keys.F) {
                station.setOperatingPlayer(null);
                station = null;
            } else {
                station.keyDown(keycode);
            }
            return true;
        }
        if (climbing) {
            if (keycode == Input.Keys.F || keycode == Input.Keys.A || keycode == Input.Keys.D) {
                stopClimbing();
            }
            return true;
        }
        if (keycode == Input.Keys.W) {
            if (!submarine.useElevatorUp(this)) {
                climbUp();
            }
            return true;
        }
        if (keycode == Input.Keys.S) {
            if (!submarine.useElevatorDown(this)) {
                climbDown();
            }
            return true;
        }
        if (keycode == Input.Keys.SPACE) {
            jump();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (isOperatingStation()) {
            if (buttonCode == XBoxGamepad.BUTTON_B) {
                station.setOperatingPlayer(null);
                station = null;
            } else {
                station.buttonPressed(buttonCode);
            }
            return true;
        }
        if (climbing) {
            if (buttonCode == XBoxGamepad.BUTTON_B) {
                stopClimbing();
            }
            return true;
        }
        if (buttonCode == XBoxGamepad.BUTTON_A) {
            jump();
            return true;
        }
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        if (isOperatingStation()) {
            station.axisMoved(axisCode, value);
            return true;
        }
        if (axisCode == XBoxGamepad.AXIS_LEFT_Y) {
            if (Math.abs(value) > ELEVATOR_CONTROLLER_DEADZONE) {
                if (-value > 0) {
                    if (submarine.useElevatorUp(this)) {
                        return true;
                    }
                } else {
                    if (submarine.useElevatorDown(this)) {
                        return true;
                    }
                }
            }
            if (Math.abs(value) > PLAYER_MOVEMENT_CONTROLLER_DEADZONE) {
                if (-value > 0) {
                    climbUp();
                } else {
                    climbDown();
                }
            }
        }
        if (axisCode == XBoxGamepad.AXIS_LEFT_X && Math.abs(value) > PLAYER_MOVEMENT_CONTROLLER_DEADZONE) {
            stopClimbing();
        }
        return false;
    }

    private void climb(int direction) {
        if (!climbing) {
            for (BaseActor ladderActor : BaseActor.getList(getParent(), "szm.orde4c.game.entity.submarine.Ladder")) {
                if (overlaps(ladderActor) || belowOverlaps(ladderActor)) {
                    if (direction > 0) {
                        climbing = getY() < ladderActor.getY() + ladderActor.getHeight() - getHeight() / 2f;
                    } else {
                        climbing = getY() > ladderActor.getY() + getHeight() / 2f;
                    }
                    if (climbing) {
                        stopMovementX();
                        velocityVector.y = MathUtils.clamp(velocityVector.y, 0, maxVerticalSpeed);
                        climbedLadder = (Ladder) ladderActor;
                        boolean above = getY() >= ladderActor.getY() + ladderActor.getHeight() - this.getHeight();
                        if (above) {
                            setPosition(ladderActor.getX() + ladderActor.getWidth() / 2.0f - this.getWidth() / 2.0f, ladderActor.getY() + ladderActor.getHeight() - getHeight());
                        } else {
                            setPosition(ladderActor.getX() + ladderActor.getWidth() / 2.0f - this.getWidth() / 2.0f, this.getY());
                        }
                        break;
                    }
                }
            }
        }
    }

    private void climbUp() {
        climb(1);
    }

    private void climbDown() {
        climb(-1);
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

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return false;
    }
}
