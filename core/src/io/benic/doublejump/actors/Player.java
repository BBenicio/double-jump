package io.benic.doublejump.actors;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.benic.doublejump.utils.Ease;


public class Player extends MovingObject {
    private static final String LOG_TAG = "Player";
    
    public static final float JUMP_VELOCITY = 600.0f;
    public static final float GRAVITY = 2000.0f;
    
    private boolean grounded = false;
    private int jumpCount = 0;
    private float ground;
    private float ceiling;
    private Vector2 lastPos = new Vector2(0, 0);
    private float rotatingTime = -1;
    private float rotateTargetTime = 0;
    private float fromRot = 0;
    private float toRot = 0;
    
    private boolean justGrounded = false;
    
    private boolean alive = true;
    
    private final int imageIndex;
    
    public Player(TextureAtlas atlas, float ground, float ceiling, int image) {
        super(atlas.findRegion("player", image));
        setOrigin(getWidth() / 2, getHeight() / 2);
        this.ground = ground;
        this.ceiling = ceiling;
//        acceleration.y = -GRAVITY;
        lastPos.y = ceiling;
        imageIndex = image;
    }
    
    public Player(TextureAtlas atlas, float ground, float ceiling) {
        this(atlas, ground, ceiling, MathUtils.random(0, 7));
    }
    
    public int getImageIndex() {
        return imageIndex;
    }
    
    @Override
    public void act(float delta) {
        if (!alive) {
            return;
        }
        
        justGrounded = false;
        lastPos.set(getX(), getY());
    
        super.act(delta);
    
        if (lastPos.y > ground && getY() <= ground) {
            setY(ground);
            velocity.y *= -0.1f;
        } else if (getY() <= ground && !grounded) {
            acceleration.y = 0;
            velocity.y = 0;
            grounded = true;
            jumpCount = 0;
            
            justGrounded = true;
        } else if (grounded && getY() > ground) {
            grounded = false;
        } else if (acceleration.y != -GRAVITY && !grounded) {
            acceleration.y = -GRAVITY;
        }
    
        if (getY() >= ceiling) {
            setY(ceiling);
            velocity.y *= -0.1f;
            recalculateRotateTime();
            rotatingTime = 0;
            fromRot = getRotation();
            toRot = 360;
        }
        
        if (rotatingTime >= 0) {
            rotatingTime += delta;
            setRotation(Ease.linear.apply(fromRot, toRot, rotatingTime / rotateTargetTime));
            if (rotatingTime >= rotateTargetTime) {
                rotatingTime = -1;
                setRotation(0);
            }
        }
    }
    
    private void recalculateRotateTime() {
        final float delta = (velocity.y * velocity.y) + GRAVITY * (getY() - ground);
        rotateTargetTime = (float) (-velocity.y - Math.sqrt(delta)) / -GRAVITY;
    }
    
    public void jump() {
        ++jumpCount;
        velocity.y += JUMP_VELOCITY;
        
        rotatingTime = 0;
        recalculateRotateTime();
        
        if (jumpCount == 1 || velocity.y < JUMP_VELOCITY) {
            fromRot = getRotation();
            toRot = 360;
        } else {
            fromRot = getRotation();
            toRot = 720;
        }
    }
    
    public int getJumpCount() {
        return jumpCount;
    }
    
    public boolean canJump() {
        return grounded || jumpCount < 2;
    }
    
    public boolean isJustGrounded() {
        return justGrounded;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
        setVisible(alive);
    }
}
