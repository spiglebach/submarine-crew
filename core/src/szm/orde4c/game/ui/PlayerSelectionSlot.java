package szm.orde4c.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Queue;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.ui.ColorSelectorArrow;
import szm.orde4c.game.util.Assets;
import szm.orde4c.game.util.ControlType;
import szm.orde4c.game.util.PlayerInfo;

public class PlayerSelectionSlot extends BaseActor implements ControllerListener, InputProcessor {
    private static final float COLOR_SWITCH_TIME_LIMIT = 0.5f;
    private static final float CONTROLLER_DEADZONE = 0.2f;
    private static final Color[] COLORS = new Color[]{Color.FIREBRICK, Color.NAVY, Color.ORANGE, Color.CYAN, Color.FOREST, Color.PINK};
    private static final Color EMPTY_COLOR = Color.TAN;
    private static final Color OCCUPIED_COLOR = Color.WHITE;

    private static Queue<Color> availableColors;
    private Color color;

    private boolean occupied;
    private boolean lockedIn;

    private ControlType controlType;
    private Controller assignedController;

    private BaseActor frame;
    private BaseActor playerDisplay;
    private ColorSelectorArrow leftArrow;
    private ColorSelectorArrow rightArrow;

    float lastMove = COLOR_SWITCH_TIME_LIMIT;

    public PlayerSelectionSlot(Stage stage) {
        super(0, 0, stage);
        availableColors = new Queue<>();
        for (Color c : COLORS) {
            availableColors.addLast(c);
        }

        occupied = false;

        frame = new BaseActor(0, 0, stage);
        frame.loadTexture(Assets.instance.getTexture(Assets.BLANK));
        frame.setSize(stage.getWidth() * 0.2f, stage.getHeight() * 0.5f);
        frame.setColor(EMPTY_COLOR);
        addActor(frame);
        setSize(frame.getWidth(), frame.getHeight());


        playerDisplay = new BaseActor(0, 0, stage);
        playerDisplay.loadAnimationFromSheet("player/slotplayer.png", 1, 8, 0.2f, true); // TODO use assetmanager
        playerDisplay.setSize(frame.getWidth(), frame.getHeight());
        playerDisplay.setVisible(false);
        frame.addActor(playerDisplay);

        final float arrowWidth = 25;
        final float arrowHeight = 40;

        leftArrow = new ColorSelectorArrow(0, frame.getHeight() / 2f, arrowWidth, arrowHeight, true, stage);
        leftArrow.setVisible(false);
        leftArrow.setColor(Color.BLACK);
        frame.addActor(leftArrow);

        rightArrow = new ColorSelectorArrow(frame.getWidth() - arrowWidth, frame.getHeight() / 2f, arrowWidth, arrowHeight, false, stage);
        rightArrow.setVisible(false);
        rightArrow.setColor(Color.BLACK);
        frame.addActor(rightArrow);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        lastMove += delta;

        if (!lockedIn && assignedController != null && lastMove > COLOR_SWITCH_TIME_LIMIT) {
            float xAxis = assignedController.getAxis(XBoxGamepad.AXIS_LEFT_X);

            if (Math.abs(xAxis) > CONTROLLER_DEADZONE) {
                if (xAxis > 0) {
                    switchColorRight();
                } else {
                    switchColorLeft();
                }
            }
        }
    }

    public void playerJoined(ControlType controlType, Controller controller) {
        this.assignedController = controller;
        this.controlType = controlType;
        occupied = true;

        rightArrow.setVisible(true);
        leftArrow.setVisible(true);
        playerDisplay.setVisible(true);

        color = availableColors.removeFirst();
        playerDisplay.setColor(color);

        frame.clearActions();
        frame.addAction(Actions.color(OCCUPIED_COLOR, 2f));

        if (controlType.equals(ControlType.KEYBOARD)) {
            InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
            im.addProcessor(0, this);
        } else {
            assignedController.addListener(this);
        }
    }

    public void playerLeft() {
        if (controlType.equals(ControlType.KEYBOARD)) {
            InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
            im.removeProcessor(this);
        } else {
            assignedController.removeListener(this);
            assignedController = null;
        }
        controlType = null;
        occupied = false;

        rightArrow.setVisible(false);
        leftArrow.setVisible(false);
        playerDisplay.setVisible(false);

        availableColors.addLast(color);

        frame.clearActions();
        frame.addAction(Actions.color(EMPTY_COLOR, 2f));
    }

    public void playerLockedIn() {
        lockedIn = true;
        rightArrow.setVisible(false);
        leftArrow.setVisible(false);
    }

    public void playerCancelled() {
        lockedIn = false;
        rightArrow.setVisible(true);
        leftArrow.setVisible(true);
    }

    public void switchColorRight() {
        lastMove = 0;
        availableColors.addLast(color);
        color = availableColors.removeFirst();
        playerDisplay.setColor(color);

        playerDisplay.resetAnimation();
        rightArrow.click();
    }

    public void switchColorLeft() {
        lastMove = 0;
        availableColors.addFirst(color);
        color = availableColors.removeLast();
        playerDisplay.setColor(color);

        playerDisplay.resetAnimation();
        leftArrow.click();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (lockedIn) {
            if (keycode == Input.Keys.F) {
                playerCancelled();
            }
        } else {
            if (keycode == Input.Keys.D) {
                switchColorRight();
            }
            if (keycode == Input.Keys.A) {
                switchColorLeft();
            }
            if (keycode == Input.Keys.F) {
                playerLeft();
            }
            if (keycode == Input.Keys.E) {
                playerLockedIn();
            }
        }
        return true;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (lockedIn) {
            if (buttonCode == XBoxGamepad.BUTTON_B) {
                playerCancelled();
            }
        } else {
            if (buttonCode == XBoxGamepad.BUTTON_B) {
                playerLeft();
            }
            if (buttonCode == XBoxGamepad.BUTTON_X) {
                playerLockedIn();
            }
        }
        return true;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        if (!lockedIn && value.equals(XBoxGamepad.DPAD_RIGHT)) {
            switchColorRight();
        }
        if (!lockedIn && value.equals(XBoxGamepad.DPAD_LEFT)) {
            switchColorLeft();
        }
        return true;
    }

    @Override
    public void disconnected(Controller controller) {
        controller.removeListener(this);
        playerLeft();
    }

    public PlayerInfo getPlayerInfo() {
        return new PlayerInfo(color, controlType, assignedController);
    }

    public boolean isOccupied() {
        return occupied;
    }

    public boolean isLockedIn() {
        return lockedIn;
    }

    public Controller getAssignedController() {
        return assignedController;
    }

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
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
}
