# QA Android — Cidade Viva 0.2.0-alpha2

## Pré-condição

- Build **debug de testes** usa `br.com.cidadeviva.game.dev`, assinatura de desenvolvimento fixa e ícone `Cidade Viva Teste`. Instalar sobre a build de testes **0.2.0-alpha1**, mantendo o app regular `Cidade Viva` 0.1.1 separado.
- Não desinstalar a build de testes antes de verificar preservação do seu save. App 0.1.1 usa dados separados; não há migração automática.

## A. Inicialização, assets e FPS

- [ ] Instalar/atualizar, iniciar sem crash ou tela preta.
- [ ] Gramado com variações sutis, água com brilho, árvores pixeladas e casas/lojas/indústrias distintas e alinhadas ao tile.
- [ ] Percorrer o mapa e zoom mínimo/máximo; sem tiles invisíveis, sprites invertidos, linhas brancas, vazamentos ou corte de edifícios na frente.
- [ ] Renderização estável após 15 minutos de partida; registrar FPS/temperatura se houver ferramenta disponível.
- [ ] Texto **0.2.0a2** exibido.

## B. Legibilidade de HUD

- [ ] Rótulos de RUA, CASAS, LOJAS, IND., APAGAR centralizados, com margem inferior e ícones identificáveis.
- [ ] Botões PAUSA/SEGUIR, 1X/2X/4X, MOVER/PINTAR, AJUDA, SALVAR, ABRIR legíveis e clicáveis no centro e nas bordas internas.
- [ ] Ferramenta ativa permanece contrastada.
- [ ] HUD não aciona construção acidental quando tocado ou arrastado.
- [ ] Verificar retrato, paisagem e sistema Android com fonte ampliada (onde disponível).

## C. Estradas e acesso dos terrenos

- [ ] Construir uma zona sem rua adjacente e observar aumento em `ZONAS SEM RUA`.
- [ ] Zona sem acesso recebe losango/indicador âmbar. Tocá-la informa `SEM RUA ADJACENTE`.
- [ ] Aguardar vários dias: zona **não** cria prédio sem rua adjacente.
- [ ] Construir uma rua em um dos quatro lados: alerta do lote desaparece e contador diminui; tocar lote indica `RUA AO LADO`.
- [ ] Observar crescimento após vários dias; não exige garantia de crescimento imediato.
- [ ] Testar rua apenas diagonal (sem ligação por lado): não conta como acesso.
- [ ] Destruir rua adjacente: status volta a sem acesso.
- [ ] Criar lotes retangulares e confirmar custo/avisos coerentes. Não perder input de PINTAR/MOVER.

## D. Salvamento e regressões

- [ ] Salvar, fechar totalmente, atualizar e reabrir. Conferir edifícios, ruas, saldo e dia.
- [ ] Carregar save existente da própria `.dev` 0.2.0a1 sem incompatibilidade.
- [ ] Comutar PAUSA e velocidade; sem crash.
- [ ] Zoom com dois dedos e rolagem em modo MOVER/PINTAR.
- [ ] App regular `.game` continua instalado e seus saves intactos.

**Para relato:** anotar modelo/Android, ação exata, resultado esperado/obtido e anexar captura ou vídeo. Compilar com sucesso não fecha os casos de QA físico.
