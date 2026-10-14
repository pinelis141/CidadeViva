package br.com.cidadeviva.sim;

import java.io.*;
import java.util.Arrays;
import java.util.zip.CRC32;

public final class CityUtilitiesTest {
    private static int checks;
    private static void ok(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static City fixture(boolean dense) {
        Tile[][] map=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)map[x][y]=new Tile(TileType.GRASS);
        for(int x=5;x<=35;x++)map[x][10]=new Tile(TileType.ROAD);
        if(dense)for(int x=5;x<=34;x++)map[x][9]=new Tile(TileType.RESIDENTIAL,3);
        else {
            map[10][9]=new Tile(TileType.RESIDENTIAL,2);map[12][9]=new Tile(TileType.COMMERCIAL,3);
            map[14][9]=new Tile(TileType.INDUSTRIAL,2);map[11][11]=new Tile(TileType.SCHOOL);
        }
        City c=new City();c.restore(map,50000,1,0);return c;
    }
    private static byte[] save(City c)throws Exception {ByteArrayOutputStream b=new ByteArrayOutputStream();CityPersistence.save(c,b);return b.toByteArray();}
    private static City reload(City c)throws Exception{return CityPersistence.load(new ByteArrayInputStream(save(c)));}
    private static void crc(byte[] b){CRC32 c=new CRC32();c.update(b,0,b.length-4);int v=(int)c.getValue();for(int i=0;i<4;i++)b[b.length-4+i]=(byte)(v>>>(24-i*8));}
    public static void main(String[] args)throws Exception {
        City city=fixture(false);int employed=city.economy().employed;
        ok(!city.powerEnabled() && !city.waterEnabled(),"legacy city begins in explicit planning mode");
        int before=city.money();city.build(6,11,TileType.POWER_PLANT);
        ok(city.powerEnabled() && !city.waterEnabled(),"first generator activates only electricity");
        ok(city.money()==before-2600,"exact construction cost");
        CityUtilities grid=city.economy().utilities;
        ok(grid.capacity(0)==600 && grid.served(0)==grid.demand(0),"connected generator supplies finite demand");
        city.build(8,11,TileType.WATER_PUMP);
        ok(city.waterEnabled() && city.economy().utilities.served(1)>0,"water activates separately");
        ok(city.economy().serviceMaintenance==84,"school plus two utilities maintenance counted once");
        int satisfaction=city.economy().neighborhoods.satisfactionAt(10,9);
        city.build(7,10,TileType.GRASS);
        ok(city.economy().utilities.served(0)==0,"road break cannot transmit energy by proximity");
        ok(city.economy().utilities.capacity(1)==0,"unpowered pump shuts down once energy is active");
        ok(city.economy().services.served(TileType.SCHOOL)==0,"public service without supplies shuts down");
        ok(city.economy().employed==0 && employed>0,"unsupplied businesses stop offering operational jobs");
        ok(city.economy().neighborhoods.satisfactionAt(10,9)<satisfaction,"utility shortage affects residents");
        city.build(7,10,TileType.ROAD);
        ok(city.economy().employed==employed,"repair restores real jobs and supply");
        City dense=fixture(true);dense.build(10,11,TileType.POWER_PLANT);
        ok(dense.economy().utilities.served(0)==600,"generator cannot exceed rated capacity");
        ok(dense.economy().utilities.demand(0)==1080,"residential demand follows population");
        dense.build(20,11,TileType.POWER_PLANT);
        ok(dense.economy().utilities.served(0)==1080,"second source expands supply without duplicates");
        int sum=0;for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)sum+=dense.economy().utilities.suppliedAt(0,x,y);
        ok(sum==1080,"per-consumer supply conserves generated units");
        city.build(6,11,TileType.GRASS);
        City loaded=reload(city);
        ok(loaded.powerEnabled() && loaded.economy().utilities.capacity(0)==0,"demolition and reload do not disable network requirements");
        for(int d=0;d<30;d++){city.nextDay();loaded.nextDay();ok(city.money()==loaded.money() && city.residents()==loaded.residents(),"network replay deterministic");}
        byte[] v5=save(fixture(false));byte[] v4=Arrays.copyOf(v5,v5.length-1);v4[3]='4';crc(v4);
        City old=CityPersistence.load(new ByteArrayInputStream(v4));
        ok(!old.powerEnabled() && old.residents()==24,"CVT4 migration preserves households without sudden outage");
        City failed=fixture(false);int cash=failed.money();
        failed.build(-1,-1,TileType.POWER_PLANT);
        ok(!failed.powerEnabled() && failed.money()==cash,"invalid build cannot activate requirements");
        System.out.println("OK: "+checks+" energy/water capacity, connectivity, effects, state and CVT4/CVT5 migration checks.");
    }
}
