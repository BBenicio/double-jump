package io.benic.doublejump.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import io.benic.doublejump.DoubleJump;


public class Prefs {
    private static final String LOG_TAG = "Prefs";

    private static Preferences preferences;

    public static final String SOUND_KEY = "sound";
    public static final String MONEY_KEY = "money";
    public static final String SCORE_KEY = "score";
    public static final String SELECTED_KEY = "selected";
    public static final String UNLOCKED_KEY = "unlocked";

    public static final boolean SOUND_DEFAULT = true;

    public static Preferences getPreferences() {
        if (preferences == null) {
            Gdx.app.log(LOG_TAG, "getting preferences");
            preferences = Gdx.app.getPreferences(DoubleJump.NAME);
        }

        return preferences;
    }

    private Prefs() {
    }
}
