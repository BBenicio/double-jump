package io.benic.doublejump.actors;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;


public class MovingObject extends Image {
    protected Vector2 velocity = new Vector2(0, 0);
    protected Vector2 acceleration = new Vector2(0, 0);
    
    public MovingObject(TextureRegion region) {
        super(region);
    }
    
    public Vector2 getVelocity() {
        return velocity;
    }
    
    public void setVelocity(float x, float y) {
        this.velocity.set(x, y);
    }
    
    public Vector2 getAcceleration() {
        return acceleration;
    }
    
    public void setAcceleration(float x, float y) {
        this.acceleration.set(x, y);
    }
    
    @Override
    public void act(float delta) {
        super.act(delta);
        
        velocity.add(acceleration.x * delta, acceleration.y * delta);
        moveBy(velocity.x * delta, velocity.y * delta);
    }
}
