## 0.8.0-alpha2 — Sem pausa financeira

- Removida por solicitação do usuário a pausa automática por caixa baixo, antes e depois do avanço do dia.
- A previsão de caixa continua disponível no ECON, sem interromper o jogo.
- Pausa manual e pausa ao abrir menus continuam disponíveis. CVT7 e assinatura de teste preservados.

## 0.8.0-alpha1 — Cidade integrada

- Seis frentes do backlog: 2×2; camadas; classes e valorização; segurança/incêndios; saneamento; transporte/congestionamento.
- CVT7, migração de formatos anteriores e auditoria completa documentada.
- Corrigidos recuperação de temporários, APIs Android 23, texturas após perda de contexto e previsão da quitação de empréstimos.
- Regressões, controles e cenários longos; lint Android adicionado antes da geração do APK.

## 0.7.0-alpha3 — Empréstimos

- ECON → EMPRÉSTIMO: três ofertas com juros totais fixos e confirmação explícita.
- Parcelas diárias no orçamento, saldo devedor e pagamento parcial sem perdão da dívida.
- CVT6 persiste obrigações e migra CVT1–CVT5.
- Testes de contratação, cobrança, quitação, corrupção e controles.

## 0.7.0-alpha2 — Balanceamento

- Administração dos lotes privados deixa de crescer ao quadrado do nível.
- Demolição devolve 50% do custo original, por toque e pincel.
- Previsão de dias de caixa e pausa financeira preventiva.
- Cenários de recuperação e regressões de ressarcimento, controles e CVT5.

## 0.7.0-alpha1-redes

Energia e água distribuídas pelas ruas, com fontes finitas, consumo, manutenção, alertas e efeitos em empregos, serviços e evolução. Cada rede é ativada pela primeira fonte construída. Lê CVT1–CVT4 e grava CVT5; não fazer downgrade após salvar. Veja [redes e QA](REDES-0.7-ALPHA1.md).

## 0.6.0-alpha1-servicos

Parques, escolas e postos de saúde com capacidade, atendimento por ruas, manutenção e efeito na satisfação/evolução residencial. Menu SERV. e desenhos próprios no tabuleiro. Lê saves antigos e grava CVT4: não fazer downgrade após salvar. Veja [serviços e QA](SERVICOS-0.6-ALPHA1.md).

## 0.5.0-alpha3-auditoria

Correções de câmera/rotação, gestos cancelados, pintura sobre HUD, cliques em modais, BACK/ajuda, feedback de deterioração, recuperação de saves interrompidos, diálogos em paisagem, reset de load, status de slots danificados, ordenação compatível com API23 e assets desktop. Testes do adaptador real adicionados ao CI. Veja [auditoria](AUDITORIA-0.5-ALPHA3.md).

## 0.5.0-alpha2-bairros

Satisfação local, poluição industrial e comércio conectado agora influenciam a evolução residencial. Reclamações no inspetor, alertas e SAT/POL em ECON. CVT3 e assinatura DEV mantidos. Serviços públicos e prédios maiores continuam pendentes. Veja [design e QA](DESIGN-BAIRROS-0.5-ALPHA2.md).

## 0.5.0-alpha1-simulacao — 11/10/2026

- `CityMobility`: rotas reais de casa ao trabalho sobre ruas ortogonais, menor caminho com limite de 24 trechos e alocação determinística por proximidade/vagas.
- Elimina emprego e tributos que dependiam apenas de rua adjacente mas ignoravam a conexão entre bairros.
- Fluxo de trabalhadores por trecho viário, tráfego visual concentrado em corredores reais e congestionamento afetando qualidade de vida.
- Penaliza viagens longas e distritos empresariais sem acesso ao mercado de trabalho na demanda.
- Economia calculada sob demanda e mantida em cache até modificação, evitando buscar rotas em todos os quadros da tela Android.
- Indicadores `VIAGEM`, `TRANS`, `TRAB` e aviso de moradores sem conexão no HUD/ECON.
- +82.238 verificações automatizadas de caminhos, interrupção/recuperação de ruas, ocupação setorial, trânsito, congestionamento e reabertura de CVT3; regressões anteriores intactas.
- Formato CVT3, assinatura/dev ID, três slots e atlas de 125 sprites preservados. `versionCode 15`.
- LIMITAÇÕES: mobilidade é agregada, não há veículos físicos simulados individualmente, congestionamentos ainda não alteram tempos de viagem e não há serviços públicos modelados.

## 0.4.0-alpha5-economia — 11/10/2026

- Rebalanceamento de demanda R/C/I por relações proporcionais de moradores, empregos e oferta de terrenos, com penalidade de raiz quadrada.
- Arrecadação apenas de atividade de empregos efetivamente ocupados; desempregados têm menor contribuição residencial e zonas sem rua deixam de ser tributadas como operacionais.
- Manutenção de ruas R$ 3/dia, lote R$ 2/dia e custo crescente ao quadrado para cada edifício de nível 1–3.
- Taxas 5–20% com retorno fiscal decrescente após 10%, evitando estratégia dominante de 20%.
- Índice derivado de qualidade urbana; baixa disponibilidade financeira, baixa qualidade e vagas ociosas desaceleram crescimento.
- Demolição gratuita para reduzir risco de travar uma cidade em falência.
- HUD e painel ECON indicam qualidade de vida e moradores empregados; documentação de cenários + 23.505 verificações extras.
- CVT3, 3 cidades, atlas de 125 sprites e pacote Android DEV mantidos.

## 0.4.0-alpha4-visual — 11/10/2026

- [CORRIGIDO] Toda construção habitada usa agora a **mesma família de nove sprites V3**, com três estilos de progressão por setor. As fachadas antigas roxas deixam de aparecer; os sprites legados permanecem no atlas, sem apagar dados.
- [CORRIGIDO] O botão CENTRAR e a abertura/troca de cidade identificam o **núcleo urbano mais denso** em uma janela 15×15 em vez de incluir todos os ramais vazios de rua no enquadramento.
- [MELHORADO] Zoom inicial mais próximo (mínimo sugerido 0,42), preservando a pinça de zoom e movimento manual para ver o restante do mapa.
- [TESTADO] Regressões adicionais de consistência visual, câmera em retrato, ramais desconectados e continuidade dos saves CVT3. Atlas de 125 sprites inalterado.
- [COMPATIBILIDADE] versionCode 13 e ID de testes `.dev`/assinatura estável; nenhuma mudança em CVT3, três cidades, impostos ou economia.
- [PENDENTE] QA visual Android, especialmente enquadramento na tela de 1080×2316 e legibilidade dos sprites. 2x2 permanece planejado.

## 0.4.0-alpha3-buildings — 11/10/2026

- [ADICIONADO] 9 sprites importados de produção para três tipos e três níveis, com silhuetas e fachadas distintas.
- [ALTERADO] Edifícios existentes passam a usar predominantemente a nova arte; parte das variantes antigas permanece para diversidade urbana.
- [ADICIONADO] 125 sprites no atlas transparente 576×2048, preservando IDs 0–115.
- [SEGURANÇA] Formato CVT3, três cidades, economia, crescimento, câmera e conquistas sem modificações.
- [PLANEJADO] Prédios 2x2: requerem footprint, desenho em profundidade e testes de saves; ainda não implementados.

## 0.4.0-alpha2-evolution — 10/10/2026

- Atlas ampliado de 80 para **116 sprites originais**; quatro arquiteturas adicionais por categoria e nível (36 novos prédios), preservando todos os índices anteriores.
- Variante de prédio por lote permanece determinística e acompanha crescimento visual sem alterar IDs ou níveis no save.
- `CityFocus`: novo enquadramento do centro urbano e zoom proporcional à área construída; acionado ao iniciar, trocar/abrir cidade e tocar em CENTRAR.
- `CityMilestones`: metas de moradores acionadas apenas ao cruzar patamares reais (100/250/500/1.000/2.500/5.000/10.000/25.000/50.000), com aviso temporário e objetivo seguinte no HUD.
- Barra de demanda: separação vertical entre texto BAIXA/MEDIA/ALTA e preenchimento da barra.
- CVT3 não modificado. Testes Java: **66.764 verificações novas** de aparência, foco, marcos e continuidade dos saves.
- Build CI Android e QA em celular: verificar resultados separadamente.

# 0.4.0-alpha1-life — ritmo, bairros e movimentação (2026-10-10)

- Cidade nova equilibrada com indústria e comércio iniciais; população, empregos e economia visualmente mais compreensíveis.
- Penalty por lote vago suavizada: R2/C3/I3; permite zonear pequenas quadras sem travar a demanda.
- Ocupação de terrenos vazios priorizada com chance maior, limitada a até seis novas construções por dia; níveis altos seguem graduais.
- Dias de 2,55s em 1×; velocidade 2×/4× e pausa preservadas.
- `CityActivity` determinístico: até 18 carros nas ruas e 26 pedestres próximos a edifícios, sem novos assets.
- `GrowthEvent` efêmero e efeitos luminosos para construções/abandono; eventos não entram nos saves CVT3.
- Novo `CityLifeTest`, regressões anteriores e CI Android. QA físico pendente.
- ID do app, chave de desenvolvimento e formato CVT3 mantidos. Versão code 10.

# 0.3.0-alpha2 — Impostos e orçamento

- Painel ECON com impostos de 5 a 20%, receita residencial/comercial/industrial, manutenção de ruas e lotes, saldo e demanda.
- Persistência por cidade dos impostos e dos dias consecutivos de demanda fraca (CVT3, CRC32, compatível para leitura de CVT1/CVT2).
- Abandono gradual de prédios após 12 dias de baixa demanda; prédios isolados sem estrada também podem declinar.
- Atalho ECON testado em diferentes resoluções e gestos bloqueados sobre o modal.
- Pacote de desenvolvimento `.dev`, mesma assinatura, versionCode 8.
- Testes de migração, corrupção, independência de slots, balanço financeiro e crescimento/abandono determinístico.

# Changelog

## 0.3.0-alpha1 — demanda, empregos e orçamento (2026-10-10)

- Relatório `CityEconomy` com pressão de demanda residencial/comercial/industrial (5..95).
- Crescimento condicionado a demanda, rua adjacente e probabilidade decrescente por nível.
- Receita de zonas, manutenção viária, saldo diário (positivo ou negativo), desemprego e vagas.
- HUD com três barras R/C/I, receita, custos e informação ao tocar em uma zona.
- Saves CVT1/CVT2 e três slots independentes mantidos sem migração destrutiva.
- Testes Java e CI Android; validação no aparelho ainda pendente.

# Cidade Viva — histórico de alterações

## 0.2.0 Alpha 4 — Vários espaços de cidade

- Menu de três espaços independentes, cada um com dia, população e saldo.
- Slot 1 conserva o caminho de salvamento existente, incluindo backup e proteção CRC32.
- Nova Cidade nos espaços vazios, sem apagar cidade anterior; proteção de espaços danificados.
- Troca entre cidades com salvamento do slot anterior antes de ativar o próximo.
- Persistência do espaço ativo após fechar/reabrir o aplicativo.
- Pequenas variações determinísticas de terreno para novas cidades.
- Testes pure-Java cobrindo troca, independência de dados, saves antigos, corrupção e restart.

# Histórico de versões

## 0.2.0-alpha3 — 10/10/2026

- Atlas original ampliado de 45 para **80 sprites** (576×1280 RGBA).
- **16 transições de margem de água** conforme adjacência real com tiles vizinhos.
- **6 novas variantes de árvore**, incluindo copas arredondadas e árvore outonal.
- **4 variações detalhadas de gramado**, distribuídas em manchas em vez de xadrez.
- **9 modelos arquitetônicos adicionais**, preservando as categorias e os níveis existentes.
- **Novos mapas** passam a gerar dois lagos contínuos e agrupamentos de vegetação.
- Fora dos limites do mapa, o fundo é verde escuro em vez de espaço quase preto.
- Testes Java de máscara de água, conectividade de lagos, determinismo e persistência
  (9.220 verificações adicionais).
- Formato de save CVT2 **inalterado**; cidades antigas não são modificadas.
- Modo de testes Android `.dev` preservado, com mesma chave estável, versão code 5.
- Não inclui ainda menu de nova cidade: para preservar o save, terreno antigo
  não é substituído automaticamente. Não excluir dados só para ver lagos novos.

# Histórico de versões

## 0.2.0-alpha2
- HUD inferior com mais espaço, legendas reposicionadas, letras maiores e pictogramas de ferramentas distintos.
- Contador de zonas sem rua, alerta âmbar nos lotes e descrição contextual ao tocar.
- Atlas original com 45 sprites em pixel art isométrica; renderização via um único `SpriteBatch`/texture atlas.
- Regeneração de sprites reprodutível via Pillow em desenvolvimento e validação PNG na CI.
- Regressões Java cobrindo terrenos sem acesso, crescimento, salvamento, regiões clicáveis e zoom.
- APK de testes `.dev` com assinatura estável, compatível com atualização de 0.2.0-alpha1.

# Histórico de mudanças

## 0.2.0-alpha1 — Construção em área

- Modo PINTAR/MOVER separado da navegação padrão; gestos com dois dedos preservados.
- Pincel viário quatro-conectado e zoneamento/demolição retangulares até 16×16.
- Custo e disponibilidade no preview; lote atômico sem cobrança duplicada.
- Indicação visual de conectividade real das ruas (marcação procedural provisória).
- Centralização da câmera, melhoria de legibilidade e edifícios iniciais menos altos.
- Novos testes puros Java de construção por área, monetização e linhas.
- Leitura de saves antigos sem alteração de formato.
- APK de desenvolvimento com ID `.dev`, instalação paralela ao app legado e assinatura debug estável.

## 0.1.1 — Estabilidade Android

- Controles touch e zoom, limites de câmera, pausa e velocidade.
- Backup, CRC32 em CVT2 e leitura de CVT1; testes Java.

## 0.3.0-alpha3-ui — HUD 2.0

- Redesenho das barras superior e inferior: cartões legíveis de caixa, população, dia e saldo, faixa de alertas e botões com área de toque preservada.
- Três cartões visuais de demanda R/C/I com barras de 0 a 100, situação baixa/média/alta e cores funcionais.
- Ferramentas com pictogramas mais destacados, nomes, custos e seleção evidente.
- Reformulação visual dos modais ECON e CIDADES sem alterar as regras de impostos e de slots.
- Novo painel contextual persistente para inspeção de lotes (nível, estrada, demanda e imposto), com proteção contra toques no mapa por trás.
- Regressões de toque ampliadas; sem mudanças em CVT3 nem na lógica de simulação.
