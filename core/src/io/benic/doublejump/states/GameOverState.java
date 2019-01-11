package io.benic.doublejump.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.TimeUtils;
import io.benic.doublejump.DoubleJump;
import io.benic.doublejump.actors.Money;
import io.benic.doublejump.games.PlayGamesListener;
import io.benic.doublejump.utils.Ease;
import io.benic.doublejump.utils.GameInfo;
import io.benic.doublejump.utils.Prefs;


public class GameOverState extends State {
    private static final String LOG_TAG = "GameOverState";
    private static final float BUTTON_HEIGHT = 52.0f;
    private static final float BUTTON_IMAGE_Y = BUTTON_HEIGHT / 2.0f - 48.0f / 2.0f;

    private Image gameOver;
    private Image ground;

    private Label scoreLabel;
    private Image highScore;

//    private TextButton menuButton;
    private Button menuButton;
    private Image continueImage;
    private TextButton continueButton;
    private Image restartImage;
    private TextButton restartButton;

    private Money money;

    private float highScoreTime = -1;

    private long startTime;
    private long buttonStartTime = -1;
    private boolean done;

    private GameInfo gameInfo;
    private Preferences preferences;

    private boolean menu = false;
    private boolean continuing = false;
    private boolean continuePressed = false;
    private boolean restarting = false;
    private float continueButtonX;
    private float restartButtonX;

    public GameOverState(AssetManager assetManager, GameInfo gameInfo) {
        super(assetManager);

        setClearColor(DoubleJump.whiteOnBlack ? Color.BLACK : Color.WHITE);

        this.gameInfo = gameInfo;
    }

    @Override
    public void start() {
        final Stage stage = getStage();
        final Color bgColor = getClearColor();
        final Color fgColor = new Color(1 - bgColor.r, 1 - bgColor.g, 1 - bgColor.b, 1);
        final TextureAtlas atlas = assetManager.get("packed/pack.atlas");
        final I18NBundle bundle = assetManager.get("lang/strings");
        final BitmapFont small = assetManager.get("fonts/small.fnt");

        startTime = TimeUtils.millis();

        Gdx.app.debug(LOG_TAG, "In GameOver");

        gameOver = new Image(atlas.findRegion("game_over"));
        gameOver.setPosition((getWidth() - gameOver.getWidth()) / 2, getHeight());
        gameOver.setColor(fgColor);
        stage.addActor(gameOver);

        scoreLabel = new Label(bundle.format("score", gameInfo.getScore()),
                               new Label.LabelStyle(small, fgColor));
        scoreLabel.setPosition(-scoreLabel.getWidth(), getHeight() - 136 - scoreLabel.getHeight());
        stage.addActor(scoreLabel);

        preferences = Prefs.getPreferences();

        if (gameInfo.getScore() > preferences.getInteger(Prefs.SCORE_KEY, 0)) {
            highScore = new Image(atlas.findRegion("high_score"));
            highScore.setColor(fgColor);
            highScore.setPosition(getWidth() - 100 - highScore.getWidth() / 2, scoreLabel.getY() - highScore.getHeight() / 2);
            highScore.setOrigin(highScore.getWidth() / 2, highScore.getHeight());
            highScore.setRotation(45.0f);
            highScore.setScale(0);
            ((Sound) assetManager.get("sound/high_score.wav")).play(DoubleJump.sound ? 1.0f : 0);
            stage.addActor(highScore);

            preferences.putInteger(Prefs.SCORE_KEY, gameInfo.getScore());
        }

        int moneyAmount = preferences.getInteger(Prefs.MONEY_KEY, 0) + gameInfo.getScore();
        preferences.putInteger(Prefs.MONEY_KEY, moneyAmount);

        preferences.flush();

        final NinePatchDrawable buttonBg = new NinePatchDrawable(atlas.createPatch("background"));
        final NinePatchDrawable buttonDownBg = new NinePatchDrawable(atlas.createPatch("background_down"));
//        final Image buttonBg = new Image(atlas.findRegion("button_bg"));
//        final Image buttonDownBg = new Image(atlas.findRegion("button_down_bg"));
        final TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(buttonBg, buttonDownBg, null,
                                                                                small);
        style.fontColor = fgColor;

        continueButton = new TextButton(bundle.get("continue"), style);
        continueButton.setTransform(true);
        continueButton.setWidth(getWidth() / 3.0f);
        continueButton.setHeight(BUTTON_HEIGHT);
        continueButton.setOrigin(continueButton.getWidth() / 2, continueButton.getHeight() / 2);
        continueButton.setColor(fgColor);
//        continueButton.setPosition((getWidth() - continueButton.getWidth()) / 2, 0);
        continueButton.setPosition(10, -continueButton.getHeight());
        continueButtonX = continueButton.getX();
        if (gameInfo.isContinued() || !DoubleJump.adLoaded) {
            continueButton.setDisabled(true);
            continueButton.setVisible(false);
        }
        stage.addActor(continueButton);

        continueImage = new Image(atlas.findRegion("video"));
        continueImage.setPosition(4, BUTTON_IMAGE_Y);
        continueImage.setColor(fgColor);
        continueButton.addActor(continueImage);
//        stage.addActor(continueImage);

//        menuButton = new TextButton(bundle.get("menu"), style);
        final Image menuImage = new Image(atlas.findRegion("home"));
        menuButton = new Button(menuImage.getDrawable());
//        menuButton.setOrigin(menuButton.getWidth() / 2, menuButton.getHeight() / 2);
        menuButton.setColor(fgColor);
//        menuButton.setPosition(continueButton.getX() - menuButton.getWidth() - (getWidth() / 64), 0);
        menuButton.setPosition(0, getHeight());
        stage.addActor(menuButton);

        restartButton = new TextButton(bundle.get("restart"), style);
        restartButton.setTransform(true);
        restartButton.setWidth(getWidth() / 3.0f);
        restartButton.setHeight(BUTTON_HEIGHT);
        restartButton.setOrigin(restartButton.getWidth() / 2, restartButton.getHeight() / 2);
        restartButton.setColor(fgColor);
//        restartButton.setPosition(continueButton.getRight() + (getWidth() / 64), 0);
        restartButton.setPosition(getWidth() - restartButton.getWidth() - 10, -restartButton.getHeight());
        restartButtonX = restartButton.getX();
        stage.addActor(restartButton);

        restartImage = new Image(atlas.findRegion("retry"));
        restartImage.setPosition(4, BUTTON_IMAGE_Y);
        restartImage.setColor(fgColor);
        restartButton.addActor(restartImage);
//        stage.addActor(restartImage);

        ground = new Image(atlas.findRegion("box"));
        ground.setColor(1 - bgColor.r, 1 - bgColor.g, 1 - bgColor.b, 1);
        ground.setSize(getWidth(), 100);
        stage.addActor(ground);

        // money = new Money(small, atlas.findRegion("box"), preferences.getInteger(Prefs.MONEY_KEY, 0));
        money = new Money(small, atlas.findRegion("box"), moneyAmount);
        money.setPosition(getWidth() - money.getWidth() - 5, -money.getHeight());
        money.setColor(DoubleJump.whiteOnBlack ? Color.BLACK : Color.WHITE);
        stage.addActor(money);

        int[] skinsPlayed = Prefs.getArray(Prefs.SKINS_PLAYED_KEY, Prefs.SKINS_PLAYED_DEFAULT);
        skinsPlayed[gameInfo.getPlayerImage()]++;
        Prefs.putArray(Prefs.SKINS_PLAYED_KEY, skinsPlayed);

        if (DoubleJump.playGames != null && DoubleJump.playGames.isSignedIn()) {
            DoubleJump.playGames.submitScore(gameInfo.getScore());

            if (gameInfo.getScore() == 0) {
                DoubleJump.playGames.unlockAchievement(PlayGamesListener.THAT_WAS_QUICK);
            }

            if (gameInfo.getDoubleJumps() >= 10) {
                DoubleJump.playGames.unlockAchievement(PlayGamesListener.TWO_JUMPS);
            }

            if (gameInfo.getScore() >= 20 && gameInfo.getDoubleJumps() == gameInfo.getJumps()) {
                DoubleJump.playGames.unlockAchievement(PlayGamesListener.DOUBLE_JUMPER);
            }

            boolean chameleon = true;
            boolean favorite = false;
            for (int p : skinsPlayed) {
                if (p < 10) {
                    chameleon = false;
                }
                if (p > 100) {
                    favorite = true;
                }
            }

            if (chameleon) {
                DoubleJump.playGames.unlockAchievement(PlayGamesListener.CHAMELEON);
            }

            if (favorite) {
                DoubleJump.playGames.unlockAchievement(PlayGamesListener.WE_HAVE_A_WINNER);
            }
        }
    }

    @Override
    public void update() {
        super.update();

        final long now = TimeUtils.millis();
        final long elapsed = now - startTime;

        if (elapsed <= 750) {
            gameOver.setY(Ease.bounceOut.apply(getHeight(), getHeight() - gameOver.getHeight() - 16, elapsed / 750.0f));
            scoreLabel.setX(Ease.bounceOut.apply(-scoreLabel.getWidth(), getWidth() - 100 - scoreLabel.getWidth(), elapsed / 750.0f));
            money.setY(Ease.quadIn.apply(-money.getHeight(), 5, Math.min(elapsed / 400.0f, 1.0f)));
            /*play.setY(Ease.elasticInOut.apply(0, ground.getHeight() + 10, Math.min(elapsed / 400.0f, 1.0f)));
            soundButton.setY(Ease.quadIn.apply(getHeight(), getHeight() - 24, Math.min(elapsed / 400.0f, 1.0f)));
            leaderboardButton.setY(Ease.quadIn.apply(getHeight(), getHeight() - 24, Math.min(elapsed / 400.0f, 1.0f)));
            achievementButton.setY(Ease.quadIn.apply(getHeight(), getHeight() - 24, Math.min(elapsed / 400.0f, 1.0f)));*/
        } else if (!done) {
            done = true;
            buttonStartTime = now;

            gameOver.setY(getHeight() - gameOver.getHeight() - 16);
            scoreLabel.setX(getWidth() - 100 - scoreLabel.getWidth());
            /*play.setY(ground.getHeight() + 10);
            soundButton.setY(getHeight() - 24);
            leaderboardButton.setY(getHeight() - 24);
            achievementButton.setY(getHeight() - 24);*/
        }

        if (buttonStartTime != -1) {
            if (now - buttonStartTime <= 400) {
                continueButton.setY(Ease.elasticInOut.apply(-continueButton.getHeight(), ground.getHeight() + 10, Math.min((now - buttonStartTime) / 400.0f, 1.0f)));
                menuButton.setY(Ease.quadIn.apply(getHeight(), getHeight() - menuButton.getHeight(), Math.min((now - buttonStartTime) / 400.0f, 1.0f)));
                restartButton.setY(Ease.elasticInOut.apply(-restartButton.getHeight(), ground.getHeight() + 10, Math.min((now - buttonStartTime) / 400.0f, 1.0f)));
            } else {
                continueButton.setY(ground.getHeight() + 10);
                menuButton.setY(getHeight() - menuButton.getHeight());
                restartButton.setY(ground.getHeight() + 10);
                buttonStartTime = -1;
            }
        }

        if (highScore != null) {
            if (elapsed <= 1000) {
                highScore.setScale(Ease.expoIn.apply(0, 1, elapsed / 1000.0f));
                highScore.setRotation(Ease.expoIn.apply(-1080 + 45, 45, elapsed / 1000.0f));
            } else if (highScoreTime == -1) {
                highScore.setRotation(45);
                highScore.setScale(1);
                highScoreTime = 0;
            } else {
                highScoreTime += Gdx.graphics.getDeltaTime();
                if (highScoreTime <= 1) {
                    highScore.setScale(Ease.backInOut.apply(1, 1.1f, highScoreTime));
                } else if (highScoreTime <= 2) {
                    highScore.setScale(Ease.backInOut.apply(1.1f, 1, highScoreTime - 1));
                } else {
                    highScoreTime = 0;
                }
            }
        }

        if (!menu && !continuing && !restarting && menuButton.isPressed()) {
            menu = true;
            continuing = false;
            restarting = false;
            StateManager.changeState(new MenuState(assetManager));
        } else if (!menu && !continuing && !restarting && restartButton.isPressed()) {
//            final String text = ((I18NBundle) assetManager.get("lang/strings")).get("restart").substring(0, 1);
//            restartButton.setText(text);

            menu = false;
            continuing = false;
            restarting = true;
            StateManager.changeState(new GameplayState(assetManager));
        } else if (!menu && !continuing && !restarting && !continuePressed && continueButton.isPressed()) {
//            final String text = ((I18NBundle) assetManager.get("lang/strings")).get("continue").substring(0, 1);
//            continueButton.setText(text);

            menu = false;
            continuing = false;
            restarting = false;
            continuePressed = true;
            DoubleJump.rewardedAd.playVideo();
        }
    }

    @Override
    public void transitionOut(float percentage) {
        gameOver.setY(Ease.quadIn.apply(getHeight() - gameOver.getHeight() - 16, getHeight(), percentage));
        scoreLabel.setX(Ease.quadIn.apply(getWidth() - 100 - scoreLabel.getWidth(), getWidth(), percentage));
        if (highScore != null) {
            highScore.setScale(Ease.expoIn.apply(1, 0, percentage));
        }

        menuButton.setY(Ease.quadIn.apply(getHeight() - menuButton.getHeight(), getHeight(), percentage));
        if (menu) {
            continueButton.setY(Ease.quadIn.apply(ground.getHeight() + 10, -continueButton.getHeight(), percentage));
            restartButton.setY(Ease.quadIn.apply(ground.getHeight() + 10, -restartButton.getHeight(), percentage));
        } else if (restarting) {
            final float scaleX = 24.0f / restartButton.getWidth();
            final float scaleY = 24.0f / restartButton.getHeight();
            continueButton.setY(Ease.quadIn.apply(ground.getHeight() + 10, -continueButton.getHeight(), percentage));
//            restartButton.setWidth(Ease.quadIn.apply(getWidth() / 3.0f, 24, percentage));
//            restartButton.setHeight(Ease.quadIn.apply(64, 24, percentage));
            restartButton.setScaleX(Ease.quadIn.apply(1, scaleX, percentage));
            restartButton.setScaleY(Ease.quadIn.apply(1, scaleY, percentage));
            restartButton.setX(Ease.quadOut.apply(restartButtonX, (getWidth() - restartButton.getWidth()) / 2, percentage));
            restartImage.setX(Ease.quadIn.apply(4, 0, percentage));
            restartImage.setY(Ease.quadIn.apply(BUTTON_IMAGE_Y, 0, percentage));
        } else if (continuing) {
            final float scaleX = 24.0f / continueButton.getWidth();
            final float scaleY = 24.0f / continueButton.getHeight();
            restartButton.setY(Ease.quadIn.apply(ground.getHeight() + 10, -restartButton.getHeight(), percentage));
//            continueButton.setWidth(Ease.quadIn.apply(getWidth() / 3.0f, 24, percentage));
//            continueButton.setHeight(Ease.quadIn.apply(64, 24, percentage));
            continueButton.setScaleX(Ease.quadIn.apply(1, scaleX, percentage));
            continueButton.setScaleY(Ease.quadIn.apply(1, scaleY, percentage));
            continueButton.setX(Ease.quadOut.apply(continueButtonX, (getWidth() - continueButton.getWidth()) / 2, percentage));
            continueImage.setX(Ease.quadIn.apply(4, 0, percentage));
            continueImage.setY(Ease.quadIn.apply(BUTTON_IMAGE_Y, 0, percentage));
        }

        money.setY(Ease.quadIn.apply(5, -money.getHeight(), percentage));
    }

    @Override
    public void resized() {
        gameOver.setPosition((getWidth() - getWidth()) / 2, getHeight() - gameOver.getHeight() - 16);
        scoreLabel.setPosition(getWidth() - 100 - scoreLabel.getWidth(), getHeight() - 136 - scoreLabel.getHeight());
        ground.setWidth(getWidth());

        if (highScore != null) {
            highScore.setPosition(getWidth() - 100 - highScore.getWidth() / 2, scoreLabel.getY() - highScore.getHeight() / 2);
        }

        continueButton.setWidth(getWidth() / 3);
        restartButton.setWidth(getWidth() / 3);
        restartButton.setX(getWidth() - restartButton.getWidth() - 10);

        money.setPosition(getWidth() - money.getWidth() - 5, 5);
    }

    public void reward() {
        Gdx.app.log(LOG_TAG, "Rewarded, continuing...");

        menu = false;
        continuing = true;
        restarting = false;
        gameInfo.setContinued(true);

        int money = preferences.getInteger(Prefs.MONEY_KEY) - gameInfo.getScore();
        preferences.putInteger(Prefs.MONEY_KEY, money);

        preferences.flush();

        StateManager.changeState(new GameplayState(assetManager, gameInfo));
    }
}
