# Cidade Viva — Buildings V3 (integração efetiva 1x1)

## Objetivo
Atender ao pedido de prédios claramente diferentes entre os níveis 1, 2 e 3, sem tocar na simulação ou nos salvos de três cidades.

## Arte
- Residencial: casa modesta → sobrado → pequeno edifício de apartamentos.
- Comercial: quiosque → café/loja → prédio comercial.
- Industrial: pequeno depósito → galpão → fábrica com chaminé e equipamentos.

Os nove PNGs de 72×128 (RGBA transparente) ficam em `art/game/buildings_v3/`. Foram adaptados dos nove prédios 1x1 da folha conceitual original aprovada. A renderização usa filtro Nearest já configurado pelo libGDX.

## IDs e mapeamento

| Setor | Nível 1 | Nível 2 | Nível 3 |
|---|---:|---:|---:|
| Residencial | 116 | 117 | 118 |
| Comercial | 119 | 120 | 121 |
| Industrial | 122 | 123 | 124 |

A função `BuildingAppearance.sprite()` escolhe os novos sprites para seis das sete famílias de coordenadas (6/7); a sétima mantém uma variante legada. A escolha é determinística: a mesma construção não muda aleatoriamente a cada carregamento, apenas quando sobe ou desce de nível.

## Pipeline

1. `python3 tools/generate_pixel_atlas.py` recompõe o atlas (Pillow, apenas em desenvolvimento).
2. `python3 tools/verify_atlas.py` valida o atlas 576×2048 e os nove sprites RGBA sem Pillow.
3. `bash tools/test-core.sh` executa regressões Java e os testes de sprites.
4. CI GitHub Actions compila o APK Android.

## Compatibilidade

As imagens são aparência apenas: o CVT3 continua a armazenar tipo e nível de cada terreno. Não há novas propriedades persistidas, novas construções automáticas, ou mudança nas taxas de crescimento. Os três slots e a assinatura do APK DEV são preservados.

## Próxima etapa (NÃO incluída)

Os conceitos 2x2 não podem ser ativados por simples troca de sprite: faltam detecção de blocos, footprint, ajuste da ordem de desenho, interação com demolição/inspeção e save/reload. Esses pontos devem ser implementados e testados numa versão separada antes de qualquer construção maior aparecer no jogo.
