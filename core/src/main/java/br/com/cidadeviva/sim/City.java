package br.com.cidadeviva.sim;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import br.com.cidadeviva.input.BuildStroke;

/** Estado e regras do jogo: sem dependências de libGDX, testável com Java puro. */
public final class City {
    public static final int WIDTH = 48;
    public static final int HEIGHT = 48;
    private static final int INITIAL_MONEY = 15000;
    private final Tile[][] tiles = new Tile[WIDTH][HEIGHT];
    private int money = INITIAL_MONEY;
    private int day = 1;
    private int lastIncome = 0;
    private CityEconomy cachedEconomy;
    // Per-city settings, persisted in CVT3. Existing CVT1/CVT2 cities default to 10%.
    private final int[] taxRates = {10, 10, 10};
    // Consecutive weak-demand days for each zone category; limits one-day collapses.
    private final int[] lowDemandDays = {0, 0, 0};
    /** Presentation-only changes produced by the most recent simulated day.
     * Never serialized: saves remain CVT3 and can be loaded by Alpha 3. */
    private final ArrayList<GrowthEvent> recentEvents = new ArrayList<>();
    private final List<GrowthEvent> publicEvents = Collections.unmodifiableList(recentEvents);
    private final ArrayList<CityMilestones.Milestone> recentMilestones = new ArrayList<>();
    private final List<CityMilestones.Milestone> publicMilestones = Collections.unmodifiableList(recentMilestones);

    public static final class GrowthEvent {
        public final int x, y, fromLevel, toLevel;
        public final TileType type;
        private GrowthEvent(int x,int y,TileType type,int fromLevel,int toLevel) {
            this.x=x;this.y=y;this.type=type;
            this.fromLevel=fromLevel;this.toLevel=toLevel;
        }
        public boolean isGrowth() {return toLevel>fromLevel;}
    }
    public List<GrowthEvent> recentEvents() {return publicEvents;}
    public List<CityMilestones.Milestone> recentMilestones() {return publicMilestones;}


    public City() { this(0); }

    /** Distinct deterministic layouts per save slot; slot one preserves original map. */
    public City(int seed) {
        int dx=seed==0?0:Math.floorMod(seed*13,9)-4;
        int dy=seed==0?0:Math.floorMod(seed*7,9)-4;
        for (int x = 0; x < WIDTH; x++) for (int y = 0; y < HEIGHT; y++) {
            // Ilhas de água, faixas de mata; centro reservado para uma cidade inicial.
            // Deterministic connected lakes and groves; saves keep their existing terrain.
            // Rivers/lakes are contiguous, rather than unrelated blue checkered squares.
            double southLake = square((x-(5.5+dx))/8.1) + square((y-(38.0+dy))/9.5);
            double northPond = square((x-(40.0-dx))/4.8) + square((y-(8.5-dy))/3.5);
            double shoreNoise = 0.10*Math.sin(x*0.91+y*0.38) + 0.07*Math.cos(x*0.37-y*0.79);
            boolean center = Math.abs(x - WIDTH / 2) < 9 && Math.abs(y - HEIGHT / 2) < 9;
            boolean water = !center && (southLake < 1.0+shoreNoise || northPond < 0.93+shoreNoise);
            double grove = Math.sin((x+dx)*0.31+1.8)*Math.cos((y+dy)*0.23+0.7)
                + 0.24*Math.sin((x+y)*0.71);
            boolean forest = !center && grove > 0.59
                && Math.floorMod((x*113+y*71+x*y*5),7)!=0;
            tiles[x][y] = new Tile(water ? TileType.WATER : (forest ? TileType.FOREST : TileType.GRASS));
        }
        // Centro inicial permite observar imediatamente como as casas crescem.
        int cx = WIDTH / 2, cy = HEIGHT / 2;
        for (int i = -5; i <= 5; i++) {
            tiles[cx + i][cy] = new Tile(TileType.ROAD);
            tiles[cx][cy + i] = new Tile(TileType.ROAD);
        }
        for (int i = -3; i <= 3; i++) {
            if (i == 0) continue;
            tiles[cx + i][cy - 1] = new Tile(TileType.RESIDENTIAL, 1);
            tiles[cx + i][cy + 1] = new Tile(i > 0 ? TileType.COMMERCIAL : TileType.INDUSTRIAL, 1);
        }
    }

    private static double square(double value) { return value * value; }

    /** Water neighbours use the same bit order as roads: SE, NW, SW, NE. */
    public int waterMask(int x,int y) {
        if (!isWater(x,y)) return 0;
        int mask=0;
        if (isWater(x+1,y)) mask|=1;
        if (isWater(x-1,y)) mask|=2;
        if (isWater(x,y+1)) mask|=4;
        if (isWater(x,y-1)) mask|=8;
        return mask;
    }
    private boolean isWater(int x,int y) {
        Tile tile=tile(x,y);
        return tile!=null && tile.type==TileType.WATER;
    }

    public Tile tile(int x, int y) { return inBounds(x,y) ? tiles[x][y] : null; }
    public boolean inBounds(int x,int y) { return x>=0 && y>=0 && x<WIDTH && y<HEIGHT; }
    public int money() { return money; }
    public int day() { return day; }
    public int lastIncome() { return lastIncome; }

    private static int zoneIndex(TileType type) {
        if (type==TileType.RESIDENTIAL) return 0;
        if (type==TileType.COMMERCIAL) return 1;
        if (type==TileType.INDUSTRIAL) return 2;
        throw new IllegalArgumentException("Apenas zonas R/C/I possuem aliquota");
    }
    public int taxRate(TileType type) { return taxRates[zoneIndex(type)]; }
    public void setTaxRate(TileType type,int percent) {
        if (percent<5 || percent>20) throw new IllegalArgumentException("Aliquota deve ser 5% a 20%");
        taxRates[zoneIndex(type)]=percent;
        cachedEconomy=null;
    }
    public int lowDemandDays(TileType type) {return lowDemandDays[zoneIndex(type)];}

    public int cost(TileType type) {
        switch (type) {
            case ROAD: return 20;
            case RESIDENTIAL: return 80;
            case COMMERCIAL: return 110;
            case INDUSTRIAL: return 150;
            case GRASS: return 0; // saneamento fiscal: remover estruturas e gratis
            default: return 0;
        }
    }

    /** Retorna mensagem de feedback; não modifica o mapa se a operação for inválida. */
    public String build(int x, int y, TileType type) {
        if (!inBounds(x, y)) return "Fora do mapa";
        Tile existing = tiles[x][y];
        if (type == TileType.GRASS) {
            if (existing.type == TileType.WATER) return "Não pode remover água";
            if (existing.type == TileType.GRASS) return "Área vazia";
            if (money < cost(type)) return "Dinheiro insuficiente";
            money -= cost(type);
            tiles[x][y] = new Tile(TileType.GRASS);
            cachedEconomy=null;
            return "Terreno liberado";
        }
        if (type != TileType.ROAD && !type.isZone()) return "Construção inválida";
        if (existing.type == TileType.WATER) return "Não pode construir na água";
        if (existing.type == type) return "Já construído";
        if (existing.type != TileType.GRASS && existing.type != TileType.FOREST) return "Remova a estrutura antes";
        if (money < cost(type)) return "Dinheiro insuficiente";
        money -= cost(type);
        tiles[x][y] = new Tile(type);
        cachedEconomy=null;
        return "Construído";
    }

    /** A preview and the batch-commit use exactly the same eligibility rules. */
    public boolean canBuild(int x,int y,TileType type) {
        if (!inBounds(x,y)) return false;
        TileType current=tiles[x][y].type;
        if (type==TileType.GRASS) return current!=TileType.GRASS && current!=TileType.WATER;
        if (type!=TileType.ROAD && !type.isZone()) return false;
        return current==TileType.GRASS || current==TileType.FOREST;
    }

    public int roadMask(int x,int y) {
        if (!isRoad(x,y)) return 0;
        int mask=0;
        if (isRoad(x+1,y)) mask |= 1; // southeast
        if (isRoad(x-1,y)) mask |= 2; // northwest
        if (isRoad(x,y+1)) mask |= 4; // southwest
        if (isRoad(x,y-1)) mask |= 8; // northeast
        return mask;
    }

    public static final class BuildResult {
        public final int built,ignored,cost;
        public final boolean insufficientFunds;
        BuildResult(int built,int ignored,int cost,boolean insufficientFunds) {
            this.built=built;this.ignored=ignored;this.cost=cost;
            this.insufficientFunds=insufficientFunds;
        }
        public String feedback() {
            if (insufficientFunds) return "Saldo insuficiente: R$ "+cost;
            if (built==0) return "Nenhuma celula disponivel";
            return built+" celulas | R$ "+cost+(ignored>0?" | "+ignored+" ignoradas":"");
        }
    }

    public BuildResult preview(List<BuildStroke.Cell> targets,TileType type) {
        LinkedHashSet<BuildStroke.Cell> unique=new LinkedHashSet<>(targets);
        int valid=0;
        for (BuildStroke.Cell cell:unique)
            if (canBuild(cell.x,cell.y,type)) valid++;
        long estimated=(long)valid * cost(type);
        return new BuildResult(valid,unique.size()-valid,(int)Math.min(Integer.MAX_VALUE,estimated),estimated>money);
    }

    /** Atomic payment for multi-tile gestures; blocked tiles are skipped for free. */
    public BuildResult buildArea(List<BuildStroke.Cell> targets,TileType type) {
        BuildResult estimate=preview(targets,type);
        if (estimate.insufficientFunds || estimate.built==0) return estimate;
        // Re-evaluate cells only after affordability, keeping this action all-or-nothing.
        for (BuildStroke.Cell cell:new LinkedHashSet<>(targets)) {
            if (canBuild(cell.x,cell.y,type)) tiles[cell.x][cell.y]=new Tile(type);
        }
        money-=estimate.cost;
        cachedEconomy=null;
        return estimate;
    }

    /** A lot requires a road on one of its four cardinal sides. A distant road does not count. */
    public int unservedZones() {
        int result=0;
        for (int x=0;x<WIDTH;x++) for (int y=0;y<HEIGHT;y++)
            if (tiles[x][y].type.isZone() && !hasRoad(x,y)) result++;
        return result;
    }

    public String zoneDescription(int x,int y) {
        if (!inBounds(x,y)) return "Fora do mapa";
        Tile t=tiles[x][y];
        if (!t.type.isZone()) return "Toque em uma zona para ver detalhes";
        String kind=t.type==TileType.RESIDENTIAL?"CASAS":
            t.type==TileType.COMMERCIAL?"LOJAS":"INDUSTRIA";
        CityEconomy report=economy();
        return kind+" NV "+t.level+" | "+(hasRoad(x,y)?"RUA AO LADO | ":"")+report.neighborhoods.complaintAt(this,report.mobility,x,y);
    }

    public boolean hasRoad(int x,int y) {
        return (isRoad(x+1,y) || isRoad(x-1,y) || isRoad(x,y+1) || isRoad(x,y-1));
    }
    private boolean isRoad(int x,int y) {
        Tile tile = tile(x,y);
        return tile != null && tile.type == TileType.ROAD;
    }

    public int residents() {
        int result = 0;
        for (Tile[] column : tiles) for (Tile t : column)
            if (t.type == TileType.RESIDENTIAL) result += t.level * 12;
        return result;
    }
    public int jobs() {
        int result = 0;
        for (Tile[] column : tiles) for (Tile t : column)
            if (t.type == TileType.COMMERCIAL) result += t.level * 10;
            else if (t.type == TileType.INDUSTRIAL) result += t.level * 18;
        return result;
    }
    public int roads() {
        int result = 0;
        for (Tile[] column : tiles) for (Tile t : column) if (t.type == TileType.ROAD) result++;
        return result;
    }
    public int zones() {
        int result = 0;
        for (Tile[] column : tiles) for (Tile t : column) if (t.type.isZone()) result++;
        return result;
    }

    /** A transparent, read-only report recomputed from persisted city tiles. */
    public CityEconomy economy() {
        if(cachedEconomy==null)cachedEconomy=CityEconomy.analyze(this);
        return cachedEconomy;
    }

    /** Deterministic local growth roll. Reloading a save never changes the day's random result. */
    private static float growthRoll(int day,int x,int y,int salt) {
        int v=day*0x9e3779b9 ^ x*0x85ebca6b ^ y*0xc2b2ae35 ^ salt*0x27d4eb2d;
        v ^= v >>> 16; v *= 0x7feb352d; v ^= v >>> 15;
        v *= 0x846ca68b; v ^= v >>> 16;
        return (v & 0x7fffffff) / 2147483648f;
    }

    /**
     * One day of demand-driven growth and actual revenue/maintenance.
     * Low-demand zones wait instead of appearing randomly. Only roadside lots grow.
     */
    public void nextDay() {
        CityEconomy report = economy();
        int populationBefore=residents();
        recentEvents.clear();
        recentMilestones.clear();
        day++;
        final TileType[] categories={TileType.RESIDENTIAL,TileType.COMMERCIAL,TileType.INDUSTRIAL};
        for(int i=0;i<categories.length;i++) {
            if(report.demand(categories[i])<20)lowDemandDays[i]=Math.min(1000000,lowDemandDays[i]+1);
            else lowDemandDays[i]=0;
        }
        lastIncome = report.net;
        // Existing CVT3 saves require a non-negative treasury.
        money = (int)Math.max(0L,Math.min(Integer.MAX_VALUE,(long)money + report.net));
        // A visible city should get new buildings early without exhausting the
        // entire supply in one tick. The deterministic daily cap also prevents
        // fast-forward bursts of dozens of instant developments.
        int vacantLimit = Math.max(1, Math.min(6, 1+zones()/14));
        int occupiedToday=0;
        // Different daily scan origins avoid growth always favouring the same
        // side of the map, while remaining deterministic across save reloads.
        int originX=Math.floorMod(day*17,WIDTH);
        int originY=Math.floorMod(day*23,HEIGHT);
        for(int xi=0;xi<WIDTH;xi++) for(int yi=0;yi<HEIGHT;yi++) {
            int x=(originX+xi)%WIDTH, y=(originY+yi)%HEIGHT;
            Tile t=tiles[x][y];
            if(!t.type.isZone()) continue;
            boolean accessible=hasRoad(x,y);
            int demand=report.demand(t.type);
            int localQuality=report.neighborhoods.satisfactionAt(x,y);
            boolean unhealthy=t.type==TileType.RESIDENTIAL && localQuality<28;
            if(t.level>0 && ((!accessible && day>=14) ||
                    (accessible && demand<20 && lowDemandDays(t.type)>=12) || (unhealthy && day>=14))) {
                float chance=!accessible?0.045f:unhealthy?0.035f:0.02f+(20-demand)*0.0015f;
                if(growthRoll(day,x,y,17)<chance) {
                    int from=t.level--;
                    recentEvents.add(new GrowthEvent(x,y,t.type,from,t.level));
                }
                continue;
            }
            if(!accessible || demand<24 || t.level>=3)continue;
            if(t.type==TileType.RESIDENTIAL && (unhealthy ||
                (t.level==1 && localQuality<45) || (t.level==2 && localQuality<65)))continue;
            if(t.level==0 && occupiedToday>=vacantLimit)continue;
            // Empty roadside lots develop quickly; established buildings gain
            // levels progressively. High demand visibly improves the rhythm.
            float rate=t.level==0?0.075f+demand*0.0026f:
                t.level==1?0.006f+demand*0.00053f:
                0.002f+demand*0.00025f;
            // A business with too many empty jobs does not expand as if every
            // workplace were staffed. Likewise a fiscal crisis slows investment.
            if(t.type!=TileType.RESIDENTIAL && report.operationalJobs>0) {
                int vacantShare=report.jobVacancies*100/report.operationalJobs;
                if(vacantShare>33) rate*=0.25f;
                else if(vacantShare>17) rate*=0.60f;
            }
            if(report.net<0 && money<3000)rate*=0.38f;
            if(report.qualityOfLife<45)rate*=0.55f;
            if(t.type==TileType.RESIDENTIAL)rate*=0.6f+localQuality/125f;
            if(growthRoll(day,x,y,0)<rate) {
                int from=t.level++;
                if(from==0)occupiedToday++;
                recentEvents.add(new GrowthEvent(x,y,t.type,from,t.level));
            }
        }
        cachedEconomy=null;
        recentMilestones.addAll(CityMilestones.crossed(populationBefore,residents()));
    }

    /** Chamado apenas por CityPersistence depois de validar o arquivo inteiro. */
    void restore(Tile[][] incoming,int restoredMoney,int restoredDay,int restoredIncome) {
        restore(incoming,restoredMoney,restoredDay,restoredIncome,new int[]{10,10,10},new int[]{0,0,0});
    }
    void restore(Tile[][] incoming,int restoredMoney,int restoredDay,int restoredIncome,int[] rates,int[] weakDays) {
        for (int x=0; x<WIDTH; x++) for (int y=0; y<HEIGHT; y++)
            tiles[x][y] = incoming[x][y];
        recentEvents.clear();
        recentMilestones.clear();
        money = restoredMoney;
        day = restoredDay;
        lastIncome = restoredIncome;
        cachedEconomy=null;
        for(int i=0;i<3;i++) {taxRates[i]=rates[i];lowDemandDays[i]=weakDays[i];}
    }
}
