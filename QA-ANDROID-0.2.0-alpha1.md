# QA Android — Cidade Viva 0.2.0-alpha1

**Status inicial:** testes Java automatizados concluídos; execução Android real pendente. Capturas e vídeos do usuário são evidências, mas não certificam funções que não foram testadas.

## Testes principais no celular

- [ ] Instalar a atualização sobre a v0.1.1 e confirmar que a cidade antiga continua intacta; abrir novamente após fechar totalmente o aplicativo.
- [ ] Verificar que o título indica `0.2.0a1` e que há seis ações superiores: PAUSA, velocidade, MOVER/PINTAR, AJUDA, SALVAR e ABRIR.
- [ ] No modo MOVER, deslizar um dedo **navega**, toque curto **constrói apenas um tile**. Duas mãos/duas pontas de dedo fazem pan e zoom, sem construir.
- [ ] Tocar PINTAR e selecionar RUA. Arrastar na diagonal cria uma sequência **conectada** de ruas; o custo corresponde aos tiles válidos sem duplicações.
- [ ] Selecionar CASAS, LOJAS ou IND., tocar e arrastar em diferentes diagonais: prévia de retângulo verde/vermelho, custo, 16×16 máximo. Soltar aplica uma única transação.
- [ ] Arrastar um retângulo incluindo água, ruas ou edifícios já existentes: células bloqueadas não são modificadas nem cobradas.
- [ ] Selecionar APAGAR e arrastar: demole áreas de até 16×16; não desconta por grama livre.
- [ ] Com pouco dinheiro, selecionar um retângulo de custo maior que o saldo: **nenhuma** construção é aplicada, saldo preservado.
- [ ] Criar curvas, T, cruzamentos e pontas viárias; verificar marcas correspondentes aos tiles vizinhos, inclusive após apagar ruas.
- [ ] Tocar CENTRAR: o foco retorna à cidade, preservando zoom.
- [ ] Iniciar pintura e adicionar segundo dedo: cancelar gesto de construção (sem gastar) e permitir navegação/zoom.
- [ ] Testar orientação, hitbox e legibilidade em retrato e, se suportado, paisagem.
- [ ] Salvar, encerrar completamente, voltar e reabrir; confirmar ruas, zonas e saldo corretos.

## Critérios de regressão

- [ ] Dia e população evoluem, botão pausa interrompe avanço de dia; velocidade acelera.
- [ ] Nenhum botão é acionado após arrastar sobre seu retângulo; não há construções ao tocar no HUD.
- [ ] Não há queda brusca de FPS em gesto de retângulo 16×16.
- [ ] Compatibilidade com save anterior v0.1.1, inclusive recuperação do backup quando aplicável.

## Limites conhecidos

- Sprites de pixel art originais, acabamento do terreno, atlas, iluminação e detalhes visuais ainda não estão prontos; esta entrega é a **primeira etapa** da v0.2.0.
- Sistema de ruas tem conexões **visuais**, ainda sem tráfego ou roteamento.
- Seleção de área não aplica edificações diretamente: aplica **zoneamento**, que passa a crescer com a simulação.
- Testes de toque, suavidade da câmera e ergonomia exigem dispositivo real.
