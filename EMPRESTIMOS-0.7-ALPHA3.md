# Cidade Viva 0.7 Alpha 3 — empréstimos

Em **ECON → EMPRÉSTIMO**, selecione uma oferta e toque em **CONFIRMAR**. A seleção sozinha não movimenta o caixa. **VOLTAR** ou o botão Voltar do Android retorna ao orçamento.

| Recebimento | Juros totais fixos | Parcelas diárias | Total a pagar |
| --- | ---: | ---: | ---: |
| R$ 3.000 | 10% | 60 × R$ 55 | R$ 3.300 |
| R$ 6.000 | 12% | 80 × R$ 84 | R$ 6.720 |
| R$ 12.000 | 15% | 100 × R$ 138 | R$ 13.800 |

- Crédito imediato, inclusive com caixa zerado. Um empréstimo ativo por cidade.
- A tela exibe saldo devedor, parcela, pagamentos restantes e previsão do saldo diário após a contratação.
- Cobrança a partir do próximo dia simulado, incluída uma única vez nas despesas. Tempo pausado não cobra parcelas.
- Custos de operação são aplicados antes da parcela. Se faltar caixa, paga-se apenas o disponível e o restante continua devido. Isso pode prolongar o prazo; não há juros compostos, multas ou encargos adicionais.
- A última cobrança respeita o saldo restante. Ao quitar, a parcela deixa de existir e outra contratação fica disponível.
- A pausa preventiva do balanceamento considera também as parcelas. O crédito não corrige automaticamente uma cidade que gasta mais do que arrecada.
- A contratação salva imediatamente. A dívida pertence à cidade/slot e persiste ao fechar e carregar o aplicativo.

## Salvamentos

Grava **CVT6** e lê CVT1–CVT5. Cidades antigas começam sem dívida, preservando caixa, impostos, população e redes. Depois de salvar nesta versão, não volte a um APK anterior: ele não reconhece CVT6.

## Validação local

- Suíte completa de regressões Java.
- 510 verificações de ofertas, cobrança dos três contratos até a quitação, falta de caixa, pagamento parcial, nova contratação, proteção contra overflow e rejeição de dívidas inválidas.
- Replay determinístico após salvar/carregar e migração de CVT5.
- Controles reais: abrir empréstimos, exigir seleção e confirmação, impedir cobrança/contratação duplicada, persistir imediatamente e voltar ao orçamento.

Compilação Android e validação visual no aparelho pendentes. O envio à branch GitHub continua bloqueado pela revisão automática de aprovação registrada nesta sessão.
