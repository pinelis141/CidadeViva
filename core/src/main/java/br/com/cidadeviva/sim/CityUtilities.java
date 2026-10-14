package br.com.cidadeviva.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Capacity-limited electricity and water carried by connected road networks. */
public final class CityUtilities {
    private static final int H=City.HEIGHT,N=City.WIDTH*H;
    private final int[][] supplied=new int[2][N];
    private final int[] capacities=new int[2],totals=new int[2],demands=new int[2];
    public final int maintenance;
    public final boolean powerEnabled,waterEnabled;
    private final City city;
    private CityUtilities(City city) {
        this.city=city;powerEnabled=city.powerEnabled();waterEnabled=city.waterEnabled();
        int cost=0;
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<H;y++) {
            TileType type=city.tile(x,y).type;
            if(type.isUtility())cost+=upkeep(type);
            for(int k=0;k<2;k++)demands[k]+=need(city.tile(x,y),k);
        }
        maintenance=cost;
        distribute(0);distribute(1);
    }
    public static int capacity(TileType type) {return 600;}
    public static int upkeep(TileType type) {return type==TileType.POWER_PLANT?36:24;}
    public static String name(TileType type) {return type==TileType.POWER_PLANT?"GERADOR ELETRICO":"POCO / RESERVATORIO";}
    public static int need(Tile tile,int kind) {
        if(tile.type==TileType.RESIDENTIAL)return tile.level*12;
        if(tile.type==TileType.COMMERCIAL)return tile.level*10;
        if(tile.type==TileType.INDUSTRIAL)return tile.level*(kind==0?30:18);
        if(tile.type.isService())return kind==0?8:4;
        if(tile.type==TileType.WATER_PUMP && kind==0)return 12;
        return 0;
    }
    private void distribute(final int kind) {
        if(!(kind==0?powerEnabled:waterEnabled))return;
        int[] distance=new int[N],queue=new int[N];
        TileType source=kind==0?TileType.POWER_PLANT:TileType.WATER_PUMP;
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<H;y++) {
            if(city.tile(x,y).type!=source || !operates(source,x,y))continue;
            int remaining=capacity(source);capacities[kind]+=remaining;
            Arrays.fill(distance,-1);int head=0,tail=0;
            for(int d=0;d<4;d++) {int nx=x+dx(d),ny=y+dy(d);if(road(nx,ny)){int id=nx*H+ny;distance[id]=0;queue[tail++]=id;}}
            while(head<tail) {
                int id=queue[head++],rx=id/H,ry=id%H;
                for(int d=0;d<4;d++) {int nx=rx+dx(d),ny=ry+dy(d);if(!road(nx,ny))continue;int next=nx*H+ny;if(distance[next]<0){distance[next]=distance[id]+1;queue[tail++]=next;}}
            }
            final int[] byDistance=new int[N];Arrays.fill(byDistance,Integer.MAX_VALUE);
            List<Integer> consumers=new ArrayList<>();
            for(int cx=0;cx<City.WIDTH;cx++)for(int cy=0;cy<H;cy++) {
                int id=cx*H+cy;
                if(need(city.tile(cx,cy),kind)==0)continue;
                for(int d=0;d<4;d++){int nx=cx+dx(d),ny=cy+dy(d);if(road(nx,ny) && distance[nx*H+ny]>=0)byDistance[id]=Math.min(byDistance[id],distance[nx*H+ny]);}
                if(byDistance[id]!=Integer.MAX_VALUE)consumers.add(id);
            }
            Collections.sort(consumers,new Comparator<Integer>() {
                public int compare(Integer a,Integer b){int order=Integer.compare(byDistance[a],byDistance[b]);return order!=0?order:Integer.compare(a,b);}
            });
            for(int id:consumers) {
                int demand=need(city.tile(id/H,id%H),kind)-supplied[kind][id];
                int assigned=Math.min(remaining,demand);
                supplied[kind][id]+=assigned;totals[kind]+=assigned;remaining-=assigned;
                if(remaining==0)break;
            }
        }
    }
    public boolean operates(TileType source,int x,int y) {
        return city.hasRoad(x,y) &&
            (source!=TileType.WATER_PUMP || !powerEnabled || supplied[0][x*H+y]>=12);
    }
    private static int dx(int d){return d==0?1:d==1?-1:0;}
    private static int dy(int d){return d==2?1:d==3?-1:0;}
    private boolean road(int x,int y){Tile t=city.tile(x,y);return t!=null && t.type==TileType.ROAD;}
    public static CityUtilities analyze(City city){return new CityUtilities(city);}
    public int suppliedAt(int kind,int x,int y){return supplied[kind][x*H+y];}
    public int capacity(int kind){return capacities[kind];}
    public int demand(int kind){return demands[kind];}
    public int served(int kind){return totals[kind];}
    public boolean hasSupply(int x,int y) {
        Tile tile=city.tile(x,y);
        return (!powerEnabled || supplied[0][x*H+y]>=need(tile,0)) &&
            (!waterEnabled || supplied[1][x*H+y]>=need(tile,1));
    }
    public int homePenalty(int x,int y) {
        int people=city.tile(x,y).level*12;
        if(people==0)return 0;
        return (powerEnabled?(people-supplied[0][x*H+y])*12/people:0)+
            (waterEnabled?(people-supplied[1][x*H+y])*18/people:0);
    }
}
