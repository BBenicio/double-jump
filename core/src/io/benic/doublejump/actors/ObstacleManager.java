package io.benic.doublejump.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import io.benic.doublejump.utils.GameInfo;
import io.benic.doublejump.utils.Utils;


public class ObstacleManager extends Group {
    private static final String LOG_TAG = "ObstacleManager";

    private static final float MIN_SPEED = 100;
    private static final float MAX_SPEED = 650;
    private static final float SPEED_INCREASE = 10;
    private static final float MIN_SPAWN_INTERVAL = 1.3f;
    private static final float MAX_SPAWN_INTERVAL = 2.5f;
    private static final float SPAWN_INTERVAL_DECREASE = 0.02f;

    private float countChances[] = new float[] { 100, 0, 0 };
    private float positionChances[] = new float[] { 100, 0, 0, 0 } ;

    private final Pool<Obstacle> obstaclePool;
    private Array<Obstacle> obstacleArray;

    private float spawnInterval;
    private float sinceLastSpawn;
    private float speed;

    private long startTime = 0;
    private float firstSpawnTime = Float.POSITIVE_INFINITY;

    private boolean done;

    private int passed = 0;

    private float[] temp = new float[4];

    public ObstacleManager(final TextureRegion texture) {
        obstaclePool = new Pool<Obstacle>() {
            @Override
            protected Obstacle newObject() {
                return new Obstacle(texture);
            }
        };
        obstacleArray = new Array<Obstacle>();

        spawnInterval = MAX_SPAWN_INTERVAL;
        sinceLastSpawn = 0;

        speed = MIN_SPEED;
    }

    public void setup(GameInfo gameInfo) {
        if (gameInfo.isContinued()) {
            countChances = gameInfo.getCountChances();
            positionChances = gameInfo.getPositionChances();
            spawnInterval = gameInfo.getSpawnInterval();
            speed = gameInfo.getSpeed();
        } else {
            gameInfo.setCountChances(countChances);
            gameInfo.setPositionChances(positionChances);
            gameInfo.setSpawnInterval(spawnInterval);
            gameInfo.setSpeed(speed);
        }
        startTime = TimeUtils.millis();
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        for (Obstacle obstacle : obstacleArray) {
            obstacle.setColor(r, g, b, a);
        }
        super.setColor(r, g, b, a);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        int len = obstacleArray.size;
        for (int i = len; --i >= 0;) {
            Obstacle obstacle = obstacleArray.get(i);
            if (!obstacle.isAlive()) {
                obstacleArray.removeIndex(i);
                obstaclePool.free(obstacle);
                removeActor(obstacle);
            } else if (!done && !obstacle.isPassed() &&
                      ((obstacle.getSide() < 0 && obstacle.getX() < getStage().getWidth() / 2 - 24 - 24) ||
                       (obstacle.getSide() > 0 && obstacle.getX() > getStage().getWidth() / 2 + 24))) {
                obstacle.setPassed(true);
                ++passed;
            }
        }

        if (done) {
            super.act(delta); // 2x speed
            return;
        }

        sinceLastSpawn += delta;
        if (sinceLastSpawn >= spawnInterval) {
            spawn();
            sinceLastSpawn = 0;
            if (speed < MAX_SPEED) {
                speed += SPEED_INCREASE;
            }
            if (spawnInterval > MIN_SPAWN_INTERVAL) {
                spawnInterval -= SPAWN_INTERVAL_DECREASE;
            }
        }
    }

    private void spawn() {
        if (Float.isInfinite(firstSpawnTime)) {
            firstSpawnTime = (TimeUtils.millis() - startTime) / 1000.0f;
            Gdx.app.log(LOG_TAG, "First spawn at " + firstSpawnTime);
        }
        int count = Utils.weightedChoice(countChances) + 1; // [1, 3]
        System.arraycopy(positionChances, 0, temp, 0, temp.length);

        for (int i = 0; i < count; ++i) {
            int p = Utils.weightedChoice(temp);
            temp[p] = Float.NaN;
        }

        for (int pos = 0; pos < 4; ++pos) {
            if (Float.isNaN(temp[pos])) {
                Obstacle obstacle = obstaclePool.obtain();
                boolean fromLeft = MathUtils.randomBoolean();
                obstacle.init(pos, fromLeft ? 1 : -1);
                obstacle.setX(fromLeft ? -24 : getStage().getWidth());
                obstacle.setSpeed(speed);
                obstacle.setColor(getColor());
                addActor(obstacle);
                obstacleArray.add(obstacle);
            }
        }

        updateChances();
    }

    private void updateChances() {
        if (countChances[0] > 0) {
            countChances[0] -= 10;
            countChances[1] += 10;
        } else if (countChances[1] > 0) {
            countChances[1] -= 10;
            countChances[2] += 10;
        }

        if (positionChances[0] > 25) {
            positionChances[0] -= 7.5;
            positionChances[1] += 2.5;
            positionChances[2] += 2.5;
            positionChances[3] += 2.5;
        }
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean playerOverlaps(Player player) {
        for (int i = 0; i < obstacleArray.size; ++i) {
            Obstacle obstacle = obstacleArray.get(i);
            if (obstacle.getX() <= player.getRight() && obstacle.getRight() >= player.getX() &&
                obstacle.getY() <= player.getTop() && obstacle.getTop() >= player.getY()) {
                obstacle.setAlive(false);
                obstacleArray.removeIndex(i);
                obstaclePool.free(obstacle);
                removeActor(obstacle);
                return true;
            }
        }

        return false;
    }

    public boolean isOver() {
        return done && obstacleArray.size == 0;
    }

    public int getPassed() {
        return passed;
    }

    public float getFirstSpawnTime() {
        return firstSpawnTime;
    }
}
