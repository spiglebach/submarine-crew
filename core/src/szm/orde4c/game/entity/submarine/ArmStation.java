package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.util.ArmType;

public class ArmStation extends Station {
    private Arm arm;

    public ArmStation(float x, float y, float width, float height, Arm arm, Submarine submarine, Stage s) {
        super(x, y, width, height, submarine, s);
        this.arm = arm;
        arm.setStation(this);
        submarine.addActor(arm);
        submarine.addActor(this);
    }

    @Override
    public void operate() {
        if (isOperated()) {
            Controller controller = getOperatingPlayer().getController();
            if (controller != null) {
                activated = controller.getButton(XBoxGamepad.BUTTON_X);
            } else {
                activated = Gdx.input.isKeyPressed(Input.Keys.E);
            }
            arm.operate(operatingPlayer.getController());
        } else {
            arm.makeAutonomousAction();
        }

    }

    @Override
    protected void playerNowOperating() {
    }

    @Override
    protected void playerNoLongerOperating() {
        arm.retract();
    }

    @Override
    public void buttonPressed(int buttonCode) {
        if (buttonCode == XBoxGamepad.BUTTON_Y) {
            arm.switchTool();
        }
    }

    @Override
    public void keyDown(int keyCode) {
        if (keyCode == Input.Keys.R) {
            arm.switchTool();
        }
    }

    @Override
    public void continiousEnergyConsumption(float delta) {
        if (activated && ArmType.DRILL.equals(arm.getType())) {
            submarine.decreaseEnergy(Arm.DRILLING_ENERY_COST * delta);
            if (submarine.getEnergy() <= 0) {
                activated = false;
            }
        }
    }
}
