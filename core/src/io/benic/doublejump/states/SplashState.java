package io.benic.doublejump.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.TimeUtils;
import io.benic.doublejump.DoubleJump;


public class SplashState extends State {
    private static final String LOG_TAG = "SplashState";
    private static final long MINIMUM_TIME = 500;
    
    private long startAt = 0;
    private final Texture splashTexture;
    private Image splashImage;
    
    private boolean loaded = false;
    
    public SplashState(AssetManager assetManager) {
        super(assetManager);
        Gdx.app.debug(LOG_TAG, "Loading splash texture");
        splashTexture = new Texture("splash.png");
        
        setClearColor(Color.BLACK);
    }
    
    @Override
    public void start() {
        startAt = TimeUtils.millis();
        Gdx.app.debug(LOG_TAG, "Starting");
    
        splashImage = new Image(splashTexture);
        getStage().addActor(splashImage);
    }
    
    @Override
    public void update() {
        super.update();
        
        final long elapsed = TimeUtils.millis() - startAt;
    
        if (!loaded && assetManager.update()) {
            loaded = true;
            Gdx.app.log(LOG_TAG, "Loaded all assets in " + Long.toString(elapsed) + " ms");
        }
        
        if (loaded && elapsed > MINIMUM_TIME) {
            Gdx.app.debug(LOG_TAG,"Changing to MenuState");
            final Music music = assetManager.get("music/double_jump.ogg");
            music.setLooping(true);
            music.setVolume(DoubleJump.sound ? 0.6f : 0);
            music.play();
            StateManager.changeState(new MenuState(assetManager));
        }
    }
    
    @Override
    public void resized() {
        splashImage.setPosition((getWidth() - splashImage.getWidth()) / 2, (getHeight() - splashImage.getHeight()) / 2);
    }
    
    @Override
    public void transitionOut(float percentage) {
        splashImage.setColor(1, 1, 1, 1 - percentage);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        
        Gdx.app.debug(LOG_TAG, "Disposing of splash texture");
    
        splashTexture.dispose();
    }
}
