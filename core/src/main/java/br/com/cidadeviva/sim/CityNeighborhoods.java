package br.com.cidadeviva.sim;

import java.util.Arrays;

/** Local conditions derived from saved tiles and real commuting. No renderer or hidden state. */
public final class CityNeighborhoods {
    private static final int H=City.HEIGHT, N=City.WIDTH*H;
    private final int[] pollution=new int[N], greenery=new int[N], satisfaction=new int[N];
    private final boolean[] shops=new boolean[N];
    private final int[] landValue=new int[N],wealth=new int[N];
    public final int popularResidents,middleResidents,wealthyResidents;
    public final int averageSatisfaction, pollutedResidents, dissatisfiedResidents;

    private CityNeighborhoods(City city,CityMobility mobility,CityServices services,CityUtilities utilities,CityCivics civics) {
        // Bounded source influence: industry level matters; vacant zones do not emit.
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<H;y++) {
            Tile t=city.tile(x,y);
            if(t.type==TileType.ROAD&&mobility.trafficAt(x,y)>=80)spread(pollution,x,y,2,Math.min(3,mobility.trafficAt(x,y)/80));
            if(t.type==TileType.INDUSTRIAL && t.level>0 && utilities.hasSupply(x,y))spread(pollution,x,y,6,t.level*3);
            if((t.type==TileType.WASTE_CENTER||t.type==TileType.SEWAGE_PLANT)&&city.money()>0&&city.hasRoad(x,y)&&utilities.hasSupply(x,y))spread(pollution,x,y,t.type==TileType.WASTE_CENTER?3:2,2);
            if(t.type==TileType.FOREST)spread(greenery,x,y,3,1);
        }
        int people=0, unhappy=0, polluted=0,popular=0,middle=0,rich=0;
        long total=0;
        int[] distance=new int[N],queue=new int[N];
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<H;y++) {
            int id=x*H+y;
            pollution[id]=Math.min(100,pollution[id]);
            greenery[id]=Math.min(12,greenery[id]);
            Tile t=city.tile(x,y);
            if(t.type!=TileType.RESIDENTIAL)continue;
            Arrays.fill(distance,-1);
            int head=0,tail=0;
            for(int road:CityRoads.entries(city,x,y)){distance[road]=0;queue[tail++]=road;}
            int traffic=0;
            while(head<tail) {
                int road=queue[head++],rx=road/H,ry=road%H;
                if(distance[road]<=2)traffic=Math.max(traffic,mobility.trafficAt(rx,ry));
                for(int d=0;d<4;d++) {
                    int nx=rx+(d==0?1:d==1?-1:0),ny=ry+(d==2?1:d==3?-1:0);
                    Tile near=city.tile(nx,ny);
                    if(near==null)continue;
                    if(near.type==TileType.COMMERCIAL && near.level>0 && mobility.workersAt(nx,ny)>0)
                        shops[id]=true;
                    if(near.type!=TileType.ROAD || distance[road]>=12)continue;
                    int next=nx*H+ny;
                    if(distance[next]<0) {distance[next]=distance[road]+1;queue[tail++]=next;}
                }
            }
            int employment=t.level==0?50:mobility.employedAt(x,y)*100/(t.level*12);
            int score=city.hasRoad(x,y)?45+employment*30/100+(shops[id]?10:0)+greenery[id]
                +services.bonusAt(city,x,y)-utilities.homePenalty(x,y)-pollution[id]/2-Math.min(15,Math.max(0,traffic-60)/8)
                -Math.max(0,city.taxRate(TileType.RESIDENTIAL)-10)*2
                -civics.sanitationPenalty(x,y)-Math.max(0,civics.crimeAt(x,y)-25)/3+civics.safetyBonus(x,y)
                +(mobility.transit.networkAt(x,y)>0?3:0)-(t.fireDays>0?35:0):15;
            satisfaction[id]=Math.max(5,Math.min(95,score));
            landValue[id]=Math.max(0,Math.min(100,satisfaction[id]+greenery[id]/2-pollution[id]/4+civics.safetyBonus(x,y)));
            wealth[id]=t.level>=3&&landValue[id]>=75?2:t.level>=2&&landValue[id]>=55?1:0;
            int residents=t.level*12;
            if(wealth[id]==2)rich+=residents;else if(wealth[id]==1)middle+=residents;else popular+=residents;
            people+=residents;total+=(long)residents*satisfaction[id];
            if(pollution[id]>=40)polluted+=residents;
            if(satisfaction[id]<40)unhappy+=residents;
        }
        averageSatisfaction=people==0?20:(int)(total/people);
        pollutedResidents=polluted;dissatisfiedResidents=unhappy;
        popularResidents=popular;middleResidents=middle;wealthyResidents=rich;
    }
    private static void spread(int[] values,int x,int y,int radius,int strength) {
        for(int nx=Math.max(0,x-radius);nx<=Math.min(City.WIDTH-1,x+radius);nx++)
            for(int ny=Math.max(0,y-radius);ny<=Math.min(H-1,y+radius);ny++) {
                int distance=Math.abs(nx-x)+Math.abs(ny-y);
                if(distance<=radius)values[nx*H+ny]+=(radius+1-distance)*strength;
            }
    }
    public static CityNeighborhoods analyze(City city,CityMobility mobility,CityServices services,CityUtilities utilities) {
        return new CityNeighborhoods(city,mobility,services,utilities,CityCivics.analyze(city,utilities,mobility));
    }
    public static CityNeighborhoods analyze(City c,CityMobility m,CityServices s,CityUtilities u,CityCivics v){return new CityNeighborhoods(c,m,s,u,v);}
    public int landValueAt(int x,int y){return landValue[x*H+y];}
    public int wealthAt(int x,int y){return wealth[x*H+y];}
    public String classAt(int x,int y){int c=wealthAt(x,y);return c==2?"ALTO PADRAO":c==1?"CLASSE MEDIA":"POPULAR";}
    public int pollutionAt(int x,int y) {return pollution[x*H+y];}
    public int satisfactionAt(int x,int y) {return satisfaction[x*H+y];}
    public int greeneryAt(int x,int y) {return greenery[x*H+y];}
    public boolean commerceAt(int x,int y) {return shops[x*H+y];}
    public String complaintAt(City city,CityMobility mobility,int x,int y) {
        Tile t=city.tile(x,y);
        if(t.fireDays>0)return "INCENDIO: PRECISA DE BOMBEIROS";
        if(!city.hasRoad(x,y))return "SEM RUA ADJACENTE";
        if(t.type!=TileType.RESIDENTIAL)return city.economy().mainConstraint(t.type,true);
        CityUtilities utilities=city.economy().utilities;
        if(utilities.powerEnabled && utilities.suppliedAt(0,x,y)<CityUtilities.need(t,0))return "ENERGIA INSUFICIENTE";
        if(utilities.waterEnabled && utilities.suppliedAt(1,x,y)<CityUtilities.need(t,1))return "AGUA INSUFICIENTE";
        CityCivics civics=city.economy().civics;
        if(civics.wasteEnabled&&civics.percent(TileType.WASTE_CENTER,x,y)<100)return "COLETA DE LIXO INSUFICIENTE";
        if(civics.sewageEnabled&&civics.percent(TileType.SEWAGE_PLANT,x,y)<100)return "ESGOTO SEM TRATAMENTO";
        if(civics.crimeAt(x,y)>=30)return "BAIRRO PRECISA DE POLICIA";
        if(pollutionAt(x,y)>=40)return "POLUICAO NO BAIRRO";
        if(t.level>0 && mobility.employedAt(x,y)<t.level*12)return "FALTAM EMPREGOS ACESSIVEIS";
        if(!commerceAt(x,y))return "FALTA COMERCIO CONECTADO";
        if(city.taxRate(TileType.RESIDENTIAL)>14)return "IMPOSTO RESIDENCIAL ALTO";
        if(t.level==2 && !city.economy().services.readyForDensity(city,x,y))return "FALTAM ESCOLA / SAUDE";
        if(satisfactionAt(x,y)<65)return "BAIRRO PRECISA MELHORAR";
        return "BAIRRO VALORIZADO";
    }
}
