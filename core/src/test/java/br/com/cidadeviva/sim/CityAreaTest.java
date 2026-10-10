package br.com.cidadeviva.sim;

import br.com.cidadeviva.input.BuildStroke;
import java.util.Arrays;
import java.util.List;

public final class CityAreaTest {
    private static int checks;
    private static void ok(boolean test,String label) {
        checks++;
        if (!test)throw new AssertionError(label);
    }
    public static void main(String[] args) {
        City city=new City();
        List<BuildStroke.Cell> cells=Arrays.asList(
            new BuildStroke.Cell(16,16), new BuildStroke.Cell(17,16),
            new BuildStroke.Cell(17,16), new BuildStroke.Cell(-1,99));
        int before=city.money();
        City.BuildResult preview=city.preview(cells,TileType.ROAD);
        ok(preview.built==2,"preview counts valid unique tiles");
        ok(preview.ignored==1,"preview skips invalid tile");
        ok(preview.cost==40,"preview computes exact cost");
        ok(before==city.money(),"preview has no side effects");
        City.BuildResult done=city.buildArea(cells,TileType.ROAD);
        ok(done.built==2 && done.cost==40,"batch builds two streets");
        ok(city.money()==before-40,"batch is charged exactly once");
        ok(city.tile(16,16).type==TileType.ROAD,"street built");
        ok(city.tile(17,16).type==TileType.ROAD,"adjacent street built");
        ok(city.roadMask(16,16)==1,"road mask right");
        ok(city.roadMask(17,16)==2,"road mask left");
        City.BuildResult repeats=city.buildArea(cells,TileType.ROAD);
        ok(repeats.built==0 && city.money()==before-40,"duplicate batch free");

        // Existing roads cannot become zoned without explicit demolition.
        List<BuildStroke.Cell> zone=Arrays.asList(
            new BuildStroke.Cell(16,16),new BuildStroke.Cell(16,17));
        City.BuildResult partly=city.buildArea(zone,TileType.RESIDENTIAL);
        ok(partly.built==1 && partly.ignored==1,"blocked tile skipped, eligible tile built");
        ok(city.tile(16,16).type==TileType.ROAD,"no road overwrite");
        ok(city.tile(16,17).type==TileType.RESIDENTIAL,"zone built");

        City poor=new City();
        List<BuildStroke.Cell> expensive=BuildStroke.rectangle(30,30,45,45);
        int oldMoney=poor.money();
        City.BuildResult tooExpensive=poor.buildArea(expensive,TileType.INDUSTRIAL);
        ok(tooExpensive.insufficientFunds,"reject unaffordable rectangle");
        ok(tooExpensive.built==256,"show requested count before charge");
        ok(poor.money()==oldMoney,"no money deducted for rejected action");
        ok(poor.tile(30,30).type!=TileType.INDUSTRIAL,"no partial construction");
        ok(poor.tile(45,45).type!=TileType.INDUSTRIAL,"no partial construction at edge");
        System.out.println("OK: "+checks+" verificações de custos, área, ruas e transações.");
    }
}
