package io.benic.doublejump;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.MobileAds;


public class AndroidLauncher extends AndroidApplication {
	private AdListener adListener;


	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// My publisher code: ca-app-pub-2833633163238735~3983472696
		// Google Test: ca-app-pub-3940256099942544~3347511713
		MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;

		DoubleJump doubleJump = new DoubleJump();
		initialize(doubleJump, config);

		adListener = new AdListener(this, doubleJump);
	}

	@Override
	protected void onResume() {
		adListener.onResume();

		super.onResume();
	}

	@Override
	protected void onPause() {
		adListener.onPause();

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		adListener.onDestroy();

		super.onDestroy();
	}
}
