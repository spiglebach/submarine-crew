package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.XBoxGamepad;

public class ShieldStation extends Station {
    private static final int CHARGE_AMOUNT = 5;

    public ShieldStation(float x, float y, float width, float height, Submarine submarine, Stage s) {
        super(x, y, width, height, submarine, s);
    }

    @Override
    public void operate() {

    }

    @Override
    public void buttonPressed(int buttonCode) {
        if (buttonCode == XBoxGamepad.BUTTON_X) {
            chargeSubmarineShields();
        }
    }

    @Override
    public void keyDown(int keyCode) {
        if (keyCode == Input.Keys.E) {
            chargeSubmarineShields();
        }
    }

    private void chargeSubmarineShields() {
        if (submarine.getEnergy() > CHARGE_AMOUNT) {
            oneTimeEnergyConsumption(CHARGE_AMOUNT);
            submarine.increaseShield(CHARGE_AMOUNT);
        }
    }

    @Override
    public void continiousEnergyConsumption(float delta) {

    }
}
