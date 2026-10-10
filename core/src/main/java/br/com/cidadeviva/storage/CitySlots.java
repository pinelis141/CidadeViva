package br.com.cidadeviva.storage;

import br.com.cidadeviva.sim.City;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Three independent cities. Slot one deliberately retains the legacy save pathname.
 *  All operations are per-slot, including .tmp/.bak/.corrupt files.
 */
public final class CitySlots {
    public static final int COUNT=3;
    private final File folder;
    private final File activeFile;
    private int active;

    public static final class SlotInfo {
        public final int slot;
        public final boolean empty, corrupt, backupUsed, active;
        public final int day, population, money;
        private SlotInfo(int slot, boolean empty, boolean corrupt, boolean backupUsed,
                         boolean active, int day, int population, int money) {
            this.slot=slot;this.empty=empty;this.corrupt=corrupt;this.backupUsed=backupUsed;
            this.active=active;this.day=day;this.population=population;this.money=money;
        }
    }
    public static final class Opened {
        public final City city;
        public final boolean created, fromBackup;
        private Opened(City city,boolean created,boolean fromBackup) {
            this.city=city;this.created=created;this.fromBackup=fromBackup;
        }
    }

    public CitySlots(File baseDir) {
        folder=baseDir;
        activeFile=new File(baseDir,"active-slot.txt");
        active=readActive();
    }
    private static void validate(int slot) {
        if (slot<1 || slot>COUNT)throw new IllegalArgumentException("Espaço inválido");
    }
    public int active() {return active;}
    public File fileFor(int slot) {
        validate(slot);
        // Slot 1 is the original Alpha 1/2/3 path. Never move/delete its files.
        if (slot==1)return new File(folder,"salvamento.cvt");
        return new File(new File(folder,"slots"),"cidade-"+slot+".cvt");
    }
    public CitySaveStore store(int slot) {return new CitySaveStore(fileFor(slot));}
    private int readActive() {
        if (!activeFile.isFile())return 1;
        try(FileInputStream in=new FileInputStream(activeFile)) {
            byte[] data=new byte[8];int n=in.read(data);
            if(n>=1 && n<=3) {
                String value=new String(data,0,n,StandardCharsets.US_ASCII).trim();
                int slot=Integer.parseInt(value);
                if(slot>=1 && slot<=COUNT)return slot;
            }
        }catch(IOException | NumberFormatException ignored) { /* fallback safely */ }
        return 1;
    }
    private void storeActive(int slot) throws IOException {
        if(!folder.isDirectory() && !folder.mkdirs() && !folder.isDirectory())
            throw new IOException("Sem acesso ao diretório das cidades");
        File temporary=new File(folder,"active-slot.tmp");
        try(FileOutputStream out=new FileOutputStream(temporary)) {
            out.write(Integer.toString(slot).getBytes(StandardCharsets.US_ASCII));
            out.getFD().sync();
        }
        if (!temporary.renameTo(activeFile)) {
            // Android/Linux permits replacing a file in the same directory atomically.
            throw new IOException("Não foi possível trocar a cidade ativa");
        }
    }
    public SlotInfo inspect(int slot) {
        validate(slot);
        CitySaveStore slotStore=store(slot);
        if(!slotStore.hasAnySave())return new SlotInfo(slot,true,false,false,slot==active,0,0,0);
        try {
            CitySaveStore.Loaded loaded=slotStore.load();
            if (loaded==null)return new SlotInfo(slot,true,false,false,slot==active,0,0,0);
            return new SlotInfo(slot,false,false,loaded.fromBackup,slot==active,
                loaded.city.day(),loaded.city.residents(),loaded.city.money());
        }catch(IOException e) {
            return new SlotInfo(slot,false,true,false,slot==active,0,0,0);
        }
    }
    /** Safe opening: refuses corrupt occupied slots. A new slot is immediately saved,
     *  preventing a failed initialization from appearing as an existing active city.
     */
    public Opened open(int slot) throws IOException {
        validate(slot);
        CitySaveStore target=store(slot);
        CitySaveStore.Loaded loaded=target.load();
        if(loaded!=null)return new Opened(loaded.city,false,loaded.fromBackup);
        City fresh=new City(slot==1?0:slot*941);
        target.save(fresh);
        return new Opened(fresh,true,false);
    }
    /** Save the outgoing city before switching. No deletion or replacement of other slots. */
    public Opened switchTo(int destination,City outgoing,boolean outgoingSafe) throws IOException {
        validate(destination);
        if(destination==active)throw new IllegalArgumentException("Esta cidade já está aberta");
        // Validate or initialize destination first. Corrupt files must not be overwritten.
        Opened opened=open(destination);
        if(outgoingSafe && outgoing!=null)store(active).save(outgoing);
        storeActive(destination);
        active=destination;
        return opened;
    }
    public void save(City city) throws IOException {store(active).save(city);}
    public Opened openActive() throws IOException {return open(active);}
}
