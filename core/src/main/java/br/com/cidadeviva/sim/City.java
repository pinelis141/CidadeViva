package br.com.cidadeviva.sim;

import java.util.Random;
import java.util.List;
import java.util.LinkedHashSet;
import br.com.cidadeviva.input.BuildStroke;

/** Estado e regras do jogo: sem dependências de libGDX, testável com Java puro. */
public final class City {
    public static final int WIDTH = 48;
    public static final int HEIGHT = 48;
    private static final int INITIAL_MONEY = 15000;
    private final Tile[][] tiles = new Tile[WIDTH][HEIGHT];
    private final Random random = new Random(1219);
    private int money = INITIAL_MONEY;
    private int day = 1;
    private int lastIncome = 0;

    public City() {
        for (int x = 0; x < WIDTH; x++) for (int y = 0; y < HEIGHT; y++) {
            // Ilhas de água, faixas de mata; centro reservado para uma cidade inicial.
            int n = Math.floorMod((x * 17 + y * 31 + x * y * 13) ^ (x * 43 - y * 29), 109);
            boolean center = Math.abs(x - WIDTH / 2) < 9 && Math.abs(y - HEIGHT / 2) < 9;
            TileType terrain = !center && (x < 10 && y > 28 && n < 60) ? TileType.WATER
                : (!center && n < 15 ? TileType.FOREST : TileType.GRASS);
            tiles[x][y] = new Tile(terrain);
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
            tiles[cx + i][cy + 1] = new Tile(i > 0 ? TileType.COMMERCIAL : TileType.RESIDENTIAL, 1);
        }
    }

    public Tile tile(int x, int y) { return inBounds(x,y) ? tiles[x][y] : null; }
    public boolean inBounds(int x,int y) { return x>=0 && y>=0 && x<WIDTH && y<HEIGHT; }
    public int money() { return money; }
    public int day() { return day; }
    public int lastIncome() { return lastIncome; }

    public int cost(TileType type) {
        switch (type) {
            case ROAD: return 20;
            case RESIDENTIAL: return 80;
            case COMMERCIAL: return 110;
            case INDUSTRIAL: return 150;
            case GRASS: return 10; // remover
            default: return 0;
        }
    }

    /** Retorna mensagem de feedback; não modifica o mapa se a operação for inválida. */
    public String build(int x, int y, TileType type) {
        if (!inBounds(x, y)) return "Fora do mapa";
        Tile existing = tiles[x][y];
        if (type == TileType.GRASS) {
            if (existing.type == TileType.GRASS) return "Área vazia";
            if (money < cost(type)) return "Dinheiro insuficiente";
            money -= cost(type);
            tiles[x][y] = new Tile(TileType.GRASS);
            return "Terreno liberado";
        }
        if (type != TileType.ROAD && !type.isZone()) return "Construção inválida";
        if (existing.type == TileType.WATER) return "Não pode construir na água";
        if (existing.type == type) return "Já construído";
        if (existing.type != TileType.GRASS && existing.type != TileType.FOREST) return "Remova a estrutura antes";
        if (money < cost(type)) return "Dinheiro insuficiente";
        money -= cost(type);
        tiles[x][y] = new Tile(type);
        return "Construído";
    }

    /** A preview and the batch-commit use exactly the same eligibility rules. */
    public boolean canBuild(int x,int y,TileType type) {
        if (!inBounds(x,y)) return false;
        TileType current=tiles[x][y].type;
        if (type==TileType.GRASS) return current!=TileType.GRASS;
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
        return estimate;
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

    /** Um ciclo de simulação. Crescimento depende de estrada adjacente e oportunidades. */
    public void nextDay() {
        day++;
        int employed = Math.min(residents(), jobs());
        lastIncome = Math.max(0, residents() / 5) + employed / 4 + zones() * 2 - roads();
        money = Math.max(0, money + lastIncome); // Evita saldo negativo incompatível com o formato de save.
        for (int x=0; x<WIDTH; x++) for (int y=0; y<HEIGHT; y++) {
            Tile t = tiles[x][y];
            if (!t.type.isZone() || !hasRoad(x,y)) continue;
            if (t.level == 0 && random.nextFloat() < 0.18f) t.level=1;
            else if (t.level > 0 && t.level < 3 && random.nextFloat() < (t.level==1?0.010f:0.003f)) t.level++;
        }
    }

    /** Chamado apenas por CityPersistence depois de validar o arquivo inteiro. */
    void restore(Tile[][] incoming,int restoredMoney,int restoredDay,int restoredIncome) {
        for (int x=0; x<WIDTH; x++) for (int y=0; y<HEIGHT; y++)
            tiles[x][y] = incoming[x][y];
        money = restoredMoney;
        day = restoredDay;
        lastIncome = restoredIncome;
    }
}
