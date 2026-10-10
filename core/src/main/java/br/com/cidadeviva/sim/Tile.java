package br.com.cidadeviva.sim;

public final class Tile {
    public TileType type;
    public int level;

    public Tile(TileType type) { this(type, 0); }
    public Tile(TileType type, int level) {
        this.type = type;
        this.level = level;
    }
}
