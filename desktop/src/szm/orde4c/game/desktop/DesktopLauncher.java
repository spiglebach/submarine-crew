package szm.orde4c.game.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import szm.orde4c.game.SubmarineCrew;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowSizeLimits(1024, 768, 1920, 1080);
		config.setAutoIconify(true);

		new Lwjgl3Application(new SubmarineCrew(), config);
	}
}
