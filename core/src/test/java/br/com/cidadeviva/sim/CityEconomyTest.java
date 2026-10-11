package br.com.cidadeviva.sim;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import br.com.cidadeviva.input.BuildStroke;

/** Pure Java regression for demand, budgets, access and backward-compatible saves. */
public final class CityEconomyTest {
    private static int assertions;
    private static void check(boolean ok,String name) {
        assertions++;
        if (!ok) throw new AssertionError(name);
    }
    private static City roundTrip(City city) throws Exception {
        ByteArrayOutputStream data=new ByteArrayOutputStream();
        CityPersistence.save(city,data);
        return CityPersistence.load(new ByteArrayInputStream(data.toByteArray()));
    }
    private static int[] findEmptyRoadside(City city, TileType type) {
        for (int x=2; x<City.WIDTH-2;x++) for(int y=2;y<City.HEIGHT-2;y++) {
            if (city.canBuild(x,y,type) && city.hasRoad(x,y))return new int[]{x,y};
        }
        throw new AssertionError("No free roadside parcel");
    }
    public static void main(String[] args) throws Exception {
        City city=new City();
        CityEconomy first=city.economy();
        check(first.population==city.residents(),"population accurate");
        check(first.jobs==city.jobs(),"jobs accurate");
        check(first.employed==Math.min(first.population,first.jobs),"employment capped");
        check(first.unemployed==Math.max(0,first.population-first.jobs),"unemployment accurate");
        check(first.jobVacancies==Math.max(0,first.jobs-first.population),"vacancies accurate");
        check(first.net==first.grossRevenue-first.expenses,"budget balanced");
        check(first.grossRevenue==first.taxResidential+first.taxCommercial+first.taxIndustrial,"tax breakdown");
        check(first.expenses==first.roadMaintenance+first.zoneMaintenance,"cost breakdown");
        check(first.roadMaintenance==city.roads()*3,"road maintenance per tile");
        check(first.zoneMaintenance>city.zones()*2,"upgraded occupied buildings increase maintenance");
        for(int n:new int[]{first.residentialDemand,first.commercialDemand,first.industrialDemand})
            check(n>=5 && n<=95,"demands bounded");
        check(first.unserved==city.unservedZones(),"road count agrees");
        int[] open=findEmptyRoadside(city,TileType.INDUSTRIAL);
        city.build(open[0],open[1],TileType.INDUSTRIAL);
        check(city.economy().industrialDemand<=first.industrialDemand,"vacant industrial supply reduces demand");
        City original=roundTrip(city);
        check(original.economy().industrialDemand==city.economy().industrialDemand,"demand roundtrips without new save format");
        int[] remote=null;
        for(int x=0;x<City.WIDTH-1 && remote==null;x++)for(int y=0;y<City.HEIGHT-1;y++) {
            if(city.canBuild(x,y,TileType.RESIDENTIAL)&& !city.hasRoad(x,y)){
                remote=new int[]{x,y};break;
            }
        }
        check(remote!=null,"find isolated lot");
        city.build(remote[0],remote[1],TileType.RESIDENTIAL);
        int unservedBefore=city.unservedZones();
        for(int i=0;i<150;i++)city.nextDay();
        check(city.tile(remote[0],remote[1]).level==0,"isolated zone never grows");
        check(city.unservedZones()>=unservedBefore,"isolated zone remains unserved");
        check(city.day()==151,"daily advance");
        City after=roundTrip(city);
        check(after.day()==city.day(),"day preserved");
        check(after.money()==city.money(),"cash preserved");
        check(after.economy().net==city.economy().net,"economy recomputed consistently after load");
        // Verify growth is deterministic for the same persisted tiles and same day.
        City twin=roundTrip(city);
        for(int i=0;i<40;i++) {city.nextDay();twin.nextDay();}
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)
            check(city.tile(x,y).level==twin.tile(x,y).level,"deterministic growth after load");
        check(city.money()==twin.money(),"same cash after replay");
        // Roads still cost money even if nothing is built around them.
        City roads=new City();
        int oldExpenses=roads.economy().expenses;
        int[] empty=null;
        for(int x=0;x<City.WIDTH && empty==null;x++)for(int y=0;y<City.HEIGHT;y++)
            if(roads.canBuild(x,y,TileType.ROAD)){empty=new int[]{x,y};break;}
        roads.build(empty[0],empty[1],TileType.ROAD);
        check(roads.economy().expenses==oldExpenses+3,"new road has maintenance");
        System.out.println("OK: "+assertions+" verificacoes da economia, demanda, crescimento e saves.");
    }
}
