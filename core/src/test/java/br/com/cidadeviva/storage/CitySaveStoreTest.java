package br.com.cidadeviva.storage;

import br.com.cidadeviva.sim.City;
import br.com.cidadeviva.sim.CityPersistence;
import br.com.cidadeviva.sim.TileType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.util.Arrays;

public final class CitySaveStoreTest {
    private static int checks;
    private static void ok(boolean yes,String label) {
        checks++;
        if (!yes) throw new AssertionError(label);
    }
    private static void delete(File dir) {
        if (dir.isDirectory()) {
            File[] nested=dir.listFiles();
            if (nested!=null) for (File child:nested)delete(child);
        }
        if (!dir.delete() && dir.exists()) throw new IllegalStateException("cleanup failed " + dir);
    }
    public static void main(String[] args) throws Exception {
        File dir=Files.createTempDirectory("cidade-viva-save-test").toFile();
        try {
            File file=new File(dir,"saves/subdir/cidade.cvt");
            CitySaveStore store=new CitySaveStore(file);
            ok(!store.hasAnySave(),"sem save inicialmente");
            ok(store.load()==null,"load vazio");
            City day1=new City();
            int initial=day1.money();
            store.save(day1);
            ok(file.exists(),"save criado");
            ok(store.load().city.money()==initial,"primeiro save carregado");
            City day2=new City();
            day2.build(20,20,TileType.ROAD);
            day2.nextDay();
            store.save(day2);
            ok(new File(file.getPath()+".bak").exists(),"backup criado");
            ok(store.load().city.day()==day2.day(),"save novo tem dia novo");
            ok(store.load().city.tile(20,20).type==TileType.ROAD,"save contém construção nova");
            // Corrupt current primary and check fallback to previously verified backup.
            try(FileOutputStream out=new FileOutputStream(file)){out.write(new byte[]{0,1,2});}
            CitySaveStore.Loaded recovered=store.load();
            ok(recovered.fromBackup,"fallback usado");
            ok(recovered.city.day()==day1.day(),"backup preserva dia anterior");
            // Saving recovered city must NEVER replace a healthy backup with the corrupt file.
            store.save(recovered.city);
            ok(store.load().city.day()==day1.day(),"save após recovery");
            ok(new File(file.getPath()+".bak").exists(),"backup após recovery");
            ok(new File(file.getPath()+".corrupt").exists(),"corrupção preservada");
            // Invalid/truncated data must not be interpreted as a valid game.
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            CityPersistence.save(day2,out);
            byte[] valid=out.toByteArray();
            for (int n:new int[]{0,1,12,40,valid.length-1}) {
                try {
                    CityPersistence.load(new ByteArrayInputStream(Arrays.copyOf(valid,n)));
                    throw new AssertionError("Truncated file accepted length "+n);
                } catch (java.io.IOException expected) { checks++; }
            }
            // In v2, a 1-byte corruption that preserves a valid tile value must still be rejected.
            byte[] changed=Arrays.copyOf(valid,valid.length);
            changed[100] ^= 1;
            try { CityPersistence.load(new ByteArrayInputStream(changed));throw new AssertionError("CRC invalid"); }
            catch(java.io.IOException expected){checks++;}
            // Existing 0.1.0 CVT1 saves must remain readable after the update.
            ByteArrayOutputStream legacy=new ByteArrayOutputStream();
            DataOutputStream old=new DataOutputStream(legacy);
            old.writeInt(0x43565431);old.writeInt(City.WIDTH);old.writeInt(City.HEIGHT);
            old.writeInt(day2.money());old.writeInt(day2.day());old.writeInt(day2.lastIncome());
            for (int x=0;x<City.WIDTH;x++) for (int y=0;y<City.HEIGHT;y++) {
                old.writeByte(day2.tile(x,y).type.ordinal());old.writeByte(day2.tile(x,y).level);
            }
            old.flush();
            City oldLoaded=CityPersistence.load(new ByteArrayInputStream(legacy.toByteArray()));
            ok(oldLoaded.day()==day2.day() && oldLoaded.money()==day2.money(),"compatibilidade CVT1");
            valid[0]=0;
            try { CityPersistence.load(new ByteArrayInputStream(valid));throw new AssertionError("magic invalid"); }
            catch(java.io.IOException expected){checks++;}
        } finally {delete(dir);}
        System.out.println("OK: "+checks+" verificações de backup e integridade de salvamento.");
    }
}
