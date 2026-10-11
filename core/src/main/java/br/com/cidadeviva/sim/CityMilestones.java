package br.com.cidadeviva.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Population achievements computed from actual daily transitions, no new save fields. */
public final class CityMilestones {
    private static final int[] GOALS={100,250,500,1000,2500,5000,10000,25000,50000};
    private CityMilestones() { }

    public static final class Milestone {
        public final int population;
        public final String title;
        public final String detail;
        private Milestone(int population,String title,String detail) {
            this.population=population;this.title=title;this.detail=detail;
        }
    }

    public static int nextGoal(int population) {
        for(int goal:GOALS)if(population<goal)return goal;
        return 0;
    }

    public static List<Milestone> crossed(int previousPopulation,int currentPopulation) {
        if(currentPopulation<=previousPopulation)return Collections.emptyList();
        ArrayList<Milestone> unlocked=new ArrayList<>();
        for(int goal:GOALS) {
            if(previousPopulation<goal && currentPopulation>=goal) {
                String headline=goal<500?"BAIRRO EM CRESCIMENTO":
                    goal<2500?"CIDADE EM EXPANSAO":"GRANDE CONQUISTA URBANA";
                unlocked.add(new Milestone(goal,headline,goal+" moradores! Sua cidade esta evoluindo."));
            }
        }
        return Collections.unmodifiableList(unlocked);
    }
}
