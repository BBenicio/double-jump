package io.benic.doublejump.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.benic.doublejump.DoubleJump;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Double Jump");
        config.setWindowedMode(DoubleJump.WIDTH, DoubleJump.HEIGHT);
        new Lwjgl3Application(new DoubleJump(), config);
    }
}
