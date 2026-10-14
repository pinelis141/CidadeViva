package br.com.cidadeviva.sim;

public enum TileType {
    GRASS, WATER, FOREST, ROAD, RESIDENTIAL, COMMERCIAL, INDUSTRIAL, PARK, SCHOOL, CLINIC;

    public boolean isService() {return this==PARK || this==SCHOOL || this==CLINIC;}

    public boolean isZone() {
        return this == RESIDENTIAL || this == COMMERCIAL || this == INDUSTRIAL;
    }
}
