package szm.orde4c.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import szm.orde4c.game.base.BaseGame;
import szm.orde4c.game.screen.*;
import szm.orde4c.game.service.SaveGameService;
import szm.orde4c.game.util.Assets;
import szm.orde4c.game.util.ControlType;
import szm.orde4c.game.util.PlayerInfo;

public class SubmarineCrew extends BaseGame {
	@Override
	public void create () {
	    super.create();
		Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode(Gdx.graphics.getPrimaryMonitor()));
		setActiveScreen(new LoadingScreen());
	}
}
