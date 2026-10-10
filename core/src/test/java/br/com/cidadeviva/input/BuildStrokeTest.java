package br.com.cidadeviva.input;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BuildStrokeTest {
    private static int checks;
    private static void ok(boolean condition, String label) {
        checks++;
        if (!condition) throw new AssertionError(label);
    }
    private static void verifyLine(int x0,int y0,int x1,int y1) {
        List<BuildStroke.Cell> line=BuildStroke.line(x0,y0,x1,y1);
        ok(line.get(0).equals(new BuildStroke.Cell(x0,y0)),"road start");
        ok(line.get(line.size()-1).equals(new BuildStroke.Cell(x1,y1)),"road end");
        ok(new HashSet<>(line).size()==line.size(),"no road duplicates");
        for (int i=1;i<line.size();i++) {
            BuildStroke.Cell a=line.get(i-1),b=line.get(i);
            ok(Math.abs(a.x-b.x)+Math.abs(a.y-b.y)==1,"four-connected road");
        }
    }
    public static void main(String[] args) {
        verifyLine(2,2,2,8);
        verifyLine(2,2,8,2);
        verifyLine(3,5,13,12);
        verifyLine(13,12,3,5);
        verifyLine(1,1,13,3);
        verifyLine(18,22,7,16);
        List<BuildStroke.Cell> single=BuildStroke.line(5,5,5,5);
        ok(single.size()==1,"single tile gesture");
        ok(BuildStroke.rectangle(8,8,10,12).size()==15,"rectangle inclusive");
        ok(BuildStroke.rectangle(10,12,8,8).size()==15,"reverse rectangle");
        ok(BuildStroke.rectangle(0,0,99,99).size()==256,"area capped to 16x16");
        ok(BuildStroke.rectangle(0,0,-99,-99).size()==256,"negative area capped");
        Set<BuildStroke.Cell> unique=new HashSet<>(BuildStroke.rectangle(4,4,7,8));
        ok(unique.size()==20,"rectangle without duplicates");
        System.out.println("OK: "+checks+" verificações de pincel, linhas e áreas.");
    }
}
