package br.com.cidadeviva.sim;

import java.util.Arrays;
import java.util.PriorityQueue;

/** Shared four-neighbour network traversal, including the perimeter of multi-tile buildings. */
public final class CityRoads {
    public static final int H=City.HEIGHT,N=City.WIDTH*H;
    public static final int[] DX={1,-1,0,0},DY={0,0,1,-1};
    private CityRoads(){}
    public static boolean road(City c,int x,int y){Tile t=c.tile(x,y);return t!=null&&t.type==TileType.ROAD;}
    public static int[] entries(City c,int x,int y){
        Tile t=c.tile(x,y);if(t==null)return new int[0];
        int ax=t.block==0?x:(t.block-1)/H,ay=t.block==0?y:(t.block-1)%H,size=t.block==0?1:2;
        int[] out=new int[8];int n=0;
        for(int bx=ax;bx<ax+size;bx++)for(int by=ay;by<ay+size;by++)for(int d=0;d<4;d++){
            int nx=bx+DX[d],ny=by+DY[d];if(!road(c,nx,ny))continue;
            int id=nx*H+ny;boolean duplicate=false;for(int k=0;k<n;k++)if(out[k]==id)duplicate=true;
            if(!duplicate)out[n++]=id;
        }
        return Arrays.copyOf(out,n);
    }
    public static int[] distances(City c,int x,int y,int limit){return distances(c,entries(c,x,y),limit);}
    public static int[] distances(City c,int[] starts,int limit){
        int[] distance=new int[N],queue=new int[N];Arrays.fill(distance,-1);int head=0,tail=0;
        for(int id:starts)if(distance[id]<0){distance[id]=0;queue[tail++]=id;}
        while(head<tail){int id=queue[head++];if(distance[id]>=limit)continue;
            for(int d=0;d<4;d++){int nx=id/H+DX[d],ny=id%H+DY[d];if(!road(c,nx,ny))continue;
                int next=nx*H+ny;if(distance[next]<0){distance[next]=distance[id]+1;queue[tail++]=next;}}
        }
        return distance;
    }
    private static final class Visit implements Comparable<Visit>{
        final int road,cost;Visit(int road,int cost){this.road=road;this.cost=cost;}
        public int compareTo(Visit v){int c=Integer.compare(cost,v.cost);return c!=0?c:Integer.compare(road,v.road);}
    }
    /** Response/collection travel budget, including congestion; sewers use ordinary distance. */
    public static int[] travelDistances(City c,int x,int y,int limit,CityMobility traffic){
        if(traffic==null)return distances(c,x,y,limit);
        int[] distance=new int[N];Arrays.fill(distance,-1);PriorityQueue<Visit> queue=new PriorityQueue<>();
        for(int id:entries(c,x,y)){distance[id]=0;queue.add(new Visit(id,0));}
        while(!queue.isEmpty()){
            Visit visit=queue.remove();if(distance[visit.road]!=visit.cost)continue;
            for(int d=0;d<4;d++){
                int nx=visit.road/H+DX[d],ny=visit.road%H+DY[d];if(!road(c,nx,ny))continue;
                int next=nx*H+ny,cost=visit.cost+1+Math.min(4,traffic.trafficAt(nx,ny)/80);
                if(cost<=limit&&(distance[next]<0||cost<distance[next])){distance[next]=cost;queue.add(new Visit(next,cost));}
            }
        }
        return distance;
    }
    public static int accessDistance(City c,int x,int y,int[] distances){
        int best=Integer.MAX_VALUE;for(int road:entries(c,x,y))if(distances[road]>=0)best=Math.min(best,distances[road]);return best;
    }
    public static int units(Tile t){return t.level*(t.type==TileType.RESIDENTIAL?12:t.type==TileType.COMMERCIAL?10:t.type==TileType.INDUSTRIAL?18:0);}
}
