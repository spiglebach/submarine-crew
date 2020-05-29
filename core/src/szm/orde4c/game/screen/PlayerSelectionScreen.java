package szm.orde4c.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.base.BaseGamepadScreen;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.ui.ButtonIndicator;
import szm.orde4c.game.ui.ControlDisplay;
import szm.orde4c.game.ui.CountdownDisplay;
import szm.orde4c.game.util.TextButtonIndicatorPair;
import szm.orde4c.game.util.ControlType;
import szm.orde4c.game.util.PlayerInfo;
import szm.orde4c.game.ui.PlayerSelectionSlot;
import szm.orde4c.game.util.Save;

import java.util.ArrayList;

public class PlayerSelectionScreen extends BaseGamepadScreen {
    private final int MAX_PLAYER_COUNT = 4;
    private ArrayList<PlayerSelectionSlot> slots;
    private CountdownDisplay countdownDisplay;

    private Save save;
    private int currentLevelIndex;
    private float delay = 0.5f;

    public PlayerSelectionScreen(int currentLevelIndex, Save save) {
        this.currentLevelIndex = currentLevelIndex;
        this.save = save;
    }

    @Override
    public void initialize() {
        initializePlayerSelectionSlots();
        initializeControlDisplay();
        initializeCountdownDisplay();
    }

    @Override
    public void update(float dt) {
        if (delay > 0) {
            delay -= dt;
            return;
        }
        boolean allLockedIn = allOccupiedSlotsLockedIn();
        int occupiedSlotCount = getOccupiedSlotCount();
        if (occupiedSlotCount > 0 && allLockedIn) {
            countdownDisplay.countdown();
        } else {
            countdownDisplay.stop();
        }
    }

    private void initializePlayerSelectionSlots() {
        slots = new ArrayList<>(MAX_PLAYER_COUNT);
        for (int i = 0; i < MAX_PLAYER_COUNT; i++) {
            uiTable.pad(100);
            PlayerSelectionSlot slot = new PlayerSelectionSlot(uiStage);
            uiTable.add(slot).expand().bottom();
            slots.add(slot);
        }
    }

    private void initializeControlDisplay() {
        uiTable.row();
        uiTable.add(new ControlDisplay(
                new TextButtonIndicatorPair[]{
                        new TextButtonIndicatorPair(ControlDisplay.TEXT_PRESS_TO_JOIN_FINALIZE,
                                new int[]{ButtonIndicator.CONTROLLER_FACE_WEST, ButtonIndicator.KEYBOARD_E}),
                        new TextButtonIndicatorPair(ControlDisplay.TEXT_PRESS_TO_SELECT_COLOR,
                                new int[]{ButtonIndicator.CONTROLLER_JOYSTICK_X, ButtonIndicator.KEYBOARD_AD})},
                1f, 0.1f, 0.5f, uiStage)).colspan(4).expandX().top();
        uiTable.row();
        uiTable.add(new ControlDisplay(new TextButtonIndicatorPair[]{
                new TextButtonIndicatorPair(ControlDisplay.TEXT_PRESS_TO_LEAVE_CANCEL,
                        new int[]{ButtonIndicator.CONTROLLER_FACE_EAST, ButtonIndicator.KEYBOARD_F})},
                1f, 0.1f, 0.5f, uiStage)).colspan(4).expand().top();
    }

    private void initializeCountdownDisplay() {
        countdownDisplay = new CountdownDisplay(3, uiStage);
        countdownDisplay.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startLevel();
            }
        });
    }

    private PlayerSelectionSlot getUnoccupiedSlot() {
        for (PlayerSelectionSlot slot : slots) {
            if (!slot.isOccupied()) {
                return slot;
            }
        }
        return null;
    }

    private boolean isControllerAssigned(Controller controller) {
        for (int i = 0; i < slots.size(); i++) {
            if (controller.equals(slots.get(i).getAssignedController())) {
                return true;
            }
        }
        return false;
    }

    private int getOccupiedSlotCount() {
        int occupiedSlotCount = 0;
        for (PlayerSelectionSlot slot : slots) {
            if (slot.isOccupied()) {
                occupiedSlotCount++;
            }
        }
        return occupiedSlotCount;
    }

    private boolean allOccupiedSlotsLockedIn() {
        for (PlayerSelectionSlot slot : slots) {
            if (slot.isOccupied() && !slot.isLockedIn()) {
                return false;
            }
        }
        return true;
    }

    private void returnToLoadGameScreen() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(this);
        for (PlayerSelectionSlot slot : slots) {
            im.removeProcessor(slot);
            if (slot.isOccupied() && slot.getPlayerInfo().getAssignedController() != null) {
                slot.getPlayerInfo().getAssignedController().removeListener(slot);
            }
        }
        BaseGame.setActiveScreen(new LoadGameScreen());
    }

    private void startLevel() {
        int occupiedSlotCount = getOccupiedSlotCount();
        PlayerInfo[] players = new PlayerInfo[occupiedSlotCount];
        int startIndex = 0;
        for (int i = 0; i < occupiedSlotCount; i++) {
            for (int j = startIndex; j < MAX_PLAYER_COUNT; j++) {
                if (slots.get(j).isOccupied()) {
                    PlayerSelectionSlot slot = slots.get(j);
                    players[i] = slot.getPlayerInfo();
                    if (slot.getAssignedController() != null) {
                        slot.getAssignedController().removeListener(slot);
                    }
                    startIndex = j + 1;
                    break;
                }
            }
        }
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.clear();
        BaseGame.setActiveScreen(new LevelScreen(currentLevelIndex, save, players));
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.E) {
            PlayerSelectionSlot unoccupiedSlot = getUnoccupiedSlot();
            if (unoccupiedSlot != null) {
                unoccupiedSlot.playerJoined(ControlType.KEYBOARD, null);
                return true;
            }
        }
        if (keycode == Input.Keys.F) {
            for (PlayerSelectionSlot slot : slots) {
                if (slot.isOccupied() && ControlType.KEYBOARD.equals(slot.getPlayerInfo().getControlType())) {
                    return false;
                }
            }
            returnToLoadGameScreen();
        }
        return false;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (delay > 0) {
            return false;
        }
        if (buttonCode == XBoxGamepad.BUTTON_X) {
            if (!isControllerAssigned(controller)) {
                PlayerSelectionSlot unoccupiedSlot = getUnoccupiedSlot();
                if (unoccupiedSlot != null) {
                    unoccupiedSlot.playerJoined(ControlType.CONTROLLER, controller);
                    return true;
                }
            }
        }
        if (buttonCode == XBoxGamepad.BUTTON_B) {
            if (!isControllerAssigned(controller) && controller.equals(Controllers.getControllers().first())) {
                returnToLoadGameScreen();
            }
        }
        return false;
    }
}
