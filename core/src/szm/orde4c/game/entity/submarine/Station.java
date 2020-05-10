package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.XBoxGamepad;

public abstract class Station extends BaseActor {
    protected Submarine submarine;
    protected Player operatingPlayer;
    protected boolean operated;
    protected boolean activated;

    public Station(float x, float y, float width, float height, Submarine submarine, Stage s) {
        super(x, y, s);
        setSize(width, height);
        setBoundaryRectangle();
        activated = false;
        this.submarine = submarine;
        submarine.addActor(this);
    }

    public abstract void operate();

    protected abstract void playerNowOperating();

    protected abstract void playerNoLongerOperating();


    public boolean isOperated() {
        return operated;
    }

    public Player getOperatingPlayer() {
        return operatingPlayer;
    }

    public void setOperatingPlayer(Player operatingPlayer) {
        operated = operatingPlayer != null;
        this.operatingPlayer = operatingPlayer;
        if (operated) {
            playerNowOperating();
        } else {
            playerNoLongerOperating();
        }
    }

    public boolean isActivated() {
        return activated;
    }

    public void buttonPressed(int buttonCode) {
        if (buttonCode == XBoxGamepad.BUTTON_X) {
            activated = !activated;
        }
    }

    public void keyDown(int keyCode) {
        if (keyCode == Input.Keys.E) {
            activated = !activated;
        }
    }

    public void axisMoved(int axisCode, float value) {
    }

    public void oneTimeEnergyConsumption(float amount) {
        submarine.decreaseEnergy(amount);
    }

    public abstract void continiousEnergyConsumption(float delta);

}
