package br.com.cidadeviva.app;

import br.com.cidadeviva.sim.City;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxNativesLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/** Exercises the actual input adapter and lifecycle without a GL context or device. */
public final class CityControlsTest {
    private static int checks;
    private static Field field(String name) throws Exception {
        Field f=CityGame.class.getDeclaredField(name);f.setAccessible(true);return f;
    }
    private static void put(CityGame game,String name,Object value) throws Exception {field(name).set(game,value);}
    private static Object get(CityGame game,String name) throws Exception {return field(name).get(game);}
    private static void check(boolean value,String message) {
        checks++;if(!value)throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        GdxNativesLoader.load();
        CityGame game=new CityGame();City city=new City();
        put(game,"city",city);put(game,"savesReady",true);
        put(game,"w",730f);put(game,"h",970f);put(game,"uiScale",1f);
        com.badlogic.gdx.Gdx.graphics=(com.badlogic.gdx.Graphics)java.lang.reflect.Proxy.newProxyInstance(
            com.badlogic.gdx.Graphics.class.getClassLoader(),new Class<?>[]{com.badlogic.gdx.Graphics.class},
            (proxy,method,values)->{
                if(method.getName().equals("getWidth"))return Math.round((float)get(game,"w"));
                if(method.getName().equals("getHeight"))return Math.round((float)get(game,"h"));
                throw new UnsupportedOperationException(method.getName());
            });
        OrthographicCamera camera=new OrthographicCamera(730,970);
        camera.position.set(80,-760,0);camera.zoom=0.7f;camera.update();
        put(game,"camera",camera);put(game,"uiProjection",new Matrix4());
        BitmapFont font=new BitmapFont(new BitmapFont.BitmapFontData(),new TextureRegion(),false);
        put(game,"font",font);
        Class<?> controls=Class.forName("br.com.cidadeviva.app.CityGame$Controls");
        Constructor<?> ctor=controls.getDeclaredConstructor(CityGame.class);ctor.setAccessible(true);
        InputAdapter input=(InputAdapter)ctor.newInstance(game);
        input.touchDown(300,400,0,0);input.touchDown(400,450,1,0);
        check((boolean)get(game,"pinchActive"),"two fingers begin pinch");
        game.resize(970,730);
        check(Math.abs(camera.position.x-80)<0.001 && Math.abs(camera.position.y+760)<0.001 &&
            Math.abs(camera.zoom-0.7f)<0.001,"rotation preserves camera focus and zoom");
        check(!((boolean[])get(game,"down"))[0] && !((boolean[])get(game,"down"))[1],"rotation clears stale fingers");
        int money=city.money();input.touchUp(300,400,0,0);input.touchUp(400,450,1,0);
        check(city.money()==money,"old finger release after rotation never builds");
        input.keyDown(Input.Keys.E);
        check((boolean)get(game,"economyVisible"),"keyboard opens economy");
        input.touchDown(10,10,0,0);input.touchDragged(100,100,0);input.touchDragged(10,10,0);
        input.touchUp(10,10,0,0);
        check((boolean)get(game,"economyVisible"),"drag away and back on modal is not a click");
        float zoom=camera.zoom;input.scrolled(0,1);
        check(camera.zoom==zoom,"wheel cannot move camera through modal");
        input.touchDown(10,10,0,0);input.touchUp(10,10,0,0);
        check(!(boolean)get(game,"economyVisible"),"stationary outside tap closes economy");
        input.touchDown(400,300,0,0);input.touchCancelled(400,300,0,0);input.touchUp(400,300,0,0);
        check(city.money()==money,"cancelled gesture cannot build on release");
        input.touchDown(400,300,0,0);input.keyDown(Input.Keys.E);
        check(!((boolean[])get(game,"down"))[0],"opening overlay interrupts map gesture");
        input.keyDown(Input.Keys.BACK);put(game,"helpVisible",true);
        input.keyDown(Input.Keys.BACK);
        check(!(boolean)get(game,"helpVisible") && !(boolean)get(game,"slotsVisible"),"back closes help before opening cities");
        input.touchDown(400,300,0,0);put(game,"savesReady",false);game.pause();
        check(!((boolean[])get(game,"down"))[0],"backgrounding clears interrupted gesture");
        put(game,"savesReady",true);
        game.resize(730,970);camera.position.set(0,-768,0);camera.zoom=1;camera.update();
        put(game,"paintMode",true);
        input.touchDown(365,389,0,0);input.touchDragged(397,405,0);
        check((boolean)get(game,"brushGesture"),"road painting starts on map");
        input.touchUp(120,210,0,0);
        check(city.money()==money,"release over action dock cancels construction");
        camera.position.set(1500,0,0);camera.update();
        input.touchDown(365,400,0,0);
        check((boolean)get(game,"suppressTap"),"paint beginning outside map is rejected");
        input.touchCancelled(365,400,0,0);
        java.io.File dir=java.nio.file.Files.createTempDirectory("city-controls").toFile();
        try {
            br.com.cidadeviva.storage.CitySlots slots=new br.com.cidadeviva.storage.CitySlots(dir);
            slots.save(city);put(game,"saves",slots);put(game,"simClock",2f);
            put(game,"inspectorVisible",true);put(game,"hoverX",21);put(game,"hoverY",23);
            java.lang.reflect.Method load=CityGame.class.getDeclaredMethod("load",boolean.class);
            load.setAccessible(true);load.invoke(game,false);
            check((float)get(game,"simClock")==0f && !(boolean)get(game,"inspectorVisible"),
                "loading discards old partial day and tile inspector");
            // The actual economy input route must require selection then confirmation.
            input.keyDown(Input.Keys.E);
            java.lang.reflect.Method economyTap=CityGame.class.getDeclaredMethod("economyTap",int.class,int.class);
            economyTap.setAccessible(true);
            float loanScale=(float)get(game,"uiScale");
            java.lang.reflect.Method econY=CityGame.class.getDeclaredMethod("econY",float.class);econY.setAccessible(true);
            float ey=(float)econY.invoke(game,loanScale);
            int footerY=Math.round((float)get(game,"h")-ey-35*loanScale);
            City borrowingCity=(City)get(game,"city");
            int initialCash=borrowingCity.money();
            economyTap.invoke(game,200,footerY);
            check((boolean)get(game,"loansPage"),"ECON opens loan offers");
            economyTap.invoke(game,500,footerY);
            check(borrowingCity.money()==initialCash,"confirm without selection cannot borrow");
            int firstOfferY=Math.round((float)get(game,"h")-ey-340*loanScale);
            economyTap.invoke(game,365,firstOfferY);
            check((int)get(game,"selectedLoan")==0&&borrowingCity.money()==initialCash,"selecting offer alone never contracts debt");
            economyTap.invoke(game,500,footerY);
            check(borrowingCity.money()==initialCash+3000&&borrowingCity.loanBalance()==3300,"confirmation contracts the selected offer exactly once");
            check(slots.store(slots.active()).load().city.loanBalance()==3300,"contract persists immediately");
            economyTap.invoke(game,500,footerY);
            check(borrowingCity.money()==initialCash+3000,"duplicate tap never borrows twice");
            input.keyDown(Input.Keys.BACK);
            check((boolean)get(game,"economyVisible")&&!(boolean)get(game,"loansPage"),"back returns from loan offers to budget");
            input.keyDown(Input.Keys.BACK);
            input.keyDown(Input.Keys.E);
            int layersHeaderY=Math.round((float)get(game,"h")-ey-519*loanScale);
            economyTap.invoke(game,550,layersHeaderY);
            check((boolean)get(game,"layersPage"),"budget opens layer selector");
            economyTap.invoke(game,365,Math.round((float)get(game,"h")-ey-85*loanScale));
            check(borrowingCity.firesEnabled()&&slots.store(slots.active()).load().city.firesEnabled(),"optional fires switch persists immediately");
            economyTap.invoke(game,365,Math.round((float)get(game,"h")-ey-85*loanScale));
            input.keyDown(Input.Keys.BACK);input.keyDown(Input.Keys.BACK);
        } finally {
            try(java.util.stream.Stream<java.nio.file.Path> files=java.nio.file.Files.walk(dir.toPath())) {
                files.sorted(java.util.Comparator.reverseOrder()).forEach(path->{
                    try{java.nio.file.Files.delete(path);}catch(java.io.IOException e){throw new RuntimeException(e);}
                });
            }
        }
        put(game,"savesReady",true);input.keyDown(Input.Keys.NUM_6);
        check((boolean)get(game,"servicesVisible"),"service selector opens");
        zoom=camera.zoom;input.scrolled(0,1);
        check(camera.zoom==zoom,"service dialog blocks camera wheel");
        // 730x970, scale 1: school row centered at world UI y=485-200+150+36.
        input.touchDown(365,413,0,0);input.touchUp(365,413,0,0);
        check(!(boolean)get(game,"servicesVisible") && get(game,"selected")==br.com.cidadeviva.sim.TileType.SCHOOL,
            "school selection updates active build tool");
        check(!(boolean)get(game,"paintMode"),"service choice starts with single-lot placement");
        input.keyDown(Input.Keys.NUM_6);input.keyDown(Input.Keys.BACK);
        check(!(boolean)get(game,"servicesVisible"),"back closes service dialog");
        input.keyDown(Input.Keys.NUM_6);input.touchDown(365,581,0,0);input.touchUp(365,581,0,0);
        check(get(game,"selected")==br.com.cidadeviva.sim.TileType.POWER_PLANT,"generator can be selected");
        input.keyDown(Input.Keys.NUM_6);input.touchDown(365,665,0,0);input.touchUp(365,665,0,0);
        check(get(game,"selected")==br.com.cidadeviva.sim.TileType.WATER_PUMP,"water source can be selected");
        for(int i=0;i<6;i++){
            input.keyDown(Input.Keys.NUM_6);
            input.touchDown(500,735,0,0);input.touchUp(500,735,0,0);
            check((int)get(game,"servicePage")==1,"second service page opens");
            int sy=970-(205+408-i*68+30);
            input.touchDown(365,sy,0,0);input.touchUp(365,sy,0,0);
            check(get(game,"selected")==br.com.cidadeviva.sim.CityServices.BUILD_MENU[5+i],"new civic service selected from its own row");
        }
        for(int[] size:new int[][]{{730,970},{970,730},{640,360}}){
            game.resize(size[0],size[1]);float scale=(float)get(game,"uiScale"),ew=Math.min(size[0]-24*scale,560*scale),ex=(size[0]-ew)/2,ey=(size[1]-558*scale)/2;
            for(int i=0;i<br.com.cidadeviva.sim.CityLayer.values().length;i++){
                input.keyDown(Input.Keys.E);
                int headerX=Math.round(ex+ew-80*scale),headerY=Math.round(size[1]-ey-519*scale);
                input.touchDown(headerX,headerY,0,0);input.touchUp(headerX,headerY,0,0);
                check((boolean)get(game,"layersPage"),"layer menu accessible in portrait and landscape");
                float cw=(ew-42*scale)/2;
                int lx=Math.round(ex+16*scale+(i%2)*(cw+10*scale)+cw/2),ly=Math.round(size[1]-ey-(410-i/2*52+22)*scale);
                int funds=((City)get(game,"city")).money();
                input.touchDown(lx,ly,0,0);input.touchUp(lx,ly,0,0);
                check(get(game,"currentLayer")==br.com.cidadeviva.sim.CityLayer.values()[i]&&!(boolean)get(game,"economyVisible"),"layer choice shows map without touch leaking through");
                check(((City)get(game,"city")).money()==funds,"diagnostic selection never changes treasury");
            }
        }
        font.dispose();
        System.out.println("OK: "+checks+" actual controls and camera/lifecycle regressions.");
    }
}
