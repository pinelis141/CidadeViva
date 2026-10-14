# Cidade Viva 0.7 Alpha 2 — recuperação financeira

## Causa reproduzida
A prefeitura arcava com manutenção quadrática de prédios privados (4/6/9 × nível²), além das ruas e serviços. O crescimento aumentava as despesas mais rapidamente que os impostos. Remover moradias ou empregos reduzia a receita imediatamente, sem devolver investimento.

Cenário determinístico: cidade inicial, um parque, escola, clínica, gerador e bomba d'água, seguido da remoção de duas residências e um comércio, sem alterar impostos.

| Medida | Alpha 1 | Alpha 2 |
| --- | ---: | ---: |
| Saldo diário com os cinco serviços | -59 | +1 |
| Saldo diário logo após as remoções | -112 | -65 |
| Caixa após 90 dias simulados | 0 | 17.352 |
| Saldo diário após 90 dias | -115 | +169 |

Simulação sem a pausa preventiva, para avaliar se o balanceamento permite recuperação por si só. Esse cenário não representa todas as cidades.

## Mudanças
- Administração local linear: 1 por lote mais 1/2/3 por nível R/C/I, em lugar de manutenção quadrática privada. Ruas, fontes e serviços conservam seus custos.
- Demolir devolve 50% do custo original do tipo de estrutura. Não depende do nível: evoluir um prédio não produz valor de revenda extra. Floresta e terreno vazio não geram créditos.
- Toque e pincel usam o mesmo ressarcimento. A prévia do pincel apresenta a devolução; células repetidas não recebem crédito extra.
- Orçamento mostra quantos dias o caixa sustenta um déficit.
- Nos últimos sete dias de caixa, pausa automática antes de debitar o próximo dia; o jogador pode reorganizar a cidade e retomar o tempo. Acúmulo de tempo acelerado é descartado.
- Cidades e redes continuam usando CVT5; assinatura e identidade de teste preservadas. Nenhum saldo é resetado ou artificialmente completado.

## Validação
- Regressões de simulação, economia, impostos, ruas, serviços, redes, salvamentos e migrações.
- Cenário de recuperação por 180 dias e repetição idêntica após salvar/carregar.
- Ressarcimento individual/em área, duplicatas, prévia sem efeitos, terreno já vazio e ciclo construção/demolição sem lucro.
- Recuperação de investimento com caixa zerado.
- Teste do controle real de pausa financeira, incluindo tempo acelerado, mensagem e preservação do caixa.

Interface, ritmo e equilíbrio em outras cidades ainda precisam de validação em aparelho. Reestruturar muitos bairros ou manter serviços em excesso ainda pode gerar déficit; custos e receitas continuam reais.
