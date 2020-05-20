package szm.orde4c.game.util;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;

public class Assets implements Disposable {
    private static final String STATE_LEVEL = "Szint textúrák betöltése...";
    private static final String STATE_ENEMY = "Ellenfél textúrák betöltése...";
    private static final String STATE_SUBMARINE = "Tengeralattjáró textúrák betöltése...";
    private static final String STATE_BUTTON = "Gomb textúrák betöltése...";
    private static final String STATE_PLAYER = "Játékos textúrák betöltése...";
    private static final String STATE_MISC = "Egyéb szükséges textúrák betöltése...";

    public static final String BLANK = "blank.jpg";
    public static final String BLANK_CIRCLE = "circle.png";
    public static final String LOADING_SCREEN = "loading.jpg";
    public static final String BUTTON = "button.png";

    public static final String SAVE_SLOT = "saveslot.jpg";
    public static final String MENU_SCREEN = "menu.jpg";

    public static final String ENEMY = "enemy/enemy.png";

    public static final String FONT_GILLSANS = "font/gillsans.fnt";

    public static final String SUBMARINE_ELEVATOR = "submarine/elevator.png";
    public static final String SUBMARINE_REFLECTOR_LIGHT_ACTIVE = "submarine/reflector-light-active.png";
    public static final String SUBMARINE_REFLECTOR_LIGHT_INACTIVE = "submarine/reflector-light-inactive.png";
    public static final String SUBMARINE_IMAGE = "submarine/submarine.png";
    public static final String SUBMARINE_TORPEDO = "submarine/torpedo.png";


    public static final String LEVEL_SELECTOR_MAPSIGN_LOCKED = "level/mapsign_locked.png";
    public static final String LEVEL_SELECTOR_MAPSIGN_NEXT = "level/mapsign_next.png";
    public static final String LEVEL_SELECTOR_MAPSIGN_COMPLETE = "level/mapsign_complete.png";
    public static final String LEVEL_SELECTOR_CURSOR = "level/level-selector-cursor.png";
    public static final String LEVEL_SELECTOR_IMAGE = "level/level-selector.png";
    public static final String LEVEL_SELECTOR_TMX = "level/level-selector.tmx";
    public static final String LEVEL_GEYSER_0 = "level/geyser_0.png";
    public static final String LEVEL_GEYSER_1 = "level/geyser_1.png";
    public static final String LEVEL_GEYSER_2 = "level/geyser_2.png";
    public static final String LEVEL_ROCK_0 = "level/rock_0.png";
    public static final String LEVEL_ROCK_1 = "level/rock_1.png";
    public static final String LEVEL_ROCK_2 = "level/rock_2.png";
    public static final String LEVEL_ROCK_3 = "level/rock_3.png";
    public static final String LEVEL_VEGETATION_0 = "level/vegetation_0.png";
    public static final String LEVEL_VEGETATION_1 = "level/vegetation_1.png";
    public static final String LEVEL_VEGETATION_2 = "level/vegetation_2.png";
    public static final String LEVEL_VEGETATION_3 = "level/vegetation_3.png";
    public static final String LEVEL_VEGETATION_4 = "level/vegetation_4.png";
    public static final String LEVEL_VEGETATION_5 = "level/vegetation_5.png";

    public static final String LEVEL1_MAP_FILE = "level/level1/level1.tmx";
    public static final String LEVEL1_MAP_BACKGROUND = "level/level1/level1background.jpg";
    public static final String LEVEL1_MAP_FOREGROUND = "level/level1/level1foreground.png";
    public static final String LEVEL2_MAP_FILE = "level/level2/level2.tmx";
    public static final String LEVEL2_MAP_BACKGROUND = "level/level2/level2background.jpg";
    public static final String LEVEL2_MAP_FOREGROUND = "level/level2/level2foreground.png";
    public static final String LEVEL3_MAP_FILE = "level/level3/level3.tmx";
    public static final String LEVEL3_MAP_BACKGROUND = "level/level3/level3background.jpg";
    public static final String LEVEL3_MAP_FOREGROUND = "level/level3/level3foreground.png";
    public static final String LEVEL4_MAP_FILE = "level/level4/level4.tmx";
    public static final String LEVEL4_MAP_BACKGROUND = "level/level4/level4background.jpg";
    public static final String LEVEL4_MAP_FOREGROUND = "level/level4/level4foreground.png";
    public static final String LEVEL5_MAP_FILE = "level/level5/level5.tmx";
    public static final String LEVEL5_MAP_BACKGROUND = "level/level5/level5background.jpg";
    public static final String LEVEL5_MAP_FOREGROUND = "level/level5/level5foreground.png";

    public static final String BUTTON_CONTROLLER_FACE_BLANK = "button/controller_face_blank.png";
    public static final String BUTTON_CONTROLLER_FACE_EAST = "button/controller_face_east.png";
    public static final String BUTTON_CONTROLLER_FACE_NORTH = "button/controller_face_north.png";
    public static final String BUTTON_CONTROLLER_FACE_WEST = "button/controller_face_west.png";
    public static final String BUTTON_CONTROLLER_FACE_SOUTH = "button/controller_face_south.png";
    public static final String BUTTON_CONTROLLER_BACK = "button/controller_back.png";
    public static final String BUTTON_CONTROLLER_START = "button/controller_start.png";
    public static final String BUTTON_CONTROLLER_JOYSTICK_Y = "button/controller_joystick_y.png";
    public static final String BUTTON_CONTROLLER_JOYSTICK_X = "button/controller_joystick_x.png";

    public static final String BUTTON_KEYBOARD_AD = "button/keyboard_ad.png";
    public static final String BUTTON_KEYBOARD_DELETE = "button/keyboard_delete.png";
    public static final String BUTTON_KEYBOARD_E = "button/keyboard_e.png";
    public static final String BUTTON_KEYBOARD_F = "button/keyboard_f.png";
    public static final String BUTTON_KEYBOARD_SPACE = "button/keyboard_space.png";
    public static final String BUTTON_KEYBOARD_WS = "button/keyboard_ws.png";
    public static final String BUTTON_PADLOCK = "button/padlock.png";

    public static final String PLAYER_ANIMATIONS = "player/player.png";
    public static final String PLAYER_ARROW = "player/arrow_left.png";
    public static final String PLAYER_SLOT = "player/slotplayer.png";

    public static final Assets instance = new Assets();

    private ArrayList<String> states;
    private int stateIndex;

    private AssetManager manager;

    private Assets() {
        manager = new AssetManager();
        manager.setLoader(TiledMap.class, new TmxMapLoader());
        preLoad();
        states = new ArrayList<>();
        states.add(STATE_ENEMY);
        states.add(STATE_SUBMARINE);
        states.add(STATE_PLAYER);
        states.add(STATE_BUTTON);
        states.add(STATE_LEVEL);
        states.add(STATE_MISC);

        stateIndex = 0;
        loadEnemyAssets();
    }

    private void preLoad() {
        manager.load(BLANK, Texture.class);
        manager.load(BUTTON, Texture.class);
        manager.load(FONT_GILLSANS, BitmapFont.class);
        manager.load(LOADING_SCREEN, Texture.class);
        manager.finishLoading();
    }

    private void loadPlayerAssets() {
        manager.load(PLAYER_ANIMATIONS, Texture.class);
        manager.load(PLAYER_ARROW, Texture.class);
        manager.load(PLAYER_SLOT, Texture.class);
    }

    private void loadEnemyAssets() {
        manager.load(ENEMY, Texture.class);
    }

    private void loadUIMisc() {
        manager.load(BLANK_CIRCLE, Texture.class);
        manager.load(SAVE_SLOT, Texture.class);
        manager.load(MENU_SCREEN, Texture.class);
        manager.load("exit_sign.png", Texture.class);
        manager.load("exit_sign_arrow.png", Texture.class);
    }

    private void loadLevelAssets() {
        manager.load(LEVEL_SELECTOR_CURSOR, Texture.class);
        manager.load(LEVEL_SELECTOR_MAPSIGN_LOCKED, Texture.class);
        manager.load(LEVEL_SELECTOR_MAPSIGN_COMPLETE, Texture.class);
        manager.load(LEVEL_SELECTOR_MAPSIGN_NEXT, Texture.class);
        manager.load(LEVEL_SELECTOR_IMAGE, Texture.class);
        manager.load(LEVEL_SELECTOR_TMX, TiledMap.class);
        manager.load(LEVEL_GEYSER_0, Texture.class);
        manager.load(LEVEL_GEYSER_1, Texture.class);
        manager.load(LEVEL_GEYSER_2, Texture.class);
        manager.load(LEVEL_ROCK_0, Texture.class);
        manager.load(LEVEL_ROCK_1, Texture.class);
        manager.load(LEVEL_ROCK_2, Texture.class);
        manager.load(LEVEL_ROCK_3, Texture.class);
        manager.load(LEVEL_VEGETATION_0, Texture.class);
        manager.load(LEVEL_VEGETATION_1, Texture.class);
        manager.load(LEVEL_VEGETATION_2, Texture.class);
        manager.load(LEVEL_VEGETATION_3, Texture.class);
        manager.load(LEVEL_VEGETATION_4, Texture.class);
        manager.load(LEVEL_VEGETATION_5, Texture.class);

        manager.load(LEVEL1_MAP_FILE, TiledMap.class);
        manager.load(LEVEL1_MAP_BACKGROUND, Texture.class);
        manager.load(LEVEL1_MAP_FOREGROUND, Texture.class);
        // assets/level/level2
        /*manager.load(LEVEL2_MAP_FILE, TiledMap.class);
        manager.load(LEVEL2_MAP_BACKGROUND, Texture.class);
        manager.load(LEVEL2_MAP_FOREGROUND, Texture.class);
        // assets/level/level3
        manager.load(LEVEL3_MAP_FILE, TiledMap.class);
        manager.load(LEVEL3_MAP_BACKGROUND, Texture.class);
        manager.load(LEVEL3_MAP_FOREGROUND, Texture.class);
        // assets/level/level4
        manager.load(LEVEL4_MAP_FILE, TiledMap.class);
        manager.load(LEVEL4_MAP_BACKGROUND, Texture.class);
        manager.load(LEVEL4_MAP_FOREGROUND, Texture.class);
        // assets/level/level5
        manager.load(LEVEL5_MAP_FILE, TiledMap.class);
        manager.load(LEVEL5_MAP_BACKGROUND, Texture.class);
        manager.load(LEVEL5_MAP_FOREGROUND, Texture.class);*/
    }

    private void loadSubmarineAssets() {
        manager.load(SUBMARINE_ELEVATOR, Texture.class);
        manager.load(SUBMARINE_REFLECTOR_LIGHT_ACTIVE, Texture.class);
        manager.load(SUBMARINE_REFLECTOR_LIGHT_INACTIVE, Texture.class);
        manager.load(SUBMARINE_IMAGE, Texture.class);
        manager.load(SUBMARINE_TORPEDO, Texture.class);
    }

    private void loadButtonAssets() {
        manager.load(BUTTON_CONTROLLER_FACE_BLANK, Texture.class);
        manager.load(BUTTON_CONTROLLER_FACE_EAST, Texture.class);
        manager.load(BUTTON_CONTROLLER_FACE_NORTH, Texture.class);
        manager.load(BUTTON_CONTROLLER_FACE_WEST, Texture.class);
        manager.load(BUTTON_CONTROLLER_FACE_SOUTH, Texture.class);
        manager.load(BUTTON_CONTROLLER_BACK, Texture.class);
        manager.load(BUTTON_CONTROLLER_START, Texture.class);
        manager.load(BUTTON_CONTROLLER_JOYSTICK_Y, Texture.class);
        manager.load(BUTTON_CONTROLLER_JOYSTICK_X, Texture.class);

        manager.load(BUTTON_KEYBOARD_AD, Texture.class);
        manager.load(BUTTON_KEYBOARD_DELETE, Texture.class);
        manager.load(BUTTON_KEYBOARD_E, Texture.class);
        manager.load(BUTTON_KEYBOARD_F, Texture.class);
        manager.load(BUTTON_KEYBOARD_SPACE, Texture.class);
        manager.load(BUTTON_KEYBOARD_WS, Texture.class);
        manager.load(BUTTON_PADLOCK, Texture.class);
    }

    public boolean update() {
        boolean finished = manager.update();
        if (finished && stateIndex >= states.size()) {
            return true;
        } else if (finished) {
            stateIndex++;
            switch (stateIndex) {
                case 1:
                    loadButtonAssets();
                    return false;
                case 2:
                    loadSubmarineAssets();
                    return false;
                case 3:
                    loadPlayerAssets();
                    return false;
                case 4:
                    loadLevelAssets();
                    return false;
                case 5:
                    loadUIMisc();
                    return false;
            }
        }
        return false;
    }

    public String getState() {
        if (stateIndex < states.size()) {
            return states.get(stateIndex);
        }
        return "Töltés befejezve!";
    }

    @Override
    public void dispose() {
        manager.dispose();
    }

    public final Texture getTexture(String name) {
        return manager.get(name, Texture.class);
    }

    public final TiledMap getTiledMap(String name) {
        return manager.get(name, TiledMap.class);
    }

    public final BitmapFont getBitmapFont(String name) {
        return manager.get(name, BitmapFont.class);
    }

    public float getProgress() {
        return manager.getProgress();
    }

    public int getStateCount() {
        return states.size();
    }

    public int getStateIndex() {
        return stateIndex;
    }

    public void loadAsset(String fileName, Class objectClass) {
        manager.load(fileName, objectClass);
        manager.finishLoadingAsset(fileName);
    }
}
