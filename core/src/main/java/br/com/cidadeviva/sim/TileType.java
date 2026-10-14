package br.com.cidadeviva.sim;

public enum TileType {
    GRASS, WATER, FOREST, ROAD, RESIDENTIAL, COMMERCIAL, INDUSTRIAL, PARK, SCHOOL, CLINIC, POWER_PLANT, WATER_PUMP, FIRE_STATION, POLICE_STATION, WASTE_CENTER, SEWAGE_PLANT, BUS_DEPOT, BUS_STOP;

    public boolean isUtility(){return this==POWER_PLANT || this==WATER_PUMP;}

    public boolean isSocialService(){return this==PARK || this==SCHOOL || this==CLINIC;}
    public boolean isCivic(){return ordinal()>=FIRE_STATION.ordinal();}
    public boolean isService() {return isSocialService() || isCivic();}

    public boolean isZone() {
        return this == RESIDENTIAL || this == COMMERCIAL || this == INDUSTRIAL;
    }
}
