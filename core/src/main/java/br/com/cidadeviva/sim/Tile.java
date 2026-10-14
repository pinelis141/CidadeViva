package br.com.cidadeviva.sim;

public final class Tile {
    public TileType type;
    public int level;
    /** Anchor id + 1 for a 2x2 building, zero for an independent lot. */
    public int block;
    public int fireDays;

    public Tile(TileType type) { this(type, 0); }
    public Tile(TileType type, int level) {
        this.type = type;
        this.level = level;
    }
}
