package br.com.cidadeviva.input;

/** UI geometry in top-left screen coordinates; pure Java for deterministic touch tests. */
public final class HudLayout {
    public static final int ACTIONS = 6;
    public static final int TOOLS = 5;
    public static final float TOP_HEIGHT = 249f;
    public static final float BOTTOM_HEIGHT = 134f;
    public static final float ACTION_TOP = 195f;
    public static final float ACTION_HEIGHT = 49f;
    public static final float INSPECTOR_HEIGHT = 86f;
    private HudLayout() { }

    public static float scale(float width,float height) {
        // Preserve normal UI sizing, but fit the tallest (558-unit) dialog
        // inside short landscape screens with breathing room.
        return Math.min(clamp(Math.min(width / 730f,height / 970f),0.65f,1.65f),height/600f);
    }
    public static float clamp(float value,float min,float max) {
        return Math.max(min,Math.min(max,value));
    }
    public static boolean isHud(float x,float y,float width,float height,float scale) {
        return x < 0 || y < 0 || x >= width || y >= height
            || y <= TOP_HEIGHT * scale || y >= height - BOTTOM_HEIGHT * scale;
    }
    /** Information card just above the tools, only active while inspecting a tile. */
    public static boolean inspectorAt(float x,float y,float width,float height,float scale) {
        return x >= 8*scale && x < width-8*scale
            && y >= height-(BOTTOM_HEIGHT+INSPECTOR_HEIGHT+6)*scale
            && y < height-(BOTTOM_HEIGHT+6)*scale;
    }
    /** Three persistent shortcuts on top right: ECON, CIDADES, CENTRAR. */
    public static int quickActionAt(float x,float y,float width,float scale) {
        if(y<0 || y>55*scale)return -1;
        if(x>=width-357*scale && x<width-248*scale)return 0;
        if(x>=width-244*scale && x<width-120*scale)return 1;
        if(x>=width-117*scale && x<width-5*scale)return 2;
        return -1;
    }
    public static int actionAt(float x,float y,float width,float scale) {
        if(x<0 || x>=width || y<ACTION_TOP*scale
            || y>=(ACTION_TOP+ACTION_HEIGHT)*scale)return -1;
        return Math.min(ACTIONS-1,(int)(x/(width/ACTIONS)));
    }
    public static int toolAt(float x,float y,float width,float height,float scale) {
        if(x<0 || x>=width || y<height-BOTTOM_HEIGHT*scale || y>=height)return -1;
        return Math.min(TOOLS-1,(int)(x/(width/TOOLS)));
    }
}
