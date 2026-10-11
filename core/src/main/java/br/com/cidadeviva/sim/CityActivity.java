package br.com.cidadeviva.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Cosmetic road vehicles and neighborhood walkers, independent of saved economy.
 * Every car follows actual four-neighbour road tiles and reverses at dead ends;
 * there is no path across water, buildings or off-map tiles.
 */
public final class CityActivity {
    private static final int[] DX={1,-1,0,0};
    private static final int[] DY={0,0,1,-1};
    private final List<Car> cars;
    private final List<Walker> walkers;

    public CityActivity(City city) {
        ArrayList<int[]> roads=new ArrayList<>();
        ArrayList<int[]> buildings=new ArrayList<>();
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
            Tile tile=city.tile(x,y);
            if(tile.type==TileType.ROAD && city.roadMask(x,y)!=0) roads.add(new int[]{x,y});
            if(tile.type.isZone() && tile.level>0 && city.hasRoad(x,y)) buildings.add(new int[]{x,y});
        }
        ArrayList<Car> moving=new ArrayList<>();
        // A sparse starting town gets movement immediately; extensive cities have
        // a hard cap to protect frame time and avoid unrealistic traffic clutter.
        int carCount=Math.min(18,roads.size()/3);
        for(int i=0;i<carCount;i++) {
            int[] position=roads.get(Math.floorMod(i*17+i*i*11+3,roads.size()));
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
                       !(nx==px && ny==py)) {
                        options[size][0]=nx;options[size][1]=ny;size++;
                    }
                }
                if(size==0) { // dead end: turn around on the existing road
                    if(route.size()<2)break;
                    int[] previous=route.get(route.size()-2);
                    px=position[0];py=position[1];position=previous;
                } else {
                    int pick=Math.floorMod(i*31+step*13+position[0]*3+position[1]*7,size);
                    px=position[0];py=position[1];
                    position=new int[]{options[pick][0],options[pick][1]};
                }
                route.add(position);
            }
            if(route.size()>1)moving.add(new Car(route,i));
        }
        cars=Collections.unmodifiableList(moving);
        ArrayList<Walker> people=new ArrayList<>();
        int peopleCount=Math.min(26,buildings.size());
        for(int i=0;i<peopleCount;i++) {
            int[] tile=buildings.get(Math.floorMod(i*11+i*i*3+1,buildings.size()));
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
        private Car(List<int[]> route,int variant) {
            cells=route.toArray(new int[0][]);
            offset=(variant*7.31f)%16f;
            speed=0.35f+(variant%4)*0.08f;
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
