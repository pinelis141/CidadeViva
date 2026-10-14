package br.com.cidadeviva.sim;

/** Bounded deterministic fires; fully covered buildings are protected, damaged levels can regrow. */
public final class CityEmergencies {
    private CityEmergencies(){}
    public static void advance(City c,CityEconomy e){
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++){
            Tile t=c.tile(x,y);if(!t.type.isZone()||t.level==0||!CityBlocks.anchor(c,x,y))continue;
            if(t.fireDays>0){
                if(e.civics.percent(TileType.FIRE_STATION,x,y)==100){CityBlocks.setFire(c,x,y,0);continue;}
                if(t.fireDays>=4){int from=t.level;CityBlocks.setLevel(c,x,y,t.level-1);c.recordDamage(x,y,from,t.level);CityBlocks.setFire(c,x,y,0);}
                else CityBlocks.setFire(c,x,y,t.fireDays+1);
            }else if(c.firesEnabled()&&e.civics.percent(TileType.FIRE_STATION,x,y)<100){
                int hash=c.day()*0x9e3779b9^x*0x85ebca6b^y*0xc2b2ae35;hash^=hash>>>16;
                int chance=t.type==TileType.INDUSTRIAL?5:2;
                if(CityMath.floorMod(hash,10000)<chance)CityBlocks.setFire(c,x,y,1);
            }
        }
    }
}
