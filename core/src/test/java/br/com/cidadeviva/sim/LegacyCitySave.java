package br.com.cidadeviva.sim;

import java.io.*;
import java.util.zip.CRC32;

/** Independent legacy-format fixtures: migration tests must not truncate the current writer. */
public final class LegacyCitySave {
    private LegacyCitySave(){}
    public static byte[] bytes(City c,int version)throws IOException{
        ByteArrayOutputStream b=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(b);
        out.writeInt(0x43565430+version);out.writeInt(City.WIDTH);out.writeInt(City.HEIGHT);out.writeInt(c.money());out.writeInt(c.day());out.writeInt(c.lastIncome());
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++){Tile t=c.tile(x,y);out.writeByte(t.type.ordinal());out.writeByte(t.level);}
        if(version>=3){for(TileType t:new TileType[]{TileType.RESIDENTIAL,TileType.COMMERCIAL,TileType.INDUSTRIAL})out.writeByte(c.taxRate(t));for(TileType t:new TileType[]{TileType.RESIDENTIAL,TileType.COMMERCIAL,TileType.INDUSTRIAL})out.writeInt(c.lowDemandDays(t));}
        if(version>=5)out.writeByte(c.utilityFlags());
        if(version>=6){out.writeInt(c.loanBalance());out.writeInt(c.loanInstallment());}
        out.flush();if(version>=2){CRC32 crc=new CRC32();crc.update(b.toByteArray());out.writeInt((int)crc.getValue());}out.flush();return b.toByteArray();
    }
}
