package br.com.cidadeviva.sim;

/** Fixed total interest; no compound interest or hidden fees. One active loan per city. */
public enum CityLoan {
    SMALL(3000,10,60), MEDIUM(6000,12,80), LARGE(12000,15,100);
    public final int principal,interestPercent,days,total,payment;
    CityLoan(int principal,int interestPercent,int days) {
        this.principal=principal;this.interestPercent=interestPercent;this.days=days;
        this.total=principal+principal*interestPercent/100;this.payment=total/days;
    }
    public static boolean validDebt(int balance,int payment) {
        if(balance==0)return payment==0;
        for(CityLoan offer:values())if(balance>0 && balance<=offer.total && payment==offer.payment)return true;
        return false;
    }
}
