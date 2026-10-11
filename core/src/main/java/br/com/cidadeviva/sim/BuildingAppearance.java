package br.com.cidadeviva.sim;

/** Stable visual-only variants; IDs do not enter save files or economy. */
public final class BuildingAppearance {
    private BuildingAppearance() { }

    public static int variant(int x,int y) {
        // Coordinates alone define a neighborhood's style through every upgrade.
        int hash=(x*0x45d9f3b)^(y*0x119de1f3)^(x*y*0x27d4eb2d);
        hash^=hash>>>16;
        return Math.floorMod(hash,7);
    }

    public static int sprite(TileType type,int level,int x,int y) {
        if(!type.isZone() || level<1) throw new IllegalArgumentException("Occupied R/C/I only");
        int kind=type==TileType.RESIDENTIAL?0:type==TileType.COMMERCIAL?1:2;
        int tier=Math.min(level,3)-1;
        int v=variant(x,y);
        if(v==2)return 71+kind*3+tier; // original Alpha 3 third style
        if(v<2)return 27+kind*6+tier*2+v; // original two styles
        return 80+kind*12+tier*4+(v-3); // 4 new styles per level
    }
}
