# Cidade Viva 0.7 Alpha 1 — energia e água

A versão 0.6 foi validada pelo usuário. Esta etapa adiciona abastecimento de energia e água, preservando parques, escola e posto de saúde.

| Fonte 1×1 | Construção | Manutenção/dia | Produção |
| --- | ---: | ---: | ---: |
| Gerador elétrico | 2.600 | 36 | 600 unidades de energia |
| Poço / reservatório | 1.800 | 24 | 600 unidades de água |

Abra **SERV.** no rodapé para escolher as fontes. Nesta etapa, as ruas distribuem as redes automaticamente: não há fios ou tubulações separados. A distribuição percorre ruas conectadas, sem alcance máximo adicional no mapa 48×48, e aloca unidades por proximidade/desempate por coordenada. Mais fontes ampliam capacidade, sem duplicar atendimento. Redes desconectadas podem ter capacidade ociosa enquanto outra região sofre falta.

## Ativação gradual e saves

Cada cidade começa com energia/água em **PLANEJAMENTO**, sem exigir essas redes. Construir a primeira fonte ativa permanentemente a respectiva necessidade. Ativar água não ativa energia automaticamente. Quando energia já está ativa, o poço precisa de 12 unidades de energia para produzir água. Demolir todas as fontes não desativa a exigência; o estado permanece após salvar/reabrir. Construções inválidas ou sem saldo não ativam a rede.

Isso vale para cidades antigas e novas e permite adaptar a cidade antes de assumir o requisito. O relatório mostra PLANEJAMENTO em vez de anunciar fornecimento fictício. Não se acrescentam prédios, dinheiro ou fontes invisíveis aos saves anteriores.

O formato novo é **CVT5** e lê CVT1/CVT2/CVT3/CVT4. Guarda tipos de fonte e duas flags de ativação, com CRC32, temporários, backups e os três espaços. Preserva package e assinatura DEV. **Atualizar sobre Cidade Viva Teste, sem desinstalar. Não voltar a APKs anteriores depois de salvar em CVT5.** Versão Android 0.7.0-alpha1-redes, versionCode 19.

## Consumo e consequências

- Residências: 12 unidades por nível em cada rede.
- Comércio: 10 por nível em cada rede.
- Indústria: 30 de energia e 18 de água por nível.
- Parque/escola/saúde: 8 de energia e 4 de água por prédio.
- Poço: 12 de energia; gerador não depende de água nesta etapa.

Falta parcial de energia reduz satisfação residencial em até 12 pontos; água, até 18. Edifícios já ocupados não sobem de nível sem abastecimento completo das redes ativas. Um lote vazio ainda pode iniciar ocupação de nível 1 e depois estagnar. Regras anteriores de satisfação, demanda, escola e saúde continuam valendo.

Comércios/indústrias sem abastecimento completo deixam de oferecer vagas operacionais e gerar os respectivos impostos de emprego. A capacidade nominal de vagas permanece visível. Serviços públicos sem abastecimento param de atender. Indústria parada por falta de rede deixa de emitir sua poluição local; outros fatores de poluição permanecem.

Manutenção das fontes entra em **Serv./Redes** no orçamento, inclusive quando isoladas. Fontes essenciais não desligam simplesmente porque o caixa municipal zerou: o custo continua e a atividade econômica pode ajudar a recuperar o orçamento. Serviços sociais mantêm a regra anterior de suspensão sem caixa. Não há combustível, aquecimento, pressão física, esgoto, redes separadas, manutenção por peça ou contratos nesta fase; unidades são abstratas, não kWh/litros reais. O poço representa captação subterrânea e não exige proximidade do lago.

## Interface e validação

SERV. e ECON mostram estado das redes, unidades distribuídas, demanda e capacidade operacional. HUD e reclamações alertam falta de energia/água. O inspetor da fonte informa capacidade/manutenção/operação. Dois desenhos de fonte integram o renderizador sem mudar o atlas anterior.

Testes cobrem conectividade e recuperação, conservação de unidades, múltiplas fontes, capacidade insuficiente, empregos/serviços/satisfação, alimentação elétrica do poço, custo exato, flags após demolição e reload, replay determinístico e migração CVT4/CVT5. Regressões anteriores continuam no CI, junto aos testes do adaptador real e build Android/desktop.

QA físico: abrir cidade validada, conferir PLANEJAMENTO, construir gerador e poço ao lado da rede, conferir orçamento, romper/religar rua, salvar/reiniciar e trocar slots. Não apagar a cidade para testar. Validar os dois itens no menu em retrato e paisagem. Nenhuma execução em Android físico foi feita nesta entrega.
