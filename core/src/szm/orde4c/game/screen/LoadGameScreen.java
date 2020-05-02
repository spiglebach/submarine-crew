package szm.orde4c.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.base.BaseGamepadScreen;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.service.SaveGameService;
import szm.orde4c.game.ui.ButtonIndicator;
import szm.orde4c.game.ui.ControlDisplay;
import szm.orde4c.game.ui.TextButtonIndicatorPair;
import szm.orde4c.game.util.Save;

import java.util.ArrayList;
import java.util.List;

public class LoadGameScreen extends BaseGamepadScreen {
    private int highlightIndex = 0;
    private final float OPTION_SWITCH_TIME_LIMIT = 0.5f;
    private float lastOptionSwitch = OPTION_SWITCH_TIME_LIMIT;
    private final float CONTROLLER_DEADZONE = 0.2f;

    private List<BaseActor> saveSlots;
    private List<Save> saves;

    @Override
    public void initialize() {
        saveSlots = new ArrayList<>();
        saves = SaveGameService.getSaves();
        for (int i = 0; i < 4; i++) {
            BaseActor saveSlot = new BaseActor(0, 0, uiStage);
            saveSlot.loadTexture("platform.png");
            saveSlot.setSize(uiStage.getWidth() * 0.4f, uiStage.getHeight() * 0.2f);
            if (saves.get(i) != null) {
                saveSlot.setColor(Color.GREEN);
                saveSlots.add(saveSlot);
            } else {
                saveSlot.setColor(Color.RED);
            }

            uiTable.pad(60);
            uiTable.add(saveSlot).expand();
            uiTable.row();

        }
        uiTable.add(new ControlDisplay(new TextButtonIndicatorPair[]{
                new TextButtonIndicatorPair(ControlDisplay.TEXT_PRESS_TO_LOAD,
                        new int[]{ButtonIndicator.CONTROLLER_FACE_BUTTON_WEST, ButtonIndicator.KEYBOARD_BUTTON_E}),
                new TextButtonIndicatorPair(ControlDisplay.TEXT_PRESS_TO_DELETE,
                        new int[]{ButtonIndicator.CONTROLLER_BUTTON_START, ButtonIndicator.KEYBOARD_BUTTON_DELETE})},
                1, 0.1f, 0.5f, uiStage));
    }

    @Override
    public void update(float delta) {
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

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            Gdx.app.exit();
        }
        if (keycode == Input.Keys.W) {
            switchOptionUp();
            return true;
        }
        if (keycode == Input.Keys.S) {
            switchOptionDown();
            return true;
        }
        if (keycode == Input.Keys.E) {
            loadSelectedSave();
            return true;
        }
        if (keycode == Input.Keys.F) {
            BaseGame.setActiveScreen(new MainMenuScreen());
            return true;
        }
        if (keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
            deleteSelectedSave();
            return true;
        }
        return false;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (buttonCode == XBoxGamepad.BUTTON_X) {
            loadSelectedSave();
            return true;
        }
        if (buttonCode == XBoxGamepad.BUTTON_START) {
            deleteSelectedSave();
            return true;
        }
        if (buttonCode == XBoxGamepad.BUTTON_B) {
            BaseGame.setActiveScreen(new MainMenuScreen());
            return true;
        }
        return false;
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
            highlightIndex = saveSlots.size() - 1;
        } else if (highlightIndex >= saveSlots.size()) {
            highlightIndex = 0;
        }
    }

    private void highlight() {
        saveSlots.get(highlightIndex).setColor(Color.YELLOW);
    }

    private void unHighlight() {
        saveSlots.get(highlightIndex).setColor(Color.WHITE);
    }

    private void loadSelectedSave() {
        // TODO properly
        Save selectedSave = saves.get(highlightIndex);
        BaseGame.setActiveScreen(new LevelSelectorScreen(selectedSave));

    }

    private void deleteSelectedSave() {
        // TODO
    }
}
