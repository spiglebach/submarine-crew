package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class ShieldStation extends Station {
    public ShieldStation(float x, float y, float width, float height, Submarine submarine, Stage s) {
        super(x, y, width, height, submarine, s);
    }

    @Override
    public void operate() {

    }

    @Override
    protected void playerNowOperating() {
    }

    @Override
    protected void playerNoLongerOperating() {

    }

    @Override
    public void buttonPressed(int buttonCode) {
        super.buttonPressed(buttonCode);
    }

    @Override
    public void keyDown(int keyCode) {
        if (keyCode == Input.Keys.E) {
            submarine.increaseShield(5);
        }
    }
}
