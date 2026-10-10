# Plano mestre — Cidade Viva (projeto de city builder em Java)

## Princípios

- Plataforma prioritária: Android, com compilação para APK; versão PC usando a mesma lógica Java.
- Framework gráfico: libGDX; arte isométrica original em pixel art.
- Projeto independente de TheoTown, sem utilizar nome, arquivos ou recursos protegidos desse jogo.
- Interface orientada a toque, baixa fricção, botões grandes e estados confiáveis.
- Renderização isolada das regras de simulação para facilitar testes automáticos.
- A cada incremento, manter projeto compilável e testável no Android.

## Marco 0: protótipo Java incluído neste arquivo — código concluído, APK não compilado

- Terreno isométrico 48×48; vegetação, água e geração reprodutível.
- Ruas, zonas residenciais/comerciais/industriais, remover estrutura.
- Adjacência a estradas, crescimento básico, moradores, empregos, impostos.
- Interface de cinco ferramentas, salvar/abrir, câmera por arrasto/pinça.
- Persistência de estado e testes puros Java.
- GitHub Actions preparado para gerar APK debug quando publicado em repositório novo.

## Marco 1: jogo confortável no celular

- Corrigir quaisquer erros apontados por compilação Android real e testar no aparelho.
- Zoom ancorado no ponto do toque, pan de dois dedos, seleção de quadrados sem erro.
- Ferramenta de pincel de vias e zoneamento em múltiplos tiles; seleção de área.
- Menus de confirmação, ajuda inicial, modo paisagem/retrato, acessibilidade.
- Sprites pixel art para água, terra, árvores, ruas, casas e edifícios.

## Marco 2: economia e infraestrutura de verdade

- Demanda residencial/comercial/industrial e custos progressivos.
- Energia elétrica, distribuição e cobertura; água e esgoto.
- Redes de transporte conectadas e veículos simulados com rotas.
- Cobertura de bombeiros, saúde, educação e segurança.
- Impostos configuráveis, orçamento de departamentos e relatórios.

## Marco 3: cidade viva

- Cidadãos/agentes de transporte, dia e noite, trânsito e congestionamento.
- Poluição, terreno, parques, qualidade de vida, felicidade e migração.
- Emergências e desastres opcionais, reação dos serviços públicos.
- Objetivos e missões, conquistas, tutoriais e feedback sonoro original.

## Marco 4: distribuição

- Testes em múltiplos tamanhos de tela, FPS e consumo de memória.
- Saves migráveis e backups confiáveis com checagem de integridade.
- Identidade visual final, iconografia, splash, efeitos e traduções.
- Geração e assinatura de build release; política de privacidade e publicação.

## Definição de concluído por fase

Um item só é considerado concluído após implementação, teste lógico, build Android bem-sucedida e validação da interação no dispositivo (quando aplicável). O código deste pacote só completou testes de lógica Java; **nenhuma build Android foi verificada** neste ambiente.
