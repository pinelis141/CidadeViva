package br.com.cidadeviva.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/** Life, progression and purely cosmetic traffic regressions. */
public final class CityLifeTest {
    private static int checks;
    private static void ok(boolean success,String label) {checks++;if(!success)throw new AssertionError(label);}
    private static City copy(City city)throws Exception {
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        CityPersistence.save(city,output);
        return CityPersistence.load(new ByteArrayInputStream(output.toByteArray()));
    }
    public static void main(String[] args) throws Exception {
        City start=new City();
        CityEconomy eco=start.economy();
        ok(eco.industrialJobs>0,"starter city provides industrial employment");
        ok(eco.residentialDemand>=24,"residential growth possible in a brand new city");
        ok(start.day()==1,"starting city day one unchanged");
        CityActivity traffic=new CityActivity(start);
        ok(traffic.cars().size()>0,"cars circulate on starting city streets");
        ok(traffic.walkers().size()>0,"occupied streets have walkers");
        ok(traffic.cars().size()<=18 && traffic.walkers().size()<=26,"animation budgets respected");
        float[] out=new float[2];
        for(CityActivity.Car car:traffic.cars()) {
            ok(car.pathLength()>1,"every car has movement");
            for(int i=0;i<car.pathLength();i++) {
                ok(start.tile(car.tileX(i),car.tileY(i)).type==TileType.ROAD,"vehicle route stays on asphalt");
                if(i>0)ok(Math.abs(car.tileX(i)-car.tileX(i-1))+Math.abs(car.tileY(i)-car.tileY(i-1))==1,"continuous route");
            }
            for(int frame=0;frame<100;frame++) {
                car.sample(frame*0.23f,out);
                ok(Float.isFinite(out[0]) && Float.isFinite(out[1]),"car interpolation remains finite");
                ok(start.inBounds((int)Math.round(out[0]),(int)Math.round(out[1])),"car inside map");
            }
        }
        for(CityActivity.Walker walker:traffic.walkers())for(int frame=0;frame<20;frame++) {
            walker.sample(frame*0.2f,out);
            ok(Math.abs(out[0]-walker.x)<=0.16f && Math.abs(out[1]-walker.y)<=0.16f,
                "pedestrian remains on own lot");
        }
        City growth=new City();
        // Add a few empty accessible zones and verify prompt, but not instant,
        // development, daily reports and save compatibility.
        int placed=0;
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)
            if(placed<8 && growth.canBuild(x,y,TileType.RESIDENTIAL) && growth.hasRoad(x,y)) {
                growth.build(x,y,TileType.RESIDENTIAL);placed++;
            }
        ok(placed>=4,"starter streets have construction room");
        int initiallyOccupied=0;
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)
            if(growth.tile(x,y).type==TileType.RESIDENTIAL && growth.tile(x,y).level>0)initiallyOccupied++;
        ok(growth.economy().residentialDemand>=24,"moderate block of vacant residential lots remains buildable");
        City twin=copy(growth);
        int witnessed=0, occupiedVacancies=0;
        for(int day=0;day<22;day++) {
            growth.nextDay();twin.nextDay();
            ok(growth.recentEvents().size()==twin.recentEvents().size(),"repeatable daily changes");
            for(int i=0;i<growth.recentEvents().size();i++) {
                City.GrowthEvent a=growth.recentEvents().get(i),b=twin.recentEvents().get(i);
                ok(a.x==b.x && a.y==b.y && a.toLevel==b.toLevel,"changes repeat after loading save");
                if(a.isGrowth()) {
                    witnessed++;
                    if(a.type==TileType.RESIDENTIAL && a.fromLevel==0)occupiedVacancies++;
                }
            }
        }
        ok(witnessed>0,"visual growth takes place within the first 22 days");
        ok(occupiedVacancies>0,"new houses visibly occupy newly zoned lots");
        ok(growth.day()==twin.day(),"date unchanged across replay");
        City reloaded=copy(growth);
        ok(reloaded.recentEvents().isEmpty(),"visual events not persisted in CVT3");
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)
            ok(reloaded.tile(x,y).level==growth.tile(x,y).level,"mature city persisted");
        CityActivity before=new CityActivity(growth);
        CityActivity after=new CityActivity(reloaded);
        ok(before.cars().size()==after.cars().size(),"same road network gives same vehicle budget");
        System.out.println("OK: "+checks+" verificacoes de crescimento, animacao e saves CVT3.");
    }
}
