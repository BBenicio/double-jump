package io.benic.doublejump.desktop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter;


public class FontWriter extends ApplicationAdapter {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font Writer");
        config.setWindowedMode(100, 100);
        new Lwjgl3Application(new FontWriter(), config);
    }
    
    private FreeTypeFontGenerator generator;
    private BitmapFontWriter.FontInfo info;
    
    @Override
    public void create() {
        Gdx.app.log("Setup", "begin");
        info = new BitmapFontWriter.FontInfo();
        info.padding = new BitmapFontWriter.Padding(1, 1, 1, 1);
        
        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/nokiafc22.ttf"));
        writeFont("small", 16, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", 128, 128);
        writeFont("medium", 24, "ABCDEFGHIJKLMNOPQRSTYVWXYZ", 128, 128);
        writeFont("big", 64, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", 512, 256);
        
        Gdx.app.log("Setup", "end");
        
        Gdx.app.exit();
    }
    
    @Override
    public void dispose() {
        generator.dispose();
    }
    
    private void writeFont(String name, int size, String chars, int pageWidth, int pageHeight) {
        Gdx.app.log("Setup", "writing " + name);
        
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        
        parameter.size = size;
        parameter.characters = chars;
        parameter.color = Color.WHITE;
        parameter.packer = new PixmapPacker(pageWidth, pageHeight, Pixmap.Format.RGBA8888, 2, false, new PixmapPacker.SkylineStrategy());
        
        FreeTypeFontGenerator.FreeTypeBitmapFontData data = generator.generateData(parameter);
        BitmapFontWriter.writeFont(data, new String[] { name + ".png" }, Gdx.files.absolute("fonts/" + name + ".fnt"), info, pageWidth, pageHeight);
        BitmapFontWriter.writePixmaps(parameter.packer.getPages(), Gdx.files.absolute("fonts"), name);
        
        Gdx.app.log("Setup", "done writing " + name);
    }
}
