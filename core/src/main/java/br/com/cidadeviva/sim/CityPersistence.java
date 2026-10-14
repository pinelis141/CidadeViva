package br.com.cidadeviva.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

/** CVT7 stores footprint, fires and civic activation, reading CVT1 through CVT6. */
public final class CityPersistence {
    private static final int V1 = 0x43565431; // CVT1
    private static final int V2 = 0x43565432; // CVT2
    private static final int V7=0x43565437; // CVT7: 2x2, fire state, sanitation activation
    private static final int V6=0x43565436; // CVT6: outstanding debt and installment
    private static final int V5=0x43565435; // CVT5 persists staged utility activation
    private static final int V4 = 0x43565434; // CVT4 adds service tile kinds
    private static final int V3 = 0x43565433; // CVT3
    private static final int SETTINGS_LENGTH = 3 + 3 * Integer.BYTES;
    private static final int HEADER_LENGTH = 6 * Integer.BYTES;
    private static final int TILE_LENGTH = City.WIDTH * City.HEIGHT * 2;
    private CityPersistence() { }

    public static void save(City city,OutputStream destination) throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream(HEADER_LENGTH+TILE_LENGTH+SETTINGS_LENGTH+4);
        DataOutputStream out = new DataOutputStream(payload);
        out.writeInt(V7);
        out.writeInt(City.WIDTH);
        out.writeInt(City.HEIGHT);
        out.writeInt(city.money());
        out.writeInt(city.day());
        out.writeInt(city.lastIncome());
        for (int x=0; x<City.WIDTH; x++) for (int y=0; y<City.HEIGHT; y++) {
            Tile tile = city.tile(x,y);
            out.writeByte(tile.type.ordinal());
            out.writeByte(tile.level);
            out.writeShort(tile.block);
            out.writeByte(tile.fireDays);
        }
        for(TileType t:new TileType[]{TileType.RESIDENTIAL,TileType.COMMERCIAL,TileType.INDUSTRIAL})
            out.writeByte(city.taxRate(t));
        for(TileType t:new TileType[]{TileType.RESIDENTIAL,TileType.COMMERCIAL,TileType.INDUSTRIAL})
            out.writeInt(city.lowDemandDays(t));
        out.writeByte(city.utilityFlags());
        out.writeInt(city.loanBalance());
        out.writeInt(city.loanInstallment());
        out.writeByte(city.urbanFlags());
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
        ByteArrayOutputStream buffer=new ByteArrayOutputStream(HEADER_LENGTH+TILE_LENGTH+SETTINGS_LENGTH+4);
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
        if (format!=V1 && format!=V2 && format!=V3 && format!=V4 && format!=V5 && format!=V6 && format!=V7) throw new IOException("Versão de salvamento incompatível");
        int expected=HEADER_LENGTH+(format==V7?City.WIDTH*City.HEIGHT*5:TILE_LENGTH)+(format==V7?SETTINGS_LENGTH+10:format==V6?SETTINGS_LENGTH+9:format==V5?SETTINGS_LENGTH+1:(format==V3 || format==V4)?SETTINGS_LENGTH:0)+(format==V1?0:4);
        if (data.length!=expected) throw new IOException("Arquivo truncado ou tamanho inválido");
        if (format!=V1) {
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
            if (type>=(format==V7?all.length:(format==V5 || format==V6)?12:format==V4?10:7) || level>3 || (!all[type].isZone() && level!=0))
                throw new IOException("Tile inválido");
            Tile tile=new Tile(all[type],level);
            if(format==V7){
                tile.block=in.readUnsignedShort();tile.fireDays=in.readUnsignedByte();
                if(tile.fireDays>4||(tile.fireDays>0&&(!tile.type.isZone()||tile.level==0)))throw new IOException("Incendio do salvamento invalido");
            }
            incoming[x][y]=tile;
        }
        int[] rates={10,10,10};
        int[] weakDays={0,0,0};
        if(format==V3 || format==V4 || format==V5 || format==V6 || format==V7) {
            for(int i=0;i<3;i++) {
                rates[i]=in.readUnsignedByte();
                if(rates[i]<5 || rates[i]>20)throw new IOException("Aliquota do salvamento invalida");
            }
            for(int i=0;i<3;i++) {
                weakDays[i]=in.readInt();
                if(weakDays[i]<0 || weakDays[i]>1000000)throw new IOException("Historico de demanda invalido");
            }
        }
        int flags=(format==V5 || format==V6 || format==V7)?in.readUnsignedByte():0;
        if(flags>3)throw new IOException("Estado de infraestrutura invalido");
        for(Tile[] column:incoming)for(Tile t:column) {
            if((t.type==TileType.POWER_PLANT && (flags&1)==0) ||
               (t.type==TileType.WATER_PUMP && (flags&2)==0))throw new IOException("Rede construida sem estado ativo");
        }
        int debt=(format==V6||format==V7)?in.readInt():0;
        int installment=(format==V6||format==V7)?in.readInt():0;
        if(!CityLoan.validDebt(debt,installment))throw new IOException("Divida do salvamento invalida");
        int urban=format==V7?in.readUnsignedByte():0;
        if(urban>7||!CityBlocks.valid(incoming))throw new IOException("Estado urbano ou bloco invalido");
        for(Tile[] column:incoming)for(Tile tile:column)
            if((tile.type==TileType.WASTE_CENTER&&(urban&1)==0)||(tile.type==TileType.SEWAGE_PLANT&&(urban&2)==0))throw new IOException("Saneamento construido sem ativacao");
        City city=new City();
        city.restore(incoming,money,day,income,rates,weakDays,flags);
        city.restoreLoan(debt,installment);
        city.restoreUrbanFlags(urban);
        return city;
    }
}
