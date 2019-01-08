package io.benic.doublejump.utils;

public class GameInfo {
    private int playerImage = 0;
    private int score = 0;
    private int jumps = 0;
    private int doubleJumps = 0;
    private float speed = 0;
    private float spawnInterval = 0;
    private float[] countChances = null;
    private float[] positionChances = null;
    private boolean continued = false;
    
    public int getPlayerImage() {
        return playerImage;
    }
    
    public void setPlayerImage(int playerImage) {
        this.playerImage = playerImage;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getJumps() {
        return jumps;
    }
    
    public void setJumps(int jumps) {
        this.jumps = jumps;
    }
    
    public int getDoubleJumps() {
        return doubleJumps;
    }
    
    public void setDoubleJumps(int doubleJumps) {
        this.doubleJumps = doubleJumps;
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    
    public float getSpawnInterval() {
        return spawnInterval;
    }
    
    public void setSpawnInterval(float spawnInterval) {
        this.spawnInterval = spawnInterval;
    }
    
    public float[] getCountChances() {
        return countChances;
    }
    
    public void setCountChances(float[] countChances) {
        this.countChances = countChances;
    }
    
    public float[] getPositionChances() {
        return positionChances;
    }
    
    public void setPositionChances(float[] positionChances) {
        this.positionChances = positionChances;
    }
    
    public boolean isContinued() {
        return continued;
    }
    
    public void setContinued(boolean continued) {
        this.continued = continued;
    }
}
