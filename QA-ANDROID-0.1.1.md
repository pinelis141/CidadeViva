# Cidade Viva 0.1.1 — Checklist de aceite Android

O APK compilado não equivale a um teste em dispositivo real. O checklist abaixo precisa ser verificado manualmente após instalar a build de teste. Caso falhe, registre o aparelho, orientação, passo a passo, comportamento esperado/observado e captura de tela/vídeo na issue correspondente.

## Instalação e renderização — CV-001

- [ ] A instalação do APK não falha.
- [ ] O jogo inicia sem tela preta/travamento ou fechamento inesperado.
- [ ] O tabuleiro 48×48 e os edifícios iniciais são visíveis.
- [ ] Não há botões fora da área visível no modo retrato nem no paisagem.
- [ ] A interface fica legível no aparelho alvo (Samsung S23 Ultra).

## HUD e ações — CV-002 e CV-006

- [ ] Selecionar cada ferramenta e tocar no mapa constrói/demole conforme o tipo.
- [ ] Deslizar sobre botões não os ativa involuntariamente.
- [ ] Tocar sobre a barra de estatísticas, nos botões e nas bordas não constrói no mapa.
- [ ] SALVAR confirma a operação; ABRIR restaura estado.
- [ ] PAUSA congela a contagem de dias; SEGUIR retoma; 1X/2X/4X mudam velocidade.
- [ ] AJUDA abre a explicação; qualquer toque a fecha sem construir.

## Câmera — CV-003

- [ ] Um toque curto constrói no tile correto sem deslocamento.
- [ ] Arrasto de um dedo move a câmera sem construir.
- [ ] Pinça amplia e reduz sem saltos visíveis, mantendo ponto sob os dedos.
- [ ] Levantar um dedo após uma pinça não constrói no mapa.
- [ ] Zoom máximo e mínimo não permitem posicionar estruturas em tiles errados.
- [ ] Pan não perde completamente o mapa, nos quatro extremos.

## Persistência — CV-004

- [ ] Construir 3 objetos, salvar e fechar completamente o aplicativo.
- [ ] Reabrir mantém mapa, saldo e dia.
- [ ] Fechar pelo sistema operacional também salva.
- [ ] Abrir um save de 0.1.0 funciona no 0.1.1 (migração de leitura CVT1).
- [ ] Backup é recuperado quando um arquivo principal está corrompido (testes automatizados cobrem a lógica; em produção, conferir com ambiente de QA apropriado).

## Resultado

**Estado:** aguardando execução manual em aparelho Android. Não fechar CV-001 enquanto estiver sem evidência física.
