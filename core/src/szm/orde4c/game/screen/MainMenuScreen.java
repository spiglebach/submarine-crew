package szm.orde4c.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.base.BaseGamepadScreen;
import szm.orde4c.game.service.SaveGameService;
import szm.orde4c.game.ui.MenuLabel;
import szm.orde4c.game.ui.PauseMenu;
import szm.orde4c.game.util.Save;

import java.util.ArrayList;

public class MainMenuScreen extends BaseGamepadScreen {
    private PauseMenu pauseMenu;

    @Override
    public void initialize() {
        ArrayList<MenuLabel> menuOptions = new ArrayList<>();

        if (SaveGameService.getLastSave() != null) {
            MenuLabel continueOption = new MenuLabel("Continue", BaseGame.largeLabelStyle) {
                @Override
                public void execute() {
                    removeMainControllerListener();
                    BaseGame.setActiveScreen(new LevelSelectorScreen(SaveGameService.getLastSave()));
                }
            };
            menuOptions.add(continueOption);
        }

        if (!SaveGameService.allSaveSlotsOccupied()) {
            MenuLabel newGame = new MenuLabel("New Game", BaseGame.largeLabelStyle) {
                @Override
                public void execute() {
                    removeMainControllerListener();
                    int firstUnoccupiedSaveId = SaveGameService.getFirstUnoccupiedSaveId();
                    Save newSave = new Save(firstUnoccupiedSaveId, 0);
                    SaveGameService.save(newSave);
                    BaseGame.setActiveScreen(new LevelSelectorScreen(newSave));
                }
            };
            menuOptions.add(newGame);
        }

        if (SaveGameService.hasAtLeastOneSave()) {
            MenuLabel loadGame = new MenuLabel("Load Game", BaseGame.largeLabelStyle) {
                @Override
                public void execute() {
                    removeMainControllerListener();
                    BaseGame.setActiveScreen(new LoadGameScreen());
                }
            };
            menuOptions.add(loadGame);
        }

        MenuLabel exit = new MenuLabel("Exit", BaseGame.largeLabelStyle) {
            @Override
            public void execute() {
                Gdx.app.exit();
            }
        };
        menuOptions.add(exit);

        pauseMenu = new PauseMenu(menuOptions, 1f, uiStage);
        uiTable.add(pauseMenu);

        try {
            Controllers.getControllers().first().addListener(pauseMenu);
        } catch (Exception e) {
            // No controllers attached!
        }
    }

    @Override
    public void connected(Controller controller) {
        if (Controllers.getControllers().size == 1) {
            controller.addListener(pauseMenu);
        }
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void show() {
        super.show();
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.addProcessor(pauseMenu);
    }

    @Override
    public void hide() {
        super.hide();
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        im.removeProcessor(pauseMenu);
    }

    private void removeMainControllerListener() {
        try {
            Controllers.getControllers().first().removeListener(pauseMenu);
        } catch (Exception e) {
            // No controller attached!
        }
    }


}
