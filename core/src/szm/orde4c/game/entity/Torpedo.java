package szm.orde4c.game.entity;

import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;

public class Torpedo extends BaseActor {
    public Torpedo(float x, float y, float rotation, Stage s) {
        super(x, y, s);
        loadTexture("submarine/torpedo.png"); // TODO MAKE TEXTURE AND ADD TO ASSETS
        setSize(200, 40); // TODO ? modify size
        setBoundaryRectangle();
        setOrigin(0, 10);
        setRotation(rotation);

        setAcceleration(125);
        setMaxSpeed(375);
        setDeceleration(0);

        float speed = 125;
        setSpeed(speed);
        velocityVector.rotate(rotation);
    }

    @Override
    public void act(float delta) {
        accelerateForward();
        super.act(delta);
        applyPhysics(delta);
    }
}
