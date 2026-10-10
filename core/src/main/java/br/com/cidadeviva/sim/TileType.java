package br.com.cidadeviva.sim;

public enum TileType {
    GRASS, WATER, FOREST, ROAD, RESIDENTIAL, COMMERCIAL, INDUSTRIAL;

    public boolean isZone() {
        return this == RESIDENTIAL || this == COMMERCIAL || this == INDUSTRIAL;
    }
}
