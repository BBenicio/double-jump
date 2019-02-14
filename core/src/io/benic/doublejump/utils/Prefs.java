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
    public static final String SKINS_PLAYED_KEY = "skins";
    public static final String TUTORIAL_KEY = "tutorial";

    public static final boolean SOUND_DEFAULT = true;
    public static final String SKINS_PLAYED_DEFAULT = "0 0 0 0 0 0 0 0 0 0 0 0";

    public static Preferences getPreferences() {
        if (preferences == null) {
            Gdx.app.log(LOG_TAG, "getting preferences");
            preferences = Gdx.app.getPreferences(DoubleJump.NAME);
        }

        return preferences;
    }

    public static int[] getArray(String key, String defValue) {
        String[] strings = preferences.getString(key, defValue).split(" ");
        int[] arr = new int[strings.length];
        for (int i = 0; i < strings.length; i++) {
            arr[i] = Integer.parseInt(strings[i]);
        }

        return arr;
    }

    public static void putArray(String key, int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i : array) {
            sb.append(i);
            sb.append(' ');
        }
        sb.deleteCharAt(sb.length() - 1);

        preferences.putString(key, sb.toString());
    }

    private Prefs() {
    }
}
