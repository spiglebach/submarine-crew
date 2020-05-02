package szm.orde4c.game;

import com.badlogic.gdx.Gdx;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.screen.LoadGameScreen;
import szm.orde4c.game.screen.MainMenuScreen;
import szm.orde4c.game.screen.PlayerSelectionScreen;

public class SubmarineCrew extends BaseGame {
	@Override
	public void create () {
	    super.create();
		Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode(Gdx.graphics.getPrimaryMonitor()));
		setActiveScreen(new MainMenuScreen());
//		setActiveScreen(new LoadGameScreen());
	}
}
