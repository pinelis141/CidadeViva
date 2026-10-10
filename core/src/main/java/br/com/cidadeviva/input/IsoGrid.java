package br.com.cidadeviva.input;

/** World-to-grid selection mathematics, independent from graphics or device. */
public final class IsoGrid {
    private IsoGrid() { }

    public static int[] pick(float worldX, float worldY, int halfWidth, int halfHeight) {
        final float a = worldX / halfWidth;
        final float b = -worldY / halfHeight;
        final int x = (int) Math.floor((a + b) / 2f + 0.5f);
        final int y = (int) Math.floor((b - a) / 2f + 0.5f);
        return new int[] {x,y};
    }

    /** Shift camera by the world distance between two screen-space points. */
    public static float[] anchoredCamera(
        float centerX, float centerY, float oldZoom, float newZoom,
        float oldScreenX, float oldScreenY, float newScreenX, float newScreenY,
        float viewportWidth, float viewportHeight
    ) {
        // Screen coordinate origin is upper-left; camera world Y is upwards.
        float beforeX = centerX + (oldScreenX - viewportWidth/2f) * oldZoom;
        float beforeY = centerY + (viewportHeight/2f - oldScreenY) * oldZoom;
        float afterOffsetX = (newScreenX - viewportWidth/2f) * newZoom;
        float afterOffsetY = (viewportHeight/2f - newScreenY) * newZoom;
        return new float[] {beforeX - afterOffsetX, beforeY - afterOffsetY};
    }

    public static float bound(float position, float worldMin, float worldMax, float viewSize) {
        float center = (worldMin + worldMax)/2f;
        if (viewSize >= worldMax - worldMin) return center;
        return HudLayout.clamp(position, worldMin + viewSize/2f, worldMax - viewSize/2f);
    }
}
