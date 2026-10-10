package br.com.cidadeviva.storage;

import br.com.cidadeviva.sim.City;
import br.com.cidadeviva.sim.TileType;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Arrays;

/** Non-Android regression tests for legacy save, independent slots, safe switching. */
public final class CitySlotsTest {
    private static int checks;
    private static void check(boolean ok,String name) {
        checks++;
        if(!ok)throw new AssertionError(name);
    }
    private static void cleanup(File dir) {
        if(dir.isDirectory()) {
            File[] children=dir.listFiles();
            if(children!=null)for(File file:children)cleanup(file);
        }
        if(!dir.delete() && dir.exists())throw new IllegalStateException("Unable to delete "+dir);
    }
    public static void main(String[] args) throws Exception {
        File dir=Files.createTempDirectory("cidade-viva-slots").toFile();
        try {
            CitySaveStore legacy=new CitySaveStore(new File(dir,"salvamento.cvt"));
            City old=new City();
            old.build(20,20,TileType.ROAD);
            old.nextDay();
            legacy.save(old);
            byte[] baseline=Files.readAllBytes(new File(dir,"salvamento.cvt").toPath());
            CitySlots slots=new CitySlots(dir);
            check(slots.active()==1,"original default active slot is 1");
            check(slots.fileFor(1).equals(new File(dir,"salvamento.cvt")),"exact old save path");
            check(slots.inspect(1).day==old.day(),"legacy city day visible");
            check(slots.inspect(2).empty,"slot2 empty");
            check(slots.inspect(3).empty,"slot3 empty");
            City loaded=slots.openActive().city;
            check(loaded.tile(20,20).type==TileType.ROAD,"legacy construction recovered");
            CitySlots.Opened second=slots.switchTo(2,loaded,true);
            check(second.created,"slot2 generated new city");
            check(second.city.day()==1,"slot2 starts on day1");
            check(slots.active()==2,"slot2 became active");
            check(Arrays.equals(baseline,Files.readAllBytes(new File(dir,"salvamento.cvt").toPath())),
                "switch does not change the original save bytes");
            check(new CitySlots(dir).active()==2,"active slot persisted");
            boolean different=false;
            for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
                if(second.city.tile(x,y).type!=old.tile(x,y).type)different=true;
            }
            check(different,"new city slot gets distinct lakes and trees");
            second.city.build(20,20,TileType.ROAD);
            second.city.nextDay();
            slots.save(second.city);
            check(slots.inspect(2).day==2,"slot2 progress saved");
            CitySlots.Opened third=slots.switchTo(3,second.city,true);
            check(third.created,"slot3 fresh and independent");
            check(slots.inspect(2).day==2,"slot2 preserved when visiting 3");
            check(slots.inspect(3).day==1,"slot3 day one");
            CitySlots restarted=new CitySlots(dir);
            check(restarted.active()==3,"new app instance remembers slot3");
            check(restarted.openActive().city.day()==1,"can restart slot3");
            CitySlots.Opened back=restarted.switchTo(1,third.city,true);
            check(!back.created,"slot1 was not reset");
            check(back.city.day()==old.day(),"legacy day unchanged");
            check(back.city.tile(20,20).type==TileType.ROAD,"legacy road preserved");
            check(restarted.inspect(2).day==2,"slot2 untouched after returning to slot1");
            // Corrupt both copies of slot 3. Switch must fail, protecting it and slot 1.
            File broken=restarted.fileFor(3);
            try(FileOutputStream out=new FileOutputStream(broken)){out.write(1);}
            File backup=new File(broken.getPath()+".bak");
            try(FileOutputStream out=new FileOutputStream(backup)){out.write(1);}
            byte[] invalid=Files.readAllBytes(broken.toPath());
            try {restarted.switchTo(3,back.city,true);throw new AssertionError("corrupt slot opened");}
            catch(java.io.IOException expected){checks++;}
            check(restarted.active()==1,"active slot unchanged on failure");
            check(restarted.inspect(3).corrupt,"damaged slot marked protected");
            check(Arrays.equals(invalid,Files.readAllBytes(broken.toPath())),"corrupt files never replaced");
            check(restarted.inspect(1).day==old.day(),"legacy survives failed switch");
            check(new CitySlots(dir).active()==1,"active slot read after failed switch");
            try{restarted.fileFor(4);throw new AssertionError("fourth slot accepted");}
            catch(IllegalArgumentException expected){checks++;}
        } finally {cleanup(dir);}
        System.out.println("OK: "+checks+" slot/legacy-isolation assertions.");
    }
}
