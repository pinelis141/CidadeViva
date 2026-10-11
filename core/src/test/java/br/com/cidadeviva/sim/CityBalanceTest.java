package br.com.cidadeviva.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
 * Economic scenarios instead of only unit assertions: idle growth, taxation,
 * road access, operation costs, crisis recovery and repeatable saves.
 */
public final class CityBalanceTest {
    private static int checks;
    private static void ok(boolean value,String description) {
        checks++;
        if(!value)throw new AssertionError(description);
    }
    private static City reload(City city)throws Exception {
        ByteArrayOutputStream dst=new ByteArrayOutputStream();
        CityPersistence.save(city,dst);
        return CityPersistence.load(new ByteArrayInputStream(dst.toByteArray()));
    }
    private static void advance(City city,int days) {
        for(int i=0;i<days;i++)city.nextDay();
    }
    private static void invariant(City c) {
        CityEconomy r=c.economy();
        ok(r.employed<=r.population && r.employed<=r.operationalJobs,"real employment bounded");
        ok(r.operationalJobs<=r.jobs && r.inactiveJobs==r.jobs-r.operationalJobs,"job access accounted for");
        ok(r.employed+r.unemployed==r.population,"labor conservation");
        ok(r.jobVacancies==r.operationalJobs-r.employed,"jobs not silently filled");
        ok(r.grossRevenue==r.taxResidential+r.taxCommercial+r.taxIndustrial,"tax sum exact");
        ok(r.expenses==r.roadMaintenance+r.zoneMaintenance,"maintenance sum exact");
        ok(r.net==r.grossRevenue-r.expenses,"net revenue exact");
        ok(r.qualityOfLife>=5&&r.qualityOfLife<=95,"quality bounded");
        ok(r.residentialDemand>=5&&r.residentialDemand<=95,"residential demand bounded");
        ok(r.commercialDemand>=5&&r.commercialDemand<=95,"commercial demand bounded");
        ok(r.industrialDemand>=5&&r.industrialDemand<=95,"industrial demand bounded");
        ok(c.money()>=0 && c.day()>=1,"CVT3-compatible treasury and days");
    }
    private static int[] findRoadside(City c) {
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)
            if(c.canBuild(x,y,TileType.RESIDENTIAL)&&c.hasRoad(x,y))return new int[]{x,y};
        throw new AssertionError("No roadside lot");
    }
    public static void main(String[] args) throws Exception {
        City plain=new City();
        CityEconomy initial=plain.economy();
        ok(initial.qualityOfLife>=50,"starter city has a credible base quality");
        ok(initial.residentialDemand>=24,"starter town can grow");
        ok(initial.unemployed==0,"starter jobs cover all initial households");
        ok(initial.operationalJobs>0,"jobs start with road access");
        int startMoney=plain.money();
        int deficitDays=0;
        for(int d=0;d<100;d++) {
            invariant(plain);
            if(plain.economy().net<0)deficitDays++;
            int prevCash=plain.money(),net=plain.economy().net;
            plain.nextDay();
            ok(plain.money()==Math.min(Integer.MAX_VALUE,Math.max(0L,(long)prevCash+net)),
                "daily budget applied exactly once");
            ok(plain.lastIncome()==net,"report matches last day's actual ledger");
        }
        ok(plain.residents()>initial.population,"starter city grows without dying immediately");
        ok(plain.money()<startMoney*2,"idle simulation does not create unlimited wealth");
        ok(deficitDays>0,"upgrading buildings eventually has a maintenance downside");
        ok(plain.money()>0,"100-day idle town is not immediately bankrupt");
        CityEconomy idle=plain.economy();

        City highTax=new City();
        City lowTax=new City();
        City moderate=new City();
        for(TileType t:new TileType[]{TileType.RESIDENTIAL,TileType.COMMERCIAL,TileType.INDUSTRIAL}) {
            highTax.setTaxRate(t,20);
            lowTax.setTaxRate(t,5);
            moderate.setTaxRate(t,12);
        }
        ok(highTax.economy().grossRevenue<initial.grossRevenue,
            "20% tax cannot double money without reducing taxable activity");
        ok(lowTax.economy().grossRevenue<initial.grossRevenue,"5% tax sacrifices current revenue");
        ok(moderate.economy().grossRevenue>=initial.grossRevenue,"moderate tax gives a limited fiscal gain");
        ok(highTax.economy().residentialDemand<initial.residentialDemand,"high tax reduces housing demand");
        ok(lowTax.economy().residentialDemand>initial.residentialDemand,"low tax improves housing demand");
        advance(highTax,100);advance(lowTax,100);advance(moderate,100);
        ok(highTax.money()<plain.money(),"20% tax loses fiscal effectiveness over 100 days");
        ok(highTax.residents()<=plain.residents(),"high tax does not speed population expansion");
        invariant(highTax);invariant(lowTax);invariant(moderate);

        // Without households, even a district full of workplaces pays no
        // commercial/industrial employment tax. Slots remain roads/zones.
        City noResidents=new City();
        Tile[][] allJobs=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
            Tile src=noResidents.tile(x,y);
            allJobs[x][y]=src.type==TileType.RESIDENTIAL ? new Tile(TileType.INDUSTRIAL,2)
                : new Tile(src.type,src.level);
        }
        noResidents.restore(allJobs,1000,1,0);
        CityEconomy emptyLabor=noResidents.economy();
        ok(emptyLabor.population==0,"no resident fixture");
        ok(emptyLabor.qualityOfLife<=20,"an empty city cannot advertise high life quality");
        ok(emptyLabor.jobs>0 && emptyLabor.employed==0,"jobs are not automatically filled");
        ok(emptyLabor.taxCommercial==0 && emptyLabor.taxIndustrial==0,
            "unstaffed businesses produce no employment revenue");
        ok(emptyLabor.jobVacancies==emptyLabor.operationalJobs,"all real vacancies counted");
        invariant(noResidents);

        // Disconnect a starter house from its only road. Residents are still
        // part of the saved city, but inaccessible homes stop paying taxes.
        City disconnected=new City();
        CityEconomy connected=disconnected.economy();
        ok(disconnected.tile(21,23).type==TileType.RESIDENTIAL,"starter edge dwelling exists");
        ok(disconnected.tile(21,24).type==TileType.ROAD,"edge road exists");
        ok(disconnected.build(21,24,TileType.GRASS).equals("Terreno liberado"),"road removed");
        CityEconomy broken=disconnected.economy();
        ok(broken.population==connected.population,"removing access does not erase residents");
        ok(broken.accessiblePopulation<connected.accessiblePopulation,"road access limits taxable households");
        ok(broken.taxResidential<connected.taxResidential,"inaccessible household tax falls");
        ok(broken.unserved>connected.unserved,"missing road creates actionable warning");

        // Square-law level costs: dense skyscrapers cost significantly more
        // than low-density lots, even with the same number of zones and roads.
        City tall=new City();
        Tile[][] elevated=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
            Tile t=tall.tile(x,y);
            elevated[x][y]=new Tile(t.type,t.type.isZone()?3:t.level);
        }
        int lowMaintenance=tall.economy().zoneMaintenance;
        tall.restore(elevated,15000,1,0);
        ok(tall.economy().zoneMaintenance>lowMaintenance*4,
            "high-density neighborhoods have superlinear operating cost");
        ok(tall.economy().net<initial.net,"premature high rises worsen finances");

        // Bankrupt players must always be able to demolish expensive structures.
        City crisis=new City();
        Tile[][] crisisMap=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
            Tile t=crisis.tile(x,y);crisisMap[x][y]=new Tile(t.type,t.level);
        }
        crisis.restore(crisisMap,0,30,-100);
        int beforeExpenses=crisis.economy().expenses;
        ok(crisis.cost(TileType.GRASS)==0,"demolition remains free in bankruptcy");
        ok(crisis.build(21,24,TileType.GRASS).equals("Terreno liberado"),
            "player can demolish with empty treasury");
        ok(crisis.economy().expenses<beforeExpenses,"demolition reduces recurring costs");
        ok(crisis.economy().residentialDemand<initial.residentialDemand,
            "negative budget with empty treasury reduces investment pressure");
        invariant(crisis);
        ok(!crisis.canBuild(0,31,TileType.GRASS) || crisis.tile(0,31).type!=TileType.WATER,
            "free demolition never turns water into buildable real estate");

        // Persisted forecasts and replay must be exactly equal after many days.
        City actual=new City();
        int[] coords=findRoadside(actual);
        actual.build(coords[0],coords[1],TileType.COMMERCIAL);
        actual.setTaxRate(TileType.RESIDENTIAL,12);
        actual.setTaxRate(TileType.INDUSTRIAL,9);
        advance(actual,32);
        City reloaded=reload(actual);
        for(int d=0;d<125;d++) {
            CityEconomy a=actual.economy(),b=reloaded.economy();
            ok(a.net==b.net && a.qualityOfLife==b.qualityOfLife &&
                a.population==b.population && a.operationalJobs==b.operationalJobs &&
                a.residentialDemand==b.residentialDemand &&
                a.commercialDemand==b.commercialDemand && a.industrialDemand==b.industrialDemand,
                "CVT3 reload economy deterministic");
            actual.nextDay();reloaded.nextDay();
            ok(actual.money()==reloaded.money() && actual.day()==reloaded.day(),
                "CVT3 replay treasury deterministic");
        }
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)
            ok(actual.tile(x,y).level==reloaded.tile(x,y).level,"CVT3 replay tiles stable");

        // Tax ranges, terrain variation and 200-day simulation invariants.
        Random rng=new Random(0xC17A2026L);
        for(int scenario=0;scenario<18;scenario++) {
            City candidate=new City(scenario%3);
            for(TileType type:new TileType[]{TileType.RESIDENTIAL,TileType.COMMERCIAL,TileType.INDUSTRIAL})
                candidate.setTaxRate(type,5+rng.nextInt(16));
            for(int d=0;d<90;d++) {
                invariant(candidate);
                candidate.nextDay();
            }
            City saved=reload(candidate);
            ok(saved.economy().net==candidate.economy().net,"scenario saves its forecast");
        }
        System.out.println("OK: "+checks+" verificacoes de balanceamento e cenarios CVT3.");
        System.out.println("SIM 100 DIAS | 10% dinheiro="+plain.money()+" pop="+idle.population+
            " net="+idle.net+" | 20% dinheiro="+highTax.money()+
            " | 5% dinheiro="+lowTax.money()+" | 12% dinheiro="+moderate.money());
    }
}
