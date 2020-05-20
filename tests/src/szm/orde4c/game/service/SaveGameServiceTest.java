package szm.orde4c.game.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import szm.orde4c.game.GdxTestRunner;
import szm.orde4c.game.util.Save;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(GdxTestRunner.class)
public class SaveGameServiceTest {

    @Test
    public void pruneSaves_withAnyNumberOfSaves_assertEquals() {
        SaveGameService.pruneSaves();
        ArrayList<Save> expectedSaves = new ArrayList<>();
        expectedSaves.add(null);
        expectedSaves.add(null);
        expectedSaves.add(null);
        expectedSaves.add(null);
        assertEquals(expectedSaves, SaveGameService.getSaves());
    }

    @Test
    public void allSaveSlotsOccupied_withNoSaves_assertFalse() {
        SaveGameService.pruneSaves();
        assertFalse(SaveGameService.allSaveSlotsOccupied());
    }

    @Test
    public void hasAtLeastOneSave_withNoSaves_assertFalse() {
        SaveGameService.pruneSaves();
        assertFalse(SaveGameService.hasAtLeastOneSave());
    }

    @Test
    public void getFirstUnoccupiedSaveId_withNoSaves_assertEquals() {
        SaveGameService.pruneSaves();
        assertEquals(0, SaveGameService.getFirstUnoccupiedSaveId());
    }

    @Test
    public void save_withZeros_assertNotEquals() {
        SaveGameService.pruneSaves();
        ArrayList<Save> expectedSaves = new ArrayList<>();
        expectedSaves.add(null);
        expectedSaves.add(null);
        expectedSaves.add(null);
        expectedSaves.add(null);
        Save save = new Save(0, 0);
        SaveGameService.save(save);
        assertNotEquals(expectedSaves, SaveGameService.getSaves());
    }
}
