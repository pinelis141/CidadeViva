package br.com.cidadeviva.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Road-network commuting simulation. No random agents or hidden saved state:
 * residents fill real reachable vacancies by shortest road path, with a maximum
 * commuter distance. This is a deterministic aggregate simulation, not a
 * citizen-by-citizen traffic simulator.
 */
public final class CityMobility {
    public static final int MAX_COMMUTE_STEPS = 24;
    private static final int[] DX = {1, -1, 0, 0};
    private static final int[] DY = {0, 0, 1, -1};
    private static final int W = City.WIDTH, H = City.HEIGHT, N = W*H;

    public final int employed, commercialWorkers, industrialWorkers;
    public final int accessibleResidents, reachableResidents, disconnectedResidents;
    public final int availableJobs, reachableJobCapacity;
    public final int averageCommuteSteps;
    /** Road tiles with sustained equivalent daily traffic above capacity. */
    public final int congestedRoads;
    public final int transitRiders;
    public final CityTransit transit;
    private final int[] networkRiders;
    public int ridersInNetwork(int network){return network<networkRiders.length?networkRiders[network]:0;}
    private final int[] roadTraffic;
    private final int[] homeEmployment, workplaceWorkers;

    private static final class Building {
        final int x,y,id,capacity;
        final TileType type;
        int remaining;
        Building(int x,int y,TileType type,int capacity) {
            this.x=x; this.y=y;this.id=x*H+y;
            this.type=type;this.capacity=capacity;this.remaining=capacity;
        }
    }
    private static final class Offer {
        final int job,distance,road;
        Offer(int job,int distance,int road) {this.job=job;this.distance=distance;this.road=road;}
    }
    private CityMobility(int employed,int commerce,int industry,int accessible,int reachable,
                         int jobCapacity,int reachableCapacity,long weightedDistance,int[] roads,int[] homeEmployment,int[] workplaceWorkers,CityTransit transit,int transitRiders,int[] networkRiders) {
        this.transit=transit;this.transitRiders=transitRiders;this.networkRiders=networkRiders;
        this.employed=employed;
        this.commercialWorkers=commerce;
        this.industrialWorkers=industry;
        this.accessibleResidents=accessible;
        this.reachableResidents=reachable;
        this.disconnectedResidents=accessible-reachable;
        this.availableJobs=jobCapacity;
        this.reachableJobCapacity=reachableCapacity;
        this.averageCommuteSteps=employed==0?0:(int)Math.round((double)weightedDistance/employed);
        this.roadTraffic=roads;
        this.homeEmployment=homeEmployment;this.workplaceWorkers=workplaceWorkers;
        int jammed=0;
        for(int flow:roads)if(flow>=80)jammed++;
        this.congestedRoads=jammed;
    }
    public int trafficAt(int x,int y) { return x<0||y<0||x>=W||y>=H?0:roadTraffic[x*H+y]; }
    public int employedAt(int x,int y) {return homeEmployment[x*H+y];}
    public int workersAt(int x,int y) {return workplaceWorkers[x*H+y];}
    private static int nearRoads(City city,int x,int y,int[] out) {
        int[] entries=CityRoads.entries(city,x,y);System.arraycopy(entries,0,out,0,entries.length);return entries.length;
    }
    private static final class Step implements Comparable<Step>{
        final int id,cost;Step(int id,int cost){this.id=id;this.cost=cost;}
        public int compareTo(Step o){int c=Integer.compare(cost,o.cost);return c!=0?c:Integer.compare(id,o.id);}
    }
    public static CityMobility analyze(City city) {return analyze(city,CityUtilities.analyze(city));}
    public static CityMobility analyze(City city,CityUtilities utilities) {
        CityTransit transit=CityTransit.analyze(city,utilities);
        CityMobility free=allocate(city,utilities,transit,null);
        return free.congestedRoads==0?free:allocate(city,utilities,transit,free.roadTraffic);
    }
    private static CityMobility allocate(City city,CityUtilities utilities,CityTransit transit,int[] previousTraffic) {
        List<Building> homes=new ArrayList<>(),jobs=new ArrayList<>();
        int accessible=0,jobCapacity=0;
        for(int x=0;x<W;x++)for(int y=0;y<H;y++) {
            Tile tile=city.tile(x,y);
            if(tile.fireDays>0||tile.level<1||!tile.type.isZone()||!city.hasRoad(x,y))continue;
            if(tile.type==TileType.RESIDENTIAL) {
                int people=tile.level*12;
                accessible+=people;
                homes.add(new Building(x,y,tile.type,people));
            } else {
                if(!utilities.hasSupply(x,y))continue;
                int positions=tile.level*(tile.type==TileType.COMMERCIAL?10:18);
                jobCapacity+=positions;
                jobs.add(new Building(x,y,tile.type,positions));
            }
        }
        int employed=0,commerce=0,industry=0,reachable=0;
        int[] flows=new int[N],homeEmployment=new int[N],workplaceWorkers=new int[N];
        long commuteSum=0;
        boolean[] reachableJobs=new boolean[jobs.size()];
        // Reuse bounded arrays, avoiding per-home allocations of 2304 nodes.
        int[] distance=new int[N],parent=new int[N],queue=new int[N];
        int[] entryRoads=new int[8],jobRoads=new int[8];
        int[] seats=transit.availableSeats(),networkRiders=new int[seats.length];int transitRiders=0;
        List<Offer> offers=new ArrayList<>();
        for(Building home:homes) {
            Arrays.fill(distance,-1);
            Arrays.fill(parent,-1);
            PriorityQueue<Step> queueCost=new PriorityQueue<>();
            int startCount=nearRoads(city,home.x,home.y,entryRoads);
            int homeNetwork=transit.networkAt(home.x,home.y);
            for(int i=0;i<startCount;i++){int start=entryRoads[i];if(distance[start]>=0)continue;distance[start]=0;queueCost.add(new Step(start,0));}
            while(!queueCost.isEmpty()) {
                Step step=queueCost.remove();int p=step.id,steps=step.cost;if(distance[p]!=steps)continue;
                if(steps>=(homeNetwork>0?48:MAX_COMMUTE_STEPS))continue;
                int x=p/H,y=p%H;
                for(int d=0;d<4;d++) {
                    int nx=x+DX[d],ny=y+DY[d];
                    if(!CityRoads.road(city,nx,ny))continue;
                    int next=nx*H+ny;
                    int cost=steps+1+(previousTraffic==null?0:Math.min(4,previousTraffic[next]/80));
                    if(distance[next]<0||cost<distance[next]){distance[next]=cost;parent[next]=p;queueCost.add(new Step(next,cost));}
                }
            }
            offers.clear();
            for(int i=0;i<jobs.size();i++) {
                Building job=jobs.get(i);
                int count=nearRoads(city,job.x,job.y,jobRoads);
                int bestDist=Integer.MAX_VALUE,bestRoad=-1;
                for(int k=0;k<count;k++) {
                    int road=jobRoads[k],d=distance[road];
                    if(d>=0&&(d<bestDist||(d==bestDist&&road<bestRoad))) {
                        bestDist=d;bestRoad=road;
                    }
                }
                if(bestRoad>=0&&bestDist<=((homeNetwork>0 && transit.networkAt(job.x,job.y)==homeNetwork && seats[homeNetwork]>0)?48:MAX_COMMUTE_STEPS)) {
                    reachableJobs[i]=true;
                    offers.add(new Offer(i,bestDist,bestRoad));
                }
            }
            if(!offers.isEmpty())reachable+=home.capacity;
            Collections.sort(offers,new Comparator<Offer>() {
                @Override public int compare(Offer a,Offer b) {
                    int distanceOrder=Integer.compare(a.distance,b.distance);
                    return distanceOrder!=0?distanceOrder:Integer.compare(jobs.get(a.job).id,jobs.get(b.job).id);
                }
            });
            int available=home.capacity;
            for(Offer offer:offers) {
                Building job=jobs.get(offer.job);
                if(available==0)break;
                if(job.remaining==0)continue;
                int workers=Math.min(available,job.remaining);
                boolean bus=homeNetwork>0&&transit.networkAt(job.x,job.y)==homeNetwork;
                if(offer.distance>MAX_COMMUTE_STEPS)workers=bus?Math.min(workers,seats[homeNetwork]):0;
                int riders=bus?Math.min(workers,seats[homeNetwork]):0;
                if(bus){seats[homeNetwork]-=riders;networkRiders[homeNetwork]+=riders;}
                transitRiders+=riders;
                available-=workers;job.remaining-=workers;employed+=workers;
                homeEmployment[home.id]+=workers;workplaceWorkers[job.id]+=workers;
                if(job.type==TileType.COMMERCIAL)commerce+=workers;else industry+=workers;
                commuteSum+=(long)workers*(offer.distance+2);
                // Count actual commute paths for traffic visualization and metrics.
                int road=offer.road;
                while(road>=0) {flows[road]+=workers-riders+(riders+11)/12;road=parent[road];}
            }
        }
        int reachableCapacity=0;
        for(int j=0;j<jobs.size();j++)if(reachableJobs[j])reachableCapacity+=jobs.get(j).capacity;
        return new CityMobility(employed,commerce,industry,accessible,reachable,
            jobCapacity,reachableCapacity,commuteSum,flows,homeEmployment,workplaceWorkers,transit,transitRiders,networkRiders);
    }
}
