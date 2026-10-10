package br.com.cidadeviva.sim;

/**
 * Immutable daily economic report. It is derived entirely from the persisted city tiles,
 * so old CVT1/CVT2 saves remain readable without adding new mandatory fields.
 *
 * Demand is not a prediction of how many citizens will arrive: it is an interpretable
 * pressure score (0..100) used as one factor for each zone's next growth decision.
 */
public final class CityEconomy {
    public final int population;
    public final int jobs;
    public final int commercialJobs;
    public final int industrialJobs;
    public final int employed;
    public final int unemployed;
    public final int jobVacancies;
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

    private CityEconomy(int population, int jobs, int commercialJobs, int industrialJobs,
                        int residentialDemand, int commercialDemand, int industrialDemand,
                        int taxResidential, int taxCommercial, int taxIndustrial,
                        int roadMaintenance, int zoneMaintenance, int unserved,
                        int residentialTaxRate,int commercialTaxRate,int industrialTaxRate) {
        this.population = population;
        this.jobs = jobs;
        this.commercialJobs = commercialJobs;
        this.industrialJobs = industrialJobs;
        this.employed = Math.min(population, jobs);
        this.unemployed = Math.max(0, population - jobs);
        this.jobVacancies = Math.max(0, jobs - population);
        this.residentialDemand = residentialDemand;
        this.commercialDemand = commercialDemand;
        this.industrialDemand = industrialDemand;
        this.taxResidential = taxResidential;
        this.taxCommercial = taxCommercial;
        this.taxIndustrial = taxIndustrial;
        this.grossRevenue = taxResidential + taxCommercial + taxIndustrial;
        this.roadMaintenance = roadMaintenance;
        this.zoneMaintenance = zoneMaintenance;
        this.expenses = roadMaintenance + zoneMaintenance;
        this.net = grossRevenue - expenses;
        this.unserved = unserved;
        this.residentialTaxRate=residentialTaxRate;
        this.commercialTaxRate=commercialTaxRate;
        this.industrialTaxRate=industrialTaxRate;
    }

    public int demand(TileType type) {
        if (type == TileType.RESIDENTIAL) return residentialDemand;
        if (type == TileType.COMMERCIAL) return commercialDemand;
        if (type == TileType.INDUSTRIAL) return industrialDemand;
        return 0;
    }

    public String mainConstraint(TileType type, boolean hasRoad) {
        if (!hasRoad) return "SEM RUA ADJACENTE";
        if (demand(type) < 24) {
            if (type == TileType.RESIDENTIAL) return "POUCAS VAGAS DE EMPREGO";
            if (type == TileType.COMMERCIAL) return "DEMANDA DE LOJAS BAIXA";
            return "DEMANDA INDUSTRIAL BAIXA";
        }
        return "DEMANDA "+demand(type)+"/100";
    }

    public static CityEconomy analyze(City city) {
        int population = 0, commerce = 0, industry = 0, roads = 0, zoneCount = 0;
        int unusedResidential = 0, unusedCommercial = 0, unusedIndustrial = 0, unserved = 0;
        for (int x = 0; x < City.WIDTH; x++) for (int y = 0; y < City.HEIGHT; y++) {
            Tile tile = city.tile(x, y);
            if (tile.type == TileType.ROAD) { roads++; continue; }
            if (!tile.type.isZone()) continue;
            zoneCount++;
            if (!city.hasRoad(x, y)) unserved++;
            switch (tile.type) {
                case RESIDENTIAL:
                    population += tile.level * 12;
                    if (tile.level == 0 && city.hasRoad(x,y)) unusedResidential++;
                    break;
                case COMMERCIAL:
                    commerce += tile.level * 10;
                    if (tile.level == 0 && city.hasRoad(x,y)) unusedCommercial++;
                    break;
                case INDUSTRIAL:
                    industry += tile.level * 18;
                    if (tile.level == 0 && city.hasRoad(x,y)) unusedIndustrial++;
                    break;
                default: break;
            }
        }
        int jobs = commerce + industry;
        // Pressure responds to underemployment, unfilled employment capacity and
        // undeveloped zoned land. It prevents limitless exponential construction.
        int rateR=city.taxRate(TileType.RESIDENTIAL);
        int rateC=city.taxRate(TileType.COMMERCIAL);
        int rateI=city.taxRate(TileType.INDUSTRIAL);
        // A 10% tax matches the prior version; each extra percentage point
        // reduces investment appetite by three demand points.
        int residential = clamp(42 + (jobs - population) / 3 - unusedResidential * 8 - 3*(rateR-10), 5, 95);
        int commercial = clamp(42 + (population - commerce * 2) / 5 - unusedCommercial * 9 - 3*(rateC-10), 5, 95);
        int industrial = clamp(46 + (population - jobs) / 3 - unusedIndustrial * 10 - 3*(rateI-10), 5, 95);
        int taxR = (int)((long)population * 3 * rateR / 50); // base 0.6 at 10%
        int taxC = (int)((long)commerce * rateC / 10); // base 1.0 at 10%
        int taxI = (int)((long)industry * 6 * rateI / 50); // base 1.2 at 10%
        int roadCost = roads * 2;
        int zoneCost = zoneCount;
        return new CityEconomy(population, jobs, commerce, industry,
            residential, commercial, industrial, taxR, taxC, taxI,
            roadCost, zoneCost, unserved,rateR,rateC,rateI);
    }

    private static int clamp(int value, int low, int high) {
        return Math.min(high, Math.max(low, value));
    }
}
