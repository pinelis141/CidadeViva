package br.com.cidadeviva.sim;

/** Camera target from the actual urban fabric instead of the geographic map center. */
public final class CityFocus {
    private static final float ISO_HALF_W=32f,ISO_HALF_H=16f;
    private CityFocus() { }

    public static final class Target {
        public final float x,y,minX,maxX,minY,maxY;
        private Target(float x,float y,float minX,float maxX,float minY,float maxY) {
            this.x=x;this.y=y;this.minX=minX;this.maxX=maxX;this.minY=minY;this.maxY=maxY;
        }
        /** World pixels per screen pixel (libGDX camera.zoom). */
        public float suggestedZoom(float width,float height,float topInset,float bottomInset) {
            float usable=Math.max(160f,height-topInset-bottomInset);
            float requiredX=(maxX-minX+160f)/(Math.max(250f,width)*0.68f);
            float requiredY=(maxY-minY+150f)/(usable*0.74f);
            return Math.max(0.64f,Math.min(1.4f,Math.max(requiredX,requiredY)));
        }
    }

    public static Target forCity(City city) {
        double weightedX=0,weightedY=0,total=0;
        float minX=Float.POSITIVE_INFINITY,maxX=Float.NEGATIVE_INFINITY;
        float minY=Float.POSITIVE_INFINITY,maxY=Float.NEGATIVE_INFINITY;
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
            Tile tile=city.tile(x,y);
            if(tile.type!=TileType.ROAD && !tile.type.isZone())continue;
            float wx=(x-y)*ISO_HALF_W,wy=-(x+y)*ISO_HALF_H;
            int weight=tile.type.isZone()?(tile.level>0?5:2):1;
            weightedX+=wx*weight;weightedY+=wy*weight;total+=weight;
            minX=Math.min(minX,wx);maxX=Math.max(maxX,wx);
            minY=Math.min(minY,wy);maxY=Math.max(maxY,wy);
        }
        if(total==0) {
            float y=-(City.WIDTH/2f+City.HEIGHT/2f)*ISO_HALF_H;
            return new Target(0,y,-150,150,y-120,y+120);
        }
        return new Target((float)(weightedX/total),(float)(weightedY/total),
            minX,maxX,minY,maxY);
    }
}
