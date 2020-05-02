package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;

import java.util.ArrayList;
import java.util.List;

public class ElevatorGroup extends BaseActor {
    private ArrayList<Elevator> elevators;
    private String group;

    public ElevatorGroup(String group, Stage s) {
        super(0, 0, s);
        this.group = group;
        elevators = new ArrayList<>();
    }

    public void addElevator(int index, Elevator elevator) {
        try {
            elevators.add(index, elevator);
        } catch (IndexOutOfBoundsException e) {
            elevators.add(elevator);
        }
    }

    public List<Elevator> getElevators() {
        return elevators;
    }

    public String getGroup() {
        return group;
    }
}
