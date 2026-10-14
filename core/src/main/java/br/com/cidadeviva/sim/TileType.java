package br.com.cidadeviva.sim;

public enum TileType {
    GRASS, WATER, FOREST, ROAD, RESIDENTIAL, COMMERCIAL, INDUSTRIAL, PARK, SCHOOL, CLINIC, POWER_PLANT, WATER_PUMP;

    public boolean isUtility(){return this==POWER_PLANT || this==WATER_PUMP;}

    public boolean isService() {return this==PARK || this==SCHOOL || this==CLINIC;}

    public boolean isZone() {
        return this == RESIDENTIAL || this == COMMERCIAL || this == INDUSTRIAL;
    }
}
