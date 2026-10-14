package br.com.cidadeviva.sim;

import java.util.*;

/** Auto-routed buses pool garage capacity per connected road component. */
public final class CityTransit {
    private static final int H=City.HEIGHT,N=CityRoads.N;
    private final int[] network=new int[N];
    private final boolean[] active=new boolean[N];
    public boolean operatesAt(int x,int y){return active[x*H+y];}
    private final List<int[]> routes=new ArrayList<>();
    private final List<Integer> routeNetworks=new ArrayList<>();
    private final List<Integer> capacities=new ArrayList<>();
    public final int activeStops,totalCapacity;
    private CityTransit(City city,CityUtilities utilities){
        int[] component=new int[N],queue=new int[N];int components=0;
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<H;y++){
            int start=x*H+y;if(!CityRoads.road(city,x,y)||component[start]>0)continue;
            int head=0,tail=0;queue[tail++]=start;component[start]=++components;
            while(head<tail){int id=queue[head++];for(int d=0;d<4;d++){
                int nx=id/H+CityRoads.DX[d],ny=id%H+CityRoads.DY[d];if(!CityRoads.road(city,nx,ny))continue;
                int next=nx*H+ny;if(component[next]==0){component[next]=components;queue[tail++]=next;}
            }}
        }
        int[] seats=new int[components+1],garageGroup=new int[N];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<H;y++)if(city.tile(x,y).type==TileType.BUS_DEPOT&&operates(city,utilities,x,y)){
            int[] entries=CityRoads.entries(city,x,y);
            // A garage between disconnected roads must not teleport vehicles or duplicate its fleet.
            garageGroup[x*H+y]=component[entries[0]];seats[component[entries[0]]]+=CityCivics.capacity(TileType.BUS_DEPOT);
        }
        List<List<Integer>> stops=new ArrayList<>();for(int k=0;k<=components;k++)stops.add(new ArrayList<Integer>());
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<H;y++)if(city.tile(x,y).type==TileType.BUS_STOP&&operates(city,utilities,x,y)){
            for(int entry:CityRoads.entries(city,x,y))if(seats[component[entry]]>0){stops.get(component[entry]).add(x*H+y);break;}
        }
        int count=0,total=0;
        for(int group=1;group<=components;group++){
            List<Integer> connected=stops.get(group);if(seats[group]==0||connected.size()<2)continue;
            int first=connected.get(0),last=connected.get(connected.size()-1);
            int[] route=path(city,componentEntries(city,first/H,first%H,component,group),componentEntries(city,last/H,last%H,component,group));
            if(route.length<2)continue;
            int net=capacities.size()+1;capacities.add(seats[group]);total+=seats[group];count+=connected.size();
            routes.add(route);routeNetworks.add(net);
            for(int id=0;id<N;id++)if(garageGroup[id]==group)active[id]=true;
            for(int stop:connected){
                active[stop]=true;
                int[] walking=CityRoads.distances(city,componentEntries(city,stop/H,stop%H,component,group),3);
                for(int bx=0;bx<City.WIDTH;bx++)for(int by=0;by<H;by++)if(city.tile(bx,by).type.isZone()&&network[bx*H+by]==0&&CityRoads.accessDistance(city,bx,by,walking)!=Integer.MAX_VALUE)network[bx*H+by]=net;
            }
        }
        activeStops=count;totalCapacity=total;
    }
    private static int[] componentEntries(City c,int x,int y,int[] components,int group){int[] all=CityRoads.entries(c,x,y),out=new int[all.length];int n=0;for(int id:all)if(components[id]==group)out[n++]=id;return Arrays.copyOf(out,n);}
    private static boolean operates(City c,CityUtilities u,int x,int y){return c.money()>0&&c.hasRoad(x,y)&&u.hasSupply(x,y);}
    public static CityTransit analyze(City c,CityUtilities u){return new CityTransit(c,u);}
    public int networkAt(int x,int y){return network[x*H+y];}
    public int[] availableSeats(){int[] seats=new int[capacities.size()+1];for(int i=0;i<capacities.size();i++)seats[i+1]=capacities.get(i);return seats;}
    public List<int[]> routes(){List<int[]> out=new ArrayList<>();for(int[] r:routes)out.add(r.clone());return Collections.unmodifiableList(out);}
    public int routeNetwork(int i){return routeNetworks.get(i);}
    private static int[] path(City c,int[] starts,int[] goals){
        int[] parent=new int[N],queue=new int[N];Arrays.fill(parent,-2);int head=0,tail=0,end=-1;
        for(int id:starts)if(parent[id]==-2){parent[id]=-1;queue[tail++]=id;}
        search:while(head<tail){int id=queue[head++];for(int g:goals)if(g==id){end=id;break search;}
            for(int d=0;d<4;d++){int nx=id/H+CityRoads.DX[d],ny=id%H+CityRoads.DY[d];if(!CityRoads.road(c,nx,ny))continue;int next=nx*H+ny;if(parent[next]==-2){parent[next]=id;queue[tail++]=next;}}}
        if(end<0)return new int[0];int size=0;for(int p=end;p>=0;p=parent[p])size++;int[] out=new int[size];for(int p=end;p>=0;p=parent[p])out[--size]=p;return out;
    }
}
