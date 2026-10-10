# Cidade Viva — demanda e economia, 0.3.0-alpha1

O sistema foi implementado em Java puro, com cálculos independentes da interface libGDX. Trata-se de uma economia inicial simplificada, não de uma simulação integral de mercado de trabalho.

## Oferta e demanda

O relatório `CityEconomy.analyze(City)` deriva seus dados dos lotes persistidos:

- Nível residencial: 12 habitantes por nível.
- Nível comercial: 10 empregos por nível.
- Nível industrial: 18 empregos por nível.
- Empregados = mínimo de habitantes e empregos; desempregados = máximo de habitantes menos empregos e zero; vagas = máximo de empregos menos habitantes e zero.
- Um lote vazio conta como oferta nova **somente se possuir rua adjacente**.

Demandas são **escores de 5 a 95** (não probabilidades ou porcentagens da população):

| Categoria | Pressão inicial antes do limite 5..95 |
|---|---|
| Residencial | `42 + (empregos - moradores) / 3 - 8 × lotes residenciais vazios` |
| Comercial | `42 + (moradores - 2 × empregos comerciais) / 5 - 9 × lotes comerciais vazios` |
| Industrial | `46 + (moradores - empregos) / 3 - 10 × lotes industriais vazios` |

Se a demanda for inferior a **24**, a zona não cresce. Qualquer crescimento também exige uma rua em um dos quatro lados do lote. A chance por dia cai de acordo com o nível, para evitar crescimento vertical excessivo. O sorteio é derivado do dia e das coordenadas, portanto reabrir uma cidade não muda aleatoriamente o futuro imediato. A demanda baixa **não** rebaixa prédios já construídos nesta etapa.

## Receitas e despesas

| Linha | Regra diária |
|---|---|
| Receita residencial | `moradores × 3 / 5` (divisão inteira) |
| Receita comercial | `empregos comerciais` |
| Receita industrial | `empregos industriais × 6 / 5` (divisão inteira) |
| Manutenção de ruas | `2 × tiles com estrada` |
| Manutenção dos lotes | `1 × tiles zoneados R/C/I` |

`RECEITA = receita residencial + comercial + industrial`.

`DESPESAS = ruas + lotes`.

`SALDO/DIA = RECEITA - DESPESAS`, podendo ser negativo. O dinheiro disponível é limitado a zero, preservando o formato de saves CVT1/CVT2. Com saldo insuficiente, o jogador não pode construir até recuperar as finanças.

## Interface

O HUD exibe a pressão **R/C/I com três barras** (verde/azul/laranja), receita, manutenção, saldo por dia, desemprego e vagas. Tocar em um lote existente mostra categoria, nível, acesso viário e, quando aplicável, um alerta de baixa demanda.

## Compatibilidade

Nenhum campo novo foi adicionado ao formato dos salvamentos. Demanda e orçamento são reconstruídos a partir dos tiles e níveis já existentes. As três cidades e backups da Alpha 4 permanecem separados. A partir da atualização, suas **regras de crescimento e orçamento mudam**, mas nenhuma construção é apagada. O campo de saldo diário do save antigo pode refletir a regra anterior até o próximo avanço de dia.

## O que ainda falta

Impostos configuráveis, felicidade, abandono e reconstrução, mercado de mercadorias, redes de transporte, serviços públicos e população por faixa etária são tarefas futuras. Também permanece pendente o teste em celular real de longa duração e da troca entre cidades após atualização.
