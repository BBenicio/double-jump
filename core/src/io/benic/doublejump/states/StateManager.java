package io.benic.doublejump.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.Viewport;


public class StateManager {
    private static final String LOG_TAG = "StateManager";
    
    private static final long TRANSITION_TIME = 500;
    
    private State currentState = null;
    private State nextState = null;
    private Viewport viewport = null;
    private boolean transitioning = false;
    private long transitionStartTime = 0;
    
    private StateManager() {}
    
    public State getCurrentState() {
        return currentState;
    }
    
    public Viewport getViewport() {
        return viewport;
    }
    
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
        if (currentState != null) {
            currentState.getStage().setViewport(viewport);
        }
    }
    
    public boolean isTransitioning() {
        return transitioning;
    }
    
    private void beginState() {
        if (currentState != null) {
            Gdx.input.setInputProcessor(currentState.getStage());
            currentState.getStage().setViewport(viewport);
            currentState.start();
        } else {
            Gdx.input.setInputProcessor(null);
        }
    }
    
    public boolean update() {
        if (currentState != null) {
            if (transitioning) {
                float percentage = (float) (TimeUtils.millis() - transitionStartTime) / (float) TRANSITION_TIME;
                if (percentage > 1) {
                    currentState.dispose();
                    currentState = nextState;
                    beginState();
                    nextState = null;
                    transitioning = false;
                } else {
                    currentState.transitionOut(percentage);
                }
            } else {
                currentState.update();
            }
            
            return true;
        }
        
        return false;
    }
    
    public void draw() {
        if (currentState != null) {
            currentState.draw();
        }
    }
    
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (currentState != null) {
            currentState.resized();
        }
    }
    
    public void dispose() {
        if (nextState != null) {
            nextState.dispose();
            nextState = null;
        }
        
        if (currentState != null) {
            currentState.dispose();
            currentState = null;
        }
    }
    
    public static void changeState(State state) {
        StateManager sm = getInstance();
    
        Gdx.app.debug(LOG_TAG,"State change requested");
        if (sm.currentState != null) {
            sm.transitioning = true;
            sm.transitionStartTime = TimeUtils.millis();
            sm.nextState = state;
        } else {
            sm.currentState = state;
            sm.transitioning = false;
            sm.nextState = null;
            sm.beginState();
        }
    }
    
    public static StateManager getInstance() {
        if (instance == null) {
            Gdx.app.debug(LOG_TAG, "Creating state manager");
            instance = new StateManager();
        }
        
        return instance;
    }
    
    private static StateManager instance = null;
}
