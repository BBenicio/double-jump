package io.benic.doublejump.utils;

import com.badlogic.gdx.math.Interpolation;


public class Ease {
    private static final float B1 = 1.0f / 2.75f;
    private static final float B2 = 2.0f / 2.75f;
    private static final float B3 = 1.5f / 2.75f;
    private static final float B4 = 2.5f / 2.75f;
    private static final float B5 = 2.25f / 2.75f;
    private static final float B6 = 2.625f / 2.75f;
    private static final float ELASTIC_PERIOD = 0.4f;
    private static final float ELASTIC_AMPLITUDE = 1f;
    
    public static final Interpolation quadIn = new Interpolation() {
        @Override
        public float apply(float a) {
            return a * a;
        }
    };
    
    public static final Interpolation quadOut = new Interpolation() {
        @Override
        public float apply(float a) {
            return -a * (a - 2);
        }
    };
    
    public static final Interpolation bounceOut = new Interpolation() {
        @Override
        public float apply(float a) {
            if (a < B1) return 7.5625f * a * a;
            if (a < B2) return 7.5625f * (a - B3) * (a - B3) + 0.75f;
            if (a < B4) return 7.5625f * (a - B5) * (a - B5) + 0.9375f;
            return 7.5625f * (a - B6) * (a - B6) + 0.984375f;
        }
    };
    
    public static final Interpolation elasticIn = new Interpolation() {
        @Override
        public float apply(float a) {
            return (float) -(ELASTIC_AMPLITUDE * Math.pow(2, 10 * (a -= 1)) * Math.sin((a - (ELASTIC_PERIOD / (2 * Math.PI) * Math.asin(1 / ELASTIC_AMPLITUDE))) * (2 * Math.PI) / ELASTIC_PERIOD));
        }
    };
    
    public static final Interpolation elasticInOut = new Interpolation() {
        @Override
        public float apply(float a) {
            if (a < 0.5f) {
                return (float) (-0.5f * (Math.pow(2f, 10f * (a -= 0.5f)) * Math.sin((a - (ELASTIC_PERIOD / 4f)) * (2f * Math.PI) / ELASTIC_PERIOD)));
            }
            return (float) (Math.pow(2f, -10f * (a -= 0.5f)) * Math.sin((a - (ELASTIC_PERIOD / 4f)) * (2f * Math.PI) / ELASTIC_PERIOD) * 0.5f + 1f);
        }
    };
    
    public static final Interpolation expoIn = new Interpolation() {
        @Override
        public float apply(float a) {
            return (float) Math.pow(2, 10 * (a - 1));
        }
    };
    
    public static final Interpolation backInOut = new Interpolation() {
        @Override
        public float apply(float a) {
            a *= 2;
            if (a < 1) return a * a * (2.70158f * a - 1.70158f) / 2;
            a--;
            return (1 - (--a) * (a) * (-2.70158f * a - 1.70158f)) / 2 + 0.5f;
        }
    };
    
    public static final Interpolation linear = Interpolation.linear;
}
