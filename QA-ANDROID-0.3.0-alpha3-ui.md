# QA Android — Cidade Viva 0.3.0 Alpha 3 UI

## Instalação e continuidade

- [ ] Instalar o APK `.game.dev` por cima do Cidade Viva Teste Alpha 2 **sem desinstalar**.
- [ ] A cidade abre sem tela preta, crash ou alteração do mapa e dos três slots.
- [ ] Impostos anteriores e arquivo CVT3 são mantidos; salvar, fechar e reabrir preserva valores.

## Legibilidade do HUD 2.0

- [ ] Dinheiro, moradores, dia e saldo legíveis à primeira vista, sem texto cortado.
- [ ] Testar valores negativos, positivos, 6 e 7 dígitos: sem texto sobreposto.
- [ ] Três cartões R/C/I com letras/números coerentes com `ECON` e barras respondendo ao crescimento.
- [ ] Alertas de zonas sem rua, desemprego e vagas legíveis; contraste sobre mapa claro/escuro.
- [ ] Botões PAUSA, VELOCIDADE, MOVER/PINTAR, AJUDA, SALVAR e ABRIR sem colisões.
- [ ] Cinco ferramentas mostram ícone, nome e custo; toque sempre escolhe a ferramenta correta.
- [ ] Seleção ativa distinta dos botões não selecionados.
- [ ] HUD não fica escondido por barras de navegação, gestos Android ou recortes de tela.

## Telas e interações

- [ ] ECON: três impostos ajustam de 5 a 20% corretamente; receita/custo/saldo aparecem.
- [ ] ECON bloqueia toque no mapa por trás e pausa a simulação enquanto aberto.
- [ ] CIDADES: três cartões distintos; abrir/voltar/criar preservam saves separados.
- [ ] Abrir e fechar AJUDA sem construir inadvertidamente.
- [ ] Tocar lote residencial: painel de inspeção mostra nível, rua, demanda e imposto.
- [ ] O painel de lote fecha ao toque e não constrói no mapa por trás; seleção de ferramenta também fecha.
- [ ] Arrastar estradas e zonas em PINTAR, câmera em MOVER, pinça com dois dedos continuam funcionando.
- [ ] Zoom máximo/mínimo não altera tamanho visual nem área clicável da UI.
- [ ] Testar em retrato e, se suportado, paisagem; verificar botões e textos em dimensões de tela pequenas.
- [ ] Jogar 10–15 minutos sem falha, travamento ou consumo excessivo de memória.

## Resultado

- [ ] Anexar pelo menos uma captura do HUD e um vídeo curto abrindo ECON, CIDADES, inspecionando lote e construindo rua.
- [ ] Relatar problemas com ação exata, resultado e orientação de tela.
