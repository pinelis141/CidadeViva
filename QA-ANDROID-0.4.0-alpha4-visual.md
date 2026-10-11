# QA Android — Cidade Viva 0.4.0 Alpha 4 — consistência visual e câmera

**Motivação:** vídeo da Alpha 3 mostrou prédios antigos roxos entre os novos e cidade pequena cercada por campo vazio. CI valida lógica e build, **não valida apresentação no aparelho**.

## Critérios obrigatórios

- [ ] Instalar a Alpha 4 sobre `Cidade Viva Teste` **sem desinstalar**; confirmar os 3 slots CVT3, impostos, dinheiro, dia e níveis anteriores.
- [ ] Abrir uma cidade antiga com diferentes níveis de R/C/I: nenhum prédio de aparência roxa/legada deve aparecer.
- [ ] Conferir três níveis: casa/sobrado/apartamento, quiosque/café/prédio comercial, depósito/galpão/fábrica.
- [ ] A abertura da cidade focaliza a área construída, não a estrada distante; tocar CENTRAR restaura o enquadramento do bairro.
- [ ] No Galaxy S23 Ultra ou telefone Android retrato 1080×2316, construções devem parecer maiores que na Alpha 3, sem ocupar ou tapar o HUD.
- [ ] Criar longa estrada sem prédios e tocar CENTRAR: ela não afasta a câmera do núcleo habitado.
- [ ] Mover e dar zoom por pinça até o resto do mapa e depois voltar; sem travar nos limites.
- [ ] Testar mais de uma cidade, lotes vazios, crescimento, abandono e carregamento de backup.
- [ ] Testar ferramentas, ECON, CIDADES, SALVAR, ABRIR, pausa, velocidades e inspeção de lote.
- [ ] Gravar vídeo de 30–60 s para verificar orientação, sobreposição, fluidez, nitidez e escala real.

## Escopo

A Alpha 4 não mexe no sistema de trânsito, balanço, crescimento, conteúdo 2×2 ou formato de salvamento. As nove imagens atuais e atlas 125 IDs são preservados. Apenas a regra de aparência e o enquadramento de câmera foram alterados.
