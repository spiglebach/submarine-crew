package szm.orde4c.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import szm.orde4c.game.util.Save;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadGameScreen extends BaseGamepadScreen {
    private boolean levelSelectionMode;
    private int highlightIndex = 0;
    private final float OPTION_SWITCH_TIME_LIMIT = 0.5f;
    private float lastOptionSwitch = OPTION_SWITCH_TIME_LIMIT;

    private List<SaveSlot> saveSlots;
    private List<Save> saves;


    private Save save;

    private HashMap<Integer, LevelStamp> levels;
    private final int MINIMUM_LEVEL_INDEX = 1;
    private int maximumLevelIndex;
    private int currentLevelIndex;
    private int previousLevelIndex;
    private int nextLevelIndex;

    private LevelSelectorCursor cursor;

    BaseActor levelSelectorBackground;
    float levelSelectorScaleX;
    float levelSelectorScaleY;

    private final float CONTROLLER_DEADZONE = 0.4f;
    private static final float LEVEL_SELECT_TIME_LIMIT = 0.5f;
    private float lastMove = LEVEL_SELECT_TIME_LIMIT;

    @Override
    public void initialize() {
        levelSelectionMode = false;
        Table savesTable = new Table();
        Table levelsTable = new Table();


        float slotWidth = uiStage.getWidth() * 0.3f;
        float slotHeight = uiStage.getHeight() * 0.15f;

        ////////////////////////////////////////////////////////////////////////////////////////////////

        levels = new HashMap<>();
        maximumLevelIndex = 1;
        levelSelectorBackground = new BaseActor(0, 0, uiStage);
        levelSelectorBackground.loadTexture("level/overview-map.png");
        float plannedWidth = uiStage.getWidth() - slotWidth;
        float plannedHeight = uiStage.getHeight() * 0.5f;
        levelSelectorScaleX = plannedWidth / levelSelectorBackground.getWidth();
        levelSelectorScaleY = plannedHeight / levelSelectorBackground.getHeight();
        levelSelectorBackground.setSize(plannedWidth, plannedHeight);

        TileMapActor tileMapActor = new TileMapActor("level/map.tmx", uiStage);
        for (MapObject levelObject : tileMapActor.getRectangleList("Level")) {
            MapProperties levelProperties = levelObject.getProperties();
            int levelId = Integer.parseInt((String) levelProperties.get("id"));
            if (levelId > maximumLevelIndex) {
                maximumLevelIndex = levelId;
            }
            String title = (String) levelProperties.get("title");
            float x = (float) levelProperties.get("x") * levelSelectorScaleX;
            float y = (float) levelProperties.get("y") * levelSelectorScaleY;
            LevelStamp levelStamp = new LevelStamp(x, y, uiStage);
            levelSelectorBackground.addActor(levelStamp);
            levels.put(levelId, levelStamp);
        }
        levelSelectorBackground.setOpacity(0.5f);
        levelsTable.add(levelSelectorBackground);


        ////////////////////////////////////////////////////////////////////////////////////////////////


        saveSlots = new ArrayList<>();
        saves = SaveGameService.getSaves();
        for (int i = 0; i < 4; i++) {
            boolean slotUsed = saves.get(i) != null;
            SaveSlot slot = new SaveSlot(slotWidth, slotHeight, maximumLevelIndex, saves.get(i), uiStage);
            if (slotUsed) {
                saveSlots.add(slot);
            }
            savesTable.pad(60);
            savesTable.add(slot).expand().left();
            savesTable.row();
        }


        uiTable.add(savesTable);
        uiTable.add(levelsTable);
        uiTable.row();

        uiTable.add(new ControlDisplay(new TextButtonIndicatorPair[]{
                new TextButtonIndicatorPair(ControlDisplay.TEXT_PRESS_TO_LOAD,
                        new int[]{ButtonIndicator.CONTROLLER_FACE_BUTTON_WEST, ButtonIndicator.KEYBOARD_BUTTON_E}),
                new TextButtonIndicatorPair(ControlDisplay.TEXT_PRESS_TO_DELETE,
                        new int[]{ButtonIndicator.CONTROLLER_BUTTON_START, ButtonIndicator.KEYBOARD_BUTTON_DELETE})},
                0.3f, 0.1f, 0.5f, uiStage)).colspan(2).expandX();

        initCursorPosition();
        highlightYellow();
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
                BaseGame.setActiveScreen(new PlayerSelectionScreen(currentLevelIndex, getSelectedSave()));
            }
            if (keycode == Input.Keys.F) {
                leaveLevelSelectionMode();
                highlightYellow();
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
                highlightGreen();
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
        }
        return false;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) { //TODO
        if (buttonCode == XBoxGamepad.BUTTON_X) {
//            loadSelectedSave();
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
        highlightYellow();
        initCursorPosition();
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

    private void highlightYellow() {
        saveSlots.get(highlightIndex).setColor(Color.YELLOW);
    }

    private void highlightGreen() {
        saveSlots.get(highlightIndex).setColor(Color.GREEN);
    }

    private void unHighlight() {
        saveSlots.get(highlightIndex).setColor(Color.WHITE);
    }

    private Save getSelectedSave() {
        // TODO properly
        Save selectedSave = saves.get(highlightIndex);
        return selectedSave;
    }

    private void deleteSelectedSave() {
        // TODO
    }

    private void selectNext() {
        changeSelection(1);
    }

    private void selectPrevious() {
        changeSelection(-1);
    }

    private void changeSelection(int direction) {
        lastMove = 0;
        currentLevelIndex = MathUtils.clamp(currentLevelIndex + direction, MINIMUM_LEVEL_INDEX, maximumLevelIndex);
        if (currentLevelIndex > nextLevelIndex) {
            currentLevelIndex = previousLevelIndex;
        } else {
            LevelStamp level = levels.get(currentLevelIndex);
            cursor.nextLevel(level);
            previousLevelIndex = currentLevelIndex;
        }
    }

    private void initCursorPosition() {
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


        currentLevelIndex = MathUtils.clamp(nextLevelIndex, MINIMUM_LEVEL_INDEX, maximumLevelIndex);
        previousLevelIndex = currentLevelIndex;
        LevelStamp nextLevel = levels.get(currentLevelIndex);

        cursor = new LevelSelectorCursor(nextLevel, uiStage);
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        if (Math.abs(value) > CONTROLLER_DEADZONE) {
            if (lastMove >= LEVEL_SELECT_TIME_LIMIT) {
                if (axisCode == XBoxGamepad.AXIS_LEFT_X) {
                    if (value > 0) {
                        selectNext();
                    } else {
                        selectPrevious();
                    }
                }
                if (axisCode == XBoxGamepad.AXIS_LEFT_Y) {
                    if (value > 0) {
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
        levelSelectionMode = true;
        levelSelectorBackground.setOpacity(1);
        for (SaveSlot slot : saveSlots) {
            slot.setOpacity(0.5f);
        }
    }

    private void leaveLevelSelectionMode() {
        levelSelectionMode = false;
        levelSelectorBackground.setOpacity(0.5f);
        for (SaveSlot slot : saveSlots) {
            slot.setOpacity(1);
        }
    }
}
