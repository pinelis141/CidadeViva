# Cidade Viva 0.5 Alpha 2 — qualidade local dos bairros

## Implementado
- Satisfação residencial de 5 a 95 por lote, calculada com emprego realmente alocado, comércio conectado e ocupado, vegetação próxima, poluição industrial, tráfego e imposto residencial.
- Indústrias ocupadas emitem conforme nível em até seis células de distância Manhattan. Emissão não depende de empregos ocupados. Zonas industriais vazias não emitem. Florestas oferecem amenidade em raio três; não removem a poluição.
- Comércio atende por caminhos de ruas com até 12 passos e exige prédio com pelo menos um trabalhador. É acesso potencial, sem simulação de estoque, compras ou capacidade de atendimento.
- Satisfação abaixo de 45 bloqueia evolução residencial de nível 1 para 2; abaixo de 65 bloqueia de 2 para 3. Abaixo de 28 bloqueia novas residências e pode causar queda gradual de nível após o dia 14, com chance determinística de 3,5% ao dia. Não há contador de deterioração local persistido.
- QUAL combina o índice urbano anterior com a média residencial ponderada por moradores (peso 2:1). SAT é a satisfação local média; POL no orçamento conta moradores expostos a emissão >=40. No inspetor, POL mostra intensidade local de 0 a 100.
- Alertas de poluição/insatisfação e reclamações específicas no inspetor. Emprego por residência e ocupação por estabelecimento conservam as alocações existentes.
- Dados derivados recalculados ao mudar obras, impostos, dia ou save. CVT3, três slots e assinatura DEV preservados.

## Validação
Testes de regressão e cenários específicos executados pelo workflow Android: poluição, vegetação, comércio desconectado, conservação de trabalhadores, bloqueio de upgrade e replay de saves. Validar no aparelho antes de considerar concluída a experiência visual.

## Próximas etapas do plano aprovado
Serviços públicos (saúde, educação, segurança e lazer), classes sociais com regras próprias, construção por fases, footprints 2x2/3x3, mercadorias, desbloqueios, estatísticas e minimapa permanecem pendentes. Este incremento estabelece as consequências ambientais e locais, sem afirmar implementação dessas fases.

## Teste Android
Instalar por cima de Cidade Viva Teste; abrir os três espaços e conferir dinheiro, impostos e edifícios. Inspecionar residências perto/longe de indústrias. Remover uma fábrica deve reduzir POL imediatamente; remover a ligação ao comércio deve alterar a reclamação. Avançar tempo e observar evolução limitada pela SAT. Confirmar pausa, velocidade, zoom, toque e legibilidade em retrato/paisagem.
