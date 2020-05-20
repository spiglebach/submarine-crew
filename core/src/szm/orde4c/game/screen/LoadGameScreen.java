package szm.orde4c.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import szm.orde4c.game.base.*;
import szm.orde4c.game.service.SaveGameService;
import szm.orde4c.game.ui.*;
import szm.orde4c.game.util.Assets;
import szm.orde4c.game.util.Save;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadGameScreen extends BaseGamepadScreen {
    private final int LEVEL_COUNT = 5;
    private boolean levelSelectionMode;
    private int highlightIndex;

    private List<SaveSlot> saveSlots;
    private List<Save> saves;
    private float saveSlotWidth;
    private float saveSlotHeight;

    private BaseActor levelSelectorSubScreen;
    private LevelSelectorCursor cursor;
    private HashMap<Integer, LevelStamp> levels;
    private final int MINIMUM_LEVEL_INDEX = 1;
    private int currentLevelIndex;
    private int previousLevelIndex;
    private int nextLevelIndex;

    private final float CONTROLLER_DEADZONE = 0.4f;
    private static final float SELECTION_COOLDOWN = 0.5f;
    private float lastSelection = SELECTION_COOLDOWN;

    @Override
    public void initialize() {
        levelSelectionMode = false;

        saveSlotWidth = uiStage.getWidth() * 0.3f;
        saveSlotHeight = uiStage.getHeight() * 0.15f;

        initializeSaveSlots();
        initializeLevelSelector();
        initializeControlDisplay();
        initializeCursorPosition();
        highlightYellow();
    }

    private void initializeControlDisplay() {
        uiTable.row();
        uiTable.add(new ControlDisplay(new TextButtonIndicatorPair[]{
                new TextButtonIndicatorPair(ControlDisplay.TEXT_PRESS_TO_LOAD,
                        new int[]{ButtonIndicator.CONTROLLER_FACE_WEST, ButtonIndicator.KEYBOARD_E}),
                new TextButtonIndicatorPair(ControlDisplay.TEXT_PRESS_TO_DELETE,
                        new int[]{ButtonIndicator.CONTROLLER_START, ButtonIndicator.KEYBOARD_DELETE})},
                1f, 0.1f, 0.5f, uiStage)).colspan(2).expandX();
        uiTable.row();
        uiTable.add(new ControlDisplay(new TextButtonIndicatorPair[]{
                new TextButtonIndicatorPair(ControlDisplay.TEXT_PRESS_TO_SELECT_SAVE,
                        new int[]{ButtonIndicator.CONTROLLER_JOYSTICK_Y, ButtonIndicator.KEYBOARD_WS}),
                new TextButtonIndicatorPair(ControlDisplay.TEXT_PRESS_TO_SELECT_LEVEL,
                        new int[]{ButtonIndicator.CONTROLLER_JOYSTICK_X, ButtonIndicator.KEYBOARD_AD})},
                1f, 0.1f, 0.5f, uiStage)).colspan(2).expandX();
    }

    private void initializeLevelSelector() {
        Table levelsTable = new Table();
        levels = new HashMap<>();
        levelSelectorSubScreen = new BaseActor(0, 0, uiStage);
        levelSelectorSubScreen.loadTexture(Assets.instance.getTexture(Assets.LEVEL_SELECTOR_IMAGE));
        float plannedWidth = uiStage.getWidth() - saveSlotWidth;
        float plannedHeight = uiStage.getHeight() * 0.5f;
        float levelSelectorScaleX = plannedWidth / levelSelectorSubScreen.getWidth();
        float levelSelectorScaleY = plannedHeight / levelSelectorSubScreen.getHeight();
        levelSelectorSubScreen.setSize(plannedWidth, plannedHeight);

        TileMapActor tileMapActor = new TileMapActor("level/level-selector.tmx");
        for (MapObject levelObject : tileMapActor.getRectangleList("Level")) {
            MapProperties levelProperties = levelObject.getProperties();
            int levelId = Integer.parseInt((String) levelProperties.get("id"));

            float x = (float) levelProperties.get("x") * levelSelectorScaleX;
            float y = (float) levelProperties.get("y") * levelSelectorScaleY;
            String title = (String) levelProperties.get("title");
            LevelStamp levelStamp = new LevelStamp(x, y, title, uiStage);
            levelSelectorSubScreen.addActor(levelStamp);
            levels.put(levelId, levelStamp);
        }
        levelSelectorSubScreen.setOpacity(0.5f);
        levelsTable.add(levelSelectorSubScreen);
        uiTable.add(levelsTable);
    }

    private void initializeSaveSlots() {
        Table savesTable = new Table();
        highlightIndex = -1;
        saveSlots = new ArrayList<>();
        saves = SaveGameService.getSaves();
        for (int i = 0; i < 4; i++) {
            SaveSlot slot = new SaveSlot(saveSlotWidth, saveSlotHeight, LEVEL_COUNT, saves.get(i), uiStage);
            if (highlightIndex == -1 && !slot.isEmptySlot()) {
                highlightIndex = i;
            }
            saveSlots.add(slot);
            savesTable.pad(10);
            savesTable.add(slot).expand().left();
            savesTable.row();
        }
        uiTable.add(savesTable);
    }


    @Override
    public void update(float delta) {
        lastSelection += delta;
        if (lastSelection >= SELECTION_COOLDOWN) {
            try {
                Controller controller = Controllers.getControllers().first();
                float yAxis = controller.getAxis(XBoxGamepad.AXIS_LEFT_Y);
                if (Math.abs(yAxis) > CONTROLLER_DEADZONE) {
                    switchOption(yAxis);
                    lastSelection = 0;
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
        if (levelSelectionMode) {
            if (keycode == Input.Keys.D) {
                selectNext();
                return true;
            }
            if (keycode == Input.Keys.A) {
                selectPrevious();
                return true;
            }
            if (keycode == Input.Keys.E) {
                loadSelectedSaveWithSelectedLevel();
            }
            if (keycode == Input.Keys.F) {
                leaveLevelSelectionMode();
                return true;
            }
        } else {
            if (keycode == Input.Keys.W) {
                switchOptionUp();
                return true;
            }
            if (keycode == Input.Keys.S) {
                switchOptionDown();
                return true;
            }
            if (keycode == Input.Keys.E) {
                enterLevelSelectionMode();
                return true;
            }
            if (keycode == Input.Keys.F) {
                leaveToMainMenu();
            }
            if (keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
                deleteSelectedSave();
                return true;
            }
        }
        return false;
    }

    private void loadSelectedSaveWithSelectedLevel() {
        BaseGame.setActiveScreen(new PlayerSelectionScreen(currentLevelIndex, getSelectedSave()));
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (levelSelectionMode) {
            if (buttonCode == XBoxGamepad.BUTTON_X) {
                loadSelectedSaveWithSelectedLevel();
            }
            if (buttonCode == XBoxGamepad.BUTTON_B) {
                leaveLevelSelectionMode();
                return true;
            }
        } else {
            if (buttonCode == XBoxGamepad.BUTTON_X) {
                enterLevelSelectionMode();
                return true;
            }
            if (buttonCode == XBoxGamepad.BUTTON_START) {
                deleteSelectedSave();
                return true;
            }
            if (buttonCode == XBoxGamepad.BUTTON_B) {
                leaveToMainMenu();
            }
        }
        return false;
    }

    private void switchOption(float amount) {
        int newHighlightIndex;
        if (amount > 0) {
            newHighlightIndex = highlightIndex + 1;
            if (newHighlightIndex >= saveSlots.size()) {
                newHighlightIndex = 0;
            }
            while (saveSlots.get(newHighlightIndex).isEmptySlot()) {
                newHighlightIndex = newHighlightIndex + 1;
                if (newHighlightIndex >= saveSlots.size()) {
                    newHighlightIndex = 0;
                }
            }
        } else {
            newHighlightIndex = highlightIndex - 1;
            if (newHighlightIndex < 0) {
                newHighlightIndex = saveSlots.size() - 1;
            }
            while (saveSlots.get(newHighlightIndex).isEmptySlot()) {
                newHighlightIndex = newHighlightIndex - 1;
                if (newHighlightIndex < 0) {
                    newHighlightIndex = saveSlots.size() - 1;
                }
            }
        }
        highlightIndex = newHighlightIndex;
        initializeCursorPosition();
    }

    private void switchOptionUp() {
        unHighlight();
        switchOption(-1);
        highlightYellow();
    }

    private void switchOptionDown() {
        unHighlight();
        switchOption(1);
        highlightYellow();
    }

    private void highlightYellow() {
        if (!saveSlots.get(highlightIndex).isEmptySlot()) {
            saveSlots.get(highlightIndex).setColor(Color.YELLOW);
        }
    }

    private void highlightGreen() {
        saveSlots.get(highlightIndex).setColor(Color.GREEN);
    }

    private void unHighlight() {
        if (highlightIndex >= 0 && highlightIndex < saveSlots.size() && !saveSlots.get(highlightIndex).isEmptySlot()) {
            saveSlots.get(highlightIndex).setColor(Color.WHITE);
        }
    }

    private Save getSelectedSave() {
        return saves.get(highlightIndex);
    }

    private void deleteSelectedSave() {
        unHighlight();
        saveSlots.get(highlightIndex).saveDeleted();
        SaveGameService.deleteSave(getSelectedSave());
        boolean allSavesEmpty = true;
        for (SaveSlot slot : saveSlots) {
            if (!slot.isEmptySlot()) {
                allSavesEmpty = false;
            }
        }
        if (allSavesEmpty) {
            leaveToMainMenu();
            return;
        }
        switchOptionDown();
    }

    private void selectNext() {
        changeSelection(1);
    }

    private void selectPrevious() {
        changeSelection(-1);
    }

    private void changeSelection(int direction) {
        lastSelection = 0;
        currentLevelIndex = MathUtils.clamp(currentLevelIndex + direction, MINIMUM_LEVEL_INDEX, LEVEL_COUNT);
        if (currentLevelIndex > nextLevelIndex) {
            currentLevelIndex = previousLevelIndex;
        } else {
            LevelStamp level = levels.get(currentLevelIndex);
            cursor.nextLevel(level);
            previousLevelIndex = currentLevelIndex;
        }
    }

    private void initializeCursorPosition() {
        currentLevelIndex = MINIMUM_LEVEL_INDEX;
        nextLevelIndex = getSelectedSave().getCompletedLevels() + 1;

        for (Map.Entry<Integer, LevelStamp> levelEntry : levels.entrySet()) {
            if (levelEntry.getKey() < nextLevelIndex) {
                levelEntry.getValue().completed();
            } else if (levelEntry.getKey() == nextLevelIndex) {
                levelEntry.getValue().next();
            } else {
                levelEntry.getValue().locked();
            }
        }


        currentLevelIndex = MathUtils.clamp(nextLevelIndex, MINIMUM_LEVEL_INDEX, LEVEL_COUNT);
        previousLevelIndex = currentLevelIndex;
        LevelStamp nextLevel = levels.get(currentLevelIndex);

        if (cursor != null) {
            cursor.remove();
        }
        cursor = new LevelSelectorCursor(nextLevel, uiStage);
        levelSelectorSubScreen.addActor(cursor);
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        if (Math.abs(value) > CONTROLLER_DEADZONE) {
            if (lastSelection >= SELECTION_COOLDOWN) {
                if (levelSelectionMode && axisCode == XBoxGamepad.AXIS_LEFT_X) {
                    if (value > 0) {
                        selectNext();
                    } else {
                        selectPrevious();
                    }
                }
                if (!levelSelectionMode && axisCode == XBoxGamepad.AXIS_LEFT_Y) {
                    if (-value > 0) {
                        selectPrevious();
                    } else {
                        selectNext();
                    }
                }
            }
        }
        return false;
    }

    private void enterLevelSelectionMode() {
        lastSelection = SELECTION_COOLDOWN;
        levelSelectionMode = true;
        levelSelectorSubScreen.setOpacity(1);
        for (SaveSlot slot : saveSlots) {
            slot.setOpacity(0.5f);
        }
        highlightGreen();
    }

    private void leaveLevelSelectionMode() {
        lastSelection = SELECTION_COOLDOWN;
        levelSelectionMode = false;
        levelSelectorSubScreen.setOpacity(0.5f);
        for (SaveSlot slot : saveSlots) {
            slot.setOpacity(1);
        }
        highlightYellow();
    }

    private void leaveToMainMenu() {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(this);
        BaseGame.setActiveScreen(new MainMenuScreen());
    }
}
