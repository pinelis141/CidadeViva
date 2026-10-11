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
        // The approved Buildings V3 designs have unmistakable progression:
        // small house/kiosk/shed -> townhome/cafe/warehouse -> apartment/
        // commercial tower/factory. They appear in six of seven locations;
        // one coordinate family retains architectural variety from Alpha 2.
        if(v<6)return 116+kind*3+tier; // new, clearly different silhouette per tier
        return 80+kind*12+tier*4+2; // legacy neighborhood alternate
    }
}
