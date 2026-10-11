package br.com.cidadeviva.app;

import br.com.cidadeviva.sim.City;
import br.com.cidadeviva.sim.CityPersistence;
import br.com.cidadeviva.sim.CityEconomy;
import br.com.cidadeviva.sim.CityActivity;
import br.com.cidadeviva.sim.CityMilestones;
import br.com.cidadeviva.sim.CityFocus;
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
    private boolean inspectorVisible = false;
    private int selectedSlot = -1;
    private CitySlots.SlotInfo[] slotInfo = new CitySlots.SlotInfo[CitySlots.COUNT];
    private TileType selected = TileType.ROAD;
    private int hoverX = -1, hoverY = -1;
    // Cosmetic animation state: no new save fields and no extra textures.
    private static final float SECONDS_PER_DAY=2.55f;
    private static final int MAX_GROWTH_PULSES=18;
    private CityActivity activity;
    private float activityClock=0f;
    private float simClock = 0;
    private final float[] activityTile=new float[2];
    private final ArrayList<GrowthPulse> growthPulses=new ArrayList<>();
    private final Vector3 projectedGrowth=new Vector3();
    private float growthNoticeCooldown=0f;
    private CityMilestones.Milestone milestoneBanner;
    private float milestoneClock=0f;

    private static final class GrowthPulse {
        final int x,y,level;
        final TileType type;
        float age;
        GrowthPulse(City.GrowthEvent event) {
            x=event.x;y=event.y;level=event.toLevel;type=event.type;
        }
    }
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
    private boolean pendingInspector = false;

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
        refreshActivity();
        focusOnCity(false);
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
        milestoneClock=Math.max(0f,milestoneClock-delta);
        if (!simulationPaused && savesReady && !slotsVisible && !economyVisible && !helpVisible) {
            activityClock += delta;
            growthNoticeCooldown=Math.max(0f,growthNoticeCooldown-delta);
            for(int i=growthPulses.size()-1;i>=0;i--) {
                GrowthPulse pulse=growthPulses.get(i);
                pulse.age+=delta;
                if(pulse.age>=2.35f)growthPulses.remove(i);
            }
            simClock += delta * simulationSpeed;
            while (simClock >= SECONDS_PER_DAY) {
                simClock -= SECONDS_PER_DAY;
                city.nextDay();
                if(!city.recentMilestones().isEmpty()) {
                    milestoneBanner=city.recentMilestones().get(city.recentMilestones().size()-1);
                    milestoneClock=6.7f;
                }
                for(City.GrowthEvent event:city.recentEvents()) {
                    if(growthPulses.size()>=MAX_GROWTH_PULSES)growthPulses.remove(0);
                    growthPulses.add(new GrowthPulse(event));
                    if(event.isGrowth() && growthNoticeCooldown<=0f) {
                        String name=event.type==TileType.RESIDENTIAL?"Casas":
                            event.type==TileType.COMMERCIAL?"Comercio":"Industria";
                        message(name+" evoluindo no bairro!");
                        growthNoticeCooldown=4.8f;
                    }
                }
                // Traffic follows the current commuting flows. Refresh only every
                // five in-game days unless a building changed to limit allocations.
                if(!city.recentEvents().isEmpty() || city.day()%5==0)refreshActivity();
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
        drawCityActivity();
        drawGrowthEffects();
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

    /** Cars use only valid connected roads; pedestrians move near occupied lots. */
    private void drawCityActivity() {
        if(activity==null)return;
        Color[] carColors={rgb(242,181,94),rgb(110,197,222),rgb(235,123,117),
            rgb(213,222,230),rgb(139,208,152)};
        for(CityActivity.Car car:activity.cars()) {
            car.sample(activityClock,activityTile);
            float sx=(activityTile[0]-activityTile[1])*HALF_W;
            float sy=-(activityTile[0]+activityTile[1])*HALF_H;
            if(!visibleOnCamera(sx,sy))continue;
            shapes.setColor(0.05f,0.08f,0.09f,0.65f);
            shapes.rect(sx-5f,sy-3.5f,10f,5f);
            shapes.setColor(carColors[car.colorVariant]);
            shapes.rect(sx-4.5f,sy-1.5f,9f,4.5f);
            shapes.setColor(rgb(190,235,234));
            shapes.rect(sx-1.5f,sy+0.4f,3f,2.2f);
        }
        Color[] clothes={rgb(254,215,140),rgb(123,191,234),rgb(244,161,160),
            rgb(183,228,152),rgb(224,197,236)};
        for(CityActivity.Walker person:activity.walkers()) {
            person.sample(activityClock,activityTile);
            float sx=(activityTile[0]-activityTile[1])*HALF_W;
            float sy=-(activityTile[0]+activityTile[1])*HALF_H-7f;
            if(!visibleOnCamera(sx,sy))continue;
            shapes.setColor(rgb(28,48,50));
            shapes.rect(sx-1.2f,sy-2.2f,2.4f,3f);
            shapes.setColor(clothes[person.colorVariant]);
            shapes.rect(sx-1.6f,sy+0.8f,3.2f,4.2f);
            shapes.setColor(rgb(239,196,159));
            shapes.rect(sx-1.1f,sy+5f,2.2f,2.2f);
        }
    }

    private boolean visibleOnCamera(float sx,float sy) {
        return sx>camera.position.x-w*camera.zoom/2f-28 &&
            sx<camera.position.x+w*camera.zoom/2f+28 &&
            sy>camera.position.y-h*camera.zoom/2f-55 &&
            sy<camera.position.y+h*camera.zoom/2f+55;
    }

    /** A short expanding diamond calls attention to buildings that grew/shrank. */
    private void drawGrowthEffects() {
        for(GrowthPulse pulse:growthPulses) {
            float sx=(pulse.x-pulse.y)*HALF_W;
            float sy=-(pulse.x+pulse.y)*HALF_H;
            if(!visibleOnCamera(sx,sy))continue;
            float progress=pulse.age/2.35f;
            float radius=17+progress*21;
            if(pulse.level>0)shapes.setColor(0.45f,1f,0.66f,(1f-progress)*0.62f);
            else shapes.setColor(1f,0.49f,0.33f,(1f-progress)*0.58f);
            thinDiamond(sx,sy,radius,radius*0.5f);
            shapes.rect(sx-1.6f,sy+15f+progress*22f,3.2f,3.2f);
        }
    }

    private void drawGrowthLabels(float u) {
        for(GrowthPulse pulse:growthPulses) {
            float sx=(pulse.x-pulse.y)*HALF_W;
            float sy=-(pulse.x+pulse.y)*HALF_H+46f+pulse.age*16f;
            projectedGrowth.set(sx,sy,0f);
            camera.project(projectedGrowth);
            if(projectedGrowth.x<20*u || projectedGrowth.x>w-70*u ||
                projectedGrowth.y<HudLayout.BOTTOM_HEIGHT*u+16*u ||
                projectedGrowth.y>h-HudLayout.TOP_HEIGHT*u-8*u)continue;
            String value=pulse.level==0?"ABANDONO":
                pulse.type==TileType.RESIDENTIAL?"+ CASAS":
                pulse.type==TileType.COMMERCIAL?"+ LOJAS":"+ INDUSTRIA";
            label(value,projectedGrowth.x,projectedGrowth.y,1.03f,
                pulse.level==0?rgb(254,150,115):rgb(163,248,174),90*u);
        }
    }

    private void refreshActivity() {activity=new CityActivity(city);}

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

    /** Styled flat UI components, rendered without external assets or text allocations. */
    private void card(float x,float y,float width,float height,Color accent,boolean active) {
        shapes.setColor(active?rgb(91,117,112):rgb(44,66,77));
        shapes.rect(x,y,width,height);
        shapes.setColor(active?rgb(34,56,65):rgb(25,43,53));
        shapes.rect(x+1.5f*uiScale,y+1.5f*uiScale,
            Math.max(0,width-3*uiScale),Math.max(0,height-3*uiScale));
        shapes.setColor(accent);
        shapes.rect(x,y,3.0f*uiScale,height);
    }

    private void label(String value,float x,float base,float scale,Color color,float allowed) {
        if(allowed<8)return;
        float actual=scale*uiScale;
        font.getData().setScale(actual);
        glyph.setText(font,value);
        while(glyph.width>allowed && actual>0.70f*uiScale) {
            actual-=0.06f*uiScale;
            font.getData().setScale(actual);
            glyph.setText(font,value);
        }
        if(glyph.width>allowed) {
            String shortened=value;
            while(shortened.length()>2 && glyph.width>allowed) {
                shortened=shortened.substring(0,shortened.length()-2)+".";
                glyph.setText(font,shortened);
            }
            value=shortened;
        }
        font.setColor(color);
        font.draw(batch,value,x,base);
    }

    private void centered(String value,float left,float width,float base,float scale,Color color) {
        float actual=scale*uiScale;
        font.getData().setScale(actual);
        glyph.setText(font,value);
        while(glyph.width>width-10*uiScale && actual>0.72f*uiScale) {
            actual-=0.06f*uiScale;
            font.getData().setScale(actual);
            glyph.setText(font,value);
        }
        font.setColor(color);
        font.draw(batch,value,left+Math.max(3*uiScale,(width-glyph.width)*0.5f),base);
    }

    private String moneyShort(int money) {
        long absolute=Math.abs((long)money);
        if(absolute>=1000000L)return (money<0?"-":"")+"R$ "+(absolute/1000000)+"M";
        if(absolute>=100000L)return (money<0?"-":"")+"R$ "+(absolute/1000)+"mil";
        return (money<0?"-":"")+"R$ "+absolute;
    }

    private void drawHud() {
        float u=uiScale;
        CityEconomy eco=city.economy();
        float top=HudLayout.TOP_HEIGHT*u;
        float bottom=HudLayout.BOTTOM_HEIGHT*u;
        Color white=rgb(232,243,237),muted=rgb(159,189,192);
        Color good=rgb(112,214,148),bad=rgb(246,137,119);
        Color cyan=rgb(115,191,232),amber=rgb(242,183,96);
        Color[] sector={good,cyan,amber};
        int[] demand={eco.residentialDemand,eco.commercialDemand,eco.industrialDemand};
        String[] demandLabel={"RESIDENCIAL","COMERCIAL","INDUSTRIAL"};
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(uiProjection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.055f,0.10f,0.135f,0.94f);
        shapes.rect(0,h-top,w,top);
        shapes.setColor(0.055f,0.10f,0.135f,0.93f);
        shapes.rect(0,0,w,bottom);
        shapes.setColor(rgb(102,174,169));
        shapes.rect(0,h-top,w,2*u);
        shapes.rect(0,bottom,w,2*u);
        // Permanent navigation, separated from information.
        float[] shortcutX={w-357*u,w-244*u,w-117*u};
        float[] shortcutW={109*u,124*u,112*u};
        for(int i=0;i<3;i++)card(shortcutX[i],h-50*u,shortcutW[i],42*u,
            i==0?good:i==1?cyan:rgb(161,173,187),false);
        // Readable primary statistics.
        float gap=6*u,margin=10*u,statWidth=(w-2*margin-3*gap)/4f;
        Color[] statColors={good,cyan,rgb(191,204,215),eco.net>=0?good:bad};
        for(int i=0;i<4;i++)card(margin+i*(statWidth+gap),h-108*u,statWidth,52*u,statColors[i],false);
        // Three real demand score cards, each with number and visible progress meter.
        float demandWidth=(w-20*u-2*gap)/3f;
        for(int i=0;i<3;i++) {
            float bx=10*u+i*(demandWidth+gap);
            card(bx,h-169*u,demandWidth,55*u,sector[i],false);
            shapes.setColor(rgb(54,76,84));
            shapes.rect(bx+10*u,h-165*u,demandWidth-20*u,6*u);
            shapes.setColor(demand[i]>=60?good:demand[i]>=30?amber:bad);
            shapes.rect(bx+10*u,h-165*u,Math.max(0,demandWidth-20*u)*demand[i]/100f,6*u);
        }
        // Thin alert/diagnostics band.
        shapes.setColor(rgb(30,52,60));
        shapes.rect(10*u,h-191*u,w-20*u,18*u);
        // Six large quick-action buttons.
        float actionWidth=w/HudLayout.ACTIONS;
        for(int i=0;i<HudLayout.ACTIONS;i++) {
            float bx=i*actionWidth+3*u;
            boolean active=(i==0&&simulationPaused)||(i==2&&paintMode);
            card(bx,h-(HudLayout.ACTION_TOP+HudLayout.ACTION_HEIGHT-2)*u,
                actionWidth-6*u,(HudLayout.ACTION_HEIGHT-6)*u,
                active?amber:(i==4?good:rgb(93,137,155)),active);
        }
        // Tool dock: full card height, separate icon and baseline for name/cost.
        float tw=w/HudLayout.TOOLS;
        for(int i=0;i<HudLayout.TOOLS;i++) {
            float bx=i*tw+4*u,bw=tw-8*u;
            boolean active=selected==tools[i];
            card(bx,14*u,bw,(HudLayout.BOTTOM_HEIGHT-25)*u,
                active?rgb(244,211,122):toolColors[i],active);
            drawToolIcon(i,bx+bw*0.5f,77*u,1.18f*u);
        }
        // Tile inspector floating over map, with its own hit-test area.
        if(inspectorVisible && city.inBounds(hoverX,hoverY) && !helpVisible &&
           !slotsVisible && !economyVisible) {
            float iy=(HudLayout.BOTTOM_HEIGHT+6)*u;
            shapes.setColor(0.045f,0.10f,0.13f,0.94f);
            shapes.rect(8*u,iy,w-16*u,HudLayout.INSPECTOR_HEIGHT*u);
            shapes.setColor(amber);
            shapes.rect(8*u,iy,4*u,HudLayout.INSPECTOR_HEIGHT*u);
            card(w-59*u,iy+42*u,44*u,36*u,cyan,false);
        }
        if(milestoneClock>0 && milestoneBanner!=null &&
            !helpVisible && !slotsVisible && !economyVisible)drawMilestoneShape(u);
        if(helpVisible) {
            shapes.setColor(0.01f,0.02f,0.03f,0.88f);shapes.rect(0,0,w,h);
            card(15*u,h*0.5f-135*u,w-30*u,270*u,cyan,false);
        }
        if(slotsVisible)drawSlotsOverlay(u);
        if(economyVisible)drawEconomyOverlay(u,eco);
        shapes.end();
        batch.setProjectionMatrix(uiProjection);
        batch.begin();
        if(!helpVisible && !slotsVisible && !economyVisible) {
        drawGrowthLabels(u);
        label("CIDADE VIVA",13*u,h-20*u,1.42f,white,
            Math.max(45,w-372*u));
        label("GESTAO URBANA",14*u,h-38*u,0.82f,muted,
            Math.max(45,w-372*u));
        centered("ECON",shortcutX[0],shortcutW[0],h-25*u,1.20f,white);
        centered("CIDADES",shortcutX[1],shortcutW[1],h-25*u,1.15f,white);
        centered("CENTRAR",shortcutX[2],shortcutW[2],h-25*u,1.08f,white);
        String[] statNames={"CAIXA","MORADORES","DIA ATUAL","SALDO / DIA"};
        String[] statValues={moneyShort(city.money()),""+city.residents(),
            ""+city.day(),(eco.net>0?"+":"")+eco.net};
        for(int i=0;i<4;i++) {
            float x=margin+i*(statWidth+gap);
            label(statNames[i],x+10*u,h-72*u,0.92f,muted,statWidth-18*u);
            label(statValues[i],x+10*u,h-90*u,1.43f,
                i==3&&eco.net<0?bad:white,statWidth-18*u);
        }
        for(int i=0;i<3;i++) {
            float x=10*u+i*(demandWidth+gap);
            label(demandLabel[i],x+9*u,h-127*u,1.04f,sector[i],
                demandWidth-55*u);
            label(""+demand[i],x+demandWidth-48*u,h-127*u,1.40f,white,40*u);
            label(demand[i]>=60?"ALTA":demand[i]>=30?"MEDIA":"BAIXA",
                x+10*u,h-144*u,0.86f,muted,demandWidth-20*u);
        }
        int missing=eco.unserved;
        int nextGoal=CityMilestones.nextGoal(city.residents());
        label(missing>0?"! "+missing+" ZONAS SEM RUA":
            eco.disconnectedResidents>0?"! "+eco.disconnectedResidents+" SEM LIGACAO AO TRABALHO":
            eco.congestedRoads>=3?"! "+eco.congestedRoads+" RUAS CONGESTIONADAS":
            nextGoal>0?"META "+nextGoal+" MORADORES":"CIDADE EM EVOLUCAO",17*u,h-178*u,0.98f,
            missing>0?amber:good,(w-32*u)*0.48f);
        String labor="QUAL "+eco.qualityOfLife+" | TRAB "+eco.employed+" | DES "+eco.unemployed;
        label(labor,Math.max(w*0.53f,17*u),h-178*u,0.96f,white,w*0.46f-15*u);
        for(int i=0;i<HudLayout.ACTIONS;i++) {
            String text=(i==0&&simulationPaused)?"SEGUIR":i==1?simulationSpeed+"X":
                i==2?(paintMode?"PINTAR":"MOVER"):ACTION_LABELS[i];
            centered(text,i*actionWidth,actionWidth,
                h-(HudLayout.ACTION_TOP+HudLayout.ACTION_HEIGHT*0.62f)*u,
                1.12f,white);
        }
        for(int i=0;i<HudLayout.TOOLS;i++) {
            float x=i*tw;
            centered(toolNames[i],x,tw,46*u,1.30f,white);
            centered("R$ "+city.cost(tools[i]),x,tw,26*u,0.96f,muted);
        }
        if(inspectorVisible && city.inBounds(hoverX,hoverY) && !helpVisible &&
            !slotsVisible && !economyVisible)drawInspector(u,eco);
        if(milestoneClock>0 && milestoneBanner!=null &&
            !helpVisible && !slotsVisible && !economyVisible)drawMilestoneText(u);
        float toastY=(HudLayout.BOTTOM_HEIGHT+18)*u;
        if(inspectorVisible && city.inBounds(hoverX,hoverY))
            toastY+=(HudLayout.INSPECTOR_HEIGHT+8)*u;
        if(!helpVisible && !slotsVisible && !economyVisible && toastClock>0) {
            label(toast,13*u,toastY,1.12f,rgb(246,234,171),w-30*u);
        }
        if(!helpVisible && paintMode && brushGesture && !brushCells.isEmpty()) {
            City.BuildResult p=city.preview(new ArrayList<>(brushCells),selected);
            label("AREA "+p.built+"  CUSTO R$ "+p.cost+
                (p.ignored>0?"  |  BLOQUEADAS "+p.ignored:""),13*u,toastY+18*u,
                1.08f,p.insufficientFunds?bad:good,w-28*u);
        }
        }
        if(helpVisible)drawHelpText(u);
        if(slotsVisible)drawSlotsText(u);
        if(economyVisible)drawEconomyText(u,eco);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /** Celebrations are short-lived and do not change economy or persisted state. */
    private float milestoneW(float u) {return Math.min(w-28*u,420*u);}
    private float milestoneY(float u) {return h-(HudLayout.TOP_HEIGHT+101)*u;}
    private void drawMilestoneShape(float u) {
        float ww=milestoneW(u),x=(w-ww)*0.5f,y=milestoneY(u);
        shapes.setColor(0.025f,0.12f,0.13f,0.95f);
        shapes.rect(x,y,ww,76*u);
        shapes.setColor(rgb(244,199,105));
        shapes.rect(x,y+73*u,ww,3*u);
        shapes.rect(x+9*u,y+15*u,4*u,46*u);
        // Pixel medal: no extra texture or GPU allocation.
        shapes.rect(x+24*u,y+26*u,27*u,27*u);
        shapes.setColor(rgb(85,76,55));
        shapes.rect(x+30*u,y+32*u,15*u,15*u);
        shapes.setColor(rgb(244,199,105));
        shapes.rect(x+36*u,y+35*u,3*u,9*u);
    }
    private void drawMilestoneText(float u) {
        float ww=milestoneW(u),x=(w-ww)*0.5f,y=milestoneY(u);
        label(milestoneBanner.title,x+62*u,y+52*u,1.20f,
            rgb(255,222,150),ww-76*u);
        label(milestoneBanner.detail,x+62*u,y+27*u,1.07f,
            rgb(235,246,238),ww-76*u);
    }

    private void drawInspector(float u,CityEconomy report) {
        Tile t=city.tile(hoverX,hoverY);
        float base=(HudLayout.BOTTOM_HEIGHT+6)*u;
        String type=t.type==TileType.RESIDENTIAL?"RESIDENCIAL":
            t.type==TileType.COMMERCIAL?"COMERCIAL":
            t.type==TileType.INDUSTRIAL?"INDUSTRIAL":
            t.type==TileType.ROAD?"ESTRADA":
            t.type==TileType.WATER?"AGUA":
            t.type==TileType.FOREST?"FLORESTA":"TERRENO LIVRE";
        Color white=rgb(229,242,237),muted=rgb(170,195,197);
        label(type+"  |  NIVEL "+t.level,21*u,base+70*u,1.22f,white,w-94*u);
        if(t.type.isZone()) {
            label("RUA: "+(city.hasRoad(hoverX,hoverY)?"SIM":"NAO")+
                "  |  DEMANDA "+report.demand(t.type)+
                "  |  IMPOSTO "+city.taxRate(t.type)+"%",21*u,
                base+45*u,1.04f,muted,w-88*u);
            String reason=report.mainConstraint(t.type,city.hasRoad(hoverX,hoverY));
            int low=city.lowDemandDays(t.type);
            if(low>0)reason+="  |  "+low+" DIAS FRACOS";
            label(reason,21*u,base+19*u,0.96f,
                !city.hasRoad(hoverX,hoverY)?rgb(243,178,108):white,w-48*u);
        } else label("POSICAO "+hoverX+","+hoverY+"  |  TOQUE EM X PARA FECHAR",
            21*u,base+30*u,1.06f,muted,w-90*u);
        centered("X",w-59*u,44*u,base+54*u,1.25f,white);
    }

    private void drawHelpText(float u) {
        float x=30*u,y=h*0.5f+112*u;
        label("COMO JOGAR",x,y,1.56f,rgb(233,242,237),w-70*u);
        String[] lines={"TOQUE: construir ou inspecionar lote",
            "MOVER: arraste para navegar", "PINTAR: ruas e zonas por arrasto",
            "DOIS DEDOS: aproximar ou afastar", "R/C/I: barras = demanda de construcoes",
            "ECON: impostos, receitas e despesas", "TOQUE NA TELA PARA FECHAR"};
        for(int i=0;i<lines.length;i++)
            label(lines[i],x,y-(35+i*28)*u,1.00f,
                i==lines.length-1?rgb(242,194,113):rgb(188,218,217),w-72*u);
    }

    private float econW(float u) {return Math.min(w-24*u,560*u);}
    private float econX(float u) {return (w-econW(u))*0.5f;}
    private float econY(float u) {return (h-558*u)*0.5f;}
    private static final TileType[] TAX_TYPES = {
        TileType.RESIDENTIAL,TileType.COMMERCIAL,TileType.INDUSTRIAL
    };
    private static final String[] TAX_NAMES = {"RESIDENCIAL", "COMERCIAL", "INDUSTRIAL"};

    private void drawEconomyOverlay(float u,CityEconomy report) {
        float x=econX(u),y=econY(u),ww=econW(u);
        shapes.setColor(0.015f,0.035f,0.045f,0.91f);
        shapes.rect(0,0,w,h);
        shapes.setColor(rgb(29,51,63));shapes.rect(x,y,ww,558*u);
        shapes.setColor(rgb(94,177,180));shapes.rect(x,y+553*u,ww,5*u);
        // Tax cards keep touch coordinates unchanged from previous versions.
        Color[] colors={rgb(117,218,147),rgb(115,191,232),rgb(240,184,104)};
        for(int i=0;i<3;i++) {
            float cy=y+(356-i*83)*u;
            card(x+14*u,cy,ww-28*u,73*u,colors[i],false);
            card(x+ww-139*u,cy+13*u,52*u,48*u,rgb(148,178,187),false);
            card(x+ww-73*u,cy+13*u,52*u,48*u,rgb(148,178,187),false);
        }
        shapes.setColor(rgb(51,76,85));
        shapes.rect(x+17*u,y+177*u,ww-34*u,2*u);
        shapes.rect(x+17*u,y+83*u,ww-34*u,2*u);
        card(x+16*u,y+13*u,ww-32*u,47*u,rgb(111,199,148),false);
    }

    private void drawEconomyText(float u,CityEconomy report) {
        float x=econX(u),y=econY(u),ww=econW(u);
        Color white=rgb(236,246,239),sub=rgb(168,198,205);
        label("ECONOMIA",x+21*u,y+520*u,1.62f,white,ww-42*u);
        label("Taxas de 5% a 20%: excesso reduz arrecadacao",
            x+21*u,y+487*u,1.04f,sub,ww-42*u);
        label("QUAL "+report.qualityOfLife+" | EMP "+report.employed+"/"+report.population+
            " | VIAGEM "+report.commuteSteps+" | TRANS "+report.congestedRoads,x+21*u,y+465*u,0.95f,sub,ww-42*u);
        for(int i=0;i<3;i++) {
            float cy=y+(356-i*83)*u;
            int demand=report.demand(TAX_TYPES[i]);
            int rate=city.taxRate(TAX_TYPES[i]);
            Color sector=i==0?rgb(117,218,147):i==1?rgb(115,191,232):rgb(240,184,104);
            label(TAX_NAMES[i],x+27*u,cy+55*u,1.16f,sector,ww-175*u);
            label("IMPOSTO "+rate+"%",x+27*u,cy+34*u,1.06f,white,ww-175*u);
            label("DEMANDA "+demand+"  |  "+city.lowDemandDays(TAX_TYPES[i])+" dias fracos",
                x+27*u,cy+16*u,0.90f,sub,ww-175*u);
            centered("-",x+ww-139*u,52*u,cy+46*u,1.66f,white);
            centered("+",x+ww-73*u,52*u,cy+46*u,1.52f,white);
        }
        label("RECEITAS",x+22*u,y+163*u,1.12f,rgb(123,224,159),ww-44*u);
        label("R$ "+report.grossRevenue+" / dia",x+ww*0.63f,y+163*u,
            1.18f,white,ww*0.34f);
        label("Casas "+report.taxResidential+" | Lojas "+report.taxCommercial+
            " | Ind. "+report.taxIndustrial,x+22*u,y+142*u,0.94f,sub,ww-44*u);
        label("DESPESAS",x+22*u,y+118*u,1.12f,rgb(244,183,113),ww-44*u);
        label("R$ "+report.expenses+" / dia",x+ww*0.63f,y+118*u,
            1.18f,white,ww*0.34f);
        label("Ruas "+report.roadMaintenance+" | Zonas/Predios "+report.zoneMaintenance,
            x+22*u,y+97*u,0.94f,sub,ww-44*u);
        label("SALDO DIARIO",x+22*u,y+70*u,1.04f,white,ww*0.49f);
        label((report.net>=0?"+":"")+"R$ "+report.net,x+ww*0.55f,y+70*u,1.22f,
            report.net>=0?rgb(131,228,170):rgb(253,151,132),ww*0.41f);
        centered("FECHAR",x+16*u,ww-32*u,y+43*u,1.18f,white);
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
        inspectorVisible=false;
        if (!savesReady) simulationPaused=true;
        selectedSlot=saves.active();
        refreshSlotInfo();
        slotsVisible=true;
    }
    private void drawSlotsOverlay(float u) {
        shapes.setColor(0.01f,0.03f,0.04f,0.91f);
        shapes.rect(0,0,w,h);
        float x=slotsX(u),y=slotsY(u),ww=slotsW(u);
        shapes.setColor(rgb(28,50,60));
        shapes.rect(x,y,ww,452*u);
        shapes.setColor(rgb(116,194,187));
        shapes.rect(x,y+448*u,ww,4*u);
        for(int i=0;i<CitySlots.COUNT;i++) {
            float sy=slotRowY(i,u);
            boolean active=selectedSlot==i+1;
            card(x+12*u,sy,ww-24*u,72*u,
                active?rgb(240,202,112):rgb(91,143,158),active);
        }
        card(x+12*u,y+15*u,(ww-33*u)*0.62f,47*u,rgb(111,205,157),false);
        card(x+21*u+(ww-33*u)*0.62f,y+15*u,
            (ww-33*u)*0.38f,47*u,rgb(214,132,124),false);
    }

    private void drawSlotsText(float u) {
        float x=slotsX(u),y=slotsY(u),ww=slotsW(u);
        Color white=rgb(231,245,241),muted=rgb(167,194,201);
        label("MINHAS CIDADES",x+23*u,y+414*u,1.56f,white,ww-47*u);
        label("Tres espacos independentes. Seus dados ficam salvos.",
            x+23*u,y+381*u,0.98f,muted,ww-45*u);
        for(int i=0;i<CitySlots.COUNT;i++) {
            CitySlots.SlotInfo info=slotInfo[i];
            if(info==null)continue;
            float sy=slotRowY(i,u);
            label("CIDADE "+info.slot,x+28*u,sy+53*u,1.30f,white,ww-157*u);
            label(info.active?"EM USO":info.corrupt?"PROTEGIDA":
                info.empty?"NOVA":"SALVA",x+ww-120*u,sy+53*u,1.03f,
                info.corrupt?rgb(246,140,124):info.active?rgb(247,208,115):rgb(137,214,193),105*u);
            String description=info.corrupt?"Arquivo com erro. Nenhum dado sera apagado.":
                info.empty?"Espaco vazio - pronto para criar":
                "DIA "+info.day+"  |  POP "+info.population+
                "  |  R$ "+info.money+(info.backupUsed?"  (BACKUP)":"");
            label(description,x+28*u,sy+24*u,1.00f,
                info.corrupt?rgb(247,143,127):muted,ww-55*u);
        }
        String action="ABRIR / CRIAR";
        if(selectedSlot>=1 && selectedSlot<=CitySlots.COUNT && slotInfo[selectedSlot-1]!=null) {
            CitySlots.SlotInfo chosen=slotInfo[selectedSlot-1];
            action=chosen.active?"CONTINUAR":chosen.corrupt?"PROTEGIDA":
                chosen.empty?"CRIAR NOVA":"ABRIR CIDADE";
        }
        centered(action,x+12*u,(ww-33*u)*0.62f,y+43*u,1.12f,white);
        centered("VOLTAR",x+21*u+(ww-33*u)*0.62f,
            (ww-33*u)*0.38f,y+43*u,1.10f,white);
        label("Cidade 1 preserva seu save original.",x+23*u,y+83*u,
            0.96f,rgb(179,213,190),ww-46*u);
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
                refreshActivity();
                growthPulses.clear();
                milestoneClock=0f;
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
            refreshActivity();
            growthPulses.clear();
            milestoneClock=0f;
            focusOnCity(false);
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

    private void focusOnCity(boolean feedback) {
        CityFocus.Target focus=CityFocus.forCity(city);
        float u=uiScale;
        camera.zoom=focus.suggestedZoom(w,h,HudLayout.TOP_HEIGHT*u,HudLayout.BOTTOM_HEIGHT*u);
        // Adjust for UI obscuring more of the map at the top than bottom.
        float verticalOffset=(HudLayout.TOP_HEIGHT-HudLayout.BOTTOM_HEIGHT)*u*0.5f*camera.zoom;
        camera.position.set(focus.x,focus.y+verticalOffset,0);
        clampCamera();
        if(feedback)message("Bairro em destaque! Pince para ampliar ou afastar.");
    }

    private void recenter() {focusOnCity(true);}

    private void pan(float dx,float dy) {
        camera.position.add(-dx*camera.zoom,dy*camera.zoom,0);
        clampCamera();
    }

    /** Anchor the previously touched world point under the new finger midpoint. */
    private void zoomAround(float factor, float oldX,float oldY,float newX,float newY) {
        float oldZoom=camera.zoom;
        float newZoom=MathUtils.clamp(oldZoom*factor,0.40f,2.1f);
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
        refreshActivity();
        hoverX=x;hoverY=y;
        inspectorVisible=true;
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
        if(result.built>0 && !result.insufficientFunds)refreshActivity();
        String response=result.feedback();
        if (selected.isZone() && !result.insufficientFunds && result.built>0 &&
            city.unservedZones()>0) response+=" | SEM RUA: "+city.unservedZones();
        message(response);
        brushCells.clear();
        brushGesture=false;
        hoverX=-1;hoverY=-1;
        inspectorVisible=false;
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
                pendingInspector=inspectorVisible && HudLayout.inspectorAt(screenX,screenY,w,h,uiScale);
                startedOnUi=helpVisible || pendingInspector || HudLayout.isHud(screenX,screenY,w,h,uiScale);
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
                } else if (pendingInspector && !suppressTap && !moving) {
                    inspectorVisible=false;
                    hoverX=-1;hoverY=-1;
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
                        inspectorVisible=false;
                        message("Ferramenta: "+toolNames[tool]+" (R$ "+city.cost(selected)+")");
                    }
                } else if (brushGesture && paintMode && !startedOnUi && !suppressTap) {
                    finishStroke(screenX,screenY);
                } else if (!moving && !startedOnUi && !suppressTap &&
                           !HudLayout.isHud(screenX,screenY,w,h,uiScale)) {
                    buildAt(screenX,screenY);
                }
                pendingAction=-1;pendingTool=-1;
                pendingInspector=false;
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
