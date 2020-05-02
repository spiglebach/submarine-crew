package szm.orde4c.game.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.XBoxGamepad;

import java.util.List;

public class PauseMenu extends BaseActor implements InputProcessor, ControllerListener {
    private List<MenuLabel> menuOptions;
    private int highlightIndex = 0;
    private final float OPTION_SWITCH_TIME_LIMIT = 0.5f;
    private float lastOptionSwitch = OPTION_SWITCH_TIME_LIMIT;
    private final float CONTROLLER_DEADZONE = 0.2f;
    private Table table;

    public PauseMenu(List<MenuLabel> menuOptions, float opacity, Stage s) {
        super(0, 0, s);
        this.menuOptions = menuOptions;

        loadTexture("platform.png");
        setColor(Color.BLACK);
        setOpacity(opacity);
        setSize(s.getWidth(), s.getHeight());

        table = new Table();
        table.setFillParent(true);
        addActor(table);

        table.pad(30);

        for (MenuLabel option : menuOptions) {
            option.setFontScale(0.5f);
            table.add(option).left();
            table.add().expandX();
            table.row();
        }

        highlight();

    }

    @Override
    public void act(float delta) {
        super.act(delta);
        lastOptionSwitch += delta;
        if (lastOptionSwitch > OPTION_SWITCH_TIME_LIMIT) {
            try {
                Controller controller = Controllers.getControllers().first();
                float yAxis = controller.getAxis(XBoxGamepad.AXIS_LEFT_Y);
                if (Math.abs(yAxis) > CONTROLLER_DEADZONE) {
                    switchOption(yAxis);
                    lastOptionSwitch = 0;
                }
            } catch (Exception e) {
                // No controller attached!
            }
        }
    }

    private void switchOption(float amount) {
        unHighlight();
        highlightIndex += amount > 0 ? 1 : -1;
        wrapIndex();
        highlight();
    }

    private void switchOptionUp() {
        switchOption(-1);
    }

    private void switchOptionDown() {
        switchOption(1);
    }

    private void wrapIndex() {
        if (highlightIndex < 0) {
            highlightIndex = menuOptions.size() - 1;
        } else if (highlightIndex >= menuOptions.size()) {
            highlightIndex = 0;
        }
    }

    private void highlight() {
        menuOptions.get(highlightIndex).setColor(Color.YELLOW);
    }

    private void unHighlight() {
        menuOptions.get(highlightIndex).setColor(Color.WHITE);
    }

    private void executeSelectedMenuOption() {
        menuOptions.get(highlightIndex).execute();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.E || keycode == Input.Keys.ENTER) {
            executeSelectedMenuOption();
        }
        if (keycode == Input.Keys.W) {
            switchOptionUp();
        }
        if (keycode == Input.Keys.S) {
            switchOptionDown();
        }
        return true;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        if (controller.equals(Controllers.getControllers().first())) {
            if (value.equals(XBoxGamepad.DPAD_UP)) {
                switchOptionUp();
            }
            if (value.equals(XBoxGamepad.DPAD_DOWN)) {
                switchOptionDown();
            }
        }
        return true;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (controller.equals(Controllers.getControllers().first())) {
            if (buttonCode == XBoxGamepad.BUTTON_X) {
                executeSelectedMenuOption();
            }
        }
        return true;
    }

    @Override
    public void connected(Controller controller) {
    }

    @Override
    public void disconnected(Controller controller) {
        controller.removeListener(this);
        try {
            Controllers.getControllers().first().addListener(this);
        } catch (Exception e) {
            // No controller attached!
        }
    }

    @Override
    public boolean buttonUp(Controller controller, int i) {
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int i, float v) {
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int i, boolean b) {
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int i, boolean b) {
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int i, Vector3 vector3) {
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
