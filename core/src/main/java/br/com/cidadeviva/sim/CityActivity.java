package br.com.cidadeviva.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Road vehicles sample measured commuter traffic; walkers animate occupied neighborhoods.
 * Every car follows actual four-neighbour road tiles and reverses at dead ends;
 * there is no path across water, buildings or off-map tiles.
 */
public final class CityActivity {
    private static final int[] DX={1,-1,0,0};
    private static final int[] DY={0,0,1,-1};
    private final List<Car> cars;
    private final List<Walker> walkers;
    private final List<Bus> buses;
    public List<Bus> buses(){return buses;}

    public CityActivity(City city) {
        CityMobility mobility=city.economy().mobility;
        ArrayList<int[]> roads=new ArrayList<>();
        ArrayList<int[]> buildings=new ArrayList<>();
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
            Tile tile=city.tile(x,y);
            // No phantom rush-hour vehicles on unused or inaccessible roads.
            if(tile.type==TileType.ROAD && city.roadMask(x,y)!=0 &&
               mobility.trafficAt(x,y)>0)roads.add(new int[]{x,y});
            if(tile.type.isZone() && tile.level>0 && city.hasRoad(x,y)) buildings.add(new int[]{x,y});
        }
        ArrayList<Car> moving=new ArrayList<>();
        // A sparse starting town gets movement immediately; extensive cities have
        // a hard cap to protect frame time and avoid unrealistic traffic clutter.
        int carCount=Math.min(18,Math.min(roads.size(),mobility.employed/7));
        for(int i=0;i<carCount;i++) {
            int[] position=roads.get(CityMath.floorMod(i*17+i*i*11+3,roads.size()));
            ArrayList<int[]> route=new ArrayList<>();
            route.add(position);
            int px=Integer.MIN_VALUE,py=Integer.MIN_VALUE;
            for(int step=0;step<7;step++) {
                int[][] options=new int[4][2];
                int size=0;
                for(int direction=0;direction<4;direction++) {
                    int nx=position[0]+DX[direction],ny=position[1]+DY[direction];
                    Tile candidate=city.tile(nx,ny);
                    if(candidate!=null && candidate.type==TileType.ROAD &&
                       mobility.trafficAt(nx,ny)>0 && !(nx==px && ny==py)) {
                        options[size][0]=nx;options[size][1]=ny;size++;
                    }
                }
                if(size==0) { // dead end: turn around on the existing road
                    if(route.size()<2)break;
                    int[] previous=route.get(route.size()-2);
                    px=position[0];py=position[1];position=previous;
                } else {
                    int pick=CityMath.floorMod(i*31+step*13+position[0]*3+position[1]*7,size);
                    px=position[0];py=position[1];
                    position=new int[]{options[pick][0],options[pick][1]};
                }
                route.add(position);
            }
            if(route.size()>1){int peak=0;for(int[] cell:route)peak=Math.max(peak,mobility.trafficAt(cell[0],cell[1]));moving.add(new Car(route,i,1f+peak/80f));}
        }
        cars=Collections.unmodifiableList(moving);
        ArrayList<Bus> transit=new ArrayList<>();int routeIndex=0;
        for(int[] route:mobility.transit.routes()){
            if(mobility.ridersInNetwork(mobility.transit.routeNetwork(routeIndex++))==0)continue;
            if(transit.size()<8)transit.add(new Bus(route,transit.size()));
        }
        buses=Collections.unmodifiableList(transit);
        ArrayList<Walker> people=new ArrayList<>();
        int peopleCount=Math.min(26,buildings.size());
        for(int i=0;i<peopleCount;i++) {
            int[] tile=buildings.get(CityMath.floorMod(i*11+i*i*3+1,buildings.size()));
            people.add(new Walker(tile[0],tile[1],i));
        }
        walkers=Collections.unmodifiableList(people);
    }
    public List<Car> cars(){return cars;}
    public List<Walker> walkers(){return walkers;}

    public static final class Car {
        private final int[][] cells;
        private final float offset,speed;
        public final int colorVariant;
        private Car(List<int[]> route,int variant,float delay) {
            cells=route.toArray(new int[0][]);
            offset=(variant*7.31f)%16f;
            speed=(0.35f+(variant%4)*0.08f)/delay;
            colorVariant=variant%5;
        }
        public int pathLength(){return cells.length;}
        public int tileX(int i){return cells[i][0];}
        public int tileY(int i){return cells[i][1];}
        /** Returns a continuously interpolated position expressed in tile coordinates. */
        public void sample(float seconds,float[] out) {
            float travel=Math.max(0f,seconds)*speed+offset;
            int segments=(cells.length-1)*2;
            float segment=travel%segments;
            int index=(int)Math.floor(segment);
            float progress=segment-index;
            int a=index<cells.length-1?index:segments-index;
            int b=index<cells.length-1?index+1:segments-index-1;
            out[0]=cells[a][0]+(cells[b][0]-cells[a][0])*progress;
            out[1]=cells[a][1]+(cells[b][1]-cells[a][1])*progress;
        }
    }
    public static final class Bus {
        private final int[] route;private final float offset;
        private Bus(int[] route,int index){this.route=route.clone();offset=index*2.7f;}
        public int pathLength(){return route.length;}
        public int roadId(int i){return route[i];}
        public void sample(float seconds,float[] out){
            float segment=(Math.max(0,seconds)*.45f+offset)%((route.length-1)*2);int i=(int)segment;float part=segment-i;
            int a=i<route.length-1?i:(route.length-1)*2-i,b=i<route.length-1?i+1:(route.length-1)*2-i-1;
            out[0]=route[a]/City.HEIGHT+(route[b]/City.HEIGHT-route[a]/City.HEIGHT)*part;
            out[1]=route[a]%City.HEIGHT+(route[b]%City.HEIGHT-route[a]%City.HEIGHT)*part;
        }
    }
    public static final class Walker {
        public final int x,y,colorVariant;
        private final float phase;
        private Walker(int x,int y,int variant) {
            this.x=x;this.y=y;this.colorVariant=variant%5;
            phase=variant*2.731f;
        }
        public void sample(float seconds,float[] out) {
            float t=seconds*0.72f+phase;
            // Remain inside their home lot rather than wandering across water.
            out[0]=x+0.15f*(float)Math.sin(t);
            out[1]=y+0.15f*(float)Math.cos(t*0.77f);
        }
    }
}
