# Cidade Viva — Backlog mestre

**Revisão:** 14/10/2026
**Repositório oficial:** `pinelis141/CidadeViva`  
**Stack:** Java 17, libGDX, Gradle, Android (prioritário), desktop (secundário).  
**Produto:** city builder isométrico original, single-player e offline, com progressão econômica, infraestrutura, serviços e evolução visual em pixel art. Sem reutilizar código, marca ou assets de TheoTown.

## Estado atual — 0.8 Alpha 1

Esta seção é a referência atual. As atualizações e marcos abaixo registram o histórico do plano e não devem ser lidos como lista atual de ausências.

| Frente | Estado de implementação | Aceite |
| --- | --- | --- |
| Simulação R/C/I, empregos, trajetos, impostos e evolução | Implementado | Regressões automatizadas; equilíbrio mais recente ainda em teste de aparelho |
| Qualidade local, poluição, reclamações e valorização | Implementado como modelo agregado | QA Android da integração pendente |
| Parques, escola e saúde | Implementado; versão 0.6 validada pelo usuário | Preservados na integração |
| Energia, água, lixo e esgoto | Implementado com capacidade e atendimento por ruas | QA Android da integração pendente |
| Bombeiros, incêndios opcionais e polícia | Implementado como modelo agregado | QA Android pendente |
| Prédios 2×2, nove desenhos e evolução conjunta | Implementado | Recursos prospectivos, footprint e demolição testados; QA visual pendente |
| Camadas do mapa e classes sociais | Implementado | Controles testados em retrato/paisagem; QA visual pendente |
| Ônibus e trânsito | Garagem + pontos, capacidade por rede, passageiros empregados, busca ponderada e alcance de serviços | Modelo agregado; sem linhas/horários individuais |
| Balanceamento e empréstimos | Implementado; previsão de quitação corrigida nesta versão | Regressões; validação de ritmo no aparelho pendente |
| Salvamentos e auditoria | CVT7, leitura CVT1–CVT6, temporários/backup/CRC, três slots | Regressores e cenários longos; QA físico pendente |

Ainda fora desta entrega: prédios 3×3, editor de linhas de ônibus, veículos/cidadãos individuais com IA, seleção de tamanhos/mapas, ferramentas avançadas de terreno, tutorial interativo, áudio, clima/dia-noite e release para publicação. Marcos populacionais existem; desbloqueios aprofundados continuam parciais. Próxima prioridade após esta versão: validar o conjunto em Android e medir desempenho/ritmo antes de expandir conteúdo.

Detalhes: [sistemas e regras](CIDADE-INTEGRADA-0.8-ALPHA1.md) e [auditoria](AUDITORIA-0.8-ALPHA1.md).

## Histórico de execução

## Atualização — 0.4.0-alpha1 (vida urbana)

**Motivação:** após testes em celular, o principal obstáculo de experiência relatado foi a falta de vida e evolução visível da cidade.

- [x] Reequilibrar cidade **nova** (casas, comércio e indústria iniciais); cidades antigas não são regeneradas.
- [x] Suavizar penalidade por terrenos vazios na demanda e tornar a ocupação de lotes com rua e demanda alta mais visível.
- [x] Limitar a 1–6 novos edifícios por dia, com evolução vertical mais lenta e lógica determinística.
- [x] Ritmo diário de 4 s → 2,55 s em 1×; 2×/4× preservados.
- [x] Primeiros carros visuais que seguem ruas reais e pedestres animados perto de prédios, com orçamento fixo de desenho.
- [x] Feedback visual para crescimento/abandono de edifícios; eventos efêmeros não alteram saves CVT3.
- [x] Nova bateria **CityLifeTest** (4.104 verificações), regressões anteriores e APK Android CI aprovados.
- [x] Código mesclado na main via [PR #28](https://github.com/pinelis141/CidadeViva/pull/28).
- [ ] **Aceite em aparelho Android**: [QA #29](https://github.com/pinelis141/CidadeViva/issues/29), observar cinco minutos de bairro e verificar se as animações são visíveis e fluidas.
- [ ] Próxima iteração: variedade de veículos/personagens em sprite art, efeitos sonoros discretos, objetivos/marcos de desenvolvimento e revisão de legibilidade HUD no aparelho.

Ver [DESIGN-CIDADE-VIVA-0.4.md](DESIGN-CIDADE-VIVA-0.4.md) e [QA-ANDROID-0.4.0-alpha1-life.md](QA-ANDROID-0.4.0-alpha1-life.md). O tráfego **ainda é apenas visual** e não simula transportes ou engarrafamentos; CVT3, backup, impostos e três cidades continuam intactos.
## Atualização — 0.4.0-alpha2 (identidade visual e metas)

**Feedback do teste Alpha 1:** os números evoluem, mas bairros ainda parecem iguais; o centro construído fica pequeno na tela e falta recompensa por progresso.

- [x] Expandido o atlas de 80 para **116 sprites originais**: 36 edifícios adicionais (4 estilos × 3 setores × 3 níveis), sem mudar índices antigos.
- [x] Novas fachadas e telhados determinísticos por posição; evolução continua visível sem alterar edifícios salvos.
- [x] `CityFocus`: reenquadramento no centro construído e zoom adaptativo ao abrir/trocar cidade e no botão CENTRAR.
- [x] `CityMilestones`: avisos temporários quando população real cruza 100, 250, 500, 1.000 etc.; objetivo seguinte no HUD.
- [x] Evitada sobreposição do texto BAIXA/MEDIA/ALTA com barras R/C/I.
- [x] Testes Java **66.764 verificações novas**, atlas 116 sprites validado, Android CI verde; [PR #30](https://github.com/pinelis141/CidadeViva/pull/30) integrado.
- [ ] [Aceite físico Android #31](https://github.com/pinelis141/CidadeViva/issues/31): variedade perceptível, centro da câmera, legibilidade, desempenho e notificações.
- [ ] Iterar economia, objetivos mais profundos e renderização de personagens conforme vídeo de QA.

CVT3, 3 slots, impostos, animações de Alpha 1 e assinatura Android DEV preservados. **Não desinstalar o aplicativo de testes.**
## Atualização — 0.4.0-alpha3 (edifícios 1x1 integrados)

**Entrega integrada:** [PR #32](https://github.com/pinelis141/CidadeViva/pull/32) — nove sprites 1x1 renderizados no jogo, não apenas conceitos.

- [x] Nove sprites visualmente distintos: três níveis por setor R/C/I, índices 116–124.
- [x] Atlas **125 sprites, 576×2048, RGBA transparente**; IDs 0–115 preservados pixel a pixel.
- [x] Atualizado `BuildingAppearance` para escolher o novo sprite em 6 de 7 famílias determinísticas, preservando uma variante antiga para diversidade.
- [x] Assets fonte 72×128 armazenados em `art/game/buildings_v3/` e incorporados ao gerador reprodutível do atlas.
- [x] Testes Java e CI Android debug aprovados; versão 0.4.0-alpha3-buildings, versionCode 12.
- [ ] [QA de campo #33](https://github.com/pinelis141/CidadeViva/issues/33): legibilidade, recortes, sobreposição, evolução e save no aparelho.
- [ ] Edifícios **2x2**: ainda pendentes. Implementar detecção de bloco e regras de ocupação, renderização e salvamento separado.

CVT3, impostos, três slots e assinatura DEV preservados; não desinstalar o aplicativo de testes.
## Atualização — 0.4.0-alpha4 (identidade visual e reenquadramento)

**Feedback vídeo Alpha 3:** prédios roxos legados quebravam o estilo dos 9 novos sprites e o bairro aparecia pequeno demais no Android.

- [x] Retirado fallback visual para fachadas legadas: R/C/I níveis 1–3 usam 100% os nove sprites novos 116–124. Atlas de 125 sprites preservado, sem alteração de PNG.
- [x] `CityFocus` localiza área urbanizada densa (janela 15×15 por prefixos 2D) e prioriza prédios ocupados; ruas distantes deixam de ampliar zoom automaticamente.
- [x] Sugestão de zoom inicial mais próxima, com foco adaptado às barras superior/inferior e zoom por pinça manual preservado.
- [x] Testes Java **76.057 afirmações** da bateria de câmera/marcos/sprites, além de regressões CVT3; CI Android debug verde.
- [x] [PR #34](https://github.com/pinelis141/CidadeViva/pull/34) integrado à main.
- [ ] [QA Android #35](https://github.com/pinelis141/CidadeViva/issues/35) exige aprovação em aparelho (identidade, câmera, escala, zoom e salvamentos).
- [ ] 2×2: footprint e economia ainda precisam ser implementados; não ativar neste marco.

Não altera CVT3, impostos, posições e níveis das três cidades. Atualizar o app de teste sem desinstalar.
## Atualização — 0.4.0-alpha5 (balanceamento econômico)

**Auditoria e implementação detalhadas:** [PR #36](https://github.com/pinelis141/CidadeViva/pull/36), com compilação Android aprovada; [QA físico #37](https://github.com/pinelis141/CidadeViva/issues/37) pendente.

- [x] Separar empregos oferecidos de empregados efetivos; cobrar impostos empresariais somente de trabalho realmente ocupado e acessível por rua.
- [x] Manutenção progressiva: R$ 3 por rua, R$ 2 por zona e custo por nível ao quadrado (`4/6/9 × nível²` para R/C/I).
- [x] Fiscalidade 5–20% com rendimento decrescente após 10%, eliminando cobrança máxima como estratégia dominante.
- [x] Demandas baseadas em razões populacionais/empregos, com penalidade de oferta de terrenos proporcional à raiz quadrada da quantidade vaga.
- [x] Indicador derivado QUALIDADE, impactos de caixa baixo e desemprego, expansão comercial/industrial limitada por vagas ociosas.
- [x] Demolição gratuita em dificuldade financeira, sem permitir aterrar lagos gratuitamente.
- [x] `CityBalanceTest`: 23.506 verificações e comparação determinística de cenários fiscais de 100 dias; demais regressões CVT3 aprovadas.
- [x] Compilação Android DEV versão `0.4.0-alpha5-economia`, versionCode 14, atlas125 mantido.
- [ ] Testes de game feel em aparelho Android: verificar caixa, demanda, qualidade, clareza dos indicadores e ausência de bloqueios.
- [ ] Próxima etapa: prédios maiores 2×2, reforço de variantes e limites de câmera, **somente após validar o equilíbrio**.

Referências: [DESIGN-BALANCEAMENTO-0.4-ALPHA5.md](DESIGN-BALANCEAMENTO-0.4-ALPHA5.md) e [QA-ANDROID-0.4.0-alpha5-balance.md](QA-ANDROID-0.4.0-alpha5-balance.md). **Formato CVT3 e três cidades preservados.**
## Atualização — 0.5.0-alpha1 (simulação viária e empregos)

**Mudança de direção:** Cidade Viva é um **simulador urbano**; decisões de ruas, bairros, impostos e empregos devem gerar consequências observáveis e testáveis, não apenas cosméticas.

- [x] **Rede viária real**: distância por BFS ao longo de ruas (limite 24), caminhos válidos entre residências e estabelecimentos.
- [x] **Empregos efetivos**: trabalhadores alocados em vagas alcançáveis, priorizando menor trajeto; desemprego e vagas podem coexistir em bairros desconectados.
- [x] **Economia dependente de rede**: comércio/indústria só geram receita fiscal sobre empregos de fato ocupados; capacidade empresarial inacessível reduz demanda C/I.
- [x] **Tráfego a partir de viagens reais**: contagem de trabalhadores por rua, carros visuais nos corredores usados; proxy de congestionamento e viagens longas reduz QUALIDADE.
- [x] Cache do relatório econômico, com invalidação em obras, mudanças fiscais, avançar dia e restaurar; não recalcular trajetos a cada quadro.
- [x] Indicadores no HUD/ECON para trabalho, deslocamento médio, trechos congestionados e bairros sem ligação viária ao emprego.
- [x] 82.238 verificações adicionais, regressões Java e atlas 125 aprovados, compilação Android DEV concluída.
- [x] Código integrado via [PR #38](https://github.com/pinelis141/CidadeViva/pull/38).
- [ ] **QA Android físico:** [Issue #39](https://github.com/pinelis141/CidadeViva/issues/39).
- [ ] Próximas fases: cobertura espacial de água/energia, serviços públicos, consequências ambientais, congestionamento dinâmico e transporte público.
- [ ] Edifícios 2x2 devem consumir recursos e depender dos serviços antes de serem liberados.

**Escopo atual:** cálculo agregado de deslocamento, não indivíduos ou veículos com IA própria. Mantém CVT3, três slots e assinatura Android DEV compatíveis.
Referências: [DESIGN-SIMULACAO-0.5.md](DESIGN-SIMULACAO-0.5.md) e [QA-ANDROID-0.5.0-alpha1.md](QA-ANDROID-0.5.0-alpha1.md).
## 1. Convenções

- **P0 — bloqueador:** impede estabilidade, controle ou perda de dados.
- **P1 — essencial:** necessário ao jogo-base e à primeira versão publicável.
- **P2 — expansão:** melhora a profundidade e a apresentação.
- **P3 — futuro:** só após um ciclo jogável e estável.
- **Estado:** `BASE EXISTE` = existe no código, mas não necessariamente está validado no dispositivo; `ABERTO` = ainda falta implementar; `PARCIAL` = incompleto.
- **Tamanho:** P (pequeno), M (médio), G (grande). São indicadores de complexidade, não prazos.
- **DoD (definition of done):** código integrado, testes de regras passando, build Android debug com CI verde, comportamento verificado em Android quando envolver UI, sem regressão em salvar/abrir ou controles. Publicação requer validação adicional de release assinada.

## Atualização de execução — 0.1.1 (09/10/2026)

**Código incorporado à branch principal:** PR #14, após CI verde na branch `feature/v0.1.1-controles-saves`.  
**APK de testes disponível:** `CidadeViva-0.1.1-Android.apk`.  
**Não substituir validação em Android por uma compilação bem-sucedida.**

| Item | Estado após implementação | Pendência |
|---|---|---|
| CV-001 — smoke test no Android | **ABERTO** | Teste manual físico com gravação de evidências. |
| CV-002 — HUD e botões | **IMPLEMENTADO / QA PENDENTE** | Confirmar acerto de regiões em retrato e paisagem. |
| CV-003 — câmera e zoom | **IMPLEMENTADO / QA PENDENTE** | Validar zoom ancorado e seleção em dispositivo real. |
| CV-004 — save e backup | **IMPLEMENTADO / QA PENDENTE** | CVT2 + CRC32, backup e leitura CVT1 testados em Java; validar no armazenamento do Android. |
| CV-005 — testes e CI | **IMPLEMENTADO** | Testes de regressão Java e Android debug compilado no Actions. |
| CV-006 — pausa, velocidade, ajuda | **IMPLEMENTADO / QA PENDENTE** | Validar legibilidade e controles no aparelho. |

**Checklist físico:** [QA-ANDROID-0.1.1.md](QA-ANDROID-0.1.1.md).  
**Critério de conclusão do marco A:** todos os itens devem estar validados; portanto **marco A ainda não está encerrado**.

## Atualização de desenvolvimento — 0.2.0-alpha1

A primeira etapa do marco visual e de construção foi integrada na `main` por meio do **PR #15**.

| Tarefa | Estado de implementação | Aceite |
|---|---|---|
| CV-007 — ruas por arrasto | Código concluído; ruas 4-conectadas sem duplicidade | Teste manual Android pendente |
| CV-008 — zoneamento por área | Código concluído; zonas e demolição em retângulos 16×16 com prévia e pagamento atômico | Teste manual Android pendente |
| CV-009 — junções viárias | Marcas de conexão calculadas por vizinhos (procedural) | Arte final e teste Android pendentes |
| CV-010 — pipeline de arte | **Não iniciado** | Atlas, camadas e licenças faltam |
| CV-011 — sprites pixel art | **Não iniciado** | Grama, água, vegetação, prédios e ruas finais faltam |
| CV-012 — HUD e detalhes | Parcial: modo PINTAR/MOVER, prévia e botão CENTRAR | Ajustes de legibilidade e painéis faltam |

A versão `0.2.0-alpha1` **não é a v0.2.0 final**. O marco continua aberto até haver sprites originais, acabamento e validação em dispositivo. O QA geral da v0.1.1 também permanece pendente de interação em aparelho real.

## Atualização de desenvolvimento — 0.2.0-alpha2

A primeira revisão de arte e interface chegou à `main` por meio do [PR #18](https://github.com/pinelis141/CidadeViva/pull/18), com build Android e testes Java aprovados.

| Tarefa | Implementação | Validação ainda necessária |
|---|---|---|
| CV-007 — pincel de ruas | Implementado na alpha1 | Vídeo do usuário confirmou o desenho básico de ruas por arrasto. |
| CV-008 — zoneamento em área | Implementado na alpha1 | Vídeo do usuário confirmou seleção e aplicação de áreas. |
| CV-009 — conexão de ruas | Sprite específico para cada um dos 16 bitmasks na alpha2 | Validar curvas, T e cruzamentos em tela. |
| CV-010 — pipeline de assets | Atlas 576×768 PNG e gerador Python originais; `ART-DIRECTION.md` | Continuar refino estético; registrar futuras fontes externas se usadas. |
| CV-011 — sprites isométricos | 45 sprites originais para terreno, árvores, água, ruas e prédios | Validar encaixe, perspectiva e legibilidade em Android real. |
| CV-012 — HUD/contexto | Toolbars redimensionadas, ícones distinguíveis e alerta de lote sem rua | Verificar toques, contraste, sobreposição e orientação em vários aparelhos. |

**Nova regra visível:** lotes comerciais/residenciais/industriais sem rua adjacente recebem aviso âmbar; tocar no lote mostra `SEM RUA ADJACENTE`. Criar uma rua no lado do lote elimina o aviso correspondente. A simulação já exigia rua adjacente; a mudança torna a condição compreensível. O saldo/população ainda seguem o modelo econômico simplificado.

**Verificações automáticas:** `tools/test-core.sh`, `tools/verify_atlas.py`, Gradle Android APK, confirmação da presença do PNG em `assets/city/atlas.png` e package debug `br.com.cidadeviva.game.dev`. **O marco visual permanece aberto até o QA físico da alpha2.**

**Checklist do aparelho:** [QA-ANDROID-0.2.0-alpha2.md](QA-ANDROID-0.2.0-alpha2.md). Conteúdo futuro: interiores? Não; próximo foco em refinamento visual, inspeção de terreno e critérios de demanda/emprego antes de avançar a serviços e veículos.

## Atualização — 0.2.0-alpha4 (três cidades e salvamento seguro)

- **Implementado:** menu CIDADES com slots 1, 2 e 3, indicadores de dia/população/saldo, criação somente em slots vazios, troca protegida e escolha do último slot após reiniciar.
- **Compatibilidade:** o slot 1 permanece no arquivo **original** `cidade-viva/salvamento.cvt`; slots 2 e 3 usam `cidade-viva/slots/cidade-2.cvt` e `cidade-viva/slots/cidade-3.cvt`, com seus próprios backups. Nenhum save antigo é renomeado, apagado ou substituído ao criar outra cidade.
- **Novo mapa:** cidades dos slots adicionais recebem alterações determinísticas de lagos e florestas; o formato de save CRC32 CVT2 continua compatível.
- **Teste:** casos puros Java de troca, corrupção, independência e reinício passaram, junto ao CI Android e verificação do atlas. [PR #20](https://github.com/pinelis141/CidadeViva/pull/20).
- **QA físico pendente:** validar a navegação entre slots, fechar/reabrir e preservar as construções na instalação Android [issue #21](https://github.com/pinelis141/CidadeViva/issues/21). Somente após aceitação física encerrar o marco.
- **Próximo sistema (v0.3.0):** demanda residencial/comercial/industrial e regras econômicas mais realistas — começar após confirmar que salvamentos e a troca entre cidades funcionam.

## Atualização — 0.3.0-alpha1 (demanda e economia)

**Implementação aprovada em CI:** [PR #22](https://github.com/pinelis141/CidadeViva/pull/22). Os sistemas de economia e crescimento estão em Java puro, com regras determinísticas e formato de salvamento original preservado.

| Item | Implementação | Pendente |
|---|---|---|
| CV-013 — demanda R/C/I | **Implementada** primeira fórmula com pressão de emprego e lotes vazios, escores 5–95 | QA e balanceamento com dados de cidade real |
| CV-014 — crescimento | **Parcial**: crescimento por demanda e acesso viário, níveis altos mais lentos | Abandono/decadência |
| CV-015 — orçamento | **Parcial**: arrecadação, manutenção, saldo positivo/negativo | Ajuste de alíquotas pelo jogador |
| CV-016 — progressão | Não implementado | Missões e desbloqueios |
| CV-017 — explicação | **Parcial**: R/C/I, 3 barras, receitas/despesas e empregos no HUD | Painel econômico com explicações detalhadas |

- Novo relatório `CityEconomy`; testes Java adicionais: **2.329 verificações**.
- Futuros relatórios não alteram automaticamente arquivos CVT1/CVT2 nem os 3 slots.
- QA físico para validar a tipografia, demanda, saldo, construção, troca de cidades e salvamento: [issue #23](https://github.com/pinelis141/CidadeViva/issues/23).
- [DESIGN-ECONOMIA.md](DESIGN-ECONOMIA.md) e [QA-ANDROID-0.3.0-alpha1.md](QA-ANDROID-0.3.0-alpha1.md).

**A Alpha 1 está implementada, mas o marco 0.3.0 continua aberto.**
## Atualização — 0.3.0-alpha2 (impostos, orçamento e abandono)

**Código integrado à main:** [PR #24](https://github.com/pinelis141/CidadeViva/pull/24). Testes de lógica, atlas e compilação Android aprovados na branch; aceite Android físico pendente.

| ID | Situação | Faltando |
|---|---|---|
| CV-013 | Demanda R/C/I com ajuste por impostos implementada | Ajustar equilíbrio via jogo real |
| CV-014 | Crescimento condicionado por demanda/rua e abandono gradual depois de 12 dias | Confirmar abandono e recuperação no Android |
| CV-015 | Orçamento e imposto R/C/I ajustável 5–20%; receita/despesa por setor | Revisão da progressão, déficits e políticas fiscais |
| CV-016 | Não iniciado | Progressão, missões e desbloqueios |
| CV-017 | Painel ECON com detalhamento e ações +/− | Verificar responsividade e legibilidade no dispositivo |

- Salvos **CVT3** com impostos e histórico de baixa demanda; leitura de **CVT1/CVT2** mantida e 3 slots independentes.
- O novo formato não é lido por APKs anteriores: **não reverter versão após salvar com Alpha 2**.
- Testes Java: **2.346 verificações novas** de tributação, corrupção, migração, isolamento e abandono determinístico, além das regressões anteriores.
- [QA em dispositivo — issue #25](https://github.com/pinelis141/CidadeViva/issues/25).
- Documentos: [DESIGN-ECONOMIA.md](DESIGN-ECONOMIA.md) e [QA-ANDROID-0.3.0-alpha2.md](QA-ANDROID-0.3.0-alpha2.md).

**Marco 0.3.0 continua aberto:** faltam progressão (CV-016), polimento de equilíbrio e validação real no celular.
## Atualização — HUD 2.0 (0.3.0-alpha3-ui)

- **PR #26 integrado:** [Redesenho de interface móvel](https://github.com/pinelis141/CidadeViva/pull/26).
- Quatro cartões de status (caixa, moradores, dia, saldo). Três cartões de demanda R/C/I com barra 0–100 e classificação baixa/média/alta.
- Barra de alertas (ruas, desempregados, vagas), seis ações principais, cinco ferramentas com custo e seleção legível.
- Painel ECON e modal CIDADES redesenhados, preservando funcionalidades e áreas de toque; painel de inspeção de lotes com demanda, taxa, estrada, nível.
- Geometria centralizada em `HudLayout` e renderização em Java/libGDX. Testes Java passaram, incluindo **2.511** verificações de toque/HUD/isometria/câmera.
- **Sem alteração dos dados:** saves CVT3, três slots e valores econômicos permanecem compatíveis com 0.3.0-alpha2.
- **QA físico pendente:** [issue #27](https://github.com/pinelis141/CidadeViva/issues/27), checar legibilidade real e interação.
- Especificação: [UI-HUD-2.0.md](UI-HUD-2.0.md) e [QA-ANDROID-0.3.0-alpha3-ui.md](QA-ANDROID-0.3.0-alpha3-ui.md).
## 2. Auditoria do estado atual (v0.1.0)

**Com código implementado:**
- [x] Mapa procedural isométrico 48 × 48, com grama, árvores e água.
- [x] Estradas e zonas residenciais, comerciais e industriais; demolição unitária.
- [x] Edifícios representados com prismas geométricos, até nível 3, a partir de estradas próximas.
- [x] Ciclos de dias, saldo, população, empregos e arrecadação *simplificados*.
- [x] Botões de ferramentas, SALVAR/ABRIR, toque, arrasto e pinça programados.
- [x] Arquivo de save local binário com marcador de versão e testes básicos Java.
- [x] Workflow GitHub Actions compilando APK debug no repositório **CidadeViva**.

**Limites confirmados pela leitura do código:**
- [ ] Jogabilidade/touch e desempenho ainda não foram validados em um celular físico.
- [ ] Ainda não há sprites finais; desenhos são formas provisórias.
- [ ] Câmera não mantém o ponto sob o dedo durante zoom; não há pintura por arrasto.
- [ ] Estradas não formam uma rede de roteamento e não possuem junções visuais automáticas.
- [ ] Crescimento e arrecadação são regras simples, não um balanço robusto de oferta/demanda.
- [ ] Sem energia, água, serviços, veículos, congestionamento, poluição, felicidade ou missões.
- [ ] Save sem backup de segurança, checksum e testes de migração de formato.
- [ ] APK compilado é **debug**, não release assinada.

> Atenção: código existente não é sinônimo de experiência aprovada. Após cada marco, testar o APK no aparelho.

## 3. Ordem de desenvolvimento

### Marco A — v0.1.1: estabilização Android / qualidade (prioridade imediata)

| ID | Prioridade | Item | Tam. | Estado | Critérios de aceite |
|---|---|---|---|---|---|
| CV-001 | P0 | Smoke test real no Android | P | ABERTO | Instala, inicia, não fecha, renderiza corretamente, seleciona 5 ferramentas, salva/abre, suporta reabertura após fechar. Checklist com resultados. |
| CV-002 | P0 | Corrigir áreas de toque e HUD | M | ABERTO | Ações correspondem aos botões em resoluções diferentes; nenhum toque em HUD constrói no mapa; feedback claro para todas as ações. |
| CV-003 | P0 | Câmera e seleção de tiles | M | ABERTO | Arrastar sem construções acidentais, zoom ancorado no gesto, pinça sem salto, limites de câmera, toque posiciona no tile correto em zoom mínimo/máximo. |
| CV-004 | P0 | Robustez do salvamento | M | ABERTO | Save temporário validado antes da substituição; backup anterior; corrupção tratada sem perder save válido; testes de round-trip e falhas. |
| CV-005 | P0 | Testes e automação contra regressões | M | ABERTO | CI compila APK e verifica construção, custo, estradas, evolução, round-trip de save, limites, erros/entradas inválidas. |
| CV-006 | P1 | Interface de pausa/velocidade e tutorial curto | M | ABERTO | Pausar retém dia; retomar e velocidade alteram simulação; indicação do tipo/custo da ferramenta e controles acessíveis. |

**Gate A:** uma pessoa consegue jogar 10–15 minutos no Android, construir e demolir sem toques incorretos, salvar, fechar e retomar a mesma cidade sem perda de progresso. CI verde. Se CV-001 revelar erro grave, resolver antes de abrir novas features.

### Marco B — v0.2.0: ferramentas e identidade visual

| ID | Prioridade | Item | Tam. | Estado | Critérios de aceite |
|---|---|---|---|---|---|
| CV-007 | P1 | Pincel de ruas por arrasto | M | ABERTO | Arrastar constrói múltiplas células sem duplicar custo; exibe prévia e total. |
| CV-008 | P1 | Zoneamento retangular | M | ABERTO | Selecionar área R/C/I com preview, custo e confirmação; não construir em água/estrutura proibida. |
| CV-009 | P1 | Estradas conectadas visualmente | M | ABERTO | Retas, curvas, T, cruzamentos e pontas calculados por vizinhos; atualiza ao construir/demolir. |
| CV-010 | P1 | Pipeline de assets pixel art | M | ABERTO | Atlas original, escala consistente, camadas separadas e licenças registradas. |
| CV-011 | P1 | Primeiros sprites isométricos | G | ABERTO | Grama, água, árvores, ruas, 3 classes de construções e variações legíveis no celular. |
| CV-012 | P1 | HUD e painéis contextuais | M | ABERTO | Saldo, população, dia, custo, aviso de erro e detalhes do tile; UI não bloqueia o mapa indevidamente. |

**Gate B:** expansão de bairros sem tocar tile a tile; todas as ruas e zonas visualmente distinguíveis.

### Marco C — v0.3.0: economia e crescimento críveis

| ID | Prioridade | Item | Tam. | Estado | Critérios de aceite |
|---|---|---|---|---|---|
| CV-013 | P1 | Demanda residencial/comercial/industrial | G | ABERTO | Demanda R/C/I varia conforme emprego, população e condições, com indicador no HUD. |
| CV-014 | P1 | Evolução e abandono de edifícios | G | ABERTO | Crescimento e decadência seguem regras determinísticas/testáveis; estrada conectada exigida. |
| CV-015 | P1 | Orçamento, tributos e manutenção | G | ABERTO | Receita/despesa diária detalhadas; tributos configuráveis; falência e custos coerentes. |
| CV-016 | P1 | Progressão por desbloqueios | M | ABERTO | Objetivos simples e desbloqueios por marcos, sem impedir o sandbox básico. |
| CV-017 | P1 | Painel de diagnóstico econômico | M | ABERTO | Explica por que há baixa demanda, desemprego ou perda de receita. |

**Gate C:** uma cidade cresce, enfrenta restrições econômicas compreensíveis, pode estagnar e se recuperar.

### Marco D — v0.4.0: infraestrutura urbana

| ID | Prioridade | Item | Tam. | Estado | Critérios de aceite |
|---|---|---|---|---|---|
| CV-018 | P1 | Grafo de ruas transitáveis | G | ABERTO | Detecta conexões, desconexões e acesso à rede principal com testes de caminhos. |
| CV-019 | P1 | Energia elétrica | G | ABERTO | Usinas, custo/capacidade, distribuição e avisos; falta de energia afeta imóveis. |
| CV-020 | P1 | Água e esgoto | G | ABERTO | Fonte, distribuição, capacidade, cobertura e custo afetam habitabilidade. |
| CV-021 | P2 | Lixo e saneamento | M | ABERTO | Capacidade, coleta simplificada e efeitos de falta de serviço. |
| CV-022 | P1 | Sobreposições de infraestrutura | M | ABERTO | Visualizar cobertura/deficiência de cada sistema sem sobrecarregar o HUD. |

**Gate D:** serviços essenciais deixam de ser decoração e influenciam concretamente o crescimento.

### Marco E — v0.5.0: serviços e qualidade de vida

| ID | Prioridade | Item | Tam. | Estado | Critérios de aceite |
|---|---|---|---|---|---|
| CV-023 | P1 | Corpo de bombeiros e incêndios | G | ABERTO | Cobertura, risco, resposta e possibilidade de perda de imóvel testáveis. |
| CV-024 | P1 | Polícia e saúde | G | ABERTO | Cobertura, despesa e impacto em indicadores/crescimento. |
| CV-025 | P2 | Educação e parques | M | ABERTO | Edifícios de serviço e cobertura impactam a atratividade das zonas. |
| CV-026 | P2 | Felicidade e migração | G | ABERTO | Índice explicado por fatores mensuráveis; população varia com qualidade de vida e emprego. |
| CV-027 | P2 | Poluição e valor do terreno | G | ABERTO | Indústrias e tráfego influenciam áreas próximas; parques/serviços podem mitigar. |

### Marco F — v0.6.0: mobilidade e cidade viva

| ID | Prioridade | Item | Tam. | Estado | Critérios de aceite |
|---|---|---|---|---|---|
| CV-028 | P2 | Veículos e rotas | G | ABERTO | Agentes visualmente percorrem ruas conectadas, sem teletransportes ou travamentos. |
| CV-029 | P2 | Capacidade viária e congestionamento | G | ABERTO | Tráfego afeta deslocamentos, serviços e produtividade de forma mensurável. |
| CV-030 | P2 | Transporte coletivo básico | G | ABERTO | Rede, pontos e custo-benefício definidos depois de tráfego estável. |
| CV-031 | P2 | Animações leves (água, árvores, pessoas) | M | ABERTO | Animações originais com desempenho aceitável no Android. |

### Marco G — v0.7.0: conteúdo, interface e imersão

| ID | Prioridade | Item | Tam. | Estado | Critérios de aceite |
|---|---|---|---|---|---|
| CV-032 | P2 | Seleção de mapas, sementes e tamanhos | G | ABERTO | Novos mapas reproduzíveis; saves incluem seed, tamanho e versão. |
| CV-033 | P2 | Ferramentas de terreno e paisagismo | G | ABERTO | Alteração de florestas, terreno e água com custos/restrições. |
| CV-034 | P2 | Tutorial interativo e objetivos | M | ABERTO | Jogador aprende primeiro ciclo sem consultar manual; pode ignorar tutorial. |
| CV-035 | P2 | Áudio e efeitos originais | M | ABERTO | Som de ações, avisos e ambientação, com controles de volume/mute. |
| CV-036 | P3 | Desastres opcionais e eventos | G | ABERTO | Ativação opcional; impacto previsível e medidas de recuperação. |
| CV-037 | P3 | Ciclo dia/noite e clima | G | ABERTO | Visual opcional sem comprometer legibilidade e performance. |

### Marco H — v1.0.0: qualidade e lançamento

| ID | Prioridade | Item | Tam. | Estado | Critérios de aceite |
|---|---|---|---|---|---|
| CV-038 | P1 | Performance e estabilidade | G | ABERTO | Medir FPS, consumo de memória, tempo de carregamento e uso de bateria em Android real. |
| CV-039 | P1 | Saves compatíveis e backups | G | ABERTO | Migração de versões, detecção de corrupção e recuperação confiável. |
| CV-040 | P1 | UX para telas e acessibilidade | M | ABERTO | Testar diferentes DPI, proporções, tamanhos de toque e contraste. |
| CV-041 | P1 | Assinatura e APK/AAB release | M | ABERTO | Build assinada independentemente da debug, versionamento e publicação sem segredos no repositório. |
| CV-042 | P1 | QA de lançamento | G | ABERTO | Testes de instalação/atualização, jogo longo, regressões e checklist de créditos/licenças. |

## 4. Sprint de trabalho seguinte — v0.1.1

**Não começar sprites finais nem grandes sistemas enquanto o núcleo de toque/salvamento não estiver estável.** Ordem recomendada:

1. **CV-001:** instalar e percorrer checklist no Android; documentar falhas.
2. **CV-002 + CV-003:** melhorar reconhecimento de gestos, área dos botões, seleção de tiles e câmera.
3. **CV-004:** proteger saves e testar corrupção/retomada.
4. **CV-005:** consolidar testes no GitHub Actions.
5. **CV-006:** pausa/velocidade/ajuda contextual.
6. Gerar **APK 0.1.1**, validar novamente e só então encerrar o marco.

**Risco mais importante:** o código atual compila e tem testes puros Java, mas isso *não prova* que toque, renderização e salvamento funcionam em todos os dispositivos. Por isso a qualidade básica vem antes do aumento do escopo.

## 5. Diretrizes técnicas

- Preservar `core/src/main/java/br/com/cidadeviva/sim` livre de APIs de Android e libGDX.
- Extrair gradualmente HUD, câmera, input e renderização de `CityGame` em classes menores, por mudanças testáveis e sem reescrita massiva.
- Nunca recalcular caminhos de todas as entidades em todo frame; simulação em passos discretos, render interpolado.
- Construções, terrenos e serviços com identificadores persistentes estáveis (não depender indefinidamente da ordem do `enum` no save).
- Dados de balanceamento externalizáveis e testáveis; evitar números mágicos espalhados.
- Arte original ou devidamente licenciada, com identidade própria.
- Commits e CI *apenas* em `pinelis141/CidadeViva`; **PixelChess. não deve ser utilizado**.
- O usuário decide a direção criativa e valida em dispositivo físico; a implementação técnica, testes automatizados, documentação e builds ficam neste fluxo de desenvolvimento.

## 6. Critérios de “jogo divertido” (além de compilar)

- O jogador enxerga consequência clara ao construir uma rua, zona ou serviço.
- Há escolhas econômicas reais: crescimento, déficit, expansão e recuperação.
- Limitações de infraestrutura e trânsito criam desafios progressivos, sem punições arbitrárias.
- O mapa cresce sem repetição visual excessiva e sem gestos frustrantes.
- Erros são compreensíveis: `sem dinheiro`, `sem acesso`, `sem energia`, `sem água`.
- Partidas podem ser retomadas com segurança.
