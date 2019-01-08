package io.benic.doublejump.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.Actor;


public class Particle extends Actor {
    private ParticleEffect particleEffect;
    
    public Particle(ParticleEffect particleEffect)
    {
        this.particleEffect = particleEffect;
    }
    
    public void start()
    {
        particleEffect.start();
    }
    
    public void setPosition(float x, float y)
    {
        particleEffect.setPosition(x, y);
    }
    
    @Override
    public void setColor(float r, float g, float b, float a) {
        super.setColor(r, g, b, a);
        float[] temp = particleEffect.getEmitters().first().getTint().getColors();
        temp[0] = getColor().r;
        temp[1] = getColor().g;
        temp[2] = getColor().b;
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        particleEffect.draw(batch, Gdx.graphics.getDeltaTime());
    }
}
