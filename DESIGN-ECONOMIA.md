# Cidade Viva — demanda e economia, 0.3.0-alpha2

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

O imposto de cada categoria acrescenta à fórmula o termo `− 3 × (alíquota − 10)`. Assim, 10% mantém a demanda do modelo anterior; impostos maiores reduzem demanda, menores aumentam, sempre limitada a 5..95. As alíquotas vão de **5% a 20%** em passos de 1% e são específicas de cada cidade.

Se a demanda for inferior a **24**, a zona não cresce. Qualquer crescimento também exige uma rua em um dos quatro lados do lote. A chance por dia cai de acordo com o nível, para evitar crescimento vertical excessivo. O sorteio é derivado do dia e das coordenadas, portanto reabrir uma cidade não muda aleatoriamente o futuro imediato. ### Abandono gradual

Quando a demanda de um setor fica **abaixo de 20** por **12 dias consecutivos**, cada imóvel construído do setor corre um risco diário pequeno de perder um nível (mínimo 0). Para um lote sem rua adjacente, há também risco de abandono, com início apenas após a fase inicial da cidade. Rebaixamento não apaga o zoneamento: o terreno poderá voltar a crescer quando as condições melhorarem. A decisão por dia e coordenada é determinística, e a sequência de dias fracos é salva para evitar reiniciar a contagem ao fechar o app.

## Receitas e despesas

| Linha | Regra diária |
|---|---|
| Receita residencial | `moradores × 3 × taxa residencial / 50` |
| Receita comercial | `empregos comerciais × taxa comercial / 10` |
| Receita industrial | `empregos industriais × 6 × taxa industrial / 50` |
| Manutenção de ruas | `2 × tiles com estrada` |
| Manutenção dos lotes | `1 × tiles zoneados R/C/I` |

`RECEITA = receita residencial + comercial + industrial`.

`DESPESAS = ruas + lotes`.

`SALDO/DIA = RECEITA - DESPESAS`, podendo ser negativo. O dinheiro disponível é limitado a zero, preservando o formato de saves CVT1/CVT2. Com saldo insuficiente, o jogador não pode construir até recuperar as finanças.

## Interface

O atalho **ECON** no canto superior direito abre um painel modal com três alíquotas ajustáveis usando + e −, demanda e tempo de demanda fraca por setor, receita detalhada, despesas e saldo diário. Enquanto o painel está aberto, a simulação fica suspensa, e os toques no painel não constroem no mapa. Cada ajuste é persistido no espaço ativo.

O HUD exibe a pressão **R/C/I com três barras** (verde/azul/laranja), receita, manutenção, saldo por dia, desemprego e vagas. Tocar em um lote existente mostra categoria, nível, acesso viário e, quando aplicável, um alerta de baixa demanda.

## Compatibilidade e salvamentos

A versão Alpha 2 usa o formato **CVT3**, com CRC32 e os mesmos 48×48 terrenos, adicionando as três alíquotas e três contadores de dias fracos. **Leitura de CVT1 e CVT2 é mantida**; arquivos antigos carregam impostos de 10% e contagem zero. Os três slots continuam independentes, e a gravação mantém `.tmp`, validação antes da troca e `.bak` para recuperação.

**Importante:** depois que a Alpha 2 salvar uma cidade como CVT3, um APK antigo pode não conseguir abri-la. Não instale versões anteriores esperando abrir saves da Alpha 2. A atualização para frente não apaga cidades existentes.

## O que ainda falta

Felicidade, alíquotas de serviços, mercado de mercadorias, redes de transporte, serviços públicos e população por faixa etária são tarefas futuras. Também permanece pendente o teste em celular real de longa duração e da troca entre cidades após atualização.
