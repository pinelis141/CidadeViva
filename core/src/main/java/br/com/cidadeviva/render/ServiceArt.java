package br.com.cidadeviva.render;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;

import br.com.cidadeviva.sim.TileType;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Small original pixel icons generated once; same 72x128 anchor as the city atlas. */
public final class ServiceArt {
    private final Pixmap[] retained=new Pixmap[11];
    private final Texture[] textures=new Texture[11];
    public ServiceArt() {
        for(int i=0;i<11;i++) {
            Pixmap p=pixels(i);
            retained[i]=p;textures[i]=new Texture(new PixmapTextureData(p,p.getFormat(),false,false,true));textures[i].setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
        }
    }
    public static Pixmap pixels(int i) {
            Pixmap p=new Pixmap(72,128,Pixmap.Format.RGBA8888);
            p.setColor(0,0,0,0);p.fill();
            p.setColor(0.32f,0.50f,0.32f,1);
            p.fillTriangle(4,111,36,95,68,111);p.fillTriangle(4,111,36,127,68,111);
            if(i==0) {
                p.setColor(0.3f,0.21f,0.12f,1);p.fillRectangle(22,82,6,30);
                p.setColor(0.12f,0.36f,0.23f,1);p.fillRectangle(10,67,30,24);
                p.setColor(0.25f,0.59f,0.30f,1);p.fillRectangle(15,60,22,20);
                p.setColor(0.65f,0.44f,0.25f,1);p.fillRectangle(40,101,17,5);p.fillRectangle(42,106,3,8);p.fillRectangle(52,106,3,8);
            } else if(i>=5) {
                p.setColor(.32f,.39f,.40f,1);p.fillRectangle(12,78,48,30);
                p.setColor(.20f,.29f,.32f,1);p.fillTriangle(9,78,36,64,63,78);p.fillTriangle(9,78,36,92,63,78);
                p.setColor(.53f,.75f,.78f,1);for(int wx=17;wx<58;wx+=13)p.fillRectangle(wx,92,7,7);
                if(i==5){p.setColor(.80f,.22f,.16f,1);p.fillRectangle(22,98,29,12);p.setColor(.98f,.78f,.28f,1);p.fillRectangle(32,70,8,18);p.fillRectangle(26,76,20,6);}
                if(i==6){p.setColor(.25f,.48f,.80f,1);p.fillRectangle(24,71,24,15);p.setColor(.94f,.82f,.42f,1);p.fillTriangle(29,72,43,72,36,84);}
                if(i==7){p.setColor(.29f,.64f,.28f,1);p.fillRectangle(18,98,13,15);p.fillRectangle(42,98,13,15);p.setColor(.63f,.79f,.41f,1);p.fillRectangle(16,96,17,4);p.fillRectangle(40,96,17,4);}
                if(i==8){p.setColor(.33f,.63f,.72f,1);p.fillRectangle(16,97,40,16);p.setColor(.69f,.88f,.91f,1);p.fillRectangle(19,100,34,6);}
                if(i==9){p.setColor(.94f,.69f,.25f,1);p.fillRectangle(18,96,38,13);p.setColor(.23f,.38f,.41f,1);p.fillRectangle(22,98,29,5);p.setColor(.12f,.19f,.20f,1);p.fillRectangle(22,107,6,5);p.fillRectangle(46,107,6,5);}
                if(i==10){p.setColor(.25f,.44f,.47f,1);p.fillRectangle(4,72,64,39);p.setColor(.69f,.76f,.70f,1);p.fillRectangle(19,91,3,25);p.fillRectangle(50,91,3,22);p.setColor(.87f,.73f,.35f,1);p.fillRectangle(15,84,42,8);p.setColor(.16f,.34f,.40f,1);p.fillRectangle(27,97,19,5);}
            } else if(i==3) {
                p.setColor(.34f,.38f,.41f,1);p.fillRectangle(13,82,46,23);
                p.setColor(.19f,.24f,.27f,1);p.fillTriangle(9,82,36,68,63,82);p.fillTriangle(9,82,36,96,63,82);
                p.setColor(.62f,.67f,.67f,1);p.fillRectangle(45,48,8,36);
                p.setColor(.98f,.83f,.27f,1);p.fillTriangle(31,81,42,81,30,95);p.fillTriangle(30,90,39,90,26,104);
            } else if(i==4) {
                p.setColor(.38f,.45f,.49f,1);p.fillRectangle(21,85,5,28);p.fillRectangle(48,85,5,24);
                p.setColor(.25f,.58f,.76f,1);p.fillRectangle(16,62,41,26);
                p.setColor(.50f,.77f,.88f,1);p.fillTriangle(13,63,36,51,60,63);p.fillTriangle(13,63,36,75,60,63);
                p.setColor(.75f,.92f,.98f,1);p.fillRectangle(31,73,8,11);
            } else {
                p.setColor(i==1?0.78f:0.83f,i==1?0.62f:0.87f,i==1?0.40f:0.85f,1);
                p.fillTriangle(12,80,36,92,36,112);p.fillTriangle(12,80,36,112,12,100);
                p.setColor(i==1?0.53f:0.57f,i==1?0.40f:0.67f,i==1?0.28f:0.68f,1);
                p.fillTriangle(36,92,60,80,60,100);p.fillTriangle(36,92,60,100,36,112);
                p.setColor(i==1?0.40f:0.86f,i==1?0.27f:0.90f,i==1?0.22f:0.88f,1);
                p.fillTriangle(10,80,36,66,62,80);p.fillTriangle(10,80,36,94,62,80);
                p.setColor(0.13f,0.25f,0.32f,1);p.fillRectangle(29,96,6,13);
                p.setColor(0.45f,0.76f,0.82f,1);p.fillRectangle(17,88,6,7);p.fillRectangle(48,88,6,7);
                if(i==2){p.setColor(0.89f,0.26f,0.25f,1);p.fillRectangle(33,70,6,18);p.fillRectangle(27,76,18,6);}
                else {p.setColor(0.98f,0.88f,0.60f,1);p.fillRectangle(28,74,17,10);p.setColor(0.4f,0.27f,0.18f,1);p.fillRectangle(35,74,2,10);}
            }
        return p;
    }
    public void draw(SpriteBatch batch,TileType type,float x,float y) {
        int i=type.isCivic()?5+type.ordinal()-TileType.FIRE_STATION.ordinal():type==TileType.PARK?0:type==TileType.SCHOOL?1:type==TileType.CLINIC?2:type==TileType.POWER_PLANT?3:4;
        batch.draw(textures[i],x-36,y-16,72,128);
    }
    public void dispose() {for(Texture t:textures)t.dispose();for(Pixmap p:retained)p.dispose();}
}
