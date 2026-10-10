package br.com.cidadeviva.render;

import br.com.cidadeviva.sim.TileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** One reproducible, original nearest-neighbor atlas (45 sprites; 72x128 each). */
public final class PixelArtAtlas {
    private static final int CELL_W=72, CELL_H=128, COLS=8;
    private final Texture texture;
    private final TextureRegion[] regions=new TextureRegion[45];

    public PixelArtAtlas() {
        texture=new Texture(Gdx.files.internal("city/atlas.png"));
        texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
        for (int i=0;i<regions.length;i++)
            regions[i]=new TextureRegion(texture,(i%COLS)*CELL_W,(i/COLS)*CELL_H,CELL_W,CELL_H);
    }

    public void draw(SpriteBatch batch,int sprite,float centerX,float centerY) {
        batch.draw(regions[sprite],centerX-36f,centerY-16f,CELL_W,CELL_H);
    }

    /** Stable visual variations do not affect saves, economy or terrain collisions. */
    public static int tileSprite(TileType type,int roadMask,int x,int y) {
        switch(type) {
            case WATER: return 3+Math.floorMod(x*5+y*3,2);
            case ROAD: return 8+(roadMask&15);
            case RESIDENTIAL: return 5;
            case COMMERCIAL: return 6;
            case INDUSTRIAL: return 7;
            default: return Math.floorMod(x*41+y*17+x*y*3,3);
        }
    }
    public static int treeSprite(int x,int y) {
        return 24+Math.floorMod(x*13+y*29,3);
    }
    public static int buildingSprite(TileType type,int level,int x,int y) {
        int kind=type==TileType.RESIDENTIAL?0:type==TileType.COMMERCIAL?1:2;
        int capped=Math.max(1,Math.min(level,3));
        return 27+kind*6+(capped-1)*2+Math.floorMod(x*7+y*11,2);
    }
    public void dispose() {texture.dispose();}
}
