package szm.orde4c.game.util;

public class Save {
    private int id;
    private int completedLevels;

    public Save(int id, int completedLevels) {
        this.id = id;
        this.completedLevels = completedLevels;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCompletedLevels() {
        return completedLevels;
    }

    public void setCompletedLevels(int completedLevels) {
        this.completedLevels = completedLevels;
    }

    @Override
    public String toString() {
        return "saveId: " + id + "  |  completedLevels: " + completedLevels;
    }
}
