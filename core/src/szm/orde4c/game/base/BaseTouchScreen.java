package szm.orde4c.game.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;

public abstract class BaseTouchScreen extends BaseScreen {
    protected Stage controlStage;
    protected Table controlTable;

    public void initializeControlArea() {
        controlStage = new Stage(new FitViewport(800, 800));
        controlTable = new Table();
        controlTable.setFillParent(true);
        controlStage.addActor(controlTable);
    }

    @Override
    public void show() {
        super.show();
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.addProcessor(controlStage);
    }

    @Override
    public void hide() {
        super.hide();
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(controlStage);
    }

    @Override
    public void render(float dt) {
        // act methods
        uiStage.act(dt);
        mainStage.act(dt);
        controlStage.act(dt);
        // defined by user
        update(dt);
        // clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // set the drawing regions and draw the graphics
        Gdx.gl.glViewport(0, 200, 800, 600);
        mainStage.draw();
        uiStage.draw();
        Gdx.gl.glViewport(0, 0, 800, 800);
        controlStage.draw();
    }
}
