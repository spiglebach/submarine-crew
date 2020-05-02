package szm.orde4c.game.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;

public class Torpedo extends BaseActor {
    public Torpedo(float x, float y, float rotation, Stage s) {
        super(x, y, s);
        loadTexture("platform.png"); // TODO add texture
        setSize(100, 20);
        setBoundaryRectangle();
        setOrigin(0, 10);
        setRotation(rotation);
        setColor(Color.RED);

        setAcceleration(500);
        setMaxSpeed(1500);
        setDeceleration(0);

        float speed = 500;
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
