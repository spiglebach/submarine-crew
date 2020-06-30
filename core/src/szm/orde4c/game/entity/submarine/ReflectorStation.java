package szm.orde4c.game.entity.submarine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.XBoxGamepad;
import szm.orde4c.game.util.Assets;

public class ReflectorStation extends Station {
    private static final float ACTIVE_REFLECTOR_COST = 2.5f;
    private BaseActor reflectorActor;
    private Animation inactiveLight;
    private Animation activeLight;
    private float reflectorRotation;

    public ReflectorStation(float x, float y, float width, float height, Submarine submarine, Stage s) {
        super(x, y, width, height, submarine, s);
        this.reflectorActor = submarine.getReflectorActor();
        reflectorActor.setRotationSpeed(30);
        activeLight = reflectorActor.loadTexture(Assets.instance.getTexture(Assets.SUBMARINE_REFLECTOR_LIGHT_ACTIVE));
        inactiveLight = reflectorActor.loadTexture(Assets.instance.getTexture(Assets.SUBMARINE_REFLECTOR_LIGHT_INACTIVE));
        reflectorActor.setAnimation(inactiveLight);
        reflectorRotation = 0;
    }

    @Override
    public void operate() {
        Controller controller = getOperatingPlayer().getController();
        float xAxis = 0;
        float yAxis = 0;
        if (controller == null) {
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                xAxis = 1;
            } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                xAxis = -1;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                yAxis = 1;
            } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                yAxis = -1;
            }
        } else {
            xAxis = controller.getAxis(XBoxGamepad.AXIS_LEFT_X);
            yAxis = -controller.getAxis(XBoxGamepad.AXIS_LEFT_Y);
        }

        Vector2 controlVector = new Vector2(xAxis, yAxis);
        controlVector.rotate(-reflectorActor.getRotation());

        float rotationControlAmount = Math.abs(controlVector.y);
        float deadZone = 0.1f;

        if (rotationControlAmount > deadZone) {
            if (controlVector.y > 0) {
                reflectorActor.rotate(1);
            } else {
                reflectorActor.rotate(-1);
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        reflectorActor.setRotation(reflectorRotation);
        reflectorActor.applyRotation(delta);
        reflectorRotation = reflectorActor.getRotation();
        reflectorActor.setRotation(reflectorActor.getRotation() + submarine.getRotation());
        if (activated) {
            reflectorActor.setAnimation(activeLight);
        } else {
            reflectorActor.setAnimation(inactiveLight);
        }
        reflectorActor.centerAtActor(submarine);
    }

    @Override
    public void continiousEnergyConsumption(float delta) {
        if (activated && submarine.getEnergyPercent() >= 0.1f) {
            submarine.decreaseEnergy(ACTIVE_REFLECTOR_COST * delta);
        } else {
            activated = false;
        }
    }
}
