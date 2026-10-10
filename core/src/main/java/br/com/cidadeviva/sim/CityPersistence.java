package br.com.cidadeviva.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

/** Compact, versioned local format. V2 adds CRC32; V1 remains readable. */
public final class CityPersistence {
    private static final int V1 = 0x43565431; // CVT1
    private static final int V2 = 0x43565432; // CVT2
    private static final int HEADER_LENGTH = 6 * Integer.BYTES;
    private static final int TILE_LENGTH = City.WIDTH * City.HEIGHT * 2;
    private CityPersistence() { }

    public static void save(City city,OutputStream destination) throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream(HEADER_LENGTH+TILE_LENGTH+4);
        DataOutputStream out = new DataOutputStream(payload);
        out.writeInt(V2);
        out.writeInt(City.WIDTH);
        out.writeInt(City.HEIGHT);
        out.writeInt(city.money());
        out.writeInt(city.day());
        out.writeInt(city.lastIncome());
        for (int x=0; x<City.WIDTH; x++) for (int y=0; y<City.HEIGHT; y++) {
            Tile tile = city.tile(x,y);
            out.writeByte(tile.type.ordinal());
            out.writeByte(tile.level);
        }
        out.flush();
        byte[] data=payload.toByteArray();
        CRC32 crc=new CRC32();
        crc.update(data,0,data.length);
        out.writeInt((int)crc.getValue());
        out.flush();
        destination.write(payload.toByteArray());
        destination.flush();
    }

    public static City load(InputStream source) throws IOException {
        ByteArrayOutputStream buffer=new ByteArrayOutputStream(HEADER_LENGTH+TILE_LENGTH+4);
        byte[] chunk=new byte[1024];
        int n;
        while ((n=source.read(chunk))!=-1) {
            if (buffer.size()+n > 1024*1024) throw new IOException("Arquivo de salvamento muito grande");
            buffer.write(chunk,0,n);
        }
        byte[] data=buffer.toByteArray();
        if (data.length<4) throw new IOException("Arquivo de salvamento incompleto");
        DataInputStream in=new DataInputStream(new ByteArrayInputStream(data));
        int format=in.readInt();
        if (format!=V1 && format!=V2) throw new IOException("Versão de salvamento incompatível");
        int expected=HEADER_LENGTH+TILE_LENGTH+(format==V2?4:0);
        if (data.length!=expected) throw new IOException("Arquivo truncado ou tamanho inválido");
        if (format==V2) {
            CRC32 crc=new CRC32();
            crc.update(data,0,data.length-4);
            long stored=((long)(data[data.length-4]&0xff)<<24)
                       |((long)(data[data.length-3]&0xff)<<16)
                       |((long)(data[data.length-2]&0xff)<<8)
                       |(data[data.length-1]&0xffL);
            if (crc.getValue()!=stored) throw new IOException("Falha na verificação de integridade (CRC32)");
        }
        if (in.readInt()!=City.WIDTH || in.readInt()!=City.HEIGHT)
            throw new IOException("Dimensão de cidade incompatível");
        int money=in.readInt();
        int day=in.readInt();
        int income=in.readInt();
        if (money<0 || day<1 || day>100000000)
            throw new IOException("Cabeçalho inválido");
        Tile[][] incoming=new Tile[City.WIDTH][City.HEIGHT];
        TileType[] all=TileType.values();
        for (int x=0; x<City.WIDTH; x++) for (int y=0; y<City.HEIGHT; y++) {
            int type=in.readUnsignedByte(),level=in.readUnsignedByte();
            if (type>=all.length || level>3 || (!all[type].isZone() && level!=0))
                throw new IOException("Tile inválido");
            incoming[x][y]=new Tile(all[type],level);
        }
        City city=new City();
        city.restore(incoming,money,day,income);
        return city;
    }
}
