package br.com.cidadeviva.sim;

/** Focuses the densest *inhabited neighborhood* instead of fitting distant road branches.
 * Pure Java so Android and desktop camera geometry can be tested without OpenGL.
 */
public final class CityFocus {
    private static final float ISO_HALF_W=32f,ISO_HALF_H=16f;
    private static final int CORE_RADIUS=7; // 15x15 tile neighborhood, not entire 48x48 map
    private CityFocus() { }

    public static final class Target {
        public final float x,y,minX,maxX,minY,maxY;
        private Target(float x,float y,float minX,float maxX,float minY,float maxY) {
            this.x=x;this.y=y;this.minX=minX;this.maxX=maxX;this.minY=minY;this.maxY=maxY;
        }
        /** Camera zoom: world pixels per screen pixel. Small value = closer buildings. */
        public float suggestedZoom(float width,float height,float topInset,float bottomInset) {
            float usable=Math.max(160f,height-Math.max(0,topInset)-Math.max(0,bottomInset));
            float byWidth=(maxX-minX+96f)/(Math.max(250f,width)*0.82f);
            float byHeight=(maxY-minY+146f)/(usable*0.72f);
            // Prioritize legibility over fitting every empty plot or road into view.
            return Math.max(0.42f,Math.min(1.10f,Math.max(byWidth,byHeight)*0.84f));
        }
    }

    private static int importance(Tile tile) {
        if(tile.type.isZone())return tile.level>0?12:3;
        return tile.type==TileType.ROAD?1:0;
    }

    public static Target forCity(City city) {
        // Integral image finds the densest walkable/occupied neighborhood in O(map area).
        int[][] prefix=new int[City.WIDTH+1][City.HEIGHT+1];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)
            prefix[x+1][y+1]=importance(city.tile(x,y))+prefix[x][y+1]
                +prefix[x+1][y]-prefix[x][y];
        int bestX=City.WIDTH/2,bestY=City.HEIGHT/2;
        int bestWeight=-1,bestDistance=Integer.MAX_VALUE;
        for(int cx=0;cx<City.WIDTH;cx++)for(int cy=0;cy<City.HEIGHT;cy++) {
            int x0=Math.max(0,cx-CORE_RADIUS),x1=Math.min(City.WIDTH,cx+CORE_RADIUS+1);
            int y0=Math.max(0,cy-CORE_RADIUS),y1=Math.min(City.HEIGHT,cy+CORE_RADIUS+1);
            int weight=prefix[x1][y1]-prefix[x0][y1]-prefix[x1][y0]+prefix[x0][y0];
            int dist=(cx-City.WIDTH/2)*(cx-City.WIDTH/2)
                +(cy-City.HEIGHT/2)*(cy-City.HEIGHT/2);
            if(weight>bestWeight || (weight==bestWeight && dist<bestDistance)) {
                bestX=cx;bestY=cy;bestWeight=weight;bestDistance=dist;
            }
        }
        if(bestWeight<=0) {
            float y=-(City.WIDTH/2f+City.HEIGHT/2f)*ISO_HALF_H;
            return new Target(0,y,-150,150,y-120,y+120);
        }

        // Prefer occupied building footprints to avoid framing vast empty road arms.
        boolean occupied=false,anyZone=false;
        int x0=Math.max(0,bestX-CORE_RADIUS),x1=Math.min(City.WIDTH-1,bestX+CORE_RADIUS);
        int y0=Math.max(0,bestY-CORE_RADIUS),y1=Math.min(City.HEIGHT-1,bestY+CORE_RADIUS);
        for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++) {
            Tile t=city.tile(x,y);
            if(t.type.isZone()) {
                anyZone=true;
                if(t.level>0)occupied=true;
            }
        }
        double sx=0,sy=0,total=0;
        float minX=Float.POSITIVE_INFINITY,maxX=Float.NEGATIVE_INFINITY;
        float minY=Float.POSITIVE_INFINITY,maxY=Float.NEGATIVE_INFINITY;
        for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++) {
            Tile t=city.tile(x,y);
            boolean included=occupied?(t.type.isZone() && t.level>0)
                :anyZone?t.type.isZone() :t.type==TileType.ROAD;
            if(!included)continue;
            float wx=(x-y)*ISO_HALF_W,wy=-(x+y)*ISO_HALF_H;
            int weight=t.level>0 && t.type.isZone()?5:1;
            sx+=wx*weight;sy+=wy*weight;total+=weight;
            minX=Math.min(minX,wx);maxX=Math.max(maxX,wx);
            minY=Math.min(minY,wy);maxY=Math.max(maxY,wy);
        }
        if(total==0)throw new IllegalStateException("Nonempty urban core without selected tiles");
        return new Target((float)(sx/total),(float)(sy/total),minX,maxX,minY,maxY);
    }
}
