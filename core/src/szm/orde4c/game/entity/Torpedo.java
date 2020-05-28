package szm.orde4c.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.effect.ExplosionEffect;

public class Torpedo extends BaseActor {
    public Torpedo(float x, float y, float rotation, Stage s) {
        super(x, y, s);
        loadTexture("submarine/torpedo.png"); // TODO MAKE TEXTURE AND ADD TO ASSETS
        setSize(50, 15);
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

    public void explode() {
        BaseActor explosionActor = new BaseActor(0, 0, getStage());
        explosionActor.setSize(100, 100);
        explosionActor.setBoundaryPolygon(10);
        explosionActor.setVisible(false);
        Vector2 rotatedTorpedoEnd = new Vector2(getWidth(), getHeight() / 2.0f).rotate(getRotation());
        rotatedTorpedoEnd.add(getX(), getY());
        explosionActor.centerAtPosition(rotatedTorpedoEnd.x, rotatedTorpedoEnd.y);

        for (BaseActor otherEnvironmentActor : BaseActor.getList(getStage(), "szm.orde4c.game.entity.Damageable")) {
            if (explosionActor.overlaps(otherEnvironmentActor)) {
                Damageable damageable = (Damageable) otherEnvironmentActor;
                if (damageable instanceof Enemy) {
                    damageable.damage(300);
                } else {
                    damageable.damage(20);
                }
            }
        }
        ExplosionEffect explosionEffect = new ExplosionEffect();
        explosionEffect.setSize(100, 100);
        explosionEffect.centerAtPosition(rotatedTorpedoEnd.x, rotatedTorpedoEnd.y);
        getStage().addActor(explosionEffect);
        explosionEffect.start();
        addAction(Actions.removeActor());
        explosionActor.addAction(Actions.after(Actions.removeActor()));
    }
}
