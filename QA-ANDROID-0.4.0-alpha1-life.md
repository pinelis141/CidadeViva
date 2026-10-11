# QA real Android — Cidade Viva 0.4.0-alpha1-life

**Pendente de aparelho físico.** Os testes Java/Android CI não substituem este checklist.

## Instalação sem perda de progresso

- [ ] Instalar por cima de `Cidade Viva Teste` (`br.com.cidadeviva.game.dev`) SEM desinstalar.
- [ ] Abrir as cidades salvas 1, 2 e 3; confirmar que dia, caixa, prédios, impostos e ruas não mudaram ao atualizar.
- [ ] Confirmar que CVT3 continua gravando e abrindo; não fazer downgrade após salvar em CVT3.

## Primeiros cinco minutos de jogo

- [ ] Selecionar espaço vazio e confirmar nova cidade equilibrada: casas, lojas e indústrias já ocupadas, todas acessíveis.
- [ ] Visualizar carros nas ruas; nenhum atravessa um lago ou prédio.
- [ ] Ver pedestres nas áreas ocupadas, sem poluição visual excessiva.
- [ ] Construir ao menos 4 terrenos residenciais junto a uma estrada; barras R/C/I ainda orientam corretamente.
- [ ] Acelerar a 4×; observar lotes vazios ganhando casas e os breves efeitos visuais.
- [ ] Os edifícios mantêm o nível após SALVAR, fechar o app e ABRIR.
- [ ] Ao tocar PAUSA, carros, pedestres e calendário param; ao retomar, seguem.
- [ ] Abrir ECON/CIDADES: não avança o dia nem permite construir atrás dos modais.
- [ ] Arrastar mapa, usar pinch zoom, desenhar estradas, zonear, apagar e selecionar tiles sem regressões.
- [ ] Medir FPS/engasgos e calor do aparelho por 10 minutos em uma cidade com muitas ruas.

## Observar e reportar

Envie captura de tela ou vídeo mostrando: 1) data, população e demanda antes; 2) quatro a oito lotes vazios perto da rua; 3) evolução em 1× e 4×; 4) movimento de carros/pedestres; 5) qualquer sobreposição na UI.

Limitações conhecidas: veículos e pedestres são ambientação visual (não trafegam para empregos); efeitos usam formas pequenas no próprio renderizador, com sprites pixel art originais preservados. Se a cidade antiga não tiver ruas conectadas ou edifícios ocupados, a atividade visual será limitada.
