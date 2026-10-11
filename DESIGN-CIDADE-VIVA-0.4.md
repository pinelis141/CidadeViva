# Cidade Viva 0.4.0-alpha1 — Cidade que cresce

## Problema observado

A evolução urbana estava pouco visível e a expansão da zona residencial podia parar após apenas alguns lotes vazios. Na cidade inicial antiga havia 108 moradores e 30 empregos, desestimulando construções residenciais. Um dia de simulação levava 4 segundos. Nenhum efeito ou agente mostrava que a cidade estava viva.

## O que a primeira Alpha 0.4 efetivamente implementa

- **Cidade nova com empregos desde o início:** três pequenas indústrias, três comércios e seis casas, todos com acesso a estrada; cidades já salvas mantêm o seu mapa original.
- **Demanda que reage melhor ao loteamento:** penalidade por terreno vazio com rua foi reduzida para R −2, C −3 e I −3 por lote; impostos, vagas e oferta continuam impactando o resultado.
- **Ocupação mais visível:** terrenos com rua e demanda ≥24 têm chance diária de nível 0 → 1 entre 13,7% e 32,2%, limitada a 1–6 novos edifícios por dia conforme tamanho da cidade. A elevação de nível 1 → 2 e 2 → 3 segue mais lenta. O limite previne mudanças instantâneas em massa.
- **Novo ritmo de simulação:** 2,55 segundos por dia em 1× (contra 4 segundos antes), com 2× e 4× ainda disponíveis. O saldo e os impostos continuam calculados uma única vez por dia.
- **Avisos visuais de evolução:** ao crescer ou sofrer abandono, cada prédio gera evento efêmero `City.GrowthEvent`; anel/diamante animado e texto curto flutuante indicam a mudança. Os eventos não alteram o formato salvo.
- **Atividade urbana leve:** até 18 carros visuais se movem apenas entre células contíguas de ruas, e até 26 pedestres circulam dentro dos terrenos com edifícios. Rotas são reconstruídas quando o mapa muda e são estáveis para o mesmo mapa. Objetos são formas simples desenhadas sem novos assets ou dependências.
- **Economia preexistente preservada:** tributos por cidade, decadência por demanda persistentemente baixa, CVT3, backup e 3 cidades.

### Limites atuais

Tráfego e pedestres são **efeitos visuais**, não possuem acidentes, engarrafamento, transporte de recursos ou comportamento de IA complexo. As zonas evoluem conforme demanda e acesso, com decisão pseudoaleatória determinística por dia e coordenada; ainda não há escolas, energia, criminalidade, felicidade, missões ou progressão por desbloqueios. A validação de experiência em Android físico segue pendente.

### Critério de aceite Android

1. Uma cidade nova deve nascer com bairros residenciais, comerciais e industriais conectados, sem substituir saves anteriores.
2. Ao zonear quatro a oito terrenos residenciais perto das ruas, ver pelo menos uma construção surgir dentro de 22 dias sob demanda ≥24.
3. O crescimento deve ser perceptível pelo efeito visual e pelo surgimento do sprite, sem ficar preso em zonas desconectadas.
4. Ver carros se deslocando nas ruas e pedestres perto de prédios; em PAUSA e nos modais o movimento deve congelar.
5. Em SALVAR, ABRIR, sair do aplicativo e trocar de cidades, manter o estado da cidade e não persistir partículas antigas.
6. Confirmar desempenho aceitável ao ampliar e reduzir o mapa em Android.

Testes: `bash tools/test-core.sh` (inclui `CityLifeTest`) e `python3 tools/verify_atlas.py`. Compilação Android no GitHub Actions.


## Alpha 2 — Identidade de bairro, câmera e marcos

**Arte**: atlas RGBA 576×1920, 116 quadros de 72×128, 8 colunas. IDs 0–79 preservados; 80–115 contêm 4 novos estilos × 3 níveis × 3 setores. Casas incluem sobrados azuis, apartamentos de varanda, telhados lilases e prédios grafite. Comércios incluem hortifruti, café, lanchonete e escritório. Indústria inclui tijolos vermelhos, tubulações azuis, armazém e fundição metálica. Sprites gerados por `tools/generate_pixel_atlas.py` com Pillow, sem dependência adicional no aplicativo.

**Simulação**: marcos dependem só da transição real de população entre dias. Avisos não geram novas regras econômicas e não são salvos. Ao reabrir save acima do marco, não se repete a conquista anterior; a próxima é mostrada quando atingida. Sem ganhos financeiros automáticos para não interferir no CVT3.

**Câmera**: ponderação das edificações ocupadas (peso 5), zonas vazias (2) e ruas (1), ignorando mata e água; sugestão de zoom com margens e limites. Foco é reposicionado verticalmente pela área ocupada pelo topo e ferramentas inferiores. O jogador continua podendo mover e usar pinça manualmente.

**HUD**: texto de status das demandas e barras R/C/I ficam em faixas separadas no mesmo cartão. Sinal de meta é informativo, sem ocultar alertas de ruas ausentes.

**Próximos passos**: validar pixel art e legibilidade em Android físico, iterar nos sprites de ruas/pedestres, melhorar marcos com recompensas de gameplay testadas e adicionar serviços (energia, água) após feedback. Não confundir animações de carros com trânsito simulado.
