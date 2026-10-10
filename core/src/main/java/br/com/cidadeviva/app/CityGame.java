package br.com.cidadeviva.app;

import br.com.cidadeviva.sim.City;
import br.com.cidadeviva.sim.CityPersistence;
import br.com.cidadeviva.sim.Tile;
import br.com.cidadeviva.sim.TileType;
import br.com.cidadeviva.input.HudLayout;
import br.com.cidadeviva.input.IsoGrid;
import br.com.cidadeviva.storage.CitySaveStore;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
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

import java.util.HashMap;
import java.util.Map;

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
    private CitySaveStore saves;
    private TileType selected = TileType.ROAD;
    private int hoverX = -1, hoverY = -1;
    private float simClock = 0;
    private String toast = "Toque no mapa para construir. Arraste para mover.";
    private float toastClock = 5f;
    private float uiScale = 1f;
    private boolean simulationPaused = false;
    private int simulationSpeed = 1;
    private boolean helpVisible = false;
    private static final String[] ACTION_LABELS = {"PAUSA", "1X", "AJUDA", "SALVAR", "ABRIR"};
    private float w, h;
    private final boolean[] down = new boolean[2];
    private final float[] px = new float[2], py = new float[2];
    private boolean moving, startedOnUi;
    private float dragTravel;
    private boolean pinchActive;
    private float pinchDistance;
    private boolean suppressTap;
    private int pendingAction = -1, pendingTool = -1;
    private boolean secondPointerOnHud = false;

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
        saves = new CitySaveStore(Gdx.files.local(SAVE_PATH).file());
        camera = new OrthographicCamera();
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        glyph = new GlyphLayout();
        uiProjection = new Matrix4();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0, -(City.WIDTH/2f + City.HEIGHT/2f) * HALF_H, 0);
        camera.zoom = 0.95f;
        clampCamera();
        Gdx.input.setInputProcessor(new Controls());
        load(false); // Se houver progresso anterior, restaurar automaticamente.
    }

    @Override public void resize(int width,int height) {
        w = Math.max(1,width); h = Math.max(1,height);
        uiScale = HudLayout.scale(w,h);
        camera.setToOrtho(false, w, h);
        clampCamera();
        uiProjection.setToOrtho2D(0,0,w,h);
        font.getData().setScale(1.15f * uiScale);
    }

    @Override public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(),0.1f);
        toastClock -= delta;
        if (!simulationPaused) {
            simClock += delta * simulationSpeed;
            while (simClock >= 4f) {
                simClock -= 4f;
                city.nextDay();
                if (city.day() % 15 == 0) save(false);
            }
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
        float u = uiScale;
        float top = HudLayout.TOP_HEIGHT*u;
        float bottom = HudLayout.BOTTOM_HEIGHT*u;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(uiProjection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(rgb(24,34,41));
        shapes.rect(0,h-top,w,top);
        shapes.rect(0,0,w,bottom);
        shapes.setColor(rgb(70,153,112));
        shapes.rect(0,h-top,w,3*u);
        shapes.setColor(rgb(49,67,72));
        shapes.rect(0,bottom,w,2*u);

        float buttonWidth = w/HudLayout.TOOLS;
        for (int i=0;i<HudLayout.TOOLS;i++) {
            float bx=i*buttonWidth+4*u;
            float bw=buttonWidth-8*u;
            boolean active=selected==tools[i];
            shapes.setColor(active?rgb(235,215,144):rgb(53,69,78));
            shapes.rect(bx,14*u,bw,60*u);
            shapes.setColor(active?rgb(67,75,75):rgb(36,47,55));
            shapes.rect(bx+2*u,16*u,bw-4*u,56*u);
            shapes.setColor(toolColors[i]);
            shapes.rect(bx+bw/2f-9*u,43*u,18*u,16*u);
        }
        for (int i=0;i<HudLayout.ACTIONS;i++) {
            float bx=i*w/HudLayout.ACTIONS+3*u;
            float by=h-(HudLayout.ACTION_TOP+HudLayout.ACTION_HEIGHT)*u;
            float bw=w/HudLayout.ACTIONS-6*u;
            boolean active=(i==0 && simulationPaused);
            shapes.setColor(active?rgb(155,113,72):rgb(49,76,83));
            shapes.rect(bx,by,bw,(HudLayout.ACTION_HEIGHT-3)*u);
        }
        if (helpVisible) {
            shapes.setColor(0f,0f,0f,0.88f);
            shapes.rect(0,0,w,h);
            shapes.setColor(rgb(34,54,59));
            shapes.rect(15*u,h/2f-115*u,w-30*u,230*u);
        }
        shapes.end();
        batch.setProjectionMatrix(uiProjection);
        batch.begin();
        font.setColor(rgb(235,243,227));
        font.getData().setScale(1.03f*u);
        font.draw(batch,"CIDADE VIVA  0.1.1",12*u,h-17*u);
        font.getData().setScale(0.89f*u);
        font.draw(batch,"R$ "+city.money()+"  |  POP "+city.residents()+"  |  DIA "+city.day(),12*u,h-41*u);
        font.draw(batch,"EMP "+city.jobs()+" | CAIXA/DIA "+city.lastIncome()+(simulationPaused?" | PAUSADO":""),12*u,h-62*u);
        font.getData().setScale(0.90f*u);
        for (int i=0;i<HudLayout.ACTIONS;i++) {
            String text=(i==0 && simulationPaused)?"SEGUIR":i==1?(simulationSpeed+"X"):ACTION_LABELS[i];
            glyph.setText(font,text);
            float bw=w/HudLayout.ACTIONS;
            font.draw(batch,text,i*bw+(bw-glyph.width)/2f,h-99*u);
        }
        font.getData().setScale(0.91f*u);
        for (int i=0;i<HudLayout.TOOLS;i++) {
            glyph.setText(font,toolNames[i]);
            float bw=w/HudLayout.TOOLS;
            font.draw(batch,toolNames[i],i*bw+(bw-glyph.width)*0.5f,27*u);
        }
        if (toastClock>0 && !helpVisible) {
            font.getData().setScale(0.87f*u);
            font.setColor(rgb(252,244,191));
            font.draw(batch,toast,12*u,(HudLayout.BOTTOM_HEIGHT+20)*u);
        }
        if (helpVisible) {
            float x=29*u,y=h/2f+86*u;
            font.setColor(rgb(248,241,214));
            font.getData().setScale(1.02f*u);
            font.draw(batch,"COMO JOGAR",x,y);
            font.getData().setScale(0.93f*u);
            font.draw(batch,"Toque: construir no terreno",x,y-38*u);
            font.draw(batch,"Arraste: mover a camera",x,y-67*u);
            font.draw(batch,"Dois dedos: aproximar/afastar",x,y-96*u);
            font.draw(batch,"Use SALVAR para guardar a cidade",x,y-125*u);
            font.draw(batch,"Toque na tela para fechar",x,y-166*u);
        }
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void message(String text) { toast=text;toastClock=3f; }

    private void save(boolean feedback) {
        try {
            saves.save(city);
            if (feedback) message("Cidade salva no dispositivo!");
        } catch (Exception e) {
            if (feedback) message("Erro ao salvar: verifique espaco disponivel");
            Gdx.app.error("CidadeViva","Erro ao salvar",e);
        }
    }

    private void load(boolean feedback) {
        try {
            CitySaveStore.Loaded result = saves.load();
            if (result == null) {
                if (feedback) message("Ainda não ha cidade salva");
                return;
            }
            city = result.city;
            if (result.fromBackup) message("Backup recuperado! Salve novamente.");
            else if (feedback) message("Cidade carregada!");
        } catch (Exception e) {
            if (feedback) message("Falha ao abrir salvamento");
            Gdx.app.error("CidadeViva","Erro ao carregar",e);
        }
    }
    @Override public void pause() { save(false); }

    @Override public void dispose() {
        save(false);
        shapes.dispose();batch.dispose();font.dispose();
    }

    private void clampCamera() {
        if (camera == null) return;
        float minX = -(City.HEIGHT-1)*HALF_W-HALF_W-72;
        float maxX = (City.WIDTH-1)*HALF_W+HALF_W+72;
        float minY = -(City.WIDTH+City.HEIGHT-2)*HALF_H-HALF_H-72;
        float maxY = HALF_H+108;
        camera.position.x=IsoGrid.bound(camera.position.x,minX,maxX,w*camera.zoom);
        camera.position.y=IsoGrid.bound(camera.position.y,minY,maxY,h*camera.zoom);
        camera.update();
    }

    private void pan(float dx,float dy) {
        camera.position.add(-dx*camera.zoom,dy*camera.zoom,0);
        clampCamera();
    }

    /** Anchor the previously touched world point under the new finger midpoint. */
    private void zoomAround(float factor, float oldX,float oldY,float newX,float newY) {
        float oldZoom=camera.zoom;
        float newZoom=MathUtils.clamp(oldZoom*factor,0.48f,2.1f);
        float[] center=IsoGrid.anchoredCamera(camera.position.x,camera.position.y,
            oldZoom,newZoom,oldX,oldY,newX,newY,w,h);
        camera.zoom=newZoom;
        camera.position.set(center[0],center[1],0);
        clampCamera();
    }

    private void buildAt(int sx,int sy) {
        scratch.set(sx,sy,0);
        camera.unproject(scratch);
        int[] index=IsoGrid.pick(scratch.x,scratch.y,HALF_W,HALF_H);
        int x=index[0],y=index[1];
        if (!city.inBounds(x,y)) {message("Fora do mapa");return;}
        message(city.build(x,y,selected));
        hoverX=x;hoverY=y;
    }

    private void activateAction(int index) {
        switch(index) {
            case 0:
                simulationPaused=!simulationPaused;
                message(simulationPaused?"Simulacao pausada":"Simulacao retomada");
                break;
            case 1:
                simulationSpeed=simulationSpeed==1?2:simulationSpeed==2?4:1;
                message("Velocidade "+simulationSpeed+"x");
                break;
            case 2: helpVisible=true;break;
            case 3: save(true);break;
            case 4: load(true);break;
            default: break;
        }
    }

    private final class Controls extends InputAdapter {
        @Override public boolean touchDown(int screenX,int screenY,int pointer,int button) {
            if (pointer > 1) {
                suppressTap=true;
                return true;
            }
            down[pointer]=true;
            px[pointer]=screenX;py[pointer]=screenY;
            if (pointer==0) {
                moving=false;
                dragTravel=0;
                suppressTap=false;
                pinchActive=false;
                pendingAction=-1;
                pendingTool=-1;
                startedOnUi=helpVisible || HudLayout.isHud(screenX,screenY,w,h,uiScale);
                if (!helpVisible) {
                    pendingAction=HudLayout.actionAt(screenX,screenY,w,uiScale);
                    pendingTool=HudLayout.toolAt(screenX,screenY,w,h,uiScale);
                }
            }
            if (pointer==1) {
                secondPointerOnHud=HudLayout.isHud(screenX,screenY,w,h,uiScale);
            }
            if (down[0] && down[1]) {
                suppressTap=true;
                if (!startedOnUi && !secondPointerOnHud && !helpVisible) {
                    pinchActive=true;
                    moving=true;
                    pinchDistance=Math.max(1f,(float)Math.hypot(px[0]-px[1],py[0]-py[1]));
                }
            }
            return true;
        }
        @Override public boolean touchDragged(int screenX,int screenY,int pointer) {
            if (pointer>1 || !down[pointer])return false;
            float prevX=px[pointer], prevY=py[pointer];
            float oldMidX=(px[0]+px[1])*0.5f,oldMidY=(py[0]+py[1])*0.5f;
            px[pointer]=screenX;py[pointer]=screenY;
            if (startedOnUi || helpVisible) {
                dragTravel += Math.abs(screenX-prevX)+Math.abs(screenY-prevY);
                if (dragTravel>Math.max(10f,12f*uiScale)) moving=true;
                return true;
            }
            if (pinchActive && down[0] && down[1]) {
                float newMidX=(px[0]+px[1])*0.5f,newMidY=(py[0]+py[1])*0.5f;
                float d=Math.max(1f,(float)Math.hypot(px[0]-px[1],py[0]-py[1]));
                zoomAround(pinchDistance/d,oldMidX,oldMidY,newMidX,newMidY);
                pinchDistance=d;
                moving=true;
                return true;
            }
            float dx=screenX-prevX,dy=screenY-prevY;
            dragTravel += Math.abs(dx)+Math.abs(dy);
            if (dragTravel>Math.max(10f,12f*uiScale)) moving=true;
            if (moving) pan(dx,dy);
            return true;
        }
        @Override public boolean touchUp(int screenX,int screenY,int pointer,int button) {
            if(pointer>1)return true;
            if(!down[pointer])return true;
            down[pointer]=false;
            if(pointer==0) {
                if(helpVisible && startedOnUi) {
                    helpVisible=false;
                } else if (startedOnUi && !suppressTap && !moving) {
                    int action=HudLayout.actionAt(screenX,screenY,w,uiScale);
                    int tool=HudLayout.toolAt(screenX,screenY,w,h,uiScale);
                    if (action>=0 && action==pendingAction) activateAction(action);
                    else if (tool>=0 && tool==pendingTool) {
                        selected=tools[tool];
                        hoverX=-1;hoverY=-1;
                        message("Ferramenta: "+toolNames[tool]+" (R$ "+city.cost(selected)+")");
                    }
                } else if (!moving && !startedOnUi && !suppressTap &&
                           !HudLayout.isHud(screenX,screenY,w,h,uiScale)) {
                    buildAt(screenX,screenY);
                }
                pendingAction=-1;pendingTool=-1;
                startedOnUi=false;
            }
            if (pointer==1) secondPointerOnHud=false;
            if (!down[0] || !down[1]) pinchActive=false;
            if(!down[0] && !down[1])suppressTap=false;
            return true;
        }
        @Override public boolean scrolled(float amountX,float amountY) {
            float sx=Gdx.input.getX(),sy=Gdx.input.getY();
            zoomAround(1f+amountY*0.12f,sx,sy,sx,sy);
            return true;
        }
        @Override public boolean keyDown(int keycode) {
            if(keycode==Input.Keys.S)save(true);
            if(keycode==Input.Keys.L)load(true);
            if(keycode==Input.Keys.SPACE)activateAction(0);
            if(keycode==Input.Keys.EQUALS)activateAction(1);
            if(keycode>=Input.Keys.NUM_1 && keycode<=Input.Keys.NUM_5)
                selected=tools[keycode-Input.Keys.NUM_1];
            return true;
        }
    }
}
