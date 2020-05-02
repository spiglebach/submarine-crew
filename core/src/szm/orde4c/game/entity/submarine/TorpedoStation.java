package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.entity.Torpedo;

public class TorpedoStation extends Station {
    private float torpedoStartX;
    private float torpedoStartY;

    public TorpedoStation(float x, float y, float width, float height, float torpedoStartX, float torpedoStartY, Submarine submarine, Stage s) {
        super(x, y, width, height, submarine, s);
        this.torpedoStartX = torpedoStartX;
        this.torpedoStartY = torpedoStartY;
    }

    private void fireTorpedo() {
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
    }

    @Override
    public void keyDown(int keyCode) {
        if (keyCode == Input.Keys.SPACE) {
            fireTorpedo();
        }
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
