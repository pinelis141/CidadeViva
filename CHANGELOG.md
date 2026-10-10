# Histórico de versões

## 0.2.0-alpha3 — 10/10/2026

- Atlas original ampliado de 45 para **80 sprites** (576×1280 RGBA).
- **16 transições de margem de água** conforme adjacência real com tiles vizinhos.
- **6 novas variantes de árvore**, incluindo copas arredondadas e árvore outonal.
- **4 variações detalhadas de gramado**, distribuídas em manchas em vez de xadrez.
- **9 modelos arquitetônicos adicionais**, preservando as categorias e os níveis existentes.
- **Novos mapas** passam a gerar dois lagos contínuos e agrupamentos de vegetação.
- Fora dos limites do mapa, o fundo é verde escuro em vez de espaço quase preto.
- Testes Java de máscara de água, conectividade de lagos, determinismo e persistência
  (9.220 verificações adicionais).
- Formato de save CVT2 **inalterado**; cidades antigas não são modificadas.
- Modo de testes Android `.dev` preservado, com mesma chave estável, versão code 5.
- Não inclui ainda menu de nova cidade: para preservar o save, terreno antigo
  não é substituído automaticamente. Não excluir dados só para ver lagos novos.

# Histórico de versões

## 0.2.0-alpha2
- HUD inferior com mais espaço, legendas reposicionadas, letras maiores e pictogramas de ferramentas distintos.
- Contador de zonas sem rua, alerta âmbar nos lotes e descrição contextual ao tocar.
- Atlas original com 45 sprites em pixel art isométrica; renderização via um único `SpriteBatch`/texture atlas.
- Regeneração de sprites reprodutível via Pillow em desenvolvimento e validação PNG na CI.
- Regressões Java cobrindo terrenos sem acesso, crescimento, salvamento, regiões clicáveis e zoom.
- APK de testes `.dev` com assinatura estável, compatível com atualização de 0.2.0-alpha1.

# Histórico de mudanças

## 0.2.0-alpha1 — Construção em área

- Modo PINTAR/MOVER separado da navegação padrão; gestos com dois dedos preservados.
- Pincel viário quatro-conectado e zoneamento/demolição retangulares até 16×16.
- Custo e disponibilidade no preview; lote atômico sem cobrança duplicada.
- Indicação visual de conectividade real das ruas (marcação procedural provisória).
- Centralização da câmera, melhoria de legibilidade e edifícios iniciais menos altos.
- Novos testes puros Java de construção por área, monetização e linhas.
- Leitura de saves antigos sem alteração de formato.
- APK de desenvolvimento com ID `.dev`, instalação paralela ao app legado e assinatura debug estável.

## 0.1.1 — Estabilidade Android

- Controles touch e zoom, limites de câmera, pausa e velocidade.
- Backup, CRC32 em CVT2 e leitura de CVT1; testes Java.
