package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.entity.Torpedo;

import java.util.Queue;

public class TorpedoStation extends Station {
    private static final float TORPEDO_LAUNCH_ENERGY_COST = 10;
    private int torpedoCount;
    private float torpedoStartX;
    private float torpedoStartY;
    private Queue<BaseActor> countIndicators;

    public TorpedoStation(float x, float y, float width, float height, float torpedoStartX, float torpedoStartY, Queue<BaseActor> indicatorQueue, Submarine submarine, Stage s) {
        super(x, y, width, height, submarine, s);
        this.torpedoStartX = torpedoStartX;
        this.torpedoStartY = torpedoStartY;
        countIndicators = indicatorQueue;
        torpedoCount = countIndicators.size();
    }

    private void fireTorpedo() {
        if (torpedoCount > 0 && submarine.getEnergy() > TORPEDO_LAUNCH_ENERGY_COST) {
            oneTimeEnergyConsumption(TORPEDO_LAUNCH_ENERGY_COST);
            torpedoCount--;
            float submarineX = submarine.getX();
            float submarineY = submarine.getY();
            float submarineOriginX = submarine.getOriginX();
            float submarineOriginY = submarine.getOriginY();

            Vector2 submarineOriginToTorpedoStartPosition = new Vector2(torpedoStartX, torpedoStartY).sub(submarineOriginX, submarineOriginY);
            submarineOriginToTorpedoStartPosition = submarineOriginToTorpedoStartPosition.rotate(submarine.getRotation());

            Vector2 absoluteTorpedoStartPosition = new Vector2(submarineX, submarineY);
            absoluteTorpedoStartPosition.add(submarineOriginX, submarineOriginY);
            absoluteTorpedoStartPosition = absoluteTorpedoStartPosition.add(submarineOriginToTorpedoStartPosition);

            new Torpedo(absoluteTorpedoStartPosition.x, absoluteTorpedoStartPosition.y, submarine.getRotation(), getStage());
            BaseActor countIndicator = countIndicators.remove();
            countIndicator.setVisible(false);
            countIndicator.remove();
        }
    }

    @Override
    public void keyDown(int keyCode) {
        if (keyCode == Input.Keys.E) {
            fireTorpedo();
        }
    }

    @Override
    public void continiousEnergyConsumption(float delta) {

    }

    @Override
    public void buttonPressed(int buttonCode) {
        if (buttonCode == XBoxGamepad.BUTTON_X) {
            fireTorpedo();
        }
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
}
