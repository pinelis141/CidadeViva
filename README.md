# Cidade Viva — Java / libGDX / Android

MVP de city builder isométrico original. **Não utiliza Godot.**

## O que já está implementado no código

- Mapa procedural isométrico 48×48, com grama, lagos e árvores.
- Rua, zoneamento residencial, comércio, indústria e demolição.
- Edifícios 3D isométricos simples com crescimento de até 3 níveis, ao lado de ruas.
- Economia por ciclos: população, empregos, impostos e manutenção das ruas.
- Toque para construir, arrastar para navegar, gesto de pinça para zoom, controles inferiores responsivos.
- Botões SALVAR/ABRIR; persistência local versionada e salvamento automático.
- Versão desktop com mouse, teclas 1–5, S e L.
- Workflow GitHub Actions para compilar APK **debug** para instalação/testes.

## Como compilar no GitHub sem abrir Android Studio

1. Crie um repositório **novo** e envie o conteúdo desta pasta para ele (não misture com o PixelChess).
2. No GitHub, vá à aba **Actions** e execute **Compilar APK Android**.
3. Ao final de uma execução bem-sucedida, abra o run e baixe o artefato `CidadeViva-Android-debug`.
4. Extraia o ZIP do artefato e instale o APK de teste no Android.

O APK **não acompanha este ZIP de código-fonte**: a compilação requer Gradle, Android SDK e dependências libGDX obtidas pela Internet, indisponíveis neste ambiente de geração. Não distribuir o APK debug como versão final na Play Store. Para publicação, será preciso assinar uma build release.

## Compilar num computador

Necessário Java 17, Android SDK 35, Android Build-Tools 35.0.0 e Gradle 8.9:

```bash
gradle :android:assembleDebug
# Arquivo gerado: android/build/outputs/apk/debug/android-debug.apk

gradle :lwjgl3:run
```

## Validar as regras sem SDK Android

```bash
./tools/test-core.sh
```

## Limites do MVP

Os modelos gráficos são formas geométricas temporárias; sprites pixel-art definitivos, trânsito, serviços públicos, redes elétricas, estrada auto-conectada, sistema de demanda profundo, animações e performance em celulares reais são etapas seguintes. O APK ainda não foi compilado nem testado em dispositivo. Identidade gráfica e mecânicas são próprias, sem reaproveitar assets de TheoTown.

## Atualização 0.1.1 — estabilização Android

- Toques do HUD separados do mapa e seleção de botão confirmada no `touchUp`.
- Zoom por pinça com ponto ancorado, pan com dois dedos e limites de câmera.
- Pausa/retomar, velocidades 1×, 2×, 4× e painel AJUDA.
- Saves CVT2 com CRC32 e backup automático; leitura retrocompatível com CVT1 (v0.1.0).
- Testes puros Java para seleção isométrica, geometria de câmera, regiões clicáveis, salvamento e compatibilidade.
- Plano de verificação manual do Android em [QA-ANDROID-0.1.1.md](QA-ANDROID-0.1.1.md).

**Importante:** a compilação CI e os testes Java não substituem a validação em aparelho Android. Issues CV-001 a CV-006 acompanham o encerramento da sprint.
