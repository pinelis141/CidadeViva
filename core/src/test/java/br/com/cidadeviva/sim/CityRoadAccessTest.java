package br.com.cidadeviva.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/** Regression checks: zoned land grows only with directly adjacent streets. */
public final class CityRoadAccessTest {
    private static int assertions;
    private static void check(boolean result,String message){
        assertions++; if(!result)throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        City city=new City();
        int x=18,y=18; // protected open central terrain in the procedural seed
        check(city.canBuild(x,y,TileType.COMMERCIAL),"free lot for zone");
        check(!city.hasRoad(x,y),"initial lot has no direct road");
        int old=city.unservedZones();
        check(city.build(x,y,TileType.COMMERCIAL).equals("Construído"),"zonable land can be designated");
        check(city.unservedZones()==old+1,"unserved counter increments");
        check(city.zoneDescription(x,y).contains("SEM RUA ADJACENTE"),"lot status explains the limitation");
        for(int i=0;i<180;i++)city.nextDay();
        check(city.tile(x,y).level==0,"a lot cannot grow without a neighbouring road");
        check(city.canBuild(x+1,y,TileType.ROAD),"adjacent land available for street");
        check(city.build(x+1,y,TileType.ROAD).equals("Construído"),"street can be built");
        check(city.hasRoad(x,y),"lot now has direct road");
        check(city.unservedZones()==old,"unserved counter decreases after building street");
        check(city.zoneDescription(x,y).contains("RUA AO LADO"),"status changes after connecting street");
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        CityPersistence.save(city,out);
        City restored=CityPersistence.load(new ByteArrayInputStream(out.toByteArray()));
        check(restored.hasRoad(x,y),"load preserves street adjacency");
        check(restored.unservedZones()==old,"load recomputes correct count");
        check(restored.zoneDescription(-1,0).equals("Fora do mapa"),"out-of-map inspection safe");
        check(!restored.zoneDescription(20,20).isEmpty(),"non-zoned inspection safe");
        System.out.println("OK: "+assertions+" access-road and save regression checks.");
    }
}
