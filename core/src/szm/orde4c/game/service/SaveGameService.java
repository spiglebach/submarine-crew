package szm.orde4c.game.service;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import szm.orde4c.game.util.Save;

import java.util.ArrayList;
import java.util.List;

public class SaveGameService {
    private static final int SAVE_SLOT_COUNT = 4;
    private static final String SAVE_PREFIX = "Save";
    private static final String LAST_SAVE_INDEX_KEY = "LastSave";
    private static final String PREFERENCE_KEY = "LocalSaves";

    public static List<Save> getSaves() {
        Preferences savePreferences = Gdx.app.getPreferences(PREFERENCE_KEY);
        ArrayList<Save> saves = new ArrayList<>();

        for (int i = 0; i < SAVE_SLOT_COUNT; i++) {
            String saveKey = SAVE_PREFIX + i;
            int completedLevels = savePreferences.getInteger(saveKey);
            if (savePreferences.contains(saveKey)) {
                saves.add(new Save(i, completedLevels));
            } else {
                saves.add(null);
            }
        }
        return saves;
    }

    public static void save(int saveId, int completedLevels) {
        Preferences savePreferences = Gdx.app.getPreferences(PREFERENCE_KEY);
        savePreferences.putInteger(SAVE_PREFIX + saveId, completedLevels);
        savePreferences.putInteger(LAST_SAVE_INDEX_KEY, saveId);
        savePreferences.flush();
    }

    public static void save(Save save) {
        save(save.getId(), save.getCompletedLevels());
    }

    public static void deleteSave(Save save) {
        deleteSave(save.getId());
    }

    public static void deleteSave(int saveId) {
        Preferences savePreferences = Gdx.app.getPreferences(PREFERENCE_KEY);
        savePreferences.remove(SAVE_PREFIX + saveId);
        savePreferences.flush();
    }

    public static Save getSaveById(int saveId) {
        for (Save save : getSaves()) {
            if (save != null && save.getId() == saveId) {
                return save;
            }
        }
        return null;
    }

    public static boolean hasAtLeastOneSave() {
        for (Save save : getSaves()) {
            if (save != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean allSaveSlotsOccupied() {
        for (Save save : getSaves()) {
            if (save == null) {
                return false;
            }
        }
        return true;
    }

    public static int getFirstUnoccupiedSaveId() {
        for (int i = 0; i < SAVE_SLOT_COUNT; i++) {
            if (getSaveById(i) == null) {
                return i;
            }
        }
        return -1;
    }

    public static void pruneSaves() {
        for (int i = 0; i <= SAVE_SLOT_COUNT; i++) {
            deleteSave(i);
        }
    }
}
