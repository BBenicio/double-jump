package io.benic.doublejump.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.TimeUtils;
import io.benic.doublejump.DoubleJump;
import io.benic.doublejump.actors.Money;
import io.benic.doublejump.actors.Shop;
import io.benic.doublejump.utils.Ease;
import io.benic.doublejump.utils.Prefs;

import java.util.Arrays;


public class MenuState extends State {
    private static final String LOG_TAG = "MenuState";

    private static final long BUTTON_PRESS_INTERVAL = 100;

    private final Sound click;
    private final Sound buy;
    private final Sound no;

    private Image ground;
    private Image play;
    private Label title;

    private Button soundButton;
    private Button leaderboardButton;
    private Button achievementButton;
    private Button shopButton;

    private Money money;

    private Shop shop;
    private long shopClick = -1;
    private boolean shopOpen;

    private float playWidth;
    private float titleY;

    private long startTime;
    private boolean done = false;
    private boolean going = false;

    private long buttonPressTime = 0;

    private Preferences preferences;

    private final ClickListener clickListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            // if (!going && !soundButton.isOver() && !achievementButton.isOver() && !leaderboardButton.isOver() && !shopButton.isOver()
            //         && !shopOpen && x < money.getX() && y > money.getY() + money.getHeight()) {
            if (!going && !shopOpen && y > shopButton.getY() + shopButton.getHeight() && y < soundButton.getY()) {
                going = true;
                StateManager.changeState(new GameplayState(assetManager));
            }
        }
    };

    private final Shop.ShopListener shopListener = new Shop.ShopListener() {
        @Override
        public void selected(int index) {
            preferences.putInteger(Prefs.SELECTED_KEY, index);

            preferences.flush();

            click.play(DoubleJump.sound ? 1.0f : 0);
        }

        @Override
        public boolean bought(int index, int cost) {
            if (cost <= money.getValue()) {
                Gdx.app.log(LOG_TAG, "item " + index + " bought");

                money.setValue(money.getValue() - cost);
                String[] unlockString = preferences.getString(Prefs.UNLOCKED_KEY, "0").split(" ");
                int[] unlocked = new int[unlockString.length + 1];
                for (int i = 1; i <= unlockString.length; i++) {
                    unlocked[i] = Integer.parseInt(unlockString[i - 1]);
                }
                unlocked[0] = index;
                Arrays.sort(unlocked);
                StringBuilder sb = new StringBuilder();
                for (int i : unlocked) {
                    sb.append(i);
                    sb.append(' ');
                }
                sb.deleteCharAt(sb.length() - 1);

                preferences.putString(Prefs.UNLOCKED_KEY, sb.toString());
                preferences.putInteger(Prefs.MONEY_KEY, money.getValue());

                buy.play(DoubleJump.sound ? 1.0f : 0);

                preferences.flush();

                return true;
            }

            no.play(DoubleJump.sound ? 1.0f : 0);

            return false;
        }

        @Override
        public void video(int value) {
            Gdx.app.log(LOG_TAG, "Watch a video for " + value);

            if (DoubleJump.rewardedAd != null) {
                DoubleJump.rewardedAd.playVideo();
            }

            // shop.setPosition(getWidth() / 2, getHeight() / 2);
        }
    };

    public MenuState(AssetManager assetManager) {
        super(assetManager);

        setClearColor(DoubleJump.whiteOnBlack ? Color.BLACK : Color.WHITE);

        no = assetManager.get("sound/no.wav");
        buy = assetManager.get("sound/buy.wav");
        click = assetManager.get("sound/click.wav");
    }

    @Override
    public void start() {
        Gdx.app.debug(LOG_TAG, "In menu");
        startTime = TimeUtils.millis();

        final Stage stage = getStage();
        final TextureAtlas atlas = assetManager.get("packed/pack.atlas");
        final I18NBundle bundle = assetManager.get("lang/strings");
        final BitmapFont big = assetManager.get("fonts/big.fnt");
        final BitmapFont small = assetManager.get("fonts/small.fnt");

        stage.addListener(clickListener);

        title = new Label(bundle.get("title"), new Label.LabelStyle(big, DoubleJump.whiteOnBlack ? Color.WHITE : Color.BLACK));
        title.setPosition((getWidth() - title.getWidth()) / 2, getHeight());
        titleY = getHeight() - title.getHeight() - 16;
        stage.addActor(title);

        play = new Image(atlas.findRegion(bundle.get("play_text_file")));
        play.setColor(DoubleJump.whiteOnBlack ? Color.WHITE : Color.BLACK);
        play.setPosition((getWidth() - play.getWidth()) / 2, 0);
        playWidth = play.getWidth();
        stage.addActor(play);

        ground = new Image(atlas.findRegion("box"));
        ground.setColor(DoubleJump.whiteOnBlack ? Color.WHITE : Color.BLACK);
        ground.setSize(getWidth(), 100);
        stage.addActor(ground);

        final Image soundOff = new Image(atlas.findRegion("sound_off"));
        final Image soundOn = new Image(atlas.findRegion("sound_on"));
        soundButton = new Button(soundOff.getDrawable(), null, soundOn.getDrawable());
        soundButton.setColor(DoubleJump.whiteOnBlack ? Color.WHITE : Color.BLACK);
        soundButton.setChecked(DoubleJump.sound);
        soundButton.setPosition(0, getHeight());
        stage.addActor(soundButton);

        leaderboardButton = new Button(new Image(atlas.findRegion("leaderboard")).getDrawable());
        leaderboardButton.setColor(DoubleJump.whiteOnBlack ? Color.WHITE : Color.BLACK);
        leaderboardButton.setPosition(getWidth() - leaderboardButton.getWidth(), getHeight());
        stage.addActor(leaderboardButton);

        achievementButton = new Button(new Image(atlas.findRegion("achievements")).getDrawable());
        achievementButton.setColor(DoubleJump.whiteOnBlack ? Color.WHITE : Color.BLACK);
        achievementButton.setPosition(getWidth() - leaderboardButton.getWidth() - 10 - achievementButton.getWidth(), getHeight());
        stage.addActor(achievementButton);

        shopButton = new Button(new Image(atlas.findRegion("shop")).getDrawable());
        shopButton.setColor(DoubleJump.whiteOnBlack ? Color.BLACK : Color.WHITE);
        shopButton.setPosition(5, -shopButton.getHeight());
        stage.addActor(shopButton);

        preferences = Prefs.getPreferences();

        String[] unlockString = preferences.getString(Prefs.UNLOCKED_KEY, "0").split(" ");
        int[] unlocked = new int[unlockString.length];
        for (int i = 0; i < unlockString.length; i++) {
            unlocked[i] = Integer.parseInt(unlockString[i]);
        }
        shop = new Shop(atlas, small, bundle, unlocked, shopListener, DoubleJump.rewardedAd != null && DoubleJump.adLoaded);
        shop.setSelected(preferences.getInteger(Prefs.SELECTED_KEY, 0));
        shop.setColor(DoubleJump.whiteOnBlack ? Color.WHITE : Color.BLACK);
        shop.setPosition(getWidth() / 2, -getHeight() / 2);
        shop.setVisible(true);
        stage.addActor(shop);
        shopOpen = false;

        money = new Money(small, atlas.findRegion("box"), preferences.getInteger(Prefs.MONEY_KEY, 0));
        money.setPosition(getWidth() - money.getWidth() - 5, -money.getHeight());
        money.setColor(DoubleJump.whiteOnBlack ? Color.BLACK : Color.WHITE);
        stage.addActor(money);
    }

    @Override
    public void update() {
        super.update();
        final long elapsed = TimeUtils.millis() - startTime;

        if (elapsed <= 750) {
            title.setY(Ease.bounceOut.apply(getHeight(), getHeight() - title.getHeight() - 16, elapsed / 750.0f));
            play.setY(Ease.elasticInOut.apply(0, ground.getHeight() + 10, Math.min(elapsed / 400.0f, 1.0f)));
            soundButton.setY(Ease.quadIn.apply(getHeight(), getHeight() - 24, Math.min(elapsed / 400.0f, 1.0f)));
            leaderboardButton.setY(Ease.quadIn.apply(getHeight(), getHeight() - 24, Math.min(elapsed / 400.0f, 1.0f)));
            achievementButton.setY(Ease.quadIn.apply(getHeight(), getHeight() - 24, Math.min(elapsed / 400.0f, 1.0f)));

            shopButton.setY(Ease.quadIn.apply(-shopButton.getHeight(), 5, Math.min(elapsed / 400.0f, 1.0f)));
            money.setY(Ease.quadIn.apply(-money.getHeight(), 5, Math.min(elapsed / 400.0f, 1.0f)));
        } else if (!done) {
            done = true;

            title.setY(getHeight() - title.getHeight() - 16);
            play.setY(ground.getHeight() + 10);
            soundButton.setY(getHeight() - 24);
            leaderboardButton.setY(getHeight() - 24);
            achievementButton.setY(getHeight() - 24);
        }

        if (soundButton.isChecked() && !DoubleJump.sound) {
            DoubleJump.sound = true;
            ((Music) assetManager.get("music/double_jump.ogg")).setVolume(0.6f);
            preferences.putBoolean(Prefs.SOUND_KEY, true);
        } else if (!soundButton.isChecked() && DoubleJump.sound) {
            DoubleJump.sound = false;
            ((Music) assetManager.get("music/double_jump.ogg")).setVolume(0);
            preferences.putBoolean(Prefs.SOUND_KEY, false);
        }
        if (TimeUtils.millis() - buttonPressTime >= BUTTON_PRESS_INTERVAL) {
            if (leaderboardButton.isPressed()) {
                buttonPressTime = TimeUtils.millis();

                click.play(DoubleJump.sound ? 1.0f : 0);

                Gdx.app.debug(LOG_TAG, "leaderboard pressed");
            }

            if (achievementButton.isPressed()) {
                buttonPressTime = TimeUtils.millis();

                click.play(DoubleJump.sound ? 1.0f : 0);

                Gdx.app.debug(LOG_TAG, "achievements pressed");
            }

            if (shopButton.isPressed() && shopClick == -1) {
                shopClick = buttonPressTime = TimeUtils.millis();

                click.play(DoubleJump.sound ? 1.0f : 0);

                Gdx.app.debug(LOG_TAG, "shop pressed");
                shopOpen = !shopOpen;

                if (shopOpen && DoubleJump.rewardedAd != null) {
                    shop.setVideoAvailable(DoubleJump.adLoaded);
                }
            }
        }

        if (TimeUtils.millis() - shopClick <= 400) {
            if (shopOpen) {
                shop.setY(Ease.quadIn.apply(-getHeight() / 2, getHeight() / 2, (TimeUtils.millis() - shopClick) / 400.0f));
            } else {
                shop.setY(Ease.quadIn.apply(getHeight() / 2, -getHeight() / 2, (TimeUtils.millis() - shopClick) / 400.0f));
            }
        } else if (shopClick != -1) {
            if (shopOpen) {
                shop.setY(getHeight() / 2);
            } else {
                shop.setY(-getHeight() / 2);
            }
            shopClick = -1;
        }
    }

    @Override
    public void transitionOut(float percentage) {
        play.setWidth(Ease.quadIn.apply(playWidth, 24, percentage));
        play.setX((getWidth() - play.getWidth()) / 2);

        soundButton.setY(Ease.quadIn.apply(getHeight() - 24, getHeight(), percentage));
        leaderboardButton.setY(Ease.quadIn.apply(getHeight() - 24, getHeight(), percentage));
        achievementButton.setY(Ease.quadIn.apply(getHeight() - 24, getHeight(), percentage));
        shopButton.setY(Ease.quadIn.apply(5, -shopButton.getHeight(), percentage));
        money.setY(Ease.quadIn.apply(5, -money.getHeight(), percentage));

        title.setY(Ease.quadIn.apply(titleY, getHeight(), percentage));
    }

    @Override
    public void resized() {
        title.setPosition((getWidth() - title.getWidth()) / 2, getHeight() - title.getHeight() - 16);
        titleY = title.getY();

        play.setPosition((getWidth() - play.getWidth()) / 2, ground.getHeight() + 10);

        soundButton.setY(getHeight() - 24);
        leaderboardButton.setPosition(getWidth() - leaderboardButton.getWidth(), getHeight() - 24);
        achievementButton.setPosition(getWidth() - leaderboardButton.getWidth() - 10 - achievementButton.getWidth(), getHeight() - 24);

        ground.setWidth(getWidth());

        money.setPosition(getWidth() - money.getWidth() - 5, 5);

        shop.setX(getWidth() / 2);
        if (shopOpen) {
            shop.setY(getHeight() / 2);
        }
    }

    public void reward() {
        money.setValue(money.getValue() + shop.getVideoValue());
        preferences.putInteger(Prefs.MONEY_KEY, money.getValue());

        preferences.flush();
    }
}
