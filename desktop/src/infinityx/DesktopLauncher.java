package infinityx;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import infinityx.lunarhaze.GDXRoot;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		//config.setForegroundFPS(60);
		//config.setWindowedMode(1024, 576);
		config.useVsync(true);
		config.setTitle("Lunar Haze");
		config.setResizable(true);
		// For some reason getting screen tear on linux if I don't start fullscreen
		config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
		new Lwjgl3Application(new GDXRoot(), config);
	}
}
