package br.com.cidadeviva.desktop;

import br.com.cidadeviva.app.CityGame;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public final class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Cidade Viva - MVP Java");
        config.setWindowedMode(1080, 720);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new CityGame(), config);
    }
}
