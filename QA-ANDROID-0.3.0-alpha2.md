# QA Android — Cidade Viva 0.3.0 Alpha 2

O APK debug foi preparado para instalar **por cima do Cidade Viva Teste**. Não desinstale a versão atual para não perder saves.

## Instalação e dados

- [ ] Instalar a versão 0.3.0a2 por cima da 0.3.0a1, sem desinstalar.
- [ ] Abrir cidade 1 e verificar o progresso; verificar cidades 2 e 3.
- [ ] Confirmar que os impostos começam em 10% nos saves de formato antigo.
- [ ] Configurar taxas diferentes em cidades 1 e 2; salvar, fechar o aplicativo e reabrir.
- [ ] Confirmar taxas independentes por cidade e sua restauração após reiniciar.
- [ ] Conferir backup dos arquivos, sem apagar saves antigos ou slots.

## Interface e economia

- [ ] ECON no topo direito abre um painel, ao lado de CIDADES e CENTRAR.
- [ ] +/- funcionam para 5–20% e travam nos limites.
- [ ] Tocar no painel não cria nem remove construções por baixo.
- [ ] Imposto maior melhora receita e reduz demanda do setor correspondente; ao baixar imposto ocorre o inverso.
- [ ] Receita total é a soma dos três setores. Despesas = ruas + lotes; saldo = receitas - despesas.
- [ ] O painel cabe na tela sem textos cortados, inclusive em aparelho de tela pequena.
- [ ] Fechar o painel retoma a simulação na velocidade escolhida, sem saltar dias.
- [ ] Testar repetidamente: pausar, pintar, mover câmera, entrar em CIDADES e ECON.

## Abandono e compatibilidade

- [ ] Em cidade com demanda comercial <20, deixar rodar mais de 12 dias e observar se alguns prédios diminuem de nível.
- [ ] O zoneamento do imóvel abandonado permanece no mapa e poderá voltar a desenvolver.
- [ ] Salvar e reabrir no meio de um período de baixa demanda preserva a progressão.
- [ ] Sem crash, tela preta, zoom incorreto ou perda de FPS em 15 minutos.

**Não instalar APKs anteriores depois de salvar no CVT3**: versões antigas não leem o novo formato. O teste automatizado não substitui a validação física.
