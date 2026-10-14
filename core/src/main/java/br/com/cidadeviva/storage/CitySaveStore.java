package br.com.cidadeviva.storage;

import br.com.cidadeviva.sim.City;
import br.com.cidadeviva.sim.CityPersistence;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/** A previous valid save remains recoverable if the current file is corrupt or a write fails. */
public final class CitySaveStore {
    private final File primary;
    private final File temporary;
    private final File backup;
    private final File discarded;

    public static final class Loaded {
        public final City city;
        public final boolean fromBackup;
        Loaded(City city, boolean fromBackup) {
            this.city = city;
            this.fromBackup = fromBackup;
        }
    }

    public CitySaveStore(File primary) {
        this.primary = primary;
        this.temporary = new File(primary.getPath() + ".tmp");
        this.backup = new File(primary.getPath() + ".bak");
        this.discarded = new File(primary.getPath() + ".corrupt");
    }

    public boolean hasAnySave() { return primary.exists() || temporary.exists() || backup.exists(); }

    public Loaded load() throws IOException {
        IOException issue = null;
        // A crash between rotating primary and promoting a fully written temp
        // must recover the latest verified state, not initialize a fresh city.
        if (temporary.exists()) {
            try { return new Loaded(read(temporary), true); }
            catch (IOException e) {
                if (issue!=null)e.addSuppressed(issue);
                issue=e;
            }
        }
        if (primary.exists()) {
            try { return new Loaded(read(primary), false); }
            catch (IOException e) { issue = e; }
        }
        if (backup.exists()) {
            try { return new Loaded(read(backup), true); }
            catch (IOException e) {
                if (issue != null) e.addSuppressed(issue);
                throw e;
            }
        }
        if (issue != null) throw issue;
        return null;
    }

    private static City read(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            return CityPersistence.load(in);
        }
    }

    private static void move(File source, File dest) throws IOException {
        if (!source.renameTo(dest)) throw new IOException("Não foi possível renomear: " + source.getName());
    }

    public void save(City city) throws IOException {
        File parent = primary.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs() && !parent.isDirectory())
            throw new IOException("Diretório de salvamento indisponível");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        CityPersistence.save(city, buffer);
        byte[] payload = buffer.toByteArray();
        // Validate before touching any previously saved state.
        CityPersistence.load(new ByteArrayInputStream(payload));
        try (FileOutputStream out = new FileOutputStream(temporary)) {
            out.write(payload);
            out.getFD().sync();
        }
        read(temporary);

        boolean goodPrimary = false;
        if (primary.exists()) {
            try { read(primary); goodPrimary = true; }
            catch (IOException e) { /* Preserve backup; do not rotate a corrupt primary onto it. */ }
        }
        if (goodPrimary) {
            // Preserve existing backup until the verified new temporary file is ready.
            // Same-directory rename is atomic on Android's internal Linux filesystem.
            move(primary, backup);
        } else if (primary.exists()) {
            // Preserve the corrupt primary separately for diagnosis, never overwrite a good backup.
            if (discarded.exists() && !discarded.delete())
                throw new IOException("Não foi possível preservar arquivo corrompido");
            move(primary, discarded);
        }
        try {
            move(temporary, primary);
        } catch (IOException failure) {
            if (!primary.exists() && goodPrimary && backup.exists()) {
                try { move(backup, primary); }
                catch (IOException restore) { failure.addSuppressed(restore); }
            }
            throw failure;
        }
    }
}
