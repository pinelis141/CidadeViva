package br.com.cidadeviva.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Deterministic, capacity-limited public services distributed through actual roads. */
public final class CityServices {
    public static final TileType[] BUILD_MENU={TileType.PARK,TileType.SCHOOL,TileType.CLINIC,TileType.POWER_PLANT,TileType.WATER_PUMP};
    public static final TileType[] TYPES={TileType.PARK,TileType.SCHOOL,TileType.CLINIC};
    private static final int H=City.HEIGHT,N=City.WIDTH*H;
    private final int[][] coverage=new int[3][N];
    private final int[] served=new int[3];
    public final int maintenance,buildings,activeBuildings;
    public static int index(TileType type) {
        for(int i=0;i<TYPES.length;i++)if(TYPES[i]==type)return i;
        throw new IllegalArgumentException("Servico invalido");
    }
    public static int capacity(TileType type) {return type.isUtility()?CityUtilities.capacity(type):type==TileType.PARK?96:type==TileType.SCHOOL?180:144;}
    public static int range(TileType type) {return type==TileType.PARK?6:12;}
    public static int upkeep(TileType type) {return type.isUtility()?CityUtilities.upkeep(type):type==TileType.PARK?8:type==TileType.SCHOOL?24:30;}
    public static String name(TileType type) {return type.isUtility()?CityUtilities.name(type):type==TileType.PARK?"PARQUE":type==TileType.SCHOOL?"ESCOLA":"POSTO DE SAUDE";}
    private CityServices(City city,CityUtilities utilities) {
        int cost=utilities.maintenance,total=0,active=0;
        int[] distance=new int[N],queue=new int[N];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<H;y++) {
            TileType type=city.tile(x,y).type;
            if(!type.isService())continue;
            total++;cost+=upkeep(type);
            if(city.money()==0 || !city.hasRoad(x,y) || !utilities.hasSupply(x,y))continue;
            active++;
            Arrays.fill(distance,-1);int head=0,tail=0;
            for(int d=0;d<4;d++) {
                int nx=x+dx(d),ny=y+dy(d);
                if(road(city,nx,ny)){int id=nx*H+ny;distance[id]=0;queue[tail++]=id;}
            }
            while(head<tail) {
                int id=queue[head++],rx=id/H,ry=id%H;
                if(distance[id]>=range(type))continue;
                for(int d=0;d<4;d++) {
                    int nx=rx+dx(d),ny=ry+dy(d);
                    if(!road(city,nx,ny))continue;
                    int next=nx*H+ny;
                    if(distance[next]<0){distance[next]=distance[id]+1;queue[tail++]=next;}
                }
            }
            final int[] distances=new int[N];Arrays.fill(distances,Integer.MAX_VALUE);
            List<Integer> homes=new ArrayList<>();
            for(int hx=0;hx<City.WIDTH;hx++)for(int hy=0;hy<H;hy++) {
                Tile home=city.tile(hx,hy);
                if(home.type!=TileType.RESIDENTIAL || home.level==0)continue;
                int id=hx*H+hy,best=Integer.MAX_VALUE;
                for(int d=0;d<4;d++) {
                    int nx=hx+dx(d),ny=hy+dy(d);
                    if(road(city,nx,ny) && distance[nx*H+ny]>=0)best=Math.min(best,distance[nx*H+ny]);
                }
                if(best!=Integer.MAX_VALUE){distances[id]=best;homes.add(id);}
            }
            Collections.sort(homes,new Comparator<Integer>() {
                public int compare(Integer a,Integer b) {int c=Integer.compare(distances[a],distances[b]);return c!=0?c:Integer.compare(a,b);}
            });
            int sector=index(type),remaining=capacity(type);
            for(int id:homes) {
                int need=city.tile(id/H,id%H).level*12-coverage[sector][id];
                int assigned=Math.min(remaining,need);
                coverage[sector][id]+=assigned;served[sector]+=assigned;remaining-=assigned;
                if(remaining==0)break;
            }
        }
        maintenance=cost;buildings=total;activeBuildings=active;
    }
    private static int dx(int d) {return d==0?1:d==1?-1:0;}
    private static int dy(int d) {return d==2?1:d==3?-1:0;}
    private static boolean road(City c,int x,int y) {Tile t=c.tile(x,y);return t!=null && t.type==TileType.ROAD;}
    public static CityServices analyze(City city) {return new CityServices(city,CityUtilities.analyze(city));}
    public static CityServices analyze(City city,CityUtilities utilities){return new CityServices(city,utilities);}
    public int coveredAt(TileType type,int x,int y) {return coverage[index(type)][x*H+y];}
    public int served(TileType type) {return served[index(type)];}
    public int bonusAt(City city,int x,int y) {
        int people=city.tile(x,y).level*12;
        if(people==0)return 0;
        return (coverage[0][x*H+y]*6+coverage[1][x*H+y]*8+coverage[2][x*H+y]*8)/people;
    }
    public boolean readyForDensity(City city,int x,int y) {
        int people=city.tile(x,y).level*12;
        return coveredAt(TileType.SCHOOL,x,y)>=people && coveredAt(TileType.CLINIC,x,y)>=people;
    }
}
