package szm.orde4c.game.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.util.Assets;

import java.util.ArrayList;

public class ButtonIndicator extends BaseActor {
    public static final int CONTROLLER_FACE_BLANK = 0;
    public static final int CONTROLLER_FACE_EAST = 1;
    public static final int CONTROLLER_FACE_NORTH = 2;
    public static final int CONTROLLER_FACE_WEST = 3;
    public static final int CONTROLLER_FACE_SOUTH = 4;
    public static final int CONTROLLER_BACK = 5;
    public static final int CONTROLLER_START = 6;
    public static final int CONTROLLER_JOYSTICK_X = 7;
    public static final int CONTROLLER_JOYSTICK_Y = 8;

    public static final int KEYBOARD_E = 9;
    public static final int KEYBOARD_F = 10;
    public static final int KEYBOARD_DELETE = 11;
    public static final int KEYBOARD_AD = 12;
    public static final int KEYBOARD_WS = 13;
    public static final int PADLOCK = 14;

    private ArrayList<Animation> animations;
    private float size;

    public ButtonIndicator(float size, int animationIndex, Stage s) {
        super(0, 0, s);
        this.size = size;

        Animation faceBlank = loadTexture(Assets.instance.getTexture(Assets.BUTTON_CONTROLLER_FACE_BLANK));
        Animation faceEast = loadTexture(Assets.instance.getTexture(Assets.BUTTON_CONTROLLER_FACE_EAST));
        Animation faceNorth = loadTexture(Assets.instance.getTexture(Assets.BUTTON_CONTROLLER_FACE_NORTH));
        Animation faceWest = loadTexture(Assets.instance.getTexture(Assets.BUTTON_CONTROLLER_FACE_WEST));
        Animation faceSouth = loadTexture(Assets.instance.getTexture(Assets.BUTTON_CONTROLLER_FACE_SOUTH));
        Animation controllerBack = loadTexture(Assets.instance.getTexture(Assets.BUTTON_CONTROLLER_BACK));
        Animation controllerStart = loadTexture(Assets.instance.getTexture(Assets.BUTTON_CONTROLLER_START));
        Animation joystickX = loadAnimationFromSheet(Assets.instance.getTexture(Assets.BUTTON_CONTROLLER_JOYSTICK_X), 1, 2, 0.5f, true);
        Animation joystickY = loadAnimationFromSheet(Assets.instance.getTexture(Assets.BUTTON_CONTROLLER_JOYSTICK_Y), 1, 2, 0.5f, true);

        Animation keyboardE = loadTexture(Assets.instance.getTexture(Assets.BUTTON_KEYBOARD_E));
        Animation keyboardF = loadTexture(Assets.instance.getTexture(Assets.BUTTON_KEYBOARD_F));
        Animation keyboardDelete = loadTexture(Assets.instance.getTexture(Assets.BUTTON_KEYBOARD_DELETE));
        Animation keyboardAD = loadTexture(Assets.instance.getTexture(Assets.BUTTON_KEYBOARD_AD));
        Animation keyboardWS = loadTexture(Assets.instance.getTexture(Assets.BUTTON_KEYBOARD_WS));
        Animation padlock = loadTexture(Assets.instance.getTexture(Assets.BUTTON_PADLOCK));

        animations = new ArrayList<>();
        animations.add(faceBlank);
        animations.add(faceEast);
        animations.add(faceNorth);
        animations.add(faceWest);
        animations.add(faceSouth);
        animations.add(controllerBack);
        animations.add(controllerStart);
        animations.add(joystickX);
        animations.add(joystickY);

        animations.add(keyboardE);
        animations.add(keyboardF);
        animations.add(keyboardDelete);
        animations.add(keyboardAD);
        animations.add(keyboardWS);
        animations.add(padlock);

        setAnimation(animationIndex);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    public void setAnimation(int animation) {
        setAnimation(animations.get(animation));
        setSize(size, size);

    }
}
