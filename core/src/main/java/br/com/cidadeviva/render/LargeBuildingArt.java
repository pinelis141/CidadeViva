package br.com.cidadeviva.render;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;

import br.com.cidadeviva.sim.TileType;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Nine original isometric 2x2 buildings. Pixel art generated once, not scaled 1x1 sprites. */
public final class LargeBuildingArt {
    private final Pixmap[] retained=new Pixmap[9];
    private final Texture[] textures=new Texture[9];
    public LargeBuildingArt(){for(int i=0;i<9;i++){Pixmap p=pixels(i);retained[i]=p;textures[i]=new Texture(new PixmapTextureData(p,p.getFormat(),false,false,true));textures[i].setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);}}
    public static Pixmap pixels(int index){
        int kind=index/3,level=index%3+1,z=42+level*20;
        Pixmap p=new Pixmap(144,208,Pixmap.Format.RGBA8888);p.setColor(0,0,0,0);p.fill();
        p.setColor(.25f,.39f,.29f,1);quad(p,8,176,72,144,136,176,72,208);
        p.setColor(.44f,.48f,.45f,1);quad(p,12,176,72,146,132,176,72,206);
        // Two distinct faces, windows following the isometric facade slopes.
        p.setColor(kind==0?.77f:kind==1?.46f:.62f,kind==0?.72f:kind==1?.66f:.52f,kind==0?.57f:kind==1?.72f:.43f,1);
        quad(p,16,172,72,200,72,200-z,16,172-z);
        p.setColor(kind==0?.55f:kind==1?.27f:.42f,kind==0?.53f:kind==1?.46f:.36f,kind==0?.43f:kind==1?.53f:.30f,1);
        quad(p,72,200,128,172,128,172-z,72,200-z);
        for(int row=0;row<level+1;row++)for(int col=0;col<4;col++){
            int x=23+col*12,leftBase=172+(x-16)/2,rightX=79+col*12,rightBase=200-(rightX-72)/2;
            p.setColor(.13f,.28f,.34f,1);p.fillRectangle(x,leftBase-row*17-13,7,10);p.fillRectangle(rightX,rightBase-row*17-13,7,10);
            p.setColor(.58f,.81f,.82f,1);p.fillRectangle(x+1,leftBase-row*17-12,5,3);p.fillRectangle(rightX+1,rightBase-row*17-12,5,3);
        }
        p.setColor(kind==0?.49f:kind==1?.28f:.42f,kind==0?.28f:kind==1?.48f:.43f,kind==0?.22f:kind==1?.59f:.40f,1);
        quad(p,14,172-z,72,143-z,130,172-z,72,201-z);
        p.setColor(.72f,.75f,.68f,1);quad(p,26,170-z,72,147-z,118,170-z,72,193-z);
        if(kind==2){p.setColor(.31f,.34f,.35f,1);p.fillRectangle(91,115-z,12,45);p.setColor(.54f,.56f,.55f,1);p.fillRectangle(89,113-z,16,6);}
        else if(kind==1){p.setColor(.79f,.90f,.87f,1);p.fillRectangle(48,158-z,30,9);p.setColor(.26f,.48f,.58f,1);p.fillRectangle(51,160-z,24,5);}
        else{p.setColor(.30f,.46f,.26f,1);p.fillRectangle(22,177,11,7);p.fillRectangle(112,177,10,7);}
        return p;
    }
    private static void quad(Pixmap p,int ax,int ay,int bx,int by,int cx,int cy,int dx,int dy){p.fillTriangle(ax,ay,bx,by,cx,cy);p.fillTriangle(ax,ay,cx,cy,dx,dy);}
    public void draw(SpriteBatch batch,TileType type,int level,float x,float y){int kind=type==TileType.RESIDENTIAL?0:type==TileType.COMMERCIAL?1:2;batch.draw(textures[kind*3+Math.max(0,Math.min(2,level-1))],x-72,y-16,144,208);}
    public void dispose(){for(Texture t:textures)t.dispose();for(Pixmap p:retained)p.dispose();}
}
