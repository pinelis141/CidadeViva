package br.com.cidadeviva.sim;

/** Conserves four lots' population and jobs while consolidating a genuine 2x2 footprint. */
public final class CityBlocks {
    private CityBlocks(){}
    public static boolean anchor(City c,int x,int y){Tile t=c.tile(x,y);return t.block==0||t.block==x*City.HEIGHT+y+1;}
    public static void setLevel(City c,int x,int y,int level){
        Tile t=c.tile(x,y);if(t.block==0){t.level=level;return;}
        int ax=(t.block-1)/City.HEIGHT,ay=(t.block-1)%City.HEIGHT;
        for(int nx=ax;nx<ax+2;nx++)for(int ny=ay;ny<ay+2;ny++)c.tile(nx,ny).level=level;
    }
    public static void setFire(City c,int x,int y,int days){
        Tile t=c.tile(x,y);if(t.block==0){t.fireDays=days;return;}
        int ax=(t.block-1)/City.HEIGHT,ay=(t.block-1)%City.HEIGHT;
        for(int nx=ax;nx<ax+2;nx++)for(int ny=ay;ny<ay+2;ny++)c.tile(nx,ny).fireDays=days;
    }
    public static boolean consolidate(City c,int x,int y,CityEconomy e){
        if(x<0||y<0||x>=City.WIDTH-1||y>=City.HEIGHT-1||!c.powerEnabled()||!c.waterEnabled())return false;
        Tile first=c.tile(x,y);if(!first.type.isZone()||first.block!=0||first.level==3||e.demand(first.type)<24)return false;
        boolean road=false;
        for(int nx=x;nx<x+2;nx++)for(int ny=y;ny<y+2;ny++){
            Tile t=c.tile(nx,ny);
            if(t.type!=first.type||t.level!=first.level||t.block!=0||t.fireDays>0)return false;
            road|=c.hasRoad(nx,ny);
        }
        if(!road)return false;
        if(first.type==TileType.RESIDENTIAL&&(!nearService(c,x,y,TileType.SCHOOL,e)||!nearService(c,x,y,TileType.CLINIC,e)))return false;
        // Evaluate the actual shared perimeter before committing: interior lots
        // cannot have supply before consolidation, but can have it as one building.
        int id=x*City.HEIGHT+y+1;
        for(int nx=x;nx<x+2;nx++)for(int ny=y;ny<y+2;ny++)c.tile(nx,ny).block=id;
        boolean ready=prospectiveReady(c,x,y,Math.max(1,first.level));
        if(!ready){for(int nx=x;nx<x+2;nx++)for(int ny=y;ny<y+2;ny++)c.tile(nx,ny).block=0;return false;}
        c.invalidateEconomy();return true;
    }
    public static boolean readyForGrowth(City c,int x,int y){return prospectiveReady(c,x,y,c.tile(x,y).level+1);}
    private static boolean prospectiveReady(City c,int x,int y,int level){
        Tile t=c.tile(x,y);int original=t.level;boolean ready=true;
        try{
            setLevel(c,x,y,level);CityEconomy projected=CityEconomy.analyze(c);
            int ax=(t.block-1)/City.HEIGHT,ay=(t.block-1)%City.HEIGHT;
            for(int nx=ax;nx<ax+2;nx++)for(int ny=ay;ny<ay+2;ny++)
                if(!projected.utilities.hasSupply(nx,ny)||!projected.civics.healthy(nx,ny)||
                    (t.type==TileType.RESIDENTIAL&&(projected.neighborhoods.satisfactionAt(nx,ny)<65||projected.services.coveredAt(TileType.SCHOOL,nx,ny)<level*12||projected.services.coveredAt(TileType.CLINIC,nx,ny)<level*12)))ready=false;
        }finally{setLevel(c,x,y,original);}
        return ready;
    }
    private static boolean nearService(City c,int x,int y,TileType type,CityEconomy e){
        for(int sx=0;sx<City.WIDTH;sx++)for(int sy=0;sy<City.HEIGHT;sy++){
            if(c.tile(sx,sy).type!=type||c.money()==0||!c.hasRoad(sx,sy)||!e.utilities.hasSupply(sx,sy))continue;
            int[] distance=CityRoads.distances(c,sx,sy,CityServices.range(type));
            for(int nx=x;nx<x+2;nx++)for(int ny=y;ny<y+2;ny++)if(CityRoads.accessDistance(c,nx,ny,distance)!=Integer.MAX_VALUE)return true;
        }
        return false;
    }
    public static void formOne(City c,CityEconomy e){
        int ox=CityMath.floorMod(c.day()*17,City.WIDTH-1),oy=CityMath.floorMod(c.day()*23,City.HEIGHT-1);
        int attempts=0;
        for(int ix=0;ix<City.WIDTH-1;ix++)for(int iy=0;iy<City.HEIGHT-1;iy++){
            int x=(ix+ox)%(City.WIDTH-1),y=(iy+oy)%(City.HEIGHT-1);Tile first=c.tile(x,y);
            if(!first.type.isZone()||first.block!=0||first.level==3)continue;
            boolean match=true;
            for(int nx=x;nx<x+2;nx++)for(int ny=y;ny<y+2;ny++){Tile t=c.tile(nx,ny);if(t.type!=first.type||t.level!=first.level||t.block!=0)match=false;}
            if(!match)continue;
            if(consolidate(c,x,y,e))return;
            if(++attempts>=4)return;
        }
    }
    public static boolean valid(Tile[][] tiles){
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++){
            Tile t=tiles[x][y];if(t.block==0)continue;
            if(t.block<1||t.block>City.WIDTH*City.HEIGHT||!t.type.isZone())return false;
            int ax=(t.block-1)/City.HEIGHT,ay=(t.block-1)%City.HEIGHT;
            if(ax>=City.WIDTH-1||ay>=City.HEIGHT-1||x<ax||x>ax+1||y<ay||y>ay+1)return false;
            for(int nx=ax;nx<ax+2;nx++)for(int ny=ay;ny<ay+2;ny++){
                Tile other=tiles[nx][ny];if(other.block!=t.block||other.type!=t.type||other.level!=t.level||other.fireDays!=t.fireDays)return false;
            }
        }
        return true;
    }
}
