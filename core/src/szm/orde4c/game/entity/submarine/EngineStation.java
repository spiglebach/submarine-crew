package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class EngineStation extends Station {

    public EngineStation(float x, float y, float width, float height, Submarine submarine, Stage s) {
        super(x, y, width, height, submarine, s);
    }


    @Override
    public void operate() {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            ((Submarine)getParent()).ascend();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            ((Submarine)getParent()).descend();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            ((Submarine)getParent()).accelerateForward();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            ((Submarine)getParent()).accelerateBackward();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            ((Submarine)getParent()).lowerNose();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            ((Submarine)getParent()).liftNose();
        }
    }

    @Override
    protected void playerNowOperating() {

    }

    @Override
    protected void playerNoLongerOperating() {

    }
}
