package szm.orde4c.game.util;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;

public class PlayerInfo {
    private final Controller assignedController;
    private final Color color;
    private final ControlType controlType;
    public PlayerInfo(Color c, ControlType ct, Controller ac) {
        color = c;
        controlType = ct;
        assignedController = controlType.equals(ControlType.CONTROLLER) ? ac : null;
    }

    public Controller getAssignedController() {
        return assignedController;
    }

    public Color getColor() {
        return color;
    }

    public ControlType getControlType() {
        return controlType;
    }
}
