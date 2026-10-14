package br.com.cidadeviva.sim;

/** Diagnostic score 0 (deficient) to 100 (healthy); -1 denotes not applicable/planning. */
public enum CityLayer {
    NONE("NORMAL"),ENERGY("ENERGIA"),WATER("AGUA"),SERVICES("ESCOLA / SAUDE"),POLLUTION("POLUICAO"),SATISFACTION("SATISFACAO"),VALUE("VALORIZACAO"),SAFETY("SEGURANCA"),WASTE("LIXO"),SEWAGE("ESGOTO"),TRAFFIC("TRANSITO"),TRANSIT("ONIBUS");
    public final String label;CityLayer(String label){this.label=label;}
    public int score(City c,CityEconomy e,int x,int y){
        Tile t=c.tile(x,y);
        switch(this){
            case ENERGY:case WATER:{int kind=this==ENERGY?0:1,need=CityUtilities.need(t,kind);if(need==0||!(kind==0?e.utilities.powerEnabled:e.utilities.waterEnabled))return -1;return e.utilities.suppliedAt(kind,x,y)*100/need;}
            case SERVICES:if(t.type!=TileType.RESIDENTIAL||t.level==0)return -1;return Math.min(e.services.coveredAt(TileType.SCHOOL,x,y),e.services.coveredAt(TileType.CLINIC,x,y))*100/(t.level*12);
            case POLLUTION:return 100-e.neighborhoods.pollutionAt(x,y);
            case SATISFACTION:return t.type==TileType.RESIDENTIAL?e.neighborhoods.satisfactionAt(x,y):-1;
            case VALUE:return t.type==TileType.RESIDENTIAL?e.neighborhoods.landValueAt(x,y):-1;
            case SAFETY:return t.type.isZone()&&t.level>0?(e.civics.percent(TileType.POLICE_STATION,x,y)+e.civics.percent(TileType.FIRE_STATION,x,y))/2:-1;
            case WASTE:return !e.civics.wasteEnabled||!t.type.isZone()||t.level==0?-1:e.civics.percent(TileType.WASTE_CENTER,x,y);
            case SEWAGE:return !e.civics.sewageEnabled||!t.type.isZone()||t.level==0?-1:e.civics.percent(TileType.SEWAGE_PLANT,x,y);
            case TRAFFIC:return t.type==TileType.ROAD?100-Math.min(100,e.mobility.trafficAt(x,y)*100/80):-1;
            case TRANSIT:return t.type.isZone()?e.mobility.transit.networkAt(x,y)>0?100:0:-1;
            default:return -1;
        }
    }
}
