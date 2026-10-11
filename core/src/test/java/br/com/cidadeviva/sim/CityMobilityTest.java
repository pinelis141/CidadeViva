package br.com.cidadeviva.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

/** Connected road travel, travel limits, real staffing and CVT3 replay. */
public final class CityMobilityTest {
    private static int checks;
    private static void ok(boolean value,String reason) {
        checks++;if(!value)throw new AssertionError(reason);
    }
    private static City fixture(boolean extraHome,boolean breakRoad) {
        City city=new City();
        Tile[][] map=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)
            map[x][y]=new Tile(TileType.GRASS);
        for(int x=5;x<=36;x++)map[x][20]=new Tile(TileType.ROAD);
        map[6][19]=new Tile(TileType.RESIDENTIAL,3); // 36 residents
        map[9][21]=new Tile(TileType.COMMERCIAL,2); // 20 jobs
        map[35][21]=new Tile(TileType.INDUSTRIAL,1); // 18 distant jobs
        if(extraHome)map[34][19]=new Tile(TileType.RESIDENTIAL,2);
        if(breakRoad)map[8][20]=new Tile(TileType.GRASS);
        city.restore(map,12000,1,0);
        return city;
    }
    private static City reload(City c) throws Exception {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        CityPersistence.save(c,out);
        return CityPersistence.load(new ByteArrayInputStream(out.toByteArray()));
    }
    public static void main(String[] args) throws Exception {
        City roadTown=fixture(false,false);
        CityEconomy r=roadTown.economy();
        ok(r.population==36&&r.operationalJobs==38,"fixture workers and jobs");
        ok(r.employed==20,"distant industry cannot teleport staff along 29-step route");
        ok(r.commutingResidents==36,"homes can see nearby commercial work");
        ok(r.reachableJobs==20,"reachable capacity excludes distant industrial plant");
        ok(r.jobVacancies==18&&r.unemployed==16,"vacancies and unemployment can coexist");
        ok(r.taxCommercial>0&&r.taxIndustrial==0,"only staffed jobs pay business taxes");
        ok(r.mobility.trafficAt(8,20)>0&&r.mobility.trafficAt(30,20)==0,
            "commuting traffic travels only the actual route");
        ok(r.congestedRoads>=0,"congestion count is never negative");
        ok(r.commuteSteps>0&&r.commuteSteps<=CityMobility.MAX_COMMUTE_STEPS+2,
            "average commute includes access walking steps");

        City both=fixture(true,false);
        CityEconomy joined=both.economy();
        ok(joined.employed==38&&joined.taxIndustrial>0,"nearby industrial workers really fill jobs");
        ok(joined.mobility.trafficAt(34,20)>0,"second neighborhood creates real trips");
        ok(joined.jobVacancies==0,"both workplaces are staffed if housing is close");
        ok(joined.unemployed==22,"extra households remain unemployed after vacancies filled");

        City cut=fixture(true,true);
        CityEconomy separated=cut.economy();
        ok(separated.employed<joined.employed,"severing a road isolates commuting access");
        ok(separated.disconnectedResidents>0,"isolated homes detected separately from lacking jobs");
        ok(separated.mobility.trafficAt(8,20)==0,"removed road never holds traffic");
        ok(separated.taxResidential<joined.taxResidential,"road loss reduces employed household tax");
        ok(separated.taxCommercial==0,"disconnected commercial jobs pay no business tax");
        ok(separated.taxIndustrial>0,"connected industry still pays business tax");
        ok(separated.population==joined.population,"road damage does not delete inhabitants");

        ok("Construído".equals(cut.build(8,20,TileType.ROAD)),"broken road can be repaired");
        ok(cut.economy().employed==joined.employed,
            "reconnecting bridges restores staffed employment immediately");
        ok(cut.economy().mobility.trafficAt(8,20)>0,
            "cached traffic is invalidated when roads change");

        City noWork=fixture(false,false);
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
            Tile t=noWork.tile(x,y);
            if(t.type==TileType.COMMERCIAL||t.type==TileType.INDUSTRIAL)t.level=0;
        }
        // No cached report yet: direct fixture mutation is intentionally only before analysis.
        CityEconomy unemployed=noWork.economy();
        ok(unemployed.employed==0&&unemployed.jobVacancies==0,"residents without jobs remain unemployed");
        ok(unemployed.taxCommercial==0&&unemployed.taxIndustrial==0,"no phantom business income");

        City before=fixture(true,false);
        City saved=reload(before);
        for(int d=0;d<35;d++) {
            CityEconomy a=before.economy(),b=saved.economy();
            ok(a.employed==b.employed&&a.commuteSteps==b.commuteSteps&&a.net==b.net,
                "road commute and budget replay exactly across saves");
            for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)
                ok(a.mobility.trafficAt(x,y)==b.mobility.trafficAt(x,y),"road loads reproducible");
            before.nextDay();saved.nextDay();
            ok(before.money()==saved.money()&&before.day()==saved.day(),"daily state reproducible");
        }
        City busy=new City();
        Tile[][] busyMap=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)
            busyMap[x][y]=new Tile(TileType.GRASS);
        for(int x=5;x<=20;x++)busyMap[x][20]=new Tile(TileType.ROAD);
        for(int x=6;x<=9;x++)busyMap[x][19]=new Tile(TileType.RESIDENTIAL,3);
        for(int x=12;x<=17;x++)busyMap[x][21]=new Tile(TileType.COMMERCIAL,3);
        busy.restore(busyMap,18000,1,0);
        CityEconomy peak=busy.economy();
        ok(peak.employed==144,"four busy residential buildings staff 144 positions");
        ok(peak.mobility.trafficAt(10,20)>=80,"single corridor accumulates actual traffic");
        ok(peak.congestedRoads>0,"rush-hour bottleneck affects urban quality");

        Random random=new Random(0x51AA77);
        for(int scenario=0;scenario<12;scenario++) {
            City stress=new City(scenario);
            // Add / remove roads and zones to challenge the bounded road matching.
            for(int i=0;i<120;i++) {
                int x=random.nextInt(City.WIDTH),y=random.nextInt(City.HEIGHT);
                if(stress.canBuild(x,y,TileType.ROAD)&&random.nextBoolean())stress.build(x,y,TileType.ROAD);
            }
            for(int day=0;day<25;day++) {
                CityEconomy a=stress.economy();
                ok(a.congestedRoads>=0&&a.congestedRoads<=stress.roads(),"congestion is road-scoped");
                ok(a.employed>=0&&a.employed<=a.accessiblePopulation,"never allocate imaginary workers");
                ok(a.employed<=a.operationalJobs,"never overfill a real position");
                ok(a.commutingResidents<=a.accessiblePopulation,"reachable homes are accessible");
                ok(a.mobility.commercialWorkers+a.mobility.industrialWorkers==a.employed,
                    "worker allocations conserved by sector");
                stress.nextDay();
            }
        }
        System.out.println("OK: "+checks+" verificacoes de rede viaria, trajetos, emprego e CVT3.");
    }
}
