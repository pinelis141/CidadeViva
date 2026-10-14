package br.com.cidadeviva.sim;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.CRC32;
import br.com.cidadeviva.input.BuildStroke;

public final class CityUrbanTest {
    private static int checks;
    private static void ok(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
    private static Tile[][] empty(){Tile[][] t=new Tile[City.WIDTH][City.HEIGHT];for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)t[x][y]=new Tile(TileType.GRASS);return t;}
    private static byte[] bytes(City c)throws Exception{ByteArrayOutputStream b=new ByteArrayOutputStream();CityPersistence.save(c,b);return b.toByteArray();}
    private static City reload(City c)throws Exception{return CityPersistence.load(new ByteArrayInputStream(bytes(c)));}
    private static City blockCity(){
        Tile[][] t=empty();for(int x=8;x<=20;x++)t[x][9]=new Tile(TileType.ROAD);
        for(int x=10;x<=11;x++)for(int y=10;y<=11;y++)t[x][y]=new Tile(TileType.RESIDENTIAL,1);
        t[15][10]=new Tile(TileType.COMMERCIAL,3);t[17][10]=new Tile(TileType.INDUSTRIAL,3);
        t[11][8]=new Tile(TileType.SCHOOL);t[12][8]=new Tile(TileType.CLINIC);t[13][8]=new Tile(TileType.PARK);
        t[14][8]=new Tile(TileType.POWER_PLANT);t[15][8]=new Tile(TileType.WATER_PUMP);
        City c=new City();c.restore(t,50000,20,0,new int[]{10,10,10},new int[]{0,0,0},3);return c;
    }
    private static City transitCity(boolean bus,boolean dense){
        Tile[][] t=empty();for(int x=5;x<=39;x++)t[x][10]=new Tile(TileType.ROAD);
        if(dense){for(int x=5;x<=8;x++){t[x][9]=new Tile(TileType.RESIDENTIAL,3);t[x][11]=new Tile(TileType.RESIDENTIAL,3);}for(int x=35;x<=38;x++){t[x][9]=new Tile(TileType.INDUSTRIAL,3);t[x][11]=new Tile(TileType.INDUSTRIAL,3);}}
        else{t[5][9]=new Tile(TileType.RESIDENTIAL,3);t[36][9]=new Tile(TileType.INDUSTRIAL,3);}
        if(bus){t[20][11]=new Tile(TileType.BUS_DEPOT);t[4][10]=new Tile(TileType.BUS_STOP);t[40][10]=new Tile(TileType.BUS_STOP);}
        City c=new City();c.restore(t,50000,20,0);return c;
    }
    private static void invalid(byte[] b)throws Exception{CRC32 crc=new CRC32();crc.update(b,0,b.length-4);ByteBuffer.wrap(b).putInt(b.length-4,(int)crc.getValue());try{CityPersistence.load(new ByteArrayInputStream(b));throw new AssertionError("corrupt urban save accepted");}catch(IOException e){checks++;}}
    public static void main(String[] args)throws Exception{
        City c=blockCity();int population=c.residents(),jobs=c.jobs();
        ok(!c.hasRoad(11,11),"back lot initially lacks its own road");
        ok(CityBlocks.consolidate(c,10,10,c.economy()),"supplied residential block consolidates with schools and health");
        ok(c.hasRoad(11,11)&&c.residents()==population&&c.jobs()==jobs,"shared road access and capacity conservation");
        ok(CityRoads.entries(c,11,11).length==2,"perimeter roads deduplicated");
        ok(c.economy().mobility.employed==population,"all group residents reach jobs through shared access");
        ok(CityBlocks.valid(newTileCopy(c)),"new group invariant");
        City twin=reload(c);ok(twin.tile(11,11).block==c.tile(10,10).block,"CVT7 retains complete footprint");
        for(int d=0;d<80;d++){c.nextDay();twin.nextDay();ok(c.money()==twin.money()&&c.residents()==twin.residents(),"urban replay");ok(CityBlocks.valid(newTileCopy(c)),"growth updates all four cells once");}
        City single=blockCity();CityBlocks.consolidate(single,10,10,single.economy());City area=reload(single);int before=single.money();
        single.build(11,11,TileType.GRASS);ok(single.money()==before+160,"demolishing any member refunds four original lots");
        for(int x=10;x<=11;x++)for(int y=10;y<=11;y++)ok(single.tile(x,y).type==TileType.GRASS&&single.tile(x,y).block==0,"single removal clears whole footprint");
        List<BuildStroke.Cell> targets=Arrays.asList(new BuildStroke.Cell(10,10),new BuildStroke.Cell(11,11),new BuildStroke.Cell(10,10));
        City.BuildResult preview=area.preview(targets,TileType.GRASS);ok(preview.cost==-160&&preview.built==4,"brush preview expands footprint without duplicate refunds");area.buildArea(targets,TileType.GRASS);ok(area.money()==single.money()&&CityBlocks.valid(newTileCopy(area)),"brush commits exactly the preview");
        City valid=blockCity();CityBlocks.consolidate(valid,10,10,valid.economy());byte[] corrupt=bytes(valid);
        ByteBuffer.wrap(corrupt).putShort(24+(11*City.HEIGHT+11)*5+2,(short)0);invalid(corrupt);
        corrupt=bytes(valid);corrupt[24+(11*City.HEIGHT+11)*5+1]=3;invalid(corrupt);
        City legacy=new City();legacy.borrow(CityLoan.MEDIUM);City migrated=CityPersistence.load(new ByteArrayInputStream(LegacyCitySave.bytes(legacy,6)));
        ok(migrated.loanBalance()==6720&&migrated.money()==legacy.money()&&migrated.urbanFlags()==0,"CVT6 migration preserves debt and keeps new requirements in planning");
        City civic=blockCity();ok(!civic.wasteEnabled()&&!civic.sewageEnabled(),"sanitation initially optional");
        civic.build(16,8,TileType.WASTE_CENTER);civic.build(17,8,TileType.SEWAGE_PLANT);civic.build(18,8,TileType.POLICE_STATION);civic.build(19,8,TileType.FIRE_STATION);
        ok(civic.wasteEnabled()&&civic.sewageEnabled()&&civic.firesEnabled(),"first facilities activate persistent flags");
        ok(civic.economy().civics.percent(TileType.WASTE_CENTER,10,10)==100,"finite garbage collection uses connected roads");
        ok(civic.economy().civics.crimeAt(10,10)<blockCity().economy().civics.crimeAt(10,10),"police reduces measurable crime risk");
        CityBlocks.setFire(civic,10,10,1);civic.invalidateEconomy();CityEmergencies.advance(civic,civic.economy());ok(civic.tile(10,10).fireDays==0,"fire brigade extinguishes covered fire before damage");
        civic.build(19,8,TileType.GRASS);CityBlocks.setFire(civic,10,10,1);
        for(int day=0;day<4;day++){civic.invalidateEconomy();CityEmergencies.advance(civic,civic.economy());}
        ok(civic.tile(10,10).level==0&&civic.tile(10,10).fireDays==0,"unprotected fire damages a level after four response days");
        civic.build(16,8,TileType.GRASS);civic.build(17,8,TileType.GRASS);civic=reload(civic);
        ok(civic.wasteEnabled()&&civic.sewageEnabled()&&!civic.economy().civics.healthy(11,10),"demolition and reload cannot bypass sanitation needs");
        CityBlocks.setFire(civic,11,10,1);civic.setFiresEnabled(false);ok(civic.tile(11,10).fireDays==0,"disabling optional fires clears active incidents");
        City noBus=transitCity(false,false),bus=transitCity(true,false);
        ok(noBus.economy().employed==0,"long commute unreachable without public transport");
        ok(bus.economy().mobility.transitRiders==36&&bus.economy().employed==36,"garage and two stops enable longer real job trips");
        ok(bus.economy().mobility.transit.activeStops==2,"only active connected stops counted");
        CityActivity activity=new CityActivity(bus);ok(!activity.buses().isEmpty(),"operating service has visible buses");
        for(CityActivity.Bus vehicle:activity.buses())for(int i=0;i<vehicle.pathLength();i++){int id=vehicle.roadId(i);ok(bus.tile(id/City.HEIGHT,id%City.HEIGHT).type==TileType.ROAD,"bus route stays on road");if(i>0){int prev=vehicle.roadId(i-1);ok(Math.abs(prev/City.HEIGHT-id/City.HEIGHT)+Math.abs(prev%City.HEIGHT-id%City.HEIGHT)==1,"bus route has no jumps");}}
        City dense=transitCity(true,true);ok(dense.economy().mobility.transitRiders==240,"single garage cannot exceed 240 daily seats");
        dense.build(21,11,TileType.BUS_DEPOT);ok(dense.economy().mobility.transitRiders==288,"second connected garage expands shared network capacity");
        bus.build(20,10,TileType.GRASS);ok(bus.economy().mobility.transitRiders==0,"broken route suspends bus commuting");
        for(CityLayer layer:CityLayer.values())for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++){int score=layer.score(c,c.economy(),x,y);ok(score>=-1&&score<=100,"layer bounded and covers all terrain types");}
        CityEconomy e=c.economy();ok(e.neighborhoods.popularResidents+e.neighborhoods.middleResidents+e.neighborhoods.wealthyResidents==e.population,"social groups conserve population");
        ok(e.expenses==e.roadMaintenance+e.zoneMaintenance+e.serviceMaintenance+e.loanPayment,"new services counted once in budget");
        System.out.println("OK: "+checks+" urban footprint, sanitation, safety, social classes, transit, overlays and CVT7 checks.");
    }
    private static Tile[][] newTileCopy(City c){Tile[][] t=new Tile[City.WIDTH][City.HEIGHT];for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)t[x][y]=c.tile(x,y);return t;}
}
