package io.benic.doublejump;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.benic.doublejump.ads.RewardedAd;
import io.benic.doublejump.states.*;
import io.benic.doublejump.utils.Prefs;


public class DoubleJump extends ApplicationAdapter {
    public static final String NAME = "io.benic.doublejump";

    public static final int WIDTH = 640;
    public static final int HEIGHT = 400;

    public static boolean sound = true;
    public static boolean whiteOnBlack = true;

    public static RewardedAd rewardedAd = null;
    public static boolean adLoaded = false;

    private static final String LOG_TAG = "DoubleJump";
    private StateManager stateManager;
    private AssetManager assetManager;

    private Preferences preferences;

    private boolean processReward = false;

    private void enqueueLoad() {
        assetManager.load("packed/pack.atlas", TextureAtlas.class);

        assetManager.load("music/double_jump.ogg", Music.class);

        assetManager.load("sound/buy.wav", Sound.class);
        assetManager.load("sound/click.wav", Sound.class);
        assetManager.load("sound/game_over.wav", Sound.class);
        assetManager.load("sound/high_score.wav", Sound.class);
        assetManager.load("sound/hit.wav", Sound.class);
        assetManager.load("sound/jump.wav", Sound.class);
        assetManager.load("sound/no.wav", Sound.class);

        assetManager.load("lang/strings", I18NBundle.class);

        assetManager.load("fonts/small.fnt", BitmapFont.class);
        assetManager.load("fonts/medium.fnt", BitmapFont.class);
        assetManager.load("fonts/big.fnt", BitmapFont.class);

        ParticleEffectLoader.ParticleEffectParameter particleEffectParameter = new ParticleEffectLoader.ParticleEffectParameter();
        particleEffectParameter.atlasFile = "packed/pack.atlas";

        assetManager.load("ground_hit.particle", ParticleEffect.class, particleEffectParameter);
        assetManager.load("death.particle", ParticleEffect.class, particleEffectParameter);
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_INFO);

        assetManager = new AssetManager();
        enqueueLoad();

        stateManager = StateManager.getInstance();
        stateManager.setViewport(new ExtendViewport(WIDTH, HEIGHT));
        StateManager.changeState(new SplashState(assetManager));

        preferences = Prefs.getPreferences();
        DoubleJump.sound = preferences.getBoolean(Prefs.SOUND_KEY, Prefs.SOUND_DEFAULT);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stateManager.resize(width, height);
    }

    @Override
    public void render() {
        if (processReward) {
            State s = stateManager.getCurrentState();
            if (s instanceof MenuState) {
                ((MenuState) s).reward();
            } else if (s instanceof GameOverState) {
                ((GameOverState) s).reward();
            }

            processReward = false;
        }

        if (!stateManager.update()) {
            Gdx.app.log(LOG_TAG, "stateManager.update returned false. Quitting...");
            Gdx.app.exit();
        }

        stateManager.draw();
    }

    @Override
    public void dispose() {
        Gdx.app.log(LOG_TAG, "disposing");
        preferences.flush();

        stateManager.dispose();
        assetManager.dispose();
    }

    public void reward() {
        processReward = true;
    }

    public void videoLoaded(boolean loaded) {
        adLoaded = loaded;
    }
}
