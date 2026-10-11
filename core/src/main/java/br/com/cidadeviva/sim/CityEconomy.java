package br.com.cidadeviva.sim;

/**
 * Daily economy derived only from CVT3-persisted tiles, treasury and tax policy.
 * No hidden simulation counters: reloading the same city produces the same result.
 *
 * Jobs are CAPACITY, not automatically taxable employment. An occupied building
 * contributes jobs only while it has adjacent road access. Employees are shared
 * proportionately between commercial and industrial establishments.
 */
public final class CityEconomy {
    public final int population;
    public final int jobs;
    public final int commercialJobs;
    public final int industrialJobs;
    public final int employed;
    public final int unemployed;
    public final int jobVacancies;
    public final int operationalJobs;
    public final int inactiveJobs;
    public final int accessiblePopulation;
    public final int qualityOfLife;
    public final int residentialDemand;
    public final int commercialDemand;
    public final int industrialDemand;
    public final int taxResidential;
    public final int taxCommercial;
    public final int taxIndustrial;
    public final int grossRevenue;
    public final int roadMaintenance;
    public final int zoneMaintenance;
    public final int expenses;
    public final int net;
    public final int unserved;
    public final int residentialTaxRate, commercialTaxRate, industrialTaxRate;

    private CityEconomy(int population, int jobs, int commerce, int industry,
                        int accessiblePopulation, int operationalJobs,
                        int residentialDemand, int commercialDemand, int industrialDemand,
                        int taxResidential, int taxCommercial, int taxIndustrial,
                        int roadMaintenance, int zoneMaintenance, int unserved,
                        int rateR, int rateC, int rateI, int qualityOfLife) {
        this.population=population;
        this.jobs=jobs;
        this.commercialJobs=commerce;
        this.industrialJobs=industry;
        this.accessiblePopulation=accessiblePopulation;
        this.operationalJobs=operationalJobs;
        this.inactiveJobs=jobs-operationalJobs;
        this.employed=Math.min(accessiblePopulation,operationalJobs);
        this.unemployed=population-employed;
        this.jobVacancies=Math.max(0,operationalJobs-employed);
        this.residentialDemand=residentialDemand;
        this.commercialDemand=commercialDemand;
        this.industrialDemand=industrialDemand;
        this.taxResidential=taxResidential;
        this.taxCommercial=taxCommercial;
        this.taxIndustrial=taxIndustrial;
        this.grossRevenue=taxResidential+taxCommercial+taxIndustrial;
        this.roadMaintenance=roadMaintenance;
        this.zoneMaintenance=zoneMaintenance;
        this.expenses=roadMaintenance+zoneMaintenance;
        this.net=grossRevenue-expenses;
        this.unserved=unserved;
        this.residentialTaxRate=rateR;
        this.commercialTaxRate=rateC;
        this.industrialTaxRate=rateI;
        this.qualityOfLife=qualityOfLife;
    }

    public int demand(TileType type) {
        if(type==TileType.RESIDENTIAL)return residentialDemand;
        if(type==TileType.COMMERCIAL)return commercialDemand;
        if(type==TileType.INDUSTRIAL)return industrialDemand;
        return 0;
    }

    public String mainConstraint(TileType type,boolean hasRoad) {
        if(!hasRoad)return "SEM RUA ADJACENTE";
        if(demand(type)<24) {
            if(type==TileType.RESIDENTIAL)
                return jobVacancies==0 && unemployed>0 ? "FALTAM EMPREGOS" : "OFERTA OU IMPOSTO ALTO";
            if(type==TileType.COMMERCIAL)return "COMERCIO SATURADO / IMPOSTO";
            return "INDUSTRIA SATURADA / IMPOSTO";
        }
        if(qualityOfLife<40)return "QUALIDADE URBANA BAIXA";
        return "DEMANDA "+demand(type)+"/100";
    }

    /** Tax rate returns relative yield with diminishing returns above 10%. */
    private static int taxYield(int base,int rate) {
        int compliance=100-6*Math.max(0,rate-10);
        return (int)(((long)base*rate*compliance)/(100L*10L));
    }
    private static int clamp(int value,int min,int max) {
        return Math.max(min,Math.min(max,value));
    }
    private static int pressure(int numerator,int denominator,int strength) {
        return (int)Math.round((double)numerator*strength/Math.max(100,denominator));
    }
    private static int supplyPenalty(int vacantLots) {
        return (int)Math.round(3.0*Math.sqrt(vacantLots));
    }

    public static CityEconomy analyze(City city) {
        int population=0, commerce=0, industry=0;
        int accessiblePopulation=0, activeCommerce=0, activeIndustry=0;
        int roads=0, zones=0, emptyR=0,emptyC=0,emptyI=0, unserved=0;
        int buildingMaintenance=0;
        for(int x=0;x<City.WIDTH;x++)for(int y=0;y<City.HEIGHT;y++) {
            Tile t=city.tile(x,y);
            if(t.type==TileType.ROAD){roads++;continue;}
            if(!t.type.isZone())continue;
            zones++;
            boolean access=city.hasRoad(x,y);
            if(!access)unserved++;
            switch(t.type) {
                case RESIDENTIAL:
                    population+=t.level*12;
                    if(access)accessiblePopulation+=t.level*12;
                    if(t.level==0&&access)emptyR++;
                    buildingMaintenance+=4*t.level*t.level;
                    break;
                case COMMERCIAL:
                    commerce+=t.level*10;
                    if(access)activeCommerce+=t.level*10;
                    if(t.level==0&&access)emptyC++;
                    buildingMaintenance+=6*t.level*t.level;
                    break;
                case INDUSTRIAL:
                    industry+=t.level*18;
                    if(access)activeIndustry+=t.level*18;
                    if(t.level==0&&access)emptyI++;
                    buildingMaintenance+=9*t.level*t.level;
                    break;
                default:break;
            }
        }
        int activeJobs=activeCommerce+activeIndustry;
        int employed=Math.min(accessiblePopulation,activeJobs);
        int employedCommerce=activeJobs==0?0:Math.min(activeCommerce, (int)Math.round((double)employed*activeCommerce/activeJobs));
        int employedIndustry=employed-employedCommerce;
        int rateR=city.taxRate(TileType.RESIDENTIAL);
        int rateC=city.taxRate(TileType.COMMERCIAL);
        int rateI=city.taxRate(TileType.INDUSTRIAL);
        // An unemployed household contributes less than a working household;
        // inaccessible homes contribute no taxes. Unstaffed workplaces pay none.
        int residentialBase=(employed*105 + (accessiblePopulation-employed)*30)/100;
        int rTax=taxYield(residentialBase,rateR);
        int cTax=taxYield(employedCommerce*2,rateC);
        int iTax=taxYield(employedIndustry*2,rateI);
        int roadCost=roads*3;
        int zoneCost=zones*2+buildingMaintenance;
        int net=rTax+cTax+iTax-roadCost-zoneCost;
        int employmentRatio=population==0?100:(int)((long)employed*100/population);
        int accessRatio=zones==0?100:100-(int)((long)unserved*100/zones);
        int quality=population==0?20:clamp(20 + employmentRatio*40/100 + accessRatio*25/100
            + (net>=0?10:0) - Math.max(0,rateR-10)*2
            - (city.money()==0 && net<0?14:0),5,95);
        // Ratios prevent an enormous city from reaching demand limits merely
        // because it has more absolute residents than a starter town.
        int taxPenaltyR=2*(rateR-10);
        int taxPenaltyC=2*(rateC-10);
        int taxPenaltyI=2*(rateI-10);
        int fiscalPressure=net<0 && city.money()<1500?12:0;
        int residential=clamp(52+pressure(activeJobs-population,population,40)
            -supplyPenalty(emptyR)-taxPenaltyR-(quality<45?(45-quality)/2:0)
            -fiscalPressure,5,95);
        int commercial=clamp(45+pressure(accessiblePopulation-2*activeCommerce,population,35)
            -supplyPenalty(emptyC)-taxPenaltyC-fiscalPressure,5,95);
        int industrial=clamp(45+pressure(accessiblePopulation-activeJobs,population,32)
            -supplyPenalty(emptyI)-taxPenaltyI-fiscalPressure,5,95);
        return new CityEconomy(population,commerce+industry,commerce,industry,
            accessiblePopulation,activeJobs,residential,commercial,industrial,
            rTax,cTax,iTax,roadCost,zoneCost,unserved,rateR,rateC,rateI,quality);
    }
}
