package io.benic.doublejump.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.TimeUtils;
import io.benic.doublejump.DoubleJump;
import io.benic.doublejump.actors.ObstacleManager;
import io.benic.doublejump.actors.Particle;
import io.benic.doublejump.actors.Player;
import io.benic.doublejump.utils.Ease;
import io.benic.doublejump.utils.GameInfo;
import io.benic.doublejump.utils.Prefs;


public class GameplayState extends State {
    private static final String LOG_TAG = "GameplayState";

    private static final long COLOR_CHANGE_INTERVAL = 20000;
    // public static final float TUTORIAL_FIRST_PAUSE = 5.280f;
    // public static final float TUTORIAL_SECOND_PAUSE = 5.750f;
    public static final float TUTORIAL_FIRST_PAUSE = 2.780f;
    public static final float TUTORIAL_SECOND_PAUSE = 3.250f;

    private Image ground;
    private Player player;
    private Image pausedLabel;

    private Button pauseButton;

    private Particle groundHit;
    private Particle death;

    private ObstacleManager obstacleManager;

    private Sound jumpSound;
    private Sound hitSound;
    private Sound gameOverSound;

    private long changeTime;
    private boolean changingColors = false;
    private boolean goingBlackOnWhite = false;

    private Label tutorialLabel;
    private String[] tutorialTexts = new String[2];
    private float tutorialTimer = 0;
    private int tutorialState = 0;
    private boolean tutorialPause = false;

    private boolean paused = false;
    private long pauseTime = -1;
    private long pausedFor = 0;

    private static int previousScore;

    private GameInfo gameInfo;

    private InputListener inputListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (!tutorialPause && (paused || pauseButton.isOver())) {
                return false;
            }

            if (player.canJump() && tutorialState > 0) {
                player.jump();
                jumpSound.play(DoubleJump.sound ? 1.0f / player.getJumpCount(): 0);
                if (player.getJumpCount() == 2) {
                    gameInfo.setDoubleJumps(gameInfo.getDoubleJumps() + 1);
                } else {
                    gameInfo.setJumps(gameInfo.getJumps() + 1);
                }

                if (tutorialPause) {
                    pausedFor = TimeUtils.millis() - pausedFor;
                    changeTime += pausedFor;

                    tutorialPause = false;
                    tutorialLabel.setVisible(false);
                    if (tutorialState == 1) {
                        tutorialLabel.setText(tutorialTexts[tutorialState]);
                    }
                }
            }
            return true;
        }
    };


    public GameplayState(AssetManager assetManager) {
        this(assetManager, new GameInfo());
    }

    public GameplayState(AssetManager assetManager, GameInfo gameInfo) {
        super(assetManager);
        setClearColor(DoubleJump.whiteOnBlack ? Color.BLACK : Color.WHITE);

        this.gameInfo = gameInfo;
        previousScore = gameInfo.getScore();
    }

    @Override
    public void start() {
        final Stage stage = getStage();
        final TextureAtlas atlas = assetManager.get("packed/pack.atlas");
        final I18NBundle bundle = assetManager.get("lang/strings");
        final BitmapFont medium = assetManager.get("fonts/medium.fnt");

        tutorialState = Prefs.getPreferences().getInteger(Prefs.TUTORIAL_KEY, 0);

        changeTime = TimeUtils.millis();

        Gdx.app.debug(LOG_TAG, "In gameplay");

        groundHit = new Particle((ParticleEffect) assetManager.get("ground_hit.particle"));
        groundHit.setPosition(getWidth() / 2, 100);
        stage.addActor(groundHit);

        death = new Particle((ParticleEffect) assetManager.get("death.particle"));
        stage.addActor(death);

        ground = new Image(atlas.findRegion("box"));
        ground.setSize(getWidth(), 100);
        stage.addActor(ground);

        if (gameInfo.isContinued()) {
            player = new Player(atlas, ground.getHeight(), getHeight() - 24, gameInfo.getPlayerImage());
        } else {
            final Preferences prefs = Prefs.getPreferences();
            player = new Player(atlas, ground.getHeight(), getHeight() - 24, prefs.getInteger(Prefs.SELECTED_KEY, 0));
            gameInfo.setPlayerImage(player.getImageIndex());
        }
        player.setPosition((getWidth() - player.getWidth()) / 2, ground.getHeight() + player.getHeight() / 2);
        stage.addActor(player);

        final Image pauseOn = new Image(atlas.findRegion("pause_on"));
        final Image pauseOff = new Image(atlas.findRegion("pause_off"));
        pauseButton = new Button(pauseOff.getDrawable(), null, pauseOn.getDrawable());
        pauseButton.setPosition(0, getHeight() - pauseButton.getHeight());
        stage.addActor(pauseButton);

        pausedLabel = new Image(atlas.findRegion("paused"));
        pausedLabel.setPosition((getWidth() - pausedLabel.getWidth()) / 2, (getHeight() - pausedLabel.getHeight()) / 2);
        pausedLabel.setOrigin(pausedLabel.getWidth() / 2, pausedLabel.getHeight() / 2);
        pausedLabel.setVisible(false);
        pausedLabel.setScale(0);
        stage.addActor(pausedLabel);

        obstacleManager = new ObstacleManager(atlas.findRegion("box"));
        obstacleManager.setup(gameInfo);
        stage.addActor(obstacleManager);

        tutorialTexts[0] = bundle.get("tut_0");
        tutorialTexts[1] = bundle.get("tut_1");
        tutorialLabel = new Label(tutorialTexts[0], new Label.LabelStyle(medium, DoubleJump.whiteOnBlack ? Color.WHITE : Color.BLACK));
        tutorialLabel.setPosition(getWidth() / 2 - tutorialLabel.getWidth() - 15, getHeight() / 2);
        tutorialLabel.setAlignment(Align.right, Align.center);
        tutorialLabel.setVisible(false);
        stage.addActor(tutorialLabel);

        DoubleJump.whiteOnBlack = goingBlackOnWhite = !DoubleJump.whiteOnBlack;
        updateColors(1000);

        jumpSound = assetManager.get("sound/jump.wav");
        hitSound = assetManager.get("sound/hit.wav");
        gameOverSound = assetManager.get("sound/game_over.wav");

        stage.addListener(inputListener);
    }

    @Override
    public void update() {
        final long now = TimeUtils.millis();

        if (pauseTime != -1) {
            pausedLabel.setScale(Ease.elasticIn.apply(paused ? 0 : 1, paused ? 1 : 0, Math.min((now - pauseTime) / 300.0f, 1f)));
            if (now - pauseTime >= 300) {
                pauseTime = -1;
                pausedLabel.setVisible(paused);
            }
        }

        if (pauseButton.isChecked() != paused) {
            paused = pauseButton.isChecked();
            pauseTime = now;
            Gdx.app.log(LOG_TAG, paused ? "Pausing" : "Unpausing");
            if (paused) {
                pausedLabel.setVisible(true);
                pausedFor = now;
            } else {
                pausedFor = now - pausedFor;
                changeTime += pausedFor; // don't count paused time towards the color change
            }
        }

        if (paused || tutorialPause) {
            return;
        }

        super.update();

        if (player.isJustGrounded()) {
            groundHit.start();
            hitSound.play(DoubleJump.sound ? 1.0f : 0);
        }

        if (player.isAlive()) {
            if (obstacleManager.playerOverlaps(player)) {
                player.setAlive(false);
                death.setPosition(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2);
                death.start();
                obstacleManager.setDone(true);
                if (!changingColors) {
                    changeTime = now;
                }
                Gdx.app.log(LOG_TAG, "Player dead!");
                gameOverSound.play(DoubleJump.sound ? 1.0f : 0);
                gameInfo.setDeaths(gameInfo.getDeaths() + 1);
            }
        }

        if (now - changeTime >= COLOR_CHANGE_INTERVAL) {
            changeTime = now;
            changingColors = true;
            goingBlackOnWhite = DoubleJump.whiteOnBlack;
            Gdx.app.log(LOG_TAG, "Changing colors");
        }

        if (changingColors && !updateColors(now - changeTime)) {
            changingColors = false;
            Gdx.app.log(LOG_TAG, "Done changing colors");
        }

        if (obstacleManager.isOver()) {
            Gdx.app.log(LOG_TAG, "Game over, going to GameOverState");
            StateManager.changeState(new GameOverState(assetManager, gameInfo));
        }

        gameInfo.setScore(previousScore + obstacleManager.getPassed());

        tutorialTimer += Gdx.graphics.getRawDeltaTime();
        if (tutorialState == 0 && tutorialTimer - obstacleManager.getFirstSpawnTime() > TUTORIAL_FIRST_PAUSE) {
            pausedFor = now;
            tutorialPause = true;
            ++tutorialState;
            tutorialLabel.setVisible(true);
            Gdx.app.log(LOG_TAG, "First tutorial pause");
        } else if (tutorialState == 1 && tutorialTimer - obstacleManager.getFirstSpawnTime() > TUTORIAL_SECOND_PAUSE) {
            if (player.canJump()) {
                pausedFor = now;
                tutorialPause = true;
                tutorialLabel.setVisible(true);
                Gdx.app.log(LOG_TAG, "Final tutorial pause");
            }
            ++tutorialState;
        }
    }

    private boolean updateColors(long elapsed) {
        final float percentage = Math.min(elapsed / 1000.0f, 1f);

        if (percentage > 0.5f) {
            if (DoubleJump.whiteOnBlack && goingBlackOnWhite) {
                Gdx.app.log(LOG_TAG, "Now black on white");
                DoubleJump.whiteOnBlack = false;
            } else if (!DoubleJump.whiteOnBlack && !goingBlackOnWhite) {
                Gdx.app.log(LOG_TAG, "Now white on black");
                DoubleJump.whiteOnBlack = true;
            }
        }

        Color bgColor;

        if (goingBlackOnWhite) {
            bgColor = getClearColor();
            bgColor.r = bgColor.g = bgColor.b = percentage;
            setClearColor(bgColor);
        } else {
            bgColor = getClearColor();
            bgColor.r = bgColor.g = bgColor.b = 1 - percentage;
            setClearColor(bgColor);
        }

        player.setColor(1 - bgColor.r, 1 - bgColor.g, 1 - bgColor.b, 1);
        ground.setColor(1 - bgColor.r, 1 - bgColor.g, 1 - bgColor.b, 1);
        pauseButton.setColor(1 - bgColor.r, 1 - bgColor.g, 1 - bgColor.b, 1);
        pausedLabel.setColor(1 - bgColor.r, 1 - bgColor.g, 1 - bgColor.b, 1);
        groundHit.setColor(1 - bgColor.r, 1 - bgColor.g, 1 - bgColor.b, 1);
        death.setColor(1 - bgColor.r, 1 - bgColor.g, 1 - bgColor.b, 1);
        obstacleManager.setColor(1 - bgColor.r, 1 - bgColor.g, 1 - bgColor.b, 1);

        return percentage < 1f;
    }

    @Override
    public void transitionOut(float percentage) {
        pauseButton.setX(Ease.quadIn.apply(0, -pauseButton.getWidth(), percentage));
    }

    @Override
    public void resized() {
        pauseButton.setY(getHeight() - 24);
        ground.setWidth(getWidth());
        player.setX((getWidth() - player.getWidth()) / 2);
    }
}
