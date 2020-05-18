package szm.orde4c.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import szm.orde4c.game.base.BaseActor;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.event.CountdownFinishedEvent;
import szm.orde4c.game.util.Assets;

public class CountdownDisplay extends BaseActor {
    private final float COUNTDOWN_TIME;
    private float timeLeft;
    private Label timeLabel;
    private boolean countingDown;

    public CountdownDisplay(float countdownTime, Stage s) {
        super(0, 0, s);
        COUNTDOWN_TIME = countdownTime;
        timeLeft = COUNTDOWN_TIME;
        countingDown = false;
        loadTexture(Assets.instance.getTexture(Assets.BLANK));
        setColor(Color.BLACK);
        setOpacity(0.5f);
        setSize(s.getWidth(), s.getHeight());

        Table table = new Table();
        table.setFillParent(true);

        timeLabel = new Label(String.valueOf(timeLeft), BaseGame.labelStyle);
        table.add(timeLabel).expand();
        addActor(table);
        setVisible(false);
    }

    public void countdown() {
        if (!countingDown) {
            countingDown = true;
            setVisible(true);
        }
    }

    public void stop() {
        countingDown = false;
        timeLeft = COUNTDOWN_TIME;
        setVisible(false);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (countingDown) {
            timeLeft -= delta;
            if (timeLeft <= 0) {
                timeLeft = 0;
                fire(new CountdownFinishedEvent());
            }
            timeLabel.setText(String.valueOf(Math.ceil(timeLeft)));
        }
    }
}
