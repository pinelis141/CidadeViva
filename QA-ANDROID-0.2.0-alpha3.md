# QA Android — Cidade Viva 0.2.0 Alpha 3

Versão debug de testes: `br.com.cidadeviva.game.dev` (chave debug de desenvolvimento
estável). **Atualize por cima da Alpha 2; não apague o aplicativo nem seus dados.**

## Checklist de aceite no celular

- [ ] Instala sem erro de assinatura e preserva cidade, saldo, população e dia.
- [ ] O jogo inicia sem tela preta; gramado e árvores exibem variantes.
- [ ] Casas, comércios e indústrias preservam nível e categoria, com variantes
      arquitetônicas e sem pixel clipping no lote da frente.
- [ ] Há margens de água quando uma área com lago estiver visível; água adjacente
      mantém costa apenas onde encontra terra (caso de cidade nova).
- [ ] Zoom mínimo e máximo mantêm sprites nítidos, sem distorção e sem perda
      de precisão dos tiles clicados.
- [ ] Fora da borda do mapa aparece fundo fosco, sem um buraco preto intenso.
- [ ] Ferramentas RUA/CASAS/LOJAS/IND/APAGAR, MOVER/PINTAR e PAUSA seguem funcionando.
- [ ] Marcador de `ZONAS SEM RUA` é reduzido ao adicionar via adjacente.
- [ ] **SALVAR**, encerrar o app completamente e abrir novamente restaura a
      cidade. Verificar ainda ABRIR e retorno após interrupção de Android.
- [ ] Medir FPS, consumo de bateria e travamentos ao ficar 15 min em 4X.

## Critérios de regressão da lógica

- [x] Testes puros Java: construção, custos, área, entrada, acesso viário, save.
- [x] TerrainTest: lagos conectados, 16 máscaras, determinismo e round-trip.
- [x] PNG RGBA 576×1280, 80 sprites, gerado via Pillow e verificado pelo CI.
- [ ] GitHub Actions debug aprovado (atualizar após execução).
- [ ] Teste Android real demonstrado em captura ou vídeo do usuário.

## Compatibilidade de saves

A Alpha 3 não migra nem recria tiles de arquivos antigos: a mudança do gerador
vale para cidades novas. Não desinstalar apenas para observar os lagos. Um fluxo
de "Nova cidade" com múltiplos slots seguros é trabalho futuro, não uma parte
concluída desta entrega.
