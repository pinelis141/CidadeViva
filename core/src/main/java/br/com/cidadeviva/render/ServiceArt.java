package br.com.cidadeviva.render;

import br.com.cidadeviva.sim.TileType;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Small original pixel icons generated once; same 72x128 anchor as the city atlas. */
public final class ServiceArt {
    private final Texture[] textures=new Texture[5];
    public ServiceArt() {
        for(int i=0;i<5;i++) {
            Pixmap p=pixels(i);
            textures[i]=new Texture(p);textures[i].setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);p.dispose();
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
        int i=type==TileType.PARK?0:type==TileType.SCHOOL?1:type==TileType.CLINIC?2:type==TileType.POWER_PLANT?3:4;
        batch.draw(textures[i],x-36,y-16,72,128);
    }
    public void dispose() {for(Texture t:textures)t.dispose();}
}
