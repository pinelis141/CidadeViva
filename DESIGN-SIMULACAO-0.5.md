# Cidade Viva 0.5 — fundação da simulação urbana (Alpha 1)

## Regra principal

Uma vaga não é preenchida só por existir uma casa e uma empresa na mesma cidade. Moradores e empresas precisam ter uma **conexão real por ruas transitáveis**. As decisões viárias mudam arrecadação, demanda, qualidade e atividade urbana.

## Modelo agregado implementado

- Mundo: 48 × 48 células. Lotes residenciais de nível `L` oferecem `12×L` trabalhadores potenciais; lotes comerciais `10×L` vagas; industriais `18×L` vagas. Preserva o balanço de empregos da Alpha 5.
- A casa e o estabelecimento precisam possuir ao menos uma rua em uma das quatro laterais. Uma rua isolada sem continuidade não garante acesso a locais de trabalho.
- Para cada edifício residencial ocupado, a simulação executa uma busca em largura (**BFS**) sobre ruas ortogonais, desde todas as ruas adjacentes à casa. Não atravessa casas, indústrias, lagos nem terreno comum.
- A busca é limitada a **24 movimentos entre trechos de rua**. Locais de trabalho disponíveis são ordenados pelo menor percurso. Empregos são alocados até o limite de vagas da empresa e trabalhadores do edifício. Empates mantêm ordem estável, para que o mesmo save produza o mesmo resultado.
- A mão de obra é **agregada por prédio**, não há uma entidade para cada pessoa; isso mantém o cálculo eficiente para Android. O mapa de tráfego conta, por trecho, quantos trabalhadores percorrem o corredor usado em cada alocação.
- `empregados = trabalhadores atribuídos a vagas de empresas alcançáveis`. `desempregados = população total − empregados`. `vagas = vagas viárias operacionais − empregados`. Desemprego e vagas podem coexistir.
- Arrecadação comercial/industrial usa empregados efetivos de cada setor. Parte residencial também varia com emprego, como já ocorria na Alpha 5.
- Para a demanda de residências/indústrias, o fator de capacidade de emprego usa apenas vagas de empresas **alcançáveis por residências existentes**. Demanda de comércio/indústria recebe penalidade proporcional à capacidade desconectada. O impacto fiscal não vira dinheiro fictício.

## Qualidade urbana e congestionamento

- `commuteSteps`: média ponderada de trajeto, contando dois trechos de acesso entre casa/rua e rua/empresa.
- `trafficAt(x,y)`: fluxo calculado por trecho usado em rotas, não um sorteio aleatório.
- `congestedRoads`: quantidade de trechos com fluxo equivalente de **80 ou mais viagens diárias**; proxy simplificado, não capacidade de pista física.
- Na qualidade urbana entram a taxa de emprego, acesso dos lotes, condição fiscal, impostos, moradores sem deslocamento viável, viagens médias longas e até 12 pontos de penalização por ruas congestionadas.
- Os carros continuam **visuais** (não colisão, semáforos ou agentes que alteram velocidade). Entretanto, sua presença é vinculada aos locais de tráfego de trabalhadores e é recalculada quando a cidade muda.

## Proteção de desempenho

- Calcular rotas a cada frame seria caro. `City.economy()` usa cache e o invalida em construção, demolição, alteração de impostos, restauração e avanço de dia. O render aproveita o mesmo relatório calculado.
- BFS limitado em células 48×48 e distância 24; alocação determinística; arrays reaproveitados por análise; contagem de veículos visuais limitada a 18.
- A simulação não lê relógio real, internet ou localização; mantém consistência após salvar/abrir e trocar de slot.

## Cenários testados

1. Bairro residencial conectado a comércio próximo: empregos preenchidos e arrecadação real.
2. Indústria com rua adjacente, mas distante além do limite: vagas não preenchidas apesar de habitantes disponíveis.
3. Novo bairro residencial perto da indústria: vagas preenchidas, desemprego diminui, trânsito aparece nos corredores utilizados.
4. Rua intermediária demolida: trabalhadores perdem o acesso, tributos das empresas inacessíveis desaparecem e surgem habitantes sem conexão.
5. Rua reconstruída: empresas voltam a contratar sem alterar populações artificialmente; cache é invalidado.
6. Corredor com 144 moradores empregados: fluxo e congestionamento detectados.
7. Persistência CVT3: 35 dias de evolução reexecutados após save com tráfego e economia idênticos, mais 12 cidades geradas com ruas modificadas e 25 dias cada.

## Limitações explícitas e fases seguintes

**Ainda não:** indivíduos com casa/escola/turno, renda familiar, serviços de água/energia, ônibus, poluição real, velocidades diferentes, congestionamento dinâmico retroalimentando a capacidade da rua, acidentes ou produção industrial física. Esses sistemas exigem regras e testes próprios. Começar pela malha viária e emprego evita colocar novos indicadores cosméticos sobre uma base sem causalidade.

**Fase seguinte recomendada:** serviços municipais (água, energia e saúde) com cobertura espacial, custos recorrentes e consequências de falta de cobertura; em seguida transportes reais e ocupação 2×2 coerente com infraestrutura.
