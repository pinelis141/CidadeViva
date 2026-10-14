package br.com.cidadeviva.sim;

import br.com.cidadeviva.input.BuildStroke;
import java.util.Arrays;
import java.io.*;

/** Player scenarios: remodel a serviced town, reclaim lots, and recover a zero-cash save. */
public final class CityRecoveryTest {
    private static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
    private static City reload(City c)throws Exception {
        ByteArrayOutputStream data=new ByteArrayOutputStream();CityPersistence.save(c,data);
        return CityPersistence.load(new ByteArrayInputStream(data.toByteArray()));
    }
    private static void add(City c,TileType type){
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)
            if(c.hasRoad(x,y)&&c.canBuild(x,y,type)){c.build(x,y,type);return;}
        throw new AssertionError("no service site");
    }
    public static void main(String[] args)throws Exception {
        City c=new City();
        for(TileType t:new TileType[]{TileType.PARK,TileType.SCHOOL,TileType.CLINIC,TileType.POWER_PLANT,TileType.WATER_PUMP})add(c,t);
        check(c.economy().net>=0,"starter town can afford one of each essential service at default tax");
        c.build(21,23,TileType.GRASS);c.build(22,23,TileType.GRASS);c.build(25,25,TileType.GRASS);
        int minimum=c.money();City twin=reload(c);
        for(int i=0;i<180;i++){
            c.nextDay();twin.nextDay();minimum=Math.min(minimum,c.money());
            check(c.money()==twin.money()&&c.economy().net==twin.economy().net,"recovery replay after CVT5 load");
        }
        check(minimum>5000,"three removed zones leave a viable recovery reserve");
        check(c.economy().net>0,"remaining districts grow back to a surplus");
        City single=new City(),area=new City();
        int before=single.money();
        single.build(21,23,TileType.GRASS);single.build(22,23,TileType.GRASS);
        java.util.List<BuildStroke.Cell> cells=Arrays.asList(new BuildStroke.Cell(21,23),new BuildStroke.Cell(22,23),new BuildStroke.Cell(21,23),new BuildStroke.Cell(-1,0));
        City.BuildResult preview=area.preview(cells,TileType.GRASS);
        check(preview.cost==-80&&preview.built==2&&preview.ignored==1,"refund preview deduplicates targets and excludes invalid cells");
        check(area.money()==before,"preview never credits refunds");
        City.BuildResult result=area.buildArea(cells,TileType.GRASS);
        check(area.money()==single.money()&&area.money()==before+80,"brush and individual removal refund exactly once");
        check(result.feedback().contains("DEVOLUCAO"),"refund described as income");
        area.buildArea(cells,TileType.GRASS);
        check(area.money()==single.money(),"empty ground cannot refund twice");
        int cash=single.money();single.build(21,23,TileType.RESIDENTIAL);single.build(21,23,TileType.GRASS);
        check(single.money()==cash-40,"building/demolition cannot create money");
        Tile[][] map=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++){Tile t=c.tile(x,y);map[x][y]=new Tile(t.type,t.level);}
        c.restore(map,0,c.day(),-100);
        // Demolishing a private developed lot can fund rebuilding in an already bankrupt save.
        c.build(23,23,TileType.GRASS);
        check(c.money()==40,"zero-cash save can reclaim original investment");
        City empty=new City();
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++){Tile t=empty.tile(x,y);map[x][y]=new Tile(t.type==TileType.RESIDENTIAL?TileType.GRASS:t.type,t.type==TileType.RESIDENTIAL?0:t.level);}
        empty.restore(map,100,1,0);
        check(empty.economy().net<0&&empty.needsBudgetPause(),"unfunded empty district triggers budget guard");
        check(!new City().needsBudgetPause(),"profitable city never pauses for low runway");
        System.out.println("OK: remodeling, refunds, bankruptcy recovery, CVT5 replay and budget guard; minimum reserve="+minimum);
    }
}
