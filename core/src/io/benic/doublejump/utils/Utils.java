package io.benic.doublejump.utils;

import com.badlogic.gdx.math.MathUtils;


public class Utils {
    public static int weightedChoice(float[] weights) {
        float value = MathUtils.random();
        float total = 0;
        for (float w : weights) {
            if (!Float.isInfinite(w) && !Float.isNaN(w)) {
                total += w;
            }
        }
        
        value *= total;
        
        int chosen;
        for (chosen = 0; chosen < weights.length && value > 0; ++chosen) {
            if (!Float.isInfinite(weights[chosen]) && !Float.isNaN(weights[chosen])) {
                value -= weights[chosen];
            }
        }
        
        return chosen - 1;
    }
}
