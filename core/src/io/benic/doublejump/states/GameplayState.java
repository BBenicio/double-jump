package io.benic.doublejump.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
//    private static final long COLOR_CHANGE_INTERVAL = 4000;

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
    private boolean goingBlackonWhite = false;

    private boolean paused = false;
    private long pauseTime = -1;
    private long pausedFor = 0;

    private static int previousScore;

    private GameInfo gameInfo;

    /*private ClickListener clickListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            if (paused || pauseButton.isOver()) {
                return;
            }

            if (player.canJump()) {
                player.jump();
            }
        }
    };*/

    private InputListener inputListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (paused || pauseButton.isOver()) {
                return false;
            }

            if (player.canJump()) {
                player.jump();
                jumpSound.play(DoubleJump.sound ? 1.0f / player.getJumpCount(): 0);
                if (player.getJumpCount() == 2) {
                    gameInfo.setDoubleJumps(gameInfo.getDoubleJumps() + 1);
                } else {
                    gameInfo.setJumps(gameInfo.getJumps() + 1);
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

        DoubleJump.whiteOnBlack = goingBlackonWhite = !DoubleJump.whiteOnBlack;
        updateColors(1000);

        jumpSound = assetManager.get("sound/jump.wav");
        hitSound = assetManager.get("sound/hit.wav");
        gameOverSound = assetManager.get("sound/game_over.wav");

//        stage.addListener(clickListener);
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

        if (paused) {
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
            }
        }

        if (now - changeTime >= COLOR_CHANGE_INTERVAL) {
            changeTime = now;
            changingColors = true;
            goingBlackonWhite = DoubleJump.whiteOnBlack;
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
    }

    private boolean updateColors(long elapsed) {
        final float percentage = Math.min(elapsed / 1000.0f, 1f);

        if (percentage > 0.5f) {
            if (DoubleJump.whiteOnBlack && goingBlackonWhite) {
                Gdx.app.log(LOG_TAG, "Now black on white");
                DoubleJump.whiteOnBlack = false;
            } else if (!DoubleJump.whiteOnBlack && !goingBlackonWhite) {
                Gdx.app.log(LOG_TAG, "Now white on black");
                DoubleJump.whiteOnBlack = true;
            }
        }

        Color bgColor;

        if (goingBlackonWhite) {
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
