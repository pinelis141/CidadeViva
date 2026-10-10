package br.com.cidadeviva.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/** Testes sem dependência de Gradle, Android ou libGDX. */
public final class CitySmokeTest {
    private static void verify(boolean ok,String message) {
        if(!ok)throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        City city=new City();
        verify(city.money()==15000,"caixa inicial");
        verify(city.residents()>0,"população inicial");
        verify(city.roads()>0,"ruas iniciais");
        verify(city.build(0,31,TileType.ROAD).equals("Não pode construir na água") ||
            city.tile(0,31).type != TileType.WATER,"sem ruas sobre a água");
        // Construir e remover fora do centro sem tocar em construções existentes.
        int beforeBuild=city.money();
        verify(city.build(20,20,TileType.ROAD).equals("Construído"),"rua válida");
        verify(city.money()==beforeBuild-20,"cobra custo da rua");
        verify(city.tile(20,20).type==TileType.ROAD,"rua construída");
        int paid=city.money();
        verify(city.build(20,20,TileType.ROAD).equals("Já construído"),"bloqueia custo duplicado");
        verify(city.money()==paid,"sem cobrar por operação duplicada");
        verify(city.build(-1,0,TileType.ROAD).equals("Fora do mapa"),"impede fora do mapa");
        verify(city.build(48,2,TileType.ROAD).equals("Fora do mapa"),"impede maior que mapa");
        verify(city.build(20,20,TileType.COMMERCIAL).equals("Remova a estrutura antes"),"bloqueia construção sobre rua");
        verify(city.build(0,47,TileType.WATER).equals("Construção inválida"),"tipo proibido");
        verify(city.build(20,20,TileType.GRASS).equals("Terreno liberado"),"remover rua");
        int oldDay=city.day();
        city.nextDay();
        verify(city.day()==oldDay+1,"simulação");
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        CityPersistence.save(city,out);
        City loaded=CityPersistence.load(new ByteArrayInputStream(out.toByteArray()));
        verify(city.money()==loaded.money(),"saldo salva");
        verify(city.day()==loaded.day(),"dia salva");
        verify(city.residents()==loaded.residents(),"mapa salva");
        verify(city.tile(20,20).type==loaded.tile(20,20).type,"demolição persiste");
        System.out.println("OK: verificações básicas de construção, economia e salvamento.");
    }
}
