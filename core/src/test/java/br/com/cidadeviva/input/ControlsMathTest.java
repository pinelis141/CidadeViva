package br.com.cidadeviva.input;

/** Deterministic test for mapping touch regions, grid selection and camera anchoring. */
public final class ControlsMathTest {
    private static int checks = 0;
    private static void ok(boolean condition, String label) {
        checks++;
        if (!condition) throw new AssertionError(label);
    }
    private static void near(float actual,float expected,String label) {
        ok(Math.abs(actual-expected)<0.0001f,label + " expected " +expected+" got "+actual);
    }
    public static void main(String[] unused) {
        for (float[] size:new float[][] {{360,800},{412,915},{1080,2400},{2400,1080},{730,970}}) {
            float w=size[0],h=size[1],u=HudLayout.scale(w,h);
            for (int i=0;i<5;i++) {
                float cx=(i+0.5f)*w/5f;
                ok(HudLayout.actionAt(cx,95*u,w,u)==i,"ação central "+i);
                ok(HudLayout.toolAt(cx,h-40*u,w,h,u)==i,"ferramenta central "+i);
                ok(HudLayout.isHud(cx,95*u,w,h,u),"faixa superior protegida");
                ok(HudLayout.isHud(cx,h-40*u,w,h,u),"faixa inferior protegida");
            }
            ok(HudLayout.actionAt(-2,95*u,w,u)==-1,"nunca selecionar fora à esquerda");
            ok(HudLayout.toolAt(w+3,h-40*u,w,h,u)==-1,"nunca selecionar fora à direita");
            ok(HudLayout.actionAt(w*0.5f,40*u,w,u)==-1,"clique na estatística sem ação");
            ok(HudLayout.toolAt(w*0.5f,h-120*u,w,h,u)==-1,"clique fora do rodapé sem ferramenta");
            ok(!HudLayout.isHud(w/2f,h/2f,w,h,u),"centro é mapa");
        }
        for (int x=0;x<48;x++) for (int y=0;y<48;y++) {
            int[] point=IsoGrid.pick((x-y)*32f,-(x+y)*16f,32,16);
            ok(point[0]==x && point[1]==y,"seleção de tile "+x+","+y);
        }
        float[] anchor=IsoGrid.anchoredCamera(12,-80,1f,0.8f,220,340,220,340,360,800);
        near(anchor[0]+(220-180)*0.8f,12+(220-180),"âncora zoom X");
        near(anchor[1]+(400-340)*0.8f,-80+(400-340),"âncora zoom Y");
        float[] dragging=IsoGrid.anchoredCamera(0,0,1f,1f,180,400,205,430,360,800);
        near(dragging[0],-25f,"pan midpoint X");
        near(dragging[1],30f,"pan midpoint Y");
        near(IsoGrid.bound(500,-300,300,200),200,"limite câmera direito");
        near(IsoGrid.bound(-500,-300,300,200),-200,"limite câmera esquerdo");
        near(IsoGrid.bound(500,-300,300,700),0,"tela maior que mapa centraliza");
        System.out.println("OK: "+checks+" verificações de HUD, isometria e câmera.");
    }
}
