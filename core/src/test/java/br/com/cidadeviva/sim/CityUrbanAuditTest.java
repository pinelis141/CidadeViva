package br.com.cidadeviva.sim;

import java.io.*;
import java.util.*;

/** Longer mixed-system scenarios, independent accounting invariants and legacy migration. */
public final class CityUrbanAuditTest {
    private static int checks;
    private static void ok(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    private static City reload(City c)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();CityPersistence.save(c,out);return CityPersistence.load(new ByteArrayInputStream(out.toByteArray()));}
    private static void place(Tile[][] map,TileType t){for(int x=9;x<39;x++)for(int y=9;y<39;y++)if(map[x][y].type!=TileType.ROAD&&(x%4==1||y%4==1)){map[x][y]=new Tile(t);return;}}
    private static City city(int seed){
        Random r=new Random(seed);Tile[][] map=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++){
            if(x<8||y<8||x>39||y>39)map[x][y]=new Tile(TileType.FOREST);
            else if(x%4==0||y%4==0)map[x][y]=new Tile(TileType.ROAD);
            else{TileType t=new TileType[]{TileType.RESIDENTIAL,TileType.RESIDENTIAL,TileType.COMMERCIAL,TileType.INDUSTRIAL}[r.nextInt(4)];map[x][y]=new Tile(t,r.nextInt(3));}
        }
        // Distinct source positions, intentionally finite: supply shortages are legitimate.
        int at=0;TileType[] facilities={TileType.POWER_PLANT,TileType.WATER_PUMP,TileType.POWER_PLANT,TileType.WATER_PUMP,TileType.PARK,TileType.SCHOOL,TileType.CLINIC,TileType.FIRE_STATION,TileType.POLICE_STATION,TileType.WASTE_CENTER,TileType.SEWAGE_PLANT,TileType.BUS_DEPOT,TileType.BUS_STOP,TileType.BUS_STOP};
        for(TileType t:facilities){int x=9+at%7*4,y=9+at/7*4;map[x][y]=new Tile(t);at++;}
        City c=new City();c.restore(map,200000,40,0,new int[]{10,10,10},new int[]{0,0,0},3);c.restoreUrbanFlags(7);return c;
    }
    private static void invariant(City c){
        CityEconomy e=c.economy();ok(e.population==c.residents()&&e.jobs==c.jobs(),"tile ledger and aggregate counts agree");
        ok(e.employed<=e.population&&e.employed<=e.operationalJobs&&e.employed>=0,"employment conservation");
        ok(e.mobility.transitRiders<=e.employed&&e.mobility.transitRiders<=e.mobility.transit.totalCapacity,"bus seats bound real employees");
        ok(e.grossRevenue==e.taxResidential+e.taxCommercial+e.taxIndustrial,"tax ledger exact");
        ok(e.expenses==e.roadMaintenance+e.zoneMaintenance+e.serviceMaintenance+e.loanPayment&&e.net==e.grossRevenue-e.expenses,"all maintenance and debt counted once");
        ok(e.neighborhoods.popularResidents+e.neighborhoods.middleResidents+e.neighborhoods.wealthyResidents==e.population,"social classes conserve residents");
        for(int k=0;k<2;k++){ok(e.utilities.served(k)<=e.utilities.demand(k)&&e.utilities.served(k)<=e.utilities.capacity(k),"utility allocation conserves resources");}
        for(TileType t:CityCivics.TYPES)ok(e.civics.served(t)<=e.civics.capacityFor(t),"civic service cannot exceed capacity");
        Tile[][] tiles=new Tile[City.WIDTH][City.HEIGHT];for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++){Tile t=c.tile(x,y);tiles[x][y]=t;ok(t.level>=0&&t.level<=3&&t.fireDays>=0&&t.fireDays<=4,"tile values bounded");ok(t.level>0||t.fireDays==0,"no fire survives an empty lot");}
        ok(CityBlocks.valid(tiles)&&c.money()>=0&&CityLoan.validDebt(c.loanBalance(),c.loanInstallment()),"building, treasury and credit invariants");
    }
    public static void main(String[] args)throws Exception{
        long started=System.nanoTime();
        for(int seed=0;seed<3;seed++){
            City c=city(seed);if(seed==1)c.borrow(CityLoan.SMALL);City replay=reload(c);Random actions=new Random(seed+19);
            for(int d=0;d<100;d++){
                if(d%17==0){int x=actions.nextInt(City.WIDTH),y=actions.nextInt(City.HEIGHT);c.build(x,y,TileType.GRASS);replay.build(x,y,TileType.GRASS);}
                invariant(c);c.nextDay();replay.nextDay();
                ok(c.money()==replay.money()&&c.residents()==replay.residents()&&c.loanBalance()==replay.loanBalance()&&c.economy().net==replay.economy().net,"mixed-system replay deterministic");
                if(d%20==0)replay=reload(c);
            }
        }
        for(int v=1;v<=6;v++){
            City old=new City();if(v==6)old.borrow(CityLoan.SMALL);
            City upgraded=CityPersistence.load(new ByteArrayInputStream(LegacyCitySave.bytes(old,v)));
            ok(upgraded.money()==old.money()&&upgraded.residents()==old.residents()&&upgraded.day()==old.day(),"CVT1-CVT6 preserve city");
            ok(upgraded.urbanFlags()==0&&upgraded.loanBalance()==old.loanBalance(),"legacy cities incur no new debt or sanitation requirement");
        }
        for(int x:new int[]{Integer.MIN_VALUE,-999,-1,0,1,999,Integer.MAX_VALUE})for(int y:new int[]{Integer.MIN_VALUE,-99,-1,1,3,99,Integer.MAX_VALUE})ok(CityMath.floorMod(x,y)==Math.floorMod(x,y),"API23 modulus matches JDK on boundary cases");
        City c=city(9);CityEconomy snapshot=c.economy();int score=snapshot.civics.percent(TileType.WASTE_CENTER,9,13);c.build(9,13,TileType.GRASS);ok(snapshot.civics.percent(TileType.WASTE_CENTER,9,13)==score,"municipal coverage is a stable daily snapshot");
        System.out.println("OK: "+checks+" mixed-system audit assertions across 300 days; local Java elapsed ms="+(System.nanoTime()-started)/1000000);
    }
}
