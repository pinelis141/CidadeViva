package br.com.cidadeviva.app;

import br.com.cidadeviva.sim.City;
import br.com.cidadeviva.sim.CityPersistence;
import br.com.cidadeviva.sim.CityEconomy;
import br.com.cidadeviva.sim.Tile;
import br.com.cidadeviva.sim.TileType;
import br.com.cidadeviva.input.HudLayout;
import br.com.cidadeviva.input.BuildStroke;
import br.com.cidadeviva.input.IsoGrid;
import br.com.cidadeviva.storage.CitySaveStore;
import br.com.cidadeviva.storage.CitySlots;
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
import br.com.cidadeviva.render.PixelArtAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
    private PixelArtAtlas pixelArt;
    private BitmapFont font;
    private GlyphLayout glyph;
    private Matrix4 uiProjection;
    private final Vector3 scratch = new Vector3();
    private CitySlots saves;
    private boolean savesReady = false;
    private boolean slotsVisible = false;
    private boolean economyVisible = false;
    private int selectedSlot = -1;
    private CitySlots.SlotInfo[] slotInfo = new CitySlots.SlotInfo[CitySlots.COUNT];
    private TileType selected = TileType.ROAD;
    private int hoverX = -1, hoverY = -1;
    private float simClock = 0;
    private String toast = "MODO MOVER: arraste para navegar. Toque para construir.";
    private float toastClock = 5f;
    private float uiScale = 1f;
    private boolean simulationPaused = false;
    private int simulationSpeed = 1;
    private boolean helpVisible = false;
    private static final String[] ACTION_LABELS = {"PAUSA", "1X", "MOVER", "AJUDA", "SALVAR", "ABRIR"};
    private boolean paintMode=false;
    private boolean brushGesture=false;
    private int startX,startY,lastStrokeX,lastStrokeY;
    private final LinkedHashSet<BuildStroke.Cell> brushCells=new LinkedHashSet<>();
    private float w, h;
    private final boolean[] down = new boolean[2];
    private final float[] px = new float[2], py = new float[2];
    private boolean moving, startedOnUi;
    private float dragTravel;
    private boolean pinchActive;
    private float pinchDistance;
    private boolean suppressTap;
    private int pendingAction = -1, pendingTool = -1;
    private boolean pendingRecenter = false;
    private int pendingQuickAction = -1;
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
        saves = new CitySlots(Gdx.files.local("cidade-viva").file());
        camera = new OrthographicCamera();
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        pixelArt = new PixelArtAtlas();
        font = new BitmapFont();
        glyph = new GlyphLayout();
        uiProjection = new Matrix4();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0, -(City.WIDTH/2f + City.HEIGHT/2f) * HALF_H, 0);
        camera.zoom = 0.95f;
        clampCamera();
        Gdx.input.setInputProcessor(new Controls());
        try {
            CitySlots.Opened opened=saves.openActive();
            city=opened.city;
            savesReady=true;
            if(opened.fromBackup)message("Cidade recuperada do backup!");
        } catch(Exception e) {
            savesReady=false;
            simulationPaused=true;
            openSlots();
            message("Salvamento com erro. Escolha outro espaco sem apagar o atual.");
            Gdx.app.error("CidadeViva","Erro ao abrir cidade ativa",e);
        }
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
        if (!simulationPaused && savesReady && !slotsVisible && !economyVisible) {
            simClock += delta * simulationSpeed;
            while (simClock >= 4f) {
                simClock -= 4f;
                city.nextDay();
                if (city.day() % 15 == 0) save(false);
            }
        }
        // Matt forest-green fog surrounds map bounds; avoids pure black corners.
        Gdx.gl.glClearColor(0.12f,0.19f,0.16f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        // Pixel atlas is rendered in isometric painter order, with a small overlay pass.
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawMapSprites();
        batch.end();
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawRoadAccessWarnings();
        drawBuildPreview();
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        drawHud();
    }

    /** One texture atlas, nearest-neighbor, no per-frame texture allocations. */
    private void drawMapSprites() {
        for (int sum=0;sum<=City.WIDTH+City.HEIGHT-2;sum++) {
            for (int x=Math.max(0,sum-City.HEIGHT+1);x<=Math.min(City.WIDTH-1,sum);x++) {
                int y=sum-x;
                float sx=(x-y)*HALF_W, sy=-(x+y)*HALF_H;
                if (sx+HALF_W < camera.position.x-w*camera.zoom/2f-76 ||
                    sx-HALF_W > camera.position.x+w*camera.zoom/2f+76 ||
                    sy < camera.position.y-h*camera.zoom/2f-138 ||
                    sy > camera.position.y+h*camera.zoom/2f+76) continue;
                Tile t=city.tile(x,y);
                int id=PixelArtAtlas.tileSprite(t.type,city.roadMask(x,y),city.waterMask(x,y),x,y);
                pixelArt.draw(batch,id,sx,sy);
                if (t.type==TileType.FOREST) pixelArt.draw(batch,PixelArtAtlas.treeSprite(x,y),sx,sy);
                if (t.type.isZone() && t.level>0)
                    pixelArt.draw(batch,PixelArtAtlas.buildingSprite(t.type,t.level,x,y),sx,sy);
            }
        }
    }

    /** Amber markers identify zoned lots that cannot yet grow without an adjacent road. */
    private void drawRoadAccessWarnings() {
        for (int x=0;x<City.WIDTH;x++) for(int y=0;y<City.HEIGHT;y++) {
            Tile t=city.tile(x,y);
            if (!t.type.isZone() || city.hasRoad(x,y)) continue;
            float sx=(x-y)*HALF_W, sy=-(x+y)*HALF_H;
            if (sx+HALF_W<camera.position.x-w*camera.zoom/2f-45 ||
                sx-HALF_W>camera.position.x+w*camera.zoom/2f+45 ||
                sy<camera.position.y-h*camera.zoom/2f-45 ||
                sy>camera.position.y+h*camera.zoom/2f+45) continue;
            shapes.setColor(rgb(251,177,86));
            thinDiamond(sx,sy,26,13);
            shapes.rect(sx-1,sy+2,2,6);
            shapes.rect(sx-1,sy-4,2,2);
        }
        if (hoverX>=0 && hoverY>=0) {
            float sx=(hoverX-hoverY)*HALF_W,sy=-(hoverX+hoverY)*HALF_H;
            shapes.setColor(rgb(255,225,124));
            thinDiamond(sx,sy,30,15);
        }
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
        if (t.type == TileType.ROAD) drawRoadConnections(sx,sy,city.roadMask(x,y));
        if (t.type.isZone() && t.level == 0) {
            shapes.setColor(rgb(170,198,164));
            thinDiamond(sx,sy,20,10);
        }
        if (x==hoverX && y==hoverY) {
            shapes.setColor(rgb(250,230,133));
            thinDiamond(sx,sy,30,15);
        }
    }

    /** A pavement marking is derived from the four actual road neighbors, never guessed. */
    private void drawRoadConnections(float sx,float sy,int mask) {
        if (mask==0) {
            shapes.setColor(rgb(206,195,144));
            thinDiamond(sx,sy,8,4);
            return;
        }
        shapes.setColor(rgb(209,191,132));
        if ((mask&1)!=0) roadDash(sx,sy,13f,-6.5f);
        if ((mask&2)!=0) roadDash(sx,sy,-13f,6.5f);
        if ((mask&4)!=0) roadDash(sx,sy,-13f,-6.5f);
        if ((mask&8)!=0) roadDash(sx,sy,13f,6.5f);
        shapes.setColor(rgb(138,146,150));
        shapes.rect(sx-1f,sy-1f,2f,2f);
    }

    private void roadDash(float sx,float sy,float dx,float dy) {
        shapes.rectLine(sx+dx*0.44f,sy+dy*0.44f,sx+dx*0.90f,sy+dy*0.90f,1.9f);
    }

    private void drawBuildPreview() {
        if (!paintMode || !brushGesture || brushCells.isEmpty()) return;
        for (BuildStroke.Cell cell:brushCells) {
            if (!city.inBounds(cell.x,cell.y)) continue;
            float sx=(cell.x-cell.y)*HALF_W, sy=-(cell.x+cell.y)*HALF_H;
            if (city.canBuild(cell.x,cell.y,selected))
                shapes.setColor(0.32f,0.94f,0.62f,0.50f);
            else shapes.setColor(1f,0.30f,0.30f,0.52f);
            diamond(sx,sy,HALF_W-1f,HALF_H-1f);
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
        float hw=17, hh=9, z=12+level*11;
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

    /** Compact, original high-contrast tool pictograms rendered on a fixed grid. */
    private void drawToolIcon(int type,float cx,float cy,float u) {
        float z=u;
        shapes.setColor(toolColors[type]);
        if (type==0) { // asphalt stripe
            shapes.rect(cx-15*z,cy-12*z,30*z,24*z);
            shapes.setColor(rgb(246,222,142));
            shapes.rect(cx-3*z,cy-1*z,6*z,2*z);
            shapes.rect(cx-3*z,cy+6*z,6*z,2*z);
        } else if (type==4) { // demolition crossed tools
            shapes.rectLine(cx-12*z,cy-11*z,cx+12*z,cy+11*z,4*z);
            shapes.rectLine(cx+12*z,cy-11*z,cx-12*z,cy+11*z,4*z);
        } else {
            Color tone=toolColors[type];
            shapes.setColor(tone);
            shapes.rect(cx-12*z,cy-11*z,24*z,19*z);
            if (type==1) shapes.triangle(cx-16*z,cy+8*z,cx+16*z,cy+8*z,cx,cy+19*z);
            if (type==2) { shapes.setColor(rgb(233,211,133));shapes.rect(cx-15*z,cy+5*z,30*z,5*z); }
            if (type==3) { shapes.rect(cx+7*z,cy+7*z,7*z,14*z); }
            shapes.setColor(rgb(26,53,68));
            shapes.rect(cx-6*z,cy-2*z,5*z,8*z);
            shapes.rect(cx+4*z,cy-2*z,5*z,8*z);
        }
    }

    private void drawHud() {
        float u = uiScale;
        CityEconomy economy = city.economy();
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
            shapes.rect(bx,15*u,bw,88*u);
            shapes.setColor(active?rgb(59,77,74):rgb(34,47,55));
            shapes.rect(bx+2*u,17*u,bw-4*u,84*u);
            drawToolIcon(i,bx+bw*0.5f,75*u,u);
        }
        // Three top shortcuts, all distinct from the simulation HUD.
        shapes.setColor(rgb(51,72,73));
        shapes.rect(w-357*u,h-48*u,109*u,40*u); // economy
        shapes.rect(w-244*u,h-48*u,124*u,40*u); // cities
        shapes.rect(w-117*u,h-48*u,112*u,40*u); // center
        for (int i=0;i<HudLayout.ACTIONS;i++) {
            float bx=i*w/HudLayout.ACTIONS+3*u;
            float by=h-(HudLayout.ACTION_TOP+HudLayout.ACTION_HEIGHT)*u;
            float bw=w/HudLayout.ACTIONS-6*u;
            boolean active=(i==0 && simulationPaused) || (i==2 && paintMode);
            shapes.setColor(active?rgb(155,113,72):rgb(49,76,83));
            shapes.rect(bx,by,bw,(HudLayout.ACTION_HEIGHT-3)*u);
        }
        // Demand meters: their bar length is the simulation's actual 0..100 score.
        float meterY=h-139*u, meterWidth=(w-36*u)/3f;
        int[] demand={economy.residentialDemand,economy.commercialDemand,economy.industrialDemand};
        Color[] demandColor={rgb(96,198,129),rgb(96,170,225),rgb(222,169,92)};
        for (int i=0;i<3;i++) {
            float bx=12*u+i*(meterWidth+6*u);
            shapes.setColor(rgb(52,67,67));
            shapes.rect(bx,meterY,meterWidth,6*u);
            shapes.setColor(demandColor[i]);
            shapes.rect(bx,meterY,meterWidth*demand[i]/100f,6*u);
        }
        if (helpVisible) {
            shapes.setColor(0f,0f,0f,0.88f);
            shapes.rect(0,0,w,h);
            shapes.setColor(rgb(34,54,59));
            shapes.rect(15*u,h/2f-115*u,w-30*u,230*u);
        }
        if (slotsVisible) drawSlotsOverlay(u);
        if (economyVisible) drawEconomyOverlay(u,economy);
        shapes.end();
        batch.setProjectionMatrix(uiProjection);
        batch.begin();
        font.setColor(rgb(235,243,227));
        font.getData().setScale(1.20f*u);
        font.draw(batch,"CIDADE VIVA  0.3.0a2",12*u,h-19*u);
        font.getData().setScale(1.10f*u);
        font.draw(batch,"R$ "+city.money()+"  |  POP "+city.residents()+"  |  DIA "+city.day(),12*u,h-43*u);
        font.draw(batch,"EMP "+economy.jobs+" | DESEMP "+economy.unemployed+
            " | VAGAS "+economy.jobVacancies+(simulationPaused?" | PAUSADO":""),12*u,h-66*u);
        font.getData().setScale(1.00f*u);
        font.draw(batch,"REC R$"+economy.grossRevenue+" - CUSTO R$"+economy.expenses+
            " = "+(economy.net>=0?"+":"")+economy.net+"/DIA",12*u,h-88*u);
        int unserved=city.unservedZones();
        font.setColor(unserved>0?rgb(253,190,114):rgb(152,220,168));
        font.getData().setScale(1.05f*u);
        font.draw(batch,unserved>0?"ZONAS SEM RUA: "+unserved+"  |  Toque no lote":"ZONAS COM ACESSO A RUA",12*u,h-108*u);
        font.setColor(rgb(227,235,218));
        font.getData().setScale(0.99f*u);
        font.draw(batch,"DEMANDA  R "+economy.residentialDemand+"   C "+economy.commercialDemand+
            "   I "+economy.industrialDemand,12*u,h-126*u);
        font.setColor(rgb(235,243,227));
        font.setColor(rgb(243,226,158));
        font.getData().setScale(0.90f*u);
        font.getData().setScale(1.00f*u);
        glyph.setText(font,"ECON");
        font.draw(batch,"ECON",w-302*u-glyph.width*0.5f,h-22*u);
        glyph.setText(font,"CIDADES");
        font.draw(batch,"CIDADES",w-182*u-glyph.width*0.5f,h-22*u);
        glyph.setText(font,"CENTRAR");
        font.draw(batch,"CENTRAR",w-61*u-glyph.width*0.5f,h-22*u);
        font.getData().setScale(1.12f*u);
        for (int i=0;i<HudLayout.ACTIONS;i++) {
            String text=(i==0 && simulationPaused)?"SEGUIR":i==1?(simulationSpeed+"X"):
                i==2?(paintMode?"PINTAR":"MOVER"):ACTION_LABELS[i];
            glyph.setText(font,text);
            float bw=w/HudLayout.ACTIONS;
            font.draw(batch,text,i*bw+(bw-glyph.width)/2f,
                h-(HudLayout.ACTION_TOP+HudLayout.ACTION_HEIGHT*0.75f)*u);
        }
        font.getData().setScale(1.22f*u);
        for (int i=0;i<HudLayout.TOOLS;i++) {
            glyph.setText(font,toolNames[i]);
            float bw=w/HudLayout.TOOLS;
            font.draw(batch,toolNames[i],i*bw+(bw-glyph.width)*0.5f,39*u);
        }
        if (!helpVisible && paintMode && brushGesture && !brushCells.isEmpty()) {
            City.BuildResult preview=city.preview(new ArrayList<>(brushCells),selected);
            font.getData().setScale(1.05f*u);
            font.setColor(preview.insufficientFunds?rgb(250,140,119):rgb(251,235,171));
            font.draw(batch,"AREA "+preview.built+" | R$ "+preview.cost+
                (preview.ignored>0?" | BLOQ "+preview.ignored:"") ,12*u,(HudLayout.BOTTOM_HEIGHT+20)*u);
        } else if (toastClock>0 && !helpVisible) {
            font.getData().setScale(1.00f*u);
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
            font.draw(batch,"MOVER: arraste a camera",x,y-67*u);
            font.draw(batch,"PINTAR: arraste ruas e areas",x,y-96*u);
            font.draw(batch,"Dois dedos: mover/zoom",x,y-125*u);
            font.draw(batch,"DEMANDA R/C/I: barra mostra procura",x,y-153*u);
            font.draw(batch,"Toque na tela para fechar",x,y-182*u);
        }
        if (slotsVisible) drawSlotsText(u);
        if (economyVisible) drawEconomyText(u,economy);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private float econW(float u) {return Math.min(w-24*u,560*u);}
    private float econX(float u) {return (w-econW(u))*0.5f;}
    private float econY(float u) {return (h-558*u)*0.5f;}
    private static final TileType[] TAX_TYPES = {
        TileType.RESIDENTIAL,TileType.COMMERCIAL,TileType.INDUSTRIAL
    };
    private static final String[] TAX_NAMES = {"RESIDENCIAL", "COMERCIAL", "INDUSTRIAL"};

    private void drawEconomyOverlay(float u, CityEconomy report) {
        float x=econX(u),y=econY(u),ww=econW(u);
        shapes.setColor(0.01f,0.03f,0.04f,0.89f);
        shapes.rect(0,0,w,h);
        shapes.setColor(rgb(26,46,53));
        shapes.rect(x,y,ww,558*u);
        shapes.setColor(rgb(112,173,134));
        shapes.rect(x,y+554*u,ww,4*u);
        for(int i=0;i<3;i++) {
            float cy=y+(356-i*83)*u;
            shapes.setColor(rgb(43,64,67));
            shapes.rect(x+14*u,cy,ww-28*u,73*u);
            shapes.setColor(rgb(74,104,102));
            shapes.rect(x+ww-139*u,cy+13*u,52*u,48*u);
            shapes.rect(x+ww-73*u,cy+13*u,52*u,48*u);
        }
        shapes.setColor(rgb(58,107,83));
        shapes.rect(x+16*u,y+13*u,ww-32*u,47*u);
    }
    private void drawEconomyText(float u,CityEconomy report) {
        float x=econX(u),y=econY(u),ww=econW(u);
        font.setColor(rgb(234,242,215));
        font.getData().setScale(1.22f*u);
        font.draw(batch,"ECONOMIA E IMPOSTOS",x+21*u,y+518*u);
        font.getData().setScale(0.90f*u);
        font.draw(batch,"Impostos altos aumentam caixa, mas reduzem demanda.",x+21*u,y+484*u);
        for(int i=0;i<3;i++) {
            float cy=y+(356-i*83)*u;
            TileType type=TAX_TYPES[i];
            int demand=report.demand(type),rate=city.taxRate(type);
            font.setColor(rgb(234,242,215));
            font.getData().setScale(0.95f*u);
            font.draw(batch,TAX_NAMES[i]+"  "+rate+"%",x+27*u,cy+51*u);
            font.setColor(rgb(188,218,199));
            font.getData().setScale(0.85f*u);
            font.draw(batch,"Demanda: "+demand+"/100  |  "+city.lowDemandDays(type)+" dias fracos",x+27*u,cy+24*u);
            font.setColor(rgb(244,238,196));
            font.getData().setScale(1.45f*u);
            font.draw(batch,"-",x+ww-117*u,cy+46*u);
            font.draw(batch,"+",x+ww-54*u,cy+46*u);
        }
        font.setColor(rgb(240,239,216));
        font.getData().setScale(0.92f*u);
        font.draw(batch,"RECEITA  R$ "+report.grossRevenue+" / dia",x+22*u,y+161*u);
        font.draw(batch,"Moradias "+report.taxResidential+"  Lojas "+report.taxCommercial+
            "  Industrias "+report.taxIndustrial,x+22*u,y+139*u);
        font.draw(batch,"DESPESAS  R$ "+report.expenses+" / dia",x+22*u,y+115*u);
        font.draw(batch,"Ruas "+report.roadMaintenance+"  Terrenos "+report.zoneMaintenance,x+22*u,y+93*u);
        font.setColor(report.net>=0?rgb(162,228,173):rgb(251,163,148));
        font.getData().setScale(1.01f*u);
        font.draw(batch,"SALDO  "+(report.net>=0?"+":"")+report.net+" / dia",x+22*u,y+73*u);
        font.setColor(rgb(235,243,223));
        font.getData().setScale(1.06f*u);
        font.draw(batch,"FECHAR",x+ww*0.5f-34*u,y+43*u);
    }
    private void economyTap(int sx,int sy) {
        float u=uiScale, x=econX(u),y=econY(u),ww=econW(u);
        float py=h-sy;
        if(sx<x || sx>x+ww || py<y || py>y+558*u) {economyVisible=false;return;}
        if(py<=y+61*u) {economyVisible=false;return;}
        for(int i=0;i<3;i++) {
            float rowY=y+(356-i*83)*u;
            if(py<rowY+13*u || py>rowY+61*u)continue;
            int change=0;
            if(sx>=x+ww-139*u && sx<=x+ww-87*u)change=-1;
            if(sx>=x+ww-73*u && sx<=x+ww-21*u)change=1;
            if(change!=0) {
                TileType kind=TAX_TYPES[i];
                int next=Math.max(5,Math.min(20,city.taxRate(kind)+change));
                city.setTaxRate(kind,next);
                save(false); // persist this city's choice immediately in CVT3
            }
            return;
        }
    }

    private float slotsW(float u) {return Math.min(w-24*u,540*u);}
    private float slotsX(float u) {return (w-slotsW(u))*0.5f;}
    private float slotsY(float u) {return h*0.5f-226*u;}
    private float slotRowY(int index,float u) {return slotsY(u)+(280-index*84)*u;}

    private void refreshSlotInfo() {
        for(int i=0;i<CitySlots.COUNT;i++)slotInfo[i]=saves.inspect(i+1);
    }
    private void openSlots() {
        economyVisible=false;
        if (!savesReady) simulationPaused=true;
        selectedSlot=saves.active();
        refreshSlotInfo();
        slotsVisible=true;
    }
    private void drawSlotsOverlay(float u) {
        shapes.setColor(0.02f,0.04f,0.04f,0.90f);
        shapes.rect(0,0,w,h);
        float xx=slotsX(u),yy=slotsY(u),ww=slotsW(u);
        shapes.setColor(rgb(28,48,53));
        shapes.rect(xx,yy,ww,452*u);
        shapes.setColor(rgb(123,173,145));
        shapes.rect(xx,yy+448*u,ww,4*u);
        for(int i=0;i<CitySlots.COUNT;i++) {
            float sy=slotRowY(i,u);
            boolean sel=selectedSlot==i+1;
            shapes.setColor(sel?rgb(208,191,124):rgb(62,88,92));
            shapes.rect(xx+12*u,sy,ww-24*u,72*u);
            shapes.setColor(sel?rgb(64,72,67):rgb(35,56,64));
            shapes.rect(xx+15*u,sy+3*u,ww-30*u,66*u);
        }
        shapes.setColor(rgb(47,108,80));
        shapes.rect(xx+12*u,yy+15*u,(ww-33*u)*0.62f,47*u);
        shapes.setColor(rgb(91,64,69));
        shapes.rect(xx+21*u+(ww-33*u)*0.62f,yy+15*u,(ww-33*u)*0.38f,47*u);
    }
    private void drawSlotsText(float u) {
        float xx=slotsX(u),yy=slotsY(u),ww=slotsW(u);
        font.setColor(rgb(239,244,216));
        font.getData().setScale(1.32f*u);
        font.draw(batch,"MINHAS CIDADES",xx+24*u,yy+414*u);
        font.getData().setScale(0.92f*u);
        font.draw(batch,"3 espacos independentes. Nenhum e apagado ao trocar.",xx+24*u,yy+380*u);
        for(int i=0;i<CitySlots.COUNT;i++) {
            CitySlots.SlotInfo info=slotInfo[i];
            if(info==null)continue;
            float sy=slotRowY(i,u);
            font.setColor(rgb(245,242,212));
            font.getData().setScale(1.16f*u);
            font.draw(batch,"CIDADE "+info.slot+(info.active?"  - EM USO":""),xx+27*u,sy+51*u);
            font.getData().setScale(0.92f*u);
            font.setColor(info.corrupt?rgb(245,135,121):rgb(207,224,205));
            String description=info.corrupt?"ARQUIVO COM ERRO - NAO SERA APAGADO":
                info.empty?"VAZIA - TOQUE PARA CRIAR":"DIA "+info.day+" | POP "+info.population+" | R$ "+info.money+
                (info.backupUsed?" | BACKUP":"");
            font.draw(batch,description,xx+27*u,sy+22*u);
        }
        font.setColor(rgb(245,244,217));
        font.getData().setScale(1.08f*u);
        String action="ABRIR / CRIAR";
        if(selectedSlot>=1 && selectedSlot<=CitySlots.COUNT && slotInfo[selectedSlot-1]!=null) {
            CitySlots.SlotInfo chosen=slotInfo[selectedSlot-1];
            if(chosen.active)action="CONTINUAR";
            else if(chosen.corrupt)action="PROTEGIDA";
            else if(chosen.empty)action="CRIAR NOVA";
            else action="ABRIR CIDADE";
        }
        font.draw(batch,action,xx+30*u,yy+44*u);
        font.draw(batch,"VOLTAR",xx+ww-125*u,yy+44*u);
        font.getData().setScale(0.88f*u);
        font.setColor(rgb(190,217,184));
        font.draw(batch,"O espaco 1 preserva o salvamento original.",xx+22*u,yy+77*u);
    }
    private void slotsTap(int screenX,int screenY) {
        float u=uiScale,xx=slotsX(u),yy=slotsY(u),ww=slotsW(u);
        float x=screenX,y=h-screenY;
        if(x<xx || x>xx+ww || y<yy || y>yy+452*u) return;
        for(int i=0;i<CitySlots.COUNT;i++) {
            float rowY=slotRowY(i,u);
            if(y>=rowY && y<=rowY+72*u) {
                selectedSlot=i+1;
                return;
            }
        }
        if(y>=yy+15*u && y<=yy+62*u) {
            if(x>xx+20*u+(ww-33*u)*0.62f) {
                if(savesReady)slotsVisible=false;
                return;
            }
            if(selectedSlot<1)return;
            if(selectedSlot==saves.active()) {
                if(savesReady) slotsVisible=false;
                return;
            }
            if(slotInfo[selectedSlot-1].corrupt) {
                message("Cidade protegida: save danificado. Use outro espaco.");
                return;
            }
            try {
                CitySlots.Opened opened=saves.switchTo(selectedSlot,city,savesReady);
                city=opened.city;
                savesReady=true;
                slotsVisible=false;
                economyVisible=false;
                simulationPaused=false;
                simClock=0;
                recenter();
                message(opened.created?"Nova cidade criada no espaco "+selectedSlot:
                    "Cidade "+selectedSlot+" carregada"+(opened.fromBackup?" (backup)":""));
            } catch(Exception e) {
                message("Erro ao trocar cidade: dados preservados");
                Gdx.app.error("CidadeViva","Falha ao trocar slot",e);
                refreshSlotInfo();
            }
        }
    }

    private void message(String text) { toast=text;toastClock=3f; }

    private void save(boolean feedback) {
        try {
            if(!savesReady)throw new IllegalStateException("Cidade não carregada com segurança");
            saves.save(city);
            if (feedback) message("Cidade salva no dispositivo!");
        } catch (Exception e) {
            if (feedback) message("Erro ao salvar: verifique espaco disponivel");
            Gdx.app.error("CidadeViva","Erro ao salvar",e);
        }
    }

    private void load(boolean feedback) {
        try {
            CitySaveStore.Loaded result = saves.store(saves.active()).load();
            if (result == null) {
                if (feedback) message("Ainda não ha cidade salva");
                return;
            }
            city = result.city;
            savesReady=true;
            if (result.fromBackup) message("Backup recuperado! Salve novamente.");
            else if (feedback) message("Cidade carregada!");
        } catch (Exception e) {
            if (feedback) message("Falha ao abrir salvamento");
            Gdx.app.error("CidadeViva","Erro ao carregar",e);
        }
    }
    @Override public void pause() { if(savesReady)save(false); }

    @Override public void dispose() {
        if(savesReady)save(false);
        shapes.dispose();batch.dispose();font.dispose();pixelArt.dispose();
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

    private void recenter() {
        camera.position.set(0,-(City.WIDTH/2f+City.HEIGHT/2f)*HALF_H,0);
        clampCamera();
        message("Camera centralizada");
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
        // Tapping an already-zoned lot inspects access rather than saying merely "already built".
        Tile existing=city.tile(x,y);
        if (existing.type.isZone() && selected!=TileType.GRASS &&
            !city.canBuild(x,y,selected)) {
            message(city.zoneDescription(x,y));
        } else {
            String result=city.build(x,y,selected);
            if (selected.isZone() && !city.hasRoad(x,y) && city.tile(x,y).type==selected)
                result="Zona criada. SEM RUA ADJACENTE!";
            message(result);
        }
        hoverX=x;hoverY=y;
    }

    /** The touch and drag selection share the same screen-to-isometric-grid mapping. */
    private int[] cellAt(int screenX,int screenY) {
        scratch.set(screenX,screenY,0);
        camera.unproject(scratch);
        return IsoGrid.pick(scratch.x,scratch.y,HALF_W,HALF_H);
    }

    private void startStroke(int screenX,int screenY) {
        brushCells.clear();
        int[] cell=cellAt(screenX,screenY);
        startX=lastStrokeX=cell[0];
        startY=lastStrokeY=cell[1];
        if (city.inBounds(startX,startY)) brushCells.add(new BuildStroke.Cell(startX,startY));
        brushGesture=false;
    }

    private void strokeTo(int screenX,int screenY) {
        int[] cell=cellAt(screenX,screenY);
        if (!city.inBounds(cell[0],cell[1])) return;
        if (selected==TileType.ROAD) {
            // Union of consecutive segments avoids missing cells at low frame rates.
            brushCells.addAll(BuildStroke.line(lastStrokeX,lastStrokeY,cell[0],cell[1]));
            lastStrokeX=cell[0]; lastStrokeY=cell[1];
        } else {
            brushCells.clear();
            brushCells.addAll(BuildStroke.rectangle(startX,startY,cell[0],cell[1]));
        }
    }

    private void finishStroke(int screenX,int screenY) {
        strokeTo(screenX,screenY);
        City.BuildResult result=city.buildArea(new ArrayList<>(brushCells),selected);
        String response=result.feedback();
        if (selected.isZone() && !result.insufficientFunds && result.built>0 &&
            city.unservedZones()>0) response+=" | SEM RUA: "+city.unservedZones();
        message(response);
        brushCells.clear();
        brushGesture=false;
        hoverX=-1;hoverY=-1;
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
            case 2:
                paintMode=!paintMode;
                brushCells.clear();
                message(paintMode?"PINTAR: arraste para construir. Dois dedos movem camera.":
                    "MOVER: arraste a camera. Toque para construir.");
                break;
            case 3: helpVisible=true;break;
            case 4: save(true);break;
            case 5: load(true);break;
            default: break;
        }
    }

    private final class Controls extends InputAdapter {
        @Override public boolean touchDown(int screenX,int screenY,int pointer,int button) {
            if (slotsVisible || economyVisible) {
                if(pointer==0) { down[0]=true;px[0]=screenX;py[0]=screenY; }
                return true;
            }
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
                pendingQuickAction=!helpVisible && screenY<=55*uiScale
                    ? HudLayout.quickActionAt(screenX,screenY,w,uiScale) : -1;
                brushCells.clear();
                brushGesture=false;
                if (!startedOnUi && paintMode) startStroke(screenX,screenY);
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
                brushCells.clear();
                brushGesture=false;
                if (!startedOnUi && !secondPointerOnHud && !helpVisible) {
                    pinchActive=true;
                    moving=true;
                    pinchDistance=Math.max(1f,(float)Math.hypot(px[0]-px[1],py[0]-py[1]));
                }
            }
            return true;
        }
        @Override public boolean touchDragged(int screenX,int screenY,int pointer) {
            if (slotsVisible || economyVisible)return true;
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
            if (paintMode && pointer==0 && !down[1] && !suppressTap) {
                if (moving) {
                    brushGesture=true;
                    strokeTo(screenX,screenY);
                }
                return true;
            }
            if (moving) pan(dx,dy);
            return true;
        }
        @Override public boolean touchUp(int screenX,int screenY,int pointer,int button) {
            if(slotsVisible || economyVisible) {
                if(pointer==0 && down[0]) {
                    down[0]=false;
                    if(Math.abs(screenX-px[0])+Math.abs(screenY-py[0])<18*uiScale) {
                        if(slotsVisible)slotsTap(screenX,screenY);
                        else economyTap(screenX,screenY);
                    }
                }
                return true;
            }
            if(pointer>1)return true;
            if(!down[pointer])return true;
            down[pointer]=false;
            if(pointer==0) {
                if(helpVisible && startedOnUi) {
                    helpVisible=false;
                } else if (startedOnUi && !suppressTap && !moving) {
                    int action=HudLayout.actionAt(screenX,screenY,w,uiScale);
                    int tool=HudLayout.toolAt(screenX,screenY,w,h,uiScale);
                    int quick=HudLayout.quickActionAt(screenX,screenY,w,uiScale);
                    if(quick>=0 && quick==pendingQuickAction) {
                        if(quick==0) {economyVisible=true;brushCells.clear();}
                        else if(quick==1) openSlots();
                        else recenter();
                    } else if (action>=0 && action==pendingAction) activateAction(action);
                    else if (tool>=0 && tool==pendingTool) {
                        selected=tools[tool];
                        hoverX=-1;hoverY=-1;
                        message("Ferramenta: "+toolNames[tool]+" (R$ "+city.cost(selected)+")");
                    }
                } else if (brushGesture && paintMode && !startedOnUi && !suppressTap) {
                    finishStroke(screenX,screenY);
                } else if (!moving && !startedOnUi && !suppressTap &&
                           !HudLayout.isHud(screenX,screenY,w,h,uiScale)) {
                    buildAt(screenX,screenY);
                }
                pendingAction=-1;pendingTool=-1;
                pendingRecenter=false;
                pendingQuickAction=-1;
                startedOnUi=false;
                brushCells.clear();
                brushGesture=false;
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
            if(keycode==Input.Keys.ESCAPE || keycode==Input.Keys.BACK) {
                if(economyVisible) {economyVisible=false;return true;}
                if(slotsVisible && savesReady)slotsVisible=false;
                else openSlots();
                return true;
            }
            if(slotsVisible || economyVisible)return true;
            if(keycode==Input.Keys.E) {economyVisible=true;return true;}
            if(keycode==Input.Keys.S)save(true);
            if(keycode==Input.Keys.L)load(true);
            if(keycode==Input.Keys.SPACE)activateAction(0);
            if(keycode==Input.Keys.B)activateAction(2);
            if(keycode==Input.Keys.EQUALS)activateAction(1);
            if(keycode>=Input.Keys.NUM_1 && keycode<=Input.Keys.NUM_5)
                selected=tools[keycode-Input.Keys.NUM_1];
            return true;
        }
    }
}
