package br.com.cidadeviva.sim;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

public final class CityLoanTest {
    private static int checks;
    private static void ok(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    private static byte[] bytes(City c)throws Exception{ByteArrayOutputStream b=new ByteArrayOutputStream();CityPersistence.save(c,b);return b.toByteArray();}
    private static City load(byte[] b)throws Exception{return CityPersistence.load(new ByteArrayInputStream(b));}
    private static void crc(byte[] b){CRC32 c=new CRC32();c.update(b,0,b.length-4);ByteBuffer.wrap(b).putInt(b.length-4,(int)c.getValue());}
    private static void rejects(byte[] b)throws Exception{crc(b);try{load(b);throw new AssertionError("invalid loan accepted");}catch(IOException expected){checks++;}}
    public static void main(String[] args)throws Exception{
        for(CityLoan offer:CityLoan.values()){
            City c=new City();int cash=c.money(),net=c.economy().net;
            ok(c.borrow(offer),"first loan accepted");
            ok(c.money()==cash+offer.principal && c.loanBalance()==offer.total,"exact principal and total interest");
            ok(c.economy().net==net-offer.payment && c.loanPaymentsLeft()==offer.days,"payment included in budget and term");
            int borrowedCash=c.money();ok(!c.borrow(CityLoan.SMALL)&&c.money()==borrowedCash,"active loan cannot be stacked");
            City twin=load(bytes(c));int paid=0;
            for(int d=0;d<offer.days;d++){
                int opening=c.money(),debt=c.loanBalance(),forecast=c.economy().net;
                c.nextDay();twin.nextDay();paid+=debt-c.loanBalance();
                ok(c.money()==opening+forecast,"daily budget includes installment exactly once");
                ok(c.money()==twin.money()&&c.loanBalance()==twin.loanBalance()&&c.loanInstallment()==twin.loanInstallment(),"CVT6 replay preserves payments");
            }
            ok(paid==offer.total&&c.loanBalance()==0&&c.loanPayment()==0&&c.loanInstallment()==0,"exact payoff without extra interest or installments");
            ok(c.borrow(CityLoan.SMALL),"new loan available after payoff");
        }
        City poor=new City();Tile[][] map=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)map[x][y]=new Tile(TileType.ROAD);
        poor.restore(map,0,1,0);
        ok(poor.borrow(CityLoan.SMALL),"credit available for a bankrupt city");
        int debt=poor.loanBalance();poor.nextDay();
        ok(poor.money()==0&&poor.loanBalance()==debt,"unfunded installment remains due");
        City partial=new City();
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)map[x][y]=new Tile(TileType.GRASS);
        partial.restore(map,20,1,0);partial.restoreLoan(55,55);partial.nextDay();
        ok(partial.money()==0&&partial.loanBalance()==35&&partial.loanPayment()==35,"partial payment retains remaining obligation");
        City tail=load(bytes(partial));ok(tail.loanBalance()==35,"partial payment survives reload");
        tail.restoreLoan(0,0);ok(tail.loanPayment()==0,"zero balance has no payment");
        byte[] current=bytes(new City());
        byte[] v5=LegacyCitySave.bytes(new City(),5);
        City migrated=load(v5);ok(migrated.loanBalance()==0&&migrated.money()==15000,"CVT5 migration starts debt-free without changing cash");
        byte[] bad=current.clone();ByteBuffer.wrap(bad).putInt(bad.length-13,-1);rejects(bad);
        bad=current.clone();ByteBuffer.wrap(bad).putInt(bad.length-9,55);rejects(bad);
        bad=current.clone();ByteBuffer.wrap(bad).putInt(bad.length-13,999999).putInt(bad.length-9,55);rejects(bad);
        bad=current.clone();ByteBuffer.wrap(bad).putInt(bad.length-13,100).putInt(bad.length-9,0);rejects(bad);
        City cap=new City();cap.restore(map,Integer.MAX_VALUE,1,0);
        ok(!cap.borrow(CityLoan.LARGE)&&cap.money()==Integer.MAX_VALUE,"credit cannot overflow treasury");
        ok(!new City().borrow(null),"invalid offer rejected");
        City finite=new City();finite.restore(map,55,1,0);finite.restoreLoan(55,55);
        ok(finite.cashRunwayDays()==Integer.MAX_VALUE,"affordable final installment does not forecast permanent depletion");
        finite.nextDay();ok(finite.loanBalance()==0&&finite.money()==0,"final affordable payment can complete");
        City transientDeficit=new City();transientDeficit.borrow(CityLoan.LARGE);
        ok(transientDeficit.economy().net<0&&transientDeficit.cashRunwayDays()==Integer.MAX_VALUE,"finite debt is not forecast as an endless operating deficit");
        System.out.println("OK: "+checks+" loan offers, payments, arrears, payoff, bankruptcy, CVT5 migration, CVT6 replay and corrupt saves.");
    }
}
