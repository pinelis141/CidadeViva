# Cidade Viva — Java / libGDX / Android

**Versão de testes: 0.2.0-alpha2** — interface corrigida, indicadores de zonas sem rua e primeiros sprites pixel art originais.

City builder isométrico original, offline. **Não utiliza Godot.** O repositório do Pixel Chess é separado e não deve ser modificado por este projeto.

## 0.2.0-alpha1 — construção em área (prévia funcional)

- Novo **modo PINTAR/MOVER**, alternado pelo terceiro botão superior. **MOVER:** arraste para deslocar o mapa, toque para construir um tile. **PINTAR:** arraste para construir uma rua contínua ou selecionar um retângulo de zonas/demolição.
- O pincel de **RUA** percorre tiles ortogonalmente conectados, inclusive com gestos diagonais rápidos.
- **CASAS**, **LOJAS**, **IND.** e **APAGAR** aplicam retângulos de até **16 × 16** tiles por gesto, com prévia verde (disponível) / vermelha (bloqueado), número de tiles e custo.
- Pagamento **atômico**: se o saldo não cobre os tiles disponíveis, não se aplica construção parcial; tiles bloqueados são ignorados sem custo, tiles já construídos não são cobrados novamente.
- Conexões viárias desenhadas conforme os quatro vizinhos reais (pontas, retas, curvas, T e cruzamentos) — sprites pixel art finais ainda pendentes.
- Botão **CENTRAR** para reenquadrar a cidade; edifícios menores e crescimento vertical desacelerado.
- Pausa, aceleração 1×/2×/4×, ajuda, salvar e abrir continuam acessíveis.
- Mantém compatibilidade com salvamentos **CVT1/CVT2** das versões anteriores, com CRC32, backup e recuperação.

**AVISO:** prévia de 0.2.0, ainda sem atlas de pixel art ou arte final. Testes automáticos e build Android não substituem a validação em celular real.


## Importante: instalação e saves dos APKs de testes

A partir desta prévia, o APK debug usa identificador **`br.com.cidadeviva.game.dev`** e aparece como **Cidade Viva Teste**. Ele pode ser instalado **ao lado** do APK v0.1.1 (`br.com.cidadeviva.game`), que permanece com seus salvamentos e dados. Os dados **não migram automaticamente** entre os dois aplicativos, pois o Android os isola por identificador. **Não desinstale a v0.1.1 para testar esta versão** se quiser preservar seu progresso.

Novas compilações de teste do Cidade Viva usam uma assinatura debug estável, compartilhada apenas para a distribuição de builds DEV. A chave de desenvolvimento está no repositório **de propósito** e não deve ser confundida com uma chave de publicação; o aplicativo final precisa de uma **chave release privada** e do ID de produção.

## Recursos já existentes desde o MVP

- Mapa procedural isométrico **48×48** com vegetação e água.
- Zoneamento residencial, comercial e industrial, expansão simplificada e economia por dias.
- Gestos de câmera, zoom ancorado e navegação multi-touch.
- Persistência local e teste de integridade com backup automático.
- Build Android debug independente e versão desktop Java.

## Compilar e testar

GitHub Actions: **Actions → Compilar APK Android** (artefato `CidadeViva-Android-debug`), ou em computador com Java 17, Android SDK 35, Gradle 8.9:

```bash
bash tools/test-core.sh
gradle :android:assembleDebug
# Arquivo em android/build/outputs/apk/debug/
```

O APK debug é para testes e não pode ser distribuído como release da Play Store sem assinatura release. Confira [QA-ANDROID-0.2.0-alpha1.md](QA-ANDROID-0.2.0-alpha1.md) para o teste manual.

## Atualizações da 0.2.0-alpha2

- **45 sprites isométricos originais** em atlas PNG 576 × 768; 16 combinações de vias, três categorias de edifícios × três níveis × duas variações; água, vegetação e terrenos variáveis.
- Renderização com `SpriteBatch` e textura `Nearest`, usando um atlas carregado apenas uma vez; regras da cidade continuam livres de libGDX.
- HUD superior ampliado para exibir `ZONAS SEM RUA`; barra inferior com rótulos altos, ícones diferenciados e áreas clicáveis revisadas.
- Zonas sem rua adjacente são destacadas em âmbar; tocar um terreno zoneado mostra tipo, nível e acesso.
- Testes Java adicionais verificam a contagem de zonas sem estrada, mudança de status, persistência e que um lote sem acesso permanece sem edifício.
- **Documentação:** [`ART-DIRECTION.md`](ART-DIRECTION.md) e [`QA-ANDROID-0.2.0-alpha2.md`](QA-ANDROID-0.2.0-alpha2.md).

A versão de teste mantém **package separado** (`br.com.cidadeviva.game.dev`) e assinatura **apenas de desenvolvimento**, para preservar o app antigo. Não usar chave DEV para publicação. A arte ainda será ampliada e refinada antes do lançamento.
