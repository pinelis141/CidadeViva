package br.com.cidadeviva.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayDeque;

/** Deterministic terrain and shoreline masks without Android/libGDX. */
public final class CityTerrainTest {
    private static int checks;
    private static void check(boolean condition,String message) {
        checks++;if(!condition)throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        City city=new City(), again=new City();
        int count=0, largest=0;
        boolean[][] seen=new boolean[City.WIDTH][City.HEIGHT];
        final int[] dx={1,-1,0,0},dy={0,0,1,-1};
        for(int x=0;x<City.WIDTH;x++) for(int y=0;y<City.HEIGHT;y++) {
            check(city.tile(x,y).type==again.tile(x,y).type,"new cities must be reproducible");
            if(city.tile(x,y).type!=TileType.WATER) {
                check(city.waterMask(x,y)==0,"non-water cannot have water mask");
                continue;
            }
            count++;
            int expected=0;
            for(int b=0;b<4;b++) {
                int xx=x+dx[b], yy=y+dy[b];
                if(city.inBounds(xx,yy) && city.tile(xx,yy).type==TileType.WATER)
                    expected|=(1<<b);
            }
            check(city.waterMask(x,y)==expected,"shoreline mask must match neighbours");
            if(seen[x][y])continue;
            int area=0;
            ArrayDeque<int[]> queue=new ArrayDeque<>();
            queue.add(new int[]{x,y});seen[x][y]=true;
            while(!queue.isEmpty()) {
                int[] point=queue.removeFirst();area++;
                for(int b=0;b<4;b++) {
                    int xx=point[0]+dx[b], yy=point[1]+dy[b];
                    if(!city.inBounds(xx,yy) || seen[xx][yy] || city.tile(xx,yy).type!=TileType.WATER)continue;
                    seen[xx][yy]=true;queue.add(new int[]{xx,yy});
                }
            }
            largest=Math.max(largest,area);
        }
        check(count>=200,"terrain should contain coherent lakes");
        check(largest>count*0.6,"water should not be checkerboard singletons");
        check(city.tile(24,24).type==TileType.ROAD,"existing starter city remains accessible");
        check(city.tile(20,20).type==TileType.GRASS,"center remains open for construction");
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        CityPersistence.save(city,output);
        City restored=CityPersistence.load(new ByteArrayInputStream(output.toByteArray()));
        for(int x=0;x<City.WIDTH;x++) for(int y=0;y<City.HEIGHT;y++) {
            check(restored.tile(x,y).type==city.tile(x,y).type,"save retains terrain, including lakes");
            check(restored.waterMask(x,y)==city.waterMask(x,y),"save retains shore masks");
        }
        System.out.println("OK: "+checks+" terrain/lake/water mask/save assertions.");
    }
}
