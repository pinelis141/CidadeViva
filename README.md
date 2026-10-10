# Cidade Viva — Java/libGDX Android

Versão em desenvolvimento: **0.3.0-alpha2**.

### Novidades da Alpha 2

- Atalho **ECON** no topo direito abre o orçamento detalhado.
- Alíquotas residencial, comercial e industrial entre **5% e 20%**, com botões +/−. Cada cidade guarda suas próprias taxas.
- Impostos maiores aumentam a receita e reduzem a demanda R/C/I; taxas menores fazem o inverso.
- **Abandono gradual:** demanda persistentemente baixa pode reduzir o nível de um edifício, sem eliminar o terreno zoneado.
- **Formato CVT3** com CRC32 armazena taxas e tempo de demanda fraca. Lê CVT1/CVT2 antigos e preserva os 3 slots e backups; versões anteriores não leem os saves CVT3.
- Teste Android: [QA-ANDROID-0.3.0-alpha2.md](QA-ANDROID-0.3.0-alpha2.md).

### Economia desta versão

Demanda residencial, comercial e industrial no HUD (R/C/I), crescimento condicionado a demanda + rua próxima, vagas e desemprego, receita/ manutenção e saldo por dia. O simulador permanece em Java puro; informações completas em [DESIGN-ECONOMIA.md](DESIGN-ECONOMIA.md). Testes físicos: [QA-ANDROID-0.3.0-alpha1.md](QA-ANDROID-0.3.0-alpha1.md).

**Migração segura:** CVT1/CVT2 são lidos e convertidos para CVT3 no próximo salvamento. Três slots e backups continuam separados. Não faça downgrade do aplicativo depois de salvar no CVT3.

# Cidade Viva — Java / libGDX / Android

**Versão de testes: 0.2.0-alpha4** — interface corrigida, indicadores de zonas sem rua e primeiros sprites pixel art originais.

City builder isométrico original, offline. **Não utiliza Godot.** O repositório do Pixel Chess é separado e não deve ser modificado por este projeto.

## 0.2.0-alpha1 — construção em área (prévia funcional)

- Novo **modo PINTAR/MOVER**, alternado pelo terceiro botão superior. **MOVER:** arraste para deslocar o mapa, toque para construir um tile. **PINTAR:** arraste para construir uma rua contínua ou selecionar um retângulo de zonas/demolição.
- O pincel de **RUA** percorre tiles ortogonalmente conectados, inclusive com gestos diagonais rápidos.
- **CASAS**, **LOJAS**, **IND.** e **APAGAR** aplicam retângulos de até **16 × 16** tiles por gesto, com prévia verde (disponível) / vermelha (bloqueado), número de tiles e custo.
- Pagamento **atômico**: se o saldo não cobre os tiles disponíveis, não se aplica construção parcial; tiles bloqueados são ignorados sem custo, tiles já construídos não são cobrados novamente.
- Conexões viárias desenhadas conforme os quatro vizinhos reais (pontas, retas, curvas, T e cruzamentos) — sprites pixel art finais ainda pendentes.
- Botão **CENTRAR** para reenquadrar a cidade; edifícios menores e crescimento vertical desacelerado.
- Pausa, aceleração 1×/2×/4×, ajuda, salvar e abrir continuam acessíveis.
- Mantém compatibilidade com salvamentos **CVT1/CVT2** das versões anteriores, com CRC32, backup e recuperação.

**AVISO:** prévia de 0.2.0, ainda sem atlas de pixel art ou arte final. Testes automáticos e build Android não substituem a validação em celular real.


## Importante: instalação e saves dos APKs de testes

A partir desta prévia, o APK debug usa identificador **`br.com.cidadeviva.game.dev`** e aparece como **Cidade Viva Teste**. Ele pode ser instalado **ao lado** do APK v0.1.1 (`br.com.cidadeviva.game`), que permanece com seus salvamentos e dados. Os dados **não migram automaticamente** entre os dois aplicativos, pois o Android os isola por identificador. **Não desinstale a v0.1.1 para testar esta versão** se quiser preservar seu progresso.

Novas compilações de teste do Cidade Viva usam uma assinatura debug estável, compartilhada apenas para a distribuição de builds DEV. A chave de desenvolvimento está no repositório **de propósito** e não deve ser confundida com uma chave de publicação; o aplicativo final precisa de uma **chave release privada** e do ID de produção.

## Recursos já existentes desde o MVP

- Mapa procedural isométrico **48×48** com vegetação e água.
- Zoneamento residencial, comercial e industrial, expansão simplificada e economia por dias.
- Gestos de câmera, zoom ancorado e navegação multi-touch.
- Persistência local e teste de integridade com backup automático.
- Build Android debug independente e versão desktop Java.

## Compilar e testar

GitHub Actions: **Actions → Compilar APK Android** (artefato `CidadeViva-Android-debug`), ou em computador com Java 17, Android SDK 35, Gradle 8.9:

```bash
bash tools/test-core.sh
gradle :android:assembleDebug
# Arquivo em android/build/outputs/apk/debug/
```

O APK debug é para testes e não pode ser distribuído como release da Play Store sem assinatura release. Confira [QA-ANDROID-0.2.0-alpha1.md](QA-ANDROID-0.2.0-alpha1.md) para o teste manual.

## Atualizações da 0.2.0-alpha3

- **45 sprites isométricos originais** em atlas PNG 576 × 768; 16 combinações de vias, três categorias de edifícios × três níveis × duas variações; água, vegetação e terrenos variáveis.
- Renderização com `SpriteBatch` e textura `Nearest`, usando um atlas carregado apenas uma vez; regras da cidade continuam livres de libGDX.
- HUD superior ampliado para exibir `ZONAS SEM RUA`; barra inferior com rótulos altos, ícones diferenciados e áreas clicáveis revisadas.
- Zonas sem rua adjacente são destacadas em âmbar; tocar um terreno zoneado mostra tipo, nível e acesso.
- Testes Java adicionais verificam a contagem de zonas sem estrada, mudança de status, persistência e que um lote sem acesso permanece sem edifício.
- **Documentação:** [`ART-DIRECTION.md`](ART-DIRECTION.md) e [`QA-ANDROID-0.2.0-alpha3.md`](QA-ANDROID-0.2.0-alpha3.md).

A versão de teste mantém **package separado** (`br.com.cidadeviva.game.dev`) e assinatura **apenas de desenvolvimento**, para preservar o app antigo. Não usar chave DEV para publicação. A arte ainda será ampliada e refinada antes do lançamento.

## Alpha 3: visual de terreno e arquitetura

- Atlas integrado com **80 sprites pixel art originais**: novas árvores, água com
  litoral em 16 configurações, grama em manchas e terceira variação de prédios.
- Renderizador calcula a **máscara de água** sem alterar regras de custo/estrada.
- Novos mapas geram lagos conectados. **Cidades já salvas não terão o terreno
  recriado nem substituído** após atualizar — isto evita perda de progresso.
- Fundo verde-escuro fosco ao afastar a câmera até as bordas do mapa.
- Arquitetura por categoria/nível, sprites selecionados deterministicamente.

### Como testar

1. Atualize o app **Cidade Viva Teste** com o APK de debug Alpha 3, sem desinstalar.
2. Confirme que a cidade antiga abre sem mudar zonas/edifícios e que SALVAR/ABRIR preserva os dados.
3. Observe variações de gramado, árvores e prédios, e afaste o zoom para avaliar o fundo.
4. As duas novas formações de lagos só surgem em cidades **novas**. Por enquanto,
   falta menu seguro para abrir outro slot; não é necessário apagar dados do app.
5. Testes lógicos: `bash tools/test-core.sh`; verificar arte:
   `python3 tools/verify_atlas.py`; compilar via GitHub Actions.

## Alpha 4 — Minhas Cidades e novos mapas sem perda de progresso

- O botão **CIDADES**, na barra superior, abre o gerenciador de três espaços independentes.
- **Cidade 1** utiliza o arquivo antigo `cidade-viva/salvamento.cvt`, preservando saves CVT1/CVT2 já existentes. Nenhum save é apagado, renomeado ou convertido sem necessidade.
- **Cidade 2** e **Cidade 3** ficam em `cidade-viva/slots/cidade-2.cvt` e `cidade-viva/slots/cidade-3.cvt`, cada um com seus próprios arquivos de backup e recuperação.
- O menu mostra população, dia, dinheiro, espaço em uso, espaço vazio ou arquivo protegido por corrupção. Escolha o espaço e confirme em **ABRIR CIDADE** ou **CRIAR NOVA**.
- Trocar salva a cidade atual *antes* de ativar o próximo espaço. Uma falha na leitura de um espaço não sobrescreve dados antigos. Espaços ocupados **não podem ser reiniciados** por esse menu.
- O espaço selecionado é lembrado após fechar/reabrir o app.
- Cidades novas dos espaços 2 e 3 têm lagos e matas com pequenas variações determinísticas. O arquivo salvo continua representando todos os tiles sem alterar os formatos CVT1/CVT2.
- O botão superior **CENTRAR** permanece disponível ao lado de CIDADES.
- **AVISO:** a primeira cidade não pode ser recriada enquanto já existir um save; isso é intencional para proteger seu progresso. Não desinstale o app de testes para criar novo mapa.
- **QA pendente:** avaliar o menu com toques, orientação, troca de slots, reinício e visual do novo mapa no Android real. Checklist em `QA-ANDROID-0.2.0-alpha4.md`.
