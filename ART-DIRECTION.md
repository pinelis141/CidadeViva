# Cidade Viva — Direção de arte e pipeline do atlas 0.2.0-alpha3

## Identidade

City builder isométrico próprio, para celulares Android, com pixel art simples e legível. Evitar a reprodução de sprites, interface, construções ou o nome de TheoTown; o jogo pertence ao projeto Cidade Viva.

- **Câmera isométrica 2:1:** tile de 64 × 32 unidades no mundo (half-width 32, half-height 16).
- **Sprites ancorados em um retângulo transparente de 72 × 128:** base no diamante central `(36,112)` de cada célula; projeção do libGDX em `x=sx-36`, `y=sy-16`.
- **Escala pixelada:** usar filtro `Nearest`, sem blur. Todas as construções devem caber dentro de sua célula 72 × 128 e respeitar ordem diagonal do mapa.
- **Paleta:** verdes acinzentados para natureza; águas azul-petróleo; habitações em tons quentes; lojas em azul/verde-mar; indústrias em terracota e cinza.
- **Sem assets de terceiros:** o primeiro atlas foi desenhado programaticamente com polígonos/pixels originais; nenhum asset de outro jogo foi utilizado.

## Índices do atlas (`android/assets/city/atlas.png`)

| Índices | Conteúdo |
|---|---|
| 0–2 | três variantes de terreno gramado |
| 3–4 | duas variantes de água |
| 5 | parcela residencial vazia |
| 6 | parcela comercial vazia |
| 7 | parcela industrial vazia |
| 8–23 | 16 combinações de 4 ligações viárias |
| 24–26 | três variantes de árvores |
| 27–32 | casas: três níveis, duas variantes |
| 33–38 | comércio: três níveis, duas variantes |
| 39–44 | indústria: três níveis, duas variantes |
| 45–60 | água: 16 margens parametrizadas por vizinhança (SE/NW/SW/NE) |
| 61–66 | seis novas árvores de copas largas e outonais |
| 67–70 | gramado com flores, pedras, moitas e solo seco |
| 71–79 | terceira variação para 3 categorias × 3 níveis de prédios |

A mesma textura usa oito colunas de sprites, cada um 72 × 128, total **576 × 1280 pixels**, e é carregada **uma vez** em `PixelArtAtlas`. `CityGame` desenha as células na ordem isométrica; a renderização fica separada da simulação pura Java. Os índices são apenas visuais e não aparecem nos saves.

## Regerar o atlas

Com Python 3 + Pillow, execute:

```bash
python3 -m pip install pillow
python3 tools/generate_pixel_atlas.py
python3 tools/verify_atlas.py
```

Pillow é necessário **somente** para gerar os recursos em desenvolvimento. O APK carrega o arquivo PNG já empacotado, sem Python/Pillow. A CI verifica a estrutura do atlas com biblioteca padrão do Python.

## Regras de produção futuras

1. Evoluir sprites por bioma, ruas pavimentadas/sem pavimentação e edificações com detalhes mais variados, mantendo a paleta.
2. Usar a base 2:1 e o mesmo ponto de ancoragem para evitar saltos visuais.
3. Conservar versões reduzidas legíveis no celular e contraste com zonas e sinais de alerta.
4. Documentar cada origem/licença; nunca importar arquivos de jogos comerciais.
5. Medir desempenho de renderização e memória em aparelhos reais antes de expandir o atlas.

**Status:** primeira leva de sprites funcionais para protótipo, não considerada arte final da versão 1.0.

## Regra de margens de água na Alpha 3

A simulação fornece `City.waterMask(x,y)` com o mesmo mapa de bits das vias: 
`1` sudeste, `2` noroeste, `4` sudoeste e `8` nordeste. O renderizador usa
`45 + waterMask` e preserva os 45 sprites antigos para estabilidade dos índices.
Cada sprite de água suaviza apenas as bordas onde não há água adjacente.
As regiões alagadas de **novas cidades** são contínuas; os terrenos de cidades
anteriores permanecem inalterados nos arquivos de salvamento existentes.

**Atenção:** dados de saves anteriores são preservados. A construção inicial de
um `new City()` possui novo terreno, mas o aplicativo **não recria** a cidade
automaticamente ao atualizar, evitando perda de prédios e progresso. Um menu
seguro de novas cidades e slots separados deverá ser considerado num marco futuro.

## Direção visual

- Reduzir ruído de grade variando a grama em manchas 4×5, e não alternando por célula.
- Melhorar vegetação com copas arredondadas e pinheiros originais combinados.
- Manter identidade arquitetônica por zona e permitir um terceiro sprite em cada nível.
- Diminuir a área vazia fora do mapa usando fundo verde-escuro fosco na câmera.
- Manter `Nearest`, atlas RGBA, footprint e save binário inalterados.
