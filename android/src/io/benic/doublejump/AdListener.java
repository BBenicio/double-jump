package io.benic.doublejump;

import com.badlogic.gdx.Gdx;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import io.benic.doublejump.ads.RewardedAd;


public class AdListener implements RewardedVideoAdListener, RewardedAd {
    public static final String LOG_TAG = "AdListener";

    private AndroidLauncher launcher;
    private RewardedVideoAd rewardedVideoAd;
    private DoubleJump doubleJump;
    private int fails = 0;

    public AdListener(AndroidLauncher launcher, DoubleJump doubleJump) {
        this.launcher = launcher;
        this.doubleJump = doubleJump;

        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(launcher);
        rewardedVideoAd.setRewardedVideoAdListener(this);

        DoubleJump.rewardedAd = this;

        loadVideo();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Gdx.app.log(LOG_TAG, "video loaded");

        doubleJump.videoLoaded(true);
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Gdx.app.log(LOG_TAG, "video opened");
    }

    @Override
    public void onRewardedVideoStarted() {
        Gdx.app.log(LOG_TAG, "video started");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Gdx.app.log(LOG_TAG, "video closed");

        loadVideo();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        Gdx.app.log(LOG_TAG, "video rewarded " + rewardItem.getAmount() + " " + rewardItem.getType());
        doubleJump.reward();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Gdx.app.log(LOG_TAG, "video left app");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Gdx.app.log(LOG_TAG, "video failed to load (" + i + ")");
        if (++fails < 3) {
            loadVideo();
            doubleJump.videoLoaded(false);
        }
    }

    @Override
    public void onRewardedVideoCompleted() {
        Gdx.app.log(LOG_TAG, "video completed");

        loadVideo();
    }

    public void onResume() {
        rewardedVideoAd.resume(launcher);
    }

    public void onPause() {
        rewardedVideoAd.pause(launcher);
    }

    public void onDestroy() {
        rewardedVideoAd.destroy(launcher);
    }

    @Override
    public void loadVideo() {
        // Continuar: ca-app-pub-2833633163238735/5104982676
        // Dinheiro: ca-app-pub-2833633163238735/6531536999
        // Google test: ca-app-pub-3940256099942544/5224354917
        rewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().addTestDevice("F0698981F98B091004BACD37D6954B52").build());
    }

    @Override
    public void isVideoLoaded() {
        launcher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doubleJump.videoLoaded(rewardedVideoAd.isLoaded());
            }
        });
    }

    @Override
    public void playVideo() {
        launcher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rewardedVideoAd.show();
            }
        });
    }
}
