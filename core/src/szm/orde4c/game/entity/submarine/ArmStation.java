package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.XBoxGamepad;

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
        arm.operate(operatingPlayer.getController());
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
        super.buttonPressed(buttonCode);
        if (buttonCode == XBoxGamepad.BUTTON_Y) {
            arm.switchTool();
        }
    }

    @Override
    public void keyDown(int keyCode) {
        super.keyDown(keyCode);
        if (keyCode == Input.Keys.R) {
            arm.switchTool();
        }
    }
}
