# Auditoria do Cidade Viva — 0.5 Alpha 3

Base: `d441d357ddd194f6d955bc9dbe16b556a16bc144` (Alpha 2). Revisão de todos os módulos de produção Java, configuração Android/desktop, fluxo de salvamentos, controles, atlas e CI. Esta auditoria combina leitura de código, reprodução por testes e compilação. Não certifica ausência de bugs nem substitui validação em telefone real.

| ID | Problema encontrado | Correção / verificação |
| --- | --- | --- |
| A01 | Rotação reinicializava posição e zoom da câmera | Preservar enquadramento, aplicar novos limites e cancelar dedos antigos; teste com o adaptador real |
| A02 | Cancelamento, pausa e abertura de overlays deixavam gestos ativos | Reset central; tratar touchCancelled; liberar sem construir após interrupção |
| A03 | Arrastar no modal e retornar à origem podia acionar o botão | Acumular percurso; movimento e múltiplos dedos cancelam o clique |
| A04 | Pincel podia selecionar atrás do HUD e confirmar ao soltar sobre botões | Proteger o HUD e o inspetor, cancelar confirmação fora do mapa visível |
| A05 | Pincel iniciado fora dos limites podia atravessar até a cidade | Rejeitar o gesto desde a origem inválida |
| A06 | Voltar na ajuda abria cidades; atalhos e roda alcançavam conteúdo por trás dos overlays | Fechar ajuda primeiro, bloquear controles atrás do modal e capturar BACK no Android |
| A07 | Quedas 3→2 e 2→1 exibiam verde e texto de crescimento | Basear feedback no sentido da mudança; mostrar deterioração em vermelho |
| A08 | Arquivo temporário válido era ignorado após interrupção da rotação do save | Primário válido tem prioridade; temporário validado recupera antes do backup. Arquivo temporário corrompido não é tratado como slot vazio |
| A09 | Diálogo ECON ficava cortado em telas baixas de paisagem | Limitar escala pela altura do maior diálogo sem mudar escala de telas normais |
| A10 | ABRIR carregava cidade com contador parcial de dia e inspeção anteriores | Zerar relógios visuais/de simulação, seleção e gestos após leitura bem-sucedida |
| A11 | Slot ativo corrompido aparecia como EM USO/CONTINUAR | Priorizar PROTEGIDA no status e ação |
| A12 | Ordenação de empregos dependia de APIs 24 com minSdk 23 | Substituir List.sort/Comparator.comparingInt/thenComparingInt por Collections.sort e Comparator.compare compatíveis, mantendo desempate por ID |
| A13 | Distribuição desktop não incluía atlas, embora gradle run funcionasse com diretório de assets | Incluir assets nos recursos desktop; verificar installDist e conteúdo do JAR |

## Evidências e regressões

- Regressões Java existentes: construção e custo atômico, conectividade, empregos, demanda, evolução/abandono, impostos, balanceamento prolongado, terrenos, foco da câmera, metas, animações, slots, CRC32 e migrações CVT1/CVT2/CVT3.
- Novos casos de save: interrupção após rotação, primeiro save apenas temporário, temporário inválido e promoção após recuperação.
- Novas dimensões: 640×360, 800×320 e 320×640, verificando encaixe do diálogo e espaço de mapa.
- `CityControlsTest`: 16 verificações do adaptador real, sem contexto OpenGL: pinça, resize, cancelamento, modal, teclado, pausa, pincel/HUD, origem fora do mapa e load.
- CI passou a executar testes de controles, instalação desktop e build Android. Atlas permanece com 125 sprites.
- APK continua com package DEV, assinatura DEV e saves CVT3. Versão 0.5.0-alpha3-auditoria, versionCode 17.

Referência para compatibilidade API24: [Android Comparator](https://developer.android.com/reference/java/util/Comparator). A análise do DEX anterior encontrou referências a java.util.Comparator.comparingInt/thenComparingInt e java.util.List.sort. O cenário em Android 6 não foi executado em dispositivo/emulador nesta auditoria; a dependência incompatível foi removida do simulador.

## Limites e testes físicos pendentes

Conferir rotação com cidade aberta; alternar app durante um arrasto; soltar pincel sobre menus; voltar na ajuda; abrir os três slots; confirmar saves, impostos e população; conferir deterioração e legibilidade em retrato/paisagem. Não foram medidos FPS, uso de bateria, comportamento da GPU ou interação em Android físico. Serviços públicos, classes sociais e footprints maiores continuam sendo funcionalidades futuras, não bugs desta versão.
