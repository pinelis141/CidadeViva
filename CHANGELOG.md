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
