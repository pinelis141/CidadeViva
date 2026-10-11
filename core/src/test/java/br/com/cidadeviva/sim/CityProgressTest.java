package br.com.cidadeviva.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.HashSet;

/** Sprite indexes, camera targeting, real population milestones and CVT3 regressions. */
public final class CityProgressTest {
    private static int checks;
    private static void ok(boolean value,String why) {checks++;if(!value)throw new AssertionError(why);}
    public static void main(String[] args) throws Exception {
        TileType[] types={TileType.RESIDENTIAL,TileType.COMMERCIAL,TileType.INDUSTRIAL};
        for(TileType type:types) {
            HashSet<Integer> variants=new HashSet<>();
            for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
                int family=BuildingAppearance.variant(x,y);
                ok(family>=0 && family<7,"seven architectural families only");
                variants.add(family);
                int first=BuildingAppearance.sprite(type,1,x,y);
                for(int lv=1;lv<=3;lv++) {
                    int id=BuildingAppearance.sprite(type,lv,x,y);
                    ok(id>=27 && id<125,"sprite in 125 item atlas");
                    ok(BuildingAppearance.sprite(type,lv,x,y)==id,"deterministic sprite");
                    if(family<6)ok(id==116+(type==TileType.RESIDENTIAL?0:type==TileType.COMMERCIAL?3:6)+lv-1,
                        "approved V3 sprite matches the sector and visible progression tier");
                    else ok(id>=80 && id<116,"secondary architecture uses preserved legacy atlas");
                }
                ok(first!=BuildingAppearance.sprite(type,2,x,y),"different tier has a distinct silhouette");
            }
            ok(variants.size()==7,"all seven appearances are represented");
        }
        try {BuildingAppearance.sprite(TileType.WATER,1,0,0);throw new AssertionError("unsupported sprite");}
        catch(IllegalArgumentException expected) {checks++;}
        City city=new City();
        CityFocus.Target focus=CityFocus.forCity(city);
        ok(Math.abs(focus.x)<40,"new city focus X near neighborhood");
        ok(Math.abs(focus.y+768)<40,"new city focus Y near starting neighborhood");
        float zoom=focus.suggestedZoom(1080,2400,370,200);
        ok(zoom>=0.64f && zoom<=1.4f,"camera zoom bounded");
        ok(Float.isFinite(zoom),"camera zoom finite");
        ok(focus.minX<focus.maxX && focus.minY<focus.maxY,"target contains occupied streets");
        int goal=CityMilestones.nextGoal(city.residents());
        ok(goal==100,"initial city begins with 100 person target");
        List<CityMilestones.Milestone> unlocked=CityMilestones.crossed(90,510);
        ok(unlocked.size()==3,"100/250/500 crossed by a fast-growing city");
        ok(unlocked.get(0).population==100 && unlocked.get(2).population==500,"goal order stable");
        ok(CityMilestones.crossed(500,500).isEmpty(),"no repeated goals on idle days");
        ok(CityMilestones.crossed(520,510).isEmpty(),"population decline is not celebrated");
        ok(CityMilestones.crossed(500,520).isEmpty(),"loading above threshold does not replay old goals");
        int seen=0;
        for(int day=0;day<200;day++) {
            city.nextDay();
            for(CityMilestones.Milestone m:city.recentMilestones()) {
                seen++;
                ok(m.population<=city.residents(),"achievement backed by actual population");
            }
            if(seen>0)break;
        }
        ok(seen>0,"real daily growth reaches at least one milestone");
        ByteArrayOutputStream encoded=new ByteArrayOutputStream();
        CityPersistence.save(city,encoded);
        City decoded=CityPersistence.load(new ByteArrayInputStream(encoded.toByteArray()));
        ok(decoded.recentMilestones().isEmpty(),"ephemeral event is not saved to CVT3");
        ok(decoded.residents()==city.residents() && decoded.day()==city.day(),"saved city statistics unchanged");
        CityFocus.Target after=CityFocus.forCity(decoded);
        ok(after.x==CityFocus.forCity(city).x && after.y==CityFocus.forCity(city).y,
            "focus calculated from persisted urban fabric");
        System.out.println("OK: "+checks+" verificacoes de arquitetura, camera, marcos e saves CVT3.");
    }
}
