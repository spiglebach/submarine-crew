package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.XBoxGamepad;

public class EngineStation extends Station {
    private final float CONTROLLER_DEADZONE = 0.3f;

    public EngineStation(float x, float y, float width, float height, Submarine submarine, Stage s) {
        super(x, y, width, height, submarine, s);
    }

    @Override
    public void operate() {
        Controller controller = getOperatingPlayer().getController();
        if (controller != null) {
            float leftAxisX = controller.getAxis(XBoxGamepad.AXIS_LEFT_X);
            float leftAxisY = -controller.getAxis(XBoxGamepad.AXIS_LEFT_Y);
            float rightAxisY = -controller.getAxis(XBoxGamepad.AXIS_RIGHT_Y);
            if (Math.abs(leftAxisX) > CONTROLLER_DEADZONE) {
                if (leftAxisX > 0) {
                    submarine.accelerateForward();
                } else {
                    submarine.accelerateBackward();
                }
            }
            if (Math.abs(leftAxisY) > CONTROLLER_DEADZONE) {
                if (leftAxisY > 0) {
                    submarine.ascend();
                } else {
                    submarine.descend();
                }
            }
            if (Math.abs(rightAxisY) > CONTROLLER_DEADZONE) {
                if (rightAxisY > 0) {
                    submarine.liftNose();
                } else {
                    submarine.lowerNose();
                }
            }
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                submarine.ascend();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                submarine.descend();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                submarine.accelerateForward();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                submarine.accelerateBackward();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                submarine.lowerNose();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
                submarine.liftNose();
            }
        }
    }

    @Override
    protected void playerNowOperating() {

    }

    @Override
    protected void playerNoLongerOperating() {

    }

    @Override
    public void continiousEnergyConsumption(float delta) {

    }

}
