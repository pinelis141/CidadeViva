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
            for (int i=0;i<HudLayout.ACTIONS;i++) {
                float cx=(i+0.5f)*w/HudLayout.ACTIONS;
                ok(HudLayout.actionAt(cx,(HudLayout.ACTION_TOP+HudLayout.ACTION_HEIGHT/2f)*u,w,u)==i,"ação central "+i);
                ok(HudLayout.isHud(cx,(HudLayout.ACTION_TOP+HudLayout.ACTION_HEIGHT/2f)*u,w,h,u),"faixa superior protegida");
            }
            for (int i=0;i<HudLayout.TOOLS;i++) {
                float cx=(i+0.5f)*w/HudLayout.TOOLS;
                ok(HudLayout.toolAt(cx,h-50*u,w,h,u)==i,"ferramenta central "+i);
                ok(HudLayout.isHud(cx,h-50*u,w,h,u),"faixa inferior protegida");
            }
            for(int k=0;k<3;k++) {
                float bx=w-(k==0?302:k==1?182:61)*u;
                ok(HudLayout.quickActionAt(bx,25*u,w,u)==k,"acao superior "+k);
                ok(HudLayout.isHud(bx,25*u,w,h,u),"acao superior protegida");
            }
            ok(HudLayout.quickActionAt(10*u,25*u,w,u)==-1,"estatisticas sem botao lateral");
            ok(HudLayout.actionAt(-2,(HudLayout.ACTION_TOP+HudLayout.ACTION_HEIGHT/2f)*u,w,u)==-1,"nunca selecionar fora à esquerda");
            ok(HudLayout.toolAt(w+3,h-50*u,w,h,u)==-1,"nunca selecionar fora à direita");
            ok(HudLayout.actionAt(w*0.5f,40*u,w,u)==-1,"clique na estatística sem ação");
            ok(HudLayout.toolAt(w*0.5f,h-(HudLayout.BOTTOM_HEIGHT+6)*u,w,h,u)==-1,"clique fora do rodapé sem ferramenta");
            ok(!HudLayout.isHud(w/2f,h/2f,w,h,u),"centro é mapa");
            float inspectorY=h-(HudLayout.BOTTOM_HEIGHT+36)*u;
            ok(HudLayout.inspectorAt(w/2f,inspectorY,w,h,u),"painel de lote tem area de toque");
            ok(!HudLayout.isHud(w/2f,inspectorY,w,h,u),"painel flutuante fora do HUD fixo");
            ok(!HudLayout.inspectorAt(w/2f,h*0.5f,w,h,u),"o mapa nao e painel contextual");
            ok(HudLayout.isHud(w/2f,75*u,w,h,u),"cartao de status protegido");
            ok(HudLayout.isHud(w/2f,150*u,w,h,u),"demanda protegida");
            ok(HudLayout.isHud(w/2f,180*u,w,h,u),"alertas protegidos");
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
