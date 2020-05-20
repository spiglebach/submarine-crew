package szm.orde4c.game.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.util.Assets;

public class Projectile extends BaseActor {
    public Projectile(float x, float y, float angle, Stage s) {
        super(x, y, s);
        loadTexture(Assets.instance.getTexture(Assets.BLANK_CIRCLE));
        setSize(25, 25);
        setBoundaryPolygon(10);
        setColor(Color.TAN);

        setAcceleration(0);
        setDeceleration(0);
        setSpeed(125);

        setRotation(angle);
        velocityVector.rotate(angle);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        applyPhysics(delta);
    }
}
