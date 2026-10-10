# QA Android — Cidade Viva 0.2.0 Alpha 4

## Pré-condição

- **Não desinstale o Cidade Viva Teste**. A atualização usa o mesmo identificador `br.com.cidadeviva.game.dev` e a mesma assinatura debug DEV.
- Se já houver uma cidade importante, anote seu dia, dinheiro e população antes de iniciar.

## Checklist de aceitação

- [ ] Iniciar o app: a cidade do antigo save Alpha 3 abre sem mudar terreno ou construções.
- [ ] Abrir **CIDADES** e conferir os três espaços. Cidade 1 deve indicar dia/população/saldo atuais.
- [ ] Selecionar espaço 2 vazio e pressionar **CRIAR NOVA**. Conferir mapa com lagos, edifícios iniciais e marcador de Cidade 2.
- [ ] Construir rua/zonas na Cidade 2, pausar e salvar. Ir para Cidade 1: tudo deve estar como antes.
- [ ] Voltar à Cidade 2: construções feitas lá devem continuar intactas.
- [ ] Criar Cidade 3, modificar e salvar. Alternar entre as três cidades.
- [ ] Fechar o jogo completamente, reabrir e confirmar que retorna à última cidade ativa.
- [ ] Selecionar um espaço ocupado: ele deve **abrir**, não apagar ou gerar cidade nova.
- [ ] Toques fora das opções do menu não devem construir no mapa por trás do painel.
- [ ] Pausa, câmera, modos MOVER/PINTAR, pincel de rua, zoneamento e SALVAR/ABRIR devem continuar funcionando no espaço atual.
- [ ] Verificar disposição do menu e dos rótulos em retrato e paisagem, se aplicável.
- [ ] Verificar indicador de terreno sem rua e ausência de travamentos em mapas diferentes.

## Testes de segurança automatizados

- O espaço 1 usa `salvamento.cvt` exatamente como antes; migração destrutiva é proibida.
- Cada slot possui `.bak`, `.tmp` e `.corrupt` próprios, sem compartilhar arquivos.
- Abrir slot com arquivos inválidos não deve substituí-lo por uma cidade nova.
- Uma troca interrompida deve deixar um save anterior utilizável e o slot atual reconhecível.
- Nenhum arquivo é apagado na interface de seleção de cidades.

**Critério de conclusão:** APK compilado + todos os testes acima validados no Android real. Se algum falhar, registrar vídeo e versão.
