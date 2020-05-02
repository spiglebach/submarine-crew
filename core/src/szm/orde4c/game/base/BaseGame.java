package szm.orde4c.game.base;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;


public abstract class BaseGame extends Game {
    public static BaseGame game;
    public static TextButton.TextButtonStyle textButtonStyle;
    public static BitmapFont customFont;
    public static BitmapFont largeCustomFont;

    public static Label.LabelStyle labelStyle;
    public static Label.LabelStyle largeLabelStyle;

    public BaseGame() {
        game = this;
    }

    @Override
    public void create() {
        customFont = new BitmapFont(Gdx.files.internal("font/kristen.fnt"));
        largeCustomFont = new BitmapFont(Gdx.files.internal("font/kristen_large.fnt"));

        labelStyle = new Label.LabelStyle();
        labelStyle.font = BaseGame.customFont;

        largeLabelStyle = new Label.LabelStyle();
        largeLabelStyle.font = BaseGame.largeCustomFont;

        textButtonStyle = new TextButton.TextButtonStyle();
        Texture buttonTex = new Texture(Gdx.files.internal("button.png"));
        NinePatch buttonPatch = new NinePatch(buttonTex, 24, 24, 24, 24);
        textButtonStyle.up = new NinePatchDrawable(buttonPatch);
        textButtonStyle.font = customFont;
        textButtonStyle.fontColor = Color.ROYAL;
        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
    }

    public static void setActiveScreen(BaseScreen screen) {
        game.setScreen(screen);
    }
}
