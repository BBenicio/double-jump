package io.benic.doublejump.games;

public interface PlayGamesListener {
    String THAT_WAS_QUICK = "CgkIlJWVva0eEAIQAg";
    String TWO_JUMPS = "CgkIlJWVva0eEAIQAw";
    String DOUBLE_JUMPER = "CgkIlJWVva0eEAIQBA";
    String RICH = "CgkIlJWVva0eEAIQBQ";
    String SOLD_OUT = "CgkIlJWVva0eEAIQBg";
    String CHAMELEON = "CgkIlJWVva0eEAIQBw";
    String WE_HAVE_A_WINNER = "CgkIlJWVva0eEAIQCA";
    String LEADERBOARD = "CgkIlJWVva0eEAIQAQ";

    void signIn();
    boolean isSignedIn();
    void showAchievements();
    void showLeaderboard();

    void unlockAchievement(String name);
    void submitScore(int score);

    boolean isDone();
}
