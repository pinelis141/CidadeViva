package br.com.cidadeviva.input;

/** Regions in top-left screen coordinates, no dependency on libGDX. */
public final class HudLayout {
    public static final int ACTIONS = 6;
    public static final int TOOLS = 5;
    public static final float TOP_HEIGHT = 152f;
    public static final float BOTTOM_HEIGHT = 117f;
    public static final float ACTION_TOP = 106f;
    public static final float ACTION_HEIGHT = 42f;
    private HudLayout() { }

    public static float scale(float width, float height) {
        return clamp(Math.min(width / 730f, height / 970f), 0.65f, 1.65f);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static boolean isHud(float x, float y, float width, float height, float scale) {
        return x < 0 || y < 0 || x >= width || y >= height
            || y <= TOP_HEIGHT * scale || y >= height - BOTTOM_HEIGHT * scale;
    }

    public static int actionAt(float x, float y, float width, float scale) {
        if (x < 0 || x >= width || y < ACTION_TOP * scale
            || y >= (ACTION_TOP + ACTION_HEIGHT) * scale) return -1;
        return Math.min(ACTIONS - 1, (int) (x / (width / ACTIONS)));
    }

    public static int toolAt(float x, float y, float width, float height, float scale) {
        if (x < 0 || x >= width || y < height - BOTTOM_HEIGHT * scale || y >= height) return -1;
        return Math.min(TOOLS - 1, (int) (x / (width / TOOLS)));
    }
}
