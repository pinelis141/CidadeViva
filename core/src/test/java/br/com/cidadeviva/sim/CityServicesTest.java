package br.com.cidadeviva.sim;

import java.io.*;
import java.util.zip.CRC32;

public final class CityServicesTest {
    private static int checks;
    private static void ok(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static City fixture(boolean services,int cash) {
        Tile[][] map=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)map[x][y]=new Tile(TileType.GRASS);
        for(int x=5;x<=30;x++) {map[x][10]=new Tile(TileType.ROAD);map[x][9]=new Tile(TileType.RESIDENTIAL,3);}
        if(services) {
            map[12][11]=new Tile(TileType.SCHOOL);map[14][11]=new Tile(TileType.CLINIC);map[16][11]=new Tile(TileType.PARK);
        }
        City city=new City();city.restore(map,cash,20,0);return city;
    }
    private static byte[] save(City city)throws Exception {ByteArrayOutputStream b=new ByteArrayOutputStream();CityPersistence.save(city,b);return b.toByteArray();}
    private static void crc(byte[] bytes) {
        CRC32 c=new CRC32();c.update(bytes,0,bytes.length-4);int v=(int)c.getValue();
        for(int i=0;i<4;i++)bytes[bytes.length-4+i]=(byte)(v>>>(24-i*8));
    }
    public static void main(String[] args)throws Exception {
        City city=fixture(true,50000);CityEconomy e=city.economy();
        ok(e.services.buildings==3 && e.services.activeBuildings==3,"all connected services active");
        ok(e.serviceMaintenance==62,"recurring costs are charged once");
        ok(e.expenses==e.roadMaintenance+e.zoneMaintenance+e.serviceMaintenance,"budget includes public services");
        for(TileType type:CityServices.TYPES) {
            ok(e.services.served(type)==CityServices.capacity(type),"single building capacity is conserved");
            int total=0;
            for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
                int covered=e.services.coveredAt(type,x,y);total+=covered;
                ok(covered>=0 && covered<=city.tile(x,y).level*12,"coverage cannot invent residents");
            }
            ok(total==e.services.served(type),"sum of household allocation equals report");
        }
        ok(e.services.coveredAt(TileType.SCHOOL,12,9)==36,"near homes receive available capacity first");
        int satisfaction=e.neighborhoods.satisfactionAt(14,9);
        ok(satisfaction>fixture(false,50000).economy().neighborhoods.satisfactionAt(14,9),"services improve actual local satisfaction");
        city.build(12,10,TileType.GRASS);
        ok(city.economy().services.served(TileType.SCHOOL)==0,"isolated school provides no coverage");
        ok(city.economy().serviceMaintenance==62,"isolated building still costs upkeep");
        City emptyCash=fixture(true,0);
        ok(emptyCash.economy().services.activeBuildings==0,"zero treasury suspends service operation");
        City overlap=fixture(true,50000);overlap.build(13,11,TileType.SCHOOL);
        ok(overlap.economy().services.served(TileType.SCHOOL)==360,"second school expands capacity without double-counting");
        City loaded=CityPersistence.load(new ByteArrayInputStream(save(overlap)));
        ok(loaded.tile(13,11).type==TileType.SCHOOL && loaded.economy().serviceMaintenance==overlap.economy().serviceMaintenance,"CVT4 retains service buildings and costs");
        for(int d=0;d<30;d++){overlap.nextDay();loaded.nextDay();ok(overlap.money()==loaded.money() && overlap.residents()==loaded.residents(),"services replay identically after save");}
        byte[] old=LegacyCitySave.bytes(fixture(false,50000),3);
        City migrated=CityPersistence.load(new ByteArrayInputStream(old));
        ok(migrated.residents()==fixture(false,50000).residents(),"CVT3 migrates without changing city");
        byte[] invalid=LegacyCitySave.bytes(fixture(true,50000),3);
        try{CityPersistence.load(new ByteArrayInputStream(invalid));throw new AssertionError("service smuggled into legacy CVT3");}catch(IOException expected){checks++;}
        City budget=new City();int money=budget.money();
        ok(budget.build(20,20,TileType.PARK).equals("Construído"),"park can be built");
        ok(budget.money()==money-350,"park construction cost exact");
        ok(budget.build(20,20,TileType.GRASS).equals("Terreno liberado"),"service demolition remains available");
        ok(budget.economy().serviceMaintenance==0,"demolition removes recurring service cost");
        Tile[][] small=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)small[x][y]=new Tile(TileType.GRASS);
        for(int x=5;x<=20;x++)small[x][10]=new Tile(TileType.ROAD);
        small[10][9]=new Tile(TileType.RESIDENTIAL,2);small[10][11]=new Tile(TileType.COMMERCIAL,3);
        City noServices=new City();noServices.restore(small,50000,1,0);
        City supported=CityPersistence.load(new ByteArrayInputStream(save(noServices)));
        supported.build(12,11,TileType.SCHOOL);supported.build(14,11,TileType.CLINIC);
        for(int d=0;d<150;d++){noServices.nextDay();supported.nextDay();}
        ok(noServices.tile(10,9).level<=2,"top residential density requires school and health");
        ok(supported.tile(10,9).level==3,"adequately served homes can reach top density");
        System.out.println("OK: "+checks+" service capacity, roads, budget, satisfaction and CVT3/CVT4 migration checks.");
    }
}
