package szm.orde4c.game.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.util.CustomActions;

import java.util.ArrayList;

public class ButtonIndicator extends BaseActor {
    public static final int CONTROLLER_FACE_BUTTON_BASE = 0;
    public static final int CONTROLLER_FACE_BUTTON_NORTH = 1;
    public static final int CONTROLLER_FACE_BUTTON_EAST = 2;
    public static final int CONTROLLER_FACE_BUTTON_SOUTH = 3;
    public static final int CONTROLLER_FACE_BUTTON_WEST = 4;
    public static final int CONTROLLER_BUTTON_BACK = 5;
    public static final int CONTROLLER_BUTTON_START = 6;

    public static final int KEYBOARD_BUTTON_ENTER = 7;
    public static final int KEYBOARD_BUTTON_BACKSPACE = 8;
    public static final int KEYBOARD_BUTTON_W = 9;
    public static final int KEYBOARD_BUTTON_S = 10;
    public static final int KEYBOARD_BUTTON_A = 11;
    public static final int KEYBOARD_BUTTON_D = 12;
    public static final int KEYBOARD_BUTTON_E = 13;
    public static final int KEYBOARD_BUTTON_F = 14;
    public static final int KEYBOARD_BUTTON_DELETE = 15;
    public static final int PADLOCK = 16;

    private ArrayList<Animation> animations;
    private float size;

    public ButtonIndicator(float size, int animationIndex, Stage s) {
        super(0, 0, s);
        this.size = size;

        Animation faceButtonBase = loadTexture("button/button_display_foreground.png");
        Animation faceButtonhighlightNorth = loadTexture("button/highlight_north.png");
        Animation faceButtonHighlightEast = loadTexture("button/controller_buttons_east_red.png");
        Animation faceButtonHighlightSouth = loadTexture("button/highlight_south.png");
        Animation faceButtonHighlightWest = loadTexture("button/highlight_west.png");
        Animation controllerButtonBack = loadTexture("button/controller_button_back.png");
        Animation controllerButtonStart = loadTexture("button/controller_button_start.png");
        Animation keyBoardEnterButton = loadTexture("button/enter_button.png");
        Animation keyboardBackspaceButton = loadTexture("button/backspace.png");
        Animation keyboardWButton = loadTexture("button/keyboard_button_w.png");
        Animation keyboardSButton = loadTexture("button/keyboard_button_s.png");
        Animation keyboardAButton = loadTexture("button/keyboard_button_a.png");
        Animation keyboardDButton = loadTexture("button/keyboard_button_d.png");
        Animation keyboardEButton = loadTexture("button/keyboard_button_e.png");
        Animation keyboardFButton = loadTexture("button/keyboard_button_f.png");
        Animation keyboardDeleteButton = loadTexture("button/keyboard_button_delete.png");
        Animation padlock = loadTexture("button/padlock.png");

        animations = new ArrayList<>();
        animations.add(faceButtonBase);
        animations.add(faceButtonhighlightNorth);
        animations.add(faceButtonHighlightEast);
        animations.add(faceButtonHighlightSouth);
        animations.add(faceButtonHighlightWest);
        animations.add(controllerButtonBack);
        animations.add(controllerButtonStart);

        animations.add(keyBoardEnterButton);
        animations.add(keyboardBackspaceButton);
        animations.add(keyboardWButton);
        animations.add(keyboardSButton);
        animations.add(keyboardAButton);
        animations.add(keyboardDButton);
        animations.add(keyboardEButton);
        animations.add(keyboardFButton);
        animations.add(keyboardDeleteButton);
        animations.add(padlock);

        setAnimation(animationIndex);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    public void setAnimation(int animation) {
        setAnimation(animations.get(animation));
        setSize(size ,size);

    }
}
