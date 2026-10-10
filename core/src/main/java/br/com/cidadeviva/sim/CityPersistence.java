package br.com.cidadeviva.sim;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Formato binário compacto, versionado e com validação antes de alterar o jogo. */
public final class CityPersistence {
    private static final int MAGIC = 0x43565431; // CVT1
    private CityPersistence() { }

    public static void save(City city,OutputStream raw) throws IOException {
        DataOutputStream out = new DataOutputStream(raw);
        out.writeInt(MAGIC);
        out.writeInt(City.WIDTH);
        out.writeInt(City.HEIGHT);
        out.writeInt(city.money());
        out.writeInt(city.day());
        out.writeInt(city.lastIncome());
        for (int x=0;x<City.WIDTH;x++) for (int y=0;y<City.HEIGHT;y++) {
            Tile tile = city.tile(x,y);
            out.writeByte(tile.type.ordinal());
            out.writeByte(tile.level);
        }
        out.flush();
    }

    public static City load(InputStream raw) throws IOException {
        DataInputStream in = new DataInputStream(raw);
        if (in.readInt() != MAGIC || in.readInt() != City.WIDTH || in.readInt() != City.HEIGHT)
            throw new IOException("Arquivo de cidade incompatível");
        int money = in.readInt();
        int day = in.readInt();
        int income = in.readInt();
        if (money < 0 || day < 1 || day > 100000000) throw new IOException("Cabeçalho inválido");
        Tile[][] incoming = new Tile[City.WIDTH][City.HEIGHT];
        TileType[] all = TileType.values();
        for (int x=0;x<City.WIDTH;x++) for (int y=0;y<City.HEIGHT;y++) {
            int t = in.readUnsignedByte(), level = in.readUnsignedByte();
            if (t >= all.length || level > 3) throw new IOException("Tile inválido");
            incoming[x][y] = new Tile(all[t],level);
        }
        City result = new City();
        result.restore(incoming,money,day,income);
        return result;
    }
}
