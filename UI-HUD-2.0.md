# Cidade Viva — HUD 2.0 (v0.3.0-alpha3-ui)

## Objetivo

Interface primeiro para Android, legível a uma distância normal, com dados de jogo organizados e botões que não interferem na câmera. O HUD usa Java/libGDX (ShapeRenderer + BitmapFont), sem dependência de novos assets de terceiros. A próxima atualização poderá substituir pictogramas por sprites originais mantendo a mesma geometria de toque.

## Hierarquia e geometria

Todas as dimensões estão em unidades de projeto multiplicadas por `HudLayout.scale(width,height)`, limitadas a `0.65–1.65`. As coordenadas de toque são de origem no canto superior esquerdo; a projeção de desenho do HUD tem origem no canto inferior esquerdo. É proibido duplicar retângulos de toque em outras classes: `HudLayout` é a fonte de verdade.

- Faixa superior: `249u`, fundo azul-petróleo quase opaco. Cabeçalho com nome à esquerda e três atalhos `ECON / CIDADES / CENTRAR` à direita.
- Estatísticas em cartões: caixa, população, dia e saldo diário. A cor do saldo sinaliza valor negativo.
- Demanda em três cartões: **RESIDENCIAL** verde, **COMERCIAL** azul, **INDUSTRIAL** âmbar. Nível BAIXA (0–29), MEDIA (30–59), ALTA (60–100). A barra varia com a demanda calculada pelo simulador, não por dados mockados.
- Faixa de alertas: zonas sem rua, desempregados e vagas.
- Linha de ações: PAUSA/SEGUIR, velocidade 1X/2X/4X, MOVER/PINTAR, AJUDA, SALVAR e ABRIR.
- Dock inferior de `134u`: ferramentas RUA, CASAS, LOJAS, IND., APAGAR. Cada cartão possui pictograma, rótulo e custo; o selecionado recebe destaque âmbar.
- Painel contextual flutuante: ao tocar em lote, exibe tipo, nível, acesso viário, demanda e imposto (quando aplicável). `X` fecha; toques no painel são consumidos, sem construir por trás.

## Modais

**ECONOMIA:** cabeçalho, três seções de impostos com botões `-` e `+`, demanda por setor, receita total e componentes, despesas e componentes, saldo diário (verde/vermelho). Continua usando os mesmos limites `5–20%` e salvamento CVT3 por cidade. O modal bloqueia construção e pausa avanço de dias enquanto aberto.

**CIDADES:** três cartões com `EM USO / SALVA / NOVA / PROTEGIDA`, número do dia, população e caixa. Preserva a confirmação já implementada e proíbe exclusão automática de saves.

## Regras de leitura e interação

- Interface totalmente desenhada em coordenadas separadas da câmera, para que zoom não reduza textos.
- Funções `label` e `centered` limitam a largura de textos e diminuem escala/abreviam em telas estreitas.
- As coordenadas dos controles ECON/CIDADES/CENTRAR e dos três botões de imposto permanecem iguais às da build anterior, preservando gestos e testes de toque.
- Os modal overlays são opacos o suficiente para que as informações possam ser lidas em cima de qualquer área da cidade.
- Barra de demanda usa dados reais de `CityEconomy`; sem alterar economia ou formato CVT3.
- O painel de inspeção aparece somente após tocar um tile e não é exibido durante modais ou ajuda.

## Critérios de aceite no Android

Veja `QA-ANDROID-0.3.0-alpha3-ui.md`. O código e os testes unitários não substituem teste real em aparelho com menus e controle por toque.
