# Auditoria — Cidade Viva 0.8 Alpha 1

## Escopo

Revisão de todos os módulos de produção em core, inicializadores Android/desktop, configuração Gradle, CI e testes. Abrange simulação, economia, empréstimos, grafos viários, serviços/redes, edifícios, seleção/gestos, câmeras, desenho, texturas, persistência, backup e slots.

## Correções encontradas

| Problema | Correção |
| --- | --- |
| `Math.floorMod` exige API 24, mas o mínimo declarado é API 23 | Helper compatível, preservando os resultados dos mapas/rolagens; teste nos limites inteiros. Referência: [Android Math](https://developer.android.com/reference/java/lang/Math#floorMod(int,%20int)). |
| Temporário válido podia ser ignorado enquanto a versão primária antiga ainda existia | Recuperação prioriza o temporário validado, inclusive antes da rotação. Temporário incompleto não mascara primário válido. |
| Texturas procedurais não se recuperavam de perda de contexto gráfico | Dados de textura gerenciados e pixmaps retidos até a liberação dos recursos. |
| Prédios 2×2 podiam exigir rua individual dos terrenos interiores | Acesso e atendimento usam todo o perímetro compartilhado. |
| Prévia de demolição poderia destacar apenas parte de um bloco | Expansão da seleção e do custo para todo o footprint, sem dupla devolução. |
| Avaliar oferta apenas antes de consolidar/expandir não garantia recursos para o novo prédio | Simulação prospectiva verifica capacidade e restaura o estado ao negar a operação. |
| Garagens da mesma rede podiam deixar frota ociosa; múltiplos acessos poderiam juntar vias desconectadas | Capacidade agrupada por componentes rodoviários reais, com uma vinculação por garagem. |
| Relatórios de abastecimento/cobertura consultavam níveis que já tinham mudado durante o dia | Necessidades e população atendida são capturadas no relatório diário. |
| Frota visual podia permanecer desatualizada por vários dias após incêndios/consolidação | Atualização uma vez por dia simulado, além das ações manuais. |
| Carros eram desenhados por cima de fachadas | Passes de terreno, atividade e edifícios, com culling ampliado para os 2×2. |
| Previsão de caixa tratava parcelas finitas como despesa eterna e podia bloquear a última parcela pagável | Projeção por fase: parcelas cheias, última parcial e operação sem dívida. Déficit transitório é identificado. |
| Inspetor de ônibus podia anunciar operação sem garagem/pontos conectados | Usa o estado operacional real da rede. |

## Evidências de teste

- Regressões de construção, pincel, orçamento, impostos, crescimento, abandono, câmera, marcos, ambientes, slots, CRC, recuperação e migrações.
- 157 casos do código real de controles, incluindo novos serviços, camadas em retrato/paisagem e telas reduzidas, empréstimos, retorno de menus e ausência de construção por toque em controles.
- 27.911 verificações de footprints, saneamento, segurança, classes, ônibus, camadas e CVT7.
- Cenários mistos de **300 dias**, com três cidades, demolições, dívidas e recargas; invariantes de caixa, vagas, população, classe, recursos, frota, níveis, incêndios e blocos. Mais de 1,38 milhão de verificações de estado.
- Contratos de empréstimo acompanhados até a quitação, incluindo falta de caixa, parcial e previsão da última parcela.
- Compilação Java com `-Xlint:all` e inspeção da imagem dos nove edifícios e seis novos serviços.
- Pipeline Android executa verificação do atlas, regressões Java, controles/ciclo de vida, distribuição desktop e `lintDebug` **antes** da etapa de APK. Verifica identidade DEV e presença do atlas no pacote.

## Limites práticos

A análise e os testes reduzem risco, não garantem ausência de bugs. Não houve teste em telefone físico ou medição real de FPS, bateria e memória no Android nesta execução. O transporte, saneamento, classes e segurança são modelos agregados descritos no documento da versão, não agentes individuais.

QA em aparelho: atualizar sem desinstalar, abrir cidade anterior, contratar/pagar empréstimo, alternar todos os serviços/camadas em retrato e paisagem, construir um bairro 2×2 abastecido, romper e reparar ruas, testar riscos, fechar/reabrir e alternar slots. Não apagar a cidade para testar.
