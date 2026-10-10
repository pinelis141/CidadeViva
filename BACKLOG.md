# Cidade Viva — Backlog mestre

**Revisão:** 09/10/2026  
**Repositório oficial:** `pinelis141/CidadeViva`  
**Stack:** Java 17, libGDX, Gradle, Android (prioritário), desktop (secundário).  
**Produto:** city builder isométrico original, single-player e offline, com progressão econômica, infraestrutura, serviços e evolução visual em pixel art. Sem reutilizar código, marca ou assets de TheoTown.

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
