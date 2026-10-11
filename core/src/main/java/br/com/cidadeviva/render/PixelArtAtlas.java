package br.com.cidadeviva.render;

import br.com.cidadeviva.sim.TileType;
import br.com.cidadeviva.sim.BuildingAppearance;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Production atlas; 116 previous indices stable, 9 new building variants. */
public final class PixelArtAtlas {
    private static final int CELL_W=72, CELL_H=128, COLS=8;
    private final Texture texture;
    private final TextureRegion[] regions=new TextureRegion[125];

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
    public static int tileSprite(TileType type,int roadMask,int waterMask,int x,int y) {
        switch(type) {
            case WATER: return 45+(waterMask&15);
            case ROAD: return 8+(roadMask&15);
            case RESIDENTIAL: return 5;
            case COMMERCIAL: return 6;
            case INDUSTRIAL: return 7;
            default: {
                // Coherent patches, scattered wildflowers and worn grass, not a
                // different green on each successive checkerboard diamond.
                int clusters=Math.floorMod((x/4)*41+(y/5)*17+(x/4)*(y/5)*3,13);
                if (clusters==3 || clusters==8) return 67+Math.floorMod(x*5+y*7,4);
                return Math.floorMod((x/5)*17+(y/5)*29,3);
            }
        }
    }
    public static int treeSprite(int x,int y) {
        int variant=Math.floorMod(x*13+y*29+(x/3)*7,9);
        return variant<3?24+variant:61+(variant-3);
    }
    public static int buildingSprite(TileType type,int level,int x,int y) {
        return BuildingAppearance.sprite(type,level,x,y);
    }
    public void dispose() {texture.dispose();}
}
