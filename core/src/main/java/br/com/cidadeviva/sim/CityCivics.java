package br.com.cidadeviva.sim;

import java.util.*;

/** Finite municipal coverage over roads. All fields are derived, never frame-dependent. */
public final class CityCivics {
    public static final TileType[] TYPES={TileType.FIRE_STATION,TileType.POLICE_STATION,TileType.WASTE_CENTER,TileType.SEWAGE_PLANT};
    private static final int H=City.HEIGHT,N=CityRoads.N;
    private final int[][] coverage=new int[4][N];
    private final int[] served=new int[4],capacity=new int[4];
    public final int maintenance,operatingBuildings;
    public final boolean wasteEnabled,sewageEnabled;
    private final int[] units=new int[N],levels=new int[N];
    private final boolean emptyTreasury;
    private CityCivics(City city,CityUtilities utilities,CityMobility traffic){
        emptyTreasury=city.money()==0;
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<H;y++){units[x*H+y]=CityRoads.units(city.tile(x,y));levels[x*H+y]=city.tile(x,y).level;}
        wasteEnabled=city.wasteEnabled();sewageEnabled=city.sewageEnabled();int cost=0,active=0;
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<H;y++){
            TileType t=city.tile(x,y).type;if(!t.isCivic())continue;cost+=upkeep(t);
            if(city.money()==0||!city.hasRoad(x,y)||!utilities.hasSupply(x,y))continue;
            if(t==TileType.BUS_DEPOT||t==TileType.BUS_STOP)continue;
            active++;int sector=index(t),remaining=capacity(t);capacity[sector]+=remaining;
            final int[] distance=CityRoads.travelDistances(city,x,y,range(t),t==TileType.SEWAGE_PLANT?null:traffic);
            final int[] best=new int[N];Arrays.fill(best,Integer.MAX_VALUE);List<Integer> lots=new ArrayList<>();
            for(int hx=0;hx<City.WIDTH;hx++)for(int hy=0;hy<H;hy++){
                if(CityRoads.units(city.tile(hx,hy))==0)continue;
                int id=hx*H+hy;best[id]=CityRoads.accessDistance(city,hx,hy,distance);if(best[id]!=Integer.MAX_VALUE)lots.add(id);
            }
            Collections.sort(lots,new Comparator<Integer>(){public int compare(Integer a,Integer b){int c=Integer.compare(best[a],best[b]);return c!=0?c:Integer.compare(a,b);}});
            for(int id:lots){int needed=CityRoads.units(city.tile(id/H,id%H))-coverage[sector][id];int take=Math.min(needed,remaining);
                coverage[sector][id]+=take;served[sector]+=take;remaining-=take;if(remaining==0)break;}
        }
        maintenance=cost;operatingBuildings=active;
    }
    public static CityCivics analyze(City c,CityUtilities u){return new CityCivics(c,u,CityMobility.analyze(c,u));}
    public static CityCivics analyze(City c,CityUtilities u,CityMobility m){return new CityCivics(c,u,m);}
    public static int index(TileType t){for(int i=0;i<TYPES.length;i++)if(TYPES[i]==t)return i;throw new IllegalArgumentException("Servico civico invalido");}
    public static int capacity(TileType t){switch(t){case FIRE_STATION:return 480;case POLICE_STATION:return 480;case WASTE_CENTER:return 600;case SEWAGE_PLANT:return 800;case BUS_DEPOT:return 240;default:return 0;}}
    public static int range(TileType t){return t==TileType.WASTE_CENTER||t==TileType.SEWAGE_PLANT?48:16;}
    public static int upkeep(TileType t){switch(t){case FIRE_STATION:return 18;case POLICE_STATION:return 16;case WASTE_CENTER:return 16;case SEWAGE_PLANT:return 20;case BUS_DEPOT:return 24;case BUS_STOP:return 1;default:return 0;}}
    public static String name(TileType t){switch(t){case FIRE_STATION:return "BOMBEIROS";case POLICE_STATION:return "POLICIA";case WASTE_CENTER:return "COLETA DE LIXO";case SEWAGE_PLANT:return "TRATAMENTO DE ESGOTO";case BUS_DEPOT:return "GARAGEM DE ONIBUS";case BUS_STOP:return "PONTO DE ONIBUS";default:return "";}}
    public int coveredAt(TileType t,int x,int y){return coverage[index(t)][x*H+y];}
    public int served(TileType t){return served[index(t)];}
    public int capacityFor(TileType t){return capacity[index(t)];}
    public int percent(TileType t,int x,int y){int need=units[x*H+y];return need==0?0:coveredAt(t,x,y)*100/need;}
    public int sanitationPenalty(int x,int y){return (wasteEnabled?(100-percent(TileType.WASTE_CENTER,x,y))*10/100:0)+(sewageEnabled?(100-percent(TileType.SEWAGE_PLANT,x,y))*14/100:0);}
    public int crimeAt(int x,int y){int id=x*H+y;if(units[id]==0)return 0;
        return Math.max(0,12+levels[id]*5+(emptyTreasury?12:0)-percent(TileType.POLICE_STATION,x,y)*35/100);}
    public int safetyBonus(int x,int y){return percent(TileType.POLICE_STATION,x,y)*5/100;}
    public boolean healthy(int x,int y){return (!wasteEnabled||percent(TileType.WASTE_CENTER,x,y)==100)&&(!sewageEnabled||percent(TileType.SEWAGE_PLANT,x,y)==100);}
}
