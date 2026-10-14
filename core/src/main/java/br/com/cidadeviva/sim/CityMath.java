package br.com.cidadeviva.sim;

/** API-23-safe floor modulus, preserving deterministic maps and simulation rolls. */
public final class CityMath {
    private CityMath(){}
    public static int floorMod(int x,int y){int r=x%y;return r!=0&&((r^y)<0)?r+y:r;}
}
