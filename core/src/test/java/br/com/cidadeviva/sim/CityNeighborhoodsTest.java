package br.com.cidadeviva.sim;

public final class CityNeighborhoodsTest {
    private static void check(boolean condition,String message) {
        if(!condition)throw new AssertionError(message);
    }
    private static City fixture(boolean polluted,boolean green,boolean connectedShop) {
        Tile[][] map=new Tile[City.WIDTH][City.HEIGHT];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++)map[x][y]=new Tile(TileType.GRASS);
        for(int x=10;x<=20;x++)map[x][10]=new Tile(TileType.ROAD);
        map[10][9]=new Tile(TileType.RESIDENTIAL,2);
        map[11][11]=new Tile(TileType.INDUSTRIAL,polluted?3:0);
        map[20][11]=new Tile(TileType.COMMERCIAL,3);
        if(!connectedShop)map[16][10]=new Tile(TileType.GRASS);
        if(green)for(int x=8;x<=12;x++)map[x][8]=new Tile(TileType.FOREST);
        City city=new City();city.restore(map,15000,30,0);return city;
    }
    public static void main(String[] args) throws Exception {
        City clean=fixture(false,false,true),dirty=fixture(true,false,true);
        CityNeighborhoods c=clean.economy().neighborhoods,d=dirty.economy().neighborhoods;
        check(c.commerceAt(10,9),"Staffed shops reached by a road count");
        check(d.pollutionAt(10,9)>c.pollutionAt(10,9),"Industry creates local emissions");
        check(d.satisfactionAt(10,9)<c.satisfactionAt(10,9),"Pollution reduces satisfaction despite employment");
        check(d.pollutedResidents==24,"Affected residents counted rather than all city residents");
        check(d.complaintAt(dirty,dirty.economy().mobility,10,9).contains("POLUICAO"),"Inspector explains industrial pollution");
        City green=fixture(false,true,true);
        check(green.economy().neighborhoods.satisfactionAt(10,9)>c.satisfactionAt(10,9),"Preserved woodland adds local amenity");
        City broken=fixture(false,false,false);
        check(!broken.economy().neighborhoods.commerceAt(10,9),"Nearby disconnected shops do not serve residents");
        check(broken.economy().mobility.employedAt(10,9)==0,"Individual home employment follows actual network");
        check(clean.economy().mobility.employedAt(10,9)==24,"Home allocation conserves residents");
        check(clean.economy().mobility.workersAt(20,11)==24,"Workplace allocation conserves workers");
        java.io.ByteArrayOutputStream saved=new java.io.ByteArrayOutputStream();
        CityPersistence.save(dirty,saved);
        City reload=CityPersistence.load(new java.io.ByteArrayInputStream(saved.toByteArray()));
        check(reload.economy().neighborhoods.satisfactionAt(10,9)==d.satisfactionAt(10,9),"CVT3 reload preserves local quality");
        for(int day=0;day<80;day++) {
            dirty.nextDay();reload.nextDay();
            check(dirty.residents()==reload.residents() && dirty.money()==reload.money(),"Daily replay is deterministic");
            check(dirty.tile(10,9).level<=2,"Polluted homes cannot upgrade to level three");
        }
        System.out.println("OK: neighborhood pollution, greenery, access, development and CVT3 replay.");
    }
}
