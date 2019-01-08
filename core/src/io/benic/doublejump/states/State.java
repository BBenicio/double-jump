package io.benic.doublejump.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;


public abstract class State {
    private Stage stage;
    private Color clearColor;
    protected AssetManager assetManager;
    
    private long lastUpdate = 0;
    
    State(AssetManager assetManager) {
        stage = new Stage();
        stage.setDebugAll(false);
        clearColor = Color.BLACK;
        this.assetManager = assetManager;
    }
    
    public final Stage getStage() {
        return stage;
    }
    
    public final Color getClearColor() {
        return clearColor.cpy();
    }
    
    public final void setClearColor(Color clearColor) {
        this.clearColor = clearColor;
    }
    
    public AssetManager getAssetManager() {
        return assetManager;
    }
    
    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
    
    public abstract void start();
    
    public void update() {
        /*final long currentUpdate = TimeUtils.millis();
        stage.act((currentUpdate - lastUpdate) / 1000.0f);
        lastUpdate = currentUpdate;*/
        stage.act(Gdx.graphics.getDeltaTime());
    }
    
    public void draw() {
        Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.draw();
    }
    
    public float getWidth() {
        return stage.getWidth();
    }
    
    public float getHeight() {
        return stage.getHeight();
    }
    
    public void resized() {}
    
    public void dispose() {
        stage.dispose();
    }
    
    public void transitionOut(float percentage) {}
}
