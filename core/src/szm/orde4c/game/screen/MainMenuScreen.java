package szm.orde4c.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.base.BaseGamepadScreen;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.ui.MenuLabel;

import java.util.ArrayList;

public class MainMenuScreen extends BaseGamepadScreen {
    private ArrayList<MenuLabel> menuOptions;
    private int highlightIndex = 0;
    private final float OPTION_SWITCH_TIME_LIMIT = 0.5f;
    private float lastOptionSwitch = OPTION_SWITCH_TIME_LIMIT;
    private final float CONTROLLER_DEADZONE = 0.2f;

    @Override
    public void initialize() {
        menuOptions = new ArrayList<>();
        MenuLabel newGame = new MenuLabel("New Game", BaseGame.largeLabelStyle) {
            @Override
            public void execute() {
                BaseGame.setActiveScreen(new PlayerSelectionScreen());
            }
        };
        newGame.setFontScale(0.5f);
        menuOptions.add(newGame);
        MenuLabel exit = new MenuLabel("Exit", BaseGame.largeLabelStyle) {
            @Override
            public void execute() {
                Gdx.app.exit();
            }
        };
        exit.setFontScale(0.5f);
        menuOptions.add(exit);

        uiTable.pad(30);

        for (Label option : menuOptions) {
            uiTable.add(option).left();
            uiTable.add().expandX();
            uiTable.row();
        }

        highlight();
    }

    @Override
    public void update(float dt) {
        lastOptionSwitch += dt;
        if (lastOptionSwitch > OPTION_SWITCH_TIME_LIMIT) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                switchOption(-1);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                switchOption(1);
            }

            try {
                Controller controller = Controllers.getControllers().first();
                float yAxis = controller.getAxis(XBoxGamepad.AXIS_LEFT_Y);
                if (Math.abs(yAxis) > CONTROLLER_DEADZONE) {
                    switchOption(yAxis);
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
        lastOptionSwitch = 0;

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
        if (keycode == Input.Keys.ESCAPE) {
            Gdx.app.exit();
        }
        if (keycode == Input.Keys.ENTER) {
            executeSelectedMenuOption();
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
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        if (controller.equals(Controllers.getControllers().first())) {
            if (value.equals(XBoxGamepad.DPAD_UP)) {
                switchOption(-1);
            }
            if (value.equals(XBoxGamepad.DPAD_DOWN)) {
                switchOption(1);
            }
        }
        return true;
    }
}
