package szm.orde4c.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.MathUtils;
import szm.orde4c.game.base.*;
import szm.orde4c.game.ui.LevelSelectorCursor;
import szm.orde4c.game.ui.LevelStamp;
import szm.orde4c.game.util.Save;
import java.util.HashMap;
import java.util.Map;

public class LevelSelectorScreen extends BaseGamepadScreen {

    private Save save;

    private HashMap<Integer, LevelStamp> levels;
    private int minimumLevelIndex;
    private int maximumLevelIndex;
    private int currentLevelIndex;
    private int previousLevelIndex;
    private int nextLevelIndex;

    private LevelSelectorCursor cursor;

    private final float CONTROLLER_DEADZONE = 0.4f;
    private static final float LEVEL_SELECT_TIME_LIMIT = 0.5f;
    private float lastMove = LEVEL_SELECT_TIME_LIMIT;

    public LevelSelectorScreen(Save savedGame) {
        super();
        this.save = savedGame;
        initCursorPosition();

    }

    @Override
    public void initialize() {
        levels = new HashMap<>();
        maximumLevelIndex = 1;
        BaseActor background = new BaseActor(0, 0, uiStage);
        background.loadTexture("overview-map.png");
        BaseActor.setWorldBounds(background);

        TileMapActor tileMapActor = new TileMapActor("level/map.tmx", uiStage);
        for (MapObject levelObject : tileMapActor.getRectangleList("Level")) {
            MapProperties levelProperties = levelObject.getProperties();
            int levelId = Integer.parseInt((String)levelProperties.get("id"));
            if (levelId > maximumLevelIndex) {
                maximumLevelIndex = levelId;
            }
            String title = (String) levelProperties.get("title");
            float x = (float) levelProperties.get("x");
            float y = (float) levelProperties.get("y");
            levels.put(levelId, new LevelStamp(x, y, uiStage));
        }
        minimumLevelIndex = (Integer)levels.keySet().toArray()[0];
        for (Integer levelId : levels.keySet()) {
            if (levelId < minimumLevelIndex) {
                minimumLevelIndex = levelId;
            }
        }

    }

    private void initCursorPosition() {
        currentLevelIndex = minimumLevelIndex;
        nextLevelIndex = save.getCompletedLevels() + 1;

        for (Map.Entry<Integer, LevelStamp> levelEntry : levels.entrySet()) {
            if (levelEntry.getKey() < nextLevelIndex) {
                levelEntry.getValue().completed();
            } else if (levelEntry.getKey() == nextLevelIndex) {
                levelEntry.getValue().next();
            } else {
                levelEntry.getValue().locked();
            }
        }



        currentLevelIndex = MathUtils.clamp(nextLevelIndex, minimumLevelIndex, maximumLevelIndex);
        previousLevelIndex = currentLevelIndex;
        LevelStamp nextLevel = levels.get(currentLevelIndex);

        cursor = new LevelSelectorCursor(nextLevel, uiStage);
    }

    @Override
    public void update(float dt) {
    }

    private void selectNext() {
        changeSelection(1);
    }

    private void selectPrevious() {
        changeSelection(-1);
    }

    private void changeSelection(int direction) {
        lastMove = 0;
        currentLevelIndex = MathUtils.clamp(currentLevelIndex + direction, minimumLevelIndex, maximumLevelIndex);
        if (currentLevelIndex > nextLevelIndex) {
            currentLevelIndex = previousLevelIndex;
        } else {
            LevelStamp level = levels.get(currentLevelIndex);
            cursor.nextLevel(level);
            previousLevelIndex = currentLevelIndex;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            Gdx.app.exit();
        }
        if (keycode == Input.Keys.D) {
            selectNext();
        }
        if (keycode == Input.Keys.A) {
            selectPrevious();
        }
        if (keycode == Input.Keys.E) {
            BaseGame.setActiveScreen(new PlayerSelectionScreen());
        }
        return false;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (buttonCode == XBoxGamepad.BUTTON_X) {
            BaseGame.setActiveScreen(new PlayerSelectionScreen());
        }
        return false;
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
}
