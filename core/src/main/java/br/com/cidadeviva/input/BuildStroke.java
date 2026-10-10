package br.com.cidadeviva.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Isometric build gestures independent of Android/libGDX. Coordinates are map cells. */
public final class BuildStroke {
    public static final int MAX_ZONE_SIDE = 16;
    private BuildStroke() { }

    public static final class Cell {
        public final int x;
        public final int y;
        public Cell(int x, int y) { this.x=x; this.y=y; }
        @Override public boolean equals(Object value) {
            if (!(value instanceof Cell)) return false;
            Cell other=(Cell)value;
            return x==other.x && y==other.y;
        }
        @Override public int hashCode() { return Objects.hash(x,y); }
    }

    /** Draw continuous four-connected road tiles, even for diagonal fast swipes. */
    public static List<Cell> line(int x0,int y0,int x1,int y1) {
        List<Cell> cells=new ArrayList<>();
        int dx=Math.abs(x1-x0),dy=Math.abs(y1-y0);
        int sx=x0<x1?1:-1,sy=y0<y1?1:-1;
        int err=dx-dy;
        cells.add(new Cell(x0,y0));
        while (x0!=x1 || y0!=y1) {
            int twice=2*err;
            // Resolve dual-axis diagonal steps into two touching, cardinal steps.
            if (twice>-dy) {
                err-=dy;x0+=sx;
                cells.add(new Cell(x0,y0));
            }
            if (twice<dx) {
                err+=dx;y0+=sy;
                cells.add(new Cell(x0,y0));
            }
        }
        return cells;
    }

    /** Zone and demolition brush: inclusive area, max 16x16 cells, limited from start. */
    public static List<Cell> rectangle(int x0,int y0,int x1,int y1) {
        x1=clamp(x1,x0-MAX_ZONE_SIDE+1,x0+MAX_ZONE_SIDE-1);
        y1=clamp(y1,y0-MAX_ZONE_SIDE+1,y0+MAX_ZONE_SIDE-1);
        List<Cell> result=new ArrayList<>();
        for (int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)
            for (int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++) result.add(new Cell(x,y));
        return result;
    }

    private static int clamp(int value,int min,int max) {
        return Math.max(min,Math.min(max,value));
    }
}
