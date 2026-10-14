package br.com.cidadeviva.sim;

import br.com.cidadeviva.storage.CitySlots;
import java.io.*;
import java.nio.file.Files;
import java.util.zip.CRC32;

/** Standalone Java 17 regression of CVT3, legacy migration, tax pressure and decline. */
public final class CityTaxPolicyTest {
    private static int assertions;
    private static void ok(boolean x,String what) {assertions++;if(!x)throw new AssertionError(what);}
    private static byte[] write(City c) throws Exception {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        CityPersistence.save(c,out);return out.toByteArray();
    }
    private static City read(byte[] raw) throws Exception {return CityPersistence.load(new ByteArrayInputStream(raw));}
    private static byte[] legacyV2(City c) throws Exception {
        ByteArrayOutputStream payload=new ByteArrayOutputStream();
        DataOutputStream out=new DataOutputStream(payload);
        out.writeInt(0x43565432);
        out.writeInt(City.WIDTH);out.writeInt(City.HEIGHT);
        out.writeInt(c.money());out.writeInt(c.day());out.writeInt(c.lastIncome());
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
            Tile t=c.tile(x,y);out.writeByte(t.type.ordinal());out.writeByte(t.level);
        }
        byte[] raw=payload.toByteArray();CRC32 crc=new CRC32();crc.update(raw);
        out.writeInt((int)crc.getValue());return payload.toByteArray();
    }
    private static void mustReject(byte[] data,String reason) throws Exception {
        try {read(data);throw new AssertionError("accepted "+reason);}
        catch(IOException expected) {assertions++;}
    }
    public static void main(String[] args) throws Exception {
        City city=new City();
        CityEconomy base=city.economy();
        for(TileType kind:new TileType[]{TileType.RESIDENTIAL,TileType.COMMERCIAL,TileType.INDUSTRIAL}) {
            ok(city.taxRate(kind)==10,"default 10% on each category");
            for(int bad:new int[]{-1,0,4,21,30}) {
                try {city.setTaxRate(kind,bad);throw new AssertionError("bad tax accepted");}
                catch(IllegalArgumentException e) {assertions++;}
            }
        }
        city.setTaxRate(TileType.RESIDENTIAL,20);
        city.setTaxRate(TileType.COMMERCIAL,5);
        city.setTaxRate(TileType.INDUSTRIAL,13);
        CityEconomy changed=city.economy();
        ok(changed.taxResidential<base.taxResidential,"20% reduces effective collection after avoidance costs");
        ok(changed.taxCommercial<=base.taxCommercial,"commercial tax income falls");
        ok(changed.residentialDemand<=base.residentialDemand,"higher tax discourages residential growth");
        ok(changed.commercialDemand>=base.commercialDemand,"lower tax attracts commerce");
        ok(changed.net==changed.grossRevenue-changed.expenses,"consistent budget");
        byte[] v3=write(city);
        ok(new DataInputStream(new ByteArrayInputStream(v3)).readInt()==0x43565436,"CVT6 signature");
        City restored=read(v3);
        ok(restored.taxRate(TileType.RESIDENTIAL)==20,"residential tax roundtrips");
        ok(restored.taxRate(TileType.COMMERCIAL)==5,"commercial tax roundtrips");
        ok(restored.taxRate(TileType.INDUSTRIAL)==13,"industrial tax roundtrips");
        ok(restored.money()==city.money() && restored.day()==city.day(),"ordinary state unchanged");
        byte[] invalid=v3.clone(); invalid[invalid.length-8]^=8;
        mustReject(invalid,"wrong CRC in new settings");
        byte[] shortData=new byte[v3.length-9];System.arraycopy(v3,0,shortData,0,shortData.length);
        mustReject(shortData,"truncated CVT3");
        City old=read(legacyV2(city));
        ok(old.taxRate(TileType.RESIDENTIAL)==10 && old.taxRate(TileType.COMMERCIAL)==10 &&
           old.taxRate(TileType.INDUSTRIAL)==10,"CVT2 loads with standard tax rates");
        ok(old.day()==city.day() && old.money()==city.money(),"legacy city state intact");
        // Construct a city with overbuilt businesses and no households: this
        // keeps commercial demand consistently low without relying on chance.
        City troubled=new City();
        Tile[][] overbuilt=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
            Tile src=troubled.tile(x,y);
            overbuilt[x][y]=new Tile(src.type,src.level);
            if(src.type.isZone())overbuilt[x][y]=new Tile(TileType.COMMERCIAL,3);
        }
        troubled.restore(overbuilt,100000,1,0,new int[]{10,20,10},new int[]{0,0,0});
        ok(troubled.economy().commercialDemand<20,"unbalanced business district has no consumers");
        int initial=troubled.economy().commercialJobs;
        for(int n=0;n<11;n++)troubled.nextDay();
        ok(troubled.economy().commercialJobs==initial,"no abandonment before prolonged low demand");
        City restarted=read(write(troubled));
        ok(restarted.lowDemandDays(TileType.COMMERCIAL)==11,"weak demand history roundtrips");
        for(int n=0;n<320;n++) {troubled.nextDay();restarted.nextDay();}
        ok(troubled.economy().commercialJobs<initial,"prolonged glut causes gradual abandonment");
        ok(restarted.economy().commercialJobs==troubled.economy().commercialJobs,"identical declines after reload");
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)
            ok(troubled.tile(x,y).level==restarted.tile(x,y).level,"persisted evolution deterministic");
        // Three independent cities: tax on one must not leak onto another.
        File folder=Files.createTempDirectory("cidade-taxes-").toFile();
        CitySlots slots=new CitySlots(folder);
        City first=slots.openActive().city;
        first.setTaxRate(TileType.RESIDENTIAL,18);
        slots.save(first);
        CitySlots.Opened second=slots.switchTo(2,first,true);
        ok(second.created,"new second city");
        ok(second.city.taxRate(TileType.RESIDENTIAL)==10,"second city uses default rate");
        second.city.setTaxRate(TileType.INDUSTRIAL,6);
        City firstAgain=slots.switchTo(1,second.city,true).city;
        ok(firstAgain.taxRate(TileType.RESIDENTIAL)==18,"first city tax kept");
        ok(firstAgain.taxRate(TileType.INDUSTRIAL)==10,"second city settings never overwrite first");
        ok(slots.switchTo(2,firstAgain,true).city.taxRate(TileType.INDUSTRIAL)==6,"second city tax saved");
        System.out.println("OK: "+assertions+" verificacoes de impostos, CVT3, abandono e slots.");
    }
}
