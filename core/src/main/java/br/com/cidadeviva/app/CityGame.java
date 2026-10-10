package br.com.cidadeviva.app;

import br.com.cidadeviva.sim.City;
import br.com.cidadeviva.sim.CityPersistence;
import br.com.cidadeviva.sim.Tile;
import br.com.cidadeviva.sim.TileType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.io.OutputStream;

/** Entrada libGDX: jogo 2D isométrico, controles Android e HUD sem assets externos. */
public final class CityGame extends ApplicationAdapter {
    private static final int HALF_W = 32;
    private static final int HALF_H = 16;
    private static final String SAVE_PATH = "cidade-viva/salvamento.cvt";
    private final TileType[] tools = {
        TileType.ROAD, TileType.RESIDENTIAL, TileType.COMMERCIAL,
        TileType.INDUSTRIAL, TileType.GRASS
    };
    private final String[] toolNames = { "RUA", "CASAS", "LOJAS", "IND.", "APAGAR" };
    private final Color[] toolColors = {
        rgb(64, 76, 88), rgb(83, 174, 108), rgb(89, 164, 218),
        rgb(227, 165, 82), rgb(210, 84, 84)
    };

    private City city;
    private OrthographicCamera camera;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout glyph;
    private Matrix4 uiProjection;
    private final Vector3 scratch = new Vector3();
    private TileType selected = TileType.ROAD;
    private int hoverX = -1, hoverY = -1;
    private float simClock = 0;
    private String toast = "Toque no mapa para construir. Arraste para mover.";
    private float toastClock = 5f;
    private float uiScale = 1f;
    private float w, h;
    private final boolean[] down = new boolean[2];
    private final float[] px = new float[2], py = new float[2];
    private boolean moving, startedOnUi;
    private float dragTravel;
    private boolean pinchActive;
    private float pinchDistance;

    private static final Map<Integer,Color> PALETTE = new HashMap<>();
    private static Color rgb(int r,int g,int b) {
        int key = (r<<16) | (g<<8) | b;
        Color found = PALETTE.get(key);
        if (found == null) {
            found = new Color(r/255f,g/255f,b/255f,1f);
            PALETTE.put(key,found);
        }
        return found;
    }

    @Override public void create() {
        city = new City();
        camera = new OrthographicCamera();
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        glyph = new GlyphLayout();
        uiProjection = new Matrix4();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0, -(City.WIDTH/2f + City.HEIGHT/2f) * HALF_H, 0);
        camera.zoom = 0.95f;
        camera.update();
        Gdx.input.setInputProcessor(new Controls());
        load(false); // Se houver progresso anterior, restaurar automaticamente.
    }

    @Override public void resize(int width,int height) {
        w = Math.max(1,width); h = Math.max(1,height);
        uiScale = MathUtils.clamp(Math.min(w / 730f, h / 970f), 0.65f, 1.65f);
        camera.setToOrtho(false, w, h);
        camera.update();
        uiProjection.setToOrtho2D(0,0,w,h);
        font.getData().setScale(1.15f * uiScale);
    }

    @Override public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(),0.1f);
        simClock += delta;
        toastClock -= delta;
        if (simClock >= 4f) {
            simClock -= 4f;
            city.nextDay();
            if (city.day() % 15 == 0) save(false);
        }
        Gdx.gl.glClearColor(0.085f,0.135f,0.155f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawMap();
        shapes.end();
        drawHud();
    }

    private void drawMap() {
        // Ordenação diagonal garante que edifícios frontais se sobreponham aos traseiros.
        for (int sum=0;sum<=City.WIDTH + City.HEIGHT - 2;sum++) {
            for (int x=Math.max(0, sum-City.HEIGHT+1);x<=Math.min(City.WIDTH-1,sum);x++) {
                int y=sum-x;
                Tile t = city.tile(x,y);
                float sx=(x-y)*HALF_W, sy=-(x+y)*HALF_H;
                if (sx+HALF_W < camera.position.x - w*camera.zoom/2f - 60 ||
                    sx-HALF_W > camera.position.x + w*camera.zoom/2f + 60 ||
                    sy < camera.position.y-h*camera.zoom/2f-90 ||
                    sy > camera.position.y+h*camera.zoom/2f+60) continue;
                drawTile(sx,sy,t,x,y);
                if (t.type == TileType.FOREST) drawTree(sx,sy,x,y);
                if (t.type.isZone() && t.level > 0) drawBuilding(sx,sy,t.type,t.level);
            }
        }
    }

    private void drawTile(float sx,float sy,Tile t,int x,int y) {
        switch (t.type) {
            case WATER: shapes.setColor(((x+y)&1)==0 ? rgb(52,127,177):rgb(60,140,187));break;
            case FOREST: shapes.setColor(rgb(70,132,70));break;
            case ROAD: shapes.setColor(rgb(79,85,91));break;
            case RESIDENTIAL: shapes.setColor(rgb(101,162,99));break;
            case COMMERCIAL: shapes.setColor(rgb(101,155,187));break;
            case INDUSTRIAL: shapes.setColor(rgb(177,149,90));break;
            default: shapes.setColor(((x*3+y)&1)==0 ? rgb(112,160,95):rgb(118,170,103));
        }
        diamond(sx,sy,HALF_W-0.6f,HALF_H-0.4f);
        if (t.type == TileType.ROAD) {
            shapes.setColor(rgb(221,190,106));
            // Marca viária sutil; pavimentação permanece alinhada à malha isométrica.
            thinDiamond(sx,sy,13,6);
        }
        if (t.type.isZone() && t.level == 0) {
            shapes.setColor(rgb(170,198,164));
            thinDiamond(sx,sy,20,10);
        }
        if (x==hoverX && y==hoverY) {
            shapes.setColor(rgb(250,230,133));
            thinDiamond(sx,sy,30,15);
        }
    }

    private void diamond(float sx,float sy,float hw,float hh) {
        shapes.triangle(sx,sy+hh,sx+hw,sy,sx,sy-hh);
        shapes.triangle(sx,sy+hh,sx,sy-hh,sx-hw,sy);
    }
    private void thinDiamond(float sx,float sy,float hw,float hh) {
        float k=2;
        shapes.triangle(sx,sy+hh,sx+k,sy+hh-k,sx+hw,sy);
        shapes.triangle(sx+hw,sy,sx+hw-k,sy,sx,sy-hh);
        shapes.triangle(sx,sy-hh,sx-k,sy-hh+k,sx-hw,sy);
        shapes.triangle(sx-hw,sy,sx-hw+k,sy,sx,sy+hh);
    }

    private void drawTree(float sx,float sy,int x,int y) {
        float size=5f+Math.floorMod(x*17+y*9,5);
        shapes.setColor(rgb(91,74,55));
        shapes.rect(sx-2,sy+1,4,size+5);
        shapes.setColor(rgb(42,97,63));
        diamond(sx,sy+size+11,size+6,size+5);
        shapes.setColor(rgb(64,126,71));
        diamond(sx-2,sy+size+15,size+3,size+5);
    }

    private void quad(float x1,float y1,float x2,float y2,float x3,float y3,float x4,float y4) {
        shapes.triangle(x1,y1,x2,y2,x3,y3);
        shapes.triangle(x1,y1,x3,y3,x4,y4);
    }

    /** Prismas isométricos coloridos funcionam como sprites provisórios, sem assets protegidos. */
    private void drawBuilding(float sx,float sy,TileType type,int level) {
        float hw=17, hh=9, z=17+level*13;
        Color roof, left, right;
        if (type == TileType.RESIDENTIAL) {
            roof=rgb(176,87,81);left=rgb(231,218,171);right=rgb(188,174,137);
        } else if (type == TileType.COMMERCIAL) {
            roof=rgb(104,191,210);left=rgb(187,216,223);right=rgb(107,155,177);
        } else {
            roof=rgb(124,127,138);left=rgb(185,150,108);right=rgb(146,113,82);
        }
        // Face visível à esquerda: canto esquerdo e canto de frente (inferior).
        shapes.setColor(left);
        quad(sx-hw,sy+z,sx,sy-hh+z,sx,sy-hh,sx-hw,sy);
        shapes.setColor(right);
        quad(sx,sy-hh+z,sx+hw,sy+z,sx+hw,sy,sx,sy-hh);
        shapes.setColor(roof);
        diamond(sx,sy+z,hw,hh);
        // Janelas em blocos coloridos nas fachadas inferiores.
        shapes.setColor(rgb(72,108,131));
        for (int floor=0;floor<level;floor++) {
            float zz=7+floor*12;
            shapes.rect(sx-12,sy+zz,5,4);
            shapes.rect(sx+6,sy+zz,5,4);
        }
    }

    private void drawHud() {
        shapes.setProjectionMatrix(uiProjection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(rgb(24,34,41));
        shapes.rect(0,h-79*uiScale,w,79*uiScale);
        shapes.rect(0,0,w,91*uiScale);
        shapes.setColor(rgb(70,153,112));
        shapes.rect(0,h-79*uiScale,w,3*uiScale);
        shapes.setColor(rgb(49,67,72));
        shapes.rect(0,91*uiScale,w,2*uiScale);
        float buttonWidth=(w-20*uiScale)/5f;
        for (int i=0;i<5;i++) {
            float bx=10*uiScale+i*buttonWidth+3*uiScale;
            float by=17*uiScale;
            float bw=buttonWidth-6*uiScale;
            boolean active=selected==tools[i];
            shapes.setColor(active ? rgb(235,215,144):rgb(53,69,78));
            shapes.rect(bx-(active?2:0),by-(active?2:0),bw+(active?4:0),57*uiScale+(active?4:0));
            shapes.setColor(active ? rgb(67,75,75):rgb(36,47,55));
            shapes.rect(bx+2*uiScale,by+2*uiScale,bw-4*uiScale,53*uiScale);
            shapes.setColor(toolColors[i]);
            shapes.rect(bx+bw/2f-9*uiScale,by+29*uiScale,18*uiScale,16*uiScale);
        }
        for (int i=0;i<2;i++) {
            float bx=w-(i==0?187:94)*uiScale;
            shapes.setColor(rgb(49,76,83));
            shapes.rect(bx,h-51*uiScale,87*uiScale,34*uiScale);
        }
        shapes.end();
        batch.setProjectionMatrix(uiProjection);
        batch.begin();
        font.setColor(rgb(235,243,227));
        // Evitar colisão entre estatísticas e controles de salvamento em telas menores.
        font.draw(batch,"CIDADE VIVA",12*uiScale,h-15*uiScale);
        font.getData().setScale(0.85f*uiScale);
        font.draw(batch,"R$ "+city.money()+"  |  POP "+city.residents()+"  |  DIA "+city.day(),12*uiScale,h-39*uiScale);
        font.draw(batch,"Empregos: "+city.jobs()+"  |  Caixa/dia: "+city.lastIncome(),12*uiScale,h-59*uiScale);
        font.getData().setScale(1.00f*uiScale);
        font.draw(batch,"SALVAR",w-179*uiScale,h-26*uiScale);
        font.draw(batch,"ABRIR",w-86*uiScale,h-26*uiScale);
        font.getData().setScale(0.91f*uiScale);
        for(int i=0;i<5;i++) {
            float bx=10*uiScale+i*buttonWidth+3*uiScale;
            float bw=buttonWidth-6*uiScale;
            glyph.setText(font,toolNames[i]);
            font.draw(batch,toolNames[i],bx+(bw-glyph.width)*0.5f,27*uiScale);
        }
        if (toastClock>0) {
            font.getData().setScale(0.9f*uiScale);
            font.setColor(rgb(252,244,191));
            font.draw(batch,toast,12*uiScale,109*uiScale);
        }
        batch.end();
    }

    private void message(String text) { toast=text;toastClock=3f; }
    private FileHandle saveFile() { return Gdx.files.local(SAVE_PATH); }
    private void save(boolean feedback) {
        try {
            FileHandle destination = saveFile();
            destination.parent().mkdirs();
            FileHandle staging=Gdx.files.local(SAVE_PATH+".tmp");
            try(OutputStream out=staging.write(false)) { CityPersistence.save(city,out); }
            // Não sobrescrever o arquivo válido antes que o temporário tenha sido concluído.
            staging.moveTo(destination);
            if (feedback) message("Cidade salva no dispositivo!");
        } catch(Exception e) {
            if(feedback) message("Falha ao salvar: "+e.getClass().getSimpleName());
            Gdx.app.error("CidadeViva","Erro ao salvar",e);
        }
    }
    private void load(boolean feedback) {
        try {
            if (!saveFile().exists()) {if(feedback) message("Ainda não há cidade salva");return;}
            try(InputStream in=saveFile().read()) { city=CityPersistence.load(in); }
            if(feedback) message("Cidade carregada!");
        } catch(Exception e) {
            if(feedback) message("Falha ao abrir salvamento");
            Gdx.app.error("CidadeViva","Erro ao carregar",e);
        }
    }
    @Override public void pause() { save(false); }
    @Override public void dispose() {
        save(false);
        shapes.dispose();batch.dispose();font.dispose();
    }

    private void pan(float dx,float dy) {
        camera.position.add(-dx*camera.zoom,dy*camera.zoom,0);
        camera.update();
    }
    private void changeZoom(float factor) {
        camera.zoom=MathUtils.clamp(camera.zoom*factor,0.48f,2.1f);
        camera.update();
    }
    private void buildAt(int sx,int sy) {
        scratch.set(sx,sy,0);
        camera.unproject(scratch);
        float a=scratch.x/HALF_W, b=-scratch.y/HALF_H;
        int x=MathUtils.floor((a+b)/2f+0.5f),y=MathUtils.floor((b-a)/2f+0.5f);
        if (!city.inBounds(x,y)) {message("Fora do mapa");return;}
        message(city.build(x,y,selected));
        hoverX=x;hoverY=y;
    }

    private final class Controls extends InputAdapter {
        @Override public boolean touchDown(int screenX,int screenY,int pointer,int button) {
            if(pointer > 1)return false;
            if(pointer==0) {
                moving=false;
                dragTravel=0;
                startedOnUi=false;
                float u=uiScale;
                if(screenY>h-91*u) {
                    startedOnUi=true;
                    int ix=Math.min(4,Math.max(0,(int)((screenX-10*u)/((w-20*u)/5f))));
                    selected=tools[ix];
                    hoverX=-1;hoverY=-1;
                    message("Ferramenta: "+toolNames[ix]+" (R$ "+city.cost(selected)+")");
                    return true;
                }
                if(screenY<79*u) {
                    startedOnUi=true;
                    if(screenX>w-187*u && screenX<w-94*u) save(true);
                    else if(screenX>=w-94*u) load(true);
                    return true;
                }
            }
            down[pointer]=true;
            px[pointer]=screenX;py[pointer]=screenY;
            if(down[0] && down[1]) {
                pinchActive=true;
                moving=true;
                pinchDistance=Math.max(1,(float)Math.sqrt((px[0]-px[1])*(px[0]-px[1])+(py[0]-py[1])*(py[0]-py[1])));
            }
            return true;
        }
        @Override public boolean touchDragged(int screenX,int screenY,int pointer) {
            if(pointer>1 || !down[pointer])return false;
            float dx=screenX-px[pointer],dy=screenY-py[pointer];
            px[pointer]=screenX;py[pointer]=screenY;
            if(down[0] && down[1]) {
                float d=Math.max(1,(float)Math.sqrt((px[0]-px[1])*(px[0]-px[1])+(py[0]-py[1])*(py[0]-py[1])));
                changeZoom(pinchDistance/d);
                pinchDistance=d;
                moving=true;
                return true;
            }
            dragTravel += Math.abs(dx)+Math.abs(dy);
            if(dragTravel>12*uiScale) moving=true;
            if(moving) pan(dx,dy);
            return true;
        }
        @Override public boolean touchUp(int screenX,int screenY,int pointer,int button) {
            if(pointer>1)return false;
            down[pointer]=false;
            if(pointer==0) {
                if(!moving && !startedOnUi && !pinchActive) buildAt(screenX,screenY);
                startedOnUi=false;
            }
            if(!down[0] && !down[1]) pinchActive=false;
            return true;
        }
        @Override public boolean scrolled(float amountX,float amountY) {
            changeZoom(1f+amountY*0.12f);
            return true;
        }
        @Override public boolean keyDown(int keycode) {
            if(keycode==Input.Keys.S)save(true);
            if(keycode==Input.Keys.L)load(true);
            if(keycode>=Input.Keys.NUM_1 && keycode<=Input.Keys.NUM_5) {
                selected=tools[keycode-Input.Keys.NUM_1];
            }
            return true;
        }
    }
}
