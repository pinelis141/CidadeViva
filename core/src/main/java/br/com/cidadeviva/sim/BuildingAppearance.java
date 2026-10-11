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
        // All inhabited tiles use one coherent art family. The old pixelated
        // purple buildings are deliberately retired from rendering; their
        // atlas indices remain untouched to avoid invalidating other assets.
        // Every tier has its own visibly different approved 1x1 silhouette.
        return 116+kind*3+tier;
    }
}
