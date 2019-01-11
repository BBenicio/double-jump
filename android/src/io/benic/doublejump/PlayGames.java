package io.benic.doublejump;

import android.content.Intent;
import android.support.annotation.NonNull;
import com.badlogic.gdx.Gdx;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import io.benic.doublejump.games.PlayGamesListener;


public class PlayGames implements PlayGamesListener {
    private static final String LOG_TAG = "PlayGames";

    private final AndroidLauncher launcher;
    private final DoubleJump doubleJump;

    private GoogleSignInClient signInClient;
    private AchievementsClient achievementsClient;
    private LeaderboardsClient leaderboardsClient;

    private boolean done = false;

    private OnSuccessListener<GoogleSignInAccount> signInSuccess = new OnSuccessListener<GoogleSignInAccount>() {
        @Override
        public void onSuccess(GoogleSignInAccount googleSignInAccount) {
            Gdx.app.log(LOG_TAG, "signed in!");

            achievementsClient = Games.getAchievementsClient(launcher, googleSignInAccount);
            leaderboardsClient = Games.getLeaderboardsClient(launcher, googleSignInAccount);

            done = true;
        }
    };

    private OnFailureListener signInFail = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Gdx.app.log(LOG_TAG, "sign in failed " + e.getMessage());
            done = true;
        }
    };

    private OnCompleteListener<Intent> achievementsListener = new OnCompleteListener<Intent>() {
        @Override
        public void onComplete(@NonNull Task<Intent> task) {
            if (task.isSuccessful()) {
                done = false;
                launcher.startActivityForResult(task.getResult(), RC_UNUSED);
            } else {
                Gdx.app.log(LOG_TAG, "show achievements failed " + task.getException());
            }
        }
    };

    private OnCompleteListener<Intent> leaderboardListener = new OnCompleteListener<Intent>() {
        @Override
        public void onComplete(@NonNull Task<Intent> task) {
            if (task.isSuccessful()) {
                done = false;
                launcher.startActivityForResult(task.getResult(), RC_UNUSED);
            } else {
                Gdx.app.log(LOG_TAG, "show leaderboard failed " + task.getException());
            }
        }
    };

    static final int RC_UNUSED = 5001;
    static final int RC_SIGN_IN = 9001;

    public PlayGames(final AndroidLauncher launcher, DoubleJump doubleJump) {
        this.launcher = launcher;
        this.doubleJump = doubleJump;

        signInClient = GoogleSignIn.getClient(launcher, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
        silentSignIn();

        DoubleJump.playGames = this;
    }

    public void silentSignIn() {
        Gdx.app.log(LOG_TAG, "trying silent sign in");
        done = false;

        signInClient.silentSignIn().addOnSuccessListener(signInSuccess).addOnFailureListener(signInFail);
    }

    @Override
    public void signIn() {
        Gdx.app.log(LOG_TAG, "requested sign in");
        done = false;

        Intent intent = signInClient.getSignInIntent();
        launcher.startActivityForResult(intent, RC_SIGN_IN);

    }

    public void signInResult(Intent intent) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
        if (result.isSuccess()) {
            signInSuccess.onSuccess(result.getSignInAccount());
        } else {
            Gdx.app.log(LOG_TAG, "sign in fail: " + result.getStatus().getStatusMessage());
        }
        GoogleSignIn.getSignedInAccountFromIntent(intent).addOnSuccessListener(signInSuccess).addOnFailureListener(signInFail);
        done = true;
    }

    @Override
    public void showAchievements() {
        Gdx.app.log(LOG_TAG, "show achievements");

        achievementsClient.getAchievementsIntent().addOnCompleteListener(achievementsListener);
    }

    @Override
    public void unlockAchievement(String name) {
        Gdx.app.log(LOG_TAG, "unlock an achievement");
        achievementsClient.unlock(name);
    }

    @Override
    public void submitScore(int score) {
        Gdx.app.log(LOG_TAG, "submit a score");
        leaderboardsClient.submitScore(PlayGamesListener.LEADERBOARD, score);
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public void showLeaderboard() {
        Gdx.app.log(LOG_TAG, "show leaderboard");
        leaderboardsClient.getLeaderboardIntent(PlayGamesListener.LEADERBOARD).addOnCompleteListener(leaderboardListener);
    }

    @Override
    public boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(launcher) != null;
    }

    public void otherResult() {
        done = true;
    }
}
