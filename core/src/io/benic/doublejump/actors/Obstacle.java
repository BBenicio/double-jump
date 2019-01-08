package io.benic.doublejump.actors;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;


public class Obstacle extends MovingObject implements Pool.Poolable {
    private static final float GAP = 24.0f * 3.0f;
    private static final float LOW = 100.0f + 24.0f / 2.0f;
    
    private int side;
    private boolean alive;
    private float speed;
    private boolean passed;
    
    public Obstacle(TextureRegion region) {
        super(region);
        setOrigin(getWidth() / 2, getHeight() / 2);
        this.side = 1;
        this.alive = false;
        this.speed = 0;
    }
    
    public void init(int pos, int side) {
        setAlive(true);
        setY(LOW + pos * GAP);
        setSide(side);
    }
    
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    
    public int getSide() {
        return side;
    }
    
    public void setSide(int side) {
        this.side = side;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
        setVisible(alive);
    }
    
    @Override
    public void act(float delta) {
        if (!alive) {
            return;
        }
        
        setVelocity(speed * side, 0);
        setRotation(getRotation() - side * 360 * delta);
        
        if ((getX() < -getWidth() && side < 0) || (getX() > getStage().getWidth() && side > 0)) {
            setAlive(false);
            setVisible(false);
        }
        
        super.act(delta);
    }
    
    @Override
    public void reset() {
        setPosition(0, 0);
        setVelocity(0, 0);
        setAcceleration(0, 0);
        setRotation(0);
        side = 1;
        setVisible(false);
        alive = false;
        passed = false;
    }
    
    public boolean isPassed() {
        return passed;
    }
    
    public void setPassed(boolean passed) {
        this.passed = passed;
    }
}
