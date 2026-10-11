# Cidade Viva — rebalanceamento 0.4.0 Alpha 5

## Decisão de design

O jogador relatou falta de equilíbrio no crescimento, nos empregos e no saldo. O protótipo cobrava **R$ 2 por rua e R$ 1 por lote**, mesmo que um edifício chegasse ao nível 3. Todos os postos de trabalho eram tributados automaticamente, mesmo sem moradores suficientes para preenchê-los. Impostos de 20% duplicavam a receita sem custo fiscal proporcional. Zonas pintadas em bloco também podiam esmagar a demanda por contagem linear.

A Alpha 5 substitui essas aproximações por uma economia determinística que responde às ações. **Não adiciona um sistema fictício de serviços ou transporte:** empregos são considerados acessíveis somente quando os edifícios têm rua adjacente; ainda não há trajetos reais casa→trabalho ou conectividade da malha principal.

## 1. População, empregos e trabalho real

- Residencial nível `L`: `12 × L` residentes.
- Comercial nível `L`: `10 × L` vagas.
- Industrial nível `L`: `18 × L` vagas.
- `empregos operacionais`: somente vagas de construções com acesso viário adjacente.
- `moradores atendidos`: residentes de construções com acesso viário.
- `trabalhando = min(moradores atendidos, empregos operacionais)`.
- `desempregados = população total − trabalhando`.
- `vagas disponíveis = empregos operacionais − trabalhando`.
- Empregos ocupados são divididos proporcionalmente entre comércio e indústria; vagas vazias não geram imposto de atividade.

**Limitação consciente:** a distribuição não simula profissões, necessidades por morador, veículos ou deslocamentos. É uma aproximação de equilíbrio de empregos adequada ao MVP.

## 2. Arrecadação

Por dia, com alíquota-base de 10%:

| Origem | Base tributável |
|---|---|
| Residencial | `1,05 × moradores empregados + 0,30 × moradores atendidos não empregados` |
| Comércio | `2,00 × empregados comerciais` |
| Indústria | `2,00 × empregados industriais` |

A alíquota altera a receita com retorno decrescente:

`arrecadação = floor(base × (alíquota / 10) × (1 − 0,06 × max(0, alíquota − 10)))`

- 5% arrecada aproximadamente 50% da base, porém favorece a demanda.
- 10% arrecada 100% da base.
- 12% arrecada aproximadamente 105,6% da base, mas penaliza levemente a demanda.
- 15% arrecada aproximadamente 105% da base.
- 20% arrecada aproximadamente 80% da base e restringe severamente o investimento.

É uma **curva de jogo** deliberada para eliminar a estratégia dominante de imposto máximo, não uma previsão econométrica do mundo real.

## 3. Manutenção e densidade

- Cada célula de rua custa `R$ 3`/dia.
- Cada célula zoneada custa `R$ 2`/dia, mesmo vazia.
- Além disso, cada edifício ocupado custa um valor proporcional ao **quadrado** do nível:

| Tipo | Nível 1 | Nível 2 | Nível 3 |
|---|---:|---:|---:|
| Residencial | R$ 4 | R$ 16 | R$ 36 |
| Comercial | R$ 6 | R$ 24 | R$ 54 |
| Industrial | R$ 9 | R$ 36 | R$ 81 |

Portanto, um prédio de nível 3 gera mais emprego/população e também exige infraestrutura cara. **O custo de cada lote é adicional.**

## 4. Demanda R/C/I

As três demandas vão de 5 a 95 pontos e usam **desequilíbrios proporcionais ao tamanho da cidade**, em vez de apenas diferenças absolutas. Isso impede que o crescimento de uma cidade grande distorça o indicador por si só.

- Residencial: reage à proporção entre vagas de emprego e moradores.
- Comercial: reage a moradores disponíveis e a estabelecimentos já instalados.
- Industrial: reage à necessidade de empregos no mercado e à oferta instalada.
- O estoque de zonas vazias limita a expansão por penalidade de `3 × raiz_quadrada(lotes vazios acessíveis)` por setor: pintar 16 lotes reduz ~12 pontos, **não dezenas de pontos por multiplicação linear**.
- A alíquota acima de 10% reduz a demanda do setor em 2 pontos por ponto percentual; impostos abaixo de 10% aumentam a demanda.
- Crises severas (`caixa < R$ 1.500` e saldo diário negativo) reduzem a demanda em mais 12 pontos.
- Qualidade urbana ruim reduz ainda mais a demanda residencial.

**A demanda não assegura construção:** é necessário acesso à rua, espaço e passagem do tempo. Uma zona sem rua não cresce.

## 5. Qualidade urbana (5–95)

Indicador derivado, sem inventar felicidade persistida:

- Emprego real é o fator social dominante.
- Acesso a ruas favorece o bairro.
- Resultado fiscal negativo reduz a qualidade.
- Imposto residencial alto reduz a qualidade.
- Tesouro zerado durante déficit aplica penalidade adicional.

O painel ECON mostra a qualidade, a fração de moradores empregados, a arrecadação e o custo diário. O HUD mostra QUAL, DES e VAGAS.

## 6. Crescimento e crise

- Mantido o crescimento determinístico, dependente de demanda e rua, e o limite de novas construções por dia.
- Comércio e indústria desaceleram a expansão quando >17% de suas vagas operacionais estão ociosas; a desaceleração aumenta acima de 33%.
- Com déficit e caixa abaixo de R$ 3.000, os investimentos desaceleram.
- Com qualidade abaixo de 45, os investimentos desaceleram mais.
- O abandono por demanda fraca durante 12 dias permanece em CVT3 e continua determinístico.
- **Demolição custa R$ 0**, para que uma cidade com caixa zerado possa cortar despesas em vez de ficar completamente travada.
- Caixa continua limitado a zero por compatibilidade de formato. Uma cidade insolvente precisa desmontar construções caras e recuperar saldo positivo antes de voltar a construir.

## 7. Resultados do simulador — 100 dias sem intervenção

Todas as cidades têm a mesma topografia, 72 habitantes e **R$ 15.000** no dia inicial. Apenas os impostos variam. Os resultados dependem das regras determinísticas atuais; não são objetivos fixos da simulação nem promessas para cidades existentes.

| Política (R/C/I) | Caixa no dia 101 | Interpretação |
|---|---:|---|
| 5% / 5% / 5% | R$ 0 | Receita insuficiente: alíquota extrema provoca crise |
| 10% / 10% / 10% | R$ 21.638 | Cresce no começo, mas ao dia 101 apresenta saldo negativo de R$ 32/dia |
| 12% / 12% / 12% | R$ 23.483 | Ajuste moderado melhora receita inicialmente |
| 20% / 20% / 20% | R$ 15.710 | Tributos máximos já não constituem atalho de enriquecimento |

O cenário 10% ainda pode acumular dinheiro enquanto seus primeiros edifícios evoluem; posteriormente, edifícios densos e empregos ociosos tornam o saldo negativo. Uma cidade bem gerida deve reagir, não ficar parada.

## 8. Invariantes automatizadas

`CityBalanceTest` executa cenários de 100–200 dias, 18 variações de impostos/mapa, corrupção zero, demolição no caixa zero, falta de rua, desemprego total, superadensamento e reabertura CVT3. Cada dia confere orçamento, ocupação de empregos, limites de demanda, caixa e qualidade, sem arredondar dinheiro indevidamente.

**Compatibilidade:** CVT3 intacto, 3 slots e backups intactos, assinatura e package de teste preservados; sem tocar em PixelChess. Novos campos de relatório econômico são derivados das mesmas células salvas.

## 9. Próximas revisões após teste Android

1. Verificar se a perda de dinheiro começa em um ritmo compreensível ao jogador.
2. Testar construção de bairros ativos e comparar demanda R/C/I com novos lotes.
3. Ajustar faixas de qualidade e renda se a experiência real parecer severa ou fácil demais.
4. Futuramente acrescentar serviços urbanos, poluição e transporte **somente depois de modelados**, evitando números sem consequência.
